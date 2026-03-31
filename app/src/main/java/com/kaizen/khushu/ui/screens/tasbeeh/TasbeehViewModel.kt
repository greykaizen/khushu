package com.kaizen.khushu.ui.screens.tasbeeh

import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaizen.khushu.data.DhikrItem
import com.kaizen.khushu.data.TasbeehCollection
import com.kaizen.khushu.data.TasbeehDao
import kotlinx.coroutines.launch

class TasbeehViewModel(private val dao: TasbeehDao) : ViewModel() {

    val collections = dao.getAll()

    init {
        viewModelScope.launch {
            if (dao.count() == 0) seedDefaults()
            val currentCount = dao.count()
            if (currentCount < 12) {
                for (i in 1..(12 - currentCount)) {
                    dao.insert(
                        TasbeehCollection(
                            title = "Dummy Card $i",
                            colorInt = TasbeehPastelColors[i % TasbeehPastelColors.size].toArgb(),
                            items = listOf(DhikrItem("Test Dhikr", 33))
                        )
                    )
                }
            }
        }
    }

    suspend fun insert(collection: TasbeehCollection) = dao.insert(collection)

    fun delete(collection: TasbeehCollection) {
        viewModelScope.launch { dao.delete(collection) }
    }

    private suspend fun seedDefaults() {
        val seeds = listOf(
            TasbeehCollection(
                title = "After Salah",
                colorInt = TasbeehPastelColors[0].toArgb(),
                items = listOf(
                    DhikrItem("Subhan Allah", 33),
                    DhikrItem("Alhamdulillah", 33),
                    DhikrItem("Allahu Akbar", 34),
                ),
            ),
            TasbeehCollection(
                title = "Morning Adhkar",
                colorInt = TasbeehPastelColors[1].toArgb(),
                items = listOf(
                    DhikrItem("Ayatul Kursi", 1),
                    DhikrItem("Surah Al-Ikhlas", 3),
                    DhikrItem("Surah Al-Falaq", 3),
                    DhikrItem("Surah An-Nas", 3),
                ),
            ),
            TasbeehCollection(
                title = "Evening Adhkar",
                colorInt = TasbeehPastelColors[2].toArgb(),
                items = listOf(
                    DhikrItem("Ayatul Kursi", 1),
                    DhikrItem("Surah Al-Ikhlas", 3),
                    DhikrItem("Surah Al-Falaq", 3),
                    DhikrItem("Surah An-Nas", 3),
                ),
            ),
            TasbeehCollection(
                title = "Istighfar",
                colorInt = TasbeehPastelColors[3].toArgb(),
                items = listOf(
                    DhikrItem("Astaghfirullah", 100),
                ),
            ),
            TasbeehCollection(
                title = "Salawat",
                colorInt = TasbeehPastelColors[4].toArgb(),
                items = listOf(
                    DhikrItem("Allahumma Salli 'ala Muhammad", 100),
                ),
            ),
            TasbeehCollection(
                title = "La ilaha illallah",
                colorInt = TasbeehPastelColors[5].toArgb(),
                items = listOf(
                    DhikrItem("La ilaha illallah", 100),
                ),
            ),
        )
        seeds.forEach { dao.insert(it) }
    }

    companion object {
        fun factory(dao: TasbeehDao) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return TasbeehViewModel(dao) as T
            }
        }
    }
}
