package com.linkit.company.core.designsystem.screenshot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.linkit.company.core.designsystem.component.checkbox.LinkItCheckbox
import com.linkit.company.core.designsystem.theme.LinkItTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(manifest = Config.NONE, sdk = [35], qualifiers = "w200dp-h80dp-mdpi")
class CheckboxScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    /** unchecked / checked / disabled(unchecked) / disabled(checked) */
    @Test
    fun checkbox_states() {
        composeRule.setContent {
            LinkItTheme {
                Row(
                    modifier = Modifier
                        .background(LinkItTheme.color.semantic.background.normal.normal)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    LinkItCheckbox(checked = false, onCheckedChange = {})
                    LinkItCheckbox(checked = true, onCheckedChange = {})
                    LinkItCheckbox(checked = false, onCheckedChange = {}, enabled = false)
                    LinkItCheckbox(checked = true, onCheckedChange = {}, enabled = false)
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun checkbox_togglesOnClickAndIgnoresWhenDisabled() {
        composeRule.setContent {
            LinkItTheme {
                var enabledChecked by remember { mutableStateOf(false) }
                var disabledChecked by remember { mutableStateOf(false) }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LinkItCheckbox(
                        checked = enabledChecked,
                        onCheckedChange = { enabledChecked = it },
                        modifier = Modifier.testTag("enabled"),
                    )
                    LinkItCheckbox(
                        checked = disabledChecked,
                        onCheckedChange = { disabledChecked = it },
                        enabled = false,
                        modifier = Modifier.testTag("disabled"),
                    )
                }
            }
        }

        composeRule.onNodeWithTag("enabled").assertIsOff().performClick().assertIsOn()
        composeRule.onNodeWithTag("disabled").assertIsOff().performClick().assertIsOff()
    }
}
