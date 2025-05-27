package filipesantoss.vortad.workload.broadcast

import filipesantoss.vortad.protocol.Message
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @see {https://github.com/jepsen-io/maelstrom/blob/main/doc/workloads.md#rpc-broadcast}
 */
@Serializable
data class BroadcastOkMessage(
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
        @SerialName("in_reply_to")
        override val inReplyTo: Int,
    ) : Message.Body(Type.BROADCAST_OK)
}
