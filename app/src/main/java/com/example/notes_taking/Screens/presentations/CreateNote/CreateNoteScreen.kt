package com.example.notes_taking.Screens.presentations.CreateNote

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Spellcheck
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notes_taking.R
import com.example.notes_taking.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun CreateNoteScreen(noteId: Int, onBack: () -> Unit, viewModel: NoteViewModel) {

    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val currentDate = remember { sdf.format(Date()) }

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(currentDate) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val errorMsg by viewModel.errorMessage.collectAsState()

    // Undo / Redo stacks
    val undoStack = remember { mutableStateListOf<String>() }
    val redoStack = remember { mutableStateListOf<String>() }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(noteId) {
        if (noteId > 0) {
            val note = viewModel.getNoteById(noteId)

            note?.let {
                title = it.title
                content = it.content
                date = it.date
                selectedImageUri = it.imageUri?.let { uriString -> Uri.parse(uriString) }
            }
        }
    }

    // ======= Image Picker =======
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val localUri = saveImageToInternalStorage(context, it)
            selectedImageUri = localUri
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            // ======= Top Bar =======
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = if (noteId > 0) "Edit Note" else "Create Note",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = ManropeFontFamily,
                        color = TextPrimary
                    )
                }

                Row {
                    IconButton(
                        onClick = {
                            if (undoStack.isNotEmpty()) {
                                redoStack.add(content)
                                content = undoStack.removeAt(undoStack.size - 1)
                            }
                        }) {
                        Icon(
                            painter = painterResource(id = R.drawable.undo),
                            contentDescription = "Undo",
                            tint = if (undoStack.isNotEmpty()) TextPrimary else TextSecondary
                        )
                    }
                    IconButton(
                        onClick = {
                            if (redoStack.isNotEmpty()) {
                                undoStack.add(content)
                                content = redoStack.removeAt(redoStack.size - 1)
                            }
                        }) {
                        Icon(
                            painter = painterResource(id = R.drawable.redo),
                            contentDescription = "Redo",
                            tint = if (redoStack.isNotEmpty()) TextPrimary else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ======= Title Field =======
            BasicTextField_Title(
                value = title, onValueChange = { title = it })

            Spacer(modifier = Modifier.height(8.dp))

            // ======= Date =======
            Text(
                text = date,
                fontSize = 13.sp,
                fontFamily = ManropeFontFamily,
                color = TextSecondary,
                modifier = Modifier.padding(start = 15.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ======= Content + Image =======
            BasicTextField_Content(value = content, onValueChange = { newValue ->
                undoStack.add(content)
                redoStack.clear()
                content = newValue
            }, selectedImageUri = selectedImageUri, onImageClick = {
                imagePickerLauncher.launch("image/*")
            }, onRemoveImage = {
                selectedImageUri = null
            })
        }

        // ======= Grok Button =======
        var geminiMenuExpanded by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 88.dp)
        ) {
            IconButton(
                onClick = { geminiMenuExpanded = true }, modifier = Modifier.size(48.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.grok),
                    contentDescription = "Grok",
                    modifier = Modifier.size(36.dp)
                )
            }

            DropdownMenu(
                expanded = geminiMenuExpanded,
                onDismissRequest = { geminiMenuExpanded = false },
                modifier = Modifier
                    .background(Color.White)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                // ======= Rephrase =======
                DropdownMenuItem(text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF4285F4),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Rephrase Text",
                            fontFamily = ManropeFontFamily,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                }, onClick = {
                    geminiMenuExpanded = false
                    if (content.isNotBlank()) {
                        viewModel.rephrase(content) { newText ->
                            undoStack.add(content)
                            content = newText
                        }
                    }
                })

                HorizontalDivider(color = Color(0xFFF0F0F0))

                // ======= Diacritize =======
                DropdownMenuItem(text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Spellcheck,
                            contentDescription = null,
                            tint = Color(0xFF4285F4),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Diacritize Text",
                            fontFamily = ManropeFontFamily,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                }, onClick = {
                    geminiMenuExpanded = false
                    if (content.isNotBlank()) {
                        viewModel.diacritize(content) { newText ->
                            undoStack.add(content)
                            content = newText
                        }
                    }
                })
            }
        }
        // ======= Loading =======
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4285F4))
                        Text(
                            text = "AI is thinking...",
                            fontFamily = ManropeFontFamily,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // ======= Error =======
        errorMsg?.let { msg ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp), action = {
                    TextButton(onClick = { viewModel.clearError() }) { // دالة لتصفير الخطأ في الـ ViewModel
                        Text("OK", color = Color.White)
                    }
                }) {
                Text(text = msg, fontFamily = ManropeFontFamily)
            }
        }
        // ======= تحديث زر الحفظ =======
        val isSaveEnabled = title.isNotBlank() && (content.isNotBlank() || selectedImageUri != null)
        // ======= Save Button =======
        Button(
            onClick = {
                if (noteId > 0) {
                    viewModel.updateNote(
                        id = noteId,
                        title = title,
                        content = content,
                        imageUri = selectedImageUri?.toString(),
                        date = date,
                        onSuccess = onBack
                    )
                } else {
                    viewModel.saveNote(
                        title = title,
                        content = content,
                        imageUri = selectedImageUri?.toString(),
                        date = date,
                        onSuccess = onBack
                    )
                }
            },
            enabled = isSaveEnabled && !viewModel.isLoading,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FabColor, contentColor = Color(0xFFFFFFFF),

                disabledContainerColor = Color(0x8F4A3728), disabledContentColor = Color(0xFFFFFFFF)
            )
        ) {
            Text(
                text = "Save Note",
                fontSize = 16.sp,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Medium
            )
        }

    }
}

// ======= Title TextField =======
@Composable
fun BasicTextField_Title(value: String, onValueChange: (String) -> Unit) {
    TextField(
        value = value, onValueChange = onValueChange, placeholder = {
            Text(
                text = "Title",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ManropeFontFamily,
                color = Color(0xFFCCCCCC)
            )
        }, textStyle = TextStyle(
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ManropeFontFamily,
            color = TextPrimary
        ), colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent
        ), modifier = Modifier.fillMaxWidth()
    )
}

// ======= Content TextField =======
@Composable
fun BasicTextField_Content(
    value: String,
    onValueChange: (String) -> Unit,
    selectedImageUri: Uri?,        // ← استقبال الصورة
    onImageClick: () -> Unit,      // ← فتح المعرض
    onRemoveImage: () -> Unit      // ← حذف الصورة
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(start = 16.dp, top = 4.dp)
    ) {
        // ======= عرض الصورة إذا موجودة =======
        if (selectedImageUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(bottom = 12.dp)
            ) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Selected Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )
                // زر حذف الصورة
                IconButton(
                    onClick = onRemoveImage,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(28.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Image",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // ======= حقل النص =======
        BasicTextField(
            value = value, onValueChange = onValueChange, textStyle = TextStyle(
                fontSize = 15.sp,
                fontFamily = ManropeFontFamily,
                color = TextPrimary,
                lineHeight = 22.sp
            ), modifier = Modifier.fillMaxWidth(), decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty() && selectedImageUri == null) {
                        Column {
                            Text(
                                text = "Note something down or click on image to",
                                fontSize = 15.sp,
                                fontFamily = ManropeFontFamily,
                                color = Color(0xFFCCCCCC)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "upload image ",
                                    fontSize = 15.sp,
                                    fontFamily = ManropeFontFamily,
                                    color = Color(0xFFCCCCCC)
                                )
                                // ← أيقونة الصورة قابلة للضغط
                                Icon(
                                    imageVector = Icons.Outlined.Image,
                                    contentDescription = null,
                                    tint = Color(0xFFCCCCCC),
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { onImageClick() })
                            }
                        }
                    }
                    innerTextField()
                }
            })
    }
}