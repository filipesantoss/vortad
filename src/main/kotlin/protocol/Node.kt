package filipesantoss.vortad.protocol

class Node {
    fun listen() {
        while (true) {
            val input = readlnOrNull()
            if (input === null) {
                break
            }

            debug(input)
        }
    }

    private fun debug(value: String) {
        System.err.println(value)
    }
}