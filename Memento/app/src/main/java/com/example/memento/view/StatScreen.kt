package com.example.memento.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memento.viewmodel.UserViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val Bg       = Color(0xFF0D0D1A)
private val Surface  = Color(0xFF16162A)
private val Surface2 = Color(0xFF1E1E35)
private val TextCol  = Color(0xFFE8E8F5)
private val Muted    = Color(0xFF5A5A80)
private val Accent   = Color(0xFF7C3AED)
private val AccentSoft = Color(0xFFA78BFA)
private val Green    = Color(0xFF22C55E)
private val Border   = Color(0xFF2A2A48)

@Composable
fun StatScreen(viewModel: UserViewModel) {
    val birthday = viewModel.birthday

    if (birthday == null) {
        Box(Modifier.fillMaxSize().background(Bg), contentAlignment = Alignment.Center) {
            Text(
                "Set your birthday to see your stats.",
                color = Muted,
                fontSize = 15.sp,
            )
        }
        return
    }

    val today = remember { LocalDate.now() }
    val daysLived    = ChronoUnit.DAYS.between(birthday, today).toInt().coerceAtLeast(0)
    val weeksLived   = daysLived / 7
    val yearsLived   = ChronoUnit.YEARS.between(birthday, today).toInt().coerceAtLeast(0)
    val totalDays    = (viewModel.lifeExpectancyYears * 365.25).toInt()
    val daysLeft     = (totalDays - daysLived).coerceAtLeast(0)
    val weeksLeft    = daysLeft / 7
    val percentLived = (daysLived.toFloat() / totalDays).coerceIn(0f, 1f)

    val hoursLived     = daysLived * 24L
    val heartbeats     = daysLived * 103_680L   // ~72 BPM × 60 × 24
    val fmt = remember { NumberFormat.getNumberInstance() }

    // Milestone: next birthday
    val nextBirthday = birthday.withYear(today.year)
        .let { if (!it.isAfter(today)) it.plusYears(1) else it }
    val daysToNextBirthday = ChronoUnit.DAYS.between(today, nextBirthday).toInt()
    val nextAge = ChronoUnit.YEARS.between(birthday, nextBirthday).toInt()

    // Milestone: next round day (next multiple of 1 000)
    val nextRoundDay = ((daysLived / 1_000) + 1) * 1_000
    val daysToRoundDay = nextRoundDay - daysLived

    // Milestone: next round week (next multiple of 100)
    val nextRoundWeek = ((weeksLived / 100) + 1) * 100
    val daysToRoundWeek = nextRoundWeek * 7 - daysLived

    // Milestone: 1 billion seconds alive (= 11 574 days after birth)
    val billionSecondsDate = birthday.plusDays(11_574)
    val daysToBillion = ChronoUnit.DAYS.between(today, billionSecondsDate).toInt()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Section 1: Life at a Glance ──────────────────────────────
        item {
            SectionHeader("Life at a Glance")
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Progress bar
                Text(
                    "${(percentLived * 100).toInt()}% of your expected life lived",
                    color = Muted,
                    fontSize = 12.sp
                )
                LinearProgressIndicator(
                    progress = { percentLived },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Accent,
                    trackColor = Surface2,
                )

                Spacer(Modifier.height(4.dp))

                // 2×2 grid of stat cards
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        label = "Weeks lived",
                        value = fmt.format(weeksLived),
                        color = AccentSoft,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Weeks remaining",
                        value = fmt.format(weeksLeft),
                        color = Green,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        label = "Years lived",
                        value = yearsLived.toString(),
                        color = AccentSoft,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Years remaining",
                        value = fmt.format(weeksLeft / 52),
                        color = Green,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ── Section 4: Time in Numbers ────────────────────────────────
        item { SectionHeader("Time in Numbers") }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                TimeRow("Days alive",        fmt.format(daysLived))
                TimeRow("Hours alive",       fmt.format(hoursLived))
                TimeRow("Sunrises witnessed",fmt.format(daysLived))
                TimeRow("Heartbeats",        formatLarge(heartbeats))
            }
        }

        // ── Section 6: Milestone Countdown ───────────────────────────
        item { SectionHeader("Upcoming Milestones") }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                MilestoneRow(
                    title = "Turn $nextAge years old",
                    daysAway = daysToNextBirthday,
                    date = nextBirthday.toString()
                )
                MilestoneRow(
                    title = "Your ${fmt.format(nextRoundDay)}th day alive",
                    daysAway = daysToRoundDay,
                    date = today.plusDays(daysToRoundDay.toLong()).toString()
                )
                MilestoneRow(
                    title = "Your ${fmt.format(nextRoundWeek)}th week alive",
                    daysAway = daysToRoundWeek,
                    date = today.plusDays(daysToRoundWeek.toLong()).toString()
                )
                if (daysToBillion > 0) {
                    MilestoneRow(
                        title = "1,000,000,000 seconds alive",
                        daysAway = daysToBillion,
                        date = billionSecondsDate.toString()
                    )
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        color = Muted,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Surface2, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Text(value, color = color, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun TimeRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = TextCol, fontSize = 14.sp)
        Text(value, color = AccentSoft, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
    Box(Modifier.fillMaxWidth().height(0.5.dp).background(Border))
}

@Composable
private fun MilestoneRow(title: String, daysAway: Int, date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextCol, fontSize = 14.sp)
            Text(date, color = Muted, fontSize = 11.sp)
        }
        Text(
            text = "in $daysAway days",
            color = Green,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
    Box(Modifier.fillMaxWidth().height(0.5.dp).background(Border))
}

private fun formatLarge(n: Long): String = when {
    n >= 1_000_000_000 -> "%.1fB".format(n / 1_000_000_000.0)
    n >= 1_000_000     -> "%.1fM".format(n / 1_000_000.0)
    else               -> NumberFormat.getNumberInstance().format(n)
}
