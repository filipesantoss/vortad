package filipesantoss.vortad.workload.broadcast

import filipesantoss.vortad.protocol.Message
import filipesantoss.vortad.workload.Node
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
        override val inReplyTo: Int? = null
    }

    class Handler(override val node: Node) : Message.Handler<TopologyMessage>() {
        override suspend fun accept(message: TopologyMessage) {
            val response = TopologyOkMessage(
                source = node.id,
                destination = message.source,
                body = TopologyOkMessage.Body(
                    messageId = node.next(),
                    inReplyTo = message.body.messageId
                )
            )

            node.produce(response)

            val neighbors = message.body.topology[node.id]
            node.meet(neighbors as Set<String>)
        }
    }
}