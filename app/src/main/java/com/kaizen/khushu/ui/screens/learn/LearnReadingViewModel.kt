package com.kaizen.khushu.ui.screens.learn

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kaizen.khushu.data.model.ContentBlock
import com.kaizen.khushu.data.repository.LearnRepository
import com.kaizen.khushu.data.repository.TranslationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LearnReadingViewModel(application: Application) : AndroidViewModel(application) {

    private val _blocks = MutableStateFlow<List<ContentBlock>?>(null)
    val blocks: StateFlow<List<ContentBlock>?> = _blocks

    val translationMap = androidx.compose.runtime.mutableStateOf<Map<String, String>>(emptyMap())
    val downloadProgress = androidx.compose.runtime.mutableFloatStateOf(0f)
    val isDownloading = androidx.compose.runtime.mutableStateOf(false)

    fun loadBlocks(topicId: String) {
        if (_blocks.value != null) return // already loaded
        viewModelScope.launch(Dispatchers.IO) {
            _blocks.value = LearnRepository.getBlocks(topicId, getApplication())
        }
    }

    fun loadTranslation(context: android.content.Context, translationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val map = TranslationRepository.load(context, translationId)
            withContext(Dispatchers.Main) {
                translationMap.value = map
            }
        }
    }

    fun downloadTranslation(context: android.content.Context, meta: com.kaizen.khushu.data.model.TranslationMeta, onDone: () -> Unit) {
        if (isDownloading.value) return
        isDownloading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            TranslationRepository.download(context, meta.id, meta.downloadUrl) { progress ->
                downloadProgress.floatValue = progress
            }
            val map = TranslationRepository.load(context, meta.id)
            withContext(Dispatchers.Main) {
                translationMap.value = map
                isDownloading.value = false
                downloadProgress.floatValue = 0f
                onDone()
            }
        }
    }
}
