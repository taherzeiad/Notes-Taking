package com.example.notes_taking.Screens.presentations.Editor

import android.net.Uri
import java.util.UUID

sealed class ContentBlock {
    data class TextBlock(
        val id: String = UUID.randomUUID().toString(),
        val text: String = ""
    ) : ContentBlock()

    data class ImageBlock(
        val id: String = UUID.randomUUID().toString(),
        val uri: Uri
    ) : ContentBlock()

    data class AudioBlock(
        val id: String = UUID.randomUUID().toString(),
        val uri: Uri,
        val name: String,
        val filePath: String = uri.path ?: ""
    ) : ContentBlock()

    data class BulletBlock(
        val id: String = UUID.randomUUID().toString(),
        val text: String = ""
    ) : ContentBlock()

    data class LinkBlock(
        val id: String = UUID.randomUUID().toString(),
        val url: String = "",
        val description: String = ""
    ) : ContentBlock()
}