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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memento.ui.theme.LocalAppColors
import com.example.memento.viewmodel.UserViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun StatScreen(viewModel: UserViewModel) {
    val c = LocalAppColors.current
    val birthday = viewModel.birthday

    if (birthday == null) {
        Box(Modifier.fillMaxSize().background(c.bg), contentAlignment = Alignment.Center) {
            Text("Set your birthday to see your stats.", color = c.muted, fontSize = 15.sp)
        }
        return
    }

    val factOfTheDay by viewModel.factOfTheDay.collectAsState()
    val today = remember { LocalDate.now() }
    val daysLived    = ChronoUnit.DAYS.between(birthday, today).toInt().coerceAtLeast(0)
    val weeksLived   = daysLived / 7
    val yearsLived   = ChronoUnit.YEARS.between(birthday, today).toInt().coerceAtLeast(0)
    val totalDays    = (viewModel.lifeExpectancyYears * 365.25).toInt()
    val daysLeft     = (totalDays - daysLived).coerceAtLeast(0)
    val weeksLeft    = daysLeft / 7
    val percentLived = (daysLived.toFloat() / totalDays).coerceIn(0f, 1f)

    val hoursLived = daysLived * 24L
    val heartbeats = daysLived * 103_680L   // ~72 BPM × 60 × 24
    val fmt = remember { NumberFormat.getNumberInstance() }

    val nextBirthday = birthday.withYear(today.year)
        .let { if (!it.isAfter(today)) it.plusYears(1) else it }
    val daysToNextBirthday = ChronoUnit.DAYS.between(today, nextBirthday).toInt()
    val nextAge = ChronoUnit.YEARS.between(birthday, nextBirthday).toInt()

    val nextRoundDay = ((daysLived / 1_000) + 1) * 1_000
    val daysToRoundDay = nextRoundDay - daysLived

    val nextRoundWeek = ((weeksLived / 100) + 1) * 100
    val daysToRoundWeek = nextRoundWeek * 7 - daysLived

    val billionSecondsDate = birthday.plusDays(11_574)
    val daysToBillion = ChronoUnit.DAYS.between(today, billionSecondsDate).toInt()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { StatSectionHeader("Life at a Glance") }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.surface, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "${(percentLived * 100).toInt()}% of your expected life lived",
                    color = c.muted,
                    fontSize = 12.sp
                )
                LinearProgressIndicator(
                    progress = { percentLived },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = c.accent,
                    trackColor = c.surface2,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(label = "Weeks lived",     value = fmt.format(weeksLived), color = c.accentSoft, modifier = Modifier.weight(1f))
                    StatCard(label = "Weeks remaining", value = fmt.format(weeksLeft),  color = c.green,      modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(label = "Years lived",     value = yearsLived.toString(),      color = c.accentSoft, modifier = Modifier.weight(1f))
                    StatCard(label = "Years remaining", value = fmt.format(weeksLeft / 52), color = c.green,      modifier = Modifier.weight(1f))
                }
            }
        }

        item { StatSectionHeader("On This Day") }
        item { FactOfTheDayCard(fact = factOfTheDay) }

        item { StatSectionHeader("Time in Numbers") }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.surface, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                TimeRow("Days alive",         fmt.format(daysLived))
                TimeRow("Hours alive",        fmt.format(hoursLived))
                TimeRow("Sunrises witnessed", fmt.format(daysLived))
                TimeRow("Heartbeats",         formatLarge(heartbeats))
            }
        }

        item { StatSectionHeader("Upcoming Milestones") }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.surface, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                MilestoneRow("Turn $nextAge years old",              daysToNextBirthday, nextBirthday.toString())
                MilestoneRow("Your ${fmt.format(nextRoundDay)}th day alive",  daysToRoundDay,  today.plusDays(daysToRoundDay.toLong()).toString())
                MilestoneRow("Your ${fmt.format(nextRoundWeek)}th week alive", daysToRoundWeek, today.plusDays(daysToRoundWeek.toLong()).toString())
                if (daysToBillion > 0) {
                    MilestoneRow("1,000,000,000 seconds alive", daysToBillion, billionSecondsDate.toString())
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun StatSectionHeader(title: String) {
    val c = LocalAppColors.current
    Text(
        text = title.uppercase(),
        color = c.muted,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Column(
        modifier = modifier
            .background(c.surface2, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Text(value, color = color, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(label, color = c.muted, fontSize = 12.sp)
    }
}

@Composable
private fun TimeRow(label: String, value: String) {
    val c = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = c.text, fontSize = 14.sp)
        Text(value, color = c.accentSoft, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
    Box(Modifier.fillMaxWidth().height(0.5.dp).background(c.border))
}

@Composable
private fun MilestoneRow(title: String, daysAway: Int, date: String) {
    val c = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = c.text, fontSize = 14.sp)
            Text(date, color = c.muted, fontSize = 11.sp)
        }
        Text(
            text = "in $daysAway days",
            color = c.green,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
    Box(Modifier.fillMaxWidth().height(0.5.dp).background(c.border))
}

@Composable
private fun FactOfTheDayCard(fact: String?) {
    val c = LocalAppColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surface, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        if (fact == null) {
            Text("Loading...", color = c.muted, fontSize = 14.sp)
        } else {
            Text(fact, color = c.text, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

private fun formatLarge(n: Long): String = when {
    n >= 1_000_000_000 -> "%.1fB".format(n / 1_000_000_000.0)
    n >= 1_000_000     -> "%.1fM".format(n / 1_000_000.0)
    else               -> NumberFormat.getNumberInstance().format(n)
}
