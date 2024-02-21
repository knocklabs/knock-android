package app.knock.client.models.errors

import java.lang.Exception

data class NetworkError(
    val title: String = "Error",
    val code: Int,
    val description: String
): Exception()