package filipesantoss.vortad.workload.broadcast

import filipesantoss.vortad.protocol.Message
import filipesantoss.vortad.workload.Node
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * @see {https://github.com/jepsen-io/maelstrom/blob/main/doc/workloads.md#rpc-topology}
 */
@Serializable
data class TopologyMessage(
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
        @SerialName("topology")
        val topology: Map<String, Set<String>>
    ) : Message.Body(Type.TOPOLOGY) {
        @Transient
        override val inReplyTo: Int? = null
    }

    class Response(
        override val to: TopologyMessage
    ) : Message.Response<TopologyMessage, TopologyOkMessage>() {
        override fun through(node: Node) = TopologyOkMessage(
            source = node.id,
            destination = to.source,
            body = TopologyOkMessage.Body(
                messageId = node.next(),
                inReplyTo = to.body.messageId
            )
        )
    }
}
