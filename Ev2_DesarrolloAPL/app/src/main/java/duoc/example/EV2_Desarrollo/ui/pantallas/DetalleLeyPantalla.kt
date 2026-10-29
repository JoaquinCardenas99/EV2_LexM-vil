package duoc.example.EV2_Desarrollo.ui.pantallas

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import duoc.example.EV2_Desarrollo.viewmodel.LeyViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleLeyPantalla(
    leyId: Int,
    viewModel: LeyViewModel,
    onVolver: () -> Unit
) {
    val ley by viewModel.leySeleccionada.collectAsState()
    val favoritos by viewModel.favoritos.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(leyId) {
        viewModel.seleccionarLey(leyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ley?.codigo ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (ley != null) {
                        IconButton(onClick = { viewModel.toggleFavorito(ley!!.id) }) {
                            Icon(
                                if (favoritos.contains(ley!!.id)) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (favoritos.contains(ley!!.id)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            ley?.let {
                Text(
                    text = it.titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Categoría: ${it.categoria}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it.descripcion,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { generarPdf(context, it) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Download, contentDescription = null)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Descargar en PDF")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Artículos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(it.articulos) { articulo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Artículo ${articulo.numero}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = articulo.texto,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun generarPdf(context: Context, ley: duoc.example.EV2_Desarrollo.model.Ley) {
    val pdfDocument = PdfDocument()
    val paint = Paint()
    val tituloPaint = Paint()
    tituloPaint.textSize = 16f
    tituloPaint.isFakeBoldText = true
    paint.textSize = 12f

    var paginaNumero = 1
    var y = 50f
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, paginaNumero).create()
    var pagina = pdfDocument.startPage(pageInfo)
    var canvas = pagina.canvas

    canvas.drawText(ley.codigo, 50f, y, tituloPaint)
    y += 25
    canvas.drawText(ley.titulo, 50f, y, paint)
    y += 20
    canvas.drawText("Categoría: ${ley.categoria}", 50f, y, paint)
    y += 30

    ley.articulos.forEach { articulo ->
        if (y > 750f) {
            pdfDocument.finishPage(pagina)
            paginaNumero++
            val nuevaPaginaInfo = PdfDocument.PageInfo.Builder(595, 842, paginaNumero).create()
            pagina = pdfDocument.startPage(nuevaPaginaInfo)
            canvas = pagina.canvas
            y = 50f
        }
        canvas.drawText("Artículo ${articulo.numero}", 50f, y, tituloPaint)
        y += 20
        val texto = articulo.texto
        val palabras = texto.split(" ")
        var linea = ""
        palabras.forEach { palabra ->
            val prueba = if (linea.isEmpty()) palabra else "$linea $palabra"
            if (paint.measureText(prueba) > 500f) {
                if (y > 750f) {
                    pdfDocument.finishPage(pagina)
                    paginaNumero++
                    val nuevaPaginaInfo = PdfDocument.PageInfo.Builder(595, 842, paginaNumero).create()
                    pagina = pdfDocument.startPage(nuevaPaginaInfo)
                    canvas = pagina.canvas
                    y = 50f
                }
                canvas.drawText(linea, 50f, y, paint)
                y += 18
                linea = palabra
            } else {
                linea = prueba
            }
        }
        if (linea.isNotEmpty()) {
            if (y > 750f) {
                pdfDocument.finishPage(pagina)
                paginaNumero++
                val nuevaPaginaInfo = PdfDocument.PageInfo.Builder(595, 842, paginaNumero).create()
                pagina = pdfDocument.startPage(nuevaPaginaInfo)
                canvas = pagina.canvas
                y = 50f
            }
            canvas.drawText(linea, 50f, y, paint)
            y += 25
        }
    }

    pdfDocument.finishPage(pagina)

    val nombreArchivo = "${ley.codigo.replace(" ", "_")}.pdf"
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Files.FileColumns.DISPLAY_NAME, nombreArchivo)
                put(MediaStore.Files.FileColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/LexMovil")
            }
            val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
                Toast.makeText(context, "PDF guardado en Descargas/LexMovil", Toast.LENGTH_LONG).show()
            }
        } else {
            val directorio = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LexMovil")
            if (!directorio.exists()) {
                directorio.mkdirs()
            }
            val archivo = File(directorio, nombreArchivo)
            val fos = FileOutputStream(archivo)
            pdfDocument.writeTo(fos)
            fos.close()
            Toast.makeText(context, "PDF guardado en Descargas/LexMovil", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error al guardar PDF", Toast.LENGTH_SHORT).show()
    }
    pdfDocument.close()
}
