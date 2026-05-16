package com.kaizen.khushu.ui.screens.tasbeeh

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import com.kaizen.khushu.ui.theme.KhushuColors
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaizen.khushu.data.model.DhikrItem
import com.kaizen.khushu.data.model.TasbeehCollection
import com.kaizen.khushu.data.local.TasbeehDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CreateDhikrRow(
    val id: Int,
    val name: String = "",
    val count: String = "",
)

class TasbeehViewModel(private val dao: TasbeehDao) : ViewModel() {

    // --- Creation State ---
    var createTitle by mutableStateOf("")
    var createColorIndex by mutableIntStateOf(0)
    val createDhikrRows = mutableStateListOf(CreateDhikrRow(id = 0))
    private var nextId = 1
    var pendingFocusId by mutableStateOf<Int?>(null)

    fun updateCreateTitle(newTitle: String) {
        createTitle = newTitle
    }

    fun updateCreateColorIndex(index: Int) {
        createColorIndex = index
    }

    fun updateDhikrName(id: Int, name: String) {
        val index = createDhikrRows.indexOfFirst { it.id == id }
        if (index != -1) {
            createDhikrRows[index] = createDhikrRows[index].copy(name = name)
        }
    }

    fun updateDhikrCount(id: Int, count: String) {
        val index = createDhikrRows.indexOfFirst { it.id == id }
        if (index != -1) {
            createDhikrRows[index] = createDhikrRows[index].copy(count = count)
        }
    }

    fun addDhikrRow() {
        val id = nextId++
        createDhikrRows.add(CreateDhikrRow(id = id))
        pendingFocusId = id
    }

    fun clearPendingFocus() {
        pendingFocusId = null
    }

    fun removeDhikrRow(id: Int) {
        createDhikrRows.removeAll { it.id == id }
    }

    fun moveDhikrRow(from: Int, to: Int) {
        createDhikrRows.add(to, createDhikrRows.removeAt(from))
    }

    fun resetCreateState() {
        createTitle = ""
        createColorIndex = 0
        createDhikrRows.clear()
        createDhikrRows.add(CreateDhikrRow(id = 0))
        nextId = 1
        pendingFocusId = null
    }

    fun loadCollectionForEdit(collection: TasbeehCollection) {
        createTitle = collection.title ?: ""
        createColorIndex = KhushuColors.Palette.indexOfFirst { it.toArgb() == collection.colorInt }.coerceAtLeast(0)
        createDhikrRows.clear()
        collection.items.forEachIndexed { index, item ->
            createDhikrRows.add(CreateDhikrRow(id = index, name = item.name, count = item.targetCount.toString()))
        }
        nextId = collection.items.size
        pendingFocusId = null
    }

    // Eagerly-started StateFlow so the list is pre-loaded before the screen is ever
    // composed — prevents the empty→populated flash that conflicts with page transitions.
    val collections: StateFlow<List<TasbeehCollection>> = dao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    private val _countIncrementSignal = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val countIncrementSignal = _countIncrementSignal.asSharedFlow()

    fun incrementActiveCount() {
        viewModelScope.launch { _countIncrementSignal.emit(Unit) }
    }

    init {
        viewModelScope.launch {
            dao.deleteDummyCards()
            if (dao.count() == 0) seedDefaults()
        }
    }

    suspend fun insert(collection: TasbeehCollection) = dao.insert(collection)

    fun delete(collection: TasbeehCollection) {
        viewModelScope.launch { dao.delete(collection) }
    }

    private suspend fun seedDefaults() {
        val seeds = listOf(
            TasbeehCollection(
                title = "أذكار بعد الصلاة",
                colorInt = KhushuColors.Palette[0].toArgb(),
                items = listOf(
                    DhikrItem("سُبْحَانَ الله", 33),
                    DhikrItem("الْحَمْدُ لِلَّه", 33),
                    DhikrItem("اللهُ أَكْبَر", 34),
                ),
            ),
            TasbeehCollection(
                title = "الصلاة على النبي ﷺ",
                colorInt = KhushuColors.Palette[1].toArgb(),
                items = listOf(
                    DhikrItem("اللَّهُمَّ صَلِّ عَلَى مُحَمَّد", 100),
                    DhikrItem("اللَّهُمَّ بَارِكْ عَلَى مُحَمَّد", 100),
                ),
            ),
            TasbeehCollection(
                title = "الاستغفار",
                colorInt = KhushuColors.Palette[2].toArgb(),
                items = listOf(
                    DhikrItem("أَسْتَغْفِرُ الله", 100),
                    DhikrItem("سُبْحَانَ اللهِ وَبِحَمْدِهِ", 100),
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
