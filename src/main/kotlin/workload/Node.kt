package filipesantoss.vortad.workload

import filipesantoss.vortad.protocol.Message
import filipesantoss.vortad.protocol.init.InitMessage
import filipesantoss.vortad.workload.broadcast.BroadcastMessage
import filipesantoss.vortad.workload.broadcast.GossipMessage
import filipesantoss.vortad.workload.broadcast.ReadMessage
import filipesantoss.vortad.workload.broadcast.TopologyMessage
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Duration
import kotlin.concurrent.thread

class Node private constructor(
    val id: String,
    private var uuid: Int = 0,
    private val known: MutableSet<Int> = mutableSetOf(),
    private val mill: MutableMap<String, MutableSet<Int>> = mutableMapOf()
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

    fun run() {
        thread {
            propagate()
        }

        listen()
    }

    private tailrec fun listen() {
        val data = readlnOrNull()
        if (data != null) {
            consume(data)
        }

        listen()
    }

    @Synchronized
    private fun consume(data: String) {
        val body = json.parseToJsonElement(data).jsonObject["body"]
        val type = body?.jsonObject["type"]?.jsonPrimitive?.content

        when (type?.uppercase()) {
            Message.Type.INIT.name -> {
                val input = json.decodeFromString<InitMessage>(data)
                val output = InitMessage.Response(input).through(this)
                produce(output)
            }

            Message.Type.TOPOLOGY.name -> {
                val input = json.decodeFromString<TopologyMessage>(data)
                val neighbors = input.body.topology[id] as Set<String>
                mill.putAll(neighbors.map { it to mutableSetOf() })
                val output = TopologyMessage.Response(input).through(this)
                produce(output)
            }

            Message.Type.BROADCAST.name -> {
                val input = json.decodeFromString<BroadcastMessage>(data)
                val message = input.body.message
                known.add(message)
                val output = BroadcastMessage.Response(input).through(this)
                produce(output)
            }

            Message.Type.GOSSIP.name -> {
                val input = json.decodeFromString<GossipMessage>(data)
                val messages = input.body.messages
                known.addAll(messages)
                mill[input.source]!!.addAll(messages)
            }

            Message.Type.READ.name -> {
                val input = json.decodeFromString<ReadMessage>(data)
                val output = ReadMessage.Response(input).through(this)
                produce(output)
            }
        }
    }

    fun next() = ++uuid

    fun getMessages(): Set<Int> = known.toSet()

    private inline fun <reified M : Message> produce(message: M) {
        println(json.encodeToString<M>(message))
    }

    private tailrec fun propagate() {
        Thread.sleep(Duration.ofMillis(100))

        for (neighbor in mill.keys) {
            share(neighbor)
        }

        propagate()
    }

    @Synchronized
    private fun share(neighbor: String) {
        val message = gossip(neighbor)

        if (message.body.messages.isNotEmpty()) {
            produce(message)
        }
    }

    private fun gossip(destination: String): GossipMessage = GossipMessage(
        source = id,
        destination = destination,
        body = GossipMessage.Body(
            messages = known subtract mill[destination]!!
        )
    )
}
