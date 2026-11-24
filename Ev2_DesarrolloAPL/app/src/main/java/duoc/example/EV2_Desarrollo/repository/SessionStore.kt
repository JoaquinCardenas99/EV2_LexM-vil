package duoc.example.EV2_Desarrollo.repository

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Solo guarda el token cifrado. La clave permanece en Android Keystore. */
interface SessionPersistence {
    fun guardar(token: String)
    fun leer(): String?
    fun borrar()
}

class SessionStore(context: Context) : SessionPersistence {
    private val prefs = context.getSharedPreferences("lexmovil_token", Context.MODE_PRIVATE)
    private val alias = "lexmovil_session_key"
    init {
        // Se retira el almacenamiento antiguo que incluía contraseñas en texto plano.
        context.getSharedPreferences("lexmovil_session", Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences("lexmovil_prefs", Context.MODE_PRIVATE).edit().clear().apply()
    }
    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }
    override fun guardar(token: String) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
        val data = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
        check(prefs.edit().putString("iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .putString("token", Base64.encodeToString(data, Base64.NO_WRAP)).commit())
    }
    override fun leer(): String? {
        val token = prefs.getString("token", null) ?: return null
        return try {
            val iv = Base64.decode(prefs.getString("iv", null) ?: error("Falta IV"), Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv)) }
            String(cipher.doFinal(Base64.decode(token, Base64.NO_WRAP)), Charsets.UTF_8)
        } catch (_: Exception) { borrar(); null }
    }
    override fun borrar() { check(prefs.edit().clear().commit()) }
}
