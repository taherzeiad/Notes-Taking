package com.example.myuniqueapp.Screens.presentations.PrivacyCenter

data class PrivacyUiState(
    // ======= Settings =======
    val aiProcessingEnabled: Boolean = true,
    val voiceStorageEnabled: Boolean = true,
    val analyticsEnabled: Boolean = false,

    // ======= Permissions =======
    val isMicGranted: Boolean = false,
    val isStorageGranted: Boolean = false,

    // ======= Operations =======
    val exportState: ExportState = ExportState.Idle,
    val deleteState: DeleteState = DeleteState.Idle,

    // ======= Dialogs =======
    val showDeleteDialog: Boolean = false,
    val showExportDialog: Boolean = false,
) {
    // computed property — لا تحسبها في الـ UI
    val privacyScorePercent: Float
        get() = listOf(
            aiProcessingEnabled, voiceStorageEnabled, !analyticsEnabled
        ).count { it } / 3f
}

// ======= One-shot Events =======
sealed class PrivacyEvent {
    data class ShowSnackbar(val message: String) : PrivacyEvent()
    data class OpenFile(val filePath: String) : PrivacyEvent()
    data object OpenAppSettings : PrivacyEvent()
}

// ======= Export State =======
sealed class ExportState {
    object Idle : ExportState()
    object Loading : ExportState()
    data class Success(val filePath: String) : ExportState()
    data class Error(val message: String) : ExportState()
}

// ======= Delete State =======
sealed class DeleteState {
    object Idle : DeleteState()
    object Loading : DeleteState()
    object Success : DeleteState()
    data class Error(val message: String) : DeleteState()
}