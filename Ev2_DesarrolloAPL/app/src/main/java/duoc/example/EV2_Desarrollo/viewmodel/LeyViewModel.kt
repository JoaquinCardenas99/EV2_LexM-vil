package duoc.example.EV2_Desarrollo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import duoc.example.EV2_Desarrollo.repository.AuthRepository
import duoc.example.EV2_Desarrollo.repository.mensajeDeRed
import duoc.example.EV2_Desarrollo.model.Ley
import duoc.example.EV2_Desarrollo.repository.LeyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LeyViewModel(application: Application) : AndroidViewModel(application) {
    private val remoto = AuthRepository(application)
    private val _notas = MutableStateFlow<Map<Int, String>>(emptyMap())
    val notas = _notas.asStateFlow()
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje = _mensaje.asStateFlow()
    private val _ocupado = MutableStateFlow(false)
    val ocupado = _ocupado.asStateFlow()
    private var solicitud: Job? = null
    private var versionSesion = 0
    private fun operacion(block: suspend () -> Unit) {
        if (_ocupado.value) return
        _ocupado.value = true
        _mensaje.value = null
        val versionActual = versionSesion
        solicitud = viewModelScope.launch {
            try { block() }
            catch (error: CancellationException) { throw error }
            catch (error: Exception) { _mensaje.value = mensajeDeRed(error) }
            finally { if (versionActual == versionSesion) _ocupado.value = false }
        }
    }
    private suspend fun actualizarFavoritos() {
        val datos = remoto.favoritos()
        _favoritos.value = datos.map { it.leyId }.toSet()
        _notas.value = datos.associate { it.leyId to it.nota }
    }
    fun cargarFavoritos() = operacion {
        repository.actualizar()
        _leyes.value = repository.buscar(_busqueda.value)
        actualizarFavoritos()
    }
    fun cargarCatalogo() = operacion {
        repository.actualizar()
        _leyes.value = repository.buscar(_busqueda.value)
    }
    fun limpiarFavoritos() {
        // Una respuesta pendiente de la cuenta anterior no debe repoblar el estado.
        versionSesion++
        solicitud?.cancel()
        _ocupado.value = false
        _favoritos.value = emptySet()
        _notas.value = emptyMap()
        _mensaje.value = null
    }
    fun guardarNota(id: Int, nota: String) = operacion {
        remoto.nota(id, nota)
        actualizarFavoritos()
        _mensaje.value = "Nota guardada"
    }
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
        _leySeleccionada.value = null
        // Espera la carga inicial para no descartar un toque rápido en el catálogo.
        val cargaPendiente = solicitud
        viewModelScope.launch {
            cargaPendiente?.join()
            operacion { _leySeleccionada.value = repository.detalle(id) }
        }
    }


    fun toggleFavorito(id: Int) = operacion {
        if (_favoritos.value.contains(id)) remoto.quitar(id) else remoto.agregar(id)
        actualizarFavoritos()
    }


    fun obtenerFavoritos(): List<Ley> {
        return repository.obtenerTodas().filter { _favoritos.value.contains(it.id) }
    }


}
