package filipesantoss.vortad.protocol

import filipesantoss.vortad.protocol.init.InitMessage
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
    private val mutex: Mutex = Mutex()
) {
    companion object {
        val json = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

        val handlers = mapOf(
            Message.Type.INIT to InitMessage.Handler()
        )

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
                consume(data)
            }
        }
    }

    private suspend fun consume(data: String) {
        val element = json.parseToJsonElement(data)
        val body = element.jsonObject["body"]
        val type = body?.jsonObject["type"]?.jsonPrimitive?.content

        when (type?.uppercase()) {
            Message.Type.INIT.name -> handlers[Message.Type.INIT]?.accept(
                this,
                json.decodeFromString<InitMessage>(data)
            )

            else -> debug(data)
        }
    }

    inline fun <reified M : Message> produce(message: M) {
        println(json.encodeToString<M>(message))
    }

    suspend fun next() = mutex.withLock {
        ++messageId
    }

    private fun debug(value: String) {
        System.err.println(value)
    }
}