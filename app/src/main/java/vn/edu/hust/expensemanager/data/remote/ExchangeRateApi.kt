package vn.edu.hust.expensemanager.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Model JSON trả về từ API tỷ giá miễn phí https://open.er-api.com
 * Ví dụ: GET /v6/latest/USD
 * {
 *   "result": "success",
 *   "base_code": "USD",
 *   "time_last_update_utc": "Tue, 27 Oct 2026 00:02:31 +0000",
 *   "rates": { "VND": 26300.0, "EUR": 0.92, ... }
 * }
 */
data class ExchangeRateResponse(
    @SerializedName("result") val result: String,
    @SerializedName("base_code") val baseCode: String,
    @SerializedName("time_last_update_utc") val lastUpdate: String?,
    @SerializedName("rates") val rates: Map<String, Double>
)

/** Interface Retrofit: mỗi hàm tương ứng một endpoint REST. */
interface ExchangeRateApi {
    @GET("v6/latest/{base}")
    suspend fun getLatest(@Path("base") base: String): ExchangeRateResponse
}

/** Khởi tạo Retrofit một lần duy nhất. */
object RetrofitClient {
    private const val BASE_URL = "https://open.er-api.com/"

    val api: ExchangeRateApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExchangeRateApi::class.java)
    }
}
