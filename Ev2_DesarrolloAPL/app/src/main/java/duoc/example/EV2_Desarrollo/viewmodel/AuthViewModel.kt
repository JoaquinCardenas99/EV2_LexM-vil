package duoc.example.EV2_Desarrollo.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import duoc.example.EV2_Desarrollo.model.ErroresFormulario
import duoc.example.EV2_Desarrollo.model.Usuario
import duoc.example.EV2_Desarrollo.repository.AuthRepository
import duoc.example.EV2_Desarrollo.repository.mensajeDeRed
import duoc.example.EV2_Desarrollo.repository.remote.RegistroRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val _usuarioLogueado = MutableStateFlow<Usuario?>(null)
    val usuarioLogueado: StateFlow<Usuario?> = _usuarioLogueado.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private val _errores = MutableStateFlow(ErroresFormulario())
    val errores: StateFlow<ErroresFormulario> = _errores.asStateFlow()
    
    private val repository = AuthRepository(application)
    private val _restaurando = MutableStateFlow(true)
    val restaurando = _restaurando.asStateFlow()
    private val _errorSesion = MutableStateFlow<String?>(null)
    val errorSesion = _errorSesion.asStateFlow()
    private val _ocupado = MutableStateFlow(false)
    val ocupado = _ocupado.asStateFlow()

    init { restaurarSesion() }

    fun restaurarSesion() {
        _restaurando.value = true
        _errorSesion.value = null
        viewModelScope.launch {
            try { _usuarioLogueado.value = repository.restaurar() }
            catch (error: CancellationException) { throw error }
            catch (error: Exception) { _errorSesion.value = mensajeDeRed(error) }
            finally { _restaurando.value = false }
        }
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

        if (_ocupado.value) return
        _ocupado.value = true
        viewModelScope.launch {
            try {
                _usuarioLogueado.value = repository.login(correo.trim(), password)
                _mensaje.value = null
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) { _mensaje.value = mensajeDeRed(error) }
            finally { _ocupado.value = false }
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
        val pass = password
        val passConf = passwordConfirm

        val erroresRegistro = validarRegistro(nombreCompleto, rut, correoTrim, pass, passConf)
        _errores.value = erroresRegistro
        if (erroresRegistro.hayErrores()) return

        if (_ocupado.value) return
        _ocupado.value = true
        viewModelScope.launch {
            try {
                repository.registrar(RegistroRequest(nombreCompleto.trim(), rut.trim(), correoTrim, tipo, pass))
                _mensaje.value = "Registro exitoso. Ahora puedes iniciar sesión"
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) { _mensaje.value = mensajeDeRed(error) }
            finally { _ocupado.value = false }
        }
    }

    fun cerrarSesion() {
        if (_ocupado.value) return
        _ocupado.value = true
        viewModelScope.launch {
            try { repository.logout(); _usuarioLogueado.value = null; _mensaje.value = null }
            catch (error: CancellationException) { throw error }
            catch (error: Exception) { _mensaje.value = mensajeDeRed(error) }
            finally { _ocupado.value = false }
        }
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