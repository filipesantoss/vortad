package filipesantoss.vortad.workload.broadcast

import filipesantoss.vortad.protocol.Message
import filipesantoss.vortad.workload.Node
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * @see {https://github.com/jepsen-io/maelstrom/blob/main/doc/workloads.md#rpc-broadcast}
 */
@Serializable
data class BroadcastMessage(
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
        @SerialName("message")
        val message: Int
    ) : Message.Body(Type.BROADCAST) {
        @Transient
        override val inReplyTo: Int? = null
    }

    class Response(
        override val to: BroadcastMessage
    ) : Message.Response<BroadcastMessage, BroadcastOkMessage>() {
        override suspend fun through(node: Node) = BroadcastOkMessage(
            source = node.id,
            destination = to.source,
            body = BroadcastOkMessage.Body(
                messageId = node.next(),
                inReplyTo = to.body.messageId
            )
        )
    }
}
