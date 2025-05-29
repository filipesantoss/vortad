package filipesantoss.vortad.workload.broadcast

import filipesantoss.vortad.protocol.Message
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class GossipMessage(
    @SerialName("src")
    override val source: String,
    @SerialName("dest")
    override val destination: String,
    @SerialName("body")
    override val body: Body
) : Message() {
    @Serializable
    data class Body(
        @SerialName("messages")
        val messages: Set<Int>
    ) : Message.Body(Type.GOSSIP) {
        @Transient
        override val messageId: Int? = null

        @Transient
        override val inReplyTo: Int? = null
    }
}
