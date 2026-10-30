package com.example.igccxi.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igccxi.ui.theme.CommonPlayerBlue
import com.example.igccxi.ui.theme.CommonPlayerBlueBorder
import com.example.igccxi.ui.theme.CommonPlayerBlueContainer

/**
 * Special "CMN" Badge in blue color representing a Common Player in Gully Cricket.
 * The common player plays, bats, and bowls for both teams.
 */
@Composable
fun CommonPlayerBadge(
    modifier: Modifier = Modifier,
    label: String = "CMN"
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = CommonPlayerBlueContainer,
        border = BorderStroke(0.6.dp, CommonPlayerBlueBorder),
        modifier = modifier
    ) {
        Text(
            text = label,
            color = CommonPlayerBlue,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        )
    }
}
