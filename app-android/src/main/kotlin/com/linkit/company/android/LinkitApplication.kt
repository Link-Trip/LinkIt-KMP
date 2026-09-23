package com.linkit.company.android

import android.app.Application
import android.content.pm.ApplicationInfo
import com.linkit.company.AndroidAppGraph
import com.linkit.company.core.common.AppGraph
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.android.MetroAppComponentProviders
import dev.zacsweers.metrox.android.MetroApplication
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

class LinkitApplication : Application(), MetroApplication {
    private val _appGraph: AppGraph by lazy {
        createGraphFactory<AndroidAppGraph.Factory>().createAndroidAppGraph(
            applicationContext = this,
        )
    }

    override val appComponentProviders: MetroAppComponentProviders
        get() = _appGraph as MetroAppComponentProviders

    override fun onCreate() {
        super.onCreate()
        // 디버그 빌드에서만 Napier 출력을 켠다. 릴리스에서는 모든 Napier 호출이 no-op이다.
        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebuggable) {
            Napier.base(DebugAntilog())
        }
    }
}
