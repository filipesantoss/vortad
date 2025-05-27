package filipesantoss.vortad.workload.broadcast

import filipesantoss.vortad.protocol.Message
import filipesantoss.vortad.workload.Node
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReadMessage(
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
    ) : Message.Body(Type.READ) {
        override val inReplyTo: Int? = null
    }

    class Handler(override val node: Node) : Message.Handler<ReadMessage>() {
        override suspend fun accept(message: ReadMessage) {
            val response = ReadOkMessage(
                source = node.id,
                destination = message.source,
                body = ReadOkMessage.Body(
                    messageId = node.next(),
                    inReplyTo = message.body.messageId,
                    messages = node.getMessages()
                )
            )

            node.produce(response)
        }
    }
}
