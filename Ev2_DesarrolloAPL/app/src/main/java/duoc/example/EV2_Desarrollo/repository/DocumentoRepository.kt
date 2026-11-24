package duoc.example.EV2_Desarrollo.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import duoc.example.EV2_Desarrollo.model.Ley
import java.io.File

/** Acceso a cámara y archivos; los destinos los elige el usuario con el selector Android. */
class DocumentoRepository(private val context: Context) {
    fun crearCaptura(): Uri {
        val carpeta = File(context.cacheDir, "capturas").apply { mkdirs() }
        return FileProvider.getUriForFile(context, "${context.packageName}.provider",
            File.createTempFile("documento_", ".jpg", carpeta))
    }
    fun vistaPrevia(uri: Uri): Bitmap {
        val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, opciones) }
        opciones.inSampleSize = 1
        while (maxOf(opciones.outWidth, opciones.outHeight) / opciones.inSampleSize > 1200) opciones.inSampleSize *= 2
        opciones.inJustDecodeBounds = false
        return context.contentResolver.openInputStream(uri).use {
            requireNotNull(BitmapFactory.decodeStream(it, null, opciones)) { "No se pudo leer la fotografía" }
        }
    }
    fun guardarFoto(origen: Uri, destino: Uri) {
        requireNotNull(context.contentResolver.openInputStream(origen)).use { entrada ->
            requireNotNull(context.contentResolver.openOutputStream(destino, "wt")).use { salida -> entrada.copyTo(salida) }
        }
    }
    fun guardarPdf(destino: Uri, ley: Ley) {
        val pdf = PdfDocument()
        try {
            val texto = Paint().apply { textSize = 12f; isAntiAlias = true }
            var numero = 1
            var pagina = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, numero).create())
            var y = 50f
            fun linea(valor: String) {
                if (y > 780f) {
                    pdf.finishPage(pagina)
                    numero++
                    pagina = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, numero).create())
                    y = 50f
                }
                pagina.canvas.drawText(valor, 45f, y, texto)
                y += 18f
            }
            fun parrafo(valor: String) {
                // breakText corta también palabras extensas para evitar texto fuera de página.
                valor.split('\n').forEach { original ->
                    var restante = original
                    while (restante.isNotEmpty()) {
                        val maximo = texto.breakText(restante, true, 505f, null).coerceAtLeast(1)
                        val espacio = restante.lastIndexOf(' ', maximo.coerceAtMost(restante.lastIndex))
                        val corte = if (maximo < restante.length && espacio > 0) espacio else maximo
                        linea(restante.take(corte))
                        restante = restante.drop(corte).trimStart()
                    }
                    linea("")
                }
            }
            parrafo(ley.codigo)
            parrafo(ley.titulo)
            parrafo("Material académico de ejemplo; no es un texto legal oficial.")
            parrafo("Categoría: ${ley.categoria}")
            ley.articulos.forEach { parrafo("Artículo ${it.numero}"); parrafo(it.texto) }
            pdf.finishPage(pagina)
            requireNotNull(context.contentResolver.openOutputStream(destino, "wt")).use { pdf.writeTo(it) }
        } finally { pdf.close() }
    }
}
