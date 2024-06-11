package com.example.geminiaichatbot


import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope


import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {


    private val _uiState: MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState.Initial)
    val uiState = _uiState.asStateFlow()

    private lateinit var generativeModel: GenerativeModel

    init {
        val config = generationConfig {
            temperature = 0.70f // 0 to 1
        }

        generativeModel = GenerativeModel(
            modelName = "gemini-1.5-pro-latest",
            apiKey = BuildConfig.apiKey,
            generationConfig = config
        )
    }


    fun questioning(userInput: String, selectedImages: List<Bitmap>) {
        _uiState.value = HomeUiState.Loading
        val prompt = "Take a look at images and answer the following questions $userInput"

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val content = content {
                    for (bitmap in selectedImages) {
                        image(bitmap)
                    }
                    text(prompt)
                }

                var outputText = ""
                generativeModel.generateContentStream(content).collect {
                    outputText += it.text ?: ""
                    _uiState.value = HomeUiState.Success(outputText)
                }

            } catch (e: Exception) {
                _uiState.value = HomeUiState.Failure(e.localizedMessage ?: "")
            }
        }


    }

}

sealed interface HomeUiState {
    object Initial : HomeUiState
    object Loading : HomeUiState
    data class Success(val outputText: String) : HomeUiState
    data class Failure(val errorText: String) : HomeUiState
}