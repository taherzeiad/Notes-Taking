import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OnboardingViewModel(
    // private val dataStoreRepository: DataStoreRepository // اختياري لحفظ الحالة
) : ViewModel() {

    // إدارة الصفحة الحالية
    var currentPage by mutableStateOf(0)
        private set

    // إدارة حالة التحميل (Loading)
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
        isLoading = true
        viewModelScope.launch {
            // هنا نقوم بحفظ أن المستخدم أتم الـ Onboarding
            // dataStoreRepository.saveOnboardingCompleted() 

            delay(1500) // محاكاة لعملية الحفظ أو التحميل
            onFinish()
        }
    }
}