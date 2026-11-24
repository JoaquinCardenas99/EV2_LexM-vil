package duoc.example.EV2_Desarrollo.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import duoc.example.EV2_Desarrollo.model.Ley
import duoc.example.EV2_Desarrollo.repository.DocumentoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DocumentoViewModel(application: Application, private val estado: SavedStateHandle) : AndroidViewModel(application) {
    private val repository = DocumentoRepository(application)
    private val _imagen = MutableStateFlow<Bitmap?>(null)
    val imagen = _imagen.asStateFlow()
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje = _mensaje.asStateFlow()
    private val _ocupado = MutableStateFlow(false)
    val ocupado = _ocupado.asStateFlow()
    private fun origen(): Uri? = estado.get<String>("captura")?.let(Uri::parse)
    init { if (estado.get<Boolean>("capturaLista") == true) cargarCaptura() }
    fun informar(texto: String) { _mensaje.value = texto }
    fun prepararCaptura(): Uri? = try {
        repository.crearCaptura().also {
            estado["captura"] = it.toString()
            estado["capturaLista"] = false
            _imagen.value = null
            _mensaje.value = null
        }
    } catch (_: Exception) { informar("No se pudo preparar la cámara"); null }
    private fun tarea(exito: String?, bloque: () -> Unit) {
        if (_ocupado.value) return
        _ocupado.value = true
        _mensaje.value = null
        viewModelScope.launch {
            try { withContext(Dispatchers.IO) { bloque() }; _mensaje.value = exito }
            catch (error: CancellationException) { throw error }
            catch (_: Exception) { _mensaje.value = "No se pudo completar la operación. Revisa el archivo o el espacio disponible." }
            finally { _ocupado.value = false }
        }
    }
    fun cargarCaptura() {
        val uri = origen() ?: return
        estado["capturaLista"] = true
        tarea(null) { _imagen.value = repository.vistaPrevia(uri) }
    }
    fun guardarFoto(destino: Uri) {
        val uri = origen() ?: return
        tarea("Fotografía guardada en la ubicación elegida") { repository.guardarFoto(uri, destino) }
    }
    fun guardarPdf(destino: Uri, ley: Ley) = tarea("PDF guardado en la ubicación elegida") {
        repository.guardarPdf(destino, ley)
    }
}
