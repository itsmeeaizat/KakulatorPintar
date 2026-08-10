package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.graphics.BitmapFactory
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.entity.ProductEntity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BorderDivider
import com.example.ui.theme.KeypadBackground
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurplePrimaryContainer
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextSubtle
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Model Data Keranjang Belanja
data class CartItem(
    val name: String,
    val price: Long,
    val stock: Int = 99,
    var qty: Int = 1
) {
    val subtotal: Long get() = price * qty
}

// Model Data Preset Produk Katalog
data class PresetProduct(
    val name: String,
    val price: Long,
    val category: String,
    val barcode: String
)

// Model Data Riwayat Transaksi Penjualan
data class TransactionRecord(
    val id: String,
    val timestamp: Long,
    val dateFormatted: String,
    val storeName: String,
    val items: List<CartItem>,
    val subtotal: Long,
    val discount: Long,
    val tax: Long,
    val serviceFee: Long,
    val totalPrice: Long,
    val cashPaid: Long,
    val change: Long,
    val paymentMethod: String
)

fun formatRupiah(amount: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount).replace("Rp", "Rp ").trim()
}

/**
 * Manager Repository untuk Menyimpan Riwayat Transaksi Penjualan ke Local Storage (SharedPreferences)
 */
object TransactionRepository {
    private const val PREFS_NAME = "pos_sales_database"
    private const val KEY_TRANSACTIONS = "transactions_json"

    fun saveTransaction(context: Context, record: TransactionRecord) {
        try {
            val list = getTransactions(context).toMutableList()
            list.add(0, record) // Tambah ke paling awal (terbaru)

            val jsonArray = JSONArray()
            list.forEach { tx ->
                val obj = JSONObject().apply {
                    put("id", tx.id)
                    put("timestamp", tx.timestamp)
                    put("dateFormatted", tx.dateFormatted)
                    put("storeName", tx.storeName)
                    put("subtotal", tx.subtotal)
                    put("discount", tx.discount)
                    put("tax", tx.tax)
                    put("serviceFee", tx.serviceFee)
                    put("totalPrice", tx.totalPrice)
                    put("cashPaid", tx.cashPaid)
                    put("change", tx.change)
                    put("paymentMethod", tx.paymentMethod)

                    val itemsArr = JSONArray()
                    tx.items.forEach { item ->
                        val itemObj = JSONObject().apply {
                            put("name", item.name)
                            put("price", item.price)
                            put("qty", item.qty)
                        }
                        itemsArr.put(itemObj)
                    }
                    put("items", itemsArr)
                }
                jsonArray.put(obj)
            }

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_TRANSACTIONS, jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getTransactions(context: Context): List<TransactionRecord> {
        val result = mutableListOf<TransactionRecord>()
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString(KEY_TRANSACTIONS, "[]") ?: "[]"
            val jsonArray = JSONArray(jsonStr)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val itemsArr = obj.getJSONArray("items")
                val itemsList = mutableListOf<CartItem>()

                for (j in 0 until itemsArr.length()) {
                    val itemObj = itemsArr.getJSONObject(j)
                    itemsList.add(
                        CartItem(
                            name = itemObj.getString("name"),
                            price = itemObj.getLong("price"),
                            qty = itemObj.getInt("qty")
                        )
                    )
                }

                result.add(
                    TransactionRecord(
                        id = obj.getString("id"),
                        timestamp = obj.getLong("timestamp"),
                        dateFormatted = obj.getString("dateFormatted"),
                        storeName = obj.optString("storeName", "TOKO KASIR"),
                        items = itemsList,
                        subtotal = obj.getLong("subtotal"),
                        discount = obj.optLong("discount", 0L),
                        tax = obj.optLong("tax", 0L),
                        serviceFee = obj.optLong("serviceFee", 0L),
                        totalPrice = obj.getLong("totalPrice"),
                        cashPaid = obj.getLong("cashPaid"),
                        change = obj.getLong("change"),
                        paymentMethod = obj.optString("paymentMethod", "Tunai (Cash)")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun deleteTransaction(context: Context, id: String) {
        val list = getTransactions(context).filter { it.id != id }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val jsonArray = JSONArray()
        list.forEach { tx ->
            val obj = JSONObject().apply {
                put("id", tx.id)
                put("timestamp", tx.timestamp)
                put("dateFormatted", tx.dateFormatted)
                put("storeName", tx.storeName)
                put("subtotal", tx.subtotal)
                put("discount", tx.discount)
                put("tax", tx.tax)
                put("serviceFee", tx.serviceFee)
                put("totalPrice", tx.totalPrice)
                put("cashPaid", tx.cashPaid)
                put("change", tx.change)
                put("paymentMethod", tx.paymentMethod)

                val itemsArr = JSONArray()
                tx.items.forEach { item ->
                    val itemObj = JSONObject().apply {
                        put("name", item.name)
                        put("price", item.price)
                        put("qty", item.qty)
                    }
                    itemsArr.put(itemObj)
                }
                put("items", itemsArr)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_TRANSACTIONS, jsonArray.toString()).apply()
    }
}

/**
 * Penyimpanan Lokal Data & Gambar QRIS Toko
 */
object QrisRepository {
    private const val PREFS_NAME = "qris_settings_prefs"
    private const val KEY_MERCHANT_NAME = "qris_merchant_name"
    private const val KEY_NMID = "qris_nmid"
    private const val KEY_STATIC_CODE = "qris_static_code"
    private const val KEY_IMAGE_PATH = "qris_image_path"

    fun saveQrisSettings(context: Context, merchantName: String, nmid: String, staticCode: String, imageUri: Uri?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var savedPath = prefs.getString(KEY_IMAGE_PATH, "") ?: ""

        if (imageUri != null) {
            try {
                val file = File(context.filesDir, "qris_merchant_image.png")
                context.contentResolver.openInputStream(imageUri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                savedPath = file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        prefs.edit()
            .putString(KEY_MERCHANT_NAME, merchantName)
            .putString(KEY_NMID, nmid)
            .putString(KEY_STATIC_CODE, staticCode)
            .putString(KEY_IMAGE_PATH, savedPath)
            .apply()
    }

    fun getQrisMerchantName(context: Context, defaultStore: String): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_MERCHANT_NAME, defaultStore) ?: defaultStore
    }

    fun getQrisNmid(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_NMID, "ID1020223456789") ?: "ID1020223456789"
    }

    fun getQrisStaticCode(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_STATIC_CODE, "00020101021126580014ID.GO.QRIS.WWW") ?: "00020101021126580014ID.GO.QRIS.WWW"
    }

    fun getQrisImagePath(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_IMAGE_PATH, "") ?: ""
    }
}

/**
 * Model Data & Penyimpanan Lokal untuk Akun & QR E-Wallet (DANA, OVO, GoPay, ShopeePay, LinkAja)
 */
data class EWalletConfig(
    val provider: String,
    val phone: String,
    val accountName: String,
    val qrImagePath: String
)

object EWalletRepository {
    private const val PREFS_NAME = "ewallet_settings_prefs"

    fun saveEWalletConfig(context: Context, provider: String, phone: String, accountName: String, imageUri: Uri?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val p = provider.lowercase()
        val keyImagePath = "ewallet_${p}_image_path"
        var savedPath = prefs.getString(keyImagePath, "") ?: ""

        if (imageUri != null) {
            try {
                val file = File(context.filesDir, "ewallet_${p}_qr.png")
                context.contentResolver.openInputStream(imageUri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                savedPath = file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        prefs.edit()
            .putString("ewallet_${p}_phone", phone)
            .putString("ewallet_${p}_name", accountName)
            .putString(keyImagePath, savedPath)
            .apply()
    }

    fun getEWalletConfig(context: Context, provider: String, defaultStore: String = ""): EWalletConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val p = provider.lowercase()
        val phone = prefs.getString("ewallet_${p}_phone", "") ?: ""
        val accountName = prefs.getString("ewallet_${p}_name", "") ?: ""
        val qrImagePath = prefs.getString("ewallet_${p}_image_path", "") ?: ""
        return EWalletConfig(provider, phone, accountName, qrImagePath)
    }

    fun isEWalletConfigured(context: Context, provider: String): Boolean {
        val config = getEWalletConfig(context, provider)
        return config.phone.isNotBlank() || (config.qrImagePath.isNotBlank() && File(config.qrImagePath).exists())
    }

    fun removeQrImage(context: Context, provider: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val p = provider.lowercase()
        val keyImagePath = "ewallet_${p}_image_path"
        val currentPath = prefs.getString(keyImagePath, "") ?: ""
        if (currentPath.isNotEmpty()) {
            val file = File(currentPath)
            if (file.exists()) file.delete()
        }
        prefs.edit().putString(keyImagePath, "").apply()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosCashierBottomSheet(
    initialTab: Int = 0,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var activeTab by remember { mutableStateOf(initialTab) } // 0: Transaksi Kasir, 1: Etalase, 2: Riwayat Penjualan

    var storeName by remember { mutableStateOf("TOKO KASIR SERBA ADA") }
    var productName by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var productQty by remember { mutableStateOf("1") }

    val etalaseViewModel: EtalaseViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val dbProducts: List<ProductEntity> by etalaseViewModel.allProducts.collectAsState()

    // Katalog Preset Produk Gabungan dengan Produk Baru dari Etalase Room DB
    val presetProducts = remember {
        listOf(
            PresetProduct("Indomie Goreng", 3500, "Makanan & Minuman", "8991001"),
            PresetProduct("Air Mineral 600ml", 4000, "Makanan & Minuman", "8991002"),
            PresetProduct("Teh Botol Sosro", 5000, "Makanan & Minuman", "8991003"),
            PresetProduct("Kopi Susu Saset", 2500, "Makanan & Minuman", "8991004"),
            PresetProduct("Minyak Goreng 1L", 18000, "Sembako", "8992001"),
            PresetProduct("Gula Pasir 1kg", 17500, "Sembako", "8992002"),
            PresetProduct("Beras Premium 5kg", 72000, "Sembako", "8992003"),
            PresetProduct("Telur Ayam 1kg", 28000, "Sembako", "8992004"),
            PresetProduct("Sabun Mandi", 4500, "Kebersihan", "8993001"),
            PresetProduct("Tisu Paseo", 12000, "Kebersihan", "8993002"),
            PresetProduct("Chitato Chips", 11500, "Snack & Kopi", "8994001"),
            PresetProduct("Biskuit Khong Guan", 45000, "Snack & Kopi", "8994002")
        )
    }

    val combinedPresetProducts = remember(presetProducts, dbProducts) {
        val dbPresets = dbProducts.map { item ->
            PresetProduct(
                name = item.name,
                price = item.price.toLong(),
                category = if (item.brand.isNotBlank() && item.brand != "Umum") item.brand else "Sembako",
                barcode = ""
            )
        }
        val existingNames = presetProducts.map { it.name.lowercase() }.toSet()
        val newDbPresets = dbPresets.filterNot { it.name.lowercase() in existingNames }
        presetProducts + newDbPresets
    }

    var selectedCategory by remember { mutableStateOf("Semua") }
    val categories = remember { listOf("Semua", "Sembako", "Makanan & Minuman", "Kebersihan", "Snack & Kopi") }

    val filteredPresets = remember(selectedCategory, combinedPresetProducts) {
        if (selectedCategory == "Semua") combinedPresetProducts
        else combinedPresetProducts.filter { it.category == selectedCategory }
    }

    val cartItems = remember { mutableStateListOf<CartItem>() }

    // Diskon & Pajak State
    var selectedDiscountMode by remember { mutableStateOf("PRESET") } // "PRESET", "CUSTOM_PERCENT", "CUSTOM_NOMINAL"
    var discountPercent by remember { mutableStateOf(0) } // 0, 5, 10, 15, 20 %
    var customDiscountPercentInput by remember { mutableStateOf("") }
    var customDiscountNominalInput by remember { mutableStateOf("") }

    var isTaxApplied by remember { mutableStateOf(false) } // PPN Status
    var isCustomTax by remember { mutableStateOf(false) } // False = 11% Standar, True = Custom %
    var customTaxPercentInput by remember { mutableStateOf("") }

    var serviceFeeInput by remember { mutableStateOf("") } // Biaya layanan

    // Metode Pembayaran
    var selectedPaymentMethod by remember { mutableStateOf("Tunai (Cash)") }
    val paymentMethods = remember { listOf("Tunai (Cash)", "QRIS", "Transfer Bank", "E-Wallet") }

    var cashPaidInput by remember { mutableStateOf("") }
    var showReceiptPreview by remember { mutableStateOf(false) }
    var showBarcodeScannerModal by remember { mutableStateOf(false) }
    var showUnknownBarcodeDialog by remember { mutableStateOf(false) }
    var unknownBarcodeCode by remember { mutableStateOf("") }
    var showRegisterProductFromScanDialog by remember { mutableStateOf(false) }
    var showQrisSetupModal by remember { mutableStateOf(false) }
    var showQrisPaymentModal by remember { mutableStateOf(false) }
    var showEWalletSetupModal by remember { mutableStateOf(false) }
    var showEWalletPaymentModal by remember { mutableStateOf(false) }
    var showEWalletNotConfiguredModal by remember { mutableStateOf(false) }
    var warningEWalletProvider by remember { mutableStateOf("DANA") }
    var selectedEWalletProvider by remember { mutableStateOf("DANA") }
    val eWalletProviders = remember { listOf("DANA", "OVO", "GoPay", "ShopeePay", "LinkAja") }

    // Kalkulasi Total Belanja, Diskon, Pajak
    val subtotalPrice = remember(cartItems.size, cartItems.sumOf { it.qty }) {
        cartItems.sumOf { it.subtotal }
    }

    val discountAmount = remember(
        subtotalPrice,
        discountPercent,
        selectedDiscountMode,
        customDiscountPercentInput,
        customDiscountNominalInput
    ) {
        when (selectedDiscountMode) {
            "CUSTOM_PERCENT" -> {
                val pct = customDiscountPercentInput.toDoubleOrNull() ?: 0.0
                (subtotalPrice * (pct / 100.0)).toLong()
            }
            "CUSTOM_NOMINAL" -> {
                val nom = customDiscountNominalInput.toLongOrNull() ?: 0L
                minOf(subtotalPrice, maxOf(0L, nom))
            }
            else -> {
                if (discountPercent > 0) (subtotalPrice * discountPercent / 100) else 0L
            }
        }
    }

    val taxableAmount = remember(subtotalPrice, discountAmount) {
        maxOf(0L, subtotalPrice - discountAmount)
    }

    val effectiveTaxPercent = remember(isTaxApplied, isCustomTax, customTaxPercentInput) {
        if (!isTaxApplied) 0.0
        else if (isCustomTax) (customTaxPercentInput.toDoubleOrNull() ?: 0.0)
        else 11.0
    }

    val taxAmount = remember(taxableAmount, isTaxApplied, effectiveTaxPercent) {
        if (isTaxApplied && effectiveTaxPercent > 0) {
            (taxableAmount * (effectiveTaxPercent / 100.0)).toLong()
        } else 0L
    }

    val serviceFee = remember(serviceFeeInput) {
        serviceFeeInput.toLongOrNull() ?: 0L
    }

    val finalTotalPrice = remember(taxableAmount, taxAmount, serviceFee) {
        taxableAmount + taxAmount + serviceFee
    }

    // Auto-fill cash paid for Non-cash methods
    LaunchedEffect(selectedPaymentMethod, finalTotalPrice) {
        if (selectedPaymentMethod != "Tunai (Cash)" && finalTotalPrice > 0) {
            cashPaidInput = finalTotalPrice.toString()
        }
    }

    val cashPaid = cashPaidInput.toLongOrNull() ?: 0L
    val change = if (cashPaid >= finalTotalPrice) cashPaid - finalTotalPrice else 0L

    // State Riwayat Transaksi
    var transactionsList by remember { mutableStateOf(emptyList<TransactionRecord>()) }
    var selectedHistoryTransaction by remember { mutableStateOf<TransactionRecord?>(null) }

    LaunchedEffect(activeTab) {
        if (activeTab == 1) {
            transactionsList = TransactionRepository.getTransactions(context)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = SurfaceCanvas,
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PurplePrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = "Kasir POS",
                            tint = PurplePrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Aplikasi Kasir & Struk POS",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Lengkap dengan Diskon, PPN & Riwayat Penjualan",
                            fontSize = 11.sp,
                            color = TextSubtle
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Tutup",
                        tint = TextDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Tabs (Kasir vs Etalase vs Riwayat)
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.Transparent,
                contentColor = PurplePrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = PurplePrimary
                    )
                }
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kasir", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Etalase", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = {
                        activeTab = 2
                        transactionsList = TransactionRepository.getTransactions(context)
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Riwayat", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp), color = BorderDivider)

            // CONTENT TAB 0: TRANSAKSI KASIR
            if (activeTab == 0) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Section Katalog & Filter Kategori + Barcode Scanner Trigger
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Katalog Produk Cepat",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PurplePrimary
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Tombol Setup QRIS Toko
                                    Surface(
                                        onClick = { showQrisSetupModal = true },
                                        shape = RoundedCornerShape(10.dp),
                                        color = PurplePrimaryContainer,
                                        border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.QrCode,
                                                contentDescription = "Setup QRIS",
                                                tint = PurplePrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "QRIS",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PurplePrimary
                                            )
                                        }
                                    }

                                    // Tombol Setup E-Wallet
                                    Surface(
                                        onClick = { showEWalletSetupModal = true },
                                        shape = RoundedCornerShape(10.dp),
                                        color = PurplePrimaryContainer,
                                        border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PhoneAndroid,
                                                contentDescription = "Setup E-Wallet",
                                                tint = PurplePrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "E-Wallet",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PurplePrimary
                                            )
                                        }
                                    }

                                    // Tombol Scanner Barcode Kamera
                                    Surface(
                                        onClick = { showBarcodeScannerModal = true },
                                        shape = RoundedCornerShape(10.dp),
                                        color = PurplePrimaryContainer,
                                        border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.QrCodeScanner,
                                                contentDescription = "Scan Barcode",
                                                tint = PurplePrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Scan",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PurplePrimary
                                            )
                                        }
                                    }
                                }
                            }

                            // Filter Category Chips
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(categories) { cat ->
                                    FilterChip(
                                        selected = selectedCategory == cat,
                                        onClick = { selectedCategory = cat },
                                        label = { Text(cat, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PurplePrimary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            // Catalog Items Horisontal
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(filteredPresets) { preset ->
                                    Surface(
                                        onClick = {
                                            val existingIndex = cartItems.indexOfFirst { it.name.equals(preset.name, ignoreCase = true) }
                                            if (existingIndex >= 0) {
                                                val current = cartItems[existingIndex]
                                                cartItems[existingIndex] = current.copy(qty = current.qty + 1)
                                            } else {
                                                cartItems.add(CartItem(name = preset.name, price = preset.price, qty = 1))
                                            }
                                            Toast.makeText(context, "+1 ${preset.name} ditambahkan", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, BorderDivider),
                                        shadowElevation = 1.dp
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(PurplePrimaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    tint = PurplePrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = preset.name,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextDark
                                                )
                                                Text(
                                                    text = formatRupiah(preset.price),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = PurplePrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section 1: Input Form Produk Custom
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, BorderDivider),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Tambah Item Custom Manual",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PurplePrimary
                                )

                                OutlinedTextField(
                                    value = productName,
                                    onValueChange = { productName = it },
                                    label = { Text("Nama Produk / Barang") },
                                    placeholder = { Text("Contoh: Sabun / Kopi Saset") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextDark,
                                        unfocusedTextColor = TextDark,
                                        focusedBorderColor = PurplePrimary,
                                        unfocusedBorderColor = BorderDivider,
                                        focusedLabelColor = PurplePrimary,
                                        unfocusedLabelColor = TextSubtle,
                                        cursorColor = PurplePrimary
                                    )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = productPrice,
                                        onValueChange = { productPrice = it.filter { char -> char.isDigit() } },
                                        label = { Text("Harga (Rp)") },
                                        placeholder = { Text("5000") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextDark,
                                            unfocusedTextColor = TextDark,
                                            focusedBorderColor = PurplePrimary,
                                            unfocusedBorderColor = BorderDivider,
                                            focusedLabelColor = PurplePrimary,
                                            unfocusedLabelColor = TextSubtle,
                                            cursorColor = PurplePrimary
                                        )
                                    )

                                    OutlinedTextField(
                                        value = productQty,
                                        onValueChange = { productQty = it.filter { char -> char.isDigit() } },
                                        label = { Text("Jumlah") },
                                        placeholder = { Text("1") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.width(90.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextDark,
                                            unfocusedTextColor = TextDark,
                                            focusedBorderColor = PurplePrimary,
                                            unfocusedBorderColor = BorderDivider,
                                            focusedLabelColor = PurplePrimary,
                                            unfocusedLabelColor = TextSubtle,
                                            cursorColor = PurplePrimary
                                        )
                                    )
                                }

                                Button(
                                    onClick = {
                                        val name = productName.trim()
                                        val price = productPrice.toLongOrNull() ?: 0L
                                        val qty = productQty.toIntOrNull() ?: 1

                                        if (name.isNotEmpty() && price > 0) {
                                            val existingIndex = cartItems.indexOfFirst { it.name.equals(name, ignoreCase = true) }
                                            if (existingIndex >= 0) {
                                                val current = cartItems[existingIndex]
                                                cartItems[existingIndex] = current.copy(qty = current.qty + qty)
                                            } else {
                                                cartItems.add(CartItem(name = name, price = price, qty = qty))
                                            }
                                            productName = ""
                                            productPrice = ""
                                            productQty = "1"
                                            Toast.makeText(context, "Item ditambahkan", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Isi nama dan harga produk valid", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Tambah ke Keranjang", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Section 2: Daftar Item Keranjang Belanja
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, BorderDivider),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Daftar Keranjang (${cartItems.size} Jenis)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PurplePrimary
                                    )

                                    if (cartItems.isNotEmpty()) {
                                        Surface(
                                            onClick = { cartItems.clear() },
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.errorContainer
                                        ) {
                                            Text(
                                                text = "Kosongkan",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                if (cartItems.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Keranjang belanja masih kosong.\nPilih dari katalog atau tambah item manual di atas.",
                                            fontSize = 13.sp,
                                            color = TextSubtle,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    cartItems.forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = item.name,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextDark
                                                )
                                                Text(
                                                    text = "${formatRupiah(item.price)} x ${item.qty} = ${formatRupiah(item.subtotal)}",
                                                    fontSize = 12.sp,
                                                    color = PurplePrimary,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }

                                            // Stepper Quantity & Delete
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = {
                                                        val index = cartItems.indexOf(item)
                                                        if (index >= 0) {
                                                            if (item.qty > 1) {
                                                                cartItems[index] = item.copy(qty = item.qty - 1)
                                                            } else {
                                                                cartItems.removeAt(index)
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Kurang", tint = TextDark)
                                                }

                                                Text(
                                                    text = "${item.qty}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    modifier = Modifier.padding(horizontal = 8.dp)
                                                )

                                                IconButton(
                                                    onClick = {
                                                        val index = cartItems.indexOf(item)
                                                        if (index >= 0) {
                                                            cartItems[index] = item.copy(qty = item.qty + 1)
                                                        }
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah", tint = PurplePrimary)
                                                }

                                                IconButton(
                                                    onClick = { cartItems.remove(item) },
                                                    modifier = Modifier.padding(start = 4.dp).size(32.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                        HorizontalDivider(color = BorderDivider.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }

                    // Section 3: Diskon, Pajak (PPN) & Metode Pembayaran
                    if (cartItems.isNotEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, BorderDivider),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Diskon, Pajak (PPN) & Biaya",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PurplePrimary
                                    )

                                    // Section Diskon
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Pilih Diskon Transaksi:", fontSize = 12.sp, color = TextSubtle)
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            val discountOpts = listOf(0, 5, 10, 15, 20)
                                            discountOpts.forEach { percent ->
                                                val isSelected = (selectedDiscountMode == "PRESET" && discountPercent == percent)
                                                Surface(
                                                    onClick = {
                                                        selectedDiscountMode = "PRESET"
                                                        discountPercent = percent
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isSelected) PurplePrimary else Color.White,
                                                    border = BorderStroke(1.dp, if (isSelected) PurplePrimary else BorderDivider),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text(
                                                        text = if (percent == 0) "Tanpa" else "$percent%",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) Color.White else TextDark,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.padding(vertical = 6.dp)
                                                    )
                                                }
                                            }

                                            // Tombol Custom Diskon
                                            val isCustomSelected = (selectedDiscountMode == "CUSTOM_PERCENT" || selectedDiscountMode == "CUSTOM_NOMINAL")
                                            Surface(
                                                onClick = {
                                                    if (!isCustomSelected) {
                                                        selectedDiscountMode = "CUSTOM_PERCENT"
                                                    }
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isCustomSelected) PurplePrimary else Color.White,
                                                border = BorderStroke(1.dp, if (isCustomSelected) PurplePrimary else BorderDivider),
                                                modifier = Modifier.weight(1.2f)
                                            ) {
                                                Text(
                                                    text = "Custom",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isCustomSelected) Color.White else TextDark,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(vertical = 6.dp)
                                                )
                                            }
                                        }

                                        // Form Input Custom Diskon jika mode Custom aktif
                                        if (selectedDiscountMode == "CUSTOM_PERCENT" || selectedDiscountMode == "CUSTOM_NOMINAL") {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 2.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    onClick = { selectedDiscountMode = "CUSTOM_PERCENT" },
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (selectedDiscountMode == "CUSTOM_PERCENT") PurplePrimaryContainer else Color(0xFFF1F5F9),
                                                    border = BorderStroke(1.dp, if (selectedDiscountMode == "CUSTOM_PERCENT") PurplePrimary else BorderDivider)
                                                ) {
                                                    Text(
                                                        text = "% Persen",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (selectedDiscountMode == "CUSTOM_PERCENT") PurplePrimary else TextSubtle,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                                    )
                                                }

                                                Surface(
                                                    onClick = { selectedDiscountMode = "CUSTOM_NOMINAL" },
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (selectedDiscountMode == "CUSTOM_NOMINAL") PurplePrimaryContainer else Color(0xFFF1F5F9),
                                                    border = BorderStroke(1.dp, if (selectedDiscountMode == "CUSTOM_NOMINAL") PurplePrimary else BorderDivider)
                                                ) {
                                                    Text(
                                                        text = "Rp Nominal",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (selectedDiscountMode == "CUSTOM_NOMINAL") PurplePrimary else TextSubtle,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                                    )
                                                }

                                                if (selectedDiscountMode == "CUSTOM_PERCENT") {
                                                    OutlinedTextField(
                                                        value = customDiscountPercentInput,
                                                        onValueChange = { input ->
                                                            val filtered = input.filter { char -> char.isDigit() || char == '.' }
                                                            val valDouble = filtered.toDoubleOrNull() ?: 0.0
                                                            if (valDouble <= 100) {
                                                                customDiscountPercentInput = filtered
                                                            }
                                                        },
                                                        label = { Text("Diskon (%)") },
                                                        placeholder = { Text("cth: 7.5") },
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(10.dp),
                                                        modifier = Modifier.weight(1f),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedTextColor = TextDark,
                                                            unfocusedTextColor = TextDark,
                                                            focusedBorderColor = PurplePrimary,
                                                            unfocusedBorderColor = BorderDivider,
                                                            focusedLabelColor = PurplePrimary,
                                                            unfocusedLabelColor = TextSubtle,
                                                            cursorColor = PurplePrimary
                                                        )
                                                    )
                                                } else {
                                                    OutlinedTextField(
                                                        value = customDiscountNominalInput,
                                                        onValueChange = { customDiscountNominalInput = it.filter { char -> char.isDigit() } },
                                                        label = { Text("Diskon (Rp)") },
                                                        placeholder = { Text("0") },
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(10.dp),
                                                        modifier = Modifier.weight(1f),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedTextColor = TextDark,
                                                            unfocusedTextColor = TextDark,
                                                            focusedBorderColor = PurplePrimary,
                                                            unfocusedBorderColor = BorderDivider,
                                                            focusedLabelColor = PurplePrimary,
                                                            unfocusedLabelColor = TextSubtle,
                                                            cursorColor = PurplePrimary
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = BorderDivider)

                                    // Section Pajak PPN & Biaya Layanan
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Pajak (PPN) & Biaya Layanan:", fontSize = 12.sp, color = TextSubtle)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Toggle PPN
                                            Surface(
                                                onClick = { isTaxApplied = !isTaxApplied },
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isTaxApplied) PurplePrimaryContainer else Color.White,
                                                border = BorderStroke(1.dp, if (isTaxApplied) PurplePrimary else BorderDivider),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = if (isTaxApplied) Icons.Default.CheckCircle else Icons.Default.Percent,
                                                        contentDescription = null,
                                                        tint = PurplePrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = if (isTaxApplied) {
                                                            if (isCustomTax) "PPN (${if (customTaxPercentInput.isBlank()) "0" else customTaxPercentInput}%)" else "PPN 11%"
                                                        } else "Tanpa PPN",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextDark
                                                    )
                                                }
                                            }

                                            // Biaya Layanan Input
                                            OutlinedTextField(
                                                value = serviceFeeInput,
                                                onValueChange = { serviceFeeInput = it.filter { char -> char.isDigit() } },
                                                label = { Text("Biaya Layanan (Rp)") },
                                                placeholder = { Text("0") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = TextDark,
                                                    unfocusedTextColor = TextDark,
                                                    focusedBorderColor = PurplePrimary,
                                                    unfocusedBorderColor = BorderDivider,
                                                    focusedLabelColor = PurplePrimary,
                                                    unfocusedLabelColor = TextSubtle,
                                                    cursorColor = PurplePrimary
                                                )
                                            )
                                        }

                                        // Jika PPN aktif, tampilkan pilihan tarif: Standar 11% atau Custom PPN
                                        if (isTaxApplied) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 2.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    onClick = { isCustomTax = false },
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (!isCustomTax) PurplePrimaryContainer else Color(0xFFF1F5F9),
                                                    border = BorderStroke(1.dp, if (!isCustomTax) PurplePrimary else BorderDivider)
                                                ) {
                                                    Text(
                                                        text = "Standar 11%",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (!isCustomTax) PurplePrimary else TextSubtle,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                                    )
                                                }

                                                Surface(
                                                    onClick = { isCustomTax = true },
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isCustomTax) PurplePrimaryContainer else Color(0xFFF1F5F9),
                                                    border = BorderStroke(1.dp, if (isCustomTax) PurplePrimary else BorderDivider)
                                                ) {
                                                    Text(
                                                        text = "Custom PPN %",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isCustomTax) PurplePrimary else TextSubtle,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                                    )
                                                }

                                                if (isCustomTax) {
                                                    OutlinedTextField(
                                                        value = customTaxPercentInput,
                                                        onValueChange = { input ->
                                                            val filtered = input.filter { char -> char.isDigit() || char == '.' }
                                                            val valDouble = filtered.toDoubleOrNull() ?: 0.0
                                                            if (valDouble <= 100) {
                                                                customTaxPercentInput = filtered
                                                            }
                                                        },
                                                        label = { Text("Tarif PPN (%)") },
                                                        placeholder = { Text("cth: 10") },
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(10.dp),
                                                        modifier = Modifier.weight(1f),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedTextColor = TextDark,
                                                            unfocusedTextColor = TextDark,
                                                            focusedBorderColor = PurplePrimary,
                                                            unfocusedBorderColor = BorderDivider,
                                                            focusedLabelColor = PurplePrimary,
                                                            unfocusedLabelColor = TextSubtle,
                                                            cursorColor = PurplePrimary
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = BorderDivider)

                                    // Pilihan Metode Pembayaran
                                    Text("Metode Pembayaran:", fontSize = 12.sp, color = TextSubtle)
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(paymentMethods) { method ->
                                            Surface(
                                                onClick = { selectedPaymentMethod = method },
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (selectedPaymentMethod == method) PurplePrimary else Color.White,
                                                border = BorderStroke(1.dp, if (selectedPaymentMethod == method) PurplePrimary else BorderDivider)
                                            ) {
                                                Text(
                                                    text = method,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (selectedPaymentMethod == method) Color.White else TextDark,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (selectedPaymentMethod == "E-Wallet") {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Pilih Aplikasi E-Wallet:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                                            Surface(
                                                onClick = { showEWalletSetupModal = true },
                                                shape = RoundedCornerShape(6.dp),
                                                color = PurplePrimaryContainer
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("Setup E-Wallet", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PurplePrimary)
                                                }
                                            }
                                        }
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(eWalletProviders) { provider ->
                                                val isSelected = (selectedEWalletProvider == provider)
                                                Surface(
                                                    onClick = { selectedEWalletProvider = provider },
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isSelected) PurplePrimaryContainer else Color.White,
                                                    border = BorderStroke(1.dp, if (isSelected) PurplePrimary else BorderDivider)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.PhoneAndroid,
                                                            contentDescription = null,
                                                            tint = if (isSelected) PurplePrimary else TextSubtle,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = provider,
                                                            fontSize = 11.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            color = if (isSelected) PurplePrimary else TextDark
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section 4: Ringkasan Total Pembayaran & Kasir Uang
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = PurplePrimaryContainer.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Subtotal Belanja:", fontSize = 13.sp, color = TextDark)
                                        Text(formatRupiah(subtotalPrice), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                                    }

                                    if (discountAmount > 0) {
                                        val discountLabel = when (selectedDiscountMode) {
                                            "CUSTOM_PERCENT" -> "Diskon (${customDiscountPercentInput.ifEmpty { "0" }}%):"
                                            "CUSTOM_NOMINAL" -> "Diskon Custom (Rp):"
                                            else -> "Diskon ($discountPercent%):"
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(discountLabel, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                                            Text("- ${formatRupiah(discountAmount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                        }
                                    }

                                    if (taxAmount > 0) {
                                        val taxLabel = if (isCustomTax) {
                                            "Pajak (PPN ${customTaxPercentInput.ifEmpty { "0" }}%):"
                                        } else {
                                            "Pajak (PPN 11%):"
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(taxLabel, fontSize = 13.sp, color = TextSubtle)
                                            Text("+ ${formatRupiah(taxAmount)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                                        }
                                    }

                                    if (serviceFee > 0) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Biaya Layanan:", fontSize = 13.sp, color = TextSubtle)
                                            Text("+ ${formatRupiah(serviceFee)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                                        }
                                    }

                                    HorizontalDivider(color = BorderDivider)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("TOTAL AKHIR:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                        Text(
                                            text = formatRupiah(finalTotalPrice),
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = PurplePrimary
                                        )
                                    }

                                    HorizontalDivider(color = BorderDivider)

                                    // Input Uang Bayar (Hanya aktif untuk Tunai/Cash)
                                    if (selectedPaymentMethod == "Tunai (Cash)") {
                                        OutlinedTextField(
                                            value = cashPaidInput,
                                            onValueChange = { cashPaidInput = it.filter { char -> char.isDigit() } },
                                            label = { Text("Uang Dibayarkan Pembeli (Rp)") },
                                            placeholder = { Text("Contoh: 50000") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = TextDark,
                                                unfocusedTextColor = TextDark,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White,
                                                focusedBorderColor = PurplePrimary,
                                                unfocusedBorderColor = BorderDivider,
                                                focusedLabelColor = PurplePrimary,
                                                unfocusedLabelColor = TextSubtle,
                                                cursorColor = PurplePrimary
                                            )
                                        )

                                        // Tombol Cepat Nominal Uang (Kasir Alfamart Style)
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            item {
                                                Surface(
                                                    onClick = { cashPaidInput = finalTotalPrice.toString() },
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = PurplePrimaryContainer,
                                                    border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.3f))
                                                ) {
                                                    Text(
                                                        text = "Uang Pas",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = PurplePrimary,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }
                                            val quickAmounts = listOf(10000L, 20000L, 50000L, 100000L, 200000L)
                                            items(quickAmounts) { amount ->
                                                Surface(
                                                    onClick = { cashPaidInput = amount.toString() },
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color.White,
                                                    border = BorderStroke(1.dp, BorderDivider)
                                                ) {
                                                    Text(
                                                        text = formatRupiah(amount),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TextDark,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }
                                        }

                                        if (cashPaid > 0) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Kembalian:", fontSize = 15.sp, color = TextDark)
                                                Text(
                                                    text = formatRupiah(change),
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (cashPaid >= finalTotalPrice) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    } else {
                                        // Non-Cash Payment Tag
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFE8F5E9),
                                            border = BorderStroke(1.dp, Color(0xFFA5D6A7)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Pembayaran via $selectedPaymentMethod (Non-Tunai). Bebas kembalian.",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF1B5E20),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tombol Buka Pratinjau Struk & Selesaikan Transaksi
                Button(
                    onClick = {
                        if (cartItems.isEmpty()) {
                            Toast.makeText(context, "Keranjang belanja kosong", Toast.LENGTH_SHORT).show()
                        } else if (selectedPaymentMethod == "Tunai (Cash)" && cashPaid < finalTotalPrice) {
                            Toast.makeText(context, "Uang pembayaran kurang dari total belanja", Toast.LENGTH_SHORT).show()
                        } else if (selectedPaymentMethod == "QRIS") {
                            showQrisPaymentModal = true
                        } else if (selectedPaymentMethod == "E-Wallet") {
                            val isConfigured = EWalletRepository.isEWalletConfigured(context, selectedEWalletProvider)
                            if (isConfigured) {
                                showEWalletPaymentModal = true
                            } else {
                                warningEWalletProvider = selectedEWalletProvider
                                showEWalletNotConfiguredModal = true
                            }
                        } else {
                            showReceiptPreview = true
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(
                        imageVector = when (selectedPaymentMethod) {
                            "QRIS" -> Icons.Default.QrCode
                            "E-Wallet" -> Icons.Default.PhoneAndroid
                            else -> Icons.Outlined.ReceiptLong
                        },
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (selectedPaymentMethod) {
                            "QRIS" -> "Bayar via QRIS (${formatRupiah(finalTotalPrice)})"
                            "E-Wallet" -> "Bayar via E-Wallet ($selectedEWalletProvider) (${formatRupiah(finalTotalPrice)})"
                            else -> "Pratinjau & Cetak Struk (${formatRupiah(finalTotalPrice)})"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // CONTENT TAB 1: ETALASE KATEGORI BERLAPIS
            if (activeTab == 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    EtalaseView(
                        onAddToCart = { product ->
                            val existingIndex = cartItems.indexOfFirst { it.name.equals(product.name, ignoreCase = true) }
                            if (existingIndex >= 0) {
                                val existing = cartItems[existingIndex]
                                cartItems[existingIndex] = existing.copy(qty = existing.qty + 1)
                            } else {
                                cartItems.add(CartItem(name = product.name, price = product.price.toLong(), qty = 1))
                            }
                        },
                        cartItemsCount = cartItems.sumOf { it.qty },
                        cartSubtotal = cartItems.sumOf { it.subtotal },
                        onOpenCart = { activeTab = 0 }
                    )
                }
            }

            // CONTENT TAB 2: RIWAYAT PENJUALAN
            if (activeTab == 2) {
                val totalOmset = remember(transactionsList) { transactionsList.sumOf { it.totalPrice } }
                val totalCount = remember(transactionsList) { transactionsList.size }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Summary Revenue Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = PurplePrimaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total Omset Penjualan", fontSize = 12.sp, color = TextSubtle)
                                Text(
                                    text = formatRupiah(totalOmset),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PurplePrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Jumlah Transaksi", fontSize = 12.sp, color = TextSubtle)
                                Text(
                                    text = "$totalCount Struk",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                            }
                        }
                    }

                    if (transactionsList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Belum ada riwayat transaksi penjualan.\nSelesaikan transaksi di tab Kasir untuk mencatat penjualan.",
                                fontSize = 13.sp,
                                color = TextSubtle,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(transactionsList) { tx ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, BorderDivider),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = tx.id,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PurplePrimary
                                            )
                                            Text(
                                                text = tx.dateFormatted,
                                                fontSize = 11.sp,
                                                color = TextSubtle
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "${tx.items.size} jenis barang (${tx.items.sumOf { it.qty }} pcs) • via ${tx.paymentMethod}",
                                            fontSize = 12.sp,
                                            color = TextDark
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = formatRupiah(tx.totalPrice),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = PurplePrimary
                                            )

                                            Row {
                                                Surface(
                                                    onClick = { selectedHistoryTransaction = tx },
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = PurplePrimaryContainer
                                                ) {
                                                    Text(
                                                        text = "Lihat Struk",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = PurplePrimary,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(6.dp))

                                                IconButton(
                                                    onClick = {
                                                        TransactionRepository.deleteTransaction(context, tx.id)
                                                        transactionsList = TransactionRepository.getTransactions(context)
                                                        Toast.makeText(context, "Transaksi dihapus", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
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
        }
    }

    // Modal Scanner Barcode Simulator Kamera HP
    if (showBarcodeScannerModal) {
        BarcodeScannerDialog(
            presetProducts = combinedPresetProducts,
            onBarcodeScanned = { matchedProduct ->
                val existingIndex = cartItems.indexOfFirst { it.name.equals(matchedProduct.name, ignoreCase = true) }
                if (existingIndex >= 0) {
                    val current = cartItems[existingIndex]
                    cartItems[existingIndex] = current.copy(qty = current.qty + 1)
                } else {
                    cartItems.add(CartItem(name = matchedProduct.name, price = matchedProduct.price, qty = 1))
                }
                Toast.makeText(context, "✅ Barcode Scanned: +1 ${matchedProduct.name}", Toast.LENGTH_SHORT).show()
            },
            onBarcodeNotFound = { unkCode ->
                showBarcodeScannerModal = false
                unknownBarcodeCode = unkCode
                showUnknownBarcodeDialog = true
            },
            onDismiss = { showBarcodeScannerModal = false }
        )
    }

    // Modal Alert Barcode Tidak Dikenali
    if (showUnknownBarcodeDialog) {
        UnknownBarcodeAlertDialog(
            barcodeCode = unknownBarcodeCode,
            onDismiss = { showUnknownBarcodeDialog = false },
            onRegisterNow = {
                showUnknownBarcodeDialog = false
                showRegisterProductFromScanDialog = true
            }
        )
    }

    // Modal Daftarkan Produk Baru Dari Kasir Scan
    if (showRegisterProductFromScanDialog) {
        val etalaseVm: EtalaseViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
        val allCategoriesState by etalaseVm.allCategories.collectAsState()
        AddProductDialog(
            allCategories = allCategoriesState,
            defaultCategoryId = allCategoriesState.firstOrNull()?.id ?: 0,
            initialBarcode = unknownBarcodeCode,
            onDismiss = { showRegisterProductFromScanDialog = false },
            onSave = { name, brand, price, stock, categoryId, barcode ->
                etalaseVm.addProduct(name, brand, price, stock, categoryId, barcode)
                showRegisterProductFromScanDialog = false
                // Auto-add newly registered product to Cashier Cart directly!
                val newPriceLong = price.toLong()
                val existingIndex = cartItems.indexOfFirst { it.name.equals(name, ignoreCase = true) }
                if (existingIndex >= 0) {
                    val current = cartItems[existingIndex]
                    cartItems[existingIndex] = current.copy(qty = current.qty + 1)
                } else {
                    cartItems.add(CartItem(name = name, price = newPriceLong, qty = 1))
                }
                Toast.makeText(context, "🎉 Produk '$name' berhasil terdaftar & masuk keranjang!", Toast.LENGTH_LONG).show()
            },
            onAddNewCategory = { name, parentId ->
                etalaseVm.addCategory(name, parentId)
                Toast.makeText(context, "✅ Kategori '$name' berhasil dibuat", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal Setup QRIS Toko
    if (showQrisSetupModal) {
        QrisSetupDialog(
            currentStoreName = storeName,
            onDismiss = { showQrisSetupModal = false }
        )
    }

    // Modal Popup Payment QRIS
    if (showQrisPaymentModal) {
        QrisPaymentDialog(
            totalPrice = finalTotalPrice,
            onOpenSetup = {
                showQrisPaymentModal = false
                showQrisSetupModal = true
            },
            onPaymentConfirmed = {
                showQrisPaymentModal = false
                showReceiptPreview = true
            },
            onDismiss = { showQrisPaymentModal = false }
        )
    }

    // Modal Warning Setup E-Wallet Belum Di-Setup
    if (showEWalletNotConfiguredModal) {
        EWalletNotConfiguredDialog(
            provider = warningEWalletProvider,
            onOpenSetup = {
                selectedEWalletProvider = warningEWalletProvider
                showEWalletNotConfiguredModal = false
                showEWalletSetupModal = true
            },
            onDismiss = { showEWalletNotConfiguredModal = false }
        )
    }

    // Modal Setup E-Wallet
    if (showEWalletSetupModal) {
        EWalletSetupDialog(
            currentStoreName = storeName,
            initialProvider = selectedEWalletProvider,
            onDismiss = { showEWalletSetupModal = false }
        )
    }

    // Modal Popup Payment E-Wallet
    if (showEWalletPaymentModal) {
        EWalletPaymentDialog(
            initialProvider = selectedEWalletProvider,
            totalPrice = finalTotalPrice,
            onOpenSetup = { provider ->
                selectedEWalletProvider = provider
                showEWalletPaymentModal = false
                showEWalletSetupModal = true
            },
            onPaymentConfirmed = { provider ->
                selectedEWalletProvider = provider
                showEWalletPaymentModal = false
                showReceiptPreview = true
            },
            onDismiss = { showEWalletPaymentModal = false }
        )
    }

    // Modal Pratinjau Struk Thermal & Print (Untuk Transaksi Baru)
    if (showReceiptPreview) {
        ReceiptThermalModal(
            storeName = storeName,
            cartItems = cartItems,
            subtotal = subtotalPrice,
            discount = discountAmount,
            tax = taxAmount,
            serviceFee = serviceFee,
            totalPrice = finalTotalPrice,
            cashPaid = cashPaid,
            change = change,
            paymentMethod = if (selectedPaymentMethod == "E-Wallet") "E-Wallet ($selectedEWalletProvider)" else selectedPaymentMethod,
            onTransactionSaved = { record ->
                TransactionRepository.saveTransaction(context, record)
            },
            onDismiss = { showReceiptPreview = false }
        )
    }

    // Modal Pratinjau Struk untuk Transaksi dari Riwayat
    if (selectedHistoryTransaction != null) {
        val tx = selectedHistoryTransaction!!
        ReceiptThermalModal(
            storeName = tx.storeName,
            cartItems = tx.items,
            subtotal = tx.subtotal,
            discount = tx.discount,
            tax = tx.tax,
            serviceFee = tx.serviceFee,
            totalPrice = tx.totalPrice,
            cashPaid = tx.cashPaid,
            change = tx.change,
            paymentMethod = tx.paymentMethod,
            existingInvoiceNo = tx.id,
            existingDateStr = tx.dateFormatted,
            onDismiss = { selectedHistoryTransaction = null }
        )
    }
}

/**
 * Modal Simulator Barcode Scanner Kamera HP untuk Kasir
 */
@Composable
fun BarcodeScannerDialog(
    presetProducts: List<PresetProduct>,
    onBarcodeScanned: (PresetProduct) -> Unit,
    onBarcodeNotFound: ((String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var barcodeInput by remember { mutableStateOf("") }
    var isFlashOn by remember { mutableStateOf(false) }
    var hasCameraPermission by remember { mutableStateOf(PermissionUtils.hasCameraPermission(context)) }

    fun processBarcode(codeStr: String) {
        val cleanCode = codeStr.trim()
        if (cleanCode.isBlank()) return
        val matched = presetProducts.find {
            (it.barcode.isNotBlank() && it.barcode.equals(cleanCode, ignoreCase = true)) ||
                    it.name.contains(cleanCode, ignoreCase = true)
        }
        if (matched != null) {
            onBarcodeScanned(matched)
            barcodeInput = ""
        } else {
            onBarcodeNotFound?.invoke(cleanCode)
            barcodeInput = ""
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "✅ Izin kamera diberikan", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "⚠️ Izin kamera ditolak. Silakan berikan izin dari Pengaturan HP.", Toast.LENGTH_LONG).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pemindai Barcode Kamera",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!hasCameraPermission) {
                    CameraPermissionRequestCard(
                        onRequestPermission = {
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    // Visual Camera Frame Viewfinder Simulation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        // Laser scan line visual
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(2.dp)
                                .background(Color.Red)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "Arahkan Barcode Produk ke Garis Merah",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        // Flash Light Toggle Button Simulator
                        IconButton(
                            onClick = { isFlashOn = !isFlashOn },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = "Flash",
                                tint = if (isFlashOn) Color.Yellow else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Manual Barcode SKU Input Field
                OutlinedTextField(
                    value = barcodeInput,
                    onValueChange = { barcodeInput = it },
                    label = { Text("Ketik No. Barcode / SKU") },
                    placeholder = { Text("Contoh: 8991001 atau 8999999") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { processBarcode(barcodeInput) }) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Proses")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Simulation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Contoh Barcode / Simulasi:", fontSize = 11.sp, color = TextSubtle)

                    Surface(
                        onClick = { processBarcode("899987654321") },
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFF3E0),
                        border = BorderStroke(1.dp, Color(0xFFFFB74D))
                    ) {
                        Text(
                            text = "⚡ Scan Gagal (Tdk Terdaftar)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    items(presetProducts) { p ->
                        Surface(
                            onClick = { onBarcodeScanned(p) },
                            shape = RoundedCornerShape(8.dp),
                            color = PurplePrimaryContainer,
                            border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "${p.barcode} (${p.name})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PurplePrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog Alert "Produk Tidak Terdaftar Saat Scan Barcode"
 */
@Composable
fun UnknownBarcodeAlertDialog(
    barcodeCode: String,
    onDismiss: () -> Unit,
    onRegisterNow: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        icon = {
            Surface(
                shape = CircleShape,
                color = Color(0xFFFFF3E0),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "Produk Tidak Terdaftar",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Kode Barcode / SKU:",
                            fontSize = 11.sp,
                            color = TextSubtle
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = barcodeCode,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PurplePrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text(
                    text = "Produk dengan kode ini belum ada di database toko Anda.\n\nApakah Anda ingin mendaftarkan produk ini sekarang agar bisa langsung dijual?",
                    fontSize = 13.sp,
                    color = TextDark,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onRegisterNow,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Daftarkan Sekarang", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderDivider)
            ) {
                Text("Batal", color = TextSubtle)
            }
        }
    )
}

/**
 * Modal Pratinjau Struk Kasir Thermal (58mm/80mm style) dengan Fitur Cetak / Print & Share & Auto Save
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptThermalModal(
    storeName: String,
    cartItems: List<CartItem>,
    subtotal: Long,
    discount: Long,
    tax: Long,
    serviceFee: Long,
    totalPrice: Long,
    cashPaid: Long,
    change: Long,
    paymentMethod: String,
    existingInvoiceNo: String? = null,
    existingDateStr: String? = null,
    onTransactionSaved: ((TransactionRecord) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val receiptState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    val currentDateStr = remember { existingDateStr ?: dateFormat.format(Date()) }
    val invoiceNo = remember { existingInvoiceNo ?: "INV-${System.currentTimeMillis().toString().takeLast(6)}" }
    var showStoragePermissionDialog by remember { mutableStateOf(false) }

    // Simpan otomatis ke database transaksi jika ini transaksi baru
    LaunchedEffect(Unit) {
        if (existingInvoiceNo == null && onTransactionSaved != null) {
            val record = TransactionRecord(
                id = invoiceNo,
                timestamp = System.currentTimeMillis(),
                dateFormatted = currentDateStr,
                storeName = storeName,
                items = cartItems.toList(),
                subtotal = subtotal,
                discount = discount,
                tax = tax,
                serviceFee = serviceFee,
                totalPrice = totalPrice,
                cashPaid = cashPaid,
                change = change,
                paymentMethod = paymentMethod
            )
            onTransactionSaved(record)
        }
    }

    val formattedReceiptText = remember {
        buildString {
            appendLine(storeName.uppercase())
            appendLine("Jl. Usaha Bersama No. 88")
            appendLine("Tlp: 0812-3456-7890")
            appendLine("================================")
            appendLine("No. Struk : $invoiceNo")
            appendLine("Tgl       : $currentDateStr")
            appendLine("Metode    : $paymentMethod")
            appendLine("Kasir     : Utama")
            appendLine("--------------------------------")
            cartItems.forEach { item ->
                appendLine(item.name)
                appendLine("  ${item.qty} x ${formatRupiah(item.price)} = ${formatRupiah(item.subtotal)}")
            }
            appendLine("--------------------------------")
            appendLine("SUBTOTAL  : ${formatRupiah(subtotal)}")
            if (discount > 0) appendLine("DISKON    : -${formatRupiah(discount)}")
            if (tax > 0) appendLine("PPN (11%) : +${formatRupiah(tax)}")
            if (serviceFee > 0) appendLine("B.LAYANAN : +${formatRupiah(serviceFee)}")
            appendLine("TOTAL     : ${formatRupiah(totalPrice)}")
            if (cashPaid > 0) {
                appendLine("BAYAR     : ${formatRupiah(cashPaid)}")
                appendLine("KEMBALI   : ${formatRupiah(change)}")
            }
            appendLine("================================")
            appendLine("  Terima Kasih Atas Kunjungan")
            appendLine("         Barang Yg Sudah")
            appendLine("   Dibeli Tdk Dpt Ditukar")
            appendLine("================================")
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = receiptState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color(0xFFF8F9FA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pratinjau Struk Belanja Kasir",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "Format Thermal Paper (58mm / 80mm Ready)",
                fontSize = 12.sp,
                color = TextSubtle,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Tampilan Struk Thermal Kertas Putih
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .width(310.dp)
                    .border(1.dp, BorderDivider, RoundedCornerShape(8.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = storeName.uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                    Text(
                        text = "POS KASIR RESMI",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray
                    )
                    Text(
                        text = "================================",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color.Black
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("No: $invoiceNo", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tgl: $currentDateStr", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Bayar: $paymentMethod", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                    }

                    Text(
                        text = "--------------------------------",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color.Black
                    )

                    // Line Items
                    cartItems.forEach { item ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = item.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color.Black
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "  ${item.qty} x ${formatRupiah(item.price)}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = formatRupiah(item.subtotal),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    Text(
                        text = "--------------------------------",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color.Black
                    )

                    // Totals Breakdown
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("SUBTOTAL", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                        Text(formatRupiah(subtotal), fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                    }
                    if (discount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("DISKON", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.Red)
                            Text("- ${formatRupiah(discount)}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.Red)
                        }
                    }
                    if (tax > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("PPN (11%)", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                            Text("+ ${formatRupiah(tax)}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                        }
                    }
                    if (serviceFee > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("B. LAYANAN", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                            Text("+ ${formatRupiah(serviceFee)}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOTAL", fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color.Black)
                        Text(formatRupiah(totalPrice), fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color.Black)
                    }

                    if (cashPaid > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("BAYAR", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                            Text(formatRupiah(cashPaid), fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("KEMBALI", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                            Text(formatRupiah(change), fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                        }
                    }

                    Text(
                        text = "================================",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Terima Kasih Atas Kunjungan Anda!",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                    Text(
                        text = "Simpan Struk Ini Sebagai Bukti",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: Print, Unduh PNG, & Share
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cetak / Print Struk Thermal
                    Button(
                        onClick = {
                            try {
                                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                                if (printManager != null) {
                                    val printAdapter = TextPrintAdapter(context, formattedReceiptText, storeName)
                                    printManager.print("Struk_$invoiceNo", printAdapter, PrintAttributes.Builder().build())
                                } else {
                                    Toast.makeText(context, "Layanan Cetak tidak tersedia di perangkat ini", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Gagal mencetak: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cetak Struk", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Unduh Gambar PNG ke Penyimpanan Perangkat (Folder: Kalkulator Pintar)
                    Button(
                        onClick = {
                            if (PermissionUtils.hasStoragePermission(context)) {
                                saveReceiptImageToGallery(
                                    context = context,
                                    storeName = storeName,
                                    invoiceNo = invoiceNo,
                                    dateStr = currentDateStr,
                                    paymentMethod = paymentMethod,
                                    cartItems = cartItems,
                                    subtotal = subtotal,
                                    discount = discount,
                                    tax = tax,
                                    serviceFee = serviceFee,
                                    totalPrice = totalPrice,
                                    cashPaid = cashPaid,
                                    change = change
                                )
                            } else {
                                showStoragePermissionDialog = true
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimaryContainer, contentColor = PurplePrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Unduh PNG", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // Bagikan Struk
                Button(
                    onClick = {
                        try {
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "=== STRUK BELANJA $storeName ===\n\n$formattedReceiptText")
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Bagikan Struk Belanja")
                            context.startActivity(shareIntent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Gagal membagikan struk", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KeypadBackground, contentColor = TextDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bagikan Teks Struk", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showStoragePermissionDialog) {
        StoragePermissionDialog(
            onDismiss = { showStoragePermissionDialog = false },
            onGranted = {
                saveReceiptImageToGallery(
                    context = context,
                    storeName = storeName,
                    invoiceNo = invoiceNo,
                    dateStr = currentDateStr,
                    paymentMethod = paymentMethod,
                    cartItems = cartItems,
                    subtotal = subtotal,
                    discount = discount,
                    tax = tax,
                    serviceFee = serviceFee,
                    totalPrice = totalPrice,
                    cashPaid = cashPaid,
                    change = change
                )
            }
        )
    }
}

/**
 * Fungsi untuk membuat Bitmap gambar PNG berkualitas tinggi dari Struk Belanja
 */
fun generateReceiptBitmap(
    storeName: String,
    invoiceNo: String,
    dateStr: String,
    paymentMethod: String,
    cartItems: List<CartItem>,
    subtotal: Long,
    discount: Long,
    tax: Long,
    serviceFee: Long,
    totalPrice: Long,
    cashPaid: Long,
    change: Long
): Bitmap {
    val width = 800
    val lineSpacing = 42
    val padding = 40

    val lines = mutableListOf<Triple<String, Boolean, Boolean>>()
    lines.add(Triple(storeName.uppercase(), true, true))
    lines.add(Triple("POS KASIR RESMI", false, true))
    lines.add(Triple("========================================", false, true))
    lines.add(Triple("No Struk : $invoiceNo", false, false))
    lines.add(Triple("Tanggal  : $dateStr", false, false))
    lines.add(Triple("Metode   : $paymentMethod", false, false))
    lines.add(Triple("Kasir    : Utama", false, false))
    lines.add(Triple("----------------------------------------", false, true))

    cartItems.forEach { item ->
        lines.add(Triple(item.name, true, false))
        val subtotalStr = "Rp ${item.subtotal}"
        lines.add(Triple("  ${item.qty} x Rp ${item.price} = $subtotalStr", false, false))
    }

    lines.add(Triple("----------------------------------------", false, true))
    lines.add(Triple("SUBTOTAL : Rp $subtotal", false, false))
    if (discount > 0) lines.add(Triple("DISKON   : -Rp $discount", false, false))
    if (tax > 0) lines.add(Triple("PPN 11%  : +Rp $tax", false, false))
    if (serviceFee > 0) lines.add(Triple("B.LAYANAN: +Rp $serviceFee", false, false))
    lines.add(Triple("TOTAL    : Rp $totalPrice", true, false))

    if (cashPaid > 0) {
        lines.add(Triple("BAYAR    : Rp $cashPaid", false, false))
        lines.add(Triple("KEMBALI  : Rp $change", false, false))
    }
    lines.add(Triple("========================================", false, true))
    lines.add(Triple("Terima Kasih Atas Kunjungan Anda!", false, true))
    lines.add(Triple("Disimpan melalui Kalkulator Pintar", false, true))

    val height = padding * 2 + lines.size * lineSpacing + 40
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val bgPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    val paint = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 28f
        typeface = Typeface.MONOSPACE
        isAntiAlias = true
    }

    var y = padding.toFloat() + 30f
    lines.forEach { (text, isBold, isCenter) ->
        paint.typeface = if (isBold) Typeface.create(Typeface.MONOSPACE, Typeface.BOLD) else Typeface.MONOSPACE
        paint.textSize = if (isBold && isCenter) 32f else 28f

        val x = if (isCenter) {
            val textWidth = paint.measureText(text)
            (width - textWidth) / 2f
        } else {
            padding.toFloat()
        }

        canvas.drawText(text, x, y, paint)
        y += lineSpacing
    }

    return bitmap
}

/**
 * Menyimapan gambar PNG Struk Belanja ke folder khusus 'Kalkulator Pintar' di Penyimpanan Galeri Perangkat
 */
fun saveReceiptImageToGallery(
    context: Context,
    storeName: String,
    invoiceNo: String,
    dateStr: String,
    paymentMethod: String,
    cartItems: List<CartItem>,
    subtotal: Long,
    discount: Long,
    tax: Long,
    serviceFee: Long,
    totalPrice: Long,
    cashPaid: Long,
    change: Long
) {
    try {
        val bitmap = generateReceiptBitmap(storeName, invoiceNo, dateStr, paymentMethod, cartItems, subtotal, discount, tax, serviceFee, totalPrice, cashPaid, change)
        val fileName = "Struk_${invoiceNo}_${System.currentTimeMillis()}.png"

        val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Kalkulator Pintar")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
                true
            } else false
        } else {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val folder = File(picturesDir, "Kalkulator Pintar")
            if (!folder.exists()) {
                folder.mkdirs()
            }
            val file = File(folder, fileName)
            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            true
        }

        if (success) {
            Toast.makeText(context, "Gambar PNG disimpan di: Pictures/Kalkulator Pintar", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Gagal menyimpan gambar struk", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Terjadi kesalahan: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

/**
 * PrintDocumentAdapter khusus untuk mencetak teks struk ke Printer Android / Thermal Print Services
 */
class TextPrintAdapter(
    private val context: Context,
    private val printText: String,
    private val jobName: String
) : PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: android.os.CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }

        val info = android.print.PrintDocumentInfo.Builder("Struk_$jobName.pdf")
            .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(1)
            .build()

        callback.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<out android.print.PageRange>?,
        destination: android.os.ParcelFileDescriptor,
        cancellationSignal: android.os.CancellationSignal?,
        callback: WriteResultCallback
    ) {
        var outputStream: FileOutputStream? = null
        try {
            outputStream = FileOutputStream(destination.fileDescriptor)

            val pdfDocument = android.graphics.pdf.PdfDocument()
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(300, 600, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 10f
                typeface = Typeface.MONOSPACE
            }

            var y = 20f
            val lines = printText.split("\n")
            for (line in lines) {
                canvas.drawText(line, 10f, y, paint)
                y += 14f
            }

            pdfDocument.finishPage(page)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()

            callback.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
        } catch (e: Exception) {
            callback.onWriteFailed(e.message)
        } finally {
            try {
                outputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

/**
 * Modal Setup QRIS Toko (Pengaturan Upload & Informasi)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrisSetupDialog(
    currentStoreName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var merchantName by remember { mutableStateOf(QrisRepository.getQrisMerchantName(context, currentStoreName)) }
    var nmid by remember { mutableStateOf(QrisRepository.getQrisNmid(context)) }
    var staticCode by remember { mutableStateOf(QrisRepository.getQrisStaticCode(context)) }
    var imageUriState by remember { mutableStateOf<Uri?>(null) }
    var savedImagePath by remember { mutableStateOf(QrisRepository.getQrisImagePath(context)) }
    var showStoragePermissionDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUriState = uri
            Toast.makeText(context, "Gambar QRIS dipilih", Toast.LENGTH_SHORT).show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Setup & Integrasi QRIS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = TextSubtle)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderDivider)

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Gambar QRIS Toko saat ini:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF5F5F7))
                                .border(1.dp, BorderDivider, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (imageUriState != null) {
                                val bitmap = remember(imageUriState) {
                                    try {
                                        val inputStream = context.contentResolver.openInputStream(imageUriState!!)
                                        BitmapFactory.decodeStream(inputStream)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Gambar QRIS Dipilih",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text("Gagal memuat gambar", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                                }
                            } else if (savedImagePath.isNotEmpty() && File(savedImagePath).exists()) {
                                val bitmap = remember(savedImagePath) {
                                    BitmapFactory.decodeFile(savedImagePath)
                                }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Gambar QRIS Tersimpan",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text("Gambar tidak ditemukan", fontSize = 12.sp, color = TextSubtle)
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = null,
                                        tint = TextSubtle,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Belum ada gambar QRIS", fontSize = 12.sp, color = TextSubtle)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (PermissionUtils.hasStoragePermission(context)) {
                                    imagePickerLauncher.launch("image/*")
                                } else {
                                    showStoragePermissionDialog = true
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = null,
                                tint = PurplePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (savedImagePath.isNotEmpty() || imageUriState != null) "Ganti Gambar QRIS" else "Unggah Gambar QRIS dari Galeri",
                                color = PurplePrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = merchantName,
                        onValueChange = { merchantName = it },
                        label = { Text("Nama Merchant / Toko QRIS", color = TextDark, fontWeight = FontWeight.Medium) },
                        placeholder = { Text("Contoh: Toko Berkah", color = Color(0xFF64748B), fontSize = 14.sp) },
                        textStyle = TextStyle(color = TextDark, fontSize = 15.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Start),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            focusedPlaceholderColor = Color(0xFF64748B),
                            unfocusedPlaceholderColor = Color(0xFF64748B),
                            focusedLabelColor = PurplePrimary,
                            unfocusedLabelColor = TextDark,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = BorderDivider,
                            cursorColor = PurplePrimary
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = nmid,
                        onValueChange = { nmid = it },
                        label = { Text("NMID (National Merchant ID)", color = TextDark, fontWeight = FontWeight.Medium) },
                        placeholder = { Text("Contoh: ID102003849500", color = Color(0xFF64748B), fontSize = 14.sp) },
                        textStyle = TextStyle(color = TextDark, fontSize = 15.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Start),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            focusedPlaceholderColor = Color(0xFF64748B),
                            unfocusedPlaceholderColor = Color(0xFF64748B),
                            focusedLabelColor = PurplePrimary,
                            unfocusedLabelColor = TextDark,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = BorderDivider,
                            cursorColor = PurplePrimary
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = staticCode,
                        onValueChange = { staticCode = it },
                        label = { Text("Kode QRIS Statis (Opsional)", color = TextDark, fontWeight = FontWeight.Medium) },
                        placeholder = { Text("Masukkan teks/string QRIS statis", color = Color(0xFF64748B), fontSize = 14.sp) },
                        textStyle = TextStyle(color = TextDark, fontSize = 15.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Start),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            focusedPlaceholderColor = Color(0xFF64748B),
                            unfocusedPlaceholderColor = Color(0xFF64748B),
                            focusedLabelColor = PurplePrimary,
                            unfocusedLabelColor = TextDark,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = BorderDivider,
                            cursorColor = PurplePrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    QrisRepository.saveQrisSettings(
                        context = context,
                        merchantName = merchantName.ifBlank { currentStoreName },
                        nmid = nmid.ifBlank { "ID1020223456789" },
                        staticCode = staticCode,
                        imageUri = imageUriState
                    )
                    Toast.makeText(context, "✅ Pengaturan QRIS Berhasil Disimpan", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan Pengaturan QRIS", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showStoragePermissionDialog) {
        StoragePermissionDialog(
            onDismiss = { showStoragePermissionDialog = false },
            onGranted = { imagePickerLauncher.launch("image/*") }
        )
    }
}

/**
 * Modal Popup Tampilan QRIS & Total Pembayaran
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrisPaymentDialog(
    totalPrice: Long,
    onOpenSetup: () -> Unit,
    onPaymentConfirmed: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val merchantName = remember { QrisRepository.getQrisMerchantName(context, "KASIR PINTAR POS") }
    val nmid = remember { QrisRepository.getQrisNmid(context) }
    val savedImagePath = remember { QrisRepository.getQrisImagePath(context) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header QRIS Standar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFD32F2F),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "QRIS",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "QR Code Standar Pembayaran Nasional",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White
                    ) {
                        Text(
                            text = "GPN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Info Merchant
            Text(
                text = merchantName,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "NMID: $nmid",
                fontSize = 12.sp,
                color = TextSubtle
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Area Gambar QRIS
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(2.dp, PurplePrimary, RoundedCornerShape(16.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (savedImagePath.isNotEmpty() && File(savedImagePath).exists()) {
                    val bitmap = remember(savedImagePath) {
                        BitmapFactory.decodeFile(savedImagePath)
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "QRIS Code Toko",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("Gagal memuat QRIS", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    // Fallback QR Matrix Card jika belum upload gambar
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "QR Placeholder",
                            tint = PurplePrimary,
                            modifier = Modifier.size(120.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Pindai QRIS untuk membayar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Semua Aplikasi E-Wallet & Bank",
                            fontSize = 10.sp,
                            color = TextSubtle
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total Nominal Belanja (Sesuai instruksi: tampilkan nominal total belanja yang jelas)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PurplePrimaryContainer,
                border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOTAL PEMBAYARAN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatRupiah(totalPrice),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PurplePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenSetup,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F7)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = TextDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Setup QRIS", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onPaymentConfirmed,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Konfirmasi Selesai", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Modal Peringatan Jika Setup Akun E-Wallet Masih Kosong
 */
@Composable
fun EWalletNotConfiguredDialog(
    provider: String,
    onOpenSetup: () -> Unit,
    onDismiss: () -> Unit
) {
    val providerColor = when (provider) {
        "DANA" -> Color(0xFF118EEA)
        "OVO" -> Color(0xFF4C2A86)
        "GoPay" -> Color(0xFF00A5CF)
        "ShopeePay" -> Color(0xFFEE4D2D)
        "LinkAja" -> Color(0xFFE31E25)
        else -> PurplePrimary
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        icon = {
            Surface(
                shape = CircleShape,
                color = Color(0xFFFFF3E0),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Peringatan",
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "Setup $provider Belum Diatur",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Anda belum mengatur akun $provider. Silakan lakukan setup terlebih dahulu di menu pengaturan.",
                    fontSize = 13.sp,
                    color = TextDark.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFF8E1),
                    border = BorderStroke(1.dp, Color(0xFFFFE082)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 Masukkan nomor HP / ID akun & upload QR code $provider Anda di menu setup.",
                        fontSize = 11.sp,
                        color = Color(0xFFB71C1C),
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onOpenSetup()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = providerColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Lakukan Setup Sekarang",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderDivider),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Text(
                    text = "Batal",
                    fontSize = 13.sp,
                    color = TextSubtle
                )
            }
        }
    )
}

/**
 * Modal Setup Pembayaran E-Wallet (DANA, OVO, GoPay, ShopeePay, LinkAja)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EWalletSetupDialog(
    currentStoreName: String,
    initialProvider: String = "DANA",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val providers = remember { listOf("DANA", "OVO", "GoPay", "ShopeePay", "LinkAja") }
    var selectedProvider by remember { mutableStateOf(initialProvider) }

    var currentConfig by remember(selectedProvider) {
        mutableStateOf(EWalletRepository.getEWalletConfig(context, selectedProvider, currentStoreName))
    }
    var phoneInput by remember(selectedProvider) { mutableStateOf(currentConfig.phone) }
    var accountNameInput by remember(selectedProvider) { mutableStateOf(currentConfig.accountName) }
    var imageUriState by remember(selectedProvider) { mutableStateOf<Uri?>(null) }
    var savedImagePath by remember(selectedProvider) { mutableStateOf(currentConfig.qrImagePath) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUriState = uri
            Toast.makeText(context, "Gambar QR $selectedProvider dipilih", Toast.LENGTH_SHORT).show()
        }
    }

    val providerColor = when (selectedProvider) {
        "DANA" -> Color(0xFF118EEA)
        "OVO" -> Color(0xFF4C2A86)
        "GoPay" -> Color(0xFF00A5CF)
        "ShopeePay" -> Color(0xFFEE4D2D)
        "LinkAja" -> Color(0xFFE31E25)
        else -> PurplePrimary
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = providerColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Setup Akun E-Wallet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = TextSubtle)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderDivider)

            // Selector Provider E-Wallet (DANA, OVO, GoPay, ShopeePay, LinkAja)
            Text(
                text = "Pilih Penyedia E-Wallet:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSubtle,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(providers) { p ->
                    val isSelected = (p == selectedProvider)
                    val pColor = when (p) {
                        "DANA" -> Color(0xFF118EEA)
                        "OVO" -> Color(0xFF4C2A86)
                        "GoPay" -> Color(0xFF00A5CF)
                        "ShopeePay" -> Color(0xFFEE4D2D)
                        "LinkAja" -> Color(0xFFE31E25)
                        else -> PurplePrimary
                    }
                    Surface(
                        onClick = { selectedProvider = p },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) pColor else Color(0xFFF5F5F7),
                        border = BorderStroke(1.dp, if (isSelected) pColor else BorderDivider)
                    ) {
                        Text(
                            text = p,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else TextDark,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = providerColor.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, providerColor.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = providerColor
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Pengaturan Akun $selectedProvider",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = providerColor
                                )
                                Text(
                                    text = "Masukkan nomor HP/akun & upload QRIS khusus (opsional)",
                                    fontSize = 11.sp,
                                    color = TextDark.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = {
                            Text(
                                text = "Nomor HP / ID Akun $selectedProvider",
                                color = if (phoneInput.isNotEmpty()) providerColor else TextDark,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        placeholder = {
                            Text(
                                text = "Contoh: 0812-3456-7890",
                                color = Color(0xFF64748B),
                                fontSize = 14.sp
                            )
                        },
                        textStyle = TextStyle(
                            color = TextDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Start
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            focusedPlaceholderColor = Color(0xFF64748B),
                            unfocusedPlaceholderColor = Color(0xFF64748B),
                            focusedLabelColor = providerColor,
                            unfocusedLabelColor = TextDark,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = providerColor,
                            unfocusedBorderColor = BorderDivider,
                            cursorColor = providerColor
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = accountNameInput,
                        onValueChange = { accountNameInput = it },
                        label = {
                            Text(
                                text = "Nama Pemilik Akun / Merchant",
                                color = if (accountNameInput.isNotEmpty()) providerColor else TextDark,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        placeholder = {
                            Text(
                                text = "Contoh: ${currentStoreName.ifBlank { "Toko Kasir" }} ($selectedProvider)",
                                color = Color(0xFF64748B),
                                fontSize = 14.sp
                            )
                        },
                        textStyle = TextStyle(
                            color = TextDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Start
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            focusedPlaceholderColor = Color(0xFF64748B),
                            unfocusedPlaceholderColor = Color(0xFF64748B),
                            focusedLabelColor = providerColor,
                            unfocusedLabelColor = TextDark,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = providerColor,
                            unfocusedBorderColor = BorderDivider,
                            cursorColor = providerColor
                        )
                    )
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Gambar QR / QRIS Khusus $selectedProvider:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE8F5E9)
                            ) {
                                Text(
                                    text = "OPSIONAL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .size(170.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF8F9FA))
                                .border(1.dp, providerColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (imageUriState != null) {
                                val bitmap = remember(imageUriState) {
                                    try {
                                        val inputStream = context.contentResolver.openInputStream(imageUriState!!)
                                        BitmapFactory.decodeStream(inputStream)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Gambar QR $selectedProvider",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text("Gagal memuat gambar", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                                }
                            } else if (savedImagePath.isNotEmpty() && File(savedImagePath).exists()) {
                                val bitmap = remember(savedImagePath) {
                                    BitmapFactory.decodeFile(savedImagePath)
                                }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Gambar QR $selectedProvider Tersimpan",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text("Gambar tidak ditemukan", fontSize = 12.sp, color = TextSubtle)
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = null,
                                        tint = providerColor.copy(alpha = 0.6f),
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Belum Ada Gambar QR",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                    Text(
                                        text = "Hanya tampilkan nomor HP saat checkout jika tidak diunggah",
                                        fontSize = 10.sp,
                                        color = TextSubtle,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = providerColor.copy(alpha = 0.15f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Upload,
                                    contentDescription = null,
                                    tint = providerColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (savedImagePath.isNotEmpty() || imageUriState != null) "Ganti Gambar QR" else "Unggah Gambar QR",
                                    color = providerColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (savedImagePath.isNotEmpty() || imageUriState != null) {
                                Button(
                                    onClick = {
                                        EWalletRepository.removeQrImage(context, selectedProvider)
                                        imageUriState = null
                                        savedImagePath = ""
                                        Toast.makeText(context, "Gambar QR $selectedProvider dihapus", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val finalPhone = phoneInput.trim()
                    val finalName = accountNameInput.ifBlank { if (currentStoreName.isNotBlank()) "$currentStoreName ($selectedProvider)" else "Merchant $selectedProvider" }
                    if (finalPhone.isEmpty() && imageUriState == null && savedImagePath.isEmpty()) {
                        Toast.makeText(context, "Mohon isi nomor HP/ID akun atau unggah Gambar QR $selectedProvider", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    EWalletRepository.saveEWalletConfig(
                        context = context,
                        provider = selectedProvider,
                        phone = finalPhone,
                        accountName = finalName,
                        imageUri = imageUriState
                    )
                    Toast.makeText(context, "✅ Pengaturan $selectedProvider Berhasil Disimpan", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = providerColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan Pengaturan $selectedProvider", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Modal Popup Pembayaran Dinamis E-Wallet (Tampil Gambar QR jika ada, atau Informasi Teks HP jika tidak ada)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EWalletPaymentDialog(
    initialProvider: String,
    totalPrice: Long,
    onOpenSetup: (String) -> Unit,
    onPaymentConfirmed: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val providers = remember { listOf("DANA", "OVO", "GoPay", "ShopeePay", "LinkAja") }
    var activeProvider by remember { mutableStateOf(initialProvider) }

    val config = remember(activeProvider) {
        EWalletRepository.getEWalletConfig(context, activeProvider)
    }

    val isConfigured = remember(config, activeProvider) {
        EWalletRepository.isEWalletConfigured(context, activeProvider)
    }

    val hasQrImage = remember(config.qrImagePath) {
        config.qrImagePath.isNotEmpty() && File(config.qrImagePath).exists()
    }

    val providerColor = when (activeProvider) {
        "DANA" -> Color(0xFF118EEA)
        "OVO" -> Color(0xFF4C2A86)
        "GoPay" -> Color(0xFF00A5CF)
        "ShopeePay" -> Color(0xFFEE4D2D)
        "LinkAja" -> Color(0xFFE31E25)
        else -> PurplePrimary
    }

    val copyToClipboard: (String, String) -> Unit = { text, label ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label tersalin ke clipboard", Toast.LENGTH_SHORT).show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = providerColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Pembayaran $activeProvider",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Transfer / Pindai E-Wallet Pelanggan",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(providers) { p ->
                    val isSelected = (p == activeProvider)
                    val pColor = when (p) {
                        "DANA" -> Color(0xFF118EEA)
                        "OVO" -> Color(0xFF4C2A86)
                        "GoPay" -> Color(0xFF00A5CF)
                        "ShopeePay" -> Color(0xFFEE4D2D)
                        "LinkAja" -> Color(0xFFE31E25)
                        else -> PurplePrimary
                    }
                    Surface(
                        onClick = { activeProvider = p },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) pColor.copy(alpha = 0.15f) else Color(0xFFF5F5F7),
                        border = BorderStroke(1.dp, if (isSelected) pColor else BorderDivider)
                    ) {
                        Text(
                            text = p,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) pColor else TextDark,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!isConfigured) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    border = BorderStroke(1.dp, Color(0xFFFFE082)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Akun $activeProvider Belum Diatur",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Anda belum memasukkan nomor HP atau QR code untuk $activeProvider. Silakan lakukan setup terlebih dahulu.",
                            fontSize = 12.sp,
                            color = TextDark.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { onOpenSetup(activeProvider) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = providerColor)
                        ) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Atur Akun $activeProvider Sekarang", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (hasQrImage) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(2.dp, providerColor, RoundedCornerShape(16.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val bitmap = remember(config.qrImagePath) {
                        BitmapFactory.decodeFile(config.qrImagePath)
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "QR Code $activeProvider",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("Gagal memuat QR Code", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Pindai QR di atas menggunakan aplikasi $activeProvider",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSubtle
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8F9FA),
                    border = BorderStroke(1.dp, BorderDivider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("No. HP / Akun $activeProvider:", fontSize = 11.sp, color = TextSubtle)
                            Text(config.phone, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text("a.n. ${config.accountName}", fontSize = 11.sp, color = TextSubtle)
                        }

                        Surface(
                            onClick = { copyToClipboard(config.phone, "Nomor HP $activeProvider") },
                            shape = RoundedCornerShape(8.dp),
                            color = providerColor.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Salin",
                                    tint = providerColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Salin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = providerColor)
                            }
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                    border = BorderStroke(1.dp, providerColor.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = providerColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Info Transfer Manual $activeProvider",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                            }

                            Text(
                                text = "a.n. ${config.accountName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSubtle
                            )
                        }

                        HorizontalDivider(color = BorderDivider)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = providerColor.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, providerColor.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Nomor HP / Akun Tujuan:", fontSize = 11.sp, color = TextSubtle)
                                    Text(
                                        text = config.phone,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = providerColor
                                    )
                                }

                                Button(
                                    onClick = { copyToClipboard(config.phone, "Nomor HP $activeProvider") },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = providerColor)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Salin No HP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Langkah Pembayaran:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text("1. Buka aplikasi $activeProvider di smartphone pembeli.", fontSize = 11.sp, color = TextSubtle)
                            Text("2. Pilih menu Kirim / Transfer -> Kirim ke Nomor HP.", fontSize = 11.sp, color = TextSubtle)
                            Text("3. Masukkan nomor HP: ${config.phone}", fontSize = 11.sp, color = TextDark, fontWeight = FontWeight.SemiBold)
                            Text("4. Masukkan nominal pas: ${formatRupiah(totalPrice)}", fontSize = 11.sp, color = TextDark, fontWeight = FontWeight.SemiBold)
                            Text("5. Periksa nama akun (${config.accountName}) dan selesaikan pembayaran.", fontSize = 11.sp, color = TextSubtle)
                        }

                        TextButton(
                            onClick = { onOpenSetup(activeProvider) },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = null,
                                tint = providerColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "📷 Ingin tampilkan Gambar QR $activeProvider? Unggah di sini",
                                fontSize = 11.sp,
                                color = providerColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = providerColor.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, providerColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOTAL PEMBAYARAN $activeProvider",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = providerColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatRupiah(totalPrice),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = providerColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onOpenSetup(activeProvider) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F7)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = TextDark, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Setup E-Wallet", color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (isConfigured) {
                            onPaymentConfirmed(activeProvider)
                        } else {
                            Toast.makeText(context, "Silakan atur akun $activeProvider terlebih dahulu!", Toast.LENGTH_SHORT).show()
                            onOpenSetup(activeProvider)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = providerColor),
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Konfirmasi Selesai", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
