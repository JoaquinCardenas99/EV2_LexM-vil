package duoc.example.EV2_Desarrollo.repository

import android.content.Context
import duoc.example.EV2_Desarrollo.repository.remote.*
import retrofit2.HttpException

class AuthRepository(private val session: SessionPersistence, private val api: LexMovilApi) {
    constructor(context: Context) : this(SessionStore(context.applicationContext),
        ApiClient.api)
    suspend fun restaurar() = session.leer()?.let { token ->
        try { api.perfil("Bearer $token") }
        catch (error: HttpException) {
            if (error.code() == 401) { session.borrar(); null } else throw error
        }
    }
    suspend fun login(correo: String, password: String) = api.login(LoginRequest(correo, password)).let {
        session.guardar(it.token)
        it.usuario
    }
    suspend fun registrar(body: RegistroRequest) = api.registrar(body)
    suspend fun logout() {
        val token = session.leer()
        // No se confirma el cierre si el servidor no pudo revocar el token.
        if (token != null) api.logout("Bearer $token")
        session.borrar()
    }
    private fun authorization() = "Bearer ${session.leer() ?: error("Inicia sesión nuevamente") }"
    suspend fun favoritos() = api.favoritos(authorization())
    suspend fun agregar(id: Int) = api.agregar(authorization(), CrearFavorito(id))
    suspend fun quitar(id: Int) = api.quitar(authorization(), id)
    suspend fun nota(id: Int, nota: String) = api.nota(authorization(), id, NotaRequest(nota))
}

fun mensajeDeRed(error: Exception): String = when (error) {
    is HttpException -> when (error.code()) {
        401 -> "Sesión vencida o credenciales incorrectas. Vuelve a iniciar sesión."
        409 -> "Ya existe una cuenta con este correo"
        400 -> "Revisa los datos ingresados"
        else -> "El servidor no pudo completar la solicitud (${error.code()})"
    }
    is java.io.IOException -> "No se pudo conectar con el servidor. Revisa la conexión e inténtalo de nuevo."
    else -> "No se pudo completar la operación. Inténtalo nuevamente."
}
