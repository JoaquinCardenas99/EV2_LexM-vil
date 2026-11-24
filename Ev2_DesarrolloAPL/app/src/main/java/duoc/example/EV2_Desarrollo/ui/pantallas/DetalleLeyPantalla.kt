package duoc.example.EV2_Desarrollo.ui.pantallas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import duoc.example.EV2_Desarrollo.viewmodel.LeyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleLeyPantalla(
    leyId: Int,
    viewModel: LeyViewModel,
    onVolver: () -> Unit
) {
    val ley by viewModel.leySeleccionada.collectAsState()
    val favoritos by viewModel.favoritos.collectAsState()
    val notas by viewModel.notas.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val ocupado by viewModel.ocupado.collectAsState()
    var nota by remember(leyId, notas[leyId]) { mutableStateOf(notas[leyId].orEmpty()) }
    val documentos: duoc.example.EV2_Desarrollo.viewmodel.DocumentoViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val mensajeDocumento by documentos.mensaje.collectAsState()
    val guardando by documentos.ocupado.collectAsState()
    val guardarPdf = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri -> if (uri != null) ley?.let { documentos.guardarPdf(uri, it) } }

    LaunchedEffect(leyId) {
        viewModel.seleccionarLey(leyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ley?.codigo ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (ley != null) {
                        IconButton(enabled = !ocupado, onClick = { viewModel.toggleFavorito(ley!!.id) }) {
                            Icon(
                                if (favoritos.contains(ley!!.id)) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (favoritos.contains(ley!!.id)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            mensajeDocumento?.let { Text(it) }
            mensaje?.let {
                Text(it)
                Button(enabled = !ocupado, onClick = { viewModel.seleccionarLey(leyId) }) { Text("Reintentar") }
            }
            if (ocupado) androidx.compose.material3.LinearProgressIndicator(Modifier.fillMaxWidth())
            if (leyId in favoritos) {
                OutlinedTextField(value = nota, onValueChange = { if (it.length <= 2000) nota = it },
                    label = { Text("Nota personal del favorito") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                Button(enabled = !ocupado, onClick = { viewModel.guardarNota(leyId, nota) }) { Text("Guardar nota") }
            }
            ley?.let {
                Text(
                    text = it.titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Categoría: ${it.categoria}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it.descripcion,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    enabled = !guardando,
                    onClick = { guardarPdf.launch("${it.codigo.replace(" ", "_")}.pdf") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Download, contentDescription = null)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Descargar en PDF")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Artículos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(it.articulos) { articulo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Artículo ${articulo.numero}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = articulo.texto,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
