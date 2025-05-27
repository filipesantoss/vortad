package filipesantoss.vortad.workload.broadcast

import filipesantoss.vortad.protocol.Message
import filipesantoss.vortad.workload.Node
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
        @Transient
        override val inReplyTo: Int? = null
    }

    class Response(
        override val to: ReadMessage
    ) : Message.Response<ReadMessage, ReadOkMessage>() {
        override suspend fun through(node: Node) = ReadOkMessage(
            source = node.id,
            destination = to.source,
            body = ReadOkMessage.Body(
                messageId = node.next(),
                inReplyTo = to.body.messageId,
                messages = node.getMessages()
            )
        )
    }
}
