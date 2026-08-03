package com.devmastercrack.finia.presentation.finia.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateTo
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devmastercrack.finia.core.theme.FiniaColors
import com.devmastercrack.finia.core.theme.FiniaText
import com.devmastercrack.finia.presentation.finia.FiniaUiState
import com.devmastercrack.finia.presentation.finia.FiniaViewModel
import com.devmastercrack.finia.presentation.finia.components.AccountCard
import com.devmastercrack.finia.presentation.finia.components.AddAccountCard
import com.devmastercrack.finia.presentation.finia.components.FlowFilterCard
import com.devmastercrack.finia.presentation.finia.components.SegmentedControl
import com.devmastercrack.finia.presentation.finia.components.TransactionRow
import com.devmastercrack.finia.presentation.finia.deriveAccountView
import com.devmastercrack.finia.presentation.finia.filterAccountTx
import com.devmastercrack.finia.presentation.finia.filterAccounts
import com.devmastercrack.finia.presentation.finia.groupByFecha
import com.devmastercrack.finia.presentation.finia.model.AccountTypeFilter
import com.devmastercrack.finia.presentation.finia.model.Transaction
import com.devmastercrack.finia.presentation.finia.model.TxFlow
import com.devmastercrack.finia.presentation.finia.util.MESES_FULL
import com.devmastercrack.finia.presentation.finia.util.fmt
import java.time.YearMonth
import kotlinx.coroutines.launch

@Composable
fun AccountsScreen(state: FiniaUiState, vm: FiniaViewModel, modifier: Modifier = Modifier) {
    val filtered = remember(state.accounts, state.accountTypeFilter) { filterAccounts(state.accounts, state.accountTypeFilter) }
    val activeAccount = filtered.getOrNull(state.activeAccountIdx) ?: filtered.firstOrNull()
    // Scoped to the filter, not shared across it: reusing one LazyListState across Todo/Cuentas/
    // Tarjetas meant the raw scroll-pixel-offset from one dataset (e.g. sitting on card 3) got
    // carried over and reapplied to a completely different, shorter dataset — that mismatch was
    // the "raro" jump. A fresh state per filter starts clean at position 0 with no correction
    // needed, so there's nothing stale left to animate through.
    val listState = key(state.accountTypeFilter) { rememberLazyListState() }
    val density = androidx.compose.ui.platform.LocalDensity.current
    val cardWidthPx = with(density) { 296.dp.roundToPx() }
    val cardSpacingPx = with(density) { 14.dp.roundToPx() }
    val carouselScope = androidx.compose.runtime.rememberCoroutineScope()
    // The exact spring the manual swipe settles into when it snaps to a card (passed to
    // snapFlingBehavior below) — reused here so a tap-triggered move animates identically
    // instead of using LazyListState.animateScrollToItem's own (softer, fixed) built-in spec.
    val carouselSnapSpec = androidx.compose.animation.core.spring<Float>(stiffness = androidx.compose.animation.core.Spring.StiffnessHigh)

    // Mirrors SnapPosition.Center's own position() formula — viewportSize includes the content
    // padding, so it has to be subtracted first, the same way SnapPosition.Center's own
    // position() does it — using the raw viewport width here (without subtracting padding) was
    // an earlier bug. Drives a manual scroll (not animateScrollToItem, whose built-in animation
    // spec can't be overridden and feels noticeably different) so a tap-triggered move and a
    // manual-swipe settle both animate with the identical carouselSnapSpec spring. Shared by the
    // effect below (index changed programmatically/by settle-detection) and by a direct card
    // tap, so tapping a non-active card scrolls it into view exactly like a manual swipe would.
    suspend fun centerOnAccount(index: Int) {
        if (filtered.isEmpty()) return
        val info = listState.layoutInfo
        val availableSpace = info.viewportSize.width - info.beforeContentPadding - info.afterContentPadding
        val desiredItemOffset = availableSpace / 2 - cardWidthPx / 2
        val targetIndex = index.coerceIn(0, filtered.lastIndex)
        // All cards (including the trailing "add account" one) share the exact same fixed
        // width, so the distance scrolled per index is constant — no need to know each item's
        // real on-screen position, just the current absolute scroll distance from index 0.
        val stridePx = cardWidthPx + cardSpacingPx
        val currentAbsolute = listState.firstVisibleItemIndex * stridePx + listState.firstVisibleItemScrollOffset
        val targetAbsolute = targetIndex * stridePx - desiredItemOffset
        val delta = (targetAbsolute - currentAbsolute).toFloat()
        if (kotlin.math.abs(delta) < 0.5f) return
        listState.scroll {
            val scrollScope = this
            var previous = 0f
            androidx.compose.animation.core.AnimationState(0f).animateTo(delta, carouselSnapSpec) {
                previous += scrollScope.scrollBy(value - previous)
            }
        }
    }

    // No firstVisibleItemIndex guard here — with these near-full-width, center-snapped cards,
    // the "previous" card is often already partially peeking at the left edge and therefore
    // already counted as the first *visible* item even when it isn't centered yet. Comparing
    // index equality against that made moving to a lower index (e.g. the detail sheet's "<"
    // button/switchAccountPrev) silently no-op the scroll, since the guard read as "already
    // there" when it wasn't. centerOnAccount's own delta<0.5px check is a precise, reliable
    // no-op guard on its own — safe to call unconditionally, including right after a manual
    // swipe settles (delta there is already ~0, so it's a real no-op, not a redundant re-scroll).
    LaunchedEffect(state.activeAccountIdx, filtered) {
        centerOnAccount(state.activeAccountIdx)
    }
    // Which card renders as "active" (full opacity). Kept separate from state.activeAccountIdx
    // itself, which only updates once the scroll settles (see the isScrollInProgress collector
    // below) — during a manual swipe that lag isn't noticeable since it lines up with the drag
    // physically slowing to a stop, but for a *tap* it meant the tapped card visibly glided to
    // the middle still dim and only lit up a beat after it had already arrived. Setting this the
    // instant a card is tapped makes it light up immediately, in step with the glide; it's then
    // resynced to the confirmed activeAccountIdx once settling actually happens (a no-op by then
    // for taps, and the only thing driving it at all for swipes).
    var visualActiveIdx by remember { androidx.compose.runtime.mutableIntStateOf(state.activeAccountIdx) }
    LaunchedEffect(state.activeAccountIdx) { visualActiveIdx = state.activeAccountIdx }
    // This effect is keyed on `listState` alone (a stable instance) so it launches once and
    // keeps collecting the whole time the screen is alive — it must NOT read `state`/`filtered`
    // directly, because a running coroutine's closure keeps whatever value those had at launch
    // time; it never sees later recompositions. That stale read was why the dot indicator and
    // the "active" card highlight could get out of sync with where you actually scrolled to.
    // rememberUpdatedState keeps a live reference the coroutine can re-read on every collection.
    val latestActiveIdx = androidx.compose.runtime.rememberUpdatedState(state.activeAccountIdx)
    val latestFiltered = androidx.compose.runtime.rememberUpdatedState(filtered)
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.collect { scrolling ->
            val items = latestFiltered.value
            if (!scrolling && items.isNotEmpty()) {
                val info = listState.layoutInfo
                if (info.visibleItemsInfo.isNotEmpty()) {
                    val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2
                    val closest = info.visibleItemsInfo
                        .filter { it.index < items.size }
                        .minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - viewportCenter) }
                    if (closest != null && closest.index != latestActiveIdx.value) {
                        vm.setActiveAccountIdx(closest.index)
                    }
                }
            }
        }
    }

    val inCurrentMonth = state.acctMonthOffset == 0
    val accountTx = remember(state.recentTx, activeAccount) {
        if (activeAccount == null) emptyList() else state.recentTx.filter { it.cuentaId == activeAccount.id }
    }
    val spentThisMonth = if (inCurrentMonth) accountTx.filter { it.monto < 0 }.sumOf { -it.monto } else 0.0
    val paidThisMonth = if (inCurrentMonth) accountTx.filter { it.monto > 0 }.sumOf { it.monto } else 0.0
    // Only chips for categories that actually have a matching record — scoped by account/month/
    // flow (everything the list itself is filtered by) but not by category, so every chip still
    // shows. accountTx alone (ignoring month/flow) was letting categories with zero visible
    // records still show up as chips.
    val categories = remember(state.recentTx, activeAccount, state.homeFlowFilter, inCurrentMonth) {
        if (activeAccount == null) listOf("Todo")
        else listOf("Todo") + filterAccountTx(state.recentTx, activeAccount.id, "Todo", state.homeFlowFilter, inCurrentMonth)
            .map { it.categoria }.distinct()
    }
    // If the flow filter changes out from under the currently selected category (its chip no
    // longer exists above), fall back to "Todo" instead of leaving an orphaned selection with no
    // highlighted chip.
    LaunchedEffect(categories) {
        if (state.homeCategoryFilter !in categories) vm.setHomeCategory("Todo")
    }
    val filteredTx = remember(state.recentTx, activeAccount, state.homeCategoryFilter, state.homeFlowFilter, inCurrentMonth) {
        if (activeAccount == null) emptyList()
        else filterAccountTx(state.recentTx, activeAccount.id, state.homeCategoryFilter, state.homeFlowFilter, inCurrentMonth)
    }
    val groups = remember(filteredTx) { groupByFecha(filteredTx) }
    // Search looks across all of the active account's transactions (not just the current
    // category/flow chip selection) since "buscar entre los registros" means finding something
    // regardless of which filter happens to be active, not narrowing further within it.
    val searchResults = remember(accountTx, state.acctSearchQuery) {
        val q = state.acctSearchQuery.trim()
        if (q.isEmpty()) emptyList() else accountTx.filter { it.concepto.contains(q, ignoreCase = true) || it.categoria.contains(q, ignoreCase = true) }
    }
    val searchGroups = remember(searchResults) { groupByFecha(searchResults) }
    val isSearching = state.acctSearchOpen && state.acctSearchQuery.isNotBlank()
    val displayGroups = if (isSearching) searchGroups else groups
    // Position of each row within the currently filtered list, used to stagger its entrance
    // animation. Ids already animated once are tracked separately so scrolling a row out of
    // view and back never replays the entrance — only a genuinely new filtered list (new key
    // here, since filteredTx's own `remember` deps changed) resets and re-animates.
    val txStaggerIndex = remember(filteredTx) { filteredTx.mapIndexed { i, t -> t.id to i }.toMap() }
    val animatedTxIds = remember(filteredTx) { androidx.compose.runtime.mutableStateSetOf<Int>() }
    val base = remember(state.acctMonthOffset) { YearMonth.of(2026, 7).plusMonths(state.acctMonthOffset.toLong()) }
    val monthLabel = MESES_FULL[base.monthValue - 1]

    val searchFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(state.acctSearchOpen) {
        if (state.acctSearchOpen) {
            searchFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    // Only the transaction list (in its own nested LazyColumn below, with weight(1f)) scrolls —
    // the account carousel, filters, and stat cards above it stay pinned instead of scrolling
    // away with everything sharing one LazyColumn.
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(top = 18.dp, start = 20.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Mis cuentas", style = FiniaText.ScreenTitle, color = FiniaColors.TextPrimary,
                modifier = Modifier.weight(1f),
            )
            androidx.compose.material3.IconButton(onClick = vm::toggleAcctSearch) {
                Icon(
                    if (state.acctSearchOpen) Icons.Filled.Close else Icons.Filled.Search,
                    contentDescription = if (state.acctSearchOpen) "Cerrar búsqueda" else "Buscar movimientos",
                    tint = FiniaColors.TextPrimary, modifier = Modifier.size(22.dp),
                )
            }
        }
        if (state.acctSearchOpen) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 20.dp, end = 20.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(FiniaColors.SurfaceNeutral)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = FiniaColors.TextSecondary, modifier = Modifier.size(18.dp))
                androidx.compose.foundation.text.BasicTextField(
                    value = state.acctSearchQuery,
                    onValueChange = vm::onAcctSearchQueryChange,
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = FiniaColors.TextPrimary),
                    decorationBox = { inner ->
                        if (state.acctSearchQuery.isEmpty()) {
                            Text("Buscar por descripción o categoría", style = FiniaText.Secondary.copy(fontSize = 14.sp), color = FiniaColors.TextSecondary)
                        }
                        inner()
                    },
                    modifier = Modifier.weight(1f).padding(start = 10.dp).focusRequester(searchFocusRequester),
                )
            }
        }
        SegmentedControl(
            options = listOf("Todo", "Cuentas", "Tarjetas"),
            selectedIndex = when (state.accountTypeFilter) {
                AccountTypeFilter.TODO -> 0
                AccountTypeFilter.CUENTAS -> 1
                AccountTypeFilter.TARJETAS -> 2
            },
            onSelect = { i ->
                vm.setAccountTypeFilter(listOf(AccountTypeFilter.TODO, AccountTypeFilter.CUENTAS, AccountTypeFilter.TARJETAS)[i])
            },
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp, start = 20.dp, end = 20.dp),
        )
        run {
            // Purely visual entrance slide when the filter changes — the new (fresh, filter-
            // scoped) list state has no scroll position to animate from, so this gives the
            // "you can see it move" motion the reset otherwise lacks, without touching the
            // scroll/snap logic that fixed the mismatched-content glitch.
            val slideOffset = remember(state.accountTypeFilter) { androidx.compose.animation.core.Animatable(28f) }
            LaunchedEffect(state.accountTypeFilter) {
                slideOffset.animateTo(0f, androidx.compose.animation.core.tween(280, easing = androidx.compose.animation.core.FastOutSlowInEasing))
            }
            LazyRow(
                state = listState,
                // Center-aligned so the active card sits in the middle with a sliver of the
                // previous AND next card peeking on both sides — signals to the user that there
                // are cards in both directions, not just to the right.
                // The active-card info below only updates once isScrollInProgress goes false, so
                // a stiffer settle spring here (vs. the default StiffnessMediumLow) means less
                // time between letting go and the content actually refreshing.
                flingBehavior = run {
                    val decaySpec = androidx.compose.animation.rememberSplineBasedDecay<Float>()
                    remember(listState, decaySpec) {
                        val snapLayoutInfo = androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider(listState)
                        androidx.compose.foundation.gestures.snapping.snapFlingBehavior(
                            snapLayoutInfoProvider = snapLayoutInfo,
                            decayAnimationSpec = decaySpec,
                            snapAnimationSpec = carouselSnapSpec,
                        )
                    }
                },
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { translationX = slideOffset.value.dp.toPx() },
            ) {
                itemsIndexed(filtered, key = { _, a -> a.id }) { i, acc ->
                    val view = remember(acc, state.creditPrimaryView) { deriveAccountView(acc, state.creditPrimaryView) }
                    AccountCard(
                        view = view,
                        isActive = i == visualActiveIdx,
                        // Tapping a card that isn't centered yet lights it up immediately
                        // (visualActiveIdx) and scrolls it into view — the same spring a manual
                        // swipe settles into (centerOnAccount) — instead of waiting for the
                        // scroll-settle effect to flip it active only once it already looks
                        // arrived. Only tapping the already-active card opens its detail sheet.
                        onClick = {
                            if (i == visualActiveIdx) {
                                vm.openAccountDetail(acc.id)
                            } else {
                                visualActiveIdx = i
                                carouselScope.launch { centerOnAccount(i) }
                            }
                        },
                    )
                }
                item {
                    AddAccountCard(onClick = vm::openNewAccountSheet)
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp), horizontalArrangement = Arrangement.Center,
        ) {
            filtered.forEachIndexed { i, _ ->
                Box(
                    Modifier
                        .padding(horizontal = 3.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (i == state.activeAccountIdx) FiniaColors.Accent else Color(0xFFDCDAD3)),
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 14.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.material3.IconButton(onClick = vm::acctPrevMonth, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior", tint = Color(0xFF5F6359), modifier = Modifier.size(20.dp))
            }
            // Tapping the month name opens the month/year picker, same as HomeScreen's "Julio".
            Text(
                monthLabel, style = FiniaText.RowTitleBold, color = FiniaColors.TextPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = vm::toggleAcctMonthPicker)
                    .padding(horizontal = 14.dp, vertical = 2.dp),
            )
            val nextEnabled = state.acctMonthOffset < 0
            androidx.compose.material3.IconButton(onClick = vm::acctNextMonth, enabled = nextEnabled, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente",
                    tint = if (nextEnabled) Color(0xFF5F6359) else Color(0xFFC9C6BC), modifier = Modifier.size(20.dp),
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 10.dp, start = 20.dp, end = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FlowFilterCard(
                Modifier.weight(1f), "Gastado", spentThisMonth, Icons.Filled.ArrowDownward, FiniaColors.Danger,
                active = state.homeFlowFilter == TxFlow.GASTO,
                activeBg = FiniaColors.DangerSoft, activeBorder = FiniaColors.Danger,
                onClick = { vm.toggleHomeFlowFilter(TxFlow.GASTO) },
            )
            FlowFilterCard(
                Modifier.weight(1f), "Ingresos", paidThisMonth, Icons.Filled.ArrowUpward, FiniaColors.Accent,
                active = state.homeFlowFilter == TxFlow.INGRESO,
                activeBg = FiniaColors.AccentSoft, activeBorder = FiniaColors.Accent,
                onClick = { vm.toggleHomeFlowFilter(TxFlow.INGRESO) },
            )
        }
        CategoryChipRow(categories = categories, selected = state.homeCategoryFilter, onSelect = vm::setHomeCategory)
        androidx.compose.foundation.layout.Spacer(Modifier.size(4.dp))

        // Only this part scrolls — its own LazyColumn, independent from the fixed section above.
        // Keyed on the active account: switching cards swaps in an entirely different set of
        // transaction ids, so this is a hard content swap, not a reorder within one continuous
        // list. Keying forces Compose to fully tear down and remount the LazyColumn — a fresh
        // LazyListState starting at the top, with no lingering exit-animating rows from the
        // previous account left mid-transition to visually clash with the new ones. animateItem()
        // below still handles smooth reordering/fade for changes *within* the same account
        // (e.g. switching the category/flow filter).
        key(activeAccount?.id) {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (displayGroups.isEmpty()) {
                    item {
                        Text(
                            when {
                                isSearching -> "Sin resultados para \"${state.acctSearchQuery}\""
                                inCurrentMonth -> "Sin movimientos en esta cuenta"
                                else -> "Sin movimientos registrados este mes"
                            },
                            style = FiniaText.SecondarySmall.copy(fontSize = 13.sp), color = FiniaColors.TextSecondary,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                } else {
                    displayGroups.forEach { group ->
                        item {
                            Text(
                                group.fecha.uppercase(), style = FiniaText.Overline, color = FiniaColors.TextSecondary,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 20.dp, end = 20.dp),
                            )
                        }
                        items(group.items, key = { it.id }) { tx ->
                            // Same swipe-to-delete as the Home screen's "Movimientos
                            // recientes" — the confirmation dialog itself is rendered globally
                            // off state.deleteTxPending (see FiniaRoot/DeleteTransactionDialog),
                            // this only needs to trigger the request.
                            val dismissState = androidx.compose.material3.rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart) {
                                        vm.requestDeleteTx(tx.id)
                                    }
                                    false
                                },
                            )
                            androidx.compose.material3.SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                modifier = Modifier.animateItem(),
                                backgroundContent = {
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .background(FiniaColors.Danger)
                                            .padding(horizontal = 24.dp),
                                        contentAlignment = Alignment.CenterEnd,
                                    ) {
                                        Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = Color.White)
                                    }
                                },
                            ) {
                                // background(ScreenBg) stays full-bleed so it fully occludes the
                                // red swipe panel behind it, but padding is applied *before*
                                // clip/clickable so the ripple itself is bounded to a rounded
                                // rect matching the row's visible inset, not a square ripple
                                // running edge-to-edge past the content.
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .background(FiniaColors.ScreenBg)
                                        .padding(horizontal = 20.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable(onClick = { vm.openTxDetail(tx.id) }),
                                ) {
                                    AnimatedTransactionRow(
                                        tx = tx,
                                        staggerIndex = txStaggerIndex[tx.id] ?: 0,
                                        alreadyAnimated = tx.id in animatedTxIds,
                                        onAnimated = { animatedTxIds += tx.id },
                                    )
                                }
                            }
                        }
                    }
                }
                item { Box(Modifier.size(1.dp, 100.dp)) }
            }
        }
    }
}

/**
 * Wraps [TransactionRow] with a fade-in + slide-up entrance, staggered by [staggerIndex] so
 * rows cascade in one after another instead of popping in all at once. Only plays the first
 * time a given row appears in the current filtered list — [alreadyAnimated] (backed by a set
 * the caller resets when the filter/account actually changes) keeps scrolling a row out of
 * view and back from replaying it every time.
 */
@Composable
private fun AnimatedTransactionRow(
    tx: Transaction,
    staggerIndex: Int,
    alreadyAnimated: Boolean,
    onAnimated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val alpha = remember { androidx.compose.animation.core.Animatable(if (alreadyAnimated) 1f else 0f) }
    val offsetY = remember { androidx.compose.animation.core.Animatable(if (alreadyAnimated) 0f else 14f) }
    LaunchedEffect(Unit) {
        if (!alreadyAnimated) {
            onAnimated()
            // Stagger only the first handful of rows — beyond that the cascade just reads as
            // slow to load rather than friendly.
            kotlinx.coroutines.delay(staggerIndex.coerceAtMost(9) * 30L)
            kotlinx.coroutines.coroutineScope {
                launch { alpha.animateTo(1f, androidx.compose.animation.core.tween(220, easing = androidx.compose.animation.core.FastOutSlowInEasing)) }
                launch { offsetY.animateTo(0f, androidx.compose.animation.core.tween(260, easing = androidx.compose.animation.core.FastOutSlowInEasing)) }
            }
        }
    }
    TransactionRow(
        tx, tx.categoria,
        modifier = modifier.graphicsLayer {
            this.alpha = alpha.value
            translationY = offsetY.value.dp.toPx()
        },
    )
}

/**
 * Category filter chips. Each chip owns and animates its own selected/unselected background —
 * deliberately NOT a separately-positioned floating indicator box. A floating box has to track
 * the selected chip's on-screen position every frame, and since that position changes as the
 * row scrolls, the box visibly slides sideways during scrolling even with instant (non-animated)
 * repositioning. Painting the highlight as part of the chip itself means it scrolls as ordinary
 * LazyRow content — glued to the chip with zero extra tracking — and only animates (color
 * crossfade) when the selection itself actually changes.
 */
@Composable
private fun CategoryChipRow(categories: List<String>, selected: String, onSelect: (String) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 6.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(categories) { cat ->
            val isSelected = cat == selected
            val bg by animateColorAsState(if (isSelected) FiniaColors.Accent else FiniaColors.SurfaceNeutral2, label = "chipBg")
            val fg by animateColorAsState(if (isSelected) Color.White else FiniaColors.TextPrimary, label = "chipFg")
            Box(
                Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(bg)
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    ) { onSelect(cat) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            ) {
                Text(cat, style = FiniaText.Chip, color = fg)
            }
        }
    }
}
