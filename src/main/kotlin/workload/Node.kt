package filipesantoss.vortad.workload

import filipesantoss.vortad.protocol.Message
import filipesantoss.vortad.protocol.init.InitMessage
import filipesantoss.vortad.workload.broadcast.BroadcastMessage
import filipesantoss.vortad.workload.broadcast.TopologyMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class Node private constructor(
    val id: String,
    private var messageId: Int = 0,
    private val mutex: Mutex = Mutex(),
    private val neighbors: MutableSet<String> = mutableSetOf(),
    private val messages: MutableSet<Int> = mutableSetOf()
) {
    companion object {
        val json = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

        fun init(): Node = runBlocking {
            val data = readln()
            val init = json.decodeFromString<InitMessage>(data)

            val node = Node(init.body.nodeId)
            node.consume(data)

            node
        }
    }

    fun listen() = runBlocking {
        while (true) {
            val data = readlnOrNull()
            if (data === null) {
                break
            }

            launch(Dispatchers.Default) {
                try {
                    consume(data)
                } catch (exception: Exception) {
                    // TODO: https://github.com/jepsen-io/maelstrom/blob/main/doc/protocol.md#errors
                    debug(exception.stackTrace.toString())
                }
            }
        }
    }

    private suspend fun consume(data: String) {
        val element = json.parseToJsonElement(data)
        val body = element.jsonObject["body"]
        val type = body?.jsonObject["type"]?.jsonPrimitive?.content

        when (type?.uppercase()) {
            Message.Type.INIT.name -> InitMessage.Handler(this).accept(
                json.decodeFromString<InitMessage>(data)
            )

            Message.Type.TOPOLOGY.name -> TopologyMessage.Handler(this).accept(
                json.decodeFromString<TopologyMessage>(data)
            )

            Message.Type.BROADCAST.name -> BroadcastMessage.Handler(this).accept(
                json.decodeFromString<BroadcastMessage>(data)
            )
        }
    }

    suspend fun next() = mutex.withLock {
        ++messageId
    }

    inline fun <reified M : Message> produce(message: M) {
        println(json.encodeToString<M>(message))
    }

    suspend fun meet(neighbors: Set<String>) = mutex.withLock {
        this.neighbors.addAll(neighbors)
    }

    suspend fun accept(message: BroadcastMessage) = mutex.withLock {
        val new = this.messages.add(message.body.message)
        if (!new) {
            return@withLock
        }

        this.neighbors.forEach {
            val gossip = BroadcastMessage(
                source = this.id,
                destination = it,
                body = message.body
            )

            this.produce(gossip)
        }
    }

    private fun debug(value: String) {
        System.err.println(value)
    }
}