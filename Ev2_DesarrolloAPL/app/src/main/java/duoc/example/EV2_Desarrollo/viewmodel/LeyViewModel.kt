package duoc.example.EV2_Desarrollo.viewmodel

import androidx.lifecycle.ViewModel
import duoc.example.EV2_Desarrollo.model.Ley
import duoc.example.EV2_Desarrollo.repository.LeyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LeyViewModel : ViewModel() {
    private val repository = LeyRepository()

    private val _leyes = MutableStateFlow(repository.obtenerTodas())
    val leyes: StateFlow<List<Ley>> = _leyes.asStateFlow()

    private val _leySeleccionada = MutableStateFlow<Ley?>(null)
    val leySeleccionada: StateFlow<Ley?> = _leySeleccionada.asStateFlow()

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    private val _favoritos = MutableStateFlow<Set<Int>>(emptySet())
    val favoritos: StateFlow<Set<Int>> = _favoritos.asStateFlow()

    fun actualizarBusqueda(texto: String) {
        _busqueda.value = texto
        if (texto.isBlank()) {
            _leyes.value = repository.obtenerTodas()
        } else {
            _leyes.value = repository.buscar(texto)
        }
    }

    fun seleccionarLey(id: Int) {
        _leySeleccionada.value = repository.obtenerPorId(id)
    }

    fun limpiarSeleccion() {
        _leySeleccionada.value = null
    }

    fun toggleFavorito(id: Int) {
        val actual = _favoritos.value.toMutableSet()
        if (actual.contains(id)) {
            actual.remove(id)
        } else {
            actual.add(id)
        }
        _favoritos.value = actual
    }

    fun esFavorito(id: Int): Boolean = _favoritos.value.contains(id)

    fun obtenerFavoritos(): List<Ley> {
        return repository.obtenerTodas().filter { _favoritos.value.contains(it.id) }
    }

    fun filtrarPorCategoria(categoria: String) {
        _leyes.value = repository.obtenerPorCategoria(categoria)
    }

    fun mostrarTodas() {
        _busqueda.value = ""
        _leyes.value = repository.obtenerTodas()
    }
}
