package app.knock.client.services

import app.knock.client.Knock
import app.knock.client.KnockEnvironment
import app.knock.client.KnockLogCategory
import app.knock.client.logDebug
import app.knock.client.logError
import app.knock.client.logNetworking
import app.knock.client.models.KnockException
import app.knock.client.models.networking.HTTPMethod
import app.knock.client.models.networking.URLQueryItem
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resumeWithException

internal open class KnockAPIService {
    val mapper = jacksonObjectMapper()

    private val environmentBaseUrl: String
        get() = Knock.shared.environment.getBaseUrl()

    private val apiBasePath: String
        get() = "$environmentBaseUrl/v1"

    private val userToken: String?
        get() = Knock.shared.environment.getUserToken()

    private val publishableKey: String
        get() = Knock.shared.environment.getPublishableKey()

    private val clientVersion: String
        get() = KnockEnvironment.clientVersion

    init {
        mapper.propertyNamingStrategy = SNAKE_CASE
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
        mapper.registerModule(JavaTimeModule())
        mapper.registerKotlinModule()
    }

    suspend inline fun <reified T: Any> get(path: String, queryItems: List<URLQueryItem>? = null): T {
        val result = makeGeneralRequest(HTTPMethod.GET, path, queryItems)
        return decodeData(result)
    }

    suspend inline fun <reified T: Any> post(path: String, body: Any? = null): T {
        val result = makeGeneralRequest(HTTPMethod.POST, path, body = body)
        return decodeData(result)
    }

    suspend inline fun <reified T: Any> put(path: String, body: Any? = null): T {
        val result = makeGeneralRequest(HTTPMethod.PUT, path, body = body)
        return decodeData(result)
    }

    suspend inline fun <reified T: Any> delete(path: String, body: Any? = null): T {
        val result = makeGeneralRequest(HTTPMethod.DELETE, path, body = body)
        return decodeData(result)
    }

    inline fun <reified T: Any> decodeData(result: String): T {
        return try {
            mapper.readValue(result)
        } catch (e: Exception) {
            val typeName = T::class.java.simpleName
            Knock.shared.logError(KnockLogCategory.NETWORKING, "Failed to decode object: $typeName", exception = e)
            Knock.shared.logDebug(KnockLogCategory.NETWORKING, "Failed to decode object: $typeName", additionalInfo = mapOf("RAW_JSON" to result))
            throw KnockException.DecodingError(typeName)
        }
    }

    /**
     * Helper to get the serialized value,
     * for instance in path parameters that have to be converted to `String`.
     *
     * One example is an `Enum` that should be transformed to its serialized value as specified by a `@JsonProperty`
     *
     * @param value the value to be serialized
     * @return the `String` representation
     */
    fun serializeValueAsString(value: Any): String {
        return mapper.valueToTree<TextNode>(value).textValue()
    }

    suspend fun makeGeneralRequest(
        method: HTTPMethod,
        path: String,
        queryItems: List<URLQueryItem>? = null,
        body: Any? = null
    ): String = withContext(Dispatchers.IO) {
        val urlString = "$apiBasePath$path"
        val baseUrl = urlString.toHttpUrlOrNull() ?: run {
            val exception = KnockException.NetworkError("Invalid URL", 400, "Invalid URL: $urlString")
            Knock.shared.logNetworking("Invalid URL: $urlString", exception = exception)
            throw exception
        }

        val url = baseUrl.newBuilder().apply {
            queryItems?.forEach { item ->
                item.value?.let { value -> addQueryParameter(item.name, "$value") }
            }
        }.build()

        var builder = Request.Builder()
            .addHeader("Authorization", "Bearer $publishableKey")
            .addHeader("User-Agent", "knock-kotlin@$clientVersion")
            .addHeader("Content-Type","application/json")
            .url(url)

        userToken?.let {
            builder = builder.addHeader("X-Knock-User-Token", it)
        }

        val request = addMethod(builder, method, body).build()
        val call = Knock.shared.httpClient.newCall(request)

        suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { call.cancel() }

            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                // Anything thrown from here would be swallowed by OkHttp and leave the caller suspended forever,
                // so every outcome (including body read failures) must resume the continuation.
                override fun onResponse(call: Call, response: Response) {
                    continuation.resumeWith(runCatching { readResponse(request, response) })
                }
            })
        }
    }

    private fun readResponse(request: Request, response: Response): String = response.use {
        val bodyString = it.body?.string()

        if (!it.isSuccessful) {
            // Keep the error body readable for callers after the underlying response is closed.
            val bufferedResponse = it.newBuilder()
                .body(bodyString?.toResponseBody(it.body?.contentType()))
                .build()
            val e = KnockException.NetworkError(code = it.code, description = "Api request failure", response = bufferedResponse, request = request)
            Knock.shared.logNetworking("Api request failure", request = request, response = it, exception = e)
            throw e
        }

        if (bodyString == null) {
            val e = KnockException.NetworkError(code = it.code, description = "Null Response Body", response = it, request = request)
            Knock.shared.logNetworking("Api request failure", request = request, response = it, exception = e)
            throw e
        }

        Knock.shared.logNetworking("Api request successful", request = request, response = it)
        bodyString
    }

    private fun addMethod(builder: Request.Builder, method: HTTPMethod, body: Any?): Request.Builder {
        return when (method) {
            HTTPMethod.GET -> builder.get()
            HTTPMethod.POST -> builder.post(mapper.writeValueAsString(body).toRequestBody())
            HTTPMethod.PUT -> builder.put(mapper.writeValueAsString(body).toRequestBody())
            HTTPMethod.DELETE -> builder.delete(mapper.writeValueAsString(body).toRequestBody())
        }
    }
}

/**
 * Percent-encodes a value for use as a single URL path segment, so IDs containing `/`, `?`, `#`, spaces etc.
 * address the intended resource.
 */
internal fun String.asPathSegment(): String =
    HttpUrl.Builder().scheme("https").host("localhost").addPathSegment(this).build().encodedPath.removePrefix("/")
