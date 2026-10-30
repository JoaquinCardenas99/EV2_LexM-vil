package duoc.example.EV2_Desarrollo.ui.pantallas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashPantalla(onTerminar: () -> Unit) {
    val escala = remember { Animatable(0.4f) }
    val transparencia = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { escala.animateTo(1f, tween(durationMillis = 900, easing = FastOutSlowInEasing)) }
        launch { transparencia.animateTo(1f, tween(durationMillis = 900)) }
        delay(2000)
        onTerminar()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Gavel,
            contentDescription = "Logo de LexMovil",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(110.dp)
                .scale(escala.value)
                .alpha(transparencia.value)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "LexMovil",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.alpha(transparencia.value)
        )
        Spacer(modifier = Modifier.height(32.dp))
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.alpha(transparencia.value)
        )
    }
}