package com.example.geminiaichatbot.chatui

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geminiaichatbot.BuildConfig
import com.example.geminiaichatbot.models.SuggestionsModel
import com.example.geminiaichatbot.models.availableSuggestions
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {


    private val _uiState: MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState.Initial)
    val uiState = _uiState.asStateFlow()

    var enable = false

    private var generativeModel: GenerativeModel
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

    // suggestion implementation
    var inputMessage by mutableStateOf("")
        private set

    fun settingInputMessage(input: String) {
        inputMessage = input
    }

    private var coroutineScope = CoroutineScope(Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    val suggestions: StateFlow<List<SuggestionsModel>> =
        snapshotFlow { inputMessage }.filter { it.isNotEmpty() }.mapLatest { fetchSuggestions(it) }
            .stateIn(
                coroutineScope,
                SharingStarted.Lazily, emptyList()
            )

    private fun fetchSuggestions(query: String): List<SuggestionsModel> {
// we can make api call here or we can get data from repository
        return availableSuggestions.filter { it.text.contains(query, true) }
    }


    //////////
    var id = -1
    fun questioning(userInput: String, selectedImages: List<Bitmap>) {

        _uiState.value = HomeUiState.Loading

        enable = true

        val prompt = "Take a look at images and answer the following questions $userInput"


        viewModelScope.launch(Dispatchers.IO) {
            try {
                val content = content {
                    for (bitmap in selectedImages) {
                        image(bitmap)
                    }
                    text(prompt)
                }
                id++
                var outputText = ""

                generativeModel.generateContentStream(content).buffer().collect {
                    if (enable) {
                        _uiState.value = HomeUiState.RemoveLoading
                        enable = false
                        Handler(Looper.getMainLooper()).postDelayed({
                            outputText += it.text ?: ""

                        }, 3000)
                    } else {
                        outputText += it.text ?: ""
                        Log.d("GenerativeStream", it.text.toString())
                        _uiState.value = HomeUiState.Success(outputText,id)
                        delay(50)
                    }

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
    object RemoveLoading : HomeUiState
    data class Success(val outputText: String, var id: Int) : HomeUiState
    data class Failure(val errorText: String) : HomeUiState
}