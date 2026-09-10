package example.yf.fruit_hall.ui.rotation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import example.yf.fruit_hall.R
import example.yf.fruit_hall.ui.rotation.MemberUi
import example.yf.fruit_hall.ui.theme.AppTheme

// 기존 포지션 탭 MemberDialog와 동일 팔레트 — 멤버는 두 탭이 공유하는 데이터라 시각적으로도 같아야 한다
private val memberColorPalette = listOf(
    "#FFB3C6", "#FF8FAB", "#FFAFD0", "#FFD6E5",
    "#F9C0D0", "#FFA8C0", "#F0C0D8", "#F5C6EC",
    "#D4B8F5", "#C9B2F0", "#E2CCFF", "#D0B4FF",
    "#B8AFEF", "#DEBEFF", "#ECD5E3", "#F0D0FF",
    "#B3D9FF", "#A8D0F8", "#BDD7FF", "#C8DCFF",
    "#A8E6CF", "#B5EAD7", "#C7F2D4", "#9FD8D8",
    "#B0E0E6", "#C8ECC8", "#C5E8C5", "#D4F0C8",
    "#FFD6A5", "#FFE5B4", "#FFEAA7", "#FFD0A8",
)

private const val DARK_TEXT = 0xFF2D2D2D

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RotationMemberDialog(
    members: List<MemberUi>,
    onAddMember: (name: String, colorHex: String) -> Unit,
    onDeleteMember: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors
    var newName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(memberColorPalette[0]) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(0.55f).heightIn(max = 700.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(appColors.grey900)
                        .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = appColors.white, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.rotation_member_manage_title), style = MaterialTheme.typography.titleMedium,
                        color = appColors.white, modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.rotation_close), tint = appColors.grey300, modifier = Modifier.size(18.dp))
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (members.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.rotation_member_empty), style = MaterialTheme.typography.bodyMedium, color = appColors.grey400)
                        }
                    } else {
                        LazyColumn(Modifier.heightIn(max = 220.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(members, key = { it.id }) { member ->
                                val memberBg = member.colorHex.toRotationColor(rotationDefaultChipColor)
                                Row(
                                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                        .background(memberBg.copy(alpha = 0.25f))
                                        .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(Modifier.size(30.dp).clip(CircleShape).background(memberBg), contentAlignment = Alignment.Center) {
                                        Text(member.name.take(1), style = MaterialTheme.typography.labelSmall, color = Color(DARK_TEXT), fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(member.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), color = Color(DARK_TEXT), fontWeight = FontWeight.Medium)
                                    IconButton(onClick = { onDeleteMember(member.id) }, modifier = Modifier.size(34.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.rotation_delete), tint = appColors.crimson400.copy(alpha = 0.6f), modifier = Modifier.size(17.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(16.dp))

                    Text(stringResource(R.string.rotation_member_add_new), style = MaterialTheme.typography.labelLarge, color = appColors.grey700, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newName, onValueChange = { newName = it }, label = { Text(stringResource(R.string.rotation_member_name_label)) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = selectedColor.toRotationColor(rotationDefaultChipColor), focusedLabelColor = appColors.grey600),
                        leadingIcon = { Box(Modifier.size(22.dp).clip(CircleShape).background(selectedColor.toRotationColor(rotationDefaultChipColor))) }
                    )

                    Spacer(Modifier.height(14.dp))
                    Text(stringResource(R.string.rotation_color_select), style = MaterialTheme.typography.labelSmall, color = appColors.grey500)
                    Spacer(Modifier.height(8.dp))

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        memberColorPalette.forEach { hex ->
                            val isSelected = selectedColor == hex
                            Box(
                                Modifier.size(30.dp).clip(CircleShape).background(hex.toRotationColor(rotationDefaultChipColor))
                                    .then(if (isSelected) Modifier.border(2.5.dp, Color(DARK_TEXT), CircleShape) else Modifier)
                                    .clickable { selectedColor = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = Color(DARK_TEXT), modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.rotation_close), color = appColors.grey600) }
                    Button(
                        onClick = { if (newName.isNotBlank()) { onAddMember(newName.trim(), selectedColor); newName = "" } },
                        enabled = newName.isNotBlank(), modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.rotation_add))
                    }
                }
            }
        }
    }
}
