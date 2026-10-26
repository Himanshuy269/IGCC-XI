package com.example.igccxi.domain.usecase

import android.content.Context
import com.example.igccxi.domain.PdfScorecardExporter
import com.example.igccxi.domain.model.Match
import java.io.File

/**
 * UseCase for exporting and sharing a formatted match scorecard as a PDF document.
 */
class ExportScorecardPdfUseCase {
    operator fun invoke(context: Context, match: Match): File? {
        return PdfScorecardExporter.generateAndSharePdf(context, match)
    }
}
