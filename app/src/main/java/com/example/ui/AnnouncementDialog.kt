package com.example.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurplePrimaryContainer
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextSubtle
import org.json.JSONObject

data class Announcement(
    val isActive: Boolean = false,
    val version: Int = 0,
    val title: String = "",
    val content: String = ""
)

object AnnouncementManager {
    private const val PREFS_NAME = "announcement_prefs"
    private const val KEY_DISMISSED_VERSION = "dismissed_announcement_version"

    /**
     * Membaca file mentah JSON (announcement.json) dari folder assets.
     */
    fun loadAnnouncementFromAssets(context: Context): Announcement? {
        return try {
            val jsonString = context.assets.open("announcement.json")
                .bufferedReader()
                .use { it.readText() }
            val json = JSONObject(jsonString)
            Announcement(
                isActive = json.optBoolean("isActive", false),
                version = json.optInt("version", 0),
                title = json.optString("title", "Pengumuman"),
                content = json.optString("content", "")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Memeriksa apakah popup pengumuman perlu ditampilkan ke pengguna.
     */
    fun shouldShowAnnouncement(context: Context, announcement: Announcement): Boolean {
        if (!announcement.isActive) return false
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastDismissedVersion = prefs.getInt(KEY_DISMISSED_VERSION, -1)
        return announcement.version > lastDismissedVersion
    }

    /**
     * Menyimpan status bahwa versi pengumuman ini sudah ditutup oleh pengguna.
     */
    fun markAnnouncementDismissed(context: Context, version: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_DISMISSED_VERSION, version).apply()
    }
}

@Composable
fun AnnouncementPopupLauncher() {
    val context = LocalContext.current
    var announcement by remember { mutableStateOf<Announcement?>(null) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val data = AnnouncementManager.loadAnnouncementFromAssets(context)
        if (data != null && AnnouncementManager.shouldShowAnnouncement(context, data)) {
            announcement = data
            isVisible = true
        }
    }

    if (isVisible && announcement != null) {
        AnnouncementDialog(
            announcement = announcement!!,
            onDismiss = {
                AnnouncementManager.markAnnouncementDismissed(context, announcement!!.version)
                isVisible = false
            }
        )
    }
}

@Composable
fun AnnouncementDialog(
    announcement: Announcement,
    onDismiss: () -> Unit
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
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Header Icon & Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PurplePrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = "Announcement",
                                tint = PurplePrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = announcement.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PurplePrimaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "v${announcement.version}.0 Update",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PurplePrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Content Box
                val scrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = announcement.content,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = TextDark,
                        modifier = Modifier.verticalScroll(scrollState)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Dismiss Button
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mengerti & Tutup",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
