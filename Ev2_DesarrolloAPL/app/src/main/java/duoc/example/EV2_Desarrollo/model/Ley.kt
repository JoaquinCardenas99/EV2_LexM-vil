package duoc.example.EV2_Desarrollo.model

data class Ley(
    val id: Int,
    val codigo: String,
    val titulo: String,
    val categoria: String,
    val descripcion: String,
    val articulos: List<Articulo>
)

data class Articulo(
    val numero: String,
    val texto: String
)
