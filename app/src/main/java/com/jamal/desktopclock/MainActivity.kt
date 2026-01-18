package com.jamal.desktopclock

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jamal.desktopclock.ui.screens.MainAppScreen
import com.jamal.desktopclock.ui.theme.DesktopClockTheme
import com.jamal.desktopclock.viewmodel.MainViewModel
import com.jamal.desktopclock.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on - disable screen timeout while app is running
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Hide system bars for immersive experience
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        val viewModelFactory = MainViewModelFactory(contentResolver)

        setContent {
            DesktopClockTheme(
                dynamicColor = false,
                darkTheme = true
            ) {
                val viewModel: MainViewModel = viewModel(factory = viewModelFactory)
                MainAppScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 1920, heightDp = 1080)
@Composable
fun MainAppPreview() {
    DesktopClockTheme(darkTheme = true, dynamicColor = false) {
        // Preview not available due to ViewModel dependency
    }
}