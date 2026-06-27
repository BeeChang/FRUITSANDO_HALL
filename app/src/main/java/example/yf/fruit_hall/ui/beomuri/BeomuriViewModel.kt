package example.yf.fruit_hall.ui.beomuri

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BeomuriViewModel @Inject constructor() : ViewModel() {

    private val defaults = listOf(
        BeomuriIngredient(0, "멜론", 150.0, 150.0),
        BeomuriIngredient(1, "키위", 50.0, 50.0),
        BeomuriIngredient(2, "설탕", 10.0, 10.0),
        BeomuriIngredient(3, "라임주스", 6.0, 6.0),
        BeomuriIngredient(4, "나빠쥬", 40.0, 40.0),
    )

    var ingredients by mutableStateOf(defaults)
        private set

    var resetCount by mutableIntStateOf(0)
        private set

    val totalGrams: Double
        get() = ingredients.sumOf { it.currentGrams }

    fun onIngredientChanged(id: Int, grams: Double) {
        if (grams <= 0.0) return
        val base = ingredients.first { it.id == id }
        val scaleFactor = grams / base.defaultGrams
        ingredients = ingredients.map { it.copy(currentGrams = it.defaultGrams * scaleFactor) }
    }

    fun reset() {
        ingredients = ingredients.map { it.copy(currentGrams = 0.0) }
        resetCount++
    }
}
