package com.example.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CustomCalendar(
//    config: HorizontalCalendarConfig = HorizontalCalendarConfig(),
) {
    var currentDate by remember {
        mutableStateOf(LocalDate.now())
    }

    val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.elevatedCardElevation(3.dp)
    ) {
        Column {
            CalendarHeader(yearMonth = currentDate.format(dateTimeFormatter),
                onNextMonth = { currentDate = currentDate.plusMonths(1).withDayOfMonth(1) }) {
                currentDate = currentDate.minusMonths(1).withDayOfMonth(1)
            }
            CalendarBody(currentDate = currentDate)
        }
    }
}

@Composable
fun CalendarHeader(yearMonth: String, onNextMonth: () -> Unit, onPreviousMonth: () -> Unit) {
    val titleStyle = androidx.compose.ui.text.TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
    )

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = yearMonth, style = titleStyle)
        Row {
            Icon(imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = null,
                modifier = Modifier.noRippleClickable { onPreviousMonth() })
            Icon(imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.noRippleClickable { onNextMonth() })

        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarBody(currentDate: LocalDate) {
    val firstDayOfWeek = currentDate.withDayOfMonth(1).dayOfWeek.value // 첫 주에 시작하는 요일 ex) 5(금요일)
    val lastDay = currentDate.lengthOfMonth()        // 마지막 일자, ex) 31
    val days = IntRange(1, lastDay).toList()    // ex) 1, 2, 3, 4, ... , 31
    val today = LocalDate.now()

    var selectedDate: LocalDate? by remember {
        mutableStateOf(null)
    }

    Column {
        HorizontalDayOfWeek()
        LazyVerticalGrid(columns = GridCells.Fixed(7)) {
            for (i in 1..firstDayOfWeek) { // 일요일부터 시작하니까 .. 사용, 월요일부터 시작하면 until 사용
                item { Box(modifier = Modifier.weight(1f)) }
            }
            items(key = { day -> day }, items = days) { day ->
                // 이번 달의 날짜를 day로 치환하여 CalendarDay로 넘긴다. ex) 2024-05-01
                val date = currentDate.withDayOfMonth(day)
                CalendarDay(
                    day = date,
                    isToday = (date == today),
                    selected = (date == selectedDate),
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
//                        .pointerInput(Unit) {
//                            detectTapGestures {
//                                Log.d("daeyoung", "tab: $it")
//                            }
//                        }

                ) { selectedDate = if (selectedDate == null || selectedDate != date) date else null }
            }
        }
    }


}

@Composable
fun HorizontalDayOfWeek() {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val dayOfWeek = arrayOf("일", "월", "화", "수", "목", "금", "토")
        repeat(7) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
            ) {
                Text(
                    text = dayOfWeek[it],
                    modifier = Modifier.align(Alignment.Center)
                )
            }

        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarDay(
    day: LocalDate,
    isToday: Boolean,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
//    val dayStyle = remember {
//        androidx.compose.ui.text.TextStyle(
//            color = if (selected || isToday) Color.White else Color.Black
//        )
//    }
    val dayStyle =
        androidx.compose.ui.text.TextStyle(
            color =
                if (selected || isToday) Color.White else Color.Black
        )

//    val containerColor = remember {
//        if (selected) Color.Red else if (isToday) Color.Gray.copy(0.7f)  else Color.Transparent
//    }

    Box(modifier = modifier.noRippleClickable { onClick() }) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor =
                    if (selected) Color.Red else if (isToday) Color.Gray.copy(0.7f) else Color.Transparent

            )
        ) {}
        Text(
            text = day.dayOfMonth.toString(),
            modifier = Modifier.align(Alignment.Center),
            style = dayStyle
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun main() {

    val currentDate = LocalDate.now()          // ex) 2024-05-31
    val nextDate = currentDate.plusMonths(1).withDayOfMonth(1)

    val lastDay = currentDate.lengthOfMonth()        // ex) 31
    val firstDayOfWeek = currentDate.dayOfWeek.name // 5
    val days = IntRange(1, lastDay).toList()    // ex) 1, 2, 3, 4, ... , 31
    var currentMonth = YearMonth.now()         // ex) 2024-05
    println(currentDate)
    println(currentMonth)
    println(lastDay)
    println(firstDayOfWeek)
    println(days)


    println("nextDate: $nextDate")
    println(nextDate.dayOfWeek.name)




    DayOfWeek.values().forEach { dayOfWeek ->
        println(dayOfWeek.getDisplayName(java.time.format.TextStyle.NARROW, Locale.KOREAN))
    }
}