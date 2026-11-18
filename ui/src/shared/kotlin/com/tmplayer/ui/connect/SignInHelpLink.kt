package com.tmplayer.ui.connect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.tmplayer.ui.i18n.LocalStrings
import com.tmplayer.ui.onboarding.QuietButton
import com.tmplayer.ui.theme.Tone
import com.tmplayer.ui.theme.glassSurface

/**
 * The sign-in footer makes the two useful routes explicit on a phone: proxy settings are a recovery
 * path when Telegram cannot be reached, while sending logs stays one tap away for a stuck sign-in.
 * The 48 dp buttons sit in a lightly glassy panel and match the direct choices on TV.
 */
@Composable
fun SignInHelpLink(onProxy: () -> Unit, onSendLogs: () -> Unit, sendLogsLabel: String, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    Row(
        modifier
            .glassSurface(Tone.accent, RoundedCornerShape(28.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QuietButton(s.loginProxySettings, onClick = onProxy)
        QuietButton(sendLogsLabel, onClick = onSendLogs)
    }
}
