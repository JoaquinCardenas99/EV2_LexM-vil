package duoc.example.EV2_Desarrollo.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import duoc.example.EV2_Desarrollo.model.UsuarioMem
import duoc.example.EV2_Desarrollo.model.UsuarioRepositoryMem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val _usuarioLogueado = MutableStateFlow<UsuarioMem?>(null)
    val usuarioLogueado: StateFlow<UsuarioMem?> = _usuarioLogueado.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()
    
    init {
        UsuarioRepositoryMem.init(application.applicationContext)
        cargarSesion(application.applicationContext)
    }
    
    private fun cargarSesion(context: Context) {
        val prefs = context.getSharedPreferences("lexmovil_session", Context.MODE_PRIVATE)
        val correo = prefs.getString("correo", null)
        val password = prefs.getString("password", null)
        if (correo != null && password != null) {
            val user = UsuarioRepositoryMem.login(correo, password)
            if (user != null) {
                _usuarioLogueado.value = user
            }
        }
    }
    
    private fun guardarSesion(context: Context, correo: String, password: String) {
        val prefs = context.getSharedPreferences("lexmovil_session", Context.MODE_PRIVATE)
        prefs.edit().putString("correo", correo).putString("password", password).apply()
    }
    
    private fun borrarSesion(context: Context) {
        val prefs = context.getSharedPreferences("lexmovil_session", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    fun login(correo: String, password: String) {
        viewModelScope.launch {
            val user = UsuarioRepositoryMem.login(correo.trim(), password)
            if (user != null) {
                _usuarioLogueado.value = user
                guardarSesion(getApplication(), correo.trim(), password)
                _mensaje.value = "Bienvenido/a ${user.nombreCompleto}"
            } else {
                _mensaje.value = "Correo o contraseña incorrectos"
            }
        }
    }

    fun registrar(
        nombreCompleto: String,
        rut: String,
        correo: String,
        tipo: String,
        password: String,
        passwordConfirm: String
    ) {
        val correoTrim = correo.trim()
        val pass = password.trim()
        val passConf = passwordConfirm.trim()

        // Validaciones
        if (nombreCompleto.isBlank() || rut.isBlank() || correoTrim.isBlank()) {
            _mensaje.value = "Completa todos los campos obligatorios"
            return
        }
        if (!correoTrim.contains("@") || !correoTrim.contains(".")) {
            _mensaje.value = "Correo electrónico inválido"
            return
        }
        if (pass != passConf) {
            _mensaje.value = "Las contraseñas no coinciden"
            return
        }
        // Debe contener mayúscula, número y carácter especial
        val tieneMayus = pass.any { it.isUpperCase() }
        val tieneNumero = pass.any { it.isDigit() }
        val tieneEspecial = pass.any { !it.isLetterOrDigit() }
        if (pass.length < 8) {
            _mensaje.value = "La contraseña debe tener al menos 8 caracteres"
            return
        }
        if (!tieneMayus || !tieneNumero || !tieneEspecial) {
            _mensaje.value = "La contraseña debe contener al menos una mayúscula, un número y un carácter especial"
            return
        }

        viewModelScope.launch {
            val existente = UsuarioRepositoryMem.obtenerPorCorreo(correoTrim)
            if (existente != null) {
                _mensaje.value = "Ya existe una cuenta con este correo"
                return@launch
            }
            val usuario = UsuarioMem(
                nombreCompleto = nombreCompleto.trim(),
                rut = rut.trim(),
                correo = correoTrim,
                tipo = tipo,
                password = pass
            )
            val res = UsuarioRepositoryMem.registrar(getApplication(), usuario)
            if (res > 0) {
                _mensaje.value = "Registro exitoso. Ahora puedes iniciar sesión"
            } else {
                _mensaje.value = "Error al registrar"
            }
        }
    }

    fun cerrarSesion() {
        borrarSesion(getApplication())
        _usuarioLogueado.value = null
    }
}
