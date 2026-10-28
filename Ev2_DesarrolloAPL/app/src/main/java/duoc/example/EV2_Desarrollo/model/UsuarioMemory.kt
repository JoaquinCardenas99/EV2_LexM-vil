package duoc.example.EV2_Desarrollo.model

data class UsuarioMem(
    val id: Long = System.currentTimeMillis(),
    val nombreCompleto: String,
    val rut: String,
    val correo: String,
    val tipo: String,
    val password: String
)

object UsuarioRepositoryMem {
    private val usuarios = mutableListOf<UsuarioMem>()

    fun registrar(usuario: UsuarioMem): Long {
        usuarios.add(usuario)
        return usuario.id
    }

    fun obtenerPorCorreo(correo: String): UsuarioMem? {
        return usuarios.find { it.correo.lowercase() == correo.lowercase() }
    }

    fun login(correo: String, password: String): UsuarioMem? {
        return usuarios.find { it.correo.lowercase() == correo.lowercase() && it.password == password }
    }
}
