package com.uni.glycolog.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.uni.glycolog.R
import com.uni.glycolog.data.MeasurementEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

// genera i report PDF con le API native Android (PdfDocument), come da specifica di progetto
class PdfReportGenerator(private val context: Context) {

    companion object {
        // dimensioni pagina A4 in punti (1/72 di pollice)
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val MARGIN = 40f
        private const val ROW_HEIGHT = 16f

        // posizioni orizzontali delle colonne della tabella
        private const val COL_DATETIME = MARGIN
        private const val COL_GLUCOSE = 150f
        private const val COL_FAST = 215f
        private const val COL_SLOW = 265f
        private const val COL_CARBS = 315f
        private const val COL_CONTEXT = 360f
        private const val COL_NOTES = 445f
        private const val MAX_NOTE_CHARS = 24
    }

    private val titlePaint = Paint().apply {
        textSize = 18f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = Color.rgb(30, 132, 73)
        isAntiAlias = true
    }
    private val sectionPaint = Paint().apply {
        textSize = 12f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = Color.BLACK
        isAntiAlias = true
    }
    private val textPaint = Paint().apply {
        textSize = 10f
        color = Color.DKGRAY
        isAntiAlias = true
    }
    private val tableHeaderPaint = Paint().apply {
        textSize = 9f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = Color.BLACK
        isAntiAlias = true
    }
    private val cellPaint = Paint().apply {
        textSize = 9f
        color = Color.DKGRAY
        isAntiAlias = true
    }
    private val linePaint = Paint().apply {
        strokeWidth = 0.5f
        color = Color.LTGRAY
    }

    // colore del valore glicemico nella tabella (toni scuri per la stampa)
    private fun glucosePaint(value: Int): Paint {
        val color = when (GlucoseStats.classify(value)) {
            GlucoseLevel.IN_RANGE -> Color.rgb(30, 132, 73)
            GlucoseLevel.WARNING -> Color.rgb(183, 149, 11)
            else -> Color.rgb(192, 57, 43)
        }
        return Paint(cellPaint).apply {
            this.color = color
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    // crea il PDF e lo salva nella cartella dell'app; ritorna il file generato
    fun generate(measurements: List<MeasurementEntity>, periodLabel: String): File {
        val document = PdfDocument()
        var pageNumber = 1
        var page = document.startPage(newPageInfo(pageNumber))
        var canvas = page.canvas
        var y = MARGIN + 10f

        val dateTimeFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        // --- intestazione ---
        canvas.drawText(context.getString(R.string.pdf_title), MARGIN, y, titlePaint)
        y += 22f
        canvas.drawText(context.getString(R.string.pdf_period, periodLabel), MARGIN, y, textPaint)
        y += 14f
        canvas.drawText(
            context.getString(R.string.pdf_generated, dateTimeFormatter.format(Date())),
            MARGIN, y, textPaint
        )
        y += 26f

        // --- statistiche del periodo ---
        val values = measurements.map { it.bloodSugarLevel }
        canvas.drawText(context.getString(R.string.pdf_stats_title), MARGIN, y, sectionPaint)
        y += 16f
        val statLines = listOf(
            context.getString(R.string.pdf_stat_count, measurements.size),
            context.getString(
                R.string.pdf_stat_mean,
                GlucoseStats.mean(values).roundToInt().toString()
            ),
            context.getString(
                R.string.pdf_stat_sd,
                Formatters.formatDecimal(GlucoseStats.standardDeviation(values))
            ),
            context.getString(R.string.pdf_stat_tir, GlucoseStats.timeInRange(values)),
            context.getString(
                R.string.pdf_stat_min_max,
                values.minOrNull() ?: 0,
                values.maxOrNull() ?: 0
            )
        )
        for (line in statLines) {
            canvas.drawText(line, MARGIN + 8f, y, textPaint)
            y += 14f
        }

        // --- distribuzione per fasce orarie ---
        y += 10f
        canvas.drawText(context.getString(R.string.pdf_slots_title), MARGIN, y, sectionPaint)
        y += 16f
        for (stat in GlucoseStats.hourlyDistribution(measurements)) {
            if (stat.count == 0) continue
            val line = context.getString(
                R.string.pdf_slot_row,
                context.getString(slotLabel(stat.slot)),
                stat.count,
                stat.mean.roundToInt().toString()
            )
            canvas.drawText(line, MARGIN + 8f, y, textPaint)
            y += 14f
        }

        // --- tabella delle misurazioni ---
        y += 12f
        y = drawTableHeader(canvas, y)
        for (m in measurements) {
            if (y > PAGE_HEIGHT - MARGIN) {
                document.finishPage(page)
                pageNumber++
                page = document.startPage(newPageInfo(pageNumber))
                canvas = page.canvas
                y = drawTableHeader(canvas, MARGIN + 10f)
            }
            canvas.drawText(dateTimeFormatter.format(Date(m.timestamp)), COL_DATETIME, y, cellPaint)
            canvas.drawText(m.bloodSugarLevel.toString(), COL_GLUCOSE, y, glucosePaint(m.bloodSugarLevel))
            canvas.drawText(m.fastInsulin?.let { Formatters.formatDose(it) } ?: "-", COL_FAST, y, cellPaint)
            canvas.drawText(m.slowInsulin?.let { Formatters.formatDose(it) } ?: "-", COL_SLOW, y, cellPaint)
            canvas.drawText(m.carbohydrates?.toString() ?: "-", COL_CARBS, y, cellPaint)
            canvas.drawText(m.temporalContext ?: "-", COL_CONTEXT, y, cellPaint)
            val note = m.notes ?: "-"
            canvas.drawText(
                if (note.length > MAX_NOTE_CHARS) note.take(MAX_NOTE_CHARS) + "…" else note,
                COL_NOTES, y, cellPaint
            )
            canvas.drawLine(MARGIN, y + 4f, PAGE_WIDTH - MARGIN, y + 4f, linePaint)
            y += ROW_HEIGHT
        }
        document.finishPage(page)

        // --- salvataggio nella cartella Documents dell'app ---
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "reports")
        if (!dir.exists()) dir.mkdirs()
        val fileName = "GlycoLog_" +
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date()) + ".pdf"
        val file = File(dir, fileName)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    private fun newPageInfo(pageNumber: Int): PdfDocument.PageInfo =
        PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()

    // intestazione della tabella; ritorna la nuova posizione verticale
    private fun drawTableHeader(canvas: android.graphics.Canvas, startY: Float): Float {
        var y = startY
        canvas.drawText(context.getString(R.string.pdf_col_datetime), COL_DATETIME, y, tableHeaderPaint)
        canvas.drawText(context.getString(R.string.pdf_col_glucose), COL_GLUCOSE, y, tableHeaderPaint)
        canvas.drawText(context.getString(R.string.pdf_col_fast), COL_FAST, y, tableHeaderPaint)
        canvas.drawText(context.getString(R.string.pdf_col_slow), COL_SLOW, y, tableHeaderPaint)
        canvas.drawText(context.getString(R.string.pdf_col_carbs), COL_CARBS, y, tableHeaderPaint)
        canvas.drawText(context.getString(R.string.pdf_col_context), COL_CONTEXT, y, tableHeaderPaint)
        canvas.drawText(context.getString(R.string.pdf_col_notes), COL_NOTES, y, tableHeaderPaint)
        canvas.drawLine(MARGIN, y + 5f, PAGE_WIDTH - MARGIN, y + 5f, linePaint)
        y += ROW_HEIGHT + 2f
        return y
    }

    private fun slotLabel(slot: TimeSlot): Int = when (slot) {
        TimeSlot.NIGHT -> R.string.slot_night
        TimeSlot.MORNING -> R.string.slot_morning
        TimeSlot.AFTERNOON -> R.string.slot_afternoon
        TimeSlot.EVENING -> R.string.slot_evening
    }
}
