package com.mypen.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.mypen.app.R
import com.mypen.app.model.NotificationSettings
import com.mypen.app.ui.components.AppCard
import com.mypen.app.ui.components.LocalAppMessages
import com.mypen.app.ui.components.RowDivider
import com.mypen.app.ui.components.SectionHeader
import com.mypen.app.ui.components.SettingsRow
import com.mypen.app.ui.theme.*
import com.mypen.app.util.DateUtils
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
    val messages = LocalAppMessages.current

    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }
    var restorePensCount by remember { mutableStateOf(0) }
    var restoreSchemaVersion by remember { mutableStateOf(1) }
    var restoreExportDate by remember { mutableStateOf("") }
    var showRestoreWarningDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val versionName = remember(context) {
        runCatching {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: ""
        }.getOrDefault("")
    }

    val msgBackupCreated = stringResource(R.string.msg_backup_created)
    val msgRestoreSuccess = stringResource(R.string.msg_restore_success)
    val msgSchemaError = stringResource(R.string.msg_backup_schema_mismatch)
    val msgReadError = stringResource(R.string.msg_read_error)
    val msgExportError = stringResource(R.string.msg_export_error)
    val msgRestoreError = stringResource(R.string.msg_restore_error)

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri: Uri? = result.data?.data
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    messages.error(msgReadError)
                    return@rememberLauncherForActivityResult
                }
                val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
                val content = reader.readText()
                reader.close()
                inputStream.close()

                val root = JSONObject(content)
                val schemaVersion = root.optInt("schemaVersion", -1)
                if (schemaVersion != 1) {
                    messages.error("$msgSchemaError ($schemaVersion)")
                    return@rememberLauncherForActivityResult
                }
                val pens = root.getJSONArray("pens")
                val exportedAt = root.optString("exportedAt", "—")

                pendingRestoreJson = content
                restorePensCount = pens.length()
                restoreSchemaVersion = schemaVersion
                restoreExportDate = exportedAt
                showRestoreWarningDialog = true
            } catch (e: Exception) {
                messages.error("$msgReadError: ${e.message}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        // 1. Notifications
        SectionHeader(title = stringResource(R.string.section_notifications))
        AppCard {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.notify_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        start = Spacing.lg,
                        end = Spacing.lg,
                        top = Spacing.md,
                        bottom = Spacing.sm
                    )
                )
                SettingsRow(
                    icon = Icons.Filled.NotificationsActive,
                    title = stringResource(R.string.notify_30_days),
                    subtitle = stringResource(R.string.notify_30_desc),
                    trailing = {
                        Switch(
                            checked = settings.notify30Days,
                            onCheckedChange = {
                                onSettingsChanged(settings.copy(notify30Days = it))
                            }
                        )
                    }
                )
                RowDivider()
                SettingsRow(
                    icon = Icons.Filled.NotificationsActive,
                    title = stringResource(R.string.notify_14_days),
                    subtitle = stringResource(R.string.notify_14_desc),
                    trailing = {
                        Switch(
                            checked = settings.notify14Days,
                            onCheckedChange = {
                                onSettingsChanged(settings.copy(notify14Days = it))
                            }
                        )
                    }
                )
                RowDivider()
                SettingsRow(
                    icon = Icons.Filled.NotificationsActive,
                    title = stringResource(R.string.notify_7_days),
                    subtitle = stringResource(R.string.notify_7_desc),
                    trailing = {
                        Switch(
                            checked = settings.notify7Days,
                            onCheckedChange = {
                                onSettingsChanged(settings.copy(notify7Days = it))
                            }
                        )
                    }
                )
            }
        }

        // 2. Backup and restore
        SectionHeader(title = stringResource(R.string.section_backup))
        AppCard {
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingsRow(
                    icon = Icons.Filled.Upload,
                    title = stringResource(R.string.btn_export_backup),
                    subtitle = stringResource(R.string.backup_export_desc),
                    onClick = {
                        try {
                            val jsonString = onExportBackup()
                            shareBackupFile(context, jsonString)
                            messages.success(msgBackupCreated)
                        } catch (e: Exception) {
                            messages.error("$msgExportError: ${e.message}")
                        }
                    }
                )
                RowDivider()
                SettingsRow(
                    icon = Icons.Filled.Restore,
                    title = stringResource(R.string.btn_import_backup),
                    subtitle = stringResource(R.string.backup_import_desc),
                    accent = StatusExpiringSoon,
                    onClick = {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "*/*"
                            putExtra(
                                Intent.EXTRA_MIME_TYPES,
                                arrayOf("application/json", "text/plain", "application/octet-stream")
                            )
                        }
                        filePickerLauncher.launch(intent)
                    }
                )
                Text(
                    text = stringResource(R.string.backup_info),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(Spacing.lg)
                )
            }
        }

        // 3. Application information
        SectionHeader(title = stringResource(R.string.section_app_info))
        AppCard {
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingsRow(
                    icon = Icons.Filled.Info,
                    title = stringResource(R.string.about_title),
                    subtitle = stringResource(R.string.about_subtitle),
                    onClick = { showAboutDialog = true }
                )
                RowDivider()
                SettingsRow(
                    icon = Icons.Filled.Info,
                    title = stringResource(R.string.settings_version),
                    subtitle = if (versionName.isNullOrBlank()) "—" else versionName,
                    accent = StatusActive
                )
            }
        }

        // 4. Medical disclaimer
        AppCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.md))
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    Text(
                        text = stringResource(R.string.medical_disclaimer_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.medical_disclaimer_text),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xl))
    }

    if (showAboutDialog) {
        AboutDialog(
            versionName = versionName,
            onDismiss = { showAboutDialog = false }
        )
    }

    if (showRestoreWarningDialog && pendingRestoreJson != null) {
        AlertDialog(
            onDismissRequest = { showRestoreWarningDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.dialog_restore_warning_title),
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.dialog_restore_warning_message,
                        restorePensCount,
                        restoreSchemaVersion,
                        restoreExportDate
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreWarningDialog = false
                        try {
                            pendingRestoreJson?.let { onRestoreBackup(it) }
                            messages.success(msgRestoreSuccess)
                        } catch (e: Exception) {
                            messages.error("$msgRestoreError: ${e.message}")
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
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

@Composable
private fun AboutDialog(versionName: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.about_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    text = stringResource(R.string.about_version, versionName),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.about_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    text = stringResource(R.string.about_features),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_close))
            }
        }
    )
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
        putExtra(Intent.EXTRA_SUBJECT, "My Pen backup")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(shareIntent, null))
}
