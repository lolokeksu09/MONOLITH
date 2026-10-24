package dev.lolokeksu.contextcapsule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lolokeksu.contextcapsule.domain.GithubSnapshot
import dev.lolokeksu.contextcapsule.domain.RepositoryAssessment
import dev.lolokeksu.contextcapsule.domain.ProjectPulse
import dev.lolokeksu.contextcapsule.domain.ProjectTask
import dev.lolokeksu.contextcapsule.github.RepositoryOverviewCodec
import dev.lolokeksu.contextcapsule.github.OverviewEntry
import java.text.DateFormat

private val White = Color(0xFFF5F8FF)
private val Dim = Color(0xFFA9BAD0)
private val Muted = Color(0xFF8493AB)
private val Blue = Color(0xFF73ACFF)
private val Edge = Color(0xFF30415B)
private val Teal = Color(0xFF79D7C1)
private val Amber = Color(0xFFFFCE83)

@Composable
fun RepositoryOverviewPanel(
    snapshot: GithubSnapshot?, hasRepository: Boolean, busy: Boolean,
    onSync: () -> Unit, onAddTask: (String) -> Unit = {},
    existingTasks: List<ProjectTask> = emptyList()
) {
    val digest = snapshot?.let { RepositoryOverviewCodec.decode(it.summary) }
    val signal = RepositoryAssessment.assess(digest?.ciStatus, digest?.prCount, digest?.issueCount, digest?.commits?.size)
    val uri = LocalUriHandler.current
    val pulse = snapshot?.let {
        ProjectPulse.compare(digest?.previousSha, digest?.previousBranch, it.sha, it.branch)
    }
    val recommendations = ProjectPulse.suggestions(digest?.ciStatus, digest?.prCount, digest?.issueCount)
        .filterNot { suggestion -> existingTasks.any { it.title == suggestion } }

    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("ОБЗОР РЕПОЗИТОРИЯ", color = Blue, fontSize = 10.sp,
                    letterSpacing = 1.9.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Состояние проекта", color = White, fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold)
            }
            if (hasRepository) OutlinedButton(
                onClick = onSync,
                enabled = !busy,
                shape = RoundedCornerShape(13.dp),
                border = BorderStroke(1.dp, Edge),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Autorenew, contentDescription = null, tint = Blue, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(5.dp))
                Text("Обновить", fontSize = 12.sp, color = White)
            }
        }
        Spacer(Modifier.height(13.dp))
        Surface(
            color = Color(0xFF111D2D),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, Color(0xFF2D4768))
        ) {
            Column(Modifier.fillMaxWidth().padding(19.dp)) {
                if (!hasRepository) {
                    Text("Нет подключённого GitHub", color = White, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    Text("Решения и задачи можно хранить без репозитория.", color = Dim, fontSize = 13.sp)
                } else if (snapshot == null) {
                    Text("Данных ещё нет", color = White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text("Нажми «Обновить» — получишь сводку изменений, CI, PR и Issues без платного ИИ.",
                        color = Dim, fontSize = 13.sp, lineHeight = 20.sp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val accent = when(signal.code) {
                            "needs_attention" -> Amber
                            "ci_passed" -> Teal
                            else -> Blue
                        }
                        Box(Modifier.size(9.dp).background(accent, RoundedCornerShape(50)))
                        Spacer(Modifier.width(11.dp))
                        Column(Modifier.weight(1f)) {
                            Text(signal.headline, color = White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Сохранённый снимок · ${DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(snapshot.checkedAt)}",
                                color = Dim, fontSize = 11.sp)
                        }
                    }
                    if (pulse != null) {
                        Spacer(Modifier.height(15.dp))
                        Column(
                            Modifier.fillMaxWidth().background(
                                if (pulse.hasNewCommits) Color(0xFF1B3D51) else Color(0xFF172A41),
                                RoundedCornerShape(15.dp)
                            ).padding(13.dp)
                        ) {
                            Text("ПОСЛЕ ПРЕДЫДУЩЕЙ СИНХРОНИЗАЦИИ", color = Blue,
                                fontSize = 10.sp, letterSpacing = 1.0.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(5.dp))
                            Text(pulse.headline, color = White, fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold)
                            if (pulse.hasNewCommits) {
                                Spacer(Modifier.height(5.dp))
                                Text("${pulse.oldSha?.take(8)} → ${pulse.newSha.take(8)}",
                                    color = Dim, fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InsightStat(signal.openPRs?.let { if (it >= 100) "100+" else "$it" } ?: "—", "PR", Modifier.weight(1f))
                        InsightStat(signal.openIssues?.let { if (it >= 100) "100+" else "$it" } ?: "—", "Issues", Modifier.weight(1f))
                        InsightStat(signal.recentCommits?.toString() ?: "—", "Коммиты¹", Modifier.weight(1f))
                    }
                    if (digest != null) {
                        (digest.description ?: digest.readme)?.let { text ->
                            Spacer(Modifier.height(15.dp))
                            Text(text, color = Dim, fontSize = 13.sp, lineHeight = 19.sp, maxLines = 4,
                                overflow = TextOverflow.Ellipsis)
                        }
                        digest.lastCommit?.let { latest ->
                            Spacer(Modifier.height(16.dp))
                            Text("ПОСЛЕДНЕЕ ИЗМЕНЕНИЕ", fontSize = 10.sp, letterSpacing = 1.2.sp,
                                color = Muted, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text(latest, color = White, fontSize = 14.sp, lineHeight = 20.sp, maxLines = 3,
                                overflow = TextOverflow.Ellipsis)
                        }
                    } else {
                        Spacer(Modifier.height(12.dp))
                        Text("Ранее сохранённый формат. Обнови GitHub для подробного обзора.",
                            color = Dim, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(11.dp))
                    Text("Ветка: ${snapshot.branch}  ·  HEAD ${snapshot.sha.take(10)}",
                        color = Blue, fontSize = 11.sp)
                    if (snapshot.warning != null) {
                        Spacer(Modifier.height(11.dp))
                        Text("Неполные данные: ${snapshot.warning}", color = Amber, fontSize = 11.sp, lineHeight = 17.sp)
                    }
                    Spacer(Modifier.height(7.dp))
                    Text("¹ Только последние загруженные записи. CI относится к последнему запуску выбранной ветки.",
                        color = Muted, fontSize = 10.sp, lineHeight = 14.sp)
                }
            }
        }
        if (digest != null && recommendations.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier.fillMaxWidth().background(Color(0xFF101722), RoundedCornerShape(17.dp))
                    .padding(16.dp)
            ) {
                Text("ЧТО ПРОВЕРИТЬ ДАЛЬШЕ", color = Blue, fontSize = 10.sp,
                    letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("Предложения по сохранённому снимку. Ничего не добавляется автоматически.",
                    color = Dim, fontSize = 12.sp, lineHeight = 17.sp)
                recommendations.forEach { recommendation ->
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(recommendation, color = White, fontSize = 12.sp,
                            lineHeight = 17.sp, modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { onAddTask(recommendation) },
                            border = BorderStroke(1.dp, Edge),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 3.dp)
                        ) { Text("В задачи", color = Blue, fontSize = 11.sp) }
                    }
                }
            }
        }
        if (digest != null) {
            Spacer(Modifier.height(19.dp))
            val blocks = listOf(
                Triple("Последние коммиты", digest.commits, Icons.Default.Code),
                Triple("Открытые PR", digest.pullRequests, Icons.Default.Source),
                Triple("Открытые Issues", digest.issues, Icons.Default.FactCheck)
            )
            blocks.forEach { (heading, items, icon) ->
                if (items.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 9.dp)) {
                        Icon(icon, contentDescription = null, tint = Blue, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(heading, color = White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Column(
                        Modifier.fillMaxWidth().background(Color(0xFF101722), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        items.take(3).forEachIndexed { index, item ->
                            TextButton(
                                onClick = { if (item.url.startsWith("https://github.com/")) uri.openUri(item.url) },
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text(item.title, color = Dim, fontSize = 12.sp, lineHeight = 17.sp,
                                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f))
                                Spacer(Modifier.width(5.dp))
                                Icon(Icons.Default.ArrowOutward, contentDescription = "Открыть в GitHub",
                                    tint = Blue, modifier = Modifier.size(15.dp))
                            }
                            if (index != items.lastIndex) HorizontalDivider(color = Edge.copy(alpha = 0.5f))
                        }
                    }
                    Spacer(Modifier.height(15.dp))
                }
            }
        }
    }
}

@Composable
private fun InsightStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.background(Color(0xFF1C304C), RoundedCornerShape(13.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(value, color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Dim, fontSize = 11.sp)
    }
}
