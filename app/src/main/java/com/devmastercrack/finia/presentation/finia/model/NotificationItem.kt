package com.devmastercrack.finia.presentation.finia.model

import androidx.compose.ui.graphics.Color

data class NotificationItem(
    val id: Int,
    val emoji: String,
    val iconBg: Color,
    val titulo: String,
    val detalle: String,
    val tiempo: String,
    val dia: String,
    val unread: Boolean,
)
