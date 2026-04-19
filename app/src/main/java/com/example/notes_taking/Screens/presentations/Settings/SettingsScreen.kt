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
import androidx.navigation.NavHostController
import com.example.notes_taking.R
import com.example.notes_taking.Screens.presentations.Home.BottomNavBar
import com.example.notes_taking.ui.theme.*

@Composable
fun SettingsScreen(navController: NavHostController) {
    var darkModeEnabled by remember { mutableStateOf(false) }

    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    val textAlign = if (isRtl) TextAlign.End else TextAlign.Start
    val horizontalAlignment = if (isRtl) Alignment.End else Alignment.Start

    Scaffold(
        containerColor = PageBackground,
        bottomBar = {
            // التعديل هنا: تمرير الـ navController والتبويب الصحيح (0 للإعدادات)
            BottomNavBar(
                navController = navController,
                selectedTab = 0
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
                    if (isRtl) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    } else {
                        ProfileAvatar()
                    }

                    Text(
                        text = stringResource(R.string.app_name_styled),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = ManropeFontFamily,
                        color = TextPrimary
                    )

                    if (isRtl) {
                        ProfileAvatar()
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
                    horizontalAlignment = horizontalAlignment
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
                        if (isRtl) {
                            EditButton()
                            ProfileInfo(
                                name = "Taher Qudeih",
                                email = "taher@sanctuary.io",
                                isRtl = true
                            )
                            ProfileAvatar(size = 64.dp, iconSize = 36.dp)
                        } else {
                            ProfileAvatar(size = 64.dp, iconSize = 36.dp)
                            ProfileInfo(
                                name = "Taher Qudeih",
                                email = "taher@sanctuary.io",
                                isRtl = false
                            )
                            EditButton()
                        }
                    }
                }
            }

            // ======= Sections =======
            item {
                SettingsSection(title = stringResource(R.string.section_account), isRtl = isRtl) {
                    SettingsItem(
                        label = stringResource(R.string.item_account_info),
                        icon = Icons.Outlined.Person,
                        isRtl = isRtl,
                        onClick = {})
                    HorizontalDivider(
                        color = CardBorder,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    SettingsItem(
                        label = stringResource(R.string.item_security),
                        icon = Icons.Outlined.Lock,
                        isRtl = isRtl,
                        onClick = {})
                }
            }

            item {
                SettingsSection(
                    title = stringResource(R.string.section_customization),
                    isRtl = isRtl
                ) {
                    SettingsItemWithToggle(
                        label = stringResource(R.string.item_dark_mode),
                        subLabel = stringResource(R.string.sub_dark_mode),
                        icon = Icons.Outlined.DarkMode,
                        checked = darkModeEnabled,
                        onCheckedChange = { darkModeEnabled = it },
                        isRtl = isRtl
                    )
                    HorizontalDivider(
                        color = CardBorder,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    SettingsItem(
                        label = stringResource(R.string.item_notifications),
                        icon = Icons.Outlined.Notifications,
                        isRtl = isRtl,
                        onClick = {})
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.section_privacy), isRtl = isRtl) {
                    SettingsItem(
                        label = stringResource(R.string.item_privacy_center),
                        icon = Icons.Outlined.Shield,
                        isRtl = isRtl,
                        onClick = {})
                    HorizontalDivider(
                        color = CardBorder,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    SettingsItem(
                        label = stringResource(R.string.item_about),
                        icon = Icons.Outlined.Info,
                        isRtl = isRtl,
                        onClick = {})
                }
            }

            // ======= Logout =======
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        DangerRed.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isRtl) {
                            Text(
                                text = stringResource(R.string.btn_logout),
                                fontSize = 16.sp,
                                fontFamily = ManropeFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = DangerRed
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.btn_logout),
                                fontSize = 16.sp,
                                fontFamily = ManropeFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = DangerRed
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// ======= Helper UI Components =======

@Composable
fun ProfileAvatar(
    size: androidx.compose.ui.unit.Dp = 40.dp,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(BrownCard),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun ProfileInfo(name: String, email: String, isRtl: Boolean) {
    Column(horizontalAlignment = if (isRtl) Alignment.End else Alignment.Start) {
        Text(
            text = name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MansalvaFontFamily,
            color = TextPrimary
        )
        Text(text = email, fontSize = 13.sp, fontFamily = ManropeFontFamily, color = TextSecondary)
    }
}

@Composable
fun EditButton() {
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

@Composable
fun SettingsSection(title: String, isRtl: Boolean, content: @Composable ColumnScope.() -> Unit) {
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

@Composable
fun SettingsItem(label: String, icon: ImageVector, isRtl: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isRtl) {
            // ← سهم يسار
            Icon(
                imageVector = Icons.Outlined.ChevronLeft,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                fontSize = 15.sp,
                fontFamily = ManropeFontFamily,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End
            )
            SettingsIconBox(icon)
        } else {
            SettingsIconBox(icon)
            Text(
                text = label,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                fontSize = 15.sp,
                fontFamily = ManropeFontFamily,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start
            )
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingsIconBox(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(IconBg, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BrownCard,
            modifier = Modifier.size(18.dp)
        )
    }
}

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
            Switch(checked = checked, onCheckedChange = onCheckedChange, colors = switchColors())
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontFamily = ManropeFontFamily,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subLabel,
                    fontSize = 12.sp,
                    fontFamily = ManropeFontFamily,
                    color = TextSecondary
                )
            }
            SettingsIconBox(icon)
        } else {
            SettingsIconBox(icon)
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontFamily = ManropeFontFamily,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subLabel,
                    fontSize = 12.sp,
                    fontFamily = ManropeFontFamily,
                    color = TextSecondary
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange, colors = switchColors())
        }
    }
}

@Composable
fun switchColors() = SwitchDefaults.colors(
    checkedThumbColor = Color.White,
    checkedTrackColor = BrownCard,
    uncheckedThumbColor = Color.White,
    uncheckedTrackColor = Color(0xFFD0C8C0)
)