package duoc.example.EV2_Desarrollo.model

import android.content.Context
import android.content.SharedPreferences

data class UsuarioMem(
    val id: Long = System.currentTimeMillis(),
    val nombreCompleto: String,
    val rut: String,
    val correo: String,
    val tipo: String,
    val password: String
)

object UsuarioRepositoryMem {
    private const val PREFS_NAME = "lexmovil_prefs"
    private const val KEY_USUARIOS = "usuarios_json"
    
    private val usuarios = mutableListOf<UsuarioMem>()
    private var initialized = false
    
    fun init(context: Context) {
        if (initialized) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_USUARIOS, null)
        if (json != null) {
            try {
                // Simple parsing
                val items = json.split("|||")
                for (item in items) {
                    if (item.isNotEmpty()) {
                        val parts = item.split("###")
                        if (parts.size == 6) {
                            usuarios.add(
                                UsuarioMem(
                                    id = parts[0].toLongOrNull() ?: System.currentTimeMillis(),
                                    nombreCompleto = parts[1],
                                    rut = parts[2],
                                    correo = parts[3],
                                    tipo = parts[4],
                                    password = parts[5]
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {}
        }
        initialized = true
    }
    
    private fun save(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = usuarios.joinToString("|||") { 
            "${it.id}###${it.nombreCompleto}###${it.rut}###${it.correo}###${it.tipo}###${it.password}"
        }
        prefs.edit().putString(KEY_USUARIOS, json).apply()
    }

    fun registrar(context: Context, usuario: UsuarioMem): Long {
        usuarios.add(usuario)
        save(context)
        return usuario.id
    }

    fun obtenerPorCorreo(correo: String): UsuarioMem? {
        return usuarios.find { it.correo.lowercase() == correo.lowercase() }
    }

    fun login(correo: String, password: String): UsuarioMem? {
        return usuarios.find { it.correo.lowercase() == correo.lowercase() && it.password == password }
    }
}

