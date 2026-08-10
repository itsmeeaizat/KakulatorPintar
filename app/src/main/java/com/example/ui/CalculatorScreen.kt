package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CalculationHistoryEntity
import com.example.ui.theme.BorderDivider
import com.example.ui.theme.KeypadBackground
import com.example.ui.theme.OnPurplePrimaryContainer
import com.example.ui.theme.OnUtilityKeyContainer
import com.example.ui.theme.OperatorKeyContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurplePrimaryContainer
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.UtilityKeyContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showHistoryBottomSheet by remember { mutableStateOf(false) }
    var showPosCashierSheet by remember { mutableStateOf(false) }
    var posInitialTab by remember { mutableStateOf(0) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Auto-scroll history list to bottom when new item added
    LaunchedEffect(uiState.historyList.size) {
        if (uiState.historyList.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    // Popup Announcement Dialog (from raw assets announcement.json)
    AnnouncementPopupLauncher()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceCanvas),
        color = SurfaceCanvas
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Bar
            HeaderBar(
                userName = uiState.currentUser?.username ?: "Pengguna Utama",
                onHistoryClick = { showHistoryBottomSheet = true },
                onPosCashierClick = {
                    posInitialTab = 0
                    showPosCashierSheet = true
                },
                onEtalaseClick = {
                    posInitialTab = 1
                    showPosCashierSheet = true
                },
                onMenuClick = { showMenu = true }
            )

            // Action Sheet Menu (Modern Instagram/iOS Style)
            if (showMenu) {
                val optionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = { showMenu = false },
                    sheetState = optionsSheetState,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    containerColor = Color.White,
                    scrimColor = Color.Black.copy(alpha = 0.45f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Opsi Menu",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                        }

                        // Menu Item 0: Mode Kasir POS & Cetak Struk
                        ActionSheetItem(
                            title = "Mode Kasir & Struk Belanja (POS)",
                            icon = Icons.Outlined.ReceiptLong,
                            iconTint = PurplePrimary,
                            textColor = PurplePrimary,
                            onClick = {
                                showMenu = false
                                posInitialTab = 0
                                showPosCashierSheet = true
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = BorderDivider.copy(alpha = 0.5f))

                        // Menu Item 1: Etalase & Katalog Produk
                        ActionSheetItem(
                            title = "Etalase & Katalog Produk",
                            icon = Icons.Default.Category,
                            iconTint = PurplePrimary,
                            textColor = TextDark,
                            onClick = {
                                showMenu = false
                                posInitialTab = 1
                                showPosCashierSheet = true
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = BorderDivider.copy(alpha = 0.5f))

                        // Menu Item 1: Buka Riwayat Lengkap
                        ActionSheetItem(
                            title = "Buka Riwayat Lengkap",
                            icon = Icons.Outlined.History,
                            iconTint = TextDark,
                            textColor = TextDark,
                            onClick = {
                                showMenu = false
                                showHistoryBottomSheet = true
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = BorderDivider.copy(alpha = 0.5f))

                        // Menu Item 2: Hapus Semua Riwayat (Red action)
                        ActionSheetItem(
                            title = "Hapus Semua Riwayat",
                            icon = Icons.Outlined.Delete,
                            iconTint = MaterialTheme.colorScheme.error,
                            textColor = MaterialTheme.colorScheme.error,
                            onClick = {
                                showMenu = false
                                showClearConfirmDialog = true
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = BorderDivider.copy(alpha = 0.5f))

                        // Menu Item 3: Tentang Aplikasi
                        ActionSheetItem(
                            title = "Tentang Aplikasi",
                            icon = Icons.Outlined.Info,
                            iconTint = TextDark,
                            textColor = TextDark,
                            onClick = {
                                showMenu = false
                                showInfoDialog = true
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Cancel / Dismiss button
                        Button(
                            onClick = { showMenu = false },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KeypadBackground,
                                contentColor = TextDark
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text(
                                text = "Batal",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // Main Display Area (Top Left Expression, Top Right History List & Bottom Large Result)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                // Area Atas: Ekspresi Ketikan Saat Ini (Atas-Kiri) & Daftar Riwayat Perhitungan (Atas-Kanan)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // 1. Teks Input Ketikan Saat Ini / Ekspresi (Atas-Kiri)
                        Column(
                            modifier = Modifier
                                .weight(1.1f)
                                .padding(end = 12.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            if (uiState.expressionDisplay.isNotEmpty()) {
                                Text(
                                    text = uiState.expressionDisplay,
                                    fontSize = if (uiState.expressionDisplay.length > 20) 18.sp else 22.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSubtle,
                                    textAlign = TextAlign.Start,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("expression_display")
                                )
                            }
                        }

                        // 2. Daftar Riwayat Perhitungan (Atas-Kanan, nempel rapat ke bawah riwayat sebelumnya)
                        Column(
                            modifier = Modifier.weight(0.9f),
                            horizontalAlignment = Alignment.End
                        ) {
                            LazyColumn(
                                state = listState,
                                reverseLayout = false,
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(0.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                items(uiState.historyList) { item ->
                                    HistoryPreviewRow(
                                        item = item,
                                        onClick = { viewModel.onHistoryItemSelect(item) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Error Message banner
                AnimatedVisibility(
                    visible = uiState.errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    uiState.errorMessage?.let { errorText ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = errorText,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 6.dp),
                    color = BorderDivider,
                    thickness = 1.dp
                )

                // 3. Angka Utama Hasil Jumlah (Sangat Besar, Tepat di Bagian Bawah Layar)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End
                ) {
                    val rawResult = when {
                        uiState.liveResult.isNotEmpty() -> uiState.liveResult
                        uiState.secondOperand.isNotEmpty() -> uiState.secondOperand
                        uiState.firstOperand.isNotEmpty() -> uiState.firstOperand
                        else -> "0"
                    }

                    // Menambahkan prefix '=' jika ini adalah hasil kalkulasi liveResult
                    val displayResult = if (uiState.liveResult.isNotEmpty()) {
                        "= $rawResult"
                    } else {
                        rawResult
                    }

                    Text(
                        text = displayResult,
                        fontSize = if (displayResult.length > 14) 48.sp else if (displayResult.length > 10) 64.sp else 78.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PurplePrimary,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("result_display")
                    )
                }
            }

            // Keypad Section (Professional Polish Design with Mode Switch & Scientific Panel)
            Surface(
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = KeypadBackground,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("keypad_container")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Mode Switch Selector Bar (Standar vs Sains) matching keypad buttons design
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KeypadButton(
                            text = "🔢 Standar",
                            containerColor = if (!uiState.isScientificMode) PurplePrimary else UtilityKeyContainer,
                            textColor = if (!uiState.isScientificMode) Color.White else OnUtilityKeyContainer,
                            height = if (uiState.isScientificMode) 50.dp else 56.dp,
                            fontSize = 15.sp,
                            isBold = true,
                            elevation = if (!uiState.isScientificMode) 2.dp else 1.dp,
                            modifier = Modifier.weight(1.2f),
                            tag = "btn_mode_standard",
                            onClick = { if (uiState.isScientificMode) viewModel.toggleScientificMode() }
                        )

                        KeypadButton(
                            text = "🧪 Sains",
                            containerColor = if (uiState.isScientificMode) PurplePrimary else UtilityKeyContainer,
                            textColor = if (uiState.isScientificMode) Color.White else OnUtilityKeyContainer,
                            height = if (uiState.isScientificMode) 50.dp else 56.dp,
                            fontSize = 15.sp,
                            isBold = true,
                            elevation = if (uiState.isScientificMode) 2.dp else 1.dp,
                            modifier = Modifier.weight(1.2f),
                            tag = "btn_mode_scientific",
                            onClick = { if (!uiState.isScientificMode) viewModel.toggleScientificMode() }
                        )

                        KeypadButton(
                            text = "00",
                            containerColor = UtilityKeyContainer,
                            textColor = OnUtilityKeyContainer,
                            height = if (uiState.isScientificMode) 50.dp else 56.dp,
                            fontSize = 16.sp,
                            isBold = true,
                            elevation = 1.dp,
                            modifier = Modifier.weight(0.8f),
                            tag = "btn_double_zero_top",
                            onClick = { viewModel.onDoubleZeroClick() }
                        )
                    }

                    // Panel Mode Sains (Scientific Mode Functions Grid)
                    AnimatedVisibility(
                        visible = uiState.isScientificMode,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Sci Row 1: √, x², xʸ (^), π, e
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                KeypadButton(
                                    text = "√",
                                    containerColor = UtilityKeyContainer,
                                    textColor = OnUtilityKeyContainer,
                                    height = 48.dp,
                                    fontSize = 18.sp,
                                    modifier = Modifier.weight(1f),
                                    tag = "btn_sqrt",
                                    onClick = { viewModel.onSquareRootClick() }
                                )
                                KeypadButton(
                                    text = "x²",
                                    containerColor = UtilityKeyContainer,
                                    textColor = OnUtilityKeyContainer,
                                    height = 48.dp,
                                    fontSize = 16.sp,
                                    modifier = Modifier.weight(1f),
                                    tag = "btn_square",
                                    onClick = { viewModel.onSquareClick() }
                                )
                                KeypadButton(
                                    text = "xʸ",
                                    containerColor = UtilityKeyContainer,
                                    textColor = OnUtilityKeyContainer,
                                    height = 48.dp,
                                    fontSize = 16.sp,
                                    modifier = Modifier.weight(1f),
                                    tag = "btn_power",
                                    onClick = { viewModel.onOperatorClick("^") }
                                )
                                KeypadButton(
                                    text = "π",
                                    containerColor = UtilityKeyContainer,
                                    textColor = OnUtilityKeyContainer,
                                    height = 48.dp,
                                    fontSize = 18.sp,
                                    modifier = Modifier.weight(1f),
                                    tag = "btn_pi",
                                    onClick = { viewModel.onConstantClick("π") }
                                )
                                KeypadButton(
                                    text = "e",
                                    containerColor = UtilityKeyContainer,
                                    textColor = OnUtilityKeyContainer,
                                    height = 48.dp,
                                    fontSize = 18.sp,
                                    modifier = Modifier.weight(1f),
                                    tag = "btn_e",
                                    onClick = { viewModel.onConstantClick("e") }
                                )
                            }

                            // Sci Row 2: sin, cos, tan, log, ln
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                KeypadButton(
                                    text = "sin",
                                    containerColor = UtilityKeyContainer,
                                    textColor = OnUtilityKeyContainer,
                                    height = 48.dp,
                                    fontSize = 15.sp,
                                    modifier = Modifier.weight(1f),
                                    tag = "btn_sin",
                                    onClick = { viewModel.onTrigFunctionClick("sin") }
                                )
                                KeypadButton(
                                    text = "cos",
                                    containerColor = UtilityKeyContainer,
                                    textColor = OnUtilityKeyContainer,
                                    height = 48.dp,
                                    fontSize = 15.sp,
                                    modifier = Modifier.weight(1f),
                                    tag = "btn_cos",
                                    onClick = { viewModel.onTrigFunctionClick("cos") }
                                )
                                KeypadButton(
                                    text = "tan",
                                    containerColor = UtilityKeyContainer,
                                    textColor = OnUtilityKeyContainer,
                                    height = 48.dp,
                                    fontSize = 15.sp,
                                    modifier = Modifier.weight(1f),
                                    tag = "btn_tan",
                                    onClick = { viewModel.onTrigFunctionClick("tan") }
                                )
                                KeypadButton(
                                    text = "log",
                                    containerColor = UtilityKeyContainer,
                                    textColor = OnUtilityKeyContainer,
                                    height = 48.dp,
                                    fontSize = 15.sp,
                                    modifier = Modifier.weight(1f),
                                    tag = "btn_log",
                                    onClick = { viewModel.onLogFunctionClick("log") }
                                )
                                KeypadButton(
                                    text = "ln",
                                    containerColor = UtilityKeyContainer,
                                    textColor = OnUtilityKeyContainer,
                                    height = 48.dp,
                                    fontSize = 15.sp,
                                    modifier = Modifier.weight(1f),
                                    tag = "btn_ln",
                                    onClick = { viewModel.onLogFunctionClick("ln") }
                                )
                            }

                            // Sci Row 3: (, ), 1/x, 00, %
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                KeypadButton(
                                    text = "(",
                                    containerColor = UtilityKeyContainer,
                                    textColor = OnUtilityKeyContainer,
                                    height = 48.dp,
                                    fontSize = 18.sp,
                                    modifier = Modifier.weight(1f),
                                    tag = "btn_paren_open",
                                    onClick = { viewModel.onDigitClick("(") }
                                )
                                KeypadButton(
                                    text = ")",
                                    containerColor = UtilityKeyContainer,
                                    textColor = OnUtilityKeyContainer,
                                    height = 48.dp,
                                    fontSize = 18.sp,
                                    modifier = Modifier.weight(1f),
                                    tag = "btn_paren_close",
                                    onClick = { viewModel.onDigitClick(")") }
                                )
                                KeypadButton(
                                    text = "1/x",
                                    containerColor = UtilityKeyContainer,
                                    textColor = OnUtilityKeyContainer,
                                    height = 48.dp,
                                    fontSize = 15.sp,
                                    modifier = Modifier.weight(1f),
                                    tag = "btn_reciprocal",
                                    onClick = { viewModel.onReciprocalClick() }
                                )
                                KeypadButton(
                                    text = "00",
                                    containerColor = UtilityKeyContainer,
                                    textColor = OnUtilityKeyContainer,
                                    height = 48.dp,
                                    fontSize = 16.sp,
                                    modifier = Modifier.weight(1f),
                                    tag = "btn_double_zero",
                                    onClick = { viewModel.onDoubleZeroClick() }
                                )
                                KeypadButton(
                                    text = "%",
                                    containerColor = UtilityKeyContainer,
                                    textColor = OnUtilityKeyContainer,
                                    height = 48.dp,
                                    fontSize = 18.sp,
                                    modifier = Modifier.weight(1f),
                                    tag = "btn_percent_sci",
                                    onClick = { viewModel.onPercentClick() }
                                )
                            }
                        }
                    }

                    // Row 1: AC, +/-, %, ÷
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KeypadButton(
                            text = "AC",
                            containerColor = UtilityKeyContainer,
                            textColor = OnUtilityKeyContainer,
                            height = if (uiState.isScientificMode) 56.dp else 62.dp,
                            modifier = Modifier.weight(1f),
                            tag = "btn_clear",
                            onClick = { viewModel.onClearClick() }
                        )
                        KeypadButton(
                            text = "+/-",
                            containerColor = UtilityKeyContainer,
                            textColor = OnUtilityKeyContainer,
                            height = if (uiState.isScientificMode) 56.dp else 62.dp,
                            modifier = Modifier.weight(1f),
                            tag = "btn_toggle_sign",
                            onClick = { viewModel.onToggleSignClick() }
                        )
                        KeypadButton(
                            text = "%",
                            containerColor = UtilityKeyContainer,
                            textColor = OnUtilityKeyContainer,
                            height = if (uiState.isScientificMode) 56.dp else 62.dp,
                            modifier = Modifier.weight(1f),
                            tag = "btn_percent",
                            onClick = { viewModel.onPercentClick() }
                        )
                        KeypadButton(
                            text = "÷",
                            containerColor = OperatorKeyContainer,
                            textColor = OnPurplePrimaryContainer,
                            isBold = true,
                            height = if (uiState.isScientificMode) 56.dp else 62.dp,
                            modifier = Modifier.weight(1f),
                            tag = "btn_divide",
                            onClick = { viewModel.onOperatorClick("÷") }
                        )
                    }

                    // Row 2: 7, 8, 9, ×
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KeypadButton(
                            text = "7",
                            containerColor = Color.White,
                            textColor = TextDark,
                            height = if (uiState.isScientificMode) 56.dp else 62.dp,
                            modifier = Modifier.weight(1f),
                            tag = "btn_7",
                            onClick = { viewModel.onDigitClick("7") }
                        )
                        KeypadButton(
                            text = "8",
                            containerColor = Color.White,
                            textColor = TextDark,
                            height = if (uiState.isScientificMode) 56.dp else 62.dp,
                            modifier = Modifier.weight(1f),
                            tag = "btn_8",
                            onClick = { viewModel.onDigitClick("8") }
                        )
                        KeypadButton(
                            text = "9",
                            containerColor = Color.White,
                            textColor = TextDark,
                            height = if (uiState.isScientificMode) 56.dp else 62.dp,
                            modifier = Modifier.weight(1f),
                            tag = "btn_9",
                            onClick = { viewModel.onDigitClick("9") }
                        )
                        KeypadButton(
                            text = "×",
                            containerColor = OperatorKeyContainer,
                            textColor = OnPurplePrimaryContainer,
                            isBold = true,
                            height = if (uiState.isScientificMode) 56.dp else 62.dp,
                            modifier = Modifier.weight(1f),
                            tag = "btn_multiply",
                            onClick = { viewModel.onOperatorClick("×") }
                        )
                    }

                    // Row 3: 4, 5, 6, −
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KeypadButton(
                            text = "4",
                            containerColor = Color.White,
                            textColor = TextDark,
                            height = if (uiState.isScientificMode) 56.dp else 62.dp,
                            modifier = Modifier.weight(1f),
                            tag = "btn_4",
                            onClick = { viewModel.onDigitClick("4") }
                        )
                        KeypadButton(
                            text = "5",
                            containerColor = Color.White,
                            textColor = TextDark,
                            height = if (uiState.isScientificMode) 56.dp else 62.dp,
                            modifier = Modifier.weight(1f),
                            tag = "btn_5",
                            onClick = { viewModel.onDigitClick("5") }
                        )
                        KeypadButton(
                            text = "6",
                            containerColor = Color.White,
                            textColor = TextDark,
                            height = if (uiState.isScientificMode) 56.dp else 62.dp,
                            modifier = Modifier.weight(1f),
                            tag = "btn_6",
                            onClick = { viewModel.onDigitClick("6") }
                        )
                        KeypadButton(
                            text = "−",
                            containerColor = OperatorKeyContainer,
                            textColor = OnPurplePrimaryContainer,
                            isBold = true,
                            height = if (uiState.isScientificMode) 56.dp else 62.dp,
                            modifier = Modifier.weight(1f),
                            tag = "btn_subtract",
                            onClick = { viewModel.onOperatorClick("−") }
                        )
                    }

                    // Row 4: 1, 2, 3, +
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KeypadButton(
                            text = "1",
                            containerColor = Color.White,
                            textColor = TextDark,
                            height = if (uiState.isScientificMode) 56.dp else 62.dp,
                            modifier = Modifier.weight(1f),
                            tag = "btn_1",
                            onClick = { viewModel.onDigitClick("1") }
                        )
                        KeypadButton(
                            text = "2",
                            containerColor = Color.White,
                            textColor = TextDark,
                            height = if (uiState.isScientificMode) 56.dp else 62.dp,
                            modifier = Modifier.weight(1f),
                            tag = "btn_2",
                            onClick = { viewModel.onDigitClick("2") }
                        )
                        KeypadButton(
                            text = "3",
                            containerColor = Color.White,
                            textColor = TextDark,
                            height = if (uiState.isScientificMode) 56.dp else 62.dp,
                            modifier = Modifier.weight(1f),
                            tag = "btn_3",
                            onClick = { viewModel.onDigitClick("3") }
                        )
                        KeypadButton(
                            text = "+",
                            containerColor = OperatorKeyContainer,
                            textColor = OnPurplePrimaryContainer,
                            isBold = true,
                            height = if (uiState.isScientificMode) 56.dp else 62.dp,
                            modifier = Modifier.weight(1f),
                            tag = "btn_add",
                            onClick = { viewModel.onOperatorClick("+") }
                        )
                    }

                    // Row 5: 00, 0, ., ⌫, =
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KeypadButton(
                            text = "0",
                            containerColor = Color.White,
                            textColor = TextDark,
                            height = if (uiState.isScientificMode) 56.dp else 62.dp,
                            modifier = Modifier.weight(1f),
                            tag = "btn_0",
                            onClick = { viewModel.onDigitClick("0") }
                        )
                        KeypadButton(
                            text = ".",
                            containerColor = Color.White,
                            textColor = TextDark,
                            height = if (uiState.isScientificMode) 56.dp else 62.dp,
                            modifier = Modifier.weight(1f),
                            tag = "btn_decimal",
                            onClick = { viewModel.onDigitClick(".") }
                        )
                        KeypadIconButton(
                            containerColor = Color.White,
                            textColor = TextDark,
                            height = if (uiState.isScientificMode) 56.dp else 62.dp,
                            modifier = Modifier.weight(1f),
                            tag = "btn_backspace",
                            onClick = { viewModel.onBackspaceClick() }
                        )
                        KeypadButton(
                            text = "=",
                            containerColor = PurplePrimary,
                            textColor = Color.White,
                            isBold = true,
                            height = if (uiState.isScientificMode) 56.dp else 62.dp,
                            elevation = 4.dp,
                            modifier = Modifier.weight(1f),
                            tag = "btn_equals",
                            onClick = { viewModel.onEqualsClick() }
                        )
                    }
                }
            }
        }
    }

    // Fitur Kasir POS & Etalase Bottom Sheet
    if (showPosCashierSheet) {
        PosCashierBottomSheet(
            initialTab = posInitialTab,
            onDismiss = { showPosCashierSheet = false }
        )
    }

    // Full History Bottom Sheet
    if (showHistoryBottomSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ModalBottomSheet(
            onDismissRequest = { showHistoryBottomSheet = false },
            sheetState = sheetState,
            containerColor = SurfaceCanvas
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PurplePrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = OnPurplePrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Riwayat Perhitungan",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }

                    if (uiState.historyList.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                showClearConfirmDialog = true
                            }
                        ) {
                            Text("Hapus Semua", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderDivider)

                if (uiState.historyList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = BorderDivider,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Belum ada riwayat perhitungan",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.historyList, key = { it.id }) { item ->
                            HistoryDetailCard(
                                item = item,
                                onSelect = {
                                    viewModel.onHistoryItemSelect(item)
                                    showHistoryBottomSheet = false
                                },
                                onDelete = { viewModel.onDeleteHistoryItem(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog to Clear History
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Hapus Semua Riwayat") },
            text = { Text("Apakah Anda yakin ingin menghapus seluruh catatan riwayat kalkulasi?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onClearAllHistory()
                        showClearConfirmDialog = false
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Info Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Kalkulator Riwayat") },
            text = {
                Column {
                    Text("Aplikasi kalkulator pintar dengan fitur riwayat otomatis berbasis Room Database local persistence.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Dipoles dengan tema Professional Polish (Material Design 3).", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
fun HeaderBar(
    userName: String,
    onHistoryClick: () -> Unit,
    onPosCashierClick: () -> Unit,
    onEtalaseClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PurplePrimaryContainer)
                    .clickable { onHistoryClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Riwayat",
                    tint = OnPurplePrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {
                Text(
                    text = "Kalkulator Pintar",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = userName,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onPosCashierClick,
                modifier = Modifier.testTag("btn_header_pos")
            ) {
                Icon(
                    imageVector = Icons.Default.PointOfSale,
                    contentDescription = "Fitur Kasir POS",
                    tint = PurplePrimary
                )
            }
            IconButton(
                onClick = onEtalaseClick,
                modifier = Modifier.testTag("btn_header_etalase")
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = "Fitur Etalase Produk",
                    tint = PurplePrimary
                )
            }
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.testTag("btn_header_menu")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = TextDark
                )
            }
        }
    }
}

@Composable
fun HistoryPreviewRow(
    item: CalculationHistoryEntity,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 0.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = item.expression.replace("*", "×").replace("/", "÷"),
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            lineHeight = 15.sp
        )
        Text(
            text = "= ${item.result}",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun HistoryDetailCard(
    item: CalculationHistoryEntity,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val dateStr = remember(item.createdAt) { dateFormat.format(Date(item.createdAt)) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = KeypadBackground),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateStr,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = item.expression.replace("*", "×").replace("/", "÷"),
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "= ${item.result}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurplePrimary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus Item",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun rememberDebouncedClick(
    debounceTimeMs: Long = 50L,
    onClick: () -> Unit
): () -> Unit {
    var lastClickTime by remember { mutableLongStateOf(0L) }
    return {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= debounceTimeMs) {
            lastClickTime = currentTime
            onClick()
        }
    }
}

@Composable
fun KeypadButton(
    text: String,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    isBold: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 22.sp,
    height: androidx.compose.ui.unit.Dp = 62.dp,
    elevation: androidx.compose.ui.unit.Dp = 1.dp,
    tag: String,
    onClick: () -> Unit
) {
    val safeClick = rememberDebouncedClick(onClick = onClick)
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        shadowElevation = elevation,
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(18.dp))
            .clickable { safeClick() }
            .testTag(tag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = if (isBold && fontSize == 22.sp) 25.sp else fontSize,
                fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun KeypadIconButton(
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 62.dp,
    tag: String,
    onClick: () -> Unit
) {
    val safeClick = rememberDebouncedClick(onClick = onClick)
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        shadowElevation = 1.dp,
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(18.dp))
            .clickable { safeClick() }
            .testTag(tag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Backspace",
                tint = textColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun ActionSheetItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}
