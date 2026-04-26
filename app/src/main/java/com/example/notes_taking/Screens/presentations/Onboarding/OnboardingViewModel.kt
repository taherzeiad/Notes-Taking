import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OnboardingViewModel : ViewModel() {

    var currentPage by mutableStateOf(0)
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun nextPage(isLastPage: Boolean, onFinish: () -> Unit) {
        if (!isLastPage) {
            currentPage++
        } else {
            startLoading(onFinish)
        }
    }

    fun skip(onFinish: () -> Unit) {
        startLoading(onFinish)
    }

    private fun startLoading(onFinish: () -> Unit) {
        if (isLoading) return
        isLoading = true
        viewModelScope.launch {
            delay(1500)
            onFinish()
        }
    }
}