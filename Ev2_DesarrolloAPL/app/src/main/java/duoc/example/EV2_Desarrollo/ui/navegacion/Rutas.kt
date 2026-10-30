package duoc.example.EV2_Desarrollo.ui.navegacion

sealed class Rutas(val ruta: String) {
    object Splash : Rutas("splash")
    object Bienvenida : Rutas("bienvenida")
    object Login : Rutas("login")
    object Registro : Rutas("registro")
    object LexMovil : Rutas("lexmovil")
    object ListaLeyes : Rutas("lista_leyes")
    object DetalleLey : Rutas("detalle_ley")
    object Busqueda : Rutas("busqueda")
    object Favoritos : Rutas("favoritos")
    object AcercaDe : Rutas("acerca_de")
    object Camara : Rutas("camara")
}
