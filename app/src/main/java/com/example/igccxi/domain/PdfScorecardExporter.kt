package com.example.igccxi.domain

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.igccxi.domain.model.Innings
import com.example.igccxi.domain.model.Match
import java.io.File
import java.io.FileOutputStream

object PdfScorecardExporter {

    fun generateAndSharePdf(context: Context, match: Match): File? {
        val pdfFile = generatePdf(context, match) ?: return null

        val uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Scorecard: ${match.teamA} vs ${match.teamB}")
            putExtra(Intent.EXTRA_TEXT, "Scorecard for ${match.teamA} vs ${match.teamB} (${match.date})\nResult: ${match.result}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooser = Intent.createChooser(shareIntent, "Download / Share Scorecard PDF").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
        return pdfFile
    }

    fun generatePdf(context: Context, match: Match): File? {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        var y = 40f

        // 1. Header Banner
        paint.color = Color.parseColor("#1B5E20") // Deep Cricket Green
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 70f, paint)

        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CRICKET MATCH SCORECARD", 30f, 38f, paint)

        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Official Match Report • IGCC XI Live Scoring", 30f, 56f, paint)

        y = 90f

        // 2. Match Details Box
        paint.color = Color.parseColor("#F5F5F5")
        canvas.drawRoundRect(25f, y, (pageWidth - 25).toFloat(), y + 65f, 8f, 8f, paint)

        paint.color = Color.parseColor("#212121")
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${match.teamA} vs ${match.teamB}", 40f, y + 25f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#616161")
        canvas.drawText("Date: ${match.date}  |  Overs: ${match.totalOvers} ov", 40f, y + 42f, paint)

        val tossInfo = if (match.tossWinner.isNotEmpty()) "Toss: ${match.tossWinner} opted to ${match.tossDecision}" else ""
        val resultInfo = if (match.result.isNotEmpty()) "Result: ${match.result}" else "Match In Progress"
        canvas.drawText("$tossInfo  |  $resultInfo", 40f, y + 55f, paint)

        y += 85f

        // 3. Innings 1
        y = drawInningsSection(canvas, paint, match.innings1, y, pageWidth)

        // 4. Innings 2 (if present)
        if (match.innings2 != null) {
            y = drawInningsSection(canvas, paint, match.innings2, y + 10f, pageWidth)
        }

        // 5. Footer
        paint.color = Color.parseColor("#9E9E9E")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Generated via IGCC XI Simple Cricket Live Scoring App", 30f, pageHeight - 20f, paint)

        document.finishPage(page)

        // Save PDF file to cache directory
        return try {
            val dir = File(context.cacheDir, "scorecards").apply { mkdirs() }
            val file = File(dir, "Scorecard_${match.teamA}_vs_${match.teamB}_${match.id}.pdf")
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            null
        }
    }

    private fun drawInningsSection(
        canvas: Canvas,
        paint: Paint,
        innings: Innings,
        startY: Float,
        pageWidth: Int
    ): Float {
        var y = startY

        // Innings Header Banner
        paint.color = Color.parseColor("#E8F5E9")
        canvas.drawRect(25f, y, (pageWidth - 25).toFloat(), y + 26f, paint)

        paint.color = Color.parseColor("#1B5E20")
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${innings.battingTeam} Innings: ${innings.totalRuns}/${innings.wickets} (${innings.oversFormatted} ov) • CRR: ${innings.currentRunRateFormatted}", 35f, y + 18f, paint)

        y += 34f

        // Batting Table Header
        paint.color = Color.parseColor("#ECEFF1")
        canvas.drawRect(25f, y, (pageWidth - 25).toFloat(), y + 18f, paint)

        paint.color = Color.parseColor("#37474F")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("BATTER", 35f, y + 13f, paint)
        canvas.drawText("R", 280f, y + 13f, paint)
        canvas.drawText("B", 330f, y + 13f, paint)
        canvas.drawText("4s", 380f, y + 13f, paint)
        canvas.drawText("6s", 430f, y + 13f, paint)
        canvas.drawText("SR", 480f, y + 13f, paint)

        y += 22f

        // Batters
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#263238")

        for (b in innings.batters) {
            val status = if (b.isOut) b.dismissal.ifEmpty { "out" } else "not out"
            canvas.drawText("${b.name} ($status)", 35f, y, paint)
            canvas.drawText("${b.runs}", 280f, y, paint)
            canvas.drawText("${b.ballsFaced}", 330f, y, paint)
            canvas.drawText("${b.fours}", 380f, y, paint)
            canvas.drawText("${b.sixes}", 430f, y, paint)
            canvas.drawText(b.strikeRateFormatted, 480f, y, paint)
            y += 14f
        }

        // Extras
        paint.color = Color.parseColor("#546E7A")
        paint.textSize = 8.5f
        canvas.drawText("Extras: ${innings.extras.total} (w ${innings.extras.wides}, nb ${innings.extras.noBalls}, b ${innings.extras.byes}, lb ${innings.extras.legByes})", 35f, y + 2f, paint)

        y += 18f

        // Bowling Table Header
        paint.color = Color.parseColor("#ECEFF1")
        canvas.drawRect(25f, y, (pageWidth - 25).toFloat(), y + 18f, paint)

        paint.color = Color.parseColor("#37474F")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("BOWLER", 35f, y + 13f, paint)
        canvas.drawText("O", 280f, y + 13f, paint)
        canvas.drawText("M", 330f, y + 13f, paint)
        canvas.drawText("R", 380f, y + 13f, paint)
        canvas.drawText("W", 430f, y + 13f, paint)
        canvas.drawText("ECON", 480f, y + 13f, paint)

        y += 22f

        // Bowlers
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#263238")

        for (bw in innings.bowlers) {
            canvas.drawText(bw.name, 35f, y, paint)
            canvas.drawText(bw.oversFormatted, 280f, y, paint)
            canvas.drawText("${bw.maidens}", 330f, y, paint)
            canvas.drawText("${bw.runsConceded}", 380f, y, paint)
            canvas.drawText("${bw.wickets}", 430f, y, paint)
            canvas.drawText(bw.economyFormatted, 480f, y, paint)
            y += 14f
        }

        return y + 8f
    }
}
