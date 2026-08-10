package com.example.ui

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

object QrBarcodeGenerator {

    /**
     * Membuat Bitmap Barcode 1D + Label Nama & Harga Produk
     */
    fun generateBarcodeBitmap(
        barcodeStr: String,
        productName: String,
        priceStr: String,
        width: Int = 640,
        height: Int = 420
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background putih
        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Border halus
        val borderPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRect(8f, 8f, width - 8f, height - 8f, borderPaint)

        // Paint Teks
        val textPaint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        // Header Store / App
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 22f
        textPaint.color = Color.parseColor("#6200EE") // Purple Primary
        canvas.drawText("LABEL BARCODE PRODUK", width / 2f, 42f, textPaint)

        // Nama Produk
        textPaint.color = Color.BLACK
        textPaint.textSize = 28f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val truncatedName = if (productName.length > 28) productName.take(25) + "..." else productName
        canvas.drawText(truncatedName, width / 2f, 85f, textPaint)

        // Harga Produk
        textPaint.textSize = 26f
        textPaint.color = Color.parseColor("#2E7D32") // Green Price
        canvas.drawText(priceStr, width / 2f, 122f, textPaint)

        // Gambar Batang Barcode (Deterministik berdasarkan digit/karakter barcode)
        val barTop = 150f
        val barBottom = 330f
        val barHeight = barBottom - barTop
        val quietZone = 50f
        val availableWidth = width - (quietZone * 2)

        // Pola bar sederhana (Code 128 / EAN simulation pattern)
        val cleanBarcode = if (barcodeStr.isBlank()) "00000000" else barcodeStr
        val modules = mutableListOf<Boolean>()
        
        // Start pattern
        modules.addAll(listOf(true, false, true, false))
        
        // Encode characters
        cleanBarcode.forEach { char ->
            val num = char.code
            for (bit in 0..5) {
                modules.add(((num shr bit) and 1) == 1)
            }
            modules.add(false) // separator
        }
        
        // Stop pattern
        modules.addAll(listOf(true, true, false, true, true))

        val moduleWidth = availableWidth / modules.size.toFloat()

        val barPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            isAntiAlias = false
        }

        var currentX = quietZone
        modules.forEach { isBlack ->
            if (isBlack) {
                canvas.drawRect(currentX, barTop, currentX + moduleWidth, barBottom, barPaint)
            }
            currentX += moduleWidth
        }

        // Teks Nomor Barcode dibawah Batang
        textPaint.color = Color.BLACK
        textPaint.textSize = 26f
        textPaint.typeface = Typeface.MONOSPACE
        canvas.drawText(cleanBarcode, width / 2f, 380f, textPaint)

        return bitmap
    }

    /**
     * Membuat Bitmap QR Code 2D + Label Nama & Harga Produk
     */
    fun generateQrCodeBitmap(
        qrContent: String,
        productName: String,
        priceStr: String,
        size: Int = 640
    ): Bitmap {
        val height = size + 140
        val bitmap = Bitmap.createBitmap(size, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background putih
        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, size.toFloat(), height.toFloat(), bgPaint)

        // Border
        val borderPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRect(8f, 8f, size - 8f, height - 8f, borderPaint)

        val textPaint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        // Header
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 22f
        textPaint.color = Color.parseColor("#6200EE")
        canvas.drawText("QR CODE PRODUK", size / 2f, 42f, textPaint)

        // Nama & Harga
        textPaint.color = Color.BLACK
        textPaint.textSize = 28f
        val truncatedName = if (productName.length > 28) productName.take(25) + "..." else productName
        canvas.drawText(truncatedName, size / 2f, 85f, textPaint)

        textPaint.textSize = 26f
        textPaint.color = Color.parseColor("#2E7D32")
        canvas.drawText(priceStr, size / 2f, 122f, textPaint)

        // Visual Matrix QR Code 21x21 Grid
        val qrTop = 145f
        val qrSize = size - 120f
        val quietX = 60f
        val gridSize = 21
        val cellSize = qrSize / gridSize

        val qrPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }

        // Matriks QR deterministik dari hash isi qrContent
        val hash = (qrContent.ifBlank { "0000" }).hashCode()
        val grid = Array(gridSize) { BooleanArray(gridSize) }

        // Finder Patterns (Tiga kotak sudut)
        fun drawFinder(startX: Int, startY: Int) {
            for (r in 0..6) {
                for (c in 0..6) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isCenter = r in 2..4 && c in 2..4
                    grid[startY + r][startX + c] = isBorder || isCenter
                }
            }
        }

        drawFinder(0, 0) // Top-Left
        drawFinder(gridSize - 7, 0) // Top-Right
        drawFinder(0, gridSize - 7) // Bottom-Left

        // Isi data pseudo-QR sisanya
        var bitIndex = 0
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                // Jangan timpa finder pattern
                if ((r < 7 && c < 7) || (r < 7 && c >= gridSize - 7) || (r >= gridSize - 7 && c < 7)) continue
                
                // Bit acak deterministik
                val bit = ((hash xor (r * 31 + c * 17 + bitIndex)) and 1) == 1
                grid[r][c] = bit
                bitIndex++
            }
        }

        // Draw Matrix
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                if (grid[r][c]) {
                    val left = quietX + (c * cellSize)
                    val top = qrTop + (r * cellSize)
                    canvas.drawRect(left, top, left + cellSize, top + cellSize, qrPaint)
                }
            }
        }

        // Teks Kode QR di bawah
        textPaint.color = Color.BLACK
        textPaint.textSize = 24f
        textPaint.typeface = Typeface.MONOSPACE
        canvas.drawText(qrContent.ifBlank { "-" }, size / 2f, height - 35f, textPaint)

        return bitmap
    }

    /**
     * Menyimpan Gambar QR/Barcode ke Galeri Perangkat
     */
    fun saveLabelToGallery(
        context: Context,
        bitmap: Bitmap,
        productName: String,
        barcodeStr: String
    ): Boolean {
        return try {
            val cleanName = productName.replace(Regex("[^a-zA-Z0-9]"), "_")
            val fileName = "Barcode_${cleanName}_${barcodeStr.ifBlank { System.currentTimeMillis().toString() }}.png"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Kalkulator Pintar/Label Barcode")
                }
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    resolver.openOutputStream(imageUri)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    }
                    Toast.makeText(context, "✅ Barcode $productName berhasil disimpan ke Galeri!", Toast.LENGTH_LONG).show()
                    true
                } else false
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val folder = File(picturesDir, "Kalkulator Pintar/Label Barcode")
                if (!folder.exists()) folder.mkdirs()

                val file = File(folder, fileName)
                FileOutputStream(file).use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
                Toast.makeText(context, "✅ Barcode $productName disimpan di: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "❌ Gagal menyimpan label barcode: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            false
        }
    }
}
