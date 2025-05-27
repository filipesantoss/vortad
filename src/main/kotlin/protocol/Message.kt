package filipesantoss.vortad.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @see {https://github.com/jepsen-io/maelstrom/blob/main/doc/protocol.md#messages}
 */
@Serializable
abstract class Message {
    abstract val source: String
    abstract val destination: String
    abstract val body: Body

    @Serializable
    abstract class Body(
        @SerialName("type")
        val type: Type
    ) {
        abstract val messageId: Int
        abstract val inReplyTo: Int?
    }

    @Serializable
    enum class Type {
        @SerialName("init")
        INIT,

        @SerialName("init_ok")
        INIT_OK,
    }

    interface Handler<M : Message> {
        suspend fun accept(node: Node, message: M)
    }
}
