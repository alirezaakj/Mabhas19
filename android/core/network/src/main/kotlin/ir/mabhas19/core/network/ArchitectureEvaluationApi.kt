package ir.mabhas19.core.network

import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

/** A04–A14 public calculations. No project save, session bootstrap or member mutation endpoints. */
interface ArchitectureEvaluationApi {
    /** Body must contain request=evaluate, never save. */
    @POST("api/arch_walls.php")
    suspend fun walls(@Body body: JsonObject): JsonObject

    @POST("api/roof_floor.php")
    suspend fun roofFloor(@Body body: JsonObject): JsonObject

    @POST("api/arch_glazing.php")
    suspend fun glazing(@Body body: JsonObject): JsonObject

    @POST("api/arch_shading.php")
    suspend fun shading(@Body body: JsonObject): JsonObject

    @POST("api/arch_daylight.php")
    suspend fun daylight(@Body body: JsonObject): JsonObject

    @POST("api/arch_airtightness.php")
    suspend fun airtightness(@Body body: JsonObject): JsonObject

    @POST("api/arch_thermal_bridges.php")
    suspend fun thermalBridges(@Body body: JsonObject): JsonObject

    @POST("api/arch_boundaries.php")
    suspend fun boundaries(@Body body: JsonObject): JsonObject
}

object ArchitectureEvaluationApiFactory {
    const val BASE_URL = "https://www.mabhas19.ir/"

    /** An injectable base URL supports MockWebServer; ordinary TLS certificate validation applies. */
    fun create(baseUrl: String = BASE_URL): ArchitectureEvaluationApi {
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
            .create(ArchitectureEvaluationApi::class.java)
    }
}
