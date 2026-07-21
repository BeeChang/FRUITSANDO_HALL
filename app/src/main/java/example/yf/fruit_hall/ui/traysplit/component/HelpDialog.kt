package example.yf.fruit_hall.ui.traysplit.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import example.yf.fruit_hall.R
import example.yf.fruit_hall.ui.theme.AppTheme

private data class HelpSectionRes(val titleRes: Int, val linesRes: Int)

private val helpSectionResources = listOf(
    HelpSectionRes(R.string.tray_help_section_space, R.array.tray_help_lines_space),
    HelpSectionRes(R.string.tray_help_section_item, R.array.tray_help_lines_item),
    HelpSectionRes(R.string.tray_help_section_tray_register, R.array.tray_help_lines_tray_register),
    HelpSectionRes(R.string.tray_help_section_settings, R.array.tray_help_lines_settings),
    HelpSectionRes(R.string.tray_help_section_run, R.array.tray_help_lines_run)
)

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    val appColors = AppTheme.colors

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth(0.7f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column {
                TraySplitDialogHeader(
                    title = stringResource(R.string.tray_help_dialog_title),
                    onDismiss = onDismiss,
                    icon = Icons.AutoMirrored.Filled.HelpOutline
                )

                Column(
                    modifier = Modifier
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    helpSectionResources.forEachIndexed { index, section ->
                        Text(
                            text = stringResource(section.titleRes),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = appColors.primary600
                        )
                        Spacer(Modifier.height(6.dp))
                        stringArrayResource(section.linesRes).forEach { line ->
                            Text(
                                text = "· $line",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        if (index != helpSectionResources.lastIndex) {
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.ok))
                    }
                }
            }
        }
    }
}
