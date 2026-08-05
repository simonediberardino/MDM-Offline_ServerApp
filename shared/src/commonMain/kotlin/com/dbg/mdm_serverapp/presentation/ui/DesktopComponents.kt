package com.dbg.mdm_serverapp.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dbg.mdm_serverapp.presentation.ui.theme.ControlCorner
import com.dbg.mdm_serverapp.presentation.ui.theme.FluentAccent
import com.dbg.mdm_serverapp.presentation.ui.theme.FluentBadgeBg
import com.dbg.mdm_serverapp.presentation.ui.theme.FluentBadgeText
import com.dbg.mdm_serverapp.presentation.ui.theme.FluentCard
import com.dbg.mdm_serverapp.presentation.ui.theme.FluentNavHover
import com.dbg.mdm_serverapp.presentation.ui.theme.FluentOnAccent
import com.dbg.mdm_serverapp.presentation.ui.theme.FluentStroke
import com.dbg.mdm_serverapp.presentation.ui.theme.FluentText
import com.dbg.mdm_serverapp.presentation.ui.theme.FluentTextSecondary

@Composable
fun PrivacyBadge(text: String) {
    Text(
        text = text,
        color = FluentBadgeText,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clip(ControlCorner)
            .background(FluentBadgeBg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
fun DesktopPrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .defaultMinSize(minWidth = 80.dp)
            .height(32.dp),
        shape = ControlCorner,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = FluentAccent,
            contentColor = FluentOnAccent,
            disabledContainerColor = FluentStroke,
            disabledContentColor = FluentTextSecondary,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun DesktopSecondaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .defaultMinSize(minWidth = 80.dp)
            .height(32.dp)
            .hoverable(interaction),
        shape = ControlCorner,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (hovered) FluentNavHover else FluentCard,
            contentColor = FluentText,
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, FluentStroke),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun DesktopTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(32.dp),
        shape = ControlCorner,
        contentPadding = PaddingValues(horizontal = 8.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = FluentAccent),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
