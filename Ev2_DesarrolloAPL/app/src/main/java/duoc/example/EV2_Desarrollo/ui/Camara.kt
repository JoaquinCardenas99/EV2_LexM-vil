package duoc.example.EV2_Desarrollo.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import duoc.example.EV2_Desarrollo.viewmodel.DocumentoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CamaraFotos(onVolver: () -> Unit, viewModel: DocumentoViewModel = viewModel()) {
    val imagen by viewModel.imagen.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val ocupado by viewModel.ocupado.collectAsState()
    val guardar = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/jpeg")) { uri ->
        uri?.let(viewModel::guardarFoto)
    }
    val capturar = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        if (exito) viewModel.cargarCaptura() else viewModel.informar("Captura cancelada")
    }
    val permiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) viewModel.prepararCaptura()?.let { uri ->
            try { capturar.launch(uri) }
            catch (_: Exception) { viewModel.informar("No hay una aplicación de cámara disponible") }
        } else viewModel.informar("Permiso de cámara denegado. Puedes volver a solicitarlo o habilitarlo en Ajustes.")
    }
    Scaffold(topBar = {
        TopAppBar(title = { Text("Fotografiar documento") }, navigationIcon = {
            IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Toma una fotografía y elige dónde guardarla.")
            imagen?.let { Image(it.asImageBitmap(), "Vista previa del documento", Modifier.weight(1f).fillMaxWidth()) }
            mensaje?.let { Text(it) }
            if (ocupado) LinearProgressIndicator(Modifier.fillMaxWidth())
            Button(enabled = !ocupado, onClick = { permiso.launch(Manifest.permission.CAMERA) }) { Text("Tomar fotografía") }
            if (imagen != null) OutlinedButton(enabled = !ocupado, onClick = { guardar.launch("Documento_${System.currentTimeMillis()}.jpg") }) {
                Text("Guardar fotografía")
            }
        }
    }
}
