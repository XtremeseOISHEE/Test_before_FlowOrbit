package com.example.floworbit.presentation.focus



import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.floworbit.presentation.dnd.DNDViewModel

class FocusTimerViewModelFactory(
    private val app: Application,
    private val dndViewModel: DNDViewModel
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FocusTimerViewModel::class.java)) {
            return FocusTimerViewModel(app, dndViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
