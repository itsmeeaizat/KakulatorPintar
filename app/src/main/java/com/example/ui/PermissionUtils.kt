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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat

/**
 * Utilitas untuk Memeriksa & Meminta Izin Akses Perangkat (Kamera, Galeri, Penyimpanan)
 */
object PermissionUtils {

    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

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

    fun hasStoragePermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 (API 29) ke atas menggunakan Scoped Storage / MediaStore API
            return true
        }
        val permissions = getRequiredStoragePermissions()
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Buka Pengaturan HP secara manual untuk memberikan izin", Toast.LENGTH_LONG).show()
        }
    }
}

/**
 * Banner Permintaan Izin Kamera untuk Barcode Scanner
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
 * Dialog Penjelasan Izin Penyimpanan/Galeri
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
            Toast.makeText(context, "✅ Izin penyimpanan diberikan", Toast.LENGTH_SHORT).show()
            onGranted()
            onDismiss()
        } else {
            isDeniedBySystem = true
            Toast.makeText(context, "⚠️ Izin ditolak. Silakan aktifkan via Pengaturan.", Toast.LENGTH_LONG).show()
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
                        .background(Color(0xFFEFF6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderSpecial,
                        contentDescription = "Penyimpanan Galeri",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Akses Penyimpanan & Galeri",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Izin ini diperlukan untuk menyimpan gambar Struk Belanja (.png) atau memilih foto QRIS dari galeri HP Anda.",
                    fontSize = 12.sp,
                    color = Color(0xFF4B5563),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isDeniedBySystem) {
                    Text(
                        text = "Izin sempat ditolak sebelumnya. Buka Pengaturan Aplikasi untuk mengaktifkan izin secara manual.",
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
                        onClick = {
                            if (isDeniedBySystem) {
                                PermissionUtils.openAppSettings(context)
                            } else {
                                launcher.launch(PermissionUtils.getRequiredStoragePermissions())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text(
                            text = if (isDeniedBySystem) "Pengaturan HP" else "Berikan Izin",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
