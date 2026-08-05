package com.dbg.mdm_serverapp.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dbg.mdm_serverapp.presentation.i18n.Strings
import com.dbg.mdm_serverapp.domain.model.DeviceDetail
import com.dbg.mdm_serverapp.presentation.ui.theme.CardCorner
import com.dbg.mdm_serverapp.presentation.ui.theme.FluentAccent
import com.dbg.mdm_serverapp.presentation.ui.theme.FluentCard
import com.dbg.mdm_serverapp.presentation.ui.theme.FluentLayerDefault
import com.dbg.mdm_serverapp.presentation.ui.theme.FluentSmoke
import com.dbg.mdm_serverapp.presentation.ui.theme.FluentStroke
import com.dbg.mdm_serverapp.presentation.ui.theme.FluentSuccess
import com.dbg.mdm_serverapp.presentation.ui.theme.FluentTextSecondary
import com.dbg.mdm_serverapp.util.formatEpoch

@Composable
fun DeviceDetailScreen(
    strings: Strings,
    detail: DeviceDetail?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FluentLayerDefault),
    ) {
        DetailCommandBar(
            strings = strings,
            title = detail?.device?.name ?: strings.deviceDetails,
            onBack = onBack,
            onRefresh = onRefresh,
        )
        HorizontalDivider(color = FluentStroke, thickness = 1.dp)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(FluentCard),
            contentAlignment = Alignment.Center,
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = FluentAccent,
                        strokeWidth = 3.dp,
                    )
                }

                detail == null -> {
                    Text(
                        text = strings.deviceNotFound,
                        style = MaterialTheme.typography.bodyLarge,
                        color = FluentTextSecondary,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(24.dp),
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                    ) {
                        Text(
                            text = strings.deviceDetails,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = detail.device.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = FluentTextSecondary,
                        )
                        Spacer(Modifier.height(20.dp))

                        DetailSection(title = strings.deviceIdentity) {
                            DetailRow(strings.deviceName, detail.device.name)
                            DetailRow(strings.deviceId, detail.device.id)
                            DetailRow(strings.deviceType, detail.device.platform)
                            DetailRow(strings.registered, formatEpoch(detail.device.registeredAt))
                        }

                        Spacer(Modifier.height(16.dp))

                        DetailSection(title = strings.deviceStatus) {
                            DetailRow(
                                label = strings.connectionStatus,
                                value = if (detail.info.online) strings.online else strings.offline,
                                trailing = {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (detail.info.online) FluentSuccess else FluentTextSecondary,
                                            ),
                                    )
                                },
                            )
                            DetailRow(strings.appVersion, detail.info.appVersion)
                            DetailRow(strings.lastSeen, formatEpoch(detail.info.lastSeenAt))
                            DetailRow(
                                strings.remoteAddress,
                                detail.info.remoteAddress?.takeIf { it.isNotBlank() } ?: "—",
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        DetailSection(title = strings.deviceFacts) {
                            if (detail.facts.isEmpty()) {
                                Text(
                                    text = strings.noDeviceFacts,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = FluentTextSecondary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                )
                            } else {
                                detail.facts.forEachIndexed { index, fact ->
                                    DetailRow(fact.key, fact.value)
                                    if (index < detail.facts.lastIndex) {
                                        HorizontalDivider(color = FluentStroke)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCommandBar(
    strings: Strings,
    title: String,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FluentCard)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DesktopSecondaryButton(text = strings.back, onClick = onBack)
            Text(text = title, style = MaterialTheme.typography.titleLarge)
        }
        DesktopSecondaryButton(text = strings.refresh, onClick = onRefresh)
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardCorner)
            .border(1.dp, FluentStroke, CardCorner),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = FluentTextSecondary,
            modifier = Modifier
                .fillMaxWidth()
                .background(FluentSmoke)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        )
        HorizontalDivider(color = FluentStroke)
        content()
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.width(180.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = FluentTextSecondary,
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}
