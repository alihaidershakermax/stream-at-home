package com.tmplayer.ui.connect

import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.ViewRootForTest
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getAllSemanticsNodes
import androidx.compose.ui.semantics.getOrNull
import com.tmplayer.data.FormFactor
import com.tmplayer.i18n.L
import com.tmplayer.ui.theme.TMPlayerTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The sign-in footer's proxy path is a visible, direct action, not hidden behind a help menu. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SignInHelpLinkTest {
    @After
    fun tearDown() {
        FormFactor.override(false)
    }

    @Test
    @Config(sdk = [35], qualifiers = "w390dp-h844dp-port-night-xhdpi")
    fun `proxy and send logs are directly selectable`() {
        var proxyOpens = 0
        var logSends = 0
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        activity.setContent {
            TMPlayerTheme {
                SignInHelpLink(
                    onProxy = { proxyOpens++ },
                    onSendLogs = { logSends++ },
                    sendLogsLabel = L.logsSend,
                )
            }
        }
        idle()

        val proxy = action(activity, L.loginProxySettings)
        val logs = action(activity, L.logsSend)
        assertNotNull("proxy settings are visible", proxy)
        assertNotNull("send logs is visible", logs)
        proxy?.config?.getOrNull(SemanticsActions.OnClick)?.action?.invoke()
        logs?.config?.getOrNull(SemanticsActions.OnClick)?.action?.invoke()
        idle()

        assertEquals(1, proxyOpens)
        assertEquals(1, logSends)
    }

    private fun idle() = repeat(10) { shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofMillis(100)) }

    private fun action(activity: ComponentActivity, label: String): SemanticsNode? {
        val nodes = mutableListOf<SemanticsNode>()
        fun walk(view: View) {
            if (view is ViewRootForTest) nodes += view.semanticsOwner.getAllSemanticsNodes(mergingEnabled = true)
            if (view is ViewGroup) for (index in 0 until view.childCount) walk(view.getChildAt(index))
        }
        walk(activity.window.decorView)
        return nodes.firstOrNull { node ->
            node.config.getOrNull(SemanticsProperties.Text).orEmpty().any { it.text == label } &&
                node.config.getOrNull(SemanticsActions.OnClick) != null
        }
    }
}
