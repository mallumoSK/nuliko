package tk.mallumo.nuliko.android

import android.os.*
import android.view.*
import androidx.activity.*
import androidx.activity.compose.*
import tk.mallumo.compose.navigation.*
import tk.mallumo.nuliko.android.service.*
import tk.mallumo.nuliko.android.ui.theme.*


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContent {
            NulikoTheme {
                NavigationRoot(startupNode = Node.HomeUI)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        ConnectorService.start(this)
    }

    override fun onStop() {
        super.onStop()
        ConnectorService.stop(this)
    }
}
