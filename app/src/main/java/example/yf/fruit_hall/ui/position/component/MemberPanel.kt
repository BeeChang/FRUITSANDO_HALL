package example.yf.fruit_hall.ui.position.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import example.yf.fruit_hall.ui.position.MemberUi
import example.yf.fruit_hall.ui.position.PositionUi

@Composable
fun MemberPanel(
    modifier: Modifier = Modifier,
    members: List<MemberUi>,
    memberWeights: Map<Long, Map<Long, Float>>,
    positions: List<PositionUi>,
    onToggleWorking: (Long) -> Unit,
    onManageClick: () -> Unit
) {
    val weightPositions = positions.filter { it.hasWeight }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "멤버",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onManageClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "멤버 관리",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(members, key = { it.id }) { member ->
                MemberRow(
                    member = member,
                    onToggle = { onToggleWorking(member.id) }
                )
            }
        }

        if (weightPositions.isNotEmpty() && members.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            WeightIndicatorSection(
                members = members,
                positions = weightPositions,
                memberWeights = memberWeights
            )
        }
    }
}

@Composable
private fun MemberRow(
    member: MemberUi,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (member.isWorking)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = member.name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (member.isWorking)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = member.isWorking,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
private fun WeightIndicatorSection(
    members: List<MemberUi>,
    positions: List<PositionUi>,
    memberWeights: Map<Long, Map<Long, Float>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "가중치 현황",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            positions.forEach { position ->
                Text(
                    text = position.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    members.filter { it.isWorking }.forEach { member ->
                        val weight = memberWeights[member.id]?.get(position.id) ?: 1f
                        WeightDot(
                            memberName = member.name,
                            weight = weight
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun WeightDot(
    memberName: String,
    weight: Float
) {
    val color = when {
        weight >= 0.8f -> Color(0xFF4CAF50)
        weight >= 0.5f -> Color(0xFFFF9800)
        weight >= 0.2f -> Color(0xFFFF5722)
        else -> Color(0xFFF44336)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = memberName.take(2),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
