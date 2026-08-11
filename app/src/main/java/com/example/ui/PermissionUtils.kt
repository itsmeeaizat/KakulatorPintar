package com.example.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat

/**
 * Utilitas Sistem Penanganan Izin Runtime (Runtime Permission Checker)
 * Menggunakan ActivityResultContracts & ContextCompat.checkSelfPermission
 */
object PermissionUtils {

    /**
     * Memeriksa status izin tunggal menggunakan ContextCompat.checkSelfPermission
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Memeriksa izin akses kamera
     */
    fun hasCameraPermission(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.CAMERA)
    }

    /**
     * Memeriksa izin lokasi (Fine atau Coarse)
     */
    fun hasLocationPermission(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
               hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    /**
     * Mendapatkan daftar izin penyimpanan sesuai versi Android SDK
     */
    fun getRequiredStoragePermissions(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES
            )
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.P -> arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            else -> arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    /**
     * Mendapatkan daftar izin lokasi
     */
    fun getRequiredLocationPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    /**
     * Memeriksa izin penyimpanan/galeri
     */
    fun hasStoragePermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 (API 29) ke atas menggunakan Scoped Storage / MediaStore API
            return true
        }
        val permissions = getRequiredStoragePermissions()
        return permissions.all { hasPermission(context, it) }
    }

    /**
     * Fallback: Mengarahkan pengguna ke Pengaturan Aplikasi jika izin ditolak secara permanen
     */
    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Silakan buka Pengaturan HP secara manual untuk memberikan izin.", Toast.LENGTH_LONG).show()
        }
    }
}

/**
 * Helper Hook Compose untuk meminta izin tunggal dengan ActivityResultContracts.RequestPermission
 * Selalu mengecek ContextCompat.checkSelfPermission sebelum menampilkan dialog sistem.
 */
@Composable
fun rememberPermissionRequester(
    permission: String,
    onPermissionResult: (Boolean) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionResult(isGranted)
    }

    return {
        if (PermissionUtils.hasPermission(context, permission)) {
            onPermissionResult(true)
        } else {
            try {
                launcher.launch(permission)
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal meminta izin: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                onPermissionResult(false)
            }
        }
    }
}

/**
 * Helper Hook Compose untuk meminta banyak izin sekaligus dengan ActivityResultContracts.RequestMultiplePermissions
 */
@Composable
fun rememberMultiplePermissionsRequester(
    permissions: Array<String>,
    onPermissionsResult: (Boolean) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val allGranted = permissionsMap.values.all { it }
        onPermissionsResult(allGranted)
    }

    return {
        val allGranted = permissions.all { PermissionUtils.hasPermission(context, it) }
        if (allGranted) {
            onPermissionsResult(true)
        } else {
            try {
                launcher.launch(permissions)
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal meminta izin: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                onPermissionsResult(false)
            }
        }
    }
}

/**
 * Banner / Card Permintaan Izin Kamera untuk Fitur Scanner
 */
@Composable
fun CameraPermissionRequestCard(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFFFEDD5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Izin Kamera",
                    tint = Color(0xFFC2410C),
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Izin Kamera Dibutuhkan",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9A3412)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Aplikasi memerlukan akses kamera HP untuk memindai barcode & QR code produk secara langsung.",
                fontSize = 12.sp,
                color = Color(0xFFC2410C),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Izinkan Kamera", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { PermissionUtils.openAppSettings(context) },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "Pengaturan HP", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Dialog Edukasi & Penanganan Izin Penyimpanan / Galeri
 */
@Composable
fun StoragePermissionDialog(
    onDismiss: () -> Unit,
    onGranted: () -> Unit
) {
    val context = LocalContext.current
    var isDeniedBySystem by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val allGranted = permissionsMap.values.all { it }
        if (allGranted) {
            Toast.makeText(context, "Izin penyimpanan diberikan", Toast.LENGTH_SHORT).show()
            onGranted()
            onDismiss()
        } else {
            isDeniedBySystem = true
            Toast.makeText(context, "Izin ditolak. Silakan aktifkan via Pengaturan.", Toast.LENGTH_LONG).show()
        }
    }

    GenericPermissionEducationalDialog(
        title = "Akses Penyimpanan & Galeri",
        description = "Izin ini diperlukan untuk menyimpan gambar Struk Belanja (.png) atau memilih foto QRIS dari galeri HP Anda.",
        icon = Icons.Default.FolderSpecial,
        iconTint = Color(0xFF2563EB),
        iconBgColor = Color(0xFFEFF6FF),
        isDenied = isDeniedBySystem,
        onDismiss = onDismiss,
        onRequest = {
            if (isDeniedBySystem) {
                PermissionUtils.openAppSettings(context)
            } else {
                launcher.launch(PermissionUtils.getRequiredStoragePermissions())
            }
        }
    )
}

/**
 * Dialog Edukasi & Penanganan Izin Lokasi Perangkat
 */
@Composable
fun LocationPermissionDialog(
    onDismiss: () -> Unit,
    onGranted: () -> Unit
) {
    val context = LocalContext.current
    var isDeniedBySystem by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val granted = permissionsMap.values.any { it }
        if (granted) {
            Toast.makeText(context, "Izin lokasi diberikan", Toast.LENGTH_SHORT).show()
            onGranted()
            onDismiss()
        } else {
            isDeniedBySystem = true
            Toast.makeText(context, "Izin lokasi ditolak. Buka Pengaturan untuk mengaktifkan.", Toast.LENGTH_LONG).show()
        }
    }

    GenericPermissionEducationalDialog(
        title = "Akses Lokasi Perangkat",
        description = "Aplikasi memerlukan akses lokasi untuk mendeteksi alamat toko kasir dan koordinat secara otomatis pada struk belanja.",
        icon = Icons.Default.LocationOn,
        iconTint = Color(0xFF16A34A),
        iconBgColor = Color(0xFFDCFCE7),
        isDenied = isDeniedBySystem,
        onDismiss = onDismiss,
        onRequest = {
            if (isDeniedBySystem) {
                PermissionUtils.openAppSettings(context)
            } else {
                launcher.launch(PermissionUtils.getRequiredLocationPermissions())
            }
        }
    )
}

/**
 * Component Dialog Edukasi Izin Pop-Up Generik
 */
@Composable
fun GenericPermissionEducationalDialog(
    title: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    iconBgColor: Color,
    isDenied: Boolean,
    onDismiss: () -> Unit,
    onRequest: () -> Unit
) {
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
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isDenied) {
                    Text(
                        text = "Izin sempat ditolak. Buka Pengaturan Aplikasi untuk mengaktifkan izin secara manual.",
                        fontSize = 11.sp,
                        color = Color(0xFFDC2626),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Batal", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = iconTint),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text(
                            text = if (isDenied) "Pengaturan HP" else "Berikan Izin",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

