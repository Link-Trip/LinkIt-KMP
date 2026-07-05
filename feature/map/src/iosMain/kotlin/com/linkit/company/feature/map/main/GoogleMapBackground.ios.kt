package com.linkit.company.feature.map.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSClassFromString
import platform.UIKit.UIColor
import platform.UIKit.UIView

@OptIn(BetaInteropApi::class)
@Composable
internal actual fun GoogleMapBackground(modifier: Modifier) {
    UIKitView(
        modifier = modifier,
        factory = {
            UIView().apply {
                backgroundColor = UIColor(
                    red = 0.88,
                    green = 0.94,
                    blue = 0.90,
                    alpha = 1.0,
                )
                accessibilityIdentifier =
                    if (NSClassFromString("GMSMapView") != null) {
                        "GoogleMapsSDKLoaded"
                    } else {
                        "GoogleMapsSDKUnavailable"
                    }
            }
        },
    )
}
