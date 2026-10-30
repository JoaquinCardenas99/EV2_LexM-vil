package duoc.example.EV2_Desarrollo.viewmodel

import android.app.Application
import android.content.Context
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import duoc.example.EV2_Desarrollo.model.ErroresFormulario
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

    private val _errores = MutableStateFlow(ErroresFormulario())
    val errores: StateFlow<ErroresFormulario> = _errores.asStateFlow()
    
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

    fun limpiarErrores() {
        _errores.value = ErroresFormulario()
    }

    fun login(correo: String, password: String) {
        val erroresLogin = ErroresFormulario(
            correo = if (!correoValido(correo.trim())) "Correo electrónico inválido" else null,
            password = if (password.isBlank()) "Ingresa tu contraseña" else null
        )
        _errores.value = erroresLogin
        if (erroresLogin.hayErrores()) return

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

        val erroresRegistro = validarRegistro(nombreCompleto, rut, correoTrim, pass, passConf)
        _errores.value = erroresRegistro
        if (erroresRegistro.hayErrores()) return

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

    private fun validarRegistro(
        nombreCompleto: String,
        rut: String,
        correo: String,
        password: String,
        passwordConfirm: String
    ): ErroresFormulario {
        val palabrasNombre = nombreCompleto.trim().split(" ").filter { it.isNotBlank() }
        return ErroresFormulario(
            nombre = if (palabrasNombre.size < 2) "Ingresa tu nombre y apellido" else null,
            rut = if (!rutValido(rut)) "RUT invalido (ej: 12.345.678-5)" else null,
            correo = if (!correoValido(correo)) "Correo electrónico invalido" else null,
            password = validarPassword(password),
            confirmacion = if (password != passwordConfirm) "Las contraseñas no coinciden" else null
        )
    }

    private fun correoValido(correo: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(correo).matches()

    private fun validarPassword(password: String): String? = when {
        password.length < 8 -> "Debe tener al menos 8 caracteres"
        password.none { it.isUpperCase() } -> "Debe incluir al menos una mayúscula"
        password.none { it.isDigit() } -> "Debe incluir al menos un número"
        password.all { it.isLetterOrDigit() } -> "Debe incluir al menos un carácter especial"
        else -> null
    }

    // Valida el RUT chileno con el algoritmo de módulo 11
    private fun rutValido(rut: String): Boolean {
        val limpio = rut.replace(".", "").replace("-", "").trim().uppercase()
        if (limpio.length < 8 || limpio.length > 9) return false
        val cuerpo = limpio.dropLast(1)
        val digitoVerificador = limpio.last()
        if (!cuerpo.all { it.isDigit() }) return false

        var suma = 0
        var multiplicador = 2
        for (digito in cuerpo.reversed()) {
            suma += digito.digitToInt() * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        val resto = 11 - (suma % 11)
        val esperado = when (resto) {
            11 -> '0'
            10 -> 'K'
            else -> resto.digitToChar()
        }
        return digitoVerificador == esperado
    }
}