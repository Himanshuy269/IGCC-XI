package com.example.igccxi.domain

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.igccxi.domain.model.Innings
import com.example.igccxi.domain.model.Match
import com.example.igccxi.domain.model.MatchStatus
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfScorecardExporter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val LEFT_MARGIN = 28f
    private const val RIGHT_MARGIN = 567f
    private const val CONTENT_WIDTH = RIGHT_MARGIN - LEFT_MARGIN // 539f

    // Theme Colors
    private val COLOR_PRIMARY_DARK = Color.parseColor("#0F3B20")
    private val COLOR_PRIMARY = Color.parseColor("#1B5E20")
    private val COLOR_PRIMARY_LIGHT = Color.parseColor("#E8F5E9")
    private val COLOR_PRIMARY_BORDER = Color.parseColor("#A5D6A7")
    private val COLOR_GOLD = Color.parseColor("#D4AF37")
    private val COLOR_TEXT_MAIN = Color.parseColor("#0F172A")
    private val COLOR_TEXT_MUTED = Color.parseColor("#475569")
    private val COLOR_TEXT_LIGHT = Color.parseColor("#94A3B8")
    private val COLOR_OUT_RED = Color.parseColor("#B91C1C")
    private val COLOR_NOT_OUT_GREEN = Color.parseColor("#15803D")
    private val COLOR_HEADER_BG = Color.parseColor("#1E293B")
    private val COLOR_ROW_ALT = Color.parseColor("#F8FAFC")
    private val COLOR_ROW_WHITE = Color.WHITE
    private val COLOR_DIVIDER = Color.parseColor("#E2E8F0")

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
        val hasSecondInnings = match.innings2 != null
        val totalPages = if (hasSecondInnings) 2 else 1

        val paint = Paint().apply { isAntiAlias = true }

        // ================= PAGE 1 =================
        val pageInfo1 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page1 = document.startPage(pageInfo1)
        val canvas1 = page1.canvas

        // 1. Header Banner
        var y1 = drawHeaderBanner(canvas1, paint, match, isCompact = false)

        // 2. Match Info & Result Card
        y1 = drawMatchInfoCard(canvas1, paint, match, y1)

        // 3. Innings 1 Section
        val innings1Title = if (hasSecondInnings) "1ST INNINGS" else "MATCH INNINGS"
        drawInningsSection(canvas1, paint, match.innings1, innings1Title, y1)

        // 4. Footer
        drawPageFooter(canvas1, paint, 1, totalPages)

        document.finishPage(page1)

        // ================= PAGE 2 (If Second Innings exists) =================
        val innings2 = match.innings2
        if (innings2 != null) {
            val pageInfo2 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
            val page2 = document.startPage(pageInfo2)
            val canvas2 = page2.canvas

            // Compact Header
            var y2 = drawHeaderBanner(canvas2, paint, match, isCompact = true)

            // Target banner if chasing
            if (match.target != null) {
                y2 = drawChaseTargetBanner(canvas2, paint, match, y2)
            }

            // Innings 2 Section
            y2 = drawInningsSection(canvas2, paint, innings2, "2ND INNINGS (CHASE)", y2)

            // Final Result Card (large prominent finish)
            if (match.result.isNotEmpty()) {
                drawFinalResultCard(canvas2, paint, match, y2 + 8f)
            }

            // Footer
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

    private fun drawHeaderBanner(
        canvas: Canvas,
        paint: Paint,
        match: Match,
        isCompact: Boolean
    ): Float {
        val bannerHeight = if (isCompact) 50f else 66f

        // Deep cricket green background
        paint.color = COLOR_PRIMARY_DARK
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), bannerHeight, paint)

        // Gold bottom accent line
        paint.color = COLOR_GOLD
        canvas.drawRect(0f, bannerHeight, PAGE_WIDTH.toFloat(), bannerHeight + 3f, paint)

        // Main Title
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = if (isCompact) 14f else 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val title = if (isCompact) "CRICKET MATCH SCORECARD • 2ND INNINGS" else "CRICKET MATCH SCORECARD"
        canvas.drawText(title, LEFT_MARGIN, if (isCompact) 28f else 32f, paint)

        // Subtitle
        paint.textSize = 9.5f
        paint.color = Color.parseColor("#E2E8F0")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val subtitle = "${match.teamA} vs ${match.teamB} • ${match.date} • IGCC XI Live Cricket"
        canvas.drawText(subtitle, LEFT_MARGIN, if (isCompact) 42f else 52f, paint)

        // Right side badge
        paint.textAlign = Paint.Align.RIGHT
        paint.color = COLOR_GOLD
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("OFFICIAL MATCH REPORT", RIGHT_MARGIN, if (isCompact) 32f else 36f, paint)

        return bannerHeight + 14f
    }

    private fun drawMatchInfoCard(
        canvas: Canvas,
        paint: Paint,
        match: Match,
        startY: Float
    ): Float {
        var y = startY
        val cardHeight = if (match.result.isNotEmpty()) 74f else 54f
        val cardRect = RectF(LEFT_MARGIN, y, RIGHT_MARGIN, y + cardHeight)

        // Card Background & Border
        paint.color = Color.parseColor("#F8FAFC")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(cardRect, 10f, 10f, paint)

        paint.color = COLOR_DIVIDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(cardRect, 10f, 10f, paint)
        paint.style = Paint.Style.FILL

        // Teams Matchup
        paint.textAlign = Paint.Align.LEFT
        paint.color = COLOR_TEXT_MAIN
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${match.teamA}  vs  ${match.teamB}", LEFT_MARGIN + 12f, y + 20f, paint)

        // Overs & Players Info
        paint.textAlign = Paint.Align.RIGHT
        paint.color = COLOR_TEXT_MUTED
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val playerSideText = if (match.playersPerTeam != 11) "${match.playersPerTeam} players per side (${match.maxWickets} wkts)" else "11 players per side"
        canvas.drawText("${match.totalOvers} Overs Match  •  $playerSideText", RIGHT_MARGIN - 12f, y + 20f, paint)

        // Toss & Common Player details
        paint.textAlign = Paint.Align.LEFT
        paint.color = COLOR_TEXT_MUTED
        paint.textSize = 8.5f
        val tossInfo = if (match.tossWinner.isNotEmpty()) "Toss: ${match.tossWinner} opted to ${match.tossDecision}" else "Toss: N/A"
        val commonInfo = if (match.hasCommonPlayer) "  •  Common Player: ${match.commonPlayerName}" else ""
        canvas.drawText("$tossInfo$commonInfo", LEFT_MARGIN + 12f, y + 36f, paint)

        // Result Banner (if completed)
        if (match.result.isNotEmpty()) {
            val resultRect = RectF(LEFT_MARGIN + 10f, y + 46f, RIGHT_MARGIN - 10f, y + 66f)
            paint.color = COLOR_PRIMARY_LIGHT
            canvas.drawRoundRect(resultRect, 6f, 6f, paint)

            paint.color = COLOR_PRIMARY_BORDER
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(resultRect, 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            paint.color = COLOR_PRIMARY
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("🏆 RESULT: ${match.result.uppercase()}", LEFT_MARGIN + 20f, y + 60f, paint)
        }

        return y + cardHeight + 12f
    }

    private fun drawChaseTargetBanner(
        canvas: Canvas,
        paint: Paint,
        match: Match,
        startY: Float
    ): Float {
        val y = startY
        val target = match.target ?: return y
        val rect = RectF(LEFT_MARGIN, y, RIGHT_MARGIN, y + 24f)

        paint.color = Color.parseColor("#EFF6FF")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(rect, 6f, 6f, paint)

        paint.color = Color.parseColor("#BFDBFE")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.parseColor("#1D4ED8")
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        val targetText = "TARGET: $target RUNS (Needs $target runs to win from ${match.totalOvers} overs)"
        canvas.drawText(targetText, LEFT_MARGIN + 12f, y + 16f, paint)

        return y + 32f
    }

    private fun drawInningsSection(
        canvas: Canvas,
        paint: Paint,
        innings: Innings,
        inningsLabel: String,
        startY: Float
    ): Float {
        var y = startY

        // 1. Innings Header Pill / Bar
        val headerHeight = 26f
        val headerRect = RectF(LEFT_MARGIN, y, RIGHT_MARGIN, y + headerHeight)
        paint.color = COLOR_PRIMARY
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(headerRect, 6f, 6f, paint)

        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${innings.battingTeam.uppercase()} ($inningsLabel)", LEFT_MARGIN + 10f, y + 17f, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.WHITE
        paint.textSize = 11f
        val scoreSummary = "${innings.totalRuns} / ${innings.wickets}  (${innings.oversFormatted} ov)   •   CRR: ${innings.currentRunRateFormatted}"
        canvas.drawText(scoreSummary, RIGHT_MARGIN - 10f, y + 17f, paint)

        y += headerHeight + 8f

        // 2. Batting Table Header
        val colBatterX = LEFT_MARGIN + 8f
        val colDismissalX = LEFT_MARGIN + 130f
        val colRunsX = LEFT_MARGIN + 355f
        val colBallsX = LEFT_MARGIN + 395f
        val colFoursX = LEFT_MARGIN + 435f
        val colSixesX = LEFT_MARGIN + 475f
        val colSrX = RIGHT_MARGIN - 8f

        val tableHeaderHeight = 18f
        val tableHeaderRect = RectF(LEFT_MARGIN, y, RIGHT_MARGIN, y + tableHeaderHeight)
        paint.color = COLOR_HEADER_BG
        canvas.drawRoundRect(tableHeaderRect, 4f, 4f, paint)

        paint.color = Color.WHITE
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("BATTER", colBatterX, y + 12.5f, paint)
        canvas.drawText("HOW OUT / BOWLER", colDismissalX, y + 12.5f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("R", colRunsX, y + 12.5f, paint)
        canvas.drawText("B", colBallsX, y + 12.5f, paint)
        canvas.drawText("4s", colFoursX, y + 12.5f, paint)
        canvas.drawText("6s", colSixesX, y + 12.5f, paint)
        canvas.drawText("SR", colSrX, y + 12.5f, paint)

        y += tableHeaderHeight + 2f

        // 3. Batters Rows
        val rowHeight = 17f
        paint.textSize = 8.5f

        for ((index, b) in innings.batters.withIndex()) {
            val isStriker = b.id == innings.currentStrikerId
            val isEven = index % 2 == 0

            // Row background
            paint.color = if (isEven) COLOR_ROW_WHITE else COLOR_ROW_ALT
            paint.style = Paint.Style.FILL
            canvas.drawRect(LEFT_MARGIN, y, RIGHT_MARGIN, y + rowHeight, paint)

            // Batter Name (Bold)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textAlign = Paint.Align.LEFT
            paint.color = COLOR_TEXT_MAIN
            val displayName = if (isStriker) "${b.name} *" else b.name
            drawEllipsizedText(canvas, displayName, colBatterX, y + 12f, 115f, paint)

            // Dismissal / How Out (Clear distinction: Out with bowler, Not out, Yet to bat)
            paint.textAlign = Paint.Align.LEFT
            if (b.isOut) {
                paint.color = COLOR_OUT_RED
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                val outText = b.dismissal.ifEmpty { "out" }
                drawEllipsizedText(canvas, outText, colDismissalX, y + 12f, 215f, paint)
            } else if (b.ballsFaced > 0 || isStriker || b.id == innings.currentNonStrikerId) {
                paint.color = COLOR_NOT_OUT_GREEN
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                canvas.drawText("not out", colDismissalX, y + 12f, paint)
            } else {
                paint.color = COLOR_TEXT_LIGHT
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                canvas.drawText("yet to bat", colDismissalX, y + 12f, paint)
            }

            // Stats Columns (Strictly Right-Aligned)
            paint.textAlign = Paint.Align.RIGHT
            paint.color = COLOR_TEXT_MAIN
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("${b.runs}", colRunsX, y + 12f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = COLOR_TEXT_MUTED
            canvas.drawText("${b.ballsFaced}", colBallsX, y + 12f, paint)
            canvas.drawText("${b.fours}", colFoursX, y + 12f, paint)
            canvas.drawText("${b.sixes}", colSixesX, y + 12f, paint)

            paint.color = COLOR_TEXT_MUTED
            canvas.drawText(b.strikeRateFormatted, colSrX, y + 12f, paint)

            // Row Bottom Divider
            paint.color = COLOR_DIVIDER
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.5f
            canvas.drawLine(LEFT_MARGIN, y + rowHeight, RIGHT_MARGIN, y + rowHeight, paint)
            paint.style = Paint.Style.FILL

            y += rowHeight
        }

        // 4. Extras Row
        val extrasRect = RectF(LEFT_MARGIN, y, RIGHT_MARGIN, y + 16f)
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRect(extrasRect, paint)

        paint.textAlign = Paint.Align.LEFT
        paint.color = COLOR_TEXT_MUTED
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val extrasDetail = "Extras: ${innings.extras.total} (wides ${innings.extras.wides}, no-balls ${innings.extras.noBalls}, byes ${innings.extras.byes}, leg-byes ${innings.extras.legByes})"
        canvas.drawText(extrasDetail, colBatterX, y + 11.5f, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.color = COLOR_TEXT_MAIN
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${innings.extras.total}", colRunsX, y + 11.5f, paint)

        y += 16f

        // 5. Total Score Row
        val totalRect = RectF(LEFT_MARGIN, y, RIGHT_MARGIN, y + 18f)
        paint.color = COLOR_PRIMARY_LIGHT
        canvas.drawRect(totalRect, paint)

        paint.textAlign = Paint.Align.LEFT
        paint.color = COLOR_PRIMARY
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL INNINGS SCORE", colBatterX, y + 12.5f, paint)

        paint.textAlign = Paint.Align.RIGHT
        val totalText = "${innings.totalRuns} / ${innings.wickets} in ${innings.oversFormatted} ov (CRR: ${innings.currentRunRateFormatted})"
        canvas.drawText(totalText, RIGHT_MARGIN - 8f, y + 12.5f, paint)

        y += 24f

        // 6. Bowling Table Header
        val colBowlerNameX = LEFT_MARGIN + 8f
        val colOversX = LEFT_MARGIN + 325f
        val colMaidensX = LEFT_MARGIN + 380f
        val colRunsConcededX = LEFT_MARGIN + 435f
        val colWicketsX = LEFT_MARGIN + 490f
        val colEconX = RIGHT_MARGIN - 8f

        val bowlingHeaderRect = RectF(LEFT_MARGIN, y, RIGHT_MARGIN, y + tableHeaderHeight)
        paint.color = COLOR_HEADER_BG
        canvas.drawRoundRect(bowlingHeaderRect, 4f, 4f, paint)

        paint.color = Color.WHITE
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("BOWLER", colBowlerNameX, y + 12.5f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("O", colOversX, y + 12.5f, paint)
        canvas.drawText("M", colMaidensX, y + 12.5f, paint)
        canvas.drawText("R", colRunsConcededX, y + 12.5f, paint)
        canvas.drawText("W", colWicketsX, y + 12.5f, paint)
        canvas.drawText("ECON", colEconX, y + 12.5f, paint)

        y += tableHeaderHeight + 2f

        // 7. Bowlers Rows
        paint.textSize = 8.5f
        for ((index, bw) in innings.bowlers.withIndex()) {
            val isEven = index % 2 == 0

            // Row background
            paint.color = if (isEven) COLOR_ROW_WHITE else COLOR_ROW_ALT
            paint.style = Paint.Style.FILL
            canvas.drawRect(LEFT_MARGIN, y, RIGHT_MARGIN, y + rowHeight, paint)

            // Bowler Name
            paint.textAlign = Paint.Align.LEFT
            paint.color = COLOR_TEXT_MAIN
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            drawEllipsizedText(canvas, bw.name, colBowlerNameX, y + 12f, 250f, paint)

            // Stats Columns (Strictly Right-Aligned)
            paint.textAlign = Paint.Align.RIGHT
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = COLOR_TEXT_MUTED
            canvas.drawText(bw.oversFormatted, colOversX, y + 12f, paint)
            canvas.drawText("${bw.maidens}", colMaidensX, y + 12f, paint)
            canvas.drawText("${bw.runsConceded}", colRunsConcededX, y + 12f, paint)

            // Wickets (Bold red/highlight if > 0)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = if (bw.wickets > 0) COLOR_OUT_RED else COLOR_TEXT_MAIN
            canvas.drawText("${bw.wickets}", colWicketsX, y + 12f, paint)

            // Economy
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = COLOR_TEXT_MUTED
            canvas.drawText(bw.economyFormatted, colEconX, y + 12f, paint)

            // Row divider
            paint.color = COLOR_DIVIDER
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.5f
            canvas.drawLine(LEFT_MARGIN, y + rowHeight, RIGHT_MARGIN, y + rowHeight, paint)
            paint.style = Paint.Style.FILL

            y += rowHeight
        }

        return y + 10f
    }

    private fun drawFinalResultCard(
        canvas: Canvas,
        paint: Paint,
        match: Match,
        startY: Float
    ) {
        val y = startY
        val cardRect = RectF(LEFT_MARGIN, y, RIGHT_MARGIN, y + 42f)

        paint.color = COLOR_PRIMARY_LIGHT
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(cardRect, 8f, 8f, paint)

        paint.color = COLOR_PRIMARY_BORDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        canvas.drawRoundRect(cardRect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        paint.textAlign = Paint.Align.CENTER
        paint.color = COLOR_PRIMARY
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val centerX = (LEFT_MARGIN + RIGHT_MARGIN) / 2f
        canvas.drawText("🏆 MATCH RESULT", centerX, y + 17f, paint)

        paint.textSize = 11f
        paint.color = COLOR_TEXT_MAIN
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(match.result.uppercase(), centerX, y + 33f, paint)
    }

    private fun drawPageFooter(
        canvas: Canvas,
        paint: Paint,
        pageNumber: Int,
        totalPages: Int
    ) {
        val footerY = PAGE_HEIGHT - 22f

        // Thin divider
        paint.color = COLOR_DIVIDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        canvas.drawLine(LEFT_MARGIN, footerY - 10f, RIGHT_MARGIN, footerY - 10f, paint)
        paint.style = Paint.Style.FILL

        // Left Note
        paint.textAlign = Paint.Align.LEFT
        paint.color = COLOR_TEXT_LIGHT
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        val timeStamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        canvas.drawText("Generated via IGCC XI Live Cricket Scoring App • Exported: $timeStamp", LEFT_MARGIN, footerY, paint)

        // Right Page Number
        paint.textAlign = Paint.Align.RIGHT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Page $pageNumber of $totalPages", RIGHT_MARGIN, footerY, paint)
    }

    private fun drawEllipsizedText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        paint: Paint
    ) {
        if (paint.measureText(text) <= maxWidth) {
            canvas.drawText(text, x, y, paint)
            return
        }
        var truncated = text
        while (truncated.isNotEmpty() && paint.measureText("$truncated…") > maxWidth) {
            truncated = truncated.dropLast(1)
        }
        canvas.drawText("$truncated…", x, y, paint)
    }
}
