package filipesantoss.vortad.workload.broadcast

import filipesantoss.vortad.protocol.Message
import filipesantoss.vortad.workload.Node
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    ) : Message.Body(Type.TOPOLOGY) {
        override val inReplyTo: Int? = null
    }

    class Handler(override val node: Node) : Message.Handler<BroadcastMessage>() {
        override suspend fun accept(message: BroadcastMessage) {
            val response = BroadcastOkMessage(
                source = node.id,
                destination = message.source,
                body = BroadcastOkMessage.Body(
                    messageId = node.next(),
                    inReplyTo = message.body.messageId
                )
            )

            node.produce(response)
            node.accept(message)
        }
    }
}
