package com.example.myuniqueapp.Screens.presentations.Privacy

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myuniqueapp.Navmain.Route
import com.example.myuniqueapp.Repository.NoteRepository
import com.example.myuniqueapp.Screens.presentations.PrivacyCenter.*
import com.example.myuniqueapp.ui.theme.ManropeFontFamily
import com.example.myuniqueapp.ui.theme.MansalvaFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    navController: NavHostController,
    repository: NoteRepository
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
    }

    val viewModel: PrivacyViewModel = viewModel(
        factory = PrivacyViewModel.Factory(repository, prefs, context)
    )

    val uiState by viewModel.uiState.collectAsState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    // ======= Permissions — رُفعت لمستوى Screen بعيداً عن Column =======
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val micGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.RECORD_AUDIO
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                val storageGranted =
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        androidx.core.content.ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.READ_MEDIA_IMAGES
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    } else {
                        androidx.core.content.ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    }

                viewModel.refreshPermissions(micGranted, storageGranted)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ======= One-shot Events =======
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PrivacyEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)

                is PrivacyEvent.OpenFile -> {
                    try {
                        val file = java.io.File(event.filePath)
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context, "${context.packageName}.provider", file
                        )
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "text/plain")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                        )
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Cannot open file")
                    }
                    viewModel.resetExportState()
                }

                is PrivacyEvent.OpenAppSettings -> {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    )
                }
            }
        }
    }

    // ======= Export Error Snackbar =======
    LaunchedEffect(uiState.exportState) {
        if (uiState.exportState is ExportState.Error) {
            viewModel.resetExportState()
        }
    }

    // ======= Delete Success Snackbar =======
    LaunchedEffect(uiState.deleteState) {
        if (uiState.deleteState is DeleteState.Success) {
            viewModel.resetDeleteState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.item_privacy_center),
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = if (isRtl) Icons.AutoMirrored.Filled.ArrowForward
                            else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            PrivacyHeaderCard(isRtl = isRtl)

            PrivacyScoreCard(
                scorePercent = uiState.privacyScorePercent,
                isRtl = isRtl
            )

            SectionHeader(title = stringResource(R.string.privacy_data_controls), isRtl = isRtl)

            PrivacyToggleItem(
                icon = Icons.Outlined.Psychology,
                title = stringResource(R.string.privacy_ai_processing),
                subtitle = stringResource(R.string.privacy_ai_processing_desc),
                checked = uiState.aiProcessingEnabled,
                onCheckedChange = viewModel::setAiProcessing,
                isRtl = isRtl
            )

            PrivacyToggleItem(
                icon = Icons.Outlined.Mic,
                title = stringResource(R.string.privacy_voice_storage),
                subtitle = stringResource(R.string.privacy_voice_storage_desc),
                checked = uiState.voiceStorageEnabled,
                onCheckedChange = viewModel::setVoiceStorage,
                isRtl = isRtl
            )

            PrivacyToggleItem(
                icon = Icons.Outlined.Analytics,
                title = stringResource(R.string.privacy_analytics),
                subtitle = stringResource(R.string.privacy_analytics_desc),
                checked = uiState.analyticsEnabled,
                onCheckedChange = viewModel::setAnalytics,
                isRtl = isRtl,
                isWarning = uiState.analyticsEnabled
            )

            SectionHeader(title = stringResource(R.string.privacy_your_data), isRtl = isRtl)

            PrivacyActionItem(
                icon = Icons.Outlined.FileDownload,
                title = stringResource(R.string.privacy_export_data),
                subtitle = stringResource(R.string.privacy_export_data_desc),
                actionLabel = stringResource(R.string.privacy_export_btn),
                actionColor = MaterialTheme.colorScheme.primary,
                isRtl = isRtl,
                isLoading = uiState.exportState is ExportState.Loading,
                onClick = viewModel::showExportDialog
            )

            PrivacyActionItem(
                icon = Icons.Outlined.DeleteForever,
                title = stringResource(R.string.privacy_delete_data),
                subtitle = stringResource(R.string.privacy_delete_data_desc),
                actionLabel = stringResource(R.string.privacy_delete_btn),
                actionColor = MaterialTheme.colorScheme.error,
                isRtl = isRtl,
                isLoading = uiState.deleteState is DeleteState.Loading,
                onClick = viewModel::showDeleteDialog
            )

            SectionHeader(title = stringResource(R.string.privacy_permissions), isRtl = isRtl)

            PermissionInfoItem(
                icon = Icons.Outlined.Mic,
                title = stringResource(R.string.permission_microphone),
                description = stringResource(R.string.permission_microphone_desc),
                isGranted = uiState.isMicGranted,
                isRtl = isRtl,
                onManage = viewModel::onManagePermission
            )

            PermissionInfoItem(
                icon = Icons.Outlined.Image,
                title = stringResource(R.string.permission_storage),
                description = stringResource(R.string.permission_storage_desc),
                isGranted = uiState.isStorageGranted,
                isRtl = isRtl,
                onManage = viewModel::onManagePermission
            )

            PrivacyPolicyCard(isRtl = isRtl, onClick = {
                navController.navigate(Route.Privacy.route)
            })

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // ======= Dialogs =======
    if (uiState.showDeleteDialog) {
        PrivacyDeleteDialog(
            isLoading = uiState.deleteState is DeleteState.Loading,
            onConfirm = {
                viewModel.hideDeleteDialog()
                viewModel.deleteAllData()
            },
            onDismiss = viewModel::hideDeleteDialog
        )
    }

    if (uiState.showExportDialog) {
        PrivacyExportDialog(
            isLoading = uiState.exportState is ExportState.Loading,
            onConfirm = {
                viewModel.hideExportDialog()
                viewModel.exportData()
            },
            onDismiss = viewModel::hideExportDialog
        )
    }
}

// ======= Header Card =======
@Composable
private fun PrivacyHeaderCard(isRtl: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val icon = @Composable {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Shield, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            val text = @Composable {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = if (isRtl) Alignment.End else Alignment.Start
                ) {
                    Text(
                        text = stringResource(R.string.privacy_center_title),
                        fontFamily = MansalvaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = if (isRtl) TextAlign.End else TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.privacy_center_subtitle),
                        fontFamily = ManropeFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        lineHeight = 20.sp,
                        textAlign = if (isRtl) TextAlign.End else TextAlign.Start
                    )
                }
            }
            if (isRtl) {
                text(); icon()
            } else {
                icon(); text()
            }
        }
    }
}

// ======= Score Card =======
@Composable
private fun PrivacyScoreCard(scorePercent: Float, isRtl: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = if (isRtl) Alignment.End else Alignment.Start
        ) {
            Text(
                text = stringResource(R.string.privacy_score),
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = if (isRtl) TextAlign.End else TextAlign.Start
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { scorePercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = when {
                    scorePercent >= 0.8f -> Color(0xFF4CAF50)
                    scorePercent >= 0.5f -> Color(0xFFFFC107)
                    else -> MaterialTheme.colorScheme.error
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when {
                    scorePercent >= 0.8f -> stringResource(R.string.privacy_score_high)
                    scorePercent >= 0.5f -> stringResource(R.string.privacy_score_medium)
                    else -> stringResource(R.string.privacy_score_low)
                },
                fontFamily = ManropeFontFamily,
                fontSize = 13.sp,
                color = when {
                    scorePercent >= 0.8f -> Color(0xFF2E7D32)
                    scorePercent >= 0.5f -> Color(0xFFF57F17)
                    else -> MaterialTheme.colorScheme.error
                },
                modifier = Modifier.fillMaxWidth(),
                textAlign = if (isRtl) TextAlign.End else TextAlign.Start
            )
        }
    }
}

// ======= Policy Card =======
@Composable
private fun PrivacyPolicyCard(isRtl: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val shieldIcon = @Composable {
                Icon(
                    Icons.Outlined.Shield, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            val arrowIcon = @Composable {
                Icon(
                    if (isRtl) Icons.AutoMirrored.Filled.ArrowBack
                    else Icons.AutoMirrored.Filled.ArrowForward,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            val text = @Composable {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = if (isRtl) Alignment.End else Alignment.Start
                ) {
                    Text(
                        stringResource(R.string.privacy_policy),
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = if (isRtl) TextAlign.End else TextAlign.Start
                    )
                    Text(
                        stringResource(R.string.privacy_policy_link_desc),
                        fontFamily = ManropeFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = if (isRtl) TextAlign.End else TextAlign.Start
                    )
                }
            }
            if (isRtl) {
                arrowIcon(); text(); shieldIcon()
            } else {
                shieldIcon(); text(); arrowIcon()
            }
        }
    }
}

// ======= Delete Dialog =======
@Composable
private fun PrivacyDeleteDialog(
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Outlined.Warning, null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                stringResource(R.string.privacy_delete_confirm_title),
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                stringResource(R.string.privacy_delete_confirm_desc),
                fontFamily = ManropeFontFamily,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                else Text(
                    stringResource(R.string.privacy_delete_btn),
                    fontFamily = ManropeFontFamily
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), fontFamily = ManropeFontFamily)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

// ======= Export Dialog =======
@Composable
private fun PrivacyExportDialog(
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Outlined.FileDownload, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                stringResource(R.string.privacy_export_confirm_title),
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                stringResource(R.string.privacy_export_confirm_desc),
                fontFamily = ManropeFontFamily,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                else Text(
                    stringResource(R.string.privacy_export_btn),
                    fontFamily = ManropeFontFamily
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), fontFamily = ManropeFontFamily)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

// ======= Section Header =======
@Composable
fun SectionHeader(title: String, isRtl: Boolean) {
    Text(
        text = title,
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth(),
        textAlign = if (isRtl) TextAlign.End else TextAlign.Start
    )
}

// ======= Toggle Item =======
@Composable
fun PrivacyToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isRtl: Boolean,
    isWarning: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWarning)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val iconBox = @Composable {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isWarning) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon, null,
                        tint = if (isWarning) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            val textCol = @Composable {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = if (isRtl) Alignment.End else Alignment.Start
                ) {
                    Text(
                        title, fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = if (isRtl) TextAlign.End else TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        subtitle, fontFamily = ManropeFontFamily,
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                        textAlign = if (isRtl) TextAlign.End else TextAlign.Start
                    )
                }
            }
            val switch = @Composable {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = if (isWarning) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                )
            }
            if (isRtl) {
                switch(); textCol(); iconBox()
            } else {
                iconBox(); textCol(); switch()
            }
        }
    }
}

// ======= Action Item =======
@Composable
fun PrivacyActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String,
    actionColor: Color,
    isRtl: Boolean,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val iconBox = @Composable {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(actionColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = actionColor, modifier = Modifier.size(20.dp))
                }
            }
            val textCol = @Composable {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = if (isRtl) Alignment.End else Alignment.Start
                ) {
                    Text(
                        title, fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = if (isRtl) TextAlign.End else TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        subtitle, fontFamily = ManropeFontFamily,
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                        textAlign = if (isRtl) TextAlign.End else TextAlign.Start
                    )
                }
            }
            val btn = @Composable {
                Button(
                    onClick = onClick,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = actionColor),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    if (isLoading) CircularProgressIndicator(
                        color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp
                    )
                    else Text(actionLabel, fontFamily = ManropeFontFamily, fontSize = 12.sp)
                }
            }
            if (isRtl) {
                btn(); textCol(); iconBox()
            } else {
                iconBox(); textCol(); btn()
            }
        }
    }
}

// ======= Permission Info Item =======
@Composable
fun PermissionInfoItem(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    isRtl: Boolean,
    onManage: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val iconBox = @Composable {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            val textCol = @Composable {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = if (isRtl) Alignment.End else Alignment.Start
                ) {
                    Text(
                        title, fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = if (isRtl) TextAlign.End else TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        description, fontFamily = ManropeFontFamily,
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                        textAlign = if (isRtl) TextAlign.End else TextAlign.Start
                    )
                }
            }
            val badgeAndBtn = @Composable {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (isGranted) Color(0xFF4CAF50).copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.errorContainer,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isGranted) stringResource(R.string.permission_granted)
                            else stringResource(R.string.permission_denied),
                            fontFamily = ManropeFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isGranted) Color(0xFF2E7D32)
                            else MaterialTheme.colorScheme.error
                        )
                    }
                    TextButton(
                        onClick = onManage,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            if (isRtl) "إدارة" else "Manage",
                            fontFamily = ManropeFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            if (isRtl) {
                badgeAndBtn(); textCol(); iconBox()
            } else {
                iconBox(); textCol(); badgeAndBtn()
            }
        }
    }
}