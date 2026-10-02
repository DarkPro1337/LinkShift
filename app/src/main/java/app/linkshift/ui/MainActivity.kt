package app.linkshift.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.linkshift.settings.Settings
import app.linkshift.ui.theme.LinkShiftTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settings = Settings(this)
        setContent {
            LinkShiftTheme {
                SettingsScreen(settings)
            }
        }
    }
}
