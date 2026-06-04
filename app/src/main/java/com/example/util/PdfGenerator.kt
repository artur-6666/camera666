package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.data.ItemEntity
import com.example.data.PhotoEntity
import com.example.data.PhotoType
import com.example.data.ProjectEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfGenerator(private val context: Context) {
    private val document = PdfDocument()
    private val pageWidth = 595
    private val pageHeight = 842
    private var pageNumber = 1
    private var currentPage: PdfDocument.Page? = null
    private var canvas: Canvas? = null
    private var yPosition = 50f
    private val xPosition = 50f
    private val bottomMargin = 50f

    private val titlePaint = Paint().apply { color = Color.BLACK; textSize = 24f; isFakeBoldText = true }
    private val subtitlePaint = Paint().apply { color = Color.DKGRAY; textSize = 16f }
    private val itemTitlePaint = Paint().apply { color = Color.BLACK; textSize = 20f; isFakeBoldText = true }
    private val sectionPaint = Paint().apply { color = Color.DKGRAY; textSize = 16f; isFakeBoldText = true }
    private val textPaint = Paint().apply { color = Color.BLACK; textSize = 12f }

    private fun newPage() {
        currentPage?.let { document.finishPage(it) }
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        currentPage = document.startPage(pageInfo)
        canvas = currentPage!!.canvas
        pageNumber++
        yPosition = 50f
    }

    private fun checkSpace(requiredSpace: Float) {
        if (yPosition + requiredSpace > pageHeight - bottomMargin) {
            newPage()
        }
    }

    suspend fun generatePdf(project: ProjectEntity, items: List<ItemEntity>, photos: List<PhotoEntity>): File? = withContext(Dispatchers.IO) {
        try {
            newPage()

            // Project Title
            canvas?.drawText("Project: ${project.name}", xPosition, yPosition, titlePaint)
            yPosition += 30
            val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
            canvas?.drawText("Generated on: ${dateFormat.format(Date())}", xPosition, yPosition, subtitlePaint)
            yPosition += 50

            for (item in items) {
                checkSpace(60f)
                canvas?.drawText("Item: ${item.name}", xPosition, yPosition, itemTitlePaint)
                yPosition += 20
                if (item.code.isNotEmpty()) {
                    canvas?.drawText("Code: ${item.code}", xPosition, yPosition, textPaint)
                    yPosition += 20
                }
                
                val itemPhotos = photos.filter { it.itemId == item.id }
                val beforePhotos = itemPhotos.filter { it.type == PhotoType.BEFORE }
                val afterPhotos = itemPhotos.filter { it.type == PhotoType.AFTER }

                drawPhotosSection("Before Photos", beforePhotos)
                drawPhotosSection("After Photos", afterPhotos)
                
                yPosition += 30
            }

            currentPage?.let { document.finishPage(it) }
            
            val docsFolder = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "BeforeAfterReports")
            if (!docsFolder.exists()) docsFolder.mkdirs()
            
            val filename = "${project.name.replace(" ", "_")}_Report_${System.currentTimeMillis()}.pdf"
            val file = File(docsFolder, filename)
            
            document.writeTo(FileOutputStream(file))
            document.close()
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun drawPhotosSection(title: String, photos: List<PhotoEntity>) {
        if (photos.isEmpty()) return
        
        checkSpace(40f)
        canvas?.drawText(title, xPosition, yPosition, sectionPaint)
        yPosition += 30

        val photoWidth = 200f
        val photoHeight = 266f // 3:4 aspect ratio approx
        var currentX = xPosition

        for (photo in photos) {
            if (currentX + photoWidth > pageWidth - 50f) {
                currentX = xPosition
                yPosition += photoHeight + 30f
                checkSpace(photoHeight + 50f)
            }
            
            checkSpace(photoHeight + 50f)

            val file = File(photo.filePath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    val destRect = Rect(currentX.toInt(), yPosition.toInt(), (currentX + photoWidth).toInt(), (yPosition + photoHeight).toInt())
                    canvas?.drawBitmap(bitmap, null, destRect, null)
                    bitmap.recycle()
                }
            }
            currentX += photoWidth + 20f
        }
        yPosition += photoHeight + 40f
    }
}
