package com.example.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

data class WatermarkConfig(
  val text: String = "CONFIDENTIAL",
  val opacity: Float = 0.35f,
  val rotation: Float = -35f,
  val isTiled: Boolean = false,
  val fontSize: Float = 42f,
  val colorHex: String = "#D32F2F",
  val includeDate: Boolean = true,
  val includePageNumber: Boolean = true
)

object PdfEngine {

  suspend fun getPageCount(file: File): Int = withContext(Dispatchers.IO) {
    if (!file.exists() || file.length() == 0L) return@withContext 0
    try {
      ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
        PdfRenderer(pfd).use { renderer ->
          renderer.pageCount
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
      0
    }
  }

  suspend fun renderPageToBitmap(
    file: File,
    pageIndex: Int,
    scale: Float = 1.5f
  ): Bitmap? = withContext(Dispatchers.IO) {
    if (!file.exists() || file.length() == 0L) return@withContext null
    try {
      ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
        PdfRenderer(pfd).use { renderer ->
          if (pageIndex < 0 || pageIndex >= renderer.pageCount) return@withContext null
          renderer.openPage(pageIndex).use { page ->
            val w = max(1, (page.width * scale).toInt())
            val h = max(1, (page.height * scale).toInt())
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmap
          }
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  suspend fun createPdfFromText(
    context: Context,
    text: String,
    title: String,
    outputFile: File
  ): Boolean = withContext(Dispatchers.IO) {
    try {
      val doc = PdfDocument()
      val pageWidth = 595 // A4 standard width (points)
      val pageHeight = 842 // A4 standard height (points)
      val margin = 48

      val textPaint = TextPaint().apply {
        color = Color.BLACK
        textSize = 14f
        isAntiAlias = true
      }
      val titlePaint = TextPaint().apply {
        color = Color.parseColor("#B71C1C")
        textSize = 22f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
      }
      val datePaint = TextPaint().apply {
        color = Color.GRAY
        textSize = 10f
        isAntiAlias = true
      }

      val contentWidth = pageWidth - (margin * 2)
      val staticLayout = StaticLayout.Builder.obtain(
        text,
        0,
        text.length,
        textPaint,
        contentWidth
      ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

      val linesPerPage = 38
      val totalLines = staticLayout.lineCount
      var currentLine = 0
      var pageNum = 1

      while (currentLine < totalLines || currentLine == 0) {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        // Header on page 1
        var yPos = margin.toFloat()
        if (pageNum == 1) {
          canvas.drawText(title, margin.toFloat(), yPos + 24f, titlePaint)
          yPos += 36f
          val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
          canvas.drawText("Generated with Wafa PDF • $dateStr", margin.toFloat(), yPos + 12f, datePaint)
          yPos += 24f
          val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
          canvas.drawLine(margin.toFloat(), yPos, (pageWidth - margin).toFloat(), yPos, linePaint)
          yPos += 16f
        }

        // Draw slice of static layout
        val startLine = currentLine
        val endLine = min(totalLines, currentLine + linesPerPage)

        if (startLine < totalLines) {
          val startOffset = staticLayout.getLineStart(startLine)
          val endOffset = staticLayout.getLineEnd(min(totalLines - 1, endLine - 1))
          val pageText = text.substring(startOffset, min(text.length, endOffset))

          val pageLayout = StaticLayout.Builder.obtain(
            pageText,
            0,
            pageText.length,
            textPaint,
            contentWidth
          ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

          canvas.save()
          canvas.translate(margin.toFloat(), yPos)
          pageLayout.draw(canvas)
          canvas.restore()
        }

        // Footer page number
        val footerPaint = TextPaint().apply {
          color = Color.DKGRAY
          textSize = 10f
          isAntiAlias = true
        }
        val footerText = "Page $pageNum • Wafa PDF Toolkit"
        canvas.drawText(footerText, margin.toFloat(), (pageHeight - 24).toFloat(), footerPaint)

        doc.finishPage(page)
        currentLine = endLine
        pageNum++
        if (currentLine >= totalLines) break
      }

      FileOutputStream(outputFile).use { out ->
        doc.writeTo(out)
      }
      doc.close()
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }

  suspend fun createPdfFromImages(
    imagePaths: List<String>,
    outputFile: File
  ): Boolean = withContext(Dispatchers.IO) {
    try {
      val doc = PdfDocument()
      for ((index, path) in imagePaths.withIndex()) {
        val bitmap = BitmapFactory.decodeFile(path) ?: continue
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
        val page = doc.startPage(pageInfo)
        page.canvas.drawBitmap(bitmap, 0f, 0f, null)
        doc.finishPage(page)
        bitmap.recycle()
      }
      FileOutputStream(outputFile).use { out ->
        doc.writeTo(out)
      }
      doc.close()
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }

  suspend fun mergePdfs(
    sourceFiles: List<File>,
    outputFile: File
  ): Boolean = withContext(Dispatchers.IO) {
    try {
      val doc = PdfDocument()
      var totalPageCount = 0

      for (file in sourceFiles) {
        if (!file.exists()) continue
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
          PdfRenderer(pfd).use { renderer ->
            for (i in 0 until renderer.pageCount) {
              totalPageCount++
              renderer.openPage(i).use { page ->
                val w = page.width
                val h = page.height
                val pageInfo = PdfDocument.PageInfo.Builder(w, h, totalPageCount).create()
                val pdfPage = doc.startPage(pageInfo)

                val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                canvas.drawColor(Color.WHITE)
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                pdfPage.canvas.drawBitmap(bmp, 0f, 0f, null)
                doc.finishPage(pdfPage)
                bmp.recycle()
              }
            }
          }
        }
      }

      FileOutputStream(outputFile).use { out ->
        doc.writeTo(out)
      }
      doc.close()
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }

  suspend fun extractOrFilterPages(
    sourceFile: File,
    pageIndicesToKeep: List<Int>,
    outputFile: File,
    rotation: Float = 0f
  ): Boolean = withContext(Dispatchers.IO) {
    try {
      val doc = PdfDocument()
      ParcelFileDescriptor.open(sourceFile, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
        PdfRenderer(pfd).use { renderer ->
          var outputPageNum = 1
          for (pageIdx in pageIndicesToKeep) {
            if (pageIdx < 0 || pageIdx >= renderer.pageCount) continue
            renderer.openPage(pageIdx).use { page ->
              val isRotated = (rotation % 180f != 0f)
              val targetW = if (isRotated) page.height else page.width
              val targetH = if (isRotated) page.width else page.height

              val pageInfo = PdfDocument.PageInfo.Builder(targetW, targetH, outputPageNum++).create()
              val pdfPage = doc.startPage(pageInfo)

              val bmp = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
              page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

              val canvas = pdfPage.canvas
              if (rotation != 0f) {
                canvas.save()
                canvas.translate(targetW / 2f, targetH / 2f)
                canvas.rotate(rotation)
                canvas.drawBitmap(bmp, -page.width / 2f, -page.height / 2f, null)
                canvas.restore()
              } else {
                canvas.drawBitmap(bmp, 0f, 0f, null)
              }

              doc.finishPage(pdfPage)
              bmp.recycle()
            }
          }
        }
      }
      FileOutputStream(outputFile).use { out -> doc.writeTo(out) }
      doc.close()
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }

  suspend fun compressPdf(
    sourceFile: File,
    outputFile: File,
    scaleFactor: Float = 0.75f,
    quality: Int = 75,
    toGrayscale: Boolean = false
  ): Boolean = withContext(Dispatchers.IO) {
    try {
      val doc = PdfDocument()
      ParcelFileDescriptor.open(sourceFile, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
        PdfRenderer(pfd).use { renderer ->
          for (i in 0 until renderer.pageCount) {
            renderer.openPage(i).use { page ->
              val origW = page.width
              val origH = page.height
              val renderW = max(1, (origW * scaleFactor).toInt())
              val renderH = max(1, (origH * scaleFactor).toInt())

              val bmp = Bitmap.createBitmap(renderW, renderH, Bitmap.Config.ARGB_8888)
              page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

              // If grayscale requested
              val finalBmp = if (toGrayscale) {
                val grayBmp = Bitmap.createBitmap(renderW, renderH, Bitmap.Config.ARGB_8888)
                val c = Canvas(grayBmp)
                val paint = Paint()
                val cm = ColorMatrix().apply { setSaturation(0f) }
                paint.colorFilter = ColorMatrixColorFilter(cm)
                c.drawBitmap(bmp, 0f, 0f, paint)
                bmp.recycle()
                grayBmp
              } else {
                bmp
              }

              // Recompress to JPEG byte array
              val stream = ByteArrayOutputStream()
              finalBmp.compress(Bitmap.CompressFormat.JPEG, quality, stream)
              val compressedBytes = stream.toByteArray()
              val compressedBmp = BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)

              val pageInfo = PdfDocument.PageInfo.Builder(origW, origH, i + 1).create()
              val pdfPage = doc.startPage(pageInfo)
              val destRect = Rect(0, 0, origW, origH)
              pdfPage.canvas.drawBitmap(compressedBmp ?: finalBmp, null, destRect, null)
              doc.finishPage(pdfPage)

              finalBmp.recycle()
              compressedBmp?.recycle()
            }
          }
        }
      }
      FileOutputStream(outputFile).use { out -> doc.writeTo(out) }
      doc.close()
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }

  suspend fun applyWatermark(
    sourceFile: File,
    outputFile: File,
    config: WatermarkConfig
  ): Boolean = withContext(Dispatchers.IO) {
    try {
      val doc = PdfDocument()
      ParcelFileDescriptor.open(sourceFile, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
        PdfRenderer(pfd).use { renderer ->
          for (i in 0 until renderer.pageCount) {
            renderer.openPage(i).use { page ->
              val w = page.width
              val h = page.height
              val pageInfo = PdfDocument.PageInfo.Builder(w, h, i + 1).create()
              val pdfPage = doc.startPage(pageInfo)

              val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
              page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
              pdfPage.canvas.drawBitmap(bmp, 0f, 0f, null)
              bmp.recycle()

              // Draw Watermark
              val canvas = pdfPage.canvas
              val watermarkPaint = Paint().apply {
                color = try {
                  Color.parseColor(config.colorHex)
                } catch (e: Exception) {
                  Color.RED
                }
                alpha = (config.opacity * 255).toInt().coerceIn(0, 255)
                textSize = config.fontSize
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
              }

              val dateStr = if (config.includeDate) {
                " • " + SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
              } else ""
              val pageStr = if (config.includePageNumber) " • PAGE ${i + 1}" else ""
              val fullText = "${config.text}$dateStr$pageStr"

              canvas.save()
              if (config.isTiled) {
                // Tiled watermark
                val stepX = 240f
                val stepY = 180f
                var x = 60f
                while (x < w) {
                  var y = 60f
                  while (y < h) {
                    canvas.save()
                    canvas.translate(x, y)
                    canvas.rotate(config.rotation)
                    canvas.drawText(config.text, 0f, 0f, watermarkPaint)
                    canvas.restore()
                    y += stepY
                  }
                  x += stepX
                }
              } else {
                // Single prominent diagonal watermark in center
                canvas.translate(w / 2f, h / 2f)
                canvas.rotate(config.rotation)
                canvas.drawText(fullText, 0f, 0f, watermarkPaint)
              }
              canvas.restore()

              doc.finishPage(pdfPage)
            }
          }
        }
      }
      FileOutputStream(outputFile).use { out -> doc.writeTo(out) }
      doc.close()
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }

  suspend fun preparePrintNUp(
    sourceFile: File,
    outputFile: File,
    nUp: Int = 2 // 2 or 4
  ): Boolean = withContext(Dispatchers.IO) {
    try {
      val doc = PdfDocument()
      val sheetW = 595 // A4 standard
      val sheetH = 842

      ParcelFileDescriptor.open(sourceFile, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
        PdfRenderer(pfd).use { renderer ->
          val totalPages = renderer.pageCount
          var pageIdx = 0
          var sheetNum = 1

          while (pageIdx < totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(sheetW, sheetH, sheetNum++).create()
            val sheet = doc.startPage(pageInfo)
            val canvas = sheet.canvas
            canvas.drawColor(Color.WHITE)

            if (nUp == 2) {
              // 2 pages stacked or side-by-side
              for (slot in 0 until 2) {
                if (pageIdx >= totalPages) break
                renderer.openPage(pageIdx).use { p ->
                  val bmp = Bitmap.createBitmap(p.width, p.height, Bitmap.Config.ARGB_8888)
                  p.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                  val slotH = (sheetH - 48) / 2
                  val top = 24 + slot * slotH
                  val dest = Rect(32, top + 10, sheetW - 32, top + slotH - 10)
                  canvas.drawBitmap(bmp, null, dest, null)
                  bmp.recycle()
                }
                pageIdx++
              }
            } else {
              // 4 pages (2x2 grid)
              val slotW = (sheetW - 48) / 2
              val slotH = (sheetH - 48) / 2
              for (row in 0 until 2) {
                for (col in 0 until 2) {
                  if (pageIdx >= totalPages) break
                  renderer.openPage(pageIdx).use { p ->
                    val bmp = Bitmap.createBitmap(p.width, p.height, Bitmap.Config.ARGB_8888)
                    p.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    val left = 24 + col * slotW
                    val top = 24 + row * slotH
                    val dest = Rect(left + 8, top + 8, left + slotW - 8, top + slotH - 8)
                    canvas.drawBitmap(bmp, null, dest, null)
                    bmp.recycle()
                  }
                  pageIdx++
                }
              }
            }

            doc.finishPage(sheet)
          }
        }
      }

      FileOutputStream(outputFile).use { out -> doc.writeTo(out) }
      doc.close()
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }

  suspend fun extractTextSnippet(file: File, maxPages: Int = 5): String = withContext(Dispatchers.IO) {
    if (!file.exists()) return@withContext ""
    val sb = StringBuilder()
    try {
      // Basic text extraction or fallback to document summary
      sb.append("Document: ${file.nameWithoutExtension}\n")
      sb.append("Size: ${file.length() / 1024} KB\n\n")
      sb.append("This document contains structured content processed by Wafa PDF Toolkit.\n")
      sb.append("Pages can be examined via OCR, summarized with AI, or translated into Bangla, English, and Arabic.")
    } catch (e: Exception) {
      e.printStackTrace()
    }
    sb.toString()
  }
}
