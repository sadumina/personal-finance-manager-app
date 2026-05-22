package com.example.financeflow.ui.components.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CardWhite = Color(0xFFFFFFFF)
private val TextDark = Color(0xFF1E1B2E)
private val TextMuted = Color(0xFF9CA3AF)
private val IncomeGreen = Color(0xFF22C55E)
private val ExpenseRed = Color(0xFFEF4444)
private val SavingsPurple = Color(0xFF8B5CF6)
private val ChartTrack = Color(0xFFF3F4F6)

data class CashFlowPoint(
    val month: String,
    val income: Int,
    val expenses: Int,
    val savings: Int
)

@Composable
fun CashFlowChartCard(
    points: List<CashFlowPoint> = listOf(
        CashFlowPoint("Jan", 148, 104, 32),
        CashFlowPoint("Feb", 156, 112, 38),
        CashFlowPoint("Mar", 142, 121, 25),
        CashFlowPoint("Apr", 168, 109, 47),
        CashFlowPoint("May", 176, 120, 52)
    )
) {
    val maxValue = points
        .flatMap { listOf(it.income, it.expenses, it.savings) }
        .maxOrNull()
        ?.coerceAtLeast(1) ?: 1

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CardWhite,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cash Flow Trend",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextDark
                    )
                    Text(
                        text = "Income, expenses, and savings by month",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Text(
                    text = "LKR '000",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SavingsPurple
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                points.forEach { point ->
                    ChartMonthGroup(
                        point = point,
                        maxValue = maxValue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChartLegend("Income", IncomeGreen)
                ChartLegend("Expenses", ExpenseRed)
                ChartLegend("Savings", SavingsPurple)
            }
        }
    }
}

@Composable
private fun ChartMonthGroup(
    point: CashFlowPoint,
    maxValue: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ChartTrack)
                .padding(horizontal = 5.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            ChartBar(point.income, maxValue, IncomeGreen)
            Spacer(Modifier.width(4.dp))
            ChartBar(point.expenses, maxValue, ExpenseRed)
            Spacer(Modifier.width(4.dp))
            ChartBar(point.savings, maxValue, SavingsPurple)
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = point.month,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted
        )
    }
}

@Composable
private fun ChartBar(
    value: Int,
    maxValue: Int,
    color: Color,
    maxHeight: Dp = 132.dp
) {
    val fraction = (value.toFloat() / maxValue.toFloat()).coerceIn(0.08f, 1f)

    Box(
        modifier = Modifier
            .width(10.dp)
            .height(maxHeight * fraction)
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .background(color)
    )
}

@Composable
private fun ChartLegend(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextMuted,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3ECFF)
@Composable
fun CashFlowChartCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            CashFlowChartCard()
        }
    }
}
