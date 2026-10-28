package duoc.example.EV2_Desarrollo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import duoc.example.EV2_Desarrollo.ui.navegacion.NavGraph
import duoc.example.EV2_Desarrollo.ui.theme.Ev2_DesarrolloAPLTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Ev2_DesarrolloAPLTheme {
                NavGraph()
            }
        }
    }
}
