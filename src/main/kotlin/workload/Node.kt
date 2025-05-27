package filipesantoss.vortad.workload

import filipesantoss.vortad.protocol.Message
import filipesantoss.vortad.protocol.init.InitMessage
import filipesantoss.vortad.workload.broadcast.BroadcastMessage
import filipesantoss.vortad.workload.broadcast.BroadcastOkMessage
import filipesantoss.vortad.workload.broadcast.ReadMessage
import filipesantoss.vortad.workload.broadcast.TopologyMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Duration.Companion.milliseconds

class Node private constructor(
    val id: String,
    private var messageId: Int = 0,
    private val mutex: Mutex = Mutex(),
    private val neighbors: MutableSet<String> = mutableSetOf(),
    private val received: MutableSet<Int> = mutableSetOf(),
    private val delivering: MutableSet<Int> = mutableSetOf()
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
            if (data == null) {
                break
            }

            launch(Dispatchers.Default) {
                consume(data)
            }
        }
    }

    private suspend fun consume(data: String) {
        val element = json.parseToJsonElement(data)
        val body = element.jsonObject["body"]
        val type = body?.jsonObject["type"]?.jsonPrimitive?.content

        when (type?.uppercase()) {
            Message.Type.INIT.name -> {
                val input = json.decodeFromString<InitMessage>(data)
                val output = InitMessage.Response(input).through(this)
                produce(output)
                meet(input.body.nodeIds)
            }

            Message.Type.TOPOLOGY.name -> {
                val input = json.decodeFromString<TopologyMessage>(data)
                val output = TopologyMessage.Response(input).through(this)
                produce(output)
            }

            Message.Type.BROADCAST.name -> {
                val input = json.decodeFromString<BroadcastMessage>(data)
                val output = BroadcastMessage.Response(input).through(this)
                produce(output)
                ping(input)
            }

            Message.Type.BROADCAST_OK.name -> {
                val input = json.decodeFromString<BroadcastOkMessage>(data)
                pong(input)
            }

            Message.Type.READ.name -> {
                val input = json.decodeFromString<ReadMessage>(data)
                val output = ReadMessage.Response(input).through(this)
                produce(output)
            }
        }
    }

    suspend fun next() = mutex.withLock {
        ++messageId
    }

    fun getMessages(): Set<Int> = received.toSet()

    private inline fun <reified M : Message> produce(message: M) {
        println(json.encodeToString<M>(message))
    }

    private suspend fun meet(neighbors: Set<String>) = mutex.withLock {
        this.neighbors.addAll(neighbors)
    }

    private suspend fun ping(message: BroadcastMessage) {
        val new = mutex.withLock {
            received.add(message.body.message)
        }

        if (new) {
            spread(message)
        }
    }

    private suspend fun pong(message: BroadcastOkMessage) = mutex.withLock {
        delivering.remove(message.body.inReplyTo)
    }

    private suspend fun spread(message: BroadcastMessage) {
        neighbors.forEach {
            if (it == message.source) {
                return
            }

            val new = copy(message, it)

            mutex.withLock {
                delivering.add(new.body.messageId)
            }

            deliver(new)
        }
    }

    private suspend fun copy(message: BroadcastMessage, to: String): BroadcastMessage = BroadcastMessage(
        source = id,
        destination = to,
        body = BroadcastMessage.Body(
            messageId = next(),
            message = message.body.message
        )
    )

    private suspend fun deliver(message: BroadcastMessage) {
        while (true) {
            mutex.withLock {
                if (message.body.messageId !in delivering) {
                    return
                }

                produce(message)
                delay(5.milliseconds)
            }
        }
    }
}
