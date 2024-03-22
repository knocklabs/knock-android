package app.knock.client.services

import app.knock.client.Knock
import app.knock.client.KnockEnvironment
import app.knock.client.KnockLogCategory
import app.knock.client.logError
import app.knock.client.logNetworking
import app.knock.client.models.KnockException
import app.knock.client.models.networking.HTTPMethod
import app.knock.client.models.networking.URLQueryItem
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
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.net.URL
import kotlin.coroutines.resume
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
            Knock.shared.logError(KnockLogCategory.NETWORKING, "Failed to decode object: $typeName", exception = e, additionalInfo = mapOf("RAW_JSON" to result))
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
        suspendCancellableCoroutine { continuation ->
            val urlString = "$apiBasePath$path"
            var url = URL(urlString).toHttpUrlOrNull()
            if (url == null) {
                val exception = KnockException.NetworkError("Invalid URL", 400, "Invalid URL: $urlString")
                Knock.shared.logNetworking("Invalid URL: $urlString", exception = exception)
                continuation.resumeWithException(exception)
                return@suspendCancellableCoroutine
            }

            queryItems?.let {
                var urlBuilder = url!!.newBuilder()
                for (item in queryItems) {
                    item.value?.let { value ->
                        urlBuilder = urlBuilder.addQueryParameter(item.name, "$value")
                    }
                }
                url = urlBuilder.build()
            }

            var builder = Request.Builder()
                .addHeader("Authorization", "Bearer $publishableKey")
                .addHeader("User-Agent", "knock-kotlin@$clientVersion")
                .addHeader("Content-Type","application/json")
                .url(url!!)

            userToken?.let {
                builder = builder.addHeader("X-Knock-User-Token", it)
            }

            builder = addMethod(builder, method, body)

            val request = builder.build()

            // Execute the request
            Knock.shared.httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (!continuation.isCancelled) {
                        continuation.resumeWithException(e)
                    }
                    return
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        val e = KnockException.NetworkError(code = response.code, description = "Api request failure", response = response, request = request)
                        Knock.shared.logNetworking("Api request failure", request = call.request(), response = response, exception = e)
                        if (!continuation.isCancelled) {
                            continuation.resumeWithException(e)
                        }
                        return
                    }

                    response.body?.string()?.let {
                        Knock.shared.logNetworking("Api request successful", request = call.request(), response = response)
                        if (!continuation.isCancelled) {
                            continuation.resume(it)
                        }
                    } ?: {
                        val e = KnockException.NetworkError(code = response.code, description = "Null Response Body", response = response, request = request)
                        Knock.shared.logNetworking("Api request failure", request = call.request(), response = response, exception = e)
                        continuation.resumeWithException(e)
                    }
                }
            })
        }
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