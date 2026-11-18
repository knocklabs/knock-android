package app.knock.client.modules

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * OkHttp interceptor that rewrites the WebSocket upgrade request to target the socket connect URL returned by the
 * socket gateway API endpoint (`POST /v1/sockets/connect`).
 */
internal class SocketGatewayInterceptor(private val cache: SocketGatewayConnectionCache) :
    Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // The original request to wss://api.knock.app/ws/v1/websocket
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        try {
            val cachedGatewayConnection = cache.get()

            // HttpUrl only accepts http(s) protocols, so convert the gateway's ws or wss protocol
            val gatewayConnectUrl = cachedGatewayConnection.url
                .replaceFirst(Regex("^ws", RegexOption.IGNORE_CASE), "http")
                .toHttpUrl()

            val mergedUrl = gatewayConnectUrl.newBuilder().apply {
                // Copy query params from the original socket connect URL, unless the gateway URL already sets them.
                // The original URL passed in by Phoenix contains some query params we want to preserve, including vsn
                // (protocol version to send on connect), api_key, and user_token.
                for (name in originalUrl.queryParameterNames - gatewayConnectUrl.queryParameterNames) {
                    for (value in originalUrl.queryParameterValues(name)) {
                        addQueryParameter(name, value)
                    }
                }
            }.build()

            val overriddenRequest = originalRequest.newBuilder().url(mergedUrl).build()
            val response = chain.proceed(overriddenRequest)

            if (response.code != 101) {
                invalidateUnlessCanceled(chain)
            }

            return response
        } catch (e: IOException) {
            invalidateUnlessCanceled(chain)
            throw e
        } catch (e: Exception) {
            invalidateUnlessCanceled(chain)
            throw IOException("Socket gateway connection failed", e)
        }
    }

    private fun invalidateUnlessCanceled(chain: Interceptor.Chain) {
        if (!chain.call().isCanceled()) {
            cache.invalidate()
        }
    }
}
