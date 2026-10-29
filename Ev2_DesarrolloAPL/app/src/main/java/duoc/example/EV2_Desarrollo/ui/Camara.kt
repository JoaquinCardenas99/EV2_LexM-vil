package duoc.example.EV2_Desarrollo.ui

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CamaraFotos() {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var permisoConcedido by remember { mutableStateOf(false) }
    var mostrarDialogoGuardar by remember { mutableStateOf(false) }
    var nombreCarpeta by remember { mutableStateOf("LexMovil") }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageUri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(imageUri!!)
                bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                mostrarDialogoGuardar = true
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permisoConcedido = granted
        if (granted) {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val photoFile = File(context.cacheDir, "DOC_$timeStamp.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",
                photoFile
            )
            imageUri = uri
            takePictureLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Escanear Documento") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (bitmap != null) {
                Text(text = "Vista previa del documento", modifier = Modifier.padding(bottom = 8.dp))
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "Foto del documento",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(onClick = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }) {
                        Text("Tomar otra foto")
                    }
                    Button(onClick = {
                        mostrarDialogoGuardar = true
                    }) {
                        Text("Guardar documento")
                    }
                }
            } else {
                Text(text = "Toma una foto de tu documento para guardarla")
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Abrir cámara")
                }
            }
        }

        if (mostrarDialogoGuardar && bitmap != null) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoGuardar = false },
                title = { Text("Guardar documento") },
                text = {
                    Column {
                        Text("Ingresa el nombre de la carpeta donde deseas guardar la imagen")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = nombreCarpeta,
                            onValueChange = { nombreCarpeta = it },
                            label = { Text("Nombre de carpeta") },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Se guardará en: Documentos/$nombreCarpeta/",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        guardarImagen(context, bitmap!!, nombreCarpeta)
                        mostrarDialogoGuardar = false
                        Toast.makeText(context, "Documento guardado en Documentos/$nombreCarpeta", Toast.LENGTH_LONG).show()
                    }) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoGuardar = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

private fun guardarImagen(context: Context, bitmap: Bitmap, nombreCarpeta: String) {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val nombreArchivo = "Documento_$timeStamp.jpg"

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, nombreArchivo)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/$nombreCarpeta")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }
            }
        } else {
            val directorio = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), nombreCarpeta)
            if (!directorio.exists()) {
                directorio.mkdirs()
            }
            val archivo = File(directorio, nombreArchivo)
            val fos = FileOutputStream(archivo)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.flush()
            fos.close()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
