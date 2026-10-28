package duoc.example.EV2_Desarrollo.repository

import duoc.example.EV2_Desarrollo.model.Articulo
import duoc.example.EV2_Desarrollo.model.Ley

class LeyRepository {
    private val leyes = listOf(
        Ley(
            id = 1,
            codigo = "CÓDIGO CIVIL",
            titulo = "Código Civil de la República de Chile",
            categoria = "Civil",
            descripcion = "Regula las relaciones jurídicas civiles entre personas.",
            articulos = listOf(
                Articulo("1", "La ley es obligatoria tanto a los chilenos como a los extranjeros residentes en Chile."),
                Articulo("2", "La ignorancia de la ley no sirve de excusa."),
                Articulo("3", "Solo corresponde al legislador interpretar la ley de un modo generalmente obligatorio.")
            )
        ),
        Ley(
            id = 2,
            codigo = "CÓDIGO PENAL",
            titulo = "Código Penal de Chile",
            categoria = "Penal",
            descripcion = "Establece los delitos y las penas aplicables en Chile.",
            articulos = listOf(
                Articulo("1", "No hay crimen ni pena sin una ley previa, escrita, vigente y aplicable al delito."),
                Articulo("10", "Son punibles los hechos realizados dolosa o culposamente.")
            )
        ),
        Ley(
            id = 3,
            codigo = "CONSTITUCIÓN POLÍTICA",
            titulo = "Constitución Política de la República de Chile",
            categoria = "Constitucional",
            descripcion = "Norma fundamental que organiza el Estado y garantiza los derechos fundamentales.",
            articulos = listOf(
                Articulo("1", "Las personas nacen libres e iguales en dignidad y derechos."),
                Articulo("5", "La soberanía reside esencialmente en la Nación."),
                Articulo("19", "La Constitución asegura a todas las personas: igualdad ante la ley.")
            )
        ),
        Ley(
            id = 4,
            codigo = "CÓDIGO DEL TRABAJO",
            titulo = "Código del Trabajo",
            categoria = "Laboral",
            descripcion = "Regula las relaciones individuales y colectivas del trabajo.",
            articulos = listOf(
                Articulo("5", "El trabajo es un derecho y un deber social."),
                Articulo("7", "Es obligación del empleador respetar la dignidad del trabajador.")
            )
        )
    )

    fun obtenerTodas(): List<Ley> = leyes
    fun obtenerPorId(id: Int): Ley? = leyes.find { it.id == id }
    fun buscar(query: String): List<Ley> {
        val q = query.lowercase()
        return leyes.filter {
            it.titulo.lowercase().contains(q) ||
            it.codigo.lowercase().contains(q) ||
            it.categoria.lowercase().contains(q) ||
            it.descripcion.lowercase().contains(q)
        }
    }
    fun obtenerPorCategoria(categoria: String): List<Ley> = leyes.filter { it.categoria == categoria }
}
