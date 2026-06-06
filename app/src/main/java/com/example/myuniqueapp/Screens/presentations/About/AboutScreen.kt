package com.example.myuniqueapp.Screens.presentations.About

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myuniqueapp.Navmain.Route
import com.example.myuniqueapp.ui.theme.ManropeFontFamily
import com.example.myuniqueapp.ui.theme.MansalvaFontFamily
import com.notestalking.myuniqueapp.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavHostController) {

    val context = LocalContext.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val viewModel = viewModel<AboutViewModel>()
    val snackbarHost = remember { SnackbarHostState() }

    // ======= معالجة الأحداث =======
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {

                is AboutEvent.OpenUrl -> {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                        )
                    }.onFailure {
                        // نُعلم المستخدم بدل الصمت
                        snackbarHost.showSnackbar(
                            if (isRtl) "لا يوجد تطبيق لفتح الرابط"
                            else "No app found to open the link"
                        )
                    }
                }

                is AboutEvent.SendEmail -> {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${event.email}")
                                putExtra(
                                    Intent.EXTRA_SUBJECT, "Support Request: Notes Taking App"
                                )
                            })
                    }.onFailure {
                        snackbarHost.showSnackbar(
                            if (isRtl) "لا يوجد تطبيق بريد مثبّت"
                            else "No email app found"
                        )
                    }
                }

                is AboutEvent.ShowSnackbar -> snackbarHost.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) }, topBar = {
        TopAppBar(
            title = {
            Text(
                stringResource(R.string.about_app),
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }, navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
        )
    }, containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ======= Logo =======
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ======= Name & Version =======
            Text(
                text = stringResource(R.string.intellectual_sanctuary),
                fontSize = 28.sp,
                fontFamily = MansalvaFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.app_version),
                fontSize = 14.sp,
                fontFamily = ManropeFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ======= Description =======
            Text(
                text = stringResource(R.string.app_description),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontFamily = ManropeFontFamily,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ======= Links =======
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AboutLinkItem(
                    icon = Icons.Default.Language,
                    label = stringResource(R.string.official_website),
                    isRtl = isRtl,
                    onClick = viewModel::onWebsiteClick
                )
                AboutLinkItem(
                    icon = Icons.Default.Mail,
                    label = stringResource(R.string.technical_support),
                    isRtl = isRtl,
                    onClick = viewModel::onSupportClick
                )
                AboutLinkItem(
                    icon = Icons.Default.PrivacyTip,
                    label = stringResource(R.string.privacy_policy),
                    isRtl = isRtl,
                    onClick = { navController.navigate(Route.Privacy.route) })
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // ======= Footer =======
            Text(
                text = stringResource(R.string.made_with_love),
                fontSize = 12.sp,
                fontFamily = ManropeFontFamily,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

// ======= Link Item =======
@Composable
fun AboutLinkItem(
    icon: ImageVector, label: String, isRtl: Boolean = false, onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val iconView = @Composable {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            val labelView = @Composable {
                Text(
                    text = label,
                    fontFamily = ManropeFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    textAlign = if (isRtl) TextAlign.End else TextAlign.Start
                )
            }

            if (isRtl) {
                labelView(); iconView()
            } else {
                iconView(); labelView()
            }
        }
    }
}