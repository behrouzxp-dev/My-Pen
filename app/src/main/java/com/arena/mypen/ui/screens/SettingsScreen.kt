package com.arena.mypen.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.arena.mypen.R
import com.arena.mypen.model.NotificationSettings
import com.arena.mypen.ui.theme.*
import com.arena.mypen.util.DateUtils
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

@Composable
fun SettingsScreen(
    settings: NotificationSettings,
    onSettingsChanged: (NotificationSettings) -> Unit,
    onExportBackup: () -> String,
    onRestoreBackup: (String) -> Unit
) {
    val context = LocalContext.current

    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }
    var restorePensCount by remember { mutableStateOf(0) }
    var restoreSchemaVersion by remember { mutableStateOf(1) }
    var restoreExportDate by remember { mutableStateOf("") }
    var showRestoreWarningDialog by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri: Uri? = result.data?.data
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
                val content = reader.readText()
                reader.close()
                inputStream?.close()

                // Preliminary validation
                val root = JSONObject(content)
                val schemaVersion = root.optInt("schemaVersion", -1)
                if (schemaVersion != 1) {
                    Toast.makeText(context, "نسخه قالب پشتیبان ($schemaVersion) ناسازگار است.", Toast.LENGTH_LONG).show()
                    return@rememberLauncherForActivityResult
                }
                val pens = root.getJSONArray("pens")
                val exportedAt = root.optString("exportedAt", "نامشخص")

                pendingRestoreJson = content
                restorePensCount = pens.length()
                restoreSchemaVersion = schemaVersion
                restoreExportDate = exportedAt
                showRestoreWarningDialog = true

            } catch (e: Exception) {
                Toast.makeText(context, "خطا در خواندن فایل: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Notification Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, DividerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.section_notifications),
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = stringResource(R.string.notify_desc),
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Switch 30 days
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.notify_30_days), fontSize = 13.sp, color = TextPrimary)
                    Switch(
                        checked = settings.notify30Days,
                        onCheckedChange = { onSettingsChanged(settings.copy(notify30Days = it)) }
                    )
                }

                Divider(color = DividerColor)

                // Switch 14 days
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.notify_14_days), fontSize = 13.sp, color = TextPrimary)
                    Switch(
                        checked = settings.notify14Days,
                        onCheckedChange = { onSettingsChanged(settings.copy(notify14Days = it)) }
                    )
                }

                Divider(color = DividerColor)

                // Switch 7 days
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.notify_7_days), fontSize = 13.sp, color = TextPrimary)
                    Switch(
                        checked = settings.notify7Days,
                        onCheckedChange = { onSettingsChanged(settings.copy(notify7Days = it)) }
                    )
                }
            }
        }

        // 2. Backup & Restore Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, DividerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.section_backup),
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = stringResource(R.string.backup_info),
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                // Export Button
                Button(
                    onClick = {
                        try {
                            val jsonString = onExportBackup()
                            shareBackupFile(context, jsonString)
                            Toast.makeText(context, R.string.msg_backup_created, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "خطا در صدور: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.btn_export_backup), fontSize = 13.sp)
                }

                // Import Button
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "*/*"
                            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json", "text/plain", "application/octet-stream"))
                        }
                        filePickerLauncher.launch(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, PrimaryBlue),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.btn_import_backup), fontSize = 13.sp)
                }
            }
        }

        // 3. Medical Disclaimer Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, DividerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.medical_disclaimer_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }
                Text(
                    text = stringResource(R.string.medical_disclaimer_text),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }

    // Restore Confirmation Dialog
    if (showRestoreWarningDialog && pendingRestoreJson != null) {
        AlertDialog(
            onDismissRequest = { showRestoreWarningDialog = false },
            title = { Text(stringResource(R.string.dialog_restore_warning_title), fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = stringResource(
                        R.string.dialog_restore_warning_message,
                        restorePensCount,
                        restoreSchemaVersion,
                        restoreExportDate
                    ),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreWarningDialog = false
                        try {
                            onRestoreBackup(pendingRestoreJson!!)
                            Toast.makeText(context, R.string.msg_restore_success, Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "خطا در بازیابی: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusExpired)
                ) {
                    Text(stringResource(R.string.btn_confirm_restore))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreWarningDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

private fun shareBackupFile(context: Context, jsonContent: String) {
    val backupDir = File(context.cacheDir, "backups").apply { if (!exists()) mkdirs() }
    val fileName = "mypen_backup_${DateUtils.todayDate().replace("-", "")}.json"
    val backupFile = File(backupDir, fileName)

    FileOutputStream(backupFile).use { fos ->
        fos.write(jsonContent.toByteArray(StandardCharsets.UTF_8))
    }

    val contentUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        backupFile
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, contentUri)
        putExtra(Intent.EXTRA_SUBJECT, "فایل پشتیبان My Pen")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(shareIntent, "ذخیره یا ارسال فایل پشتیبان"))
}
