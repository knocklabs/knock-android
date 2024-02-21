package app.knock.client.services

import app.knock.client.Knock
import app.knock.client.KnockEnvironment
import app.knock.client.KnockLogger
import app.knock.client.log
import app.knock.client.models.networking.HTTPMethod
import app.knock.client.models.networking.URLQueryItem
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal open class KnockAPIService {
    private val httpClient = OkHttpClient()
    val mapper = jacksonObjectMapper()

    private val environmentBaseUrl: String
        get() = Knock.environment.getBaseUrl()

    private val apiBasePath: String
        get() = "$environmentBaseUrl/v1"

    private val userToken: String?
        get() = Knock.environment.getUserToken()

    private val publishableKey: String
        get() = Knock.environment.getPublishableKey()

    private val clientVersion: String
        get() = KnockEnvironment.appVersion

    init {
        mapper.propertyNamingStrategy = SNAKE_CASE
        mapper.registerModule(JavaTimeModule())
        mapper.registerKotlinModule()
    }

    suspend inline fun <reified T: Any> get(path: String, queryItems: List<URLQueryItem>? = null): Result<T> {
        val result = makeGeneralRequest(HTTPMethod.GET, path, queryItems)
        return decodeData(result)
    }

    suspend inline fun <reified T: Any> post(path: String, body: Any?): Result<T> {
        val result = makeGeneralRequest(HTTPMethod.POST, path, body = body)
        return decodeData(result)
    }

    suspend inline fun <reified T: Any> put(path: String, body: Any?): Result<T> {
        val result = makeGeneralRequest(HTTPMethod.PUT, path, body = body)
        return decodeData(result)
    }

    suspend inline fun <reified T: Any> delete(path: String, body: Any? = null): Result<T> {
        val result = makeGeneralRequest(HTTPMethod.DELETE, path, body = body)
        return decodeData(result)
    }

    inline fun <reified T: Any> decodeData(result: Result<String>): Result<T> {
        return result.fold(
            onSuccess = {
                try {
                    Result.success(mapper.readValue(it))
                } catch (e: Exception) {
                    Knock.log(KnockLogger.LogType.ERROR, KnockLogger.LogCategory.NETWORKING, "Failed to decode object")
                    Result.failure(e)
                }
            },
            onFailure = {
                Result.failure(it)
            }
        )
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

    // RequestBody? type
//    private suspend fun makeGeneralRequest(method: HTTPMethod, path: String, queryItems: List<URLQueryItem>?, body: Any?): Result<String> {
//        var url = URL("$apiBasePath$path").toHttpUrlOrNull()!!
//
//        queryItems?.let {
//            var urlBuilder = url.newBuilder()
//            for (item in queryItems) {
//                item.value?.let { value ->
//                    urlBuilder = urlBuilder.addQueryParameter(item.name, "$value")
//                }
//            }
//            url = urlBuilder.build()
//        }
//
//        var builder = Request.Builder()
//            .addHeader("Authorization", "Bearer $publishableKey")
//            .addHeader("User-Agent", "knock-kotlin@$clientVersion")
//            .addHeader("Content-Type","application/json")
//            .url(url)
//
//        userToken?.let {
//            builder = builder.addHeader("X-Knock-User-Token", it)
//        }
//
//        builder = addMethod(builder, method, body)
//
//        val request = builder.build()
//
//        httpClient.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Result.failure<IOException>(e)
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                response.use {
//                    if (!response.isSuccessful) {
//                        Result.failure<IOException>(IOException("Unexpected code $response"))
//                    }
//
//                    try {
//                        Result.success(response.body!!.string())
//                    } catch (e: Exception) {
//                        Result.failure(e)
//                    }
//                }
//            }
//        })
//    }

    suspend fun makeGeneralRequest(
        method: HTTPMethod,
        path: String,
        queryItems: List<URLQueryItem>? = null,
        body: Any? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        suspendCoroutine { continuation ->

            var url = URL("$apiBasePath$path").toHttpUrlOrNull()!!

            queryItems?.let {
                var urlBuilder = url.newBuilder()
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
                .url(url)

            userToken?.let {
                builder = builder.addHeader("X-Knock-User-Token", it)
            }

            builder = addMethod(builder, method, body)

            val request = builder.build()

            // Execute the request
            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resume(Result.failure(e))
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        continuation.resume(Result.failure(IOException("Unexpected code $response")))
                        return
                    }

                    response.body?.string()?.let {
                        continuation.resume(Result.success(it))
                    } ?: continuation.resume(Result.failure(IOException("Null Response Body")))
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