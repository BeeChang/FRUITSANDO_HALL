package example.yf.fruit_hall.ui.traysplit.component

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import example.yf.fruit_hall.R
import example.yf.fruit_hall.core.Level
import example.yf.fruit_hall.core.RoundCapacity
import example.yf.fruit_hall.ui.component.AppOnlyConfirmDialog
import example.yf.fruit_hall.ui.component.util.ClickShrinkEffect
import example.yf.fruit_hall.ui.component.util.toTrimmedDecimalString
import example.yf.fruit_hall.ui.theme.AppTheme
import example.yf.fruit_hall.ui.traysplit.AllocationSettingsUi
import example.yf.fruit_hall.ui.traysplit.SpaceUi

private data class CapacityRowState(val isFixed: Boolean, val fixedText: String, val flexText: String)

private fun RoundCapacity.toRowState(): CapacityRowState = when (this) {
    is RoundCapacity.Fixed -> CapacityRowState(true, trays.toString(), "1.0")
    is RoundCapacity.Flexible -> CapacityRowState(false, "", weight.toTrimmedDecimalString())
}

@Composable
private fun InfoLabel(label: String, description: String) {
    val appColors = AppTheme.colors
    var showInfo by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = appColors.grey600)
        Spacer(Modifier.width(4.dp))
        ClickShrinkEffect(onClick = { showInfo = true }, shrinkFactor = 0.8f) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = stringResource(R.string.tray_settings_info_cd),
                tint = appColors.grey400,
                modifier = Modifier.size(14.dp)
            )
        }
    }
    AppOnlyConfirmDialog(
        title = label,
        content = description,
        confirmButtonText = stringResource(R.string.ok),
        isShowDialog = showInfo,
        onConfirm = { showInfo = false },
        onDismiss = { showInfo = false }
    )
}

@Composable
private fun LevelPicker(label: String, description: String, selected: Level, onSelect: (Level) -> Unit) {
    val appColors = AppTheme.colors
    Column {
        InfoLabel(label, description)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                Level.LOW to stringResource(R.string.tray_level_low),
                Level.MID to stringResource(R.string.tray_level_mid),
                Level.HIGH to stringResource(R.string.tray_level_high)
            ).forEach { (level, text) ->
                val isSelected = selected == level
                ClickShrinkEffect(onClick = { onSelect(level) }, shrinkFactor = 0.93f) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) appColors.primary500 else MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) appColors.white else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AllocationSettingsDialog(
    settings: AllocationSettingsUi,
    spaces: List<SpaceUi>,
    onConfirm: (AllocationSettingsUi) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors
    var rounds by remember { mutableStateOf(settings.rounds) }
    var capacityRows by remember {
        mutableStateOf(settings.capacity.map { it.toRowState() })
    }
    var flexDeviation by remember { mutableStateOf(settings.flexDeviation) }
    var allowedMissingTypes by remember { mutableStateOf(settings.allowedMissingTypes) }
    var topNText by remember { mutableStateOf(settings.topN.toString()) }
    var showAdvanced by remember { mutableStateOf(false) }
    var spreadStrength by remember { mutableStateOf(settings.spreadStrength) }
    var orderStrictness by remember { mutableStateOf(settings.orderStrictness) }
    var moveAversion by remember { mutableStateOf(settings.moveAversion) }
    var ilsIterationsText by remember { mutableStateOf(settings.ilsIterations.toString()) }

    val anyFlexible = capacityRows.any { !it.isFixed }
    val capacityValid = capacityRows.all { row ->
        if (row.isFixed) row.fixedText.toIntOrNull()?.let { it >= 0 } == true
        else row.flexText.toDoubleOrNull()?.let { it >= 0 } == true
    }
    val primaryLocationSpace = spaces.find { it.id == settings.primaryLocationSpaceId }
    val primaryLocationName = primaryLocationSpace?.name ?: stringResource(R.string.tray_settings_primary_location_unset)

    fun resizeToRounds(newRounds: Int) {
        rounds = newRounds.coerceIn(2, 8)
        capacityRows = List(rounds) { i -> capacityRows.getOrNull(i) ?: CapacityRowState(false, "", "1.0") }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(0.5f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column {
                TraySplitDialogHeader(
                    title = stringResource(R.string.tray_allocation_settings),
                    onDismiss = onDismiss,
                    icon = Icons.Default.Tune
                )

                Column(
                    modifier = Modifier
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Text(stringResource(R.string.tray_settings_rounds_label), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = appColors.grey700)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { resizeToRounds(rounds - 1) }) { Text("–", style = MaterialTheme.typography.titleLarge) }
                        Text(stringResource(R.string.tray_round_ordinal, rounds), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { resizeToRounds(rounds + 1) }) { Text("+", style = MaterialTheme.typography.titleLarge) }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.tray_settings_capacity_label), style = MaterialTheme.typography.labelMedium, color = appColors.grey600)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.tray_settings_capacity_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = appColors.grey500
                    )
                    Spacer(Modifier.height(8.dp))
                    val capacityFixedLabel = stringResource(R.string.tray_settings_capacity_fixed)
                    val capacityFlexLabel = stringResource(R.string.tray_settings_capacity_flex)
                    capacityRows.forEachIndexed { i, row ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(stringResource(R.string.tray_round_ordinal, i + 1), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
                            Spacer(Modifier.width(8.dp))
                            listOf(true to capacityFixedLabel, false to capacityFlexLabel).forEach { (isFixed, label) ->
                                val isSelected = row.isFixed == isFixed
                                ClickShrinkEffect(
                                    onClick = {
                                        capacityRows = capacityRows.toMutableList().also {
                                            it[i] = row.copy(isFixed = isFixed)
                                        }
                                    },
                                    shrinkFactor = 0.9f
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) appColors.primary500 else MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                                            color = if (isSelected) appColors.white else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Spacer(Modifier.width(6.dp))
                            }
                            Spacer(Modifier.width(4.dp))
                            if (row.isFixed) {
                                OutlinedTextField(
                                    value = row.fixedText,
                                    onValueChange = { new ->
                                        capacityRows = capacityRows.toMutableList().also {
                                            it[i] = row.copy(fixedText = new.filter(Char::isDigit))
                                        }
                                    },
                                    placeholder = { Text(stringResource(R.string.tray_settings_capacity_fixed_placeholder)) },
                                    modifier = Modifier.width(90.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            } else {
                                OutlinedTextField(
                                    value = row.flexText,
                                    onValueChange = { new ->
                                        val filtered = new.filter { c -> c.isDigit() || c == '.' }.take(5)
                                        capacityRows = capacityRows.toMutableList().also {
                                            it[i] = row.copy(flexText = filtered)
                                        }
                                    },
                                    placeholder = { Text(stringResource(R.string.tray_settings_capacity_flex_placeholder)) },
                                    modifier = Modifier.width(90.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                            }
                        }
                    }

                    if (anyFlexible) {
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.tray_settings_flex_deviation_label), style = MaterialTheme.typography.labelMedium, color = appColors.grey600, modifier = Modifier.weight(1f))
                            IconButton(onClick = { flexDeviation = (flexDeviation - 1).coerceAtLeast(0) }) { Text("–") }
                            Text("$flexDeviation")
                            IconButton(onClick = { flexDeviation++ }) { Text("+") }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.tray_settings_missing_types_label), style = MaterialTheme.typography.labelMedium, color = appColors.grey600, modifier = Modifier.weight(1f))
                        IconButton(onClick = { allowedMissingTypes = (allowedMissingTypes - 1).coerceAtLeast(0) }) { Text("–") }
                        Text("$allowedMissingTypes")
                        IconButton(onClick = { allowedMissingTypes++ }) { Text("+") }
                    }

                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.tray_settings_top_n_label), style = MaterialTheme.typography.labelMedium, color = appColors.grey600, modifier = Modifier.weight(1f))
                        OutlinedTextField(
                            value = topNText,
                            onValueChange = { topNText = it.filter(Char::isDigit).take(2) },
                            modifier = Modifier.width(70.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = appColors.warning500, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.tray_settings_primary_location_prefix, primaryLocationName) +
                                (primaryLocationSpace?.capacity?.let {
                                    stringResource(R.string.tray_settings_primary_location_capacity_suffix, it)
                                } ?: ""),
                            style = MaterialTheme.typography.labelMedium,
                            color = appColors.grey600
                        )
                    }
                    Text(
                        text = stringResource(R.string.tray_settings_primary_location_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = appColors.grey500
                    )

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(10.dp))

                    ClickShrinkEffect(onClick = { showAdvanced = !showAdvanced }, shrinkFactor = 0.97f) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.tray_settings_advanced_label), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = appColors.grey700, modifier = Modifier.weight(1f))
                            Icon(if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = appColors.grey500)
                        }
                    }

                    if (showAdvanced) {
                        Spacer(Modifier.height(12.dp))
                        LevelPicker(
                            stringResource(R.string.tray_settings_spread_label),
                            stringResource(R.string.tray_settings_spread_desc),
                            spreadStrength
                        ) { spreadStrength = it }
                        Spacer(Modifier.height(12.dp))
                        LevelPicker(
                            stringResource(R.string.tray_settings_order_label),
                            stringResource(R.string.tray_settings_order_desc),
                            orderStrictness
                        ) { orderStrictness = it }
                        Spacer(Modifier.height(12.dp))
                        LevelPicker(
                            stringResource(R.string.tray_settings_move_label),
                            stringResource(R.string.tray_settings_move_desc),
                            moveAversion
                        ) { moveAversion = it }
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            InfoLabel(
                                stringResource(R.string.tray_settings_ils_label),
                                stringResource(R.string.tray_settings_ils_desc)
                            )
                            Spacer(Modifier.weight(1f))
                            OutlinedTextField(
                                value = ilsIterationsText,
                                onValueChange = { ilsIterationsText = it.filter(Char::isDigit).take(3) },
                                modifier = Modifier.width(70.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(20.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.cancel), color = appColors.grey600) }
                    Button(
                        enabled = capacityValid,
                        onClick = {
                            val capacity = capacityRows.map { row ->
                                if (row.isFixed) RoundCapacity.Fixed(row.fixedText.toIntOrNull() ?: 0)
                                else RoundCapacity.Flexible(row.flexText.toDoubleOrNull() ?: 1.0)
                            }
                            onConfirm(
                                AllocationSettingsUi(
                                    rounds = rounds,
                                    capacity = capacity,
                                    flexDeviation = flexDeviation,
                                    allowedMissingTypes = allowedMissingTypes,
                                    primaryLocationSpaceId = settings.primaryLocationSpaceId,
                                    topN = topNText.toIntOrNull()?.coerceAtLeast(1) ?: 5,
                                    ilsIterations = ilsIterationsText.toIntOrNull()?.coerceAtLeast(1) ?: 6,
                                    spreadStrength = spreadStrength,
                                    orderStrictness = orderStrictness,
                                    moveAversion = moveAversion
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}
