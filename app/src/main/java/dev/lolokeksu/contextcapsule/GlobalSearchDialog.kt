package dev.lolokeksu.contextcapsule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import dev.lolokeksu.contextcapsule.domain.SearchCategory
import dev.lolokeksu.contextcapsule.domain.SearchHit
import kotlinx.coroutines.delay

private val SearchInk = Color(PremiumPalette.Background.toInt())
private val SearchPanel = Color(PremiumPalette.Panel.toInt())
private val SearchAccent = Color(PremiumPalette.Blue.toInt())
private val SearchText = Color(PremiumPalette.Title.toInt())
private val SearchMuted = Color(PremiumPalette.Secondary.toInt())
private val SearchStroke = Color(PremiumPalette.Outline.toInt())

/** User-controlled, local-only, result-limited search across all projects. */
@Composable
fun GlobalSearchDialog(
    results: List<SearchHit>,
    onSearch: (String, SearchCategory, Boolean) -> Unit,
    onPick: (SearchHit) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(SearchCategory.ALL) }
    var includeArchived by remember { mutableStateOf(false) }
    LaunchedEffect(query, category, includeArchived) {
        delay(180L)
        onSearch(query, category, includeArchived)
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(max = 620.dp),
            color = SearchInk,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, SearchStroke)
        ) {
            Column(Modifier.padding(17.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("ПОИСК ВО ВСЕХ ПРОЕКТАХ", color = SearchAccent,
                            fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(Modifier.height(5.dp))
                        Text("Найти запись", color = SearchText, fontSize = 23.sp,
                            fontWeight = FontWeight.SemiBold)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("global_search_close")) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть поиск", tint = SearchMuted)
                    }
                }
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { if (it.length <= 120) query = it },
                    label = { Text("Текст заметки или задачи") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier.fillMaxWidth().testTag("global_search_field")
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf(
                        SearchCategory.ALL to "Все",
                        SearchCategory.NOTES to "Заметки",
                        SearchCategory.TASKS to "Задачи"
                    ).forEach { (value, title) ->
                        FilterChip(
                            selected = category == value,
                            onClick = { category = value },
                            label = { Text(title, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF284467),
                                selectedLabelColor = SearchText
                            )
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = includeArchived,
                        onCheckedChange = { includeArchived = it },
                        modifier = Modifier.testTag("global_search_archived")
                    )
                    Text("Искать также в архиве", color = SearchMuted, fontSize = 12.sp)
                }
                HorizontalDivider(color = SearchStroke)
                Spacer(Modifier.height(9.dp))
                val meaningful = query.trim().isNotEmpty()
                if (!meaningful) {
                    Text("Начни вводить слово или фразу. Поиск выполняется только на устройстве.",
                        color = SearchMuted, fontSize = 13.sp, lineHeight = 20.sp,
                        modifier = Modifier.padding(vertical = 22.dp))
                } else if (results.isEmpty()) {
                    Text("Совпадений не найдено", color = SearchMuted, fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 22.dp))
                } else {
                    Text("Найдено: ${results.size}${if (results.size == 80) " · показаны первые 80" else ""}",
                        color = SearchMuted, fontSize = 11.sp)
                    Spacer(Modifier.height(7.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(results, key = { "${it.projectId}:${it.category}:${it.recordId}" }) { hit ->
                            Surface(
                                color = SearchPanel,
                                shape = RoundedCornerShape(15.dp),
                                border = BorderStroke(1.dp, SearchStroke),
                                modifier = Modifier.fillMaxWidth()
                                    .testTag("global_result_${hit.recordId}")
                                    .clickable { onPick(hit) }
                            ) {
                                Column(Modifier.padding(13.dp)) {
                                    Text(hit.projectTitle, color = SearchAccent,
                                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(5.dp))
                                    Text(hit.text, color = SearchText, fontSize = 14.sp,
                                        maxLines = 3, lineHeight = 19.sp,
                                        overflow = TextOverflow.Ellipsis)
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        when (hit.category) {
                                            SearchCategory.NOTES ->
                                                if (hit.confirmed) "Решение подтверждено"
                                                else if (hit.pinned) "Закреплённая · на проверку"
                                                else "Заметка · на проверку"
                                            SearchCategory.TASKS ->
                                                if (hit.completed) "Задача · выполнена"
                                                else "Задача · открыта"
                                            SearchCategory.ALL -> "Запись"
                                        },
                                        color = SearchMuted, fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
