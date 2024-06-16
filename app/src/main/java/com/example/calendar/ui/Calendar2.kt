package com.example.calendar.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toIntRect
import com.example.calendar.CalendarHeader
import com.example.calendar.HorizontalDayOfWeek
import com.example.calendar.noRippleClickable
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Calendar2() {
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
            CalendarBody2(currentDate = currentDate)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarBody2(currentDate: LocalDate) {
    val firstDayOfWeek = currentDate.withDayOfMonth(1).dayOfWeek.value // 첫 주에 시작하는 요일 ex) 5(금요일)
    val lastDay = currentDate.lengthOfMonth()        // 마지막 일자, ex) 31
    val days = IntRange(1, lastDay).toList()    // ex) 1, 2, 3, 4, ... , 31
    val today = LocalDate.now()

    var selectedDate: LocalDate? by remember {
        mutableStateOf(null)
    }

    val selectedIds = rememberSaveable { mutableStateOf(emptySet<Int>()) } // NEW
    val inSelectionMode by remember { derivedStateOf { selectedIds.value.isNotEmpty() } } // NEW

    val state = rememberLazyGridState() // NEW

    Column {
        HorizontalDayOfWeek()
        LazyVerticalGrid(
            state = state,
            columns = GridCells.Fixed(7),
            modifier = Modifier.photoGridDragHandler(state, selectedIds)
        ) {
            for (i in 1..firstDayOfWeek) { // 일요일부터 시작하니까 .. 사용, 월요일부터 시작하면 until 사용
                item { Box(modifier = Modifier.weight(1f)) }
            }
            items(key = { day -> day }, items = days) { day ->
                // 이번 달의 날짜를 day로 치환하여 CalendarDay로 넘긴다. ex) 2024-05-01
                val date = currentDate.withDayOfMonth(day)
                val selected = selectedIds.value.contains(day)

                CalendarDay2(
                    day = date,
                    isToday = (date == today),
                    selected = selected,
                    inSelectionMode = inSelectionMode,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .noRippleClickable {
                            selectedIds.value = if (selected) {
                                selectedIds.value.minus(day)
                            } else {
                                selectedIds.value.plus(day)
                            }
                        }
//                        .pointerInput(Unit) {
//                            detectTapGestures {
//                                Log.d("daeyoung", "tab: $it")
//                            }
//                        }

                ) {
                    selectedDate = if (selectedDate == null || selectedDate != date) date else null
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarDay2(
    day: LocalDate,
    isToday: Boolean,
    selected: Boolean,
    inSelectionMode: Boolean,
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
            if (isToday) {
                Color.White
            } else if(inSelectionMode) {
                if (selected) Color.White else Color.Black
            } else {
                Color.Black
            }
        )

//    val containerColor = remember {
//        if (selected) Color.Red else if (isToday) Color.Gray.copy(0.7f)  else Color.Transparent
//    }

//    Box(modifier = modifier.noRippleClickable { onClick() }) {
    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            shape = CircleShape,
            colors = CardDefaults.run {
                cardColors(
                        containerColor =
                        if (isToday) {
                            Color.Gray.copy(0.7f)
                        } else if(inSelectionMode) {
                            if (selected) Color.Red else Color.Transparent
                        } else {
                            Color.Transparent
                        }

                    )
            }
        ) {}
        Text(
            text = day.dayOfMonth.toString(),
            modifier = Modifier.align(Alignment.Center),
            style = dayStyle
        )
    }
}

fun Modifier.photoGridDragHandler(
    lazyGridState: LazyGridState,
    selectedIds: MutableState<Set<Int>>
) = pointerInput(Unit) {
    var initialKey: Int? = null
    var currentKey: Int? = null
    detectDragGesturesAfterLongPress(
        onDragStart = { offset ->
            Log.d("daeYoung", "onDragStart")
            lazyGridState.gridItemKeyAtPosition(offset)?.let { key -> // #1
                if (!selectedIds.value.contains(key)) { // #2
                    initialKey = key
                    currentKey = key
                    selectedIds.value = selectedIds.value + key // #3
                }
            }
        },
        onDragCancel = {
            Log.d("daeYoung", "onDragCancel")
            initialKey = null },
        onDragEnd = {
            Log.d("daeYoung", "onDragEnd")
            initialKey = null },
        onDrag = { change, _ ->
            Log.d("daeYoung", "onDrag")
            if (initialKey != null) {
                // Add or remove photos from selection based on drag position
                lazyGridState.gridItemKeyAtPosition(change.position)?.let { key ->
                    if (currentKey != key) {
                        selectedIds.value = selectedIds.value
                            .minus(initialKey!!..currentKey!!)
                            .minus(currentKey!!..initialKey!!)
                            .plus(initialKey!!..key)
                            .plus(key..initialKey!!)
                        currentKey = key
                    }
                }
            }
        }
    )
}

fun LazyGridState.gridItemKeyAtPosition(hitPoint: Offset): Int? =
    layoutInfo.visibleItemsInfo.find { itemInfo ->
        itemInfo.size.toIntRect().contains(hitPoint.round() - itemInfo.offset)
    }?.key as? Int