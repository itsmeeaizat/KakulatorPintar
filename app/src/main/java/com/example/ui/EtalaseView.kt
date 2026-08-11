package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import java.text.NumberFormat
import java.util.Locale
import com.example.ui.QrBarcodeGenerator
import com.example.ui.PermissionUtils
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.entity.CategoryEntity
import com.example.data.entity.ProductEntity
import com.example.ui.theme.BorderDivider
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurplePrimaryContainer
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextSubtle

/**
 * Komponen Utama Etalase Warung dengan Sistem Kategori Berlapis (Multi-Level Category Showcase)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EtalaseView(
    viewModel: EtalaseViewModel = viewModel(),
    onAddToCart: (ProductEntity) -> Unit = {},
    cartItemsCount: Int = 0,
    cartSubtotal: Long = 0L,
    onOpenCart: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val navigationStack by viewModel.navigationStack.collectAsState()
    val currentCategory by viewModel.currentCategory.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()
    val products by viewModel.productsInCurrentCategory.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showAllProductsDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var qrPrintProduct by remember { mutableStateOf<ProductEntity?>(null) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceCanvas),
        color = SurfaceCanvas
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Bar & Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onDismiss != null) {
                        IconButton(onClick = { onDismiss() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Tutup",
                                tint = TextDark
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(PurplePrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = PurplePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Etalase Produk Warung",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Kategori Berlapis & Stok Real-time",
                            fontSize = 12.sp,
                            color = TextSubtle
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar (Ukurannya seragam persis kartu di bawah: Rounded 14.dp, border, elevation)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Cari produk atau brand di semua kategori...", fontSize = 13.sp, color = TextSubtle) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSubtle) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Text("✕", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextSubtle)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = BorderDivider
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mode Pencarian vs Mode Navigasi Kategori
            if (searchQuery.isNotBlank()) {
                // Tampilan Hasil Pencarian
                Text(
                    text = "Hasil Pencarian: '${searchQuery}' (${searchResults.size} Produk)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = BorderDivider,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tidak ditemukan produk dengan kata kunci tersebut",
                                fontSize = 13.sp,
                                color = TextSubtle
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(searchResults, key = { it.id }) { product ->
                            ProductCardItem(
                                product = product,
                                onAddToCart = {
                                    onAddToCart(product)
                                    Toast.makeText(context, "${product.name} ditambahkan", Toast.LENGTH_SHORT).show()
                                },
                                onEdit = { editingProduct = product },
                                onDelete = { viewModel.deleteProduct(product.id) }
                            )
                        }
                    }
                }
            } else {
                // Jalur Breadcrumb (Navigasi Berlapis: Utama > Makanan > Mie > Indomie)
                BreadcrumbBar(
                    navigationStack = navigationStack,
                    onBreadcrumbClick = { index -> viewModel.navigateToBreadcrumbIndex(index) },
                    onBackClick = { viewModel.navigateBack() }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tombol Pintas: Semua Produk (List View A-Z) & Label Jumlah Stok
                AllProductsQuickBar(
                    totalProducts = allProducts.size,
                    totalStock = allProducts.sumOf { it.stock },
                    onClick = { showAllProductsDialog = true }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Area Konten Etalase (Kategori & Produk)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Seksi Sub-Kategori Header & Button
                        item {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (navigationStack.isEmpty()) "Kategori Utama" else "Sub-Kategori (${currentCategory?.name})",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                    Text(
                                        text = "${subCategories.size} Pilihan",
                                        fontSize = 12.sp,
                                        color = TextSubtle
                                    )
                                }

                                OutlinedButton(
                                    onClick = { showAddCategoryDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = PurplePrimaryContainer.copy(alpha = 0.35f),
                                        contentColor = PurplePrimary
                                    )
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("+ Tambah Kategori Baru", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Daftar Card Sub-Kategori
                        if (subCategories.isNotEmpty()) {
                            items(subCategories, key = { "cat_${it.id}" }) { category ->
                                CategoryCardItem(
                                    category = category,
                                    onClick = { viewModel.navigateToCategory(category) },
                                    onDelete = { viewModel.deleteCategory(category.id) }
                                )
                            }
                        } else if (navigationStack.isEmpty()) {
                            item {
                                Text(
                                    text = "Belum ada kategori utama",
                                    fontSize = 12.sp,
                                    color = TextSubtle,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }

                        // Divider Spacing
                        item {
                            HorizontalDivider(
                                color = BorderDivider.copy(alpha = 0.6f),
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }

                        // Seksi Daftar Produk Header & Button
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (navigationStack.isEmpty()) "Semua Produk (${products.size})" else "Produk di '${currentCategory?.name}' (${products.size})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )

                                Surface(
                                    onClick = { showAddProductDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    color = PurplePrimaryContainer,
                                    border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+ Produk Baru", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PurplePrimary)
                                    }
                                }
                            }
                        }

                        // Daftar Card Produk
                        if (products.isEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, BorderDivider),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Default.AddShoppingCart,
                                                contentDescription = null,
                                                tint = BorderDivider,
                                                modifier = Modifier.size(36.dp)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = if (navigationStack.isEmpty()) "Belum ada produk di toko" else "Belum ada produk di kategori '${currentCategory?.name}'",
                                                fontSize = 12.sp,
                                                color = TextSubtle
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            items(products, key = { "prod_${it.id}" }) { product ->
                                ProductCardItem(
                                    product = product,
                                    onAddToCart = {
                                        onAddToCart(product)
                                        Toast.makeText(context, "'${product.name}' masuk keranjang", Toast.LENGTH_SHORT).show()
                                    },
                                    onEdit = { editingProduct = product },
                                    onDelete = { viewModel.deleteProduct(product.id) },
                                    onPrintQrBarcode = { qrPrintProduct = it }
                                )
                            }
                        }
                    }
                }
            }

            // Sticky Floating Cart Bar (Bottom of Etalase view)
            if (cartItemsCount > 0 && onOpenCart != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PurplePrimary,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onOpenCart() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$cartItemsCount",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "$cartItemsCount item di keranjang",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                val localeId = Locale("id", "ID")
                                val fmt = NumberFormat.getCurrencyInstance(localeId).apply { maximumFractionDigits = 0 }
                                Text(
                                    text = fmt.format(cartSubtotal),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Buka Kasir ➔",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PurplePrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Form Dialog "Tambah Kategori Baru"
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            allCategories = allCategories,
            currentParentId = currentCategory?.id,
            onDismiss = { showAddCategoryDialog = false },
            onSave = { name, parentId ->
                viewModel.addCategory(name, parentId)
                showAddCategoryDialog = false
                Toast.makeText(context, "Kategori '$name' berhasil dibuat", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal Form Dialog "Tambah Produk Baru"
    if (showAddProductDialog) {
        AddProductDialog(
            allCategories = allCategories,
            defaultCategoryId = currentCategory?.id ?: allCategories.firstOrNull()?.id ?: 0,
            onDismiss = { showAddProductDialog = false },
            onSave = { name, brand, price, stock, categoryId, barcode ->
                viewModel.addProduct(name, brand, price, stock, categoryId, barcode)
                showAddProductDialog = false
                Toast.makeText(context, "Produk '$name' berhasil ditambahkan", Toast.LENGTH_SHORT).show()
            },
            onAddNewCategory = { name, parentId ->
                viewModel.addCategory(name, parentId)
                Toast.makeText(context, "Kategori '$name' berhasil dibuat", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal Dialog "Semua Produk (List View A-Z)"
    if (showAllProductsDialog) {
        AllProductsListDialog(
            allProducts = allProducts,
            allCategories = allCategories,
            onAddToCart = onAddToCart,
            onEditProduct = { product -> editingProduct = product },
            onDismiss = { showAllProductsDialog = false }
        )
    }

    // Modal Form Dialog "Sunting Produk"
    if (editingProduct != null) {
        EditProductDialog(
            product = editingProduct!!,
            allCategories = allCategories,
            onDismiss = { editingProduct = null },
            onSave = { id, name, brand, price, stock, categoryId, barcode ->
                viewModel.updateProduct(id, name, brand, price, stock, categoryId, barcode)
                editingProduct = null
                Toast.makeText(context, "Produk '$name' berhasil diperbarui", Toast.LENGTH_SHORT).show()
            },
            onAddNewCategory = { name, parentId ->
                viewModel.addCategory(name, parentId)
                Toast.makeText(context, "Kategori '$name' berhasil dibuat", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal Dialog "Cetak Barcode / QR Produk"
    if (qrPrintProduct != null) {
        ProductQrPrintDialog(
            product = qrPrintProduct!!,
            onDismiss = { qrPrintProduct = null }
        )
    }
}

/**
 * Jalur Breadcrumb Navigasi Berlapis (e.g., Utama > Makanan > Mie > Indomie)
 */
@Composable
fun BreadcrumbBar(
    navigationStack: List<CategoryEntity>,
    onBreadcrumbClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderDivider),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (navigationStack.isNotEmpty()) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali ke level sebelumnya",
                        tint = PurplePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Item Root "Utama"
                item {
                    BreadcrumbChip(
                        label = "Utama",
                        isSelected = navigationStack.isEmpty(),
                        icon = Icons.Default.Home,
                        onClick = { onBreadcrumbClick(-1) }
                    )
                }

                itemsIndexed(navigationStack) { index, category ->
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextSubtle,
                        modifier = Modifier.size(16.dp)
                    )

                    val isLast = index == navigationStack.lastIndex
                    BreadcrumbChip(
                        label = category.name,
                        isSelected = isLast,
                        icon = Icons.Default.Category,
                        onClick = { onBreadcrumbClick(index) }
                    )
                }
            }
        }
    }
}

@Composable
fun BreadcrumbChip(
    label: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) PurplePrimaryContainer else Color(0xFFF1F5F9),
        border = if (isSelected) BorderStroke(1.dp, PurplePrimary) else null,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) PurplePrimary else TextSubtle,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) PurplePrimary else TextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Kartu Item Kategori dalam Daftar Baris (Horizontal Row)
 */
@Composable
fun CategoryCardItem(
    category: CategoryEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderDivider),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon Kategori dengan warna & style yang selaras dengan header atas (PurplePrimaryContainer & Category Icon)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PurplePrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = category.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Klik untuk buka →",
                        fontSize = 11.sp,
                        color = TextSubtle,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Kategori",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextSubtle,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Kartu Item Produk dengan Indikator Stok & Tombol Tambah ke Keranjang
 */
@Composable
fun ProductCardItem(
    product: ProductEntity,
    onAddToCart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPrintQrBarcode: ((ProductEntity) -> Unit)? = null
) {
    val localeId = Locale("id", "ID")
    val formatter = NumberFormat.getCurrencyInstance(localeId).apply {
        maximumFractionDigits = 0
    }
    val formattedPrice = formatter.format(product.price)

    // Dynamic stock color badge
    val (stockBg, stockTextColor, stockText) = when {
        product.stock <= 0 -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Stok Habis!")
        product.stock < 5 -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "Stok: ${product.stock} (Kritis)")
        product.stock < 10 -> Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), "Stok: ${product.stock}")
        else -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Stok: ${product.stock}")
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderDivider),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Brand Badge & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = product.brand.ifBlank { "Umum" },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSubtle,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onPrintQrBarcode != null) {
                        IconButton(
                            onClick = { onPrintQrBarcode(product) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "Cetak Barcode/QR",
                                tint = Color(0xFF166534),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Sunting Produk",
                            tint = PurplePrimary,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Produk",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Name
            Text(
                text = product.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Price
            Text(
                text = formattedPrice,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PurplePrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Stock Badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = stockBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (product.stock < 5) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = stockTextColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Text(
                        text = stockText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = stockTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Add to Cart Button
            Button(
                onClick = onAddToCart,
                enabled = true,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (product.stock > 0) PurplePrimary else Color(0xFFEA580C)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (product.stock > 0) "+ Keranjang" else "+ Keranjang (Stok 0)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Dialog Form "Tambah Kategori Baru"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryDialog(
    allCategories: List<CategoryEntity>,
    currentParentId: Int?,
    onDismiss: () -> Unit,
    onSave: (name: String, parentId: Int?) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }

    // Pembagian Kategori berdasarkan Level Hierarchy (Level 1, Level 2, Level 3)
    val level1Categories = remember(allCategories) {
        allCategories.filter { it.parentId == null }
    }
    val level1Ids = remember(level1Categories) {
        level1Categories.map { it.id }.toSet()
    }
    val level2Categories = remember(allCategories, level1Ids) {
        allCategories.filter { it.parentId != null && level1Ids.contains(it.parentId) }
    }
    val level2Ids = remember(level2Categories) {
        level2Categories.map { it.id }.toSet()
    }

    // Menentukan level awal berdasarkan posisi navigasi aktif
    val initialLevel = remember(currentParentId, level1Ids, level2Ids) {
        when {
            currentParentId == null -> 1
            level1Ids.contains(currentParentId) -> 2
            level2Ids.contains(currentParentId) -> 3
            else -> 1
        }
    }

    var selectedLevel by remember { mutableStateOf(initialLevel) }
    var selectedParentId by remember { mutableStateOf<Int?>(currentParentId) }
    var isParentDropdownExpanded by remember { mutableStateOf(false) }

    // Popup Kecil untuk Peringatan Prasyarat Kategori Kosong
    var warningMessage by remember { mutableStateOf<String?>(null) }

    // Label Nama Induk Terpilih
    val parentCategoryName = remember(selectedLevel, selectedParentId, level1Categories, level2Categories) {
        when (selectedLevel) {
            1 -> "Kategori Utama (Level 1)"
            2 -> level1Categories.find { it.id == selectedParentId }?.name ?: "Pilih Induk Level 1..."
            3 -> level2Categories.find { it.id == selectedParentId }?.name ?: "Pilih Induk Level 2..."
            else -> "Kategori Utama"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "Tambah Kategori Baru",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Input Nama Kategori Baru
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Nama Kategori Baru") },
                    placeholder = { Text("Contoh: Sembako, Minuman, Dapur") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedLabelColor = PurplePrimary,
                        unfocusedLabelColor = TextSubtle,
                        focusedPlaceholderColor = TextSubtle,
                        unfocusedPlaceholderColor = TextSubtle,
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = BorderDivider
                    )
                )

                // Label Pilihan Posisi Level
                Text(
                    text = "Posisi Kategori (Pilih Level):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSubtle
                )

                // Tombol Pilihan Level: 1, 2, 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val levelList = listOf(
                        1 to "Tombol 1",
                        2 to "Tombol 2",
                        3 to "Tombol 3"
                    )

                    levelList.forEach { (levelNum, levelLabel) ->
                        val isSelected = (selectedLevel == levelNum)
                        Surface(
                            onClick = {
                                when (levelNum) {
                                    1 -> {
                                        selectedLevel = 1
                                        selectedParentId = null
                                    }
                                    2 -> {
                                        if (level1Categories.isEmpty()) {
                                            warningMessage = "Anda harus menambahkan kategori 1"
                                        } else {
                                            selectedLevel = 2
                                            if (selectedParentId == null || !level1Ids.contains(selectedParentId)) {
                                                selectedParentId = level1Categories.first().id
                                            }
                                        }
                                    }
                                    3 -> {
                                        if (level1Categories.isEmpty() && level2Categories.isEmpty()) {
                                            warningMessage = "Anda harus menambahkan kategori 1 dan 2"
                                        } else if (level2Categories.isEmpty()) {
                                            warningMessage = "Anda harus menambahkan kategori 2"
                                        } else {
                                            selectedLevel = 3
                                            if (selectedParentId == null || !level2Ids.contains(selectedParentId)) {
                                                selectedParentId = level2Categories.first().id
                                            }
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) PurplePrimary else Color.White,
                            border = BorderStroke(1.dp, if (isSelected) PurplePrimary else BorderDivider),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = levelLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else TextDark,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }

                // Dropdown Pilihan Induk Kategori jika Level 2 atau Level 3 dipilih
                if (selectedLevel == 2 || selectedLevel == 3) {
                    val availableParents = if (selectedLevel == 2) level1Categories else level2Categories
                    val labelText = if (selectedLevel == 2) "Pilih Induk (Kategori Level 1):" else "Pilih Induk (Kategori Level 2):"

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = labelText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSubtle
                        )

                        ExposedDropdownMenuBox(
                            expanded = isParentDropdownExpanded,
                            onExpandedChange = { isParentDropdownExpanded = !isParentDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = parentCategoryName,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isParentDropdownExpanded) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC),
                                    focusedBorderColor = PurplePrimary,
                                    unfocusedBorderColor = BorderDivider
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = isParentDropdownExpanded,
                                onDismissRequest = { isParentDropdownExpanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                availableParents.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name, fontSize = 13.sp, color = TextDark) },
                                        onClick = {
                                            selectedParentId = category.id
                                            isParentDropdownExpanded = false
                                        },
                                        modifier = Modifier.background(Color.White)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (categoryName.isNotBlank()) {
                        val finalParentId = if (selectedLevel == 1) null else selectedParentId
                        onSave(categoryName.trim(), finalParentId)
                    }
                },
                enabled = categoryName.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                Text("Simpan Kategori", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSubtle)
            }
        }
    )

    // Popup Kecil Peringatan jika Kategori Prasyarat Belum Ada
    if (warningMessage != null) {
        AlertDialog(
            onDismissRequest = { warningMessage = null },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Perhatian",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
            },
            text = {
                Text(
                    text = warningMessage!!,
                    fontSize = 13.sp,
                    color = TextDark
                )
            },
            confirmButton = {
                Button(
                    onClick = { warningMessage = null },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("OK, Mengerti", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/**
 * Modal Dialog "Pilih / Ubah Kategori" Berlatar Putih Modern
 * Dilengkapi Pencarian Kategori & Pembuat Kategori Baru (Level 1, 2, 3) On-The-Fly
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPickerModalDialog(
    selectedCategoryId: Int,
    allCategories: List<CategoryEntity>,
    onCategorySelected: (Int) -> Unit,
    onAddNewCategory: (name: String, parentId: Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showCreateForm by remember { mutableStateOf(false) }

    // State untuk form buat kategori baru
    var newCategoryName by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableIntStateOf(1) } // 1, 2, atau 3
    var selectedParentId by remember { mutableStateOf<Int?>(null) }

    val level1Categories = remember(allCategories) { allCategories.filter { it.parentId == null } }
    val level1Ids = remember(level1Categories) { level1Categories.map { it.id }.toSet() }
    val level2Categories = remember(allCategories, level1Ids) { allCategories.filter { it.parentId != null && level1Ids.contains(it.parentId) } }

    // Map jalur hirarki kategori
    val categoryPathMap = remember(allCategories) {
        val catMap = allCategories.associateBy { it.id }
        allCategories.associate { cat ->
            val path = mutableListOf<String>()
            var curr: CategoryEntity? = cat
            while (curr != null) {
                path.add(0, curr.name)
                curr = curr.parentId?.let { catMap[it] }
            }
            cat.id to path.joinToString(" > ")
        }
    }

    val filteredCategories = remember(allCategories, searchQuery) {
        if (searchQuery.isBlank()) allCategories
        else allCategories.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PurplePrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = PurplePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = "Pilih / Pindahkan Kategori",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = TextSubtle)
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Pencarian Kategori
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari nama kategori...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSubtle) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = null, tint = TextSubtle) } }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = BorderDivider
                    )
                )

                // Tombol Toggle + Buat Kategori Baru
                Surface(
                    onClick = { showCreateForm = !showCreateForm },
                    shape = RoundedCornerShape(12.dp),
                    color = if (showCreateForm) PurplePrimaryContainer else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, if (showCreateForm) PurplePrimary else BorderDivider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (showCreateForm) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = null,
                                tint = PurplePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (showCreateForm) "Tutup Form Kategori Baru" else "+ Buat Kategori Baru",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PurplePrimary
                            )
                        }
                        Text(
                            text = if (showCreateForm) "▲" else "▼",
                            fontSize = 11.sp,
                            color = PurplePrimary
                        )
                    }
                }

                // Form On-The-Fly Tambah Kategori
                if (showCreateForm) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
                        border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Tambah Kategori Baru",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PurplePrimary
                            )

                            // Nama Kategori Input
                            OutlinedTextField(
                                value = newCategoryName,
                                onValueChange = { newCategoryName = it },
                                label = { Text("Nama Kategori Baru *") },
                                placeholder = { Text("Contoh: Minuman Dingin") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextDark,
                                    unfocusedTextColor = TextDark,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = PurplePrimary,
                                    unfocusedBorderColor = BorderDivider
                                )
                            )

                            // Pilih Level Kategori (1, 2, 3)
                            Text(
                                text = "Tingkat / Level Kategori:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    1 to "1. Utama",
                                    2 to "2. Sub-Kategori",
                                    3 to "3. Sub-Sub"
                                ).forEach { (lvl, lbl) ->
                                    val isLvlSelected = selectedLevel == lvl
                                    Surface(
                                        onClick = {
                                            selectedLevel = lvl
                                            if (lvl == 2) {
                                                selectedParentId = level1Categories.firstOrNull()?.id
                                            } else if (lvl == 3) {
                                                selectedParentId = level2Categories.firstOrNull()?.id
                                            } else {
                                                selectedParentId = null
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isLvlSelected) PurplePrimary else Color.White,
                                        border = BorderStroke(1.dp, if (isLvlSelected) PurplePrimary else BorderDivider),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = lbl,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLvlSelected) Color.White else TextDark,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            // Pilih Induk Kategori jika Level 2 atau 3
                            if (selectedLevel == 2 || selectedLevel == 3) {
                                val parents = if (selectedLevel == 2) level1Categories else level2Categories
                                val pLabel = if (selectedLevel == 2) "Pilih Induk (Level 1):" else "Pilih Induk (Level 2):"

                                if (parents.isEmpty()) {
                                    Text(
                                        text = "Buat kategori Level ${selectedLevel - 1} terlebih dahulu!",
                                        fontSize = 11.sp,
                                        color = Color(0xFFD97706),
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = pLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextDark
                                    )

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(parents, key = { it.id }) { cat ->
                                            val isPSelected = selectedParentId == cat.id
                                            Surface(
                                                onClick = { selectedParentId = cat.id },
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isPSelected) PurplePrimaryContainer else Color.White,
                                                border = BorderStroke(1.dp, if (isPSelected) PurplePrimary else BorderDivider)
                                            ) {
                                                Text(
                                                    text = cat.name,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isPSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isPSelected) PurplePrimary else TextDark,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    if (newCategoryName.isNotBlank()) {
                                        val parent = if (selectedLevel == 1) null else selectedParentId
                                        onAddNewCategory(newCategoryName.trim(), parent)
                                        newCategoryName = ""
                                        showCreateForm = false
                                    }
                                },
                                enabled = newCategoryName.isNotBlank() && (selectedLevel == 1 || selectedParentId != null),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Simpan Kategori Baru", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                HorizontalDivider(color = BorderDivider.copy(alpha = 0.6f))

                Text(
                    text = "Daftar Kategori Tersedia:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                // List Kategori
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    // Opsi 0: Tanpa Kategori
                    item {
                        val isSelected = selectedCategoryId == 0
                        Surface(
                            onClick = {
                                onCategorySelected(0)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PurplePrimaryContainer else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, if (isSelected) PurplePrimary else BorderDivider),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Tanpa Kategori (Utama / Umum)",
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) PurplePrimary else TextDark
                                    )
                                    Text(
                                        text = "Produk berada di tingkat paling atas warung",
                                        fontSize = 10.sp,
                                        color = TextSubtle
                                    )
                                }
                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    items(filteredCategories, key = { it.id }) { cat ->
                        val isSelected = selectedCategoryId == cat.id
                        val fullPath = categoryPathMap[cat.id] ?: cat.name

                        Surface(
                            onClick = {
                                onCategorySelected(cat.id)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PurplePrimaryContainer else Color.White,
                            border = BorderStroke(1.dp, if (isSelected) PurplePrimary else BorderDivider),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cat.name,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelected) PurplePrimary else TextDark
                                    )
                                    if (cat.parentId != null) {
                                        Text(
                                            text = "Jalur: $fullPath",
                                            fontSize = 10.sp,
                                            color = TextSubtle,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", fontWeight = FontWeight.Bold, color = PurplePrimary)
            }
        }
    )
}

/**
 * Dialog Form "Tambah Produk Baru"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    allCategories: List<CategoryEntity>,
    defaultCategoryId: Int,
    initialBarcode: String = "",
    onDismiss: () -> Unit,
    onSave: (name: String, brand: String, price: Double, stock: Int, categoryId: Int, barcode: String) -> Unit,
    onAddNewCategory: (name: String, parentId: Int?) -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var brandName by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var stockText by remember { mutableStateOf("10") }
    var barcodeText by remember(initialBarcode) { mutableStateOf(initialBarcode) }
    var selectedCategoryId by remember { mutableStateOf(defaultCategoryId) }
    var showCategoryPicker by remember { mutableStateOf(false) }

    val selectedCategoryName = remember(selectedCategoryId, allCategories) {
        if (selectedCategoryId == 0) {
            "Tanpa Kategori (Umum / Utama)"
        } else {
            allCategories.find { it.id == selectedCategoryId }?.name ?: "Tanpa Kategori (Umum / Utama)"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = PurplePrimaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AddShoppingCart,
                            contentDescription = null,
                            tint = PurplePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = if (initialBarcode.isNotBlank()) "Daftarkan Produk Baru" else "Tambah Produk Baru",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Kode Barcode Field (if present or requested)
                OutlinedTextField(
                    value = barcodeText,
                    onValueChange = { barcodeText = it },
                    label = { Text("Kode Barcode / SKU") },
                    placeholder = { Text("Scan / Ketik kode barcode") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedLabelColor = PurplePrimary,
                        unfocusedLabelColor = TextSubtle,
                        focusedPlaceholderColor = TextSubtle,
                        unfocusedPlaceholderColor = TextSubtle,
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = BorderDivider
                    )
                )

                // Nama Produk Input
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Nama Produk *") },
                    placeholder = { Text("Contoh: Indomie Goreng Rendang") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedLabelColor = PurplePrimary,
                        unfocusedLabelColor = TextSubtle,
                        focusedPlaceholderColor = TextSubtle,
                        unfocusedPlaceholderColor = TextSubtle,
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = BorderDivider
                    )
                )

                // Merk / Brand Input
                OutlinedTextField(
                    value = brandName,
                    onValueChange = { brandName = it },
                    label = { Text("Merk / Brand (Opsional)") },
                    placeholder = { Text("Contoh: Indomie, Aqua, Wings") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedLabelColor = PurplePrimary,
                        unfocusedLabelColor = TextSubtle,
                        focusedPlaceholderColor = TextSubtle,
                        unfocusedPlaceholderColor = TextSubtle,
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = BorderDivider
                    )
                )

                // Harga & Stok Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Harga Jual (Rp) *") },
                        placeholder = { Text("3500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            focusedLabelColor = PurplePrimary,
                            unfocusedLabelColor = TextSubtle,
                            focusedPlaceholderColor = TextSubtle,
                            unfocusedPlaceholderColor = TextSubtle,
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = BorderDivider
                        )
                    )

                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it.filter { char -> char.isDigit() } },
                        label = { Text("Jumlah Stok") },
                        placeholder = { Text("10") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            focusedLabelColor = PurplePrimary,
                            unfocusedLabelColor = TextSubtle,
                            focusedPlaceholderColor = TextSubtle,
                            unfocusedPlaceholderColor = TextSubtle,
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = BorderDivider
                        )
                    )
                }

                HorizontalDivider(
                    color = BorderDivider.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                // Tombol Pilihan Kategori Produk Berlatar Putih Modern
                Text(
                    text = "Tambah ke Kategori:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )

                Surface(
                    onClick = { showCategoryPicker = true },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = PurplePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Kategori Terpilih:",
                                    fontSize = 10.sp,
                                    color = TextSubtle,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = selectedCategoryName,
                                    fontSize = 13.sp,
                                    color = TextDark,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PurplePrimaryContainer
                        ) {
                            Text(
                                text = "Pilih / Ubah ➔",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PurplePrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            val priceVal = priceText.toDoubleOrNull()
            val parsedStock = stockText.toIntOrNull()
            val stock = if (parsedStock != null && parsedStock >= 0) parsedStock else 0
            val isFormValid = productName.isNotBlank() && priceVal != null && priceVal > 0.0 && parsedStock != null && parsedStock >= 0
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    if (isFormValid) {
                        onSave(productName.trim(), brandName.trim(), price, stock, selectedCategoryId, barcodeText.trim())
                    }
                },
                enabled = isFormValid,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                Text("Simpan Produk", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSubtle)
            }
        }
    )

    if (showCategoryPicker) {
        CategoryPickerModalDialog(
            selectedCategoryId = selectedCategoryId,
            allCategories = allCategories,
            onCategorySelected = { newCatId -> selectedCategoryId = newCatId },
            onAddNewCategory = { name, parentId -> onAddNewCategory(name, parentId) },
            onDismiss = { showCategoryPicker = false }
        )
    }
}

/**
 * Dialog Form "Sunting Produk" (Ubah Nama, Merk, Harga, Stok, & Kategori)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(
    product: ProductEntity,
    allCategories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (id: Int, name: String, brand: String, price: Double, stock: Int, categoryId: Int, barcode: String) -> Unit,
    onAddNewCategory: (name: String, parentId: Int?) -> Unit
) {
    var productName by remember(product) { mutableStateOf(product.name) }
    var brandName by remember(product) { mutableStateOf(product.brand) }
    var priceText by remember(product) { mutableStateOf(if (product.price % 1.0 == 0.0) product.price.toLong().toString() else product.price.toString()) }
    var stockText by remember(product) { mutableStateOf(product.stock.toString()) }
    var barcodeText by remember(product) { mutableStateOf(product.barcode) }
    var selectedCategoryId by remember(product) { mutableStateOf(product.categoryId) }
    var showCategoryPicker by remember { mutableStateOf(false) }

    val selectedCategoryName = remember(selectedCategoryId, allCategories) {
        if (selectedCategoryId == 0) {
            "Tanpa Kategori (Umum / Utama)"
        } else {
            allCategories.find { it.id == selectedCategoryId }?.name ?: "Tanpa Kategori (Umum / Utama)"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = PurplePrimaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = PurplePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = "Sunting Produk",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Kode Barcode Field
                OutlinedTextField(
                    value = barcodeText,
                    onValueChange = { barcodeText = it },
                    label = { Text("Kode Barcode / SKU") },
                    placeholder = { Text("Ketik kode barcode") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedLabelColor = PurplePrimary,
                        unfocusedLabelColor = TextSubtle,
                        focusedPlaceholderColor = TextSubtle,
                        unfocusedPlaceholderColor = TextSubtle,
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = BorderDivider
                    )
                )

                // Nama Produk Input
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Nama Produk *") },
                    placeholder = { Text("Contoh: Indomie Goreng Rendang") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedLabelColor = PurplePrimary,
                        unfocusedLabelColor = TextSubtle,
                        focusedPlaceholderColor = TextSubtle,
                        unfocusedPlaceholderColor = TextSubtle,
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = BorderDivider
                    )
                )

                // Merk / Brand Input
                OutlinedTextField(
                    value = brandName,
                    onValueChange = { brandName = it },
                    label = { Text("Merk / Brand (Opsional)") },
                    placeholder = { Text("Contoh: Indomie, Aqua, Wings") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedLabelColor = PurplePrimary,
                        unfocusedLabelColor = TextSubtle,
                        focusedPlaceholderColor = TextSubtle,
                        unfocusedPlaceholderColor = TextSubtle,
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = BorderDivider
                    )
                )

                // Harga & Stok Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Harga Jual (Rp) *") },
                        placeholder = { Text("3500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            focusedLabelColor = PurplePrimary,
                            unfocusedLabelColor = TextSubtle,
                            focusedPlaceholderColor = TextSubtle,
                            unfocusedPlaceholderColor = TextSubtle,
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = BorderDivider
                        )
                    )

                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it.filter { char -> char.isDigit() } },
                        label = { Text("Jumlah Stok *") },
                        placeholder = { Text("10") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            focusedLabelColor = PurplePrimary,
                            unfocusedLabelColor = TextSubtle,
                            focusedPlaceholderColor = TextSubtle,
                            unfocusedPlaceholderColor = TextSubtle,
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = BorderDivider
                        )
                    )
                }

                HorizontalDivider(
                    color = BorderDivider.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                // Tombol Pilihan Kategori Produk Berlatar Putih Modern
                Text(
                    text = "Kategori Produk / Pindahkan Ke:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )

                Surface(
                    onClick = { showCategoryPicker = true },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = PurplePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Kategori Terpilih:",
                                    fontSize = 10.sp,
                                    color = TextSubtle,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = selectedCategoryName,
                                    fontSize = 13.sp,
                                    color = TextDark,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PurplePrimaryContainer
                        ) {
                            Text(
                                text = "Pilih / Pindahkan ➔",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PurplePrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            val priceVal = priceText.toDoubleOrNull()
            val isFormValid = productName.isNotBlank() && priceVal != null && priceVal > 0.0
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    val stock = stockText.toIntOrNull() ?: 0
                    if (isFormValid) {
                        onSave(product.id, productName.trim(), brandName.trim(), price, stock, selectedCategoryId, barcodeText.trim())
                    }
                },
                enabled = isFormValid,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSubtle)
            }
        }
    )

    if (showCategoryPicker) {
        CategoryPickerModalDialog(
            selectedCategoryId = selectedCategoryId,
            allCategories = allCategories,
            onCategorySelected = { newCatId -> selectedCategoryId = newCatId },
            onAddNewCategory = { name, parentId -> onAddNewCategory(name, parentId) },
            onDismiss = { showCategoryPicker = false }
        )
    }
}

/**
 * Bar / Tombol Pintas ke Tampilan Semua Produk (List View A-Z)
 */
@Composable
fun AllProductsQuickBar(
    totalProducts: Int,
    totalStock: Int,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderDivider),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PurplePrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ListAlt,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Semua Produk (A - Z)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$totalProducts Barang • Total Stok: $totalStock Pcs",
                        fontSize = 11.sp,
                        color = TextSubtle,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextSubtle,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Dialog / Popup Tampilan Semua Produk (List View Terurut A - Z) dengan Search Bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllProductsListDialog(
    allProducts: List<ProductEntity>,
    allCategories: List<CategoryEntity>,
    onAddToCart: (ProductEntity) -> Unit,
    onEditProduct: (ProductEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    // Urut otomatis A-Z & Filter Real-time
    val filteredAndSortedProducts = remember(allProducts, searchQuery) {
        allProducts
            .filter {
                searchQuery.isBlank() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.brand.contains(searchQuery, ignoreCase = true)
            }
            .sortedBy { it.name.lowercase(Locale.getDefault()) }
    }

    val totalStock = remember(allProducts) { allProducts.sumOf { it.stock } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(PurplePrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = PurplePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Daftar Semua Produk (A - Z)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "${allProducts.size} Produk • Total Stok: $totalStock Pcs",
                                fontSize = 11.sp,
                                color = TextSubtle
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = TextSubtle
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar dalam Dialog List View A-Z
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari produk atau merk (A-Z)...", fontSize = 13.sp, color = TextSubtle) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSubtle) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Text("✕", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSubtle)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = BorderDivider
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Label Info Pengurutan & Filter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Urutan Abjad A-Z (${filteredAndSortedProducts.size} Barang)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSubtle
                    )
                    if (searchQuery.isNotBlank()) {
                        Text(
                            text = "Filter aktif",
                            fontSize = 11.sp,
                            color = PurplePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // List Produk Vertikal
                if (filteredAndSortedProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = BorderDivider,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (allProducts.isEmpty()) "Belum ada produk di etalase warung" else "Tidak ada produk yang cocok dengan '$searchQuery'",
                                fontSize = 13.sp,
                                color = TextSubtle,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredAndSortedProducts, key = { it.id }) { product ->
                            val categoryName = remember(product.categoryId, allCategories) {
                                if (product.categoryId == 0) "Umum" else allCategories.find { c -> c.id == product.categoryId }?.name ?: "Umum"
                            }

                            ProductRowItem(
                                product = product,
                                categoryName = categoryName,
                                onAddToCart = {
                                    onAddToCart(product)
                                    Toast.makeText(context, "${product.name} masuk keranjang", Toast.LENGTH_SHORT).show()
                                },
                                onEdit = { onEditProduct(product) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tombol Selesai / Tutup
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = TextDark)
                ) {
                    Text("Tutup", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

/**
 * Item Baris Produk Tunggal untuk List View Vertikal
 */
@Composable
fun ProductRowItem(
    product: ProductEntity,
    categoryName: String,
    onAddToCart: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderDivider),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(PurplePrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = product.name.take(1).uppercase(Locale.getDefault()),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = categoryName,
                                fontSize = 10.sp,
                                color = TextSubtle,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                        if (product.brand.isNotEmpty() && product.brand != "Umum") {
                            Text(
                                text = "• ${product.brand}",
                                fontSize = 10.sp,
                                color = TextSubtle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Harga, Stok & Tombol + Beli
            Column(horizontalAlignment = Alignment.End) {
                val formattedPrice = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                    maximumFractionDigits = 0
                }.format(product.price)

                Text(
                    text = formattedPrice,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurplePrimary
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val stockBg = when {
                        product.stock > 5 -> Color(0xFFDCFCE7)
                        product.stock in 1..5 -> Color(0xFFFEF9C3)
                        else -> Color(0xFFFEE2E2)
                    }
                    val stockTextColor = when {
                        product.stock > 5 -> Color(0xFF166534)
                        product.stock in 1..5 -> Color(0xFF854D0E)
                        else -> Color(0xFF991B1B)
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = stockBg,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = "Stok: ${product.stock}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = stockTextColor,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    Button(
                        onClick = onAddToCart,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("+ Beli", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Sunting Produk",
                            tint = PurplePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog Cetak / Simpan Label Barcode & QR Code Produk
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductQrPrintDialog(
    product: ProductEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isQrMode by remember { mutableStateOf(false) } // false: Barcode 1D, true: QR Code
    var showStoragePermissionDialog by remember { mutableStateOf(false) }

    val formattedPrice = remember(product.price) {
        val localeId = Locale("id", "ID")
        NumberFormat.getCurrencyInstance(localeId).apply { maximumFractionDigits = 0 }.format(product.price)
    }

    val barcodeCode = if (product.barcode.isNotBlank()) product.barcode else "SKU-${product.id}"

    val bitmap = remember(product, isQrMode) {
        if (isQrMode) {
            QrBarcodeGenerator.generateQrCodeBitmap(
                qrContent = barcodeCode,
                productName = product.name,
                priceStr = formattedPrice
            )
        } else {
            QrBarcodeGenerator.generateBarcodeBitmap(
                barcodeStr = barcodeCode,
                productName = product.name,
                priceStr = formattedPrice
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = PurplePrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = PurplePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = "Cetak Barcode / QR",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                }
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Selector Barcode vs QR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = !isQrMode,
                        onClick = { isQrMode = false },
                        label = { Text("Barcode 1D", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White,
                            selectedLabelColor = PurplePrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    FilterChip(
                        selected = isQrMode,
                        onClick = { isQrMode = true },
                        label = { Text("QR Code 2D", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White,
                            selectedLabelColor = PurplePrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Preview Label Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderDivider),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Preview Label",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Kode Barcode / SKU: $barcodeCode",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PurplePrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text(
                    text = "Label ini dapat disimpan ke galeri foto HP untuk dicetak ke stiker produk.",
                    fontSize = 11.sp,
                    color = TextSubtle,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (PermissionUtils.hasStoragePermission(context)) {
                        val savedUri = QrBarcodeGenerator.saveLabelToGallery(
                            context = context,
                            bitmap = bitmap,
                            productName = product.name,
                            barcodeStr = barcodeCode
                        )
                        if (savedUri != null) {
                            Toast.makeText(context, "Label berhasil disimpan ke Galeri!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Gagal menyimpan label", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        showStoragePermissionDialog = true
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Simpan ke Galeri", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Tutup", color = TextSubtle)
            }
        }
    )

    if (showStoragePermissionDialog) {
        StoragePermissionDialog(
            onDismiss = { showStoragePermissionDialog = false },
            onGranted = {
                QrBarcodeGenerator.saveLabelToGallery(
                    context = context,
                    bitmap = bitmap,
                    productName = product.name,
                    barcodeStr = barcodeCode
                )
                Toast.makeText(context, "Label berhasil disimpan ke Galeri!", Toast.LENGTH_LONG).show()
            }
        )
    }
}
