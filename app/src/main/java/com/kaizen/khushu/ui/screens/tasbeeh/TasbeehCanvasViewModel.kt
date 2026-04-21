package com.kaizen.khushu.ui.screens.tasbeeh

import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaizen.khushu.data.local.CanvasDao
import com.kaizen.khushu.data.model.TasbeehCanvasPresetDomain
import com.kaizen.khushu.data.model.toDomain
import com.kaizen.khushu.data.model.toEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TasbeehCanvasViewModel(private val dao: CanvasDao) : ViewModel() {

    private fun getInitialLayout(): TasbeehCanvasLayout {
        return TasbeehCanvasLayout(
            backgroundColorInt = 0xFF000000.toInt(),
            widgets = DefaultTasbihPreset.widgets
        )
    }

    val layout: StateFlow<TasbeehCanvasLayout> = dao.getTasbeehDefault()
        .map { it ?: getInitialLayout() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), getInitialLayout())

    val presets: StateFlow<List<TasbeehCanvasPresetDomain>> = dao.getAllTasbeehPresets()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedWidgetId = MutableStateFlow<String?>(null)
    val selectedWidgetId: StateFlow<String?> = _selectedWidgetId

    private val _isUiVisible = MutableStateFlow(true)
    val isUiVisible: StateFlow<Boolean> = _isUiVisible

    private val _workingWidgets = MutableStateFlow<List<TasbihWidget>>(emptyList())
    val workingWidgets: StateFlow<List<TasbihWidget>> = _workingWidgets

    private val _workingBackgroundColor = MutableStateFlow(0xFF000000.toInt())
    val workingBackgroundColor: StateFlow<Int> = _workingBackgroundColor

    private val _canvasWidth = MutableStateFlow(0f)
    private val _canvasHeight = MutableStateFlow(0f)

    init {
        viewModelScope.launch {
            layout.collectLatest { 
                if (_workingWidgets.value.isEmpty()) {
                    _workingWidgets.value = it.widgets 
                    _workingBackgroundColor.value = it.backgroundColorInt
                }
            }
        }
    }

    fun setCanvasSize(width: Float, height: Float) {
        _canvasWidth.value = width
        _canvasHeight.value = height
    }

    fun updateWidgetSize(id: String, width: Float, height: Float) {
        _workingWidgets.update { list ->
            list.map { widget ->
                if (widget.id != id) return@map widget
                when (widget) {
                    is TasbihWidget.StringBeadWidget -> widget.copy(width = width, height = height)
                    is TasbihWidget.DhikrNameWidget -> widget.copy(width = width, height = height)
                    is TasbihWidget.CounterWidget -> widget.copy(width = width, height = height)
                    is TasbihWidget.ProgressCircleWidget -> widget.copy(width = width, height = height)
                    is TasbihWidget.MeaningWidget -> widget.copy(width = width, height = height)
                }
            }
        }
    }

    fun addWidget(widget: TasbihWidget) {
        _workingWidgets.update { it + widget }
    }

    fun addNewWidgetFromMenu(widget: TasbihWidget) {
        val centeredWidget = when (widget) {
            is TasbihWidget.StringBeadWidget -> widget.copy(offsetX = 0.88f, offsetY = 0.5f)
            is TasbihWidget.DhikrNameWidget -> widget.copy(offsetX = 0.5f, offsetY = 0.2f)
            is TasbihWidget.CounterWidget -> widget.copy(offsetX = 0.5f, offsetY = 0.5f)
            is TasbihWidget.ProgressCircleWidget -> widget.copy(offsetX = 0.5f, offsetY = 0.5f)
            is TasbihWidget.MeaningWidget -> widget.copy(offsetX = 0.5f, offsetY = 0.25f)
        }
        _workingWidgets.update { it + centeredWidget }
    }

    fun updateWidget(updated: TasbihWidget) {
        _workingWidgets.update { list -> 
            list.map { 
                if (it.id == updated.id) {
                    if (updated is TasbihWidget.StringBeadWidget) {
                        updated.copy(offsetY = 0.5f) // Horizontal lock
                    } else updated
                } else it 
            } 
        }
    }

    fun updateBackgroundColor(color: Int) {
        _workingBackgroundColor.value = color
    }

    fun clearWidgets() {
        _workingWidgets.value = emptyList()
    }

    fun removeWidget(id: String) {
        _workingWidgets.update { it.filter { w -> w.id != id } }
        if (_selectedWidgetId.value == id) _selectedWidgetId.value = null
    }

    fun selectWidget(id: String?) {
        _selectedWidgetId.value = id
    }

    fun toggleUiVisibility() {
        _isUiVisible.update { !it }
    }

    fun showUi() { _isUiVisible.value = true }

    fun alignSelectedWidget(horizontal: Alignment.Horizontal?, vertical: Alignment.Vertical?) {
        val selectedId = _selectedWidgetId.value ?: return
        val canvasWidth = _canvasWidth.value
        val canvasHeight = _canvasHeight.value
        if (canvasWidth <= 0 || canvasHeight <= 0) return

        _workingWidgets.update { list ->
            list.map { widget ->
                if (widget.id != selectedId) return@map widget
                if (widget is TasbihWidget.StringBeadWidget && vertical != null) return@map widget // Ignore vertical for string

                val scaledWidth = widget.width * widget.scale
                val scaledHeight = widget.height * widget.scale
                val widthPct = scaledWidth / canvasWidth
                val heightPct = scaledHeight / canvasHeight

                var newX = widget.offsetX
                var newY = widget.offsetY

                when (horizontal) {
                    Alignment.Start -> newX = widthPct / 2f
                    Alignment.CenterHorizontally -> newX = 0.5f
                    Alignment.End -> newX = 1f - (widthPct / 2f)
                }
                when (vertical) {
                    Alignment.Top -> newY = heightPct / 2f
                    Alignment.CenterVertically -> newY = 0.5f
                    Alignment.Bottom -> newY = 1f - (heightPct / 2f)
                }

                when (widget) {
                    is TasbihWidget.StringBeadWidget -> widget.copy(offsetX = newX)
                    is TasbihWidget.DhikrNameWidget -> widget.copy(offsetX = newX, offsetY = newY)
                    is TasbihWidget.CounterWidget -> widget.copy(offsetX = newX, offsetY = newY)
                    is TasbihWidget.ProgressCircleWidget -> widget.copy(offsetX = newX, offsetY = newY)
                    is TasbihWidget.MeaningWidget -> widget.copy(offsetX = newX, offsetY = newY)
                }
            }
        }
    }

    fun saveLayout() {
        viewModelScope.launch {
            dao.saveTasbeeh(TasbeehCanvasLayout(
                backgroundColorInt = _workingBackgroundColor.value,
                widgets = _workingWidgets.value,
            ))
        }
    }

    fun saveAsPreset(name: String) {
        viewModelScope.launch {
            val preset = TasbeehCanvasPresetDomain(
                id = "preset_${System.currentTimeMillis()}",
                name = name,
                backgroundColor = _workingBackgroundColor.value,
                widgets = _workingWidgets.value,
                isDeletable = true
            )
            dao.insertTasbeehPreset(preset.toEntity())
        }
    }

    fun loadPreset(preset: TasbeehCanvasPresetDomain) {
        _workingWidgets.value = preset.widgets
        _workingBackgroundColor.value = preset.backgroundColor
        _selectedWidgetId.value = null
    }

    fun deletePreset(id: String) {
        viewModelScope.launch {
            dao.deleteTasbeehPreset(id)
        }
    }

    fun resetToDefault() {
        val initial = getInitialLayout()
        _workingWidgets.value = initial.widgets
        _workingBackgroundColor.value = initial.backgroundColorInt
    }

    companion object {
        fun factory(dao: CanvasDao) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return TasbeehCanvasViewModel(dao) as T
            }
        }
    }
}
