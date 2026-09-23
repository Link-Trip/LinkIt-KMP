package com.linkit.company.core.ui.terms

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.UIKit.UIApplication
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
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
    val allowedHost = remember(url) { NSURL.URLWithString(url)?.host }
    val delegate = remember(allowedHost) {
        TermsNavigationDelegate(
            allowedHost = allowedHost,
            onLoadingChanged = { currentOnLoadingChanged(it) },
            onError = { currentOnError() },
        )
    }
    val webView = remember {
        WKWebView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0), configuration = WKWebViewConfiguration()).apply {
            navigationDelegate = delegate
        }
    }

    LaunchedEffect(url, reloadToken) {
        val request = NSURL.URLWithString(url)?.let { NSURLRequest.requestWithURL(it) } ?: return@LaunchedEffect
        webView.loadRequest(request)
    }

    UIKitView(
        factory = { webView },
        modifier = modifier,
    )
}

private class TermsNavigationDelegate(
    private val allowedHost: String?,
    private val onLoadingChanged: (Boolean) -> Unit,
    private val onError: () -> Unit,
) : NSObject(), WKNavigationDelegateProtocol {

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didStartProvisionalNavigation: WKNavigation?) {
        onLoadingChanged(true)
    }

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
        onLoadingChanged(false)
    }

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didFailNavigation: WKNavigation?, withError: NSError) {
        onError()
        onLoadingChanged(false)
    }

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didFailProvisionalNavigation: WKNavigation?, withError: NSError) {
        onError()
        onLoadingChanged(false)
    }

    override fun webView(
        webView: WKWebView,
        decidePolicyForNavigationAction: WKNavigationAction,
        decisionHandler: (WKNavigationActionPolicy) -> Unit,
    ) {
        val target = decidePolicyForNavigationAction.request.URL
        val host = target?.host
        if (target == null || host == null || host == allowedHost) {
            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
            return
        }
        UIApplication.sharedApplication.openURL(target, options = emptyMap<Any?, Any>(), completionHandler = null)
        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
    }
}
