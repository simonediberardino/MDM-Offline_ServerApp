package com.dbg.mdm_serverapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dbg.mdm_serverapp.i18n.Strings
import com.dbg.mdm_serverapp.model.Device
import com.dbg.mdm_serverapp.ui.theme.CardCorner
import com.dbg.mdm_serverapp.ui.theme.ControlCorner
import com.dbg.mdm_serverapp.ui.theme.FluentAccent
import com.dbg.mdm_serverapp.ui.theme.FluentCard
import com.dbg.mdm_serverapp.ui.theme.FluentInfoBarBg
import com.dbg.mdm_serverapp.ui.theme.FluentInfoBarStroke
import com.dbg.mdm_serverapp.ui.theme.FluentLayerDefault
import com.dbg.mdm_serverapp.ui.theme.FluentNavHover
import com.dbg.mdm_serverapp.ui.theme.FluentNavSelected
import com.dbg.mdm_serverapp.ui.theme.FluentSmoke
import com.dbg.mdm_serverapp.ui.theme.FluentStroke
import com.dbg.mdm_serverapp.ui.theme.FluentSuccess
import com.dbg.mdm_serverapp.ui.theme.FluentText
import com.dbg.mdm_serverapp.ui.theme.FluentTextSecondary
import com.dbg.mdm_serverapp.util.formatEpoch

private enum class HomeSection {
    Devices,
    Help,
}

@Composable
fun DashboardScreen(
    strings: Strings,
    running: Boolean,
    lanAddress: String,
    devices: List<Device>,
    onlineDeviceCount: Int,
    onRefresh: () -> Unit,
    onShowTutorial: () -> Unit,
    onDeviceClick: (Device) -> Unit,
) {
    var section by remember { mutableStateOf(HomeSection.Devices) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FluentLayerDefault),
    ) {
        CommandBar(
            strings = strings,
            onRefresh = onRefresh,
            onShowTutorial = onShowTutorial,
        )
        HorizontalDivider(color = FluentStroke, thickness = 1.dp)

        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            NavigationPane(
                strings = strings,
                section = section,
                deviceCount = onlineDeviceCount,
                onSelect = { section = it },
            )
            VerticalDivider(color = FluentStroke, thickness = 1.dp)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(FluentCard)
                    .padding(24.dp),
            ) {
                InfoBar(
                    strings = strings,
                    running = running,
                    lanAddress = lanAddress,
                    onlineCount = onlineDeviceCount,
                )
                Spacer(Modifier.height(20.dp))

                when (section) {
                    HomeSection.Devices -> DevicesSection(
                        strings = strings,
                        devices = devices,
                        onDeviceClick = onDeviceClick,
                    )
                    HomeSection.Help -> HelpSection(strings, onShowTutorial)
                }
            }
        }
    }
}

@Composable
private fun CommandBar(
    strings: Strings,
    onRefresh: () -> Unit,
    onShowTutorial: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FluentCard)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = strings.appTitle,
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.width(12.dp))
                PrivacyBadge(strings.privacyBadge)
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = strings.appSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = FluentTextSecondary,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DesktopSecondaryButton(text = strings.showTutorialAgain, onClick = onShowTutorial)
            DesktopSecondaryButton(text = strings.refresh, onClick = onRefresh)
        }
    }
}

@Composable
private fun NavigationPane(
    strings: Strings,
    section: HomeSection,
    deviceCount: Int,
    onSelect: (HomeSection) -> Unit,
) {
    Column(
        modifier = Modifier
            .width(220.dp)
            .fillMaxHeight()
            .background(FluentSmoke)
            .padding(vertical = 8.dp, horizontal = 8.dp),
    ) {
        Text(
            text = strings.home.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = FluentTextSecondary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
        NavItem(
            label = strings.devices,
            badge = if (deviceCount > 0) deviceCount.toString() else null,
            selected = section == HomeSection.Devices,
            onClick = { onSelect(HomeSection.Devices) },
        )
        Spacer(Modifier.weight(1f))
        NavItem(
            label = strings.help,
            selected = section == HomeSection.Help,
            onClick = { onSelect(HomeSection.Help) },
        )
    }
}

@Composable
private fun NavItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    badge: String? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val background = when {
        selected -> FluentNavSelected
        hovered -> FluentNavHover
        else -> FluentSmoke
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(ControlCorner)
            .background(background)
            .hoverable(interaction)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(16.dp)
                .background(if (selected) FluentAccent else FluentSmoke),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) FluentAccent else FluentText,
            modifier = Modifier.weight(1f),
        )
        if (badge != null) {
            Text(
                text = badge,
                style = MaterialTheme.typography.labelMedium,
                color = FluentAccent,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun InfoBar(
    strings: Strings,
    running: Boolean,
    lanAddress: String,
    onlineCount: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ControlCorner)
            .background(FluentInfoBarBg)
            .border(1.dp, FluentInfoBarStroke, ControlCorner)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (running) FluentSuccess else FluentTextSecondary),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (running) strings.statusReady else strings.statusStopped,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = "${strings.computerAddress}: $lanAddress",
            style = MaterialTheme.typography.bodyMedium,
            color = FluentTextSecondary,
        )
        Text(
            text = "${strings.connectedCount}: $onlineCount",
            style = MaterialTheme.typography.bodyMedium,
            color = FluentTextSecondary,
        )
    }
}

@Composable
private fun DevicesSection(
    strings: Strings,
    devices: List<Device>,
    onDeviceClick: (Device) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(strings.homeHint, style = MaterialTheme.typography.bodyLarge, color = FluentTextSecondary)
        Spacer(Modifier.height(16.dp))

        if (devices.isEmpty()) {
            NoDevicesGuide(strings)
            return
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(CardCorner)
                .border(1.dp, FluentStroke, CardCorner),
        ) {
            DeviceTableHeader(strings)
            HorizontalDivider(color = FluentStroke)
            LazyColumn {
                items(devices, key = { it.id }) { device ->
                    DeviceTableRow(
                        device = device,
                        onClick = { onDeviceClick(device) },
                    )
                    HorizontalDivider(color = FluentStroke)
                }
            }
        }
    }
}

@Composable
private fun NoDevicesGuide(strings: Strings) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardCorner)
            .border(1.dp, FluentStroke, CardCorner)
            .background(FluentSmoke)
            .padding(24.dp),
    ) {
        Text(text = strings.noDevicesTitle, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            text = strings.noDevicesBody,
            style = MaterialTheme.typography.bodyLarge,
            color = FluentTextSecondary,
        )
        Spacer(Modifier.height(20.dp))
        Text(strings.noDevicesStep1, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        Text(strings.noDevicesStep2, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        Text(strings.noDevicesStep3, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun DeviceTableHeader(strings: Strings) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FluentSmoke)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        HeaderCell(strings.deviceName, Modifier.weight(1.4f))
        HeaderCell(strings.deviceType, Modifier.weight(1f))
        HeaderCell(strings.registered, Modifier.weight(1.2f))
    }
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = FluentTextSecondary,
    )
}

@Composable
private fun DeviceTableRow(
    device: Device,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (hovered) FluentNavHover else FluentCard)
            .hoverable(interaction)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(device.name, modifier = Modifier.weight(1.4f), fontWeight = FontWeight.Medium)
        Text(device.platform, modifier = Modifier.weight(1f), color = FluentTextSecondary)
        Text(formatEpoch(device.registeredAt), modifier = Modifier.weight(1.2f), color = FluentTextSecondary)
    }
}

@Composable
private fun HelpSection(strings: Strings, onShowTutorial: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardCorner)
            .border(1.dp, FluentStroke, CardCorner)
            .padding(24.dp),
    ) {
        PrivacyBadge(strings.privacyBadge)
        Spacer(Modifier.height(12.dp))
        Text(strings.helpTitle, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(10.dp))
        Text(strings.helpBody, style = MaterialTheme.typography.bodyLarge, color = FluentTextSecondary)
        Spacer(Modifier.height(20.dp))
        DesktopPrimaryButton(text = strings.showTutorialAgain, onClick = onShowTutorial)
    }
}
