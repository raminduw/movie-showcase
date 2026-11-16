package dev.ramindu.cinenote

import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import dev.ramindu.cinenote.core.designsystem.theme.CineNoteTheme
import dev.ramindu.cinenote.navigation.CineNoteApp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 12+ SplashScreen API (back-ported by core-splashscreen): animated icon, then a zoom-out exit.
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        splash.setOnExitAnimationListener { provider ->
            val view = provider.view
            val scale = android.animation.PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.4f)
            val scaleY = android.animation.PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.4f)
            val alpha = android.animation.PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f)
            android.animation.ObjectAnimator.ofPropertyValuesHolder(view, scale, scaleY, alpha).apply {
                interpolator = AnticipateInterpolator()
                duration = 350L
                doOnEnd { provider.remove() }
                start()
            }
        }

        enableEdgeToEdge()
        setContent {
            CineNoteTheme {
                CineNoteApp()
            }
        }
    }
}
