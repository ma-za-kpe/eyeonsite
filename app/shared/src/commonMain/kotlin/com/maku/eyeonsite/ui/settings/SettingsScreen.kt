package com.maku.eyeonsite.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.domain.UserRole
import com.maku.eyeonsite.ui.task.TopBar
import com.maku.eyeonsite.ui.theme.AlertCoral
import com.maku.eyeonsite.ui.theme.AppVariant
import com.maku.eyeonsite.ui.theme.CautionAmber
import com.maku.eyeonsite.ui.theme.EyeOnSiteTheme
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.LocalAppVariant
import com.maku.eyeonsite.ui.theme.ThemeMode

private const val RADIO_DOT_SIZE_DP = 10

@Composable
fun SettingsScreen(
    role: UserRole,
    currentMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        TopBar(title = "Settings", onBack = onBack)

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            SettingsSection(title = "Appearance") {
                ThemeMode.entries.forEach { mode ->
                    ThemeModeCard(
                        mode = mode,
                        selected = currentMode == mode,
                        onClick = { onThemeChange(mode) },
                    )
                }
            }

            SettingsSection(title = "Account") {
                RoleRow(role)
                SignOutRow(onSignOut)
            }

            Text(
                text = "EyeOnSite 0.1.0-alpha · Manti Labs",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

@Composable
private fun ThemeModeCard(
    mode: ThemeMode,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val variant = LocalAppVariant.current
    val accentColor = if (variant == AppVariant.INSPECTOR) CautionAmber else Forest

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (selected) {
                        accentColor.copy(alpha = 0.08f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    },
                ).border(
                    width = if (selected) 1.5.dp else 1.dp,
                    color =
                        if (selected) {
                            accentColor
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        },
                    shape = RoundedCornerShape(10.dp),
                ).clickable { onClick() }
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(2.dp, if (selected) accentColor else MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(
                    Modifier
                        .size(RADIO_DOT_SIZE_DP.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = mode.label,
                style = MaterialTheme.typography.labelLarge,
                color =
                    if (selected) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
            Text(
                text = mode.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RoleRow(role: UserRole) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Role",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = if (role == UserRole.CLIENT) "Diaspora Client" else "Field Inspector",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun SignOutRow(onSignOut: () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(AlertCoral.copy(alpha = 0.08f))
                .border(1.dp, AlertCoral.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                .clickable { onSignOut() }
                .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Sign Out",
            style = MaterialTheme.typography.labelLarge,
            color = AlertCoral,
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun SettingsScreenClientDarkPreview() {
    EyeOnSiteTheme(variant = AppVariant.CLIENT, darkTheme = true) {
        SettingsScreen(
            role = UserRole.CLIENT,
            currentMode = ThemeMode.DARK,
            onThemeChange = {},
            onBack = {},
            onSignOut = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun SettingsScreenClientLightPreview() {
    EyeOnSiteTheme(variant = AppVariant.CLIENT, darkTheme = false) {
        SettingsScreen(
            role = UserRole.CLIENT,
            currentMode = ThemeMode.LIGHT,
            onThemeChange = {},
            onBack = {},
            onSignOut = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun SettingsScreenInspectorDarkPreview() {
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = true) {
        SettingsScreen(
            role = UserRole.INSPECTOR,
            currentMode = ThemeMode.SYSTEM,
            onThemeChange = {},
            onBack = {},
            onSignOut = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun SettingsScreenInspectorLightPreview() {
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = false) {
        SettingsScreen(
            role = UserRole.INSPECTOR,
            currentMode = ThemeMode.LIGHT,
            onThemeChange = {},
            onBack = {},
            onSignOut = {},
        )
    }
}
