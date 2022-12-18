package tk.mallumo.nuliko.android

import android.os.*
import androidx.activity.*
import androidx.activity.compose.*
import tk.mallumo.compose.navigation.*
import tk.mallumo.nuliko.android.ui.theme.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NulikoTheme {
                NavigationRoot(startupNode = Node.HomeUI)
            }
        }
    }
}
