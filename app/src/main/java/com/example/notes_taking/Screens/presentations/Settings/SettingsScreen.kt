package com.example.notes_taking.Screens.presentations.Settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notes_taking.R
import com.example.notes_taking.Screens.presentations.Home.BottomNavBar
import com.example.notes_taking.ui.theme.*

@Composable
fun SettingsScreen(onBack: () -> Unit = {}) {
    var darkModeEnabled by remember { mutableStateOf(false) }

    // ← جلب اتجاه اللغة الحالية
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    // ← تحديد المحاذاة حسب الاتجاه
    val textAlign = if (isRtl) TextAlign.End else TextAlign.Start
    val horizontalAlignment = if (isRtl) Alignment.End else Alignment.Start
    val horizontalArrangement = if (isRtl) Arrangement.End else Arrangement.Start

    Scaffold(
        containerColor = PageBackground,
        bottomBar = {
            BottomNavBar(
                selectedTab = 0,
                onNavigate = { index ->
                    when (index) {
                        3 -> onBack()
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ======= Top Bar =======
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ← الأيقونة تتبدل حسب الاتجاه
                    if (isRtl) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BrownCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Text(
                        text = "Intellectual Sanctuary",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = ManropeFontFamily,
                        color = TextPrimary
                    )

                    if (isRtl) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BrownCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // ======= Page Title =======
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = horizontalAlignment // ← ديناميكي
                ) {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MansalvaFontFamily,
                        color = TextPrimary,
                        textAlign = textAlign
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.settings_subtitle),
                        fontSize = 14.sp,
                        fontFamily = ManropeFontFamily,
                        color = TextSecondary,
                        textAlign = textAlign
                    )
                }
            }

            // ======= Profile Card =======
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ← ترتيب العناصر حسب الاتجاه
                        if (isRtl) {
                            // زر تعديل على اليسار
                            Box(
                                modifier = Modifier
                                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.btn_edit),
                                    fontSize = 14.sp,
                                    fontFamily = ManropeFontFamily,
                                    color = TextPrimary
                                )
                            }

                            // المعلومات في الوسط
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Taher Qudeih",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = MansalvaFontFamily,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "ahmed@sanctuary.io",
                                    fontSize = 13.sp,
                                    fontFamily = ManropeFontFamily,
                                    color = TextSecondary
                                )
                            }

                            // الصورة على اليمين
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(BrownCard),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        } else {
                            // الصورة على اليسار
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(BrownCard),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            // المعلومات في الوسط
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = "Taher Qudeih",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = MansalvaFontFamily,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "ahmed@sanctuary.io",
                                    fontSize = 13.sp,
                                    fontFamily = ManropeFontFamily,
                                    color = TextSecondary
                                )
                            }

                            // زر تعديل على اليمين
                            Box(
                                modifier = Modifier
                                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.btn_edit),
                                    fontSize = 14.sp,
                                    fontFamily = ManropeFontFamily,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // ======= الحساب والأمان =======
            item {
                SettingsSection(title = stringResource(R.string.section_account), isRtl = isRtl) {
                    SettingsItem(label = stringResource(R.string.item_account_info), icon = Icons.Outlined.Person, isRtl = isRtl, onClick = {})
                    HorizontalDivider(color = CardBorder, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(label = stringResource(R.string.item_security), icon = Icons.Outlined.Lock, isRtl = isRtl, onClick = {})
                }
            }

            // ======= التخصيص =======
            item {
                SettingsSection(title = stringResource(R.string.section_customization), isRtl = isRtl) {
                    SettingsItemWithToggle(
                        label = stringResource(R.string.item_dark_mode),
                        subLabel = stringResource(R.string.sub_dark_mode),
                        icon = Icons.Outlined.DarkMode,
                        checked = darkModeEnabled,
                        onCheckedChange = { darkModeEnabled = it },
                        isRtl = isRtl
                    )
                    HorizontalDivider(color = CardBorder, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(label = stringResource(R.string.item_notifications), icon = Icons.Outlined.Notifications, isRtl = isRtl, onClick = {})
                }
            }

            // ======= الخصوصية والمعلومات =======
            item {
                SettingsSection(title = stringResource(R.string.section_privacy), isRtl = isRtl) {
                    SettingsItem(label = stringResource(R.string.item_privacy_center), icon = Icons.Outlined.Shield, isRtl = isRtl, onClick = {})
                    HorizontalDivider(color = CardBorder, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(label = stringResource(R.string.item_about), icon = Icons.Outlined.Info, isRtl = isRtl, onClick = {})
                }
            }

            // ======= زر تسجيل الخروج =======
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.3f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isRtl) {
                            Text(text = stringResource(R.string.btn_logout), fontSize = 16.sp, fontFamily = ManropeFontFamily, fontWeight = FontWeight.SemiBold, color = DangerRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(imageVector = Icons.AutoMirrored.Outlined.Logout, contentDescription = null, tint = DangerRed, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(imageVector = Icons.AutoMirrored.Outlined.Logout, contentDescription = null, tint = DangerRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.btn_logout), fontSize = 16.sp, fontFamily = ManropeFontFamily, fontWeight = FontWeight.SemiBold, color = DangerRed)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// ======= Settings Section =======
@Composable
fun SettingsSection(
    title: String,
    isRtl: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontFamily = ManropeFontFamily,
            color = SectionTitle,
            textAlign = if (isRtl) TextAlign.End else TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(content = content)
        }
    }
}

// ======= Settings Item =======
@Composable
fun SettingsItem(
    label: String,
    icon: ImageVector,
    isRtl: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isRtl) {
            // ← سهم يسار | نص | أيقونة يمين
            Icon(imageVector = Icons.Outlined.ChevronLeft, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            Text(text = label, fontSize = 15.sp, fontFamily = ManropeFontFamily, color = TextPrimary, fontWeight = FontWeight.Medium)
            Box(modifier = Modifier.size(36.dp).background(IconBg, CircleShape), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = BrownCard, modifier = Modifier.size(18.dp))
            }
        } else {
            // ← أيقونة يسار | نص | سهم يمين
            Box(modifier = Modifier.size(36.dp).background(IconBg, CircleShape), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = BrownCard, modifier = Modifier.size(18.dp))
            }
            Text(text = label, fontSize = 15.sp, fontFamily = ManropeFontFamily, color = TextPrimary, fontWeight = FontWeight.Medium)
            Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
    }
}

// ======= Settings Item With Toggle =======
@Composable
fun SettingsItemWithToggle(
    label: String,
    subLabel: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isRtl: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isRtl) {
            // Toggle يسار | نص | أيقونة يمين
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = BrownCard,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFD0C8C0)
                )
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(text = label, fontSize = 15.sp, fontFamily = ManropeFontFamily, color = TextPrimary, fontWeight = FontWeight.Medium)
                Text(text = subLabel, fontSize = 12.sp, fontFamily = ManropeFontFamily, color = TextSecondary)
            }
            Box(modifier = Modifier.size(36.dp).background(IconBg, CircleShape), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = BrownCard, modifier = Modifier.size(18.dp))
            }
        } else {
            // أيقونة يسار | نص | Toggle يمين
            Box(modifier = Modifier.size(36.dp).background(IconBg, CircleShape), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = BrownCard, modifier = Modifier.size(18.dp))
            }
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(text = label, fontSize = 15.sp, fontFamily = ManropeFontFamily, color = TextPrimary, fontWeight = FontWeight.Medium)
                Text(text = subLabel, fontSize = 12.sp, fontFamily = ManropeFontFamily, color = TextSecondary)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = BrownCard,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFD0C8C0)
                )
            )
        }
    }
}