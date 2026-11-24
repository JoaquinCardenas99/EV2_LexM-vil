package duoc.example.EV2_Desarrollo

import duoc.example.EV2_Desarrollo.repository.LeyRepository
import duoc.example.EV2_Desarrollo.repository.IndicadorRepository
import duoc.example.EV2_Desarrollo.repository.IndicadorApi
import duoc.example.EV2_Desarrollo.repository.remote.LexMovilApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Test
import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CatalogoRepositoryTest {
    private val server = MockWebServer().apply { start() }
    private val retrofit = Retrofit.Builder().baseUrl(server.url("/"))
        .addConverterFactory(GsonConverterFactory.create()).build()
    @After fun cerrar() { server.shutdown() }
    private val ley = """{"id":1,"codigo":"CIVIL","titulo":"Derecho civil","categoria":"Civil","descripcion":"Ejemplo","articulos":[]}"""
    @Test fun consultaCatalogoYFiltraSinMasSolicitudes() = runBlocking {
        server.enqueue(MockResponse().setBody("[$ley]"))
        val repository = LeyRepository(retrofit.create(LexMovilApi::class.java))
        assertEquals(1, repository.actualizar().size)
        assertEquals("/laws", server.takeRequest().path)
        assertEquals(1, repository.buscar("  CIVIL ").size)
        assertTrue(repository.buscar("inexistente").isEmpty())
        assertEquals(1, server.requestCount)
    }
    @Test fun consultaDetallePorIdentificador() = runBlocking {
        server.enqueue(MockResponse().setBody(ley))
        assertEquals(1, LeyRepository(retrofit.create(LexMovilApi::class.java)).detalle(1).id)
        assertEquals("/laws/1", server.takeRequest().path)
    }
    @Test fun consultaUfExterna() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"serie":[{"fecha":"2026-11-23T03:00:00Z","valor":40000.5}]}"""))
        assertEquals(40000.5, IndicadorRepository(retrofit.create(IndicadorApi::class.java)).obtenerUf().valor, 0.01)
        assertEquals("/api/uf", server.takeRequest().path)
    }
    @Test fun ufVaciaNoSeMuestraComoCero() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"serie":[]}"""))
        try { IndicadorRepository(retrofit.create(IndicadorApi::class.java)).obtenerUf(); fail("Debe rechazar una serie vacía") }
        catch (_: IllegalStateException) { }
    }
}
