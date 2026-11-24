package duoc.example.EV2_Desarrollo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import duoc.example.EV2_Desarrollo.repository.IndicadorRepository
import duoc.example.EV2_Desarrollo.repository.ValorUf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EstadoUf(val valor: ValorUf? = null, val cargando: Boolean = false, val error: String? = null)
class IndicadorViewModel : ViewModel() {
    private val repository = IndicadorRepository()
    private val _estado = MutableStateFlow(EstadoUf())
    val estado = _estado.asStateFlow()
    init { actualizar() }
    fun actualizar() {
        if (_estado.value.cargando) return
        _estado.value = EstadoUf(cargando = true)
        viewModelScope.launch {
            try { _estado.value = EstadoUf(valor = repository.obtenerUf()) }
            catch (error: CancellationException) { throw error }
            catch (_: Exception) { _estado.value = EstadoUf(error = "La UF no está disponible. Puedes seguir usando la app.") }
        }
    }
}
