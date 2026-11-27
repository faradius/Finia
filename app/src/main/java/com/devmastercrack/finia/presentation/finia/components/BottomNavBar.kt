package com.devmastercrack.finia.presentation.finia.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.presentation.finia.model.FiniaScreen

/**
 * Stock Material 3 [NavigationBar] — inherits Google's spec metrics (80dp bar height,
 * 24dp icons, animated pill indicator, ripple, motion) instead of hand-tuned dp values.
 * The "+" action lives outside this bar as [BottomNavFab], docked bottom-end by the
 * caller via Scaffold(floatingActionButtonPosition = FabPosition.End).
 */
@Composable
fun BottomNavBar(
    screen: FiniaScreen,
    onHome: () -> Unit,
    onAccounts: () -> Unit,
    onAdd: () -> Unit,
    onAI: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = Color.White,
        contentColor = FiniaColors.TextSecondary,
        tonalElevation = 0.dp,
    ) {
        NavItem(Icons.Outlined.Home, Icons.Filled.Home, "Inicio", screen == FiniaScreen.HOME, onHome)
        NavItem(Icons.Outlined.CreditCard, Icons.Filled.CreditCard, "Cuentas", screen == FiniaScreen.ACCOUNTS, onAccounts)
        NavItem(Icons.Outlined.AutoAwesome, Icons.Filled.AutoAwesome, "Asistente", screen == FiniaScreen.AI, onAI)
        NavItem(Icons.Outlined.MoreHoriz, Icons.Filled.MoreHoriz, "Más", screen == FiniaScreen.SETTINGS, onMore)
    }
}

// M3 nav-bar convention: unselected tabs show the outlined icon, the selected tab switches to
// the filled variant of the same glyph — not two visually-unrelated icons per tab.
@Composable
private fun RowScope.NavItem(outlinedIcon: ImageVector, filledIcon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(if (selected) filledIcon else outlinedIcon, contentDescription = label) },
        label = { Text(label, style = FiniaText.NavLabel) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = FiniaColors.Accent,
            selectedTextColor = FiniaColors.Accent,
            indicatorColor = FiniaColors.AccentSoft,
            unselectedIconColor = FiniaColors.TextSecondary,
            unselectedTextColor = FiniaColors.TextSecondary,
        ),
    )
}

/**
 * "Add" action, docked bottom-end above the navigation bar — the placement M3's own
 * guidelines call out as the default for compact/phone screens: "the best place for
 * the FAB is typically the lower right corner of a screen, since it's easy to reach
 * and is less likely to cover important content"
 * (m3.material.io/components/floating-action-button/guidelines).
 *
 * Uses [FloatingActionButtonDefaults.shape] — the default rounded-square container,
 * matching the M3 spec's own FAB icon examples (not a plain circle).
 */
@Composable
fun BottomNavFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = FiniaColors.Accent,
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 3.dp),
    ) {
        Icon(Icons.Filled.Add, contentDescription = "Agregar gasto")
    }
}
