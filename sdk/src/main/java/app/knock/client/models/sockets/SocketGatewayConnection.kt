package app.knock.client.models.sockets

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

/** The response from `POST /v1/sockets/connect`. */
@JsonIgnoreProperties(ignoreUnknown = true)
internal data class SocketGatewayConnection(
    val mode: String,
    val url: String,
    @JsonProperty("expires_at") val expiresAt: Instant,
)
