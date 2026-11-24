package duoc.example.EV2_Desarrollo.repository

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

data class ValorUf(val fecha: String, val valor: Double)
data class RespuestaUf(val serie: List<ValorUf>)
interface IndicadorApi { @GET("api/uf") suspend fun uf(): RespuestaUf }

/** Fuente independiente del backend propio, consultada exclusivamente por HTTPS. */
class IndicadorRepository(private val api: IndicadorApi = Retrofit.Builder()
    .baseUrl("https://mindicador.cl/")
    .client(OkHttpClient.Builder().callTimeout(12, TimeUnit.SECONDS).build())
    .addConverterFactory(GsonConverterFactory.create()).build().create(IndicadorApi::class.java)) {
    suspend fun obtenerUf(): ValorUf = api.uf().serie.firstOrNull()
        ?.takeIf { it.valor.isFinite() && it.valor > 0 && it.fecha.length >= 10 }
        ?: error("La fuente no entregó un valor de UF válido")
}
