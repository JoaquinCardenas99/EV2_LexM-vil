package duoc.example.EV2_Desarrollo.model

data class ErroresFormulario(
    val nombre: String? = null,
    val rut: String? = null,
    val correo: String? = null,
    val password: String? = null,
    val confirmacion: String? = null
) {
    fun hayErrores(): Boolean =
        listOfNotNull(nombre, rut, correo, password, confirmacion).isNotEmpty()
}
