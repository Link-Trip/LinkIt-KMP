package com.linkit.company.feature.map.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.linkit.company.core.designsystem.component.button.ButtonColor
import com.linkit.company.core.designsystem.component.button.ButtonSize
import com.linkit.company.core.designsystem.component.button.ButtonVariant
import com.linkit.company.core.designsystem.component.button.LinkItButton
import com.linkit.company.core.designsystem.component.navigation.LinkItTopNavigation
import com.linkit.company.core.designsystem.component.navigation.TopNavigationDefaults
import com.linkit.company.core.designsystem.component.popup.LinkItToast
import com.linkit.company.core.designsystem.component.popup.ToastVariant
import com.linkit.company.core.designsystem.component.popup.dialog.DialogDefaults
import com.linkit.company.core.designsystem.component.popup.dialog.LinkItDialog
import com.linkit.company.core.designsystem.component.switch.LinkItSwitch
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.foundation.interaction.InteractionDefaults
import com.linkit.company.core.designsystem.theme.LinkItTheme
import com.linkit.company.domain.model.settings.MapDisplayType
import com.linkit.company.feature.map.mypage.platform.rememberNotificationPermissionController
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import linkitcompany.feature.map.generated.resources.Res
import linkitcompany.feature.map.generated.resources.mypage_map_default
import linkitcompany.feature.map.generated.resources.mypage_map_satellite
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** 화면 하단에 표시되는 토스트. [id] 가 바뀌면 새 토스트로 간주해 표시 시간을 다시 센다. */
data class MyPageToast(
    val id: Int,
    val message: String,
    val type: MyPageToastType,
)

private const val ToastDurationMillis = 3_000L

@Composable
fun MyPageScreen(
    onBack: () -> Unit = {},
    onOpenTerms: () -> Unit = {},
    onAppReset: () -> Unit = {},
    viewModel: MyPageViewModel = metroViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val notificationPermission = rememberNotificationPermissionController()

    val scope = rememberCoroutineScope()
    var toast by remember { mutableStateOf<MyPageToast?>(null) }
    var toastId by remember { mutableIntStateOf(0) }

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is MyPageSideEffect.ShowToast -> {
                    toastId += 1
                    toast = MyPageToast(toastId, effect.message, effect.type)
                }
                MyPageSideEffect.NavigateToNotificationSettings ->
                    notificationPermission.openAppNotificationSettings()
                MyPageSideEffect.AppResetCompleted -> onAppReset()
            }
        }
    }

    LaunchedEffect(toast?.id) {
        if (toast != null) {
            delay(ToastDurationMillis)
            toast = null
        }
    }

    // 진입 시(옵저버 등록 시 ON_RESUME 전달)와 기기 설정에서 복귀할 때마다 알림 상태를 다시 읽는다.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        scope.launch {
            viewModel.onIntent(
                MyPageIntent.RefreshNotificationStatus(notificationPermission.isAppNotificationEnabled()),
            )
        }
    }

    MyPageContent(
        uiState = uiState,
        toast = toast,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onOpenTerms = onOpenTerms,
    )
}

@Composable
fun MyPageContent(
    uiState: MyPageUiState,
    onIntent: (MyPageIntent) -> Unit,
    modifier: Modifier = Modifier,
    toast: MyPageToast? = null,
    onBack: () -> Unit = {},
    onOpenTerms: () -> Unit = {},
) {
    val showNotificationCard = uiState.notificationStatus == NotificationStatus.DISABLED

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.normal),
    ) {
        Column(Modifier.fillMaxSize()) {
            LinkItTopNavigation(
                title = MyPageStrings.Title,
                navigationIcon = { TopNavigationDefaults.BackButton(onClick = onBack) },
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = if (showNotificationCard) 0.dp else MainVerticalPadding,
                        bottom = MainVerticalPadding,
                    ),
                verticalArrangement = Arrangement.spacedBy(SectionSpacing),
            ) {
                if (showNotificationCard) {
                    NotificationDisabledCard(
                        onActionClick = { onIntent(MyPageIntent.OpenNotificationSettings) },
                        modifier = Modifier.padding(horizontal = ContentHorizontalPadding),
                    )
                }

                Section(MyPageStrings.SectionMap) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = ContentHorizontalPadding),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        MapDisplayTypeCard(
                            label = MyPageStrings.MapDefault,
                            image = Res.drawable.mypage_map_default,
                            selected = uiState.mapDisplayType == MapDisplayType.DEFAULT,
                            onClick = { onIntent(MyPageIntent.SelectMapDisplayType(MapDisplayType.DEFAULT)) },
                            modifier = Modifier.weight(1f).testTag("mypage-map-default"),
                        )
                        MapDisplayTypeCard(
                            label = MyPageStrings.MapSatellite,
                            image = Res.drawable.mypage_map_satellite,
                            selected = uiState.mapDisplayType == MapDisplayType.SATELLITE,
                            onClick = { onIntent(MyPageIntent.SelectMapDisplayType(MapDisplayType.SATELLITE)) },
                            modifier = Modifier.weight(1f).testTag("mypage-map-satellite"),
                        )
                    }
                }

                Section(MyPageStrings.SectionNotification) {
                    SettingRow(
                        icon = LinkItIcon.Utility.Bell,
                        text = MyPageStrings.RowNotification,
                        onClick = { onIntent(MyPageIntent.OpenNotificationSettings) },
                        modifier = Modifier
                            .padding(horizontal = ContentHorizontalPadding)
                            .testTag("mypage-row-notification"),
                    ) {
                        LinkItSwitch(
                            checked = uiState.notificationStatus == NotificationStatus.ENABLED,
                            onCheckedChange = null,
                        )
                    }
                }

                Section(MyPageStrings.SectionHelp) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = ContentHorizontalPadding),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        SettingRow(
                            icon = LinkItIcon.Communication.Message,
                            text = MyPageStrings.RowFeedback,
                            onClick = { onIntent(MyPageIntent.OpenFeedbackSheet) },
                            modifier = Modifier.testTag("mypage-row-feedback"),
                            trailing = { ChevronRight() },
                        )
                        RowDivider()
                        SettingRow(
                            icon = LinkItIcon.Utility.Document,
                            text = MyPageStrings.RowTerms,
                            onClick = onOpenTerms,
                            modifier = Modifier.testTag("mypage-row-terms"),
                            trailing = { ChevronRight() },
                        )
                    }
                }

                Section(MyPageStrings.SectionApp) {
                    SettingRow(
                        icon = LinkItIcon.Control.Refresh,
                        text = MyPageStrings.RowReset,
                        onClick = { onIntent(MyPageIntent.ShowResetDialog) },
                        modifier = Modifier
                            .padding(horizontal = ContentHorizontalPadding)
                            .testTag("mypage-row-reset"),
                    )
                }
            }
        }

        if (toast != null) {
            LinkItToast(
                text = toast.message,
                variant = when (toast.type) {
                    MyPageToastType.SUCCESS -> ToastVariant.Positive
                    MyPageToastType.ERROR -> ToastVariant.Negative
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = ContentHorizontalPadding, vertical = ToastBottomPadding)
                    .fillMaxWidth()
                    .testTag("mypage-toast"),
            )
        }

        if (uiState.isResetDialogVisible) {
            LinkItDialog(
                title = MyPageStrings.ResetDialogTitle,
                description = MyPageStrings.ResetDialogDescription,
                confirmText = MyPageStrings.ResetDialogConfirm,
                onConfirmClick = { onIntent(MyPageIntent.ConfirmReset) },
                onDismissRequest = { onIntent(MyPageIntent.DismissResetDialog) },
                secondaryText = MyPageStrings.ResetDialogCancel,
                confirmColors = DialogDefaults.negativeConfirmColors(),
                actionsEnabled = !uiState.isResetInProgress,
            )
        }

        uiState.feedbackSheet?.let { sheet ->
            FeedbackBottomSheet(
                state = sheet,
                onIntent = onIntent,
            )
        }
    }
}

/** Figma "Ai information" 카드: 기기 알림이 꺼져 있을 때만 최상단에 표시. */
@Composable
private fun NotificationDisabledCard(
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(LinkItTheme.color.atomic.Violet99)
            .border(1.dp, LinkItTheme.color.atomic.Violet95, shape)
            .padding(17.dp)
            .testTag("mypage-notification-card"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    imageVector = MyPageIllustrations.Bell,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = MyPageStrings.NotificationCardTitle,
                    style = LinkItTheme.typography.body2NormalBold,
                    color = LinkItTheme.color.semantic.label.normal,
                )
            }
            Text(
                text = MyPageStrings.NotificationCardBody,
                style = LinkItTheme.typography.label2Medium,
                color = LinkItTheme.color.semantic.label.neutral,
            )
        }
        LinkItButton(
            onClick = onActionClick,
            text = MyPageStrings.NotificationCardAction,
            variant = ButtonVariant.Outlined,
            color = ButtonColor.Primary,
            size = ButtonSize.Small,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = LinkItTheme.typography.label1NormalSemibold,
            color = LinkItTheme.color.semantic.label.neutral,
            modifier = Modifier.padding(horizontal = ContentHorizontalPadding),
        )
        content()
    }
}

@Composable
private fun MapDisplayTypeCard(
    label: String,
    image: DrawableResource,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier.clickable(
            interactionSource = null,
            indication = null,
            role = Role.RadioButton,
            onClick = onClick,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .clip(shape)
                .then(
                    if (selected) {
                        Modifier.border(2.dp, LinkItTheme.color.semantic.primary.normal, shape)
                    } else {
                        Modifier
                    },
                ),
        ) {
            Image(
                painter = painterResource(image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Text(
            text = label,
            style = if (selected) {
                LinkItTheme.typography.label1NormalBold
            } else {
                LinkItTheme.typography.label1NormalMedium
            },
            color = if (selected) {
                LinkItTheme.color.semantic.primary.normal
            } else {
                LinkItTheme.color.semantic.label.alternative
            },
        )
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = null,
                indication = InteractionDefaults.indication(),
                role = Role.Button,
                onClick = onClick,
            )
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = LinkItTheme.color.semantic.label.normal,
                modifier = Modifier.size(RowIconSize),
            )
            Text(
                text = text,
                style = LinkItTheme.typography.body2NormalMedium,
                color = LinkItTheme.color.semantic.label.normal,
            )
        }
        trailing?.invoke(this)
    }
}

@Composable
private fun ChevronRight() {
    Icon(
        imageVector = LinkItIcon.Arrow.ChevronRightTight,
        contentDescription = null,
        tint = LinkItTheme.color.semantic.label.alternative,
        modifier = Modifier.height(RowIconSize),
    )
}

@Composable
private fun RowDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(LinkItTheme.color.semantic.line.solid.neutral),
    )
}

private val ContentHorizontalPadding = 20.dp
private val MainVerticalPadding = 24.dp
private val SectionSpacing = 16.dp
private val RowIconSize = 20.dp
private val ToastBottomPadding = 20.dp
