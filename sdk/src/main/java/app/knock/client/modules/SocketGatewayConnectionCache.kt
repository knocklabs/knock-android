package app.knock.client.modules

import app.knock.client.Knock
import app.knock.client.KnockLogCategory
import app.knock.client.logDebug
import app.knock.client.models.sockets.SocketGatewayConnection

internal class SocketGatewayConnectionCache(
    private val fetch: () -> SocketGatewayConnection,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private var cached: SocketGatewayConnection? = null

    @Synchronized
    fun get(): SocketGatewayConnection {
        cached?.takeIf { it.expiresAt.toEpochMilli() - EXPIRY_PADDING_MS > now() }
            ?.let {
                Knock.shared.logDebug(
                    KnockLogCategory.FEED,
                    "Retrieved cached socket gateway connection (expires at ${it.expiresAt})"
                )
                return it
            }

        Knock.shared.logDebug(KnockLogCategory.FEED, "Fetching new socket gateway connection")
        return fetch().also { cached = it }
    }

    @Synchronized
    fun invalidate() {
        Knock.shared.logDebug(KnockLogCategory.FEED, "Invalidating cached socket gateway connection")
        cached = null
    }

    companion object {
        const val EXPIRY_PADDING_MS = 5_000L
    }
}
