package filipesantoss.vortad.protocol.init

import filipesantoss.vortad.protocol.Message
import filipesantoss.vortad.protocol.Node
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @see {https://github.com/jepsen-io/maelstrom/blob/main/doc/protocol.md#initialization}
 */
@Serializable
data class InitMessage(
    @SerialName("src")
    override val source: String,
    @SerialName("dest")
    override val destination: String,
    @SerialName("body")
    override val body: Body
) : Message() {
    @Serializable
    data class Body(
        @SerialName("msg_id")
        override val messageId: Int,
        @SerialName("node_id")
        val nodeId: String
    ) : Message.Body(Type.INIT) {
        override val inReplyTo: Int? = null
    }

    class Handler : Message.Handler<InitMessage> {
        override suspend fun accept(node: Node, message: InitMessage) {
            val response = InitOkMessage(
                source = node.id,
                destination = message.source,
                body = InitOkMessage.Body(
                    messageId = node.next(),
                    inReplyTo = message.body.messageId
                )
            )

            node.produce(response)
        }
    }
}