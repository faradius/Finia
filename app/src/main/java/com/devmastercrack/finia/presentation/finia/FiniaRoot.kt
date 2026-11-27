package com.devmastercrack.finia.presentation.finia

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.presentation.finia.model.FiniaScreen
import com.devmastercrack.finia.presentation.finia.screens.AiScreen
import com.devmastercrack.finia.presentation.finia.screens.DebtsScreen
import com.devmastercrack.finia.presentation.finia.screens.HomeScreen
import com.devmastercrack.finia.presentation.finia.screens.MoreScreen
import com.devmastercrack.finia.presentation.finia.screens.NotifSettingsScreen
import com.devmastercrack.finia.presentation.finia.screens.NotificationsScreen
import com.devmastercrack.finia.presentation.finia.screens.ProfileScreen
import com.devmastercrack.finia.presentation.finia.screens.AccountsScreen
import com.devmastercrack.finia.presentation.finia.components.BottomNavBar
import com.devmastercrack.finia.presentation.finia.components.BottomNavFab
import com.devmastercrack.finia.presentation.finia.sheets.AccountDetailSheet
import com.devmastercrack.finia.presentation.finia.sheets.AccountPickerSheet
import com.devmastercrack.finia.presentation.finia.sheets.AddTransactionSheet
import com.devmastercrack.finia.presentation.finia.sheets.AdvancedDetailsSheet
import com.devmastercrack.finia.presentation.finia.sheets.CalendarDialog
import com.devmastercrack.finia.presentation.finia.sheets.CardPaymentSheet
import com.devmastercrack.finia.presentation.finia.sheets.CategoryPickerSheet
import com.devmastercrack.finia.presentation.finia.sheets.DestAccountPickerSheet
import com.devmastercrack.finia.presentation.finia.sheets.NewAccountSheet

// Reached via a forward icon tap and left via their own back arrow — a hierarchical push/pop,
// not a switch between peer bottom-tab destinations. Used to give AnimatedContent below a
// directional slide for these instead of the same fade-through used for tab switches, which
// read as an abrupt, unrelated hand-off for what's really "drilling in" and "backing out".
// The slide direction mirrors where the triggering icon actually sits on Home's header — the
// profile icon is top-left, so Profile slides in from the left; the bell is top-right (and
// NotifSettings is reached by drilling further forward from a list row), so those slide in
// from the right.
private val LeftDrillInScreens = setOf(FiniaScreen.PROFILE)
private val RightDrillInScreens = setOf(FiniaScreen.NOTIFICATIONS, FiniaScreen.NOTIF_SETTINGS)

@Composable
fun FiniaRoot(modifier: Modifier = Modifier, vm: FiniaViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val showBottomNav = state.screen !in setOf(
        FiniaScreen.AI, FiniaScreen.PROFILE, FiniaScreen.NOTIFICATIONS, FiniaScreen.NOTIF_SETTINGS,
    )

    // Hide the bottom bar + FAB on scroll-down, reveal them on scroll-up (m3.material.io
    // FAB "Behaviors" guidance: FABs move independently of scrolling content). A
    // NestedScrollConnection placed above the screen content observes scroll deltas from
    // whatever scrollable each screen uses (LazyColumn, verticalScroll, ...) without needing
    // each screen to opt in individually.
    //
    // Only UserInput deltas count (not SideEffect/fling), and deltas must accumulate past a
    // threshold before flipping — otherwise the small rubber-band bounce a LazyColumn does
    // when it hits the end of its content re-triggers "scroll up" and the bar pops back on
    // right after it hides.
    var bottomBarVisible by remember { mutableStateOf(true) }
    LaunchedEffect(state.screen) { bottomBarVisible = true }
    var scrollAccumPx by remember { mutableFloatStateOf(0f) }
    val scrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput || available.y == 0f) return Offset.Zero
                if (scrollAccumPx != 0f && (scrollAccumPx < 0f) != (available.y < 0f)) scrollAccumPx = 0f
                scrollAccumPx += available.y
                val thresholdPx = 40f
                if (scrollAccumPx <= -thresholdPx) {
                    bottomBarVisible = false
                    scrollAccumPx = 0f
                } else if (scrollAccumPx >= thresholdPx) {
                    bottomBarVisible = true
                    scrollAccumPx = 0f
                }
                return Offset.Zero
            }
        }
    }
    var barHeightPx by remember { mutableIntStateOf(0) }
    var fabHeightPx by remember { mutableIntStateOf(0) }
    // Scaffold leaves a 16dp gap (FabSpacing) between the FAB and the bar above it; that
    // gap has to be part of the hide distance too, plus a little extra so no sliver of the
    // rounded corner peeks past the screen edge.
    val fabExtraHidePx = with(LocalDensity.current) { 32.dp.roundToPx() }
    // Snappy, no-overshoot spring while hiding (overshooting off-screen isn't visible anyway);
    // a bouncy spring on the way back in so the bar/FAB settle with a little overshoot wobble.
    val hideSpec = spring<Int>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
    val revealSpec = spring<Int>(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
    // Screens like Profile/AI/Notifications that never show the bar are treated as just another
    // "hidden" state, animated through the exact same offset spring as the scroll-driven hide —
    // previously they instead conditionally skipped composing BottomNavBar/BottomNavFab
    // entirely, which popped them in/out in a single frame with no animation at all (the
    // "jumps/flickers" navigating to/from Profile).
    val effectiveBarVisible = bottomBarVisible && showBottomNav
    val barOffsetPx by animateIntAsState(
        targetValue = if (effectiveBarVisible) 0 else barHeightPx,
        animationSpec = if (effectiveBarVisible) revealSpec else hideSpec,
        label = "bottomBarOffset",
    )
    val fabOffsetPx by animateIntAsState(
        targetValue = if (effectiveBarVisible) 0 else fabHeightPx + barHeightPx + fabExtraHidePx,
        animationSpec = if (effectiveBarVisible) revealSpec else hideSpec,
        label = "fabOffset",
    )

    Box(modifier.fillMaxSize().background(FiniaColors.ScreenBg)) {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            // targetSdk 36 means edge-to-edge is mandatory (Android 15+ no longer lets an app
            // opt out) — content draws under the status/navigation bars unless something
            // explicitly reserves that space. Scaffold's own default already does this, but
            // stating it explicitly here removes any doubt: real devices with a visible status
            // bar and a 3-button (not gesture) navigation bar were showing content drawn flush
            // under both, since those insets are exactly what WindowInsets.systemBars covers.
            contentWindowInsets = WindowInsets.systemBars,
            bottomBar = {
                // Always composed (never conditional on showBottomNav) so the offset animation
                // above can actually play when a screen change is what's hiding it — see
                // effectiveBarVisible.
                BottomNavBar(
                    screen = state.screen,
                    onHome = vm::goHome,
                    onAccounts = vm::goAccounts,
                    onAdd = vm::openAdd,
                    onAI = vm::goAI,
                    onMore = vm::goSettings,
                    modifier = Modifier
                        .onSizeChanged { barHeightPx = it.height }
                        .offset { IntOffset(0, barOffsetPx) },
                )
            },
            floatingActionButton = {
                BottomNavFab(
                    onClick = vm::openAdd,
                    modifier = Modifier
                        .onSizeChanged { fabHeightPx = it.height }
                        .offset { IntOffset(0, fabOffsetPx) },
                )
            },
            floatingActionButtonPosition = FabPosition.End,
        ) { innerPadding ->
            // Scaffold reserves bottom padding for the bar's full, fixed height regardless of
            // our visual offset trick above (offsetting doesn't shrink what it reserves) — so
            // hiding the bar used to leave that reserved strip sitting empty. Animate the
            // applied bottom padding down in lockstep with the bar itself so the content
            // actually expands into the space the bar just vacated.
            val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
            val density2 = LocalDensity.current
            val fullBottomPaddingPx = with(density2) { innerPadding.calculateBottomPadding().roundToPx() }
            val contentBottomPaddingPx by animateIntAsState(
                targetValue = if (effectiveBarVisible) fullBottomPaddingPx else 0,
                animationSpec = if (effectiveBarVisible) revealSpec else hideSpec,
                label = "contentBottomPadding",
            )
            AnimatedContent(
                targetState = state.screen,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = innerPadding.calculateStartPadding(layoutDirection),
                        end = innerPadding.calculateEndPadding(layoutDirection),
                        top = innerPadding.calculateTopPadding(),
                        bottom = with(density2) { contentBottomPaddingPx.toDp() },
                    )
                    .nestedScroll(scrollConnection),
                transitionSpec = {
                    val enteringLeft = targetState in LeftDrillInScreens && initialState !in LeftDrillInScreens
                    val leavingLeft = initialState in LeftDrillInScreens && targetState !in LeftDrillInScreens
                    val enteringRight = targetState in RightDrillInScreens && initialState !in RightDrillInScreens
                    val leavingRight = initialState in RightDrillInScreens && targetState !in RightDrillInScreens
                    val slideSpec = tween<androidx.compose.ui.unit.IntOffset>(300, easing = FastOutSlowInEasing)
                    when {
                        // Push: the drill-in screen slides in from whichever side its trigger
                        // icon sits on, while the screen behind it softly recedes (fade +
                        // slight scale down), instead of both cross-fading in place.
                        enteringLeft -> {
                            (slideInHorizontally(animationSpec = slideSpec) { -it } + fadeIn(tween(300)))
                                .togetherWith(fadeOut(tween(200)) + scaleOut(targetScale = 0.96f, animationSpec = tween(300)))
                        }
                        enteringRight -> {
                            (slideInHorizontally(animationSpec = slideSpec) { it } + fadeIn(tween(300)))
                                .togetherWith(fadeOut(tween(200)) + scaleOut(targetScale = 0.96f, animationSpec = tween(300)))
                        }
                        // Pop: mirrors its push exactly in reverse, so the back arrow reads as
                        // "undoing" the same motion that opened the screen.
                        leavingLeft -> {
                            (fadeIn(tween(300)) + scaleIn(initialScale = 0.96f, animationSpec = tween(300)))
                                .togetherWith(slideOutHorizontally(animationSpec = slideSpec) { -it } + fadeOut(tween(200)))
                        }
                        leavingRight -> {
                            (fadeIn(tween(300)) + scaleIn(initialScale = 0.96f, animationSpec = tween(300)))
                                .togetherWith(slideOutHorizontally(animationSpec = slideSpec) { it } + fadeOut(tween(200)))
                        }
                        // Peer bottom-tab switches keep the existing "fade through": outgoing
                        // screen fades out quickly, incoming screen fades + scales in from
                        // slightly smaller.
                        else -> {
                            (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                                scaleIn(initialScale = 0.94f, animationSpec = tween(220, delayMillis = 90)))
                                .togetherWith(fadeOut(animationSpec = tween(90)))
                        }
                    }
                },
                label = "screenTransition",
            ) { screen ->
                when (screen) {
                    FiniaScreen.HOME -> HomeScreen(state, vm)
                    FiniaScreen.ACCOUNTS -> AccountsScreen(state, vm)
                    FiniaScreen.DEBTS -> DebtsScreen(state, vm)
                    FiniaScreen.AI -> AiScreen(state, vm)
                    FiniaScreen.SETTINGS -> MoreScreen(state, vm)
                    FiniaScreen.PROFILE -> ProfileScreen(state, vm)
                    FiniaScreen.NOTIFICATIONS -> NotificationsScreen(state, vm)
                    FiniaScreen.NOTIF_SETTINGS -> NotifSettingsScreen(state, vm)
                }
            }
        }

        // Overlay stack, back-to-front, mirroring Finia.dc.html's z-index order.
        if (state.addOpen) {
            AddTransactionSheet(state, vm)
        }
        if (state.advancedOpen) {
            AdvancedDetailsSheet(state, vm)
        }
        if (state.accountConfigOpen != null) {
            AccountDetailSheet(state, vm)
        }
        if (state.categoryPickerOpen) {
            CategoryPickerSheet(state, vm)
        }
        if (state.accountPickerOpen) {
            AccountPickerSheet(state, vm)
        }
        if (state.destAccountPickerOpen) {
            DestAccountPickerSheet(state, vm)
        }
        if (state.datePickerOpen) {
            CalendarDialog(
                selectedIso = state.calSelected,
                onCancel = vm::toggleDatePicker,
                onAccept = { iso -> vm.selectCalDay(iso); vm.acceptDate() },
            )
        }
        if (state.newAccountOpen && !state.accountPickerOpen) {
            NewAccountSheet(state, vm)
        }
        if (state.cardPaymentOpen) {
            CardPaymentSheet(state, vm)
        }
        if (state.deleteTxPending != null) {
            com.devmastercrack.finia.presentation.finia.sheets.DeleteTransactionDialog(state, vm)
        }
        if (state.txDetailOpen != null) {
            com.devmastercrack.finia.presentation.finia.sheets.TransactionDetailSheet(state, vm)
        }
        if (state.homeMonthPickerOpen) {
            val base = java.time.YearMonth.of(2026, 7)
            com.devmastercrack.finia.presentation.finia.sheets.MonthYearPickerDialog(
                selectedYearMonth = base.plusMonths(state.homeScreenMonthOffset.toLong()),
                currentYearMonth = base,
                onCancel = vm::toggleHomeMonthPicker,
                onSelect = { ym -> vm.selectHomeMonthOffset(java.time.temporal.ChronoUnit.MONTHS.between(base, ym).toInt()) },
            )
        }
        if (state.acctMonthPickerOpen) {
            val base = java.time.YearMonth.of(2026, 7)
            com.devmastercrack.finia.presentation.finia.sheets.MonthYearPickerDialog(
                selectedYearMonth = base.plusMonths(state.acctMonthOffset.toLong()),
                currentYearMonth = base,
                onCancel = vm::toggleAcctMonthPicker,
                onSelect = { ym -> vm.selectAcctMonthOffset(java.time.temporal.ChronoUnit.MONTHS.between(base, ym).toInt()) },
            )
        }
        if (state.accCalOpen) {
            CalendarDialog(
                selectedIso = state.accCalSelected,
                onCancel = vm::closeAccCal,
                onAccept = { iso -> vm.selectAccCalDay(iso); vm.acceptAccCal() },
            )
        }

        val snackbarText = state.snackbar
        // Centralized auto-dismiss — most call sites that set state.snackbar never scheduled a
        // clear themselves (only the AI-auto-categorization one did, via its own delayed
        // coroutine), so those snackbars just stayed on screen forever. Keying on snackbarText
        // means a new message restarts the timer instead of the old one's countdown closing it
        // early.
        LaunchedEffect(snackbarText) {
            if (snackbarText != null) {
                kotlinx.coroutines.delay(2200)
                vm.clearSnackbar()
            }
        }
        if (snackbarText != null) {
            // M3 snackbar guidelines (m3.material.io/components/snackbar/guidelines):
            // "Avoid placing snackbars in front of navigation" and "Snackbars should appear
            // above FABs" — clearing the bar's height alone wasn't enough, since the FAB floats
            // higher still (bar height + Scaffold's own FabSpacing gap + the FAB's own height).
            // That clearance only makes sense while the bar/FAB are actually visible, though —
            // when they're hidden (scrolled away, or a screen that never shows them),
            // reserving that same space just left a big dead gap under the snackbar instead of
            // it dropping down toward the edge. Animated with the same effectiveBarVisible/
            // spring pair the bar itself hides and reveals with, so the snackbar tracks it.
            val fabSpacingPx = with(LocalDensity.current) { 16.dp.roundToPx() }
            val snackbarClearancePx by animateIntAsState(
                targetValue = if (effectiveBarVisible) barHeightPx + fabSpacingPx + fabHeightPx else 0,
                animationSpec = if (effectiveBarVisible) revealSpec else hideSpec,
                label = "snackbarClearance",
            )
            // Always-on floor: the system nav bar/gesture inset plus a small margin, so even
            // with the bar/FAB fully hidden the snackbar still stops short of the very edge.
            val navBarInsetDp = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val snackbarBottomPadding = with(LocalDensity.current) { snackbarClearancePx.toDp() } + navBarInsetDp + 16.dp
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = snackbarBottomPadding, top = 24.dp),
            ) {
                com.devmastercrack.finia.presentation.finia.components.FiniaSnackbar(snackbarText)
            }
        }
    }
}
