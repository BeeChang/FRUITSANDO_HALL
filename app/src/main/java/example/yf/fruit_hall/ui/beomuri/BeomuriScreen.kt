package example.yf.fruit_hall.ui.beomuri

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import example.yf.fruit_hall.ui.component.AppConfirmButton
import example.yf.fruit_hall.ui.component.util.toTrimmedDecimalString

private val ingredientColors = listOf(
    Color(0xFF43A047),  // 멜론 - 그린
    Color(0xFF2E7D32),  // 키위 - 딥그린
    Color(0xFFFFB300),  // 설탕 - 앰버
    Color(0xFF039BE5),  // 라임주스 - 스카이블루
    Color(0xFFE53935),  // 나빠쥬 - 크림슨
)

@Composable
fun BeomuriScreen(
    viewModel: BeomuriViewModel = hiltViewModel()
) {
    val ingredients = viewModel.ingredients
    val totalGrams = viewModel.totalGrams

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 340.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                BeomuriHeader(totalGrams = totalGrams)
            }

            items(items = ingredients, key = { it.id }) { ingredient ->
                IngredientCard(
                    ingredient = ingredient,
                    color = ingredientColors[ingredient.id],
                    onValueChanged = { viewModel.onIngredientChanged(ingredient.id, it) },
                    resetCount = viewModel.resetCount,
                    onResetAndFocus = if (ingredient.id == 0) viewModel::reset else null
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                ActionBar(onReset = viewModel::reset)
            }
        }
    }
}

@Composable
private fun BeomuriHeader(totalGrams: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "버무리",
            style = MaterialTheme.typography.headlineMedium,
        )
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "총 중량",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${totalGrams.toGramString()} g",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun IngredientCard(
    ingredient: BeomuriIngredient,
    color: Color,
    onValueChanged: (Double) -> Unit,
    resetCount: Int,
    onResetAndFocus: (() -> Unit)? = null,
) {
    var text by remember(ingredient.id) { mutableStateOf(ingredient.currentGrams.toGramString()) }
    var isFocused by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(ingredient.currentGrams) {
        if (!isFocused) text = ingredient.currentGrams.toGramString()
    }
    LaunchedEffect(resetCount) {
        text = ingredient.currentGrams.toGramString()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (onResetAndFocus != null) {
                    Text(
                        text = "용량입력",
                        style = MaterialTheme.typography.titleMedium,
                        color = color,
                        modifier = Modifier.clickable {
                            onResetAndFocus()
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }
                    )
                }
            }

            OutlinedTextField(
                value = text,
                onValueChange = { newVal ->
                    val processed = when {
                        text == "0" && newVal.length > 1 && newVal != "0." ->
                            newVal.replaceFirst("0", "").ifEmpty { "0" }
                        newVal.length > 1 && newVal.startsWith("0") && newVal[1] != '.' ->
                            newVal.trimStart('0').ifEmpty { "0" }
                        else -> newVal
                    }
                    text = processed
                    processed.toDoubleOrNull()?.let { d ->
                        if (d > 0.0) onValueChanged(d)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { state ->
                        isFocused = state.isFocused
                        if (!state.isFocused) text = ingredient.currentGrams.toGramString()
                    },
                suffix = {
                    Text(
                        text = "g",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    textAlign = TextAlign.Center
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = color,
                )
            )

            Text(
                text = "기준: ${ingredient.defaultGrams.toGramString()} g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionBar(onReset: () -> Unit) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            AppConfirmButton(
                text = "초기화",
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onReset()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun Double.toGramString(): String = toTrimmedDecimalString()
