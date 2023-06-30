import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class Data(
    val email: String,
    val message: String,
)

interface ApiService {
    @POST("/send-data")
    fun postData(@Body data: Data): Call<Data>
}
