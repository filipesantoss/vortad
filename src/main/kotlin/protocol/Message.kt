package filipesantoss.vortad.protocol

import filipesantoss.vortad.workload.Node
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @see {https://github.com/jepsen-io/maelstrom/blob/main/doc/protocol.md#messages}
 */
@Serializable
abstract class Message {
    abstract val source: String
    abstract val destination: String
    abstract val body: Body

    @Serializable
    abstract class Body(
        @SerialName("type")
        val type: Type
    ) {
        abstract val messageId: Int
        abstract val inReplyTo: Int?
    }

    @Serializable
    enum class Type {
        @SerialName("init")
        INIT,

        @SerialName("init_ok")
        INIT_OK,

        @SerialName("topology")
        TOPOLOGY,

        @SerialName("topology_ok")
        TOPOLOGY_OK,

        @SerialName("broadcast")
        BROADCAST,

        @SerialName("broadcast_ok")
        BROADCAST_OK,

        @SerialName("read")
        READ,

        @SerialName("read_ok")
        READ_OK,
    }

    abstract class Response<I : Message, O : Message> {
        abstract val to: I

        abstract suspend fun through(node: Node): O
    }
}
