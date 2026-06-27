package example.yf.fruit_hall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import example.yf.fruit_hall.ui.FruitHallApp
import example.yf.fruit_hall.ui.theme.FruitHallTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FruitHallTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FruitHallApp()
                }
            }
        }
    }
}