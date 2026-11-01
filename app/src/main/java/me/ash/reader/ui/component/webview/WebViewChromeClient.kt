package me.ash.reader.ui.component.webview

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class WebViewChromeClient(
    context: Context,
    private val onFullscreenChanged: ((Boolean) -> Unit)? = null,
) : WebChromeClient() {

    private val activity: Activity? = context.findActivity()
    private var customView: View? = null
    private var customViewCallback: CustomViewCallback? = null
    private var originalOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    override fun onShowCustomView(view: View, callback: CustomViewCallback) {
        if (customView != null || activity == null) {
            callback.onCustomViewHidden()
            return
        }
        customView = view
        customViewCallback = callback
        originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                onHideCustomView()
                true
            } else {
                false
            }
        }
        WindowCompat.getInsetsController(activity.window, view).apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        activity
            .findViewById<ViewGroup>(android.R.id.content)
            .addView(
                view,
                ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                ),
            )
        onFullscreenChanged?.invoke(true)
    }

    override fun onHideCustomView() {
        val view = customView ?: return
        customView = null
        activity?.requestedOrientation = originalOrientation
        (view.parent as? ViewGroup)?.removeView(view)
        activity?.let { WindowCompat.getInsetsController(it.window, it.window.decorView) }
            ?.show(WindowInsetsCompat.Type.systemBars())
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null
        onFullscreenChanged?.invoke(false)
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
