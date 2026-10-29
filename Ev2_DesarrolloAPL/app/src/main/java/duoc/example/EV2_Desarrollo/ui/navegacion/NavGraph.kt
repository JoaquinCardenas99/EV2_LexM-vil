package duoc.example.EV2_Desarrollo.ui.navegacion

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import duoc.example.EV2_Desarrollo.ui.CamaraFotos
import duoc.example.EV2_Desarrollo.ui.auth.BienvenidaPantalla
import duoc.example.EV2_Desarrollo.ui.auth.LoginPantalla
import duoc.example.EV2_Desarrollo.ui.auth.RegistroPantalla
import duoc.example.EV2_Desarrollo.ui.pantallas.AcercaDePantalla
import duoc.example.EV2_Desarrollo.ui.pantallas.BusquedaPantalla
import duoc.example.EV2_Desarrollo.ui.pantallas.DetalleLeyPantalla
import duoc.example.EV2_Desarrollo.ui.pantallas.FavoritosPantalla
import duoc.example.EV2_Desarrollo.ui.pantallas.LexMovilPantalla
import duoc.example.EV2_Desarrollo.ui.pantallas.ListaLeyesPantalla
import duoc.example.EV2_Desarrollo.viewmodel.AuthViewModel
import duoc.example.EV2_Desarrollo.viewmodel.LeyViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val leyViewModel: LeyViewModel = viewModel()

    NavHost(navController = navController, startDestination = Rutas.Bienvenida.ruta) {
        composable(Rutas.Bienvenida.ruta) {
            BienvenidaPantalla(
                onIrLogin = { navController.navigate(Rutas.Login.ruta) },
                onIrRegistro = { navController.navigate(Rutas.Registro.ruta) }
            )
        }
        composable(Rutas.Login.ruta) {
            LoginPantalla(
                viewModel = authViewModel,
                onVolver = { navController.popBackStack() },
                onLoginExitoso = {
                    navController.navigate(Rutas.LexMovil.ruta) {
                        popUpTo(Rutas.Bienvenida.ruta) { inclusive = true }
                    }
                }
            )
        }
        composable(Rutas.Registro.ruta) {
            RegistroPantalla(
                viewModel = authViewModel,
                onVolver = { navController.popBackStack() },
                onRegistroExitoso = {
                    navController.navigate(Rutas.Login.ruta) {
                        popUpTo(Rutas.Registro.ruta) { inclusive = true }
                    }
                }
            )
        }
        composable(Rutas.LexMovil.ruta) {
            LexMovilPantalla(
                authViewModel = authViewModel,
                onCerrarSesion = {
                    navController.navigate(Rutas.Bienvenida.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onIrListaLeyes = { navController.navigate(Rutas.ListaLeyes.ruta) },
                onIrBusqueda = { navController.navigate(Rutas.Busqueda.ruta) },
                onIrFavoritos = { navController.navigate(Rutas.Favoritos.ruta) },
                onIrAcercaDe = { navController.navigate(Rutas.AcercaDe.ruta) },
                onIrCamara = { navController.navigate(Rutas.Camara.ruta) }
            )
        }
        composable(Rutas.Camara.ruta) {
            CamaraFotos()
        }
        composable(Rutas.ListaLeyes.ruta) {
            ListaLeyesPantalla(
                viewModel = leyViewModel,
                onVolver = { navController.popBackStack() },
                onVerDetalle = { id -> navController.navigate("${Rutas.DetalleLey.ruta}/$id") }
            )
        }
        composable(Rutas.Busqueda.ruta) {
            BusquedaPantalla(
                viewModel = leyViewModel,
                onVolver = { navController.popBackStack() },
                onVerDetalle = { id -> navController.navigate("${Rutas.DetalleLey.ruta}/$id") }
            )
        }
        composable(Rutas.Favoritos.ruta) {
            FavoritosPantalla(
                viewModel = leyViewModel,
                onVolver = { navController.popBackStack() },
                onVerDetalle = { id -> navController.navigate("${Rutas.DetalleLey.ruta}/$id") }
            )
        }
        composable(Rutas.AcercaDe.ruta) {
            AcercaDePantalla(onVolver = { navController.popBackStack() })
        }
        composable(
            route = "${Rutas.DetalleLey.ruta}/{leyId}",
            arguments = listOf(navArgument("leyId") { type = NavType.IntType })
        ) { backStackEntry ->
            val leyId = backStackEntry.arguments?.getInt("leyId") ?: 0
            DetalleLeyPantalla(
                leyId = leyId,
                viewModel = leyViewModel,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}

