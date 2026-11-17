package app.knock.client.modules

import app.knock.client.models.sockets.SocketGatewayConnection

internal class SocketGatewayConnectionCache(
    private val fetch: () -> SocketGatewayConnection,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private var cached: SocketGatewayConnection? = null

    @Synchronized
    fun get(): SocketGatewayConnection {
        cached?.takeIf { it.expiresAt.toEpochMilli() - EXPIRY_PADDING_MS > now() }
            ?.let { return it }
        return fetch().also { cached = it }
    }

    @Synchronized
    fun invalidate() {
        cached = null
    }

    companion object {
        const val EXPIRY_PADDING_MS = 5_000L
    }
}
