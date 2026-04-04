package com.kaizen.khushu.ui.screens.salah

import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaizen.khushu.data.CanvasDao
import com.kaizen.khushu.data.CanvasPreset
import com.kaizen.khushu.data.CanvasWidget
import com.kaizen.khushu.data.DefaultPresets
import com.kaizen.khushu.data.SalahCanvasLayout
import com.kaizen.khushu.data.toEntity
import com.kaizen.khushu.data.toDomain
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SalahCanvasViewModel(private val dao: CanvasDao) : ViewModel() {

    val layout: StateFlow<SalahCanvasLayout> = dao.getDefault()
        .map { it ?: SalahCanvasLayout() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SalahCanvasLayout())

    val customPresets = dao.getAllPresets()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedWidgetId = MutableStateFlow<String?>(null)
    val selectedWidgetId: StateFlow<String?> = _selectedWidgetId

    private val _isUiVisible = MutableStateFlow(true)
    val isUiVisible: StateFlow<Boolean> = _isUiVisible

    // Working copy of widgets and background (not yet saved to DB)
    private val _workingWidgets = MutableStateFlow<List<CanvasWidget>>(emptyList())
    val workingWidgets: StateFlow<List<CanvasWidget>> = _workingWidgets

    private val _workingBackgroundColor = MutableStateFlow(0xFF000000.toInt())
    val workingBackgroundColor: StateFlow<Int> = _workingBackgroundColor

    // Canvas dimensions for alignment calculations
    private val _canvasWidth = MutableStateFlow(0f)
    val canvasWidth: StateFlow<Float> = _canvasWidth

    private val _canvasHeight = MutableStateFlow(0f)
    val canvasHeight: StateFlow<Float> = _canvasHeight

    init {
        viewModelScope.launch {
            layout.collectLatest { 
                if (_workingWidgets.value.isEmpty()) {
                    _workingWidgets.value = it.widgets 
                    _workingBackgroundColor.value = it.backgroundColorInt
                }
            }
        }
        viewModelScope.launch {
            if (dao.getPresetCount() == 0) {
                DefaultPresets.defaults.forEach { preset ->
                    dao.insertPreset(preset.toEntity())
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
                    is CanvasWidget.RakatCount -> widget.copy(width = width, height = height)
                    is CanvasWidget.ClockWidget -> widget.copy(width = width, height = height)
                    is CanvasWidget.CustomText -> widget.copy(width = width, height = height)
                }
            }
        }
    }

    fun addWidget(widget: CanvasWidget) {
        _workingWidgets.update { it + widget }
    }

    fun updateWidget(updated: CanvasWidget) {
        _workingWidgets.update { list -> list.map { if (it.id == updated.id) updated else it } }
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

                val widgetWidth = widget.width
                val widgetHeight = widget.height
                val scale = widget.scale

                var newOffsetX = widget.offsetX
                var newOffsetY = widget.offsetY

                val scaledWidth = widgetWidth * scale
                val scaledHeight = widgetHeight * scale

                when (horizontal) {
                    androidx.compose.ui.Alignment.Start -> {
                        newOffsetX = 0f
                    }
                    androidx.compose.ui.Alignment.CenterHorizontally -> {
                        newOffsetX = (canvasWidth - scaledWidth) / 2f
                    }
                    androidx.compose.ui.Alignment.End -> {
                        newOffsetX = canvasWidth - scaledWidth
                    }
                }

                when (vertical) {
                    androidx.compose.ui.Alignment.Top -> {
                        newOffsetY = 0f
                    }
                    androidx.compose.ui.Alignment.CenterVertically -> {
                        newOffsetY = (canvasHeight - scaledHeight) / 2f
                    }
                    androidx.compose.ui.Alignment.Bottom -> {
                        newOffsetY = canvasHeight - scaledHeight
                    }
                }

                newOffsetX = newOffsetX.coerceAtLeast(0f)
                newOffsetY = newOffsetY.coerceAtLeast(0f)

                when (widget) {
                    is CanvasWidget.RakatCount -> widget.copy(offsetX = newOffsetX, offsetY = newOffsetY)
                    is CanvasWidget.ClockWidget -> widget.copy(offsetX = newOffsetX, offsetY = newOffsetY)
                    is CanvasWidget.CustomText -> widget.copy(offsetX = newOffsetX, offsetY = newOffsetY)
                }
            }
        }
    }

    fun saveLayout(backgroundColor: Int) {
        viewModelScope.launch {
            dao.save(SalahCanvasLayout(
                backgroundColorInt = _workingBackgroundColor.value,
                widgets = _workingWidgets.value,
            ))
        }
    }

    fun saveCustomPreset(name: String, widgets: List<CanvasWidget>, background: Int) {
        viewModelScope.launch {
            val newPreset = CanvasPreset(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                backgroundColor = background,
                widgets = widgets.toList(),
                isDeletable = true
            )
            dao.insertPreset(newPreset.toEntity())
        }
    }

    fun deletePreset(id: String) {
        viewModelScope.launch { dao.deletePreset(id) }
    }

    fun renamePreset(id: String, newName: String) {
        viewModelScope.launch { dao.renamePreset(id, newName) }
    }

    companion object {
        fun factory(dao: CanvasDao) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SalahCanvasViewModel(dao) as T
            }
        }
    }
}
