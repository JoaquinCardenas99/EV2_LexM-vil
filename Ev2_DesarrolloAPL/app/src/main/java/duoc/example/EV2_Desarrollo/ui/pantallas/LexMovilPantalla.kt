package duoc.example.EV2_Desarrollo.ui.pantallas

import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import duoc.example.EV2_Desarrollo.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LexMovilPantalla(
    authViewModel: AuthViewModel,
    onIrListaLeyes: () -> Unit,
    onIrBusqueda: () -> Unit,
    onIrFavoritos: () -> Unit,
    onIrAcercaDe: () -> Unit,
    onIrCamara: () -> Unit
) {
    val usuario by authViewModel.usuarioLogueado.collectAsState()
    val indicador: duoc.example.EV2_Desarrollo.viewmodel.IndicadorViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val uf by indicador.estado.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LexMovil") },
                actions = {
                    IconButton(onClick = {
                        authViewModel.cerrarSesion()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Estudio y consulta de material jurídico de ejemplo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    usuario?.let {
                        Text(text = "Usuario: ${it.nombreCompleto}")
                        Text(text = "Tipo: ${it.tipo} | ${it.correo}")
                    }
                }
            }

            Card(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("UF de referencia", fontWeight = FontWeight.Bold)
                    Text("Consulta para estudiar montos expresados en UF.")
                    if (uf.cargando) Text("Consultando…")
                    uf.valor?.let { valor ->
                        val formato = java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("es-CL"))
                        formato.minimumFractionDigits = 2
                        formato.maximumFractionDigits = 2
                        Text("$${formato.format(valor.valor)} CLP · ${valor.fecha.take(10)}")
                        Text("Fuente: mindicador.cl", style = MaterialTheme.typography.bodySmall)
                    }
                    uf.error?.let { Text(it) }
                    androidx.compose.material3.TextButton(enabled = !uf.cargando, onClick = { indicador.actualizar() }) { Text("Actualizar UF") }
                }
            }
            Button(
                onClick = onIrListaLeyes,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Book, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Consultar Leyes y Códigos")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onIrBusqueda,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Search, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Búsqueda Avanzada")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onIrFavoritos,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Favorite, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Mis Favoritos")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onIrAcercaDe,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Acerca de LexMovil")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onIrCamara,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Fotografiar documento")
                }
            }
        }
    }
}

