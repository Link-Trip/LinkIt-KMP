package com.linkit.company.core.ui.terms

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun PlatformWebView(
    url: String,
    reloadToken: Int,
    onLoadingChanged: (Boolean) -> Unit,
    onError: () -> Unit,
    modifier: Modifier,
) {
    val currentOnLoadingChanged by rememberUpdatedState(onLoadingChanged)
    val currentOnError by rememberUpdatedState(onError)
    val allowedHost = Uri.parse(url).host

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        currentOnLoadingChanged(true)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        currentOnLoadingChanged(false)
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?,
                    ) {
                        if (request?.isForMainFrame == true) {
                            currentOnError()
                        }
                    }

                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val target = request?.url ?: return false
                        if (target.host == allowedHost) return false
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, target).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                        return true
                    }
                }
                tag = reloadToken
                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.tag != reloadToken) {
                webView.tag = reloadToken
                webView.loadUrl(url)
            }
        },
    )
}
