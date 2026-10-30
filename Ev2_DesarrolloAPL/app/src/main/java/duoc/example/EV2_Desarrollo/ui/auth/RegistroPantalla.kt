package duoc.example.EV2_Desarrollo.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import duoc.example.EV2_Desarrollo.ui.componentes.CampoTexto
import duoc.example.EV2_Desarrollo.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroPantalla(
    viewModel: AuthViewModel,
    onVolver: () -> Unit,
    onRegistroExitoso: () -> Unit
) {
    var nombreCompleto by remember { mutableStateOf("") }
    var rut by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Estudiante") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }

    val mensaje by viewModel.mensaje.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val opciones = listOf("Estudiante", "Abogado")
    val errores by viewModel.errores.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.limpiarErrores()
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            if (it.contains("exitoso", ignoreCase = true)) {
                onRegistroExitoso()
            }
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrarse") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            CampoTexto(
                valor = nombreCompleto,
                alCambiar = { nombreCompleto = it },
                etiqueta = "Nombre completo",
                icono = Icons.Filled.Person,
                error = errores.nombre
            )
            Spacer(modifier = Modifier.height(8.dp))
            CampoTexto(
                valor = rut,
                alCambiar = { rut = it },
                etiqueta = "RUT",
                icono = Icons.Filled.Badge,
                error = errores.rut
            )
            Spacer(modifier = Modifier.height(8.dp))
            CampoTexto(
                valor = correo,
                alCambiar = { correo = it },
                etiqueta = "Correo electrónico",
                icono = Icons.Filled.Email,
                error = errores.correo
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Tipo de usuario", modifier = Modifier.align(Alignment.Start))
            opciones.forEach { opcion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (tipo == opcion),
                            onClick = { tipo = opcion },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (tipo == opcion),
                        onClick = null
                    )
                    Text(
                        text = opcion,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            CampoTexto(
                valor = password,
                alCambiar = { password = it },
                etiqueta = "Contraseña",
                icono = Icons.Filled.Lock,
                error = errores.password,
                esPassword = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            CampoTexto(
                valor = passwordConfirm,
                alCambiar = { passwordConfirm = it },
                etiqueta = "Confirmar contraseña",
                icono = Icons.Filled.Lock,
                error = errores.confirmacion,
                esPassword = true
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    viewModel.registrar(
                        nombreCompleto = nombreCompleto,
                        rut = rut,
                        correo = correo,
                        tipo = tipo,
                        password = password,
                        passwordConfirm = passwordConfirm
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrarse")
            }
        }
    }
}
