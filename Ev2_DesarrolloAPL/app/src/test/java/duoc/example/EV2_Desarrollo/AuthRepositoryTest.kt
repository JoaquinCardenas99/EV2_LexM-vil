package duoc.example.EV2_Desarrollo

import duoc.example.EV2_Desarrollo.repository.AuthRepository
import duoc.example.EV2_Desarrollo.repository.SessionPersistence
import duoc.example.EV2_Desarrollo.repository.remote.LexMovilApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.HttpException
import retrofit2.converter.gson.GsonConverterFactory

class AuthRepositoryTest {
    private val server = MockWebServer()
    private val session = object : SessionPersistence {
        var token: String? = null
        override fun guardar(token: String) { this.token = token }
        override fun leer() = token
        override fun borrar() { token = null }
    }
    private lateinit var repository: AuthRepository
    @Before fun preparar() {
        server.start()
        repository = AuthRepository(session, Retrofit.Builder().baseUrl(server.url("/api/"))
            .addConverterFactory(GsonConverterFactory.create()).build().create(LexMovilApi::class.java))
    }
    @After fun cerrar() { server.shutdown() }
    private val usuario = """{"id":"1","nombreCompleto":"Nombre Apellido","rut":"12.345.678-5","correo":"test@example.com","tipo":"Estudiante"}"""
    @Test fun loginGuardaTokenYNoPassword() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"token":"token-servidor","usuario":$usuario}"""))
        assertEquals("1", repository.login("test@example.com", "Clave123!").id)
        assertEquals("token-servidor", session.token)
        assertEquals("/api/auth/login", server.takeRequest().path)
    }
    @Test fun restauraSesionConTokenGuardado() = runBlocking {
        session.token = "persistido"
        server.enqueue(MockResponse().setBody(usuario))
        assertEquals("1", repository.restaurar()?.id)
        assertEquals("Bearer persistido", server.takeRequest().getHeader("Authorization"))
    }
    @Test fun sesionVencidaEliminaToken() = runBlocking {
        session.token = "vencido"
        server.enqueue(MockResponse().setResponseCode(401))
        assertNull(repository.restaurar())
        assertNull(session.token)
    }
    @Test fun errorDeServidorConservaSesionParaReintentar() = runBlocking {
        session.token = "vigente"
        server.enqueue(MockResponse().setResponseCode(503))
        try { repository.restaurar(); fail("Debió fallar") } catch (_: HttpException) { }
        assertEquals("vigente", session.token)
    }
    @Test fun logoutRevocaYBorraToken() = runBlocking {
        session.token = "vigente"
        server.enqueue(MockResponse().setResponseCode(204))
        repository.logout()
        assertNull(session.token)
        assertEquals("/api/auth/logout", server.takeRequest().path)
    }
    @Test fun sinTokenNoConsultaServidor() = runBlocking {
        assertNull(repository.restaurar())
        assertEquals(0, server.requestCount)
    }
}
