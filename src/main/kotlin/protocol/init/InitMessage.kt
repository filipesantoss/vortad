package filipesantoss.vortad.protocol.init

import filipesantoss.vortad.protocol.Message
import filipesantoss.vortad.workload.Node
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
        val nodeId: String,
        @SerialName("node_ids")
        val nodeIds: Set<String>
    ) : Message.Body(Type.INIT) {
        override val inReplyTo: Int? = null
    }

    class Response(
        override val to: InitMessage
    ) : Message.Response<InitMessage, InitOkMessage>() {
        override suspend fun through(node: Node) = InitOkMessage(
            source = node.id,
            destination = to.source,
            body = InitOkMessage.Body(
                messageId = node.next(),
                inReplyTo = to.body.messageId
            )
        )
    }
}
