package com.linkit.company

import com.linkit.company.core.navigation.LinkItNavKey
import com.linkit.company.feature.home.HomeViewController
import com.linkit.company.feature.intro.IntroViewController
import com.linkit.company.feature.schedule.ScheduleViewController
import platform.UIKit.UINavigationController
import platform.UIKit.UIViewController

// DataStore와 인증 저장소는 화면 전환·SwiftUI 재생성에도 하나의 그래프를 공유한다.
private val appGraph by lazy { createIosAppGraph() }

fun MainViewController(): UIViewController = IosRootViewController()

private class IosRootViewController : UINavigationController(navigationBarClass = null, toolbarClass = null) {
    override fun viewDidLoad() {
        super.viewDidLoad()
        setNavigationBarHidden(true, animated = false)
        showIntro()
    }

    private fun showIntro(showResetCompletedToast: Boolean = false) {
        setViewControllers(
            listOf(
                IntroViewController(
                    appGraph = appGraph,
                    onComplete = ::showHome,
                    showResetCompletedToast = showResetCompletedToast,
                ),
            ),
            animated = false,
        )
    }

    private fun showHome() {
        setViewControllers(
            listOf(
                HomeViewController(
                    appGraph = appGraph,
                    navigateToScheduleEdit = { showSchedule(LinkItNavKey.ScheduleEdit) },
                    navigateToSchedule = { tripPlanId, title, focusedPlaceId ->
                        showSchedule(LinkItNavKey.ScheduleTripDetail(tripPlanId, title, focusedPlaceId))
                    },
                    onAppReset = { showIntro(showResetCompletedToast = true) },
                ),
            ),
            animated = false,
        )
    }

    private fun showSchedule(startRoute: LinkItNavKey) {
        pushViewController(
            ScheduleViewController(
                appGraph = appGraph,
                onClose = { popViewControllerAnimated(true) },
                startRoute = startRoute,
            ),
            animated = true,
        )
    }
}
