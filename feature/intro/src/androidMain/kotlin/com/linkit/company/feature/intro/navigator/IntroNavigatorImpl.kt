package com.linkit.company.feature.intro.navigator

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.linkit.company.core.navigation.navigator.feature.IntroNavigator
import com.linkit.company.core.navigation.navigator.startActivity
import com.linkit.company.feature.intro.IntroActivity
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

/**
 * 앱 초기화 완료 후 진입용 Navigator.
 *
 * 항상 `NEW_TASK | CLEAR_TASK`로 기존 백스택을 비우고, 완료 토스트 extra를 붙인다.
 * 뒤로가기로 마이페이지·메인 지도에 복귀할 수 없어야 한다(FR-026).
 */
@ContributesBinding(AppScope::class)
@Inject
class IntroNavigatorImpl : IntroNavigator {

    override fun navigateWithLauncher(
        activity: ComponentActivity,
        intentBuilder: (Intent.() -> Intent)?,
        launcher: ActivityResultLauncher<Intent>?,
    ) = startActivity<IntroActivity>(
        activity = activity,
        intentBuilder = {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra(IntroActivity.EXTRA_SHOW_RESET_TOAST, true)
            intentBuilder?.invoke(this) ?: this
        },
        launcher = launcher,
    )
}
