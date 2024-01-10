package app.knock.client

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.net.URL

data class URLQueryItem(
    val name: String,
    val value: Any? = null
)

enum class HTTPMethod {
    GET,
    POST,
    PUT,
    DELETE
}

class KnockAPI(
    private var publishableKey: String,
    private var userToken: String? = null,
    hostname: String? = null
) {
    private val httpClient = OkHttpClient()
    val mapper = jacksonObjectMapper()
    var hostname = "https://api.knock.app"
    private val apiBasePath: String
        get() = "$hostname/v1"
    private val clientVersion: String
        get() = "0.1.0"

    init {
        if(hostname != null) {
            this.hostname = hostname
        }

        mapper.propertyNamingStrategy = SNAKE_CASE
        mapper.registerModule(JavaTimeModule())
        mapper.registerKotlinModule()
    }

    inline fun <reified T: Any> decodeFromGet(path: String, queryItems: List<URLQueryItem>?, crossinline handler: (Result<T>) -> Unit) {
        get(path, queryItems) { result ->
            decodeData(result, handler)
        }
    }

    inline fun <reified T: Any> decodeFromPost(path: String, body: Any?, crossinline handler: (Result<T>) -> Unit) {
        post(path, body) { result ->
            decodeData(result, handler)
        }
    }

    inline fun <reified T: Any> decodeFromPut(path: String, body: Any?, crossinline handler: (Result<T>) -> Unit) {
        put(path, body) { result ->
            decodeData(result, handler)
        }
    }

    inline fun <reified T: Any> decodeFromDelete(path: String, body: Any?, crossinline handler: (Result<T>) -> Unit) {
        delete(path, body) { result ->
            decodeData(result, handler)
        }
    }

    inline fun <reified T : Any> decodeData(result: Result<String>, crossinline handler: (Result<T>) -> Unit) {
        result.fold(
            onFailure = {
                handler(Result.failure(it))
            },
            onSuccess = {
                try {
                    val decodedObject: T = mapper.readValue(it)
                    handler(Result.success(decodedObject))
                } catch (e: Exception) {
                    handler(Result.failure(e))
                }
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

    fun get(path: String, queryItems: List<URLQueryItem>?, handler: (Result<String>) -> Unit) {
        makeGeneralRequest(HTTPMethod.GET, path, queryItems, null, handler)
    }

    fun post(path: String, body: Any?, handler: (Result<String>) -> Unit) {
        makeGeneralRequest(HTTPMethod.POST, path, null, body, handler)
    }

    fun put(path: String, body: Any?, handler: (Result<String>) -> Unit) {
        makeGeneralRequest(HTTPMethod.PUT, path, null, body, handler)
    }

    fun delete(path: String, body: Any?, handler: (Result<String>) -> Unit) {
        makeGeneralRequest(HTTPMethod.DELETE, path, null, body, handler)
    }

    // RequestBody? type
    private fun makeGeneralRequest(method: HTTPMethod, path: String, queryItems: List<URLQueryItem>?, body: Any?, callback: (Result<String>) -> Unit) {
        var url = URL("$apiBasePath$path").toHttpUrlOrNull()!!

        if (queryItems != null) {
            var urlBuilder = url.newBuilder()
            for (item in queryItems) {
                val optionalValue = item.value
                if (optionalValue != null) {
                    urlBuilder = urlBuilder.addQueryParameter(item.name, "$optionalValue")
                }

            }
            url = urlBuilder.build()
        }

        var builder = Request.Builder()
            .addHeader("Authorization", "Bearer $publishableKey")
            .addHeader("User-Agent", "knock-kotlin@$clientVersion")
            .addHeader("Content-Type","application/json")
            .url(url)

        if (userToken != null) {
            builder = builder.addHeader("X-Knock-User-Token", userToken!!)
        }

        builder = addMethod(builder, method, body)

        val request = builder.build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback(Result.failure(IOException("Unexpected code $response")))
                        return
                    }

                    try {
                        callback(Result.success(response.body!!.string()))
                    }
                    catch (e: Exception) {
                        callback(Result.failure(e))
                    }
                }
            }
        })
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