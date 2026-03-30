package com.example.notes_taking.Screens.presentations.CreateNote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun CreateNoteScreen(onBack: () -> Unit) {

    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val currentDate = remember { sdf.format(Date()) }

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(currentDate) }

    // Undo / Redo stacks
    val undoStack = remember { mutableStateListOf<String>() }
    val redoStack = remember { mutableStateListOf<String>() }

    val context = LocalContext.current

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
                        text = "Create Note",
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

            // ======= Content Field =======
            BasicTextField_Content(
                value = content, onValueChange = { newValue ->
                    undoStack.add(content)
                    redoStack.clear()
                    content = newValue
                })
        }

        // ======= Save Button =======
        Button(
            onClick = { /* حفظ النوتة */ },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF0E6DF)
            )
        ) {
            Text(
                text = "Save Note",
                fontSize = 16.sp,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
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
fun BasicTextField_Content(value: String, onValueChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        if (value.isEmpty()) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            ) {
                Text(
                    text = "Note something down or click on image to upload image",
                    fontSize = 15.sp,
                    fontFamily = ManropeFontFamily,
                    color = Color(0xFFCCCCCC),
                )
                Icon(
                    painter = painterResource(id = R.drawable.imageicon),
                    contentDescription = null,
                    tint = Color(0xFFCCCCCC),
                    modifier = Modifier
                        .size(20.dp)
                        .padding(top = 2.dp)
                )
            }
        }

        TextField(
            value = value, onValueChange = onValueChange, textStyle = TextStyle(
                fontSize = 15.sp,
                fontFamily = ManropeFontFamily,
                color = TextPrimary,
                lineHeight = 22.sp
            ), colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            ), modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(), maxLines = Int.MAX_VALUE
        )
    }
}