package com.kaizen.khushu.ui.screens.hadith

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kaizen.khushu.data.model.*
import com.kaizen.khushu.data.repository.HadithRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*

class HadithViewModel(application: Application) : AndroidViewModel(application) {
    val sections = mutableStateOf<List<HadithSection>>(emptyList())
    val currentHadiths = mutableStateOf<List<ContentBlock>>(emptyList())
    val isLoading = mutableStateOf(false)

    fun loadSections(bookId: String) {
        isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val list = HadithRepository.getSections(getApplication(), bookId)
            withContext(Dispatchers.Main) {
                sections.value = list
                isLoading.value = false
            }
        }
    }

    fun loadHadiths(bookId: String, sectionNumber: Int, bookName: String) {
        isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val rawList = HadithRepository.getHadiths(getApplication(), bookId, sectionNumber)
            val arabicBook = HadithRepository.getArabicBook(getApplication(), bookId)
            val blocks = rawList.map { obj ->
                val num = obj["hadithnumber"]?.jsonPrimitive?.intOrNull ?: 0
                val text = obj["text"]?.jsonPrimitive?.content ?: ""
                val arabic = arabicBook[num] ?: obj["textArabic"]?.jsonPrimitive?.content
                
                // Extract grade
                val grades = obj["grades"]?.jsonArray ?: JsonArray(emptyList())
                val grade = if (grades.isNotEmpty()) {
                    grades[0].jsonObject["grade"]?.jsonPrimitive?.content
                } else if (bookId == "bukhari" || bookId == "muslim") {
                    "Sahih"
                } else null

                HadithBlock(
                    collection = bookId,
                    number = num,
                    display = "$bookName $num",
                    textEn = text,
                    textArabic = arabic,
                    grade = grade,
                    verified = true
                )
            }
            withContext(Dispatchers.Main) {
                currentHadiths.value = blocks
                isLoading.value = false
            }
        }
    }
}
