package duoc.example.EV2_Desarrollo.repository.remote

import duoc.example.EV2_Desarrollo.model.Usuario
import retrofit2.http.*

data class LoginRequest(val correo: String, val password: String)
data class RegistroRequest(val nombreCompleto: String, val rut: String, val correo: String, val tipo: String, val password: String)
data class SesionResponse(val token: String, val usuario: Usuario)
data class FavoritoRemoto(val leyId: Int, val nota: String)
data class CrearFavorito(val leyId: Int)
data class NotaRequest(val nota: String)

interface LexMovilApi {
    @GET("laws") suspend fun leyes(): List<duoc.example.EV2_Desarrollo.model.Ley>
    @GET("laws/{id}") suspend fun ley(@Path("id") id: Int): duoc.example.EV2_Desarrollo.model.Ley
    @POST("auth/register") suspend fun registrar(@Body body: RegistroRequest): Usuario
    @POST("auth/login") suspend fun login(@Body body: LoginRequest): SesionResponse
    @GET("auth/me") suspend fun perfil(@Header("Authorization") token: String): Usuario
    @POST("auth/logout") suspend fun logout(@Header("Authorization") token: String)
    @GET("favorites") suspend fun favoritos(@Header("Authorization") token: String): List<FavoritoRemoto>
    @POST("favorites") suspend fun agregar(@Header("Authorization") token: String, @Body body: CrearFavorito)
    @PUT("favorites/{id}") suspend fun nota(@Header("Authorization") token: String, @Path("id") id: Int, @Body body: NotaRequest)
    @DELETE("favorites/{id}") suspend fun quitar(@Header("Authorization") token: String, @Path("id") id: Int)
}
