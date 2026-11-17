package app.knock.client.models.sockets

import java.io.IOException

internal class UnsupportedSocketModeException(val mode: String?) :
    IOException("Unsupported socket mode: $mode")
