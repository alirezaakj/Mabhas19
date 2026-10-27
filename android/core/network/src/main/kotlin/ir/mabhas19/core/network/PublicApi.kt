package ir.mabhas19.core.network

import java.util.concurrent.TimeUnit
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET

@Serializable
data class ReferenceResponse(
    val ok: Boolean,
    val reference: JsonObject? = null,
    val error: String? = null
)

/** A01 only. Member authentication/mutations are deliberately not invented. */
interface PublicApi {
    @GET("api/reference.php")
    suspend fun reference(): ReferenceResponse
}

object PublicApiFactory {
    const val BASE_URL = "https://mabhas19.ir/"

    fun create(baseUrl: String = BASE_URL): PublicApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(false)
            .followSslRedirects(false)
            .build()
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PublicApi::class.java)
    }
}
