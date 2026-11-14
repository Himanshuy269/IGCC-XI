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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfScorecardExporter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val LEFT_MARGIN = 24f
    private const val RIGHT_MARGIN = 571f

    // Clean Design Colors matching the official cricket scorecard UI
    private val COLOR_DARK_GREEN = Color.parseColor("#155D27")  // Innings bar background
    private val COLOR_SAGE_GREEN = Color.parseColor("#85A885")  // Table header bar
    private val COLOR_HEADER_TEXT = Color.parseColor("#111111")
    private val COLOR_TEXT_MAIN = Color.parseColor("#111111")
    private val COLOR_TEXT_MUTED = Color.parseColor("#555555")
    private val COLOR_TEXT_LIGHT = Color.parseColor("#777777")
    private val COLOR_DIVIDER = Color.parseColor("#D1D5DB")
    private val COLOR_TOP_LINE = Color.parseColor("#333333")

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
            putExtra(Intent.EXTRA_SUBJECT, "Scorecard: ${match.teamA} v/s ${match.teamB}")
            putExtra(Intent.EXTRA_TEXT, "Scorecard: ${match.teamA} v/s ${match.teamB} (${match.date})\nResult: ${match.result}")
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
        val hasSecondInnings = match.innings2 != null
        val totalPages = if (hasSecondInnings) 2 else 1

        val paint = Paint().apply { isAntiAlias = true }

        // ================= PAGE 1: INNINGS 1 =================
        val pageInfo1 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page1 = document.startPage(pageInfo1)
        val canvas1 = page1.canvas

        // 1. Clean Match Header
        var y1 = drawMatchHeader(canvas1, paint, match)

        // 2. Innings 1 Section (Full Scorecard)
        drawInningsSection(canvas1, paint, match, match.innings1, y1)

        // 3. Footer
        drawPageFooter(canvas1, paint, 1, totalPages)

        document.finishPage(page1)

        // ================= PAGE 2: INNINGS 2 (If exists) =================
        val innings2 = match.innings2
        if (innings2 != null) {
            val pageInfo2 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
            val page2 = document.startPage(pageInfo2)
            val canvas2 = page2.canvas

            // 1. Clean Match Header
            var y2 = drawMatchHeader(canvas2, paint, match)

            // 2. Innings 2 Section (Full Scorecard)
            drawInningsSection(canvas2, paint, match, innings2, y2)

            // 3. Footer
            drawPageFooter(canvas2, paint, 2, totalPages)

            document.finishPage(page2)
        }

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

    /**
     * Draws the clean header:
     * -----------------------------
     *       Team A v/s Team B
     * -----------------------------
     * Match Result (or overs/date)
     */
    private fun drawMatchHeader(
        canvas: Canvas,
        paint: Paint,
        match: Match
    ): Float {
        var y = 22f

        // Top horizontal divider
        paint.color = COLOR_TOP_LINE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        canvas.drawLine(LEFT_MARGIN, y, RIGHT_MARGIN, y, paint)
        paint.style = Paint.Style.FILL

        y += 18f

        // Match Title: "Team A v/s Team B" (Centered, Bold)
        paint.color = COLOR_TEXT_MAIN
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 17f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val title = "${match.teamA} v/s ${match.teamB}"
        val centerX = (LEFT_MARGIN + RIGHT_MARGIN) / 2f
        canvas.drawText(title, centerX, y, paint)

        y += 7f

        // Bottom horizontal divider under title
        paint.color = COLOR_TOP_LINE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        canvas.drawLine(LEFT_MARGIN, y, RIGHT_MARGIN, y, paint)
        paint.style = Paint.Style.FILL

        y += 13f

        // Result Subtitle on left (e.g. "Junior won by 12 runs.")
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 9f
        paint.color = COLOR_TEXT_MAIN
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        val resultText = if (match.result.isNotBlank()) {
            val res = match.result.trim()
            if (res.endsWith(".")) res else "$res."
        } else {
            val toss = if (match.tossWinner.isNotBlank()) "Toss: ${match.tossWinner} chose to ${match.tossDecision}" else ""
            "${match.totalOvers} Overs Match • ${match.date} ${if (toss.isNotEmpty()) "• $toss" else ""}"
        }
        canvas.drawText(resultText, LEFT_MARGIN, y, paint)

        return y + 9f
    }

    /**
     * Draws a complete Innings section:
     * - Dark green banner: [Team Name] [Score-Wickets (Overs)]
     * - Batsman Table: Batsman, R, B, 4s, 6s, SR with 2-line rows (Name + Dismissal)
     * - Extras row: Extras (total) b B, lb LB, wd WD, nb NB, 0 P
     * - Total row: Total Score-Wickets (Overs) CRR
     * - Bowler Table: Bowler, O, M, R, W, ER
     * - Fall of wickets Table: Fall of wickets, Score, Over
     */
    private fun drawInningsSection(
        canvas: Canvas,
        paint: Paint,
        match: Match,
        innings: Innings,
        startY: Float
    ): Float {
        var y = startY

        // 1. Dark Green Innings Header Banner
        val bannerHeight = 18f
        paint.color = COLOR_DARK_GREEN
        paint.style = Paint.Style.FILL
        canvas.drawRect(LEFT_MARGIN, y, RIGHT_MARGIN, y + bannerHeight, paint)

        // Left: Team Name (Bold White)
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(innings.battingTeam, LEFT_MARGIN + 6f, y + 13f, paint)

        // Right: Score-Wickets (Overs) (Bold White) e.g. "52-7 (8.0)"
        paint.textAlign = Paint.Align.RIGHT
        val inningsScoreText = "${innings.totalRuns}-${innings.wickets} (${innings.oversFormatted})"
        canvas.drawText(inningsScoreText, RIGHT_MARGIN - 6f, y + 13f, paint)

        y += bannerHeight

        // 2. Batting Table Column Alignments
        val colBatterX = LEFT_MARGIN + 4f
        val colRunsX = LEFT_MARGIN + 320f
        val colBallsX = LEFT_MARGIN + 380f
        val colFoursX = LEFT_MARGIN + 435f
        val colSixesX = LEFT_MARGIN + 490f
        val colSrX = RIGHT_MARGIN - 4f

        // Table Header: Soft Sage Green Bar
        val tableHeaderHeight = 15f
        paint.color = COLOR_SAGE_GREEN
        canvas.drawRect(LEFT_MARGIN, y, RIGHT_MARGIN, y + tableHeaderHeight, paint)

        paint.color = COLOR_HEADER_TEXT
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Batsman", colBatterX, y + 11f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("R", colRunsX, y + 11f, paint)
        canvas.drawText("B", colBallsX, y + 11f, paint)
        canvas.drawText("4s", colFoursX, y + 11f, paint)
        canvas.drawText("6s", colSixesX, y + 11f, paint)
        canvas.drawText("SR", colSrX, y + 11f, paint)

        y += tableHeaderHeight

        // 3. Batters Rows (2 lines per batter: Line 1 name & stats, Line 2 dismissal)
        val batterRowHeight = 25f
        val effectiveFow = innings.getEffectiveFallOfWickets()

        for (b in innings.batters) {
            val isStriker = b.id == innings.currentStrikerId
            val isNonStriker = b.id == innings.currentNonStrikerId
            val isAtCrease = isStriker || isNonStriker

            // Robust check for whether batter was dismissed:
            val isBatterOut = b.isOut ||
                    (b.dismissal.isNotBlank() && !b.dismissal.equals("not out", ignoreCase = true) && !b.dismissal.equals("yet to bat", ignoreCase = true)) ||
                    (b.id.isNotEmpty() && effectiveFow.any { it.batterId == b.id }) ||
                    (!isAtCrease && (b.ballsFaced > 0 || b.runs > 0) && innings.wickets > 0)

            val dismissalText = when {
                isBatterOut -> b.dismissal.ifBlank { "out" }
                isAtCrease -> "not out"
                b.ballsFaced > 0 || b.runs > 0 -> "not out"
                else -> "not out"
            }

            // Line 1: Batter Name
            paint.textAlign = Paint.Align.LEFT
            paint.color = COLOR_TEXT_MAIN
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8.5f
            val batterDisplayName = if (match.isCommonPlayer(b.name)) "${b.name} [CMN]" else b.name
            canvas.drawText(batterDisplayName, colBatterX, y + 10f, paint)

            // Line 1 Stats: R, B, 4s, 6s, SR (Right-Aligned)
            paint.textAlign = Paint.Align.RIGHT
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("${b.runs}", colRunsX, y + 10f, paint)
            canvas.drawText("${b.ballsFaced}", colBallsX, y + 10f, paint)
            canvas.drawText("${b.fours}", colFoursX, y + 10f, paint)
            canvas.drawText("${b.sixes}", colSixesX, y + 10f, paint)

            val srText = String.format(Locale.US, "%.2f", b.strikeRate)
            canvas.drawText(srText, colSrX, y + 10f, paint)

            // Line 2: Dismissal / How out (Muted dark gray text right below name)
            paint.textAlign = Paint.Align.LEFT
            paint.color = COLOR_TEXT_MUTED
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(dismissalText, colBatterX, y + 21f, paint)

            // Bottom Divider
            paint.color = COLOR_DIVIDER
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.5f
            canvas.drawLine(LEFT_MARGIN, y + batterRowHeight, RIGHT_MARGIN, y + batterRowHeight, paint)
            paint.style = Paint.Style.FILL

            y += batterRowHeight
        }

        // 4. Extras Row
        val extrasRowHeight = 15f
        paint.textAlign = Paint.Align.LEFT
        paint.color = COLOR_TEXT_MAIN
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Extras", colBatterX, y + 11f, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val extrasSummary = "(${innings.extras.total}) ${innings.extras.byes} B, ${innings.extras.legByes} LB, ${innings.extras.wides} WD, ${innings.extras.noBalls} NB, 0 P"
        canvas.drawText(extrasSummary, RIGHT_MARGIN - 4f, y + 11f, paint)

        // Divider
        paint.color = COLOR_DIVIDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.5f
        canvas.drawLine(LEFT_MARGIN, y + extrasRowHeight, RIGHT_MARGIN, y + extrasRowHeight, paint)
        paint.style = Paint.Style.FILL

        y += extrasRowHeight

        // 5. Total Row
        val totalRowHeight = 15f
        paint.textAlign = Paint.Align.LEFT
        paint.color = COLOR_TEXT_MAIN
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Total", colBatterX, y + 11f, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val crrFormatted = String.format(Locale.US, "%.2f", innings.currentRunRate)
        val totalSummary = "${innings.totalRuns}-${innings.wickets} (${innings.oversFormatted}) $crrFormatted"
        canvas.drawText(totalSummary, RIGHT_MARGIN - 4f, y + 11f, paint)

        // Divider
        paint.color = COLOR_DIVIDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.5f
        canvas.drawLine(LEFT_MARGIN, y + totalRowHeight, RIGHT_MARGIN, y + totalRowHeight, paint)
        paint.style = Paint.Style.FILL

        y += totalRowHeight

        // 6. Bowler Table Header: Soft Sage Green Bar
        val colBowlerX = LEFT_MARGIN + 4f
        val colOversX = LEFT_MARGIN + 320f
        val colMaidensX = LEFT_MARGIN + 380f
        val colRunsConcededX = LEFT_MARGIN + 435f
        val colWicketsX = LEFT_MARGIN + 490f
        val colEconX = RIGHT_MARGIN - 4f

        paint.color = COLOR_SAGE_GREEN
        canvas.drawRect(LEFT_MARGIN, y, RIGHT_MARGIN, y + tableHeaderHeight, paint)

        paint.color = COLOR_HEADER_TEXT
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Bowler", colBowlerX, y + 11f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("O", colOversX, y + 11f, paint)
        canvas.drawText("M", colMaidensX, y + 11f, paint)
        canvas.drawText("R", colRunsConcededX, y + 11f, paint)
        canvas.drawText("W", colWicketsX, y + 11f, paint)
        canvas.drawText("ER", colEconX, y + 11f, paint)

        y += tableHeaderHeight

        // 7. Bowler Rows
        val bowlerRowHeight = 15f
        paint.textSize = 8.5f

        for (bw in innings.bowlers) {
            paint.textAlign = Paint.Align.LEFT
            paint.color = COLOR_TEXT_MAIN
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val bowlerDisplayName = if (match.isCommonPlayer(bw.name)) "${bw.name} [CMN]" else bw.name
            canvas.drawText(bowlerDisplayName, colBowlerX, y + 11f, paint)

            paint.textAlign = Paint.Align.RIGHT
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(bw.oversFormatted, colOversX, y + 11f, paint)
            canvas.drawText("${bw.maidens}", colMaidensX, y + 11f, paint)
            canvas.drawText("${bw.runsConceded}", colRunsConcededX, y + 11f, paint)
            canvas.drawText("${bw.wickets}", colWicketsX, y + 11f, paint)

            val erText = String.format(Locale.US, "%.2f", bw.economyRate)
            canvas.drawText(erText, colEconX, y + 11f, paint)

            // Divider
            paint.color = COLOR_DIVIDER
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.5f
            canvas.drawLine(LEFT_MARGIN, y + bowlerRowHeight, RIGHT_MARGIN, y + bowlerRowHeight, paint)
            paint.style = Paint.Style.FILL

            y += bowlerRowHeight
        }

        // 8. Fall of Wickets Table
        val fowList = effectiveFow
        if (fowList.isNotEmpty()) {
            val colFowNameX = LEFT_MARGIN + 4f
            val colFowScoreX = LEFT_MARGIN + 440f
            val colFowOverX = RIGHT_MARGIN - 4f

            // Header: Soft Sage Green Bar
            paint.color = COLOR_SAGE_GREEN
            canvas.drawRect(LEFT_MARGIN, y, RIGHT_MARGIN, y + tableHeaderHeight, paint)

            paint.color = COLOR_HEADER_TEXT
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("Fall of wickets", colFowNameX, y + 11f, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Score", colFowScoreX, y + 11f, paint)
            canvas.drawText("Over", colFowOverX, y + 11f, paint)

            y += tableHeaderHeight

            // FOW Rows
            val fowRowHeight = 15f
            for (fow in fowList) {
                paint.textAlign = Paint.Align.LEFT
                paint.color = COLOR_TEXT_MAIN
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 8.5f
                canvas.drawText(fow.batterName, colFowNameX, y + 11f, paint)

                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText("${fow.score}/${fow.wicketNumber}", colFowScoreX, y + 11f, paint)
                canvas.drawText(fow.oversFormatted, colFowOverX, y + 11f, paint)

                // Divider
                paint.color = COLOR_DIVIDER
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 0.5f
                canvas.drawLine(LEFT_MARGIN, y + fowRowHeight, RIGHT_MARGIN, y + fowRowHeight, paint)
                paint.style = Paint.Style.FILL

                y += fowRowHeight
            }
        }

        return y + 12f
    }

    private fun drawPageFooter(
        canvas: Canvas,
        paint: Paint,
        pageNumber: Int,
        totalPages: Int
    ) {
        val footerY = PAGE_HEIGHT - 18f

        // Thin divider
        paint.color = COLOR_DIVIDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.6f
        canvas.drawLine(LEFT_MARGIN, footerY - 8f, RIGHT_MARGIN, footerY - 8f, paint)
        paint.style = Paint.Style.FILL

        // Left note
        paint.textAlign = Paint.Align.LEFT
        paint.color = COLOR_TEXT_LIGHT
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val timeStamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        canvas.drawText("Generated by IGCC XI Live Cricket • $timeStamp", LEFT_MARGIN, footerY, paint)

        // Right page number
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Page $pageNumber of $totalPages", RIGHT_MARGIN, footerY, paint)
    }
}
