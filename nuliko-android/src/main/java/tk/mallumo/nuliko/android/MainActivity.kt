package tk.mallumo.nuliko.android

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import tk.mallumo.compose.navigation.HomeUI
import tk.mallumo.compose.navigation.NavigationRoot
import tk.mallumo.compose.navigation.Node
import tk.mallumo.nuliko.android.service.ConnectorService
import tk.mallumo.nuliko.android.ui.theme.NulikoTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContent {
            NulikoTheme {
                Crossfade(ConnectorService.isConnected) {
                    if (it) NavigationRoot(startupNode = Node.HomeUI)
                    else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }

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
