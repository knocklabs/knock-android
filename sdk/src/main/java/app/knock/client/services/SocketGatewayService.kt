package app.knock.client.services

import app.knock.client.models.sockets.SocketGatewayConnection
import app.knock.client.models.sockets.UnsupportedSocketModeException

internal class SocketGatewayService : KnockAPIService() {
    /**
     * Requests a websocket connection from the socket gateway API endpoint (`POST /v1/sockets/connect`). This function
     * makes a blocking request on purpose; it is called from an OkHttp interceptor that is already running on a
     * dispatcher thread.
     */
    fun fetchSocketGatewayConnection(): SocketGatewayConnection {
        val connection =
            postBlocking<SocketGatewayConnection>("/sockets/connect", emptyMap<String, Any>())
        return validate(connection)
    }

    fun validate(connection: SocketGatewayConnection): SocketGatewayConnection {
        if (connection.mode != SUPPORTED_MODE) {
            throw UnsupportedSocketModeException(connection.mode)
        }
        return connection
    }

    private companion object {
        const val SUPPORTED_MODE = "websocket"
    }
}
