package duoc.example.EV2_Desarrollo.repository

import duoc.example.EV2_Desarrollo.model.Ley
import duoc.example.EV2_Desarrollo.repository.remote.ApiClient
import duoc.example.EV2_Desarrollo.repository.remote.LexMovilApi

/** Mantiene la última respuesta para filtrar sin una solicitud por cada tecla. */
class LeyRepository(private val api: LexMovilApi = ApiClient.api) {
    private var leyes: List<Ley> = emptyList()
    suspend fun actualizar(): List<Ley> = api.leyes().also { leyes = it }
    suspend fun detalle(id: Int): Ley = api.ley(id)
    fun obtenerTodas(): List<Ley> = leyes
    fun buscar(query: String): List<Ley> {
        val texto = query.trim()
        return leyes.filter { ley ->
            listOf(ley.titulo, ley.codigo, ley.categoria, ley.descripcion)
                .any { it.contains(texto, ignoreCase = true) }
        }
    }
}
