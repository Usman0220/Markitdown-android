package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiMarkItDownService
import com.example.converter.MarkItDownEngine
import com.example.converter.UrlToMarkdownConverter
import com.example.data.db.AppDatabase
import com.example.data.model.ConversionDocument
import com.example.data.repository.ConversionRepository
import com.example.sample.SampleDocuments
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ViewerTab {
    RENDERED,
    RAW_MARKDOWN
}

class MarkItDownViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ConversionRepository
    val geminiService = GeminiMarkItDownService()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ConversionRepository(db.conversionDocumentDao())
    }

    val allDocuments: StateFlow<List<ConversionDocument>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteDocuments: StateFlow<List<ConversionDocument>> = repository.favoriteDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentDocument = MutableStateFlow<ConversionDocument?>(null)
    val currentDocument: StateFlow<ConversionDocument?> = _currentDocument.asStateFlow()

    private val _isConverting = MutableStateFlow(false)
    val isConverting: StateFlow<Boolean> = _isConverting.asStateFlow()

    private val _conversionProgress = MutableStateFlow("")
    val conversionProgress: StateFlow<String> = _conversionProgress.asStateFlow()

    private val _viewerTab = MutableStateFlow(ViewerTab.RENDERED)
    val viewerTab: StateFlow<ViewerTab> = _viewerTab.asStateFlow()

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    private val _formatFilter = MutableStateFlow("ALL")
    val formatFilter: StateFlow<String> = _formatFilter.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun setViewerTab(tab: ViewerTab) {
        _viewerTab.value = tab
    }

    fun setHistorySearchQuery(query: String) {
        _historySearchQuery.value = query
    }

    fun setFormatFilter(filter: String) {
        _formatFilter.value = filter
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun selectDocument(document: ConversionDocument) {
        _currentDocument.value = document
    }

    fun convertUri(uri: Uri, fileName: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isConverting.value = true
            _conversionProgress.value = "Parsing $fileName..."
            try {
                val doc = MarkItDownEngine.convertUri(
                    context = getApplication(),
                    uri = uri,
                    fileName = fileName,
                    geminiService = geminiService
                )
                val savedId = repository.saveDocument(doc)
                val savedDoc = doc.copy(id = savedId)
                _currentDocument.value = savedDoc
                _snackbarMessage.value = "Converted $fileName successfully"
                onComplete()
            } catch (e: Exception) {
                _snackbarMessage.value = "Conversion failed: ${e.message}"
            } finally {
                _isConverting.value = false
                _conversionProgress.value = ""
            }
        }
    }

    fun convertUrl(url: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isConverting.value = true
            _conversionProgress.value = "Fetching and extracting $url..."
            try {
                val result = UrlToMarkdownConverter.convertUrl(url)
                result.fold(
                    onSuccess = { (title, markdown) ->
                        val wordCount = MarkItDownEngine.countWords(markdown)
                        val doc = ConversionDocument(
                            title = title,
                            sourceFileName = url.substringAfter("://").take(40),
                            sourceFormat = "URL",
                            fileSizeBytes = markdown.toByteArray().size.toLong(),
                            markdownContent = markdown,
                            wordCount = wordCount,
                            charCount = markdown.length,
                            readingTimeMinutes = (wordCount / 200).coerceAtLeast(1)
                        )
                        val id = repository.saveDocument(doc)
                        _currentDocument.value = doc.copy(id = id)
                        _snackbarMessage.value = "Web page converted successfully"
                        onComplete()
                    },
                    onFailure = { error ->
                        _snackbarMessage.value = "Error fetching URL: ${error.message}"
                    }
                )
            } catch (e: Exception) {
                _snackbarMessage.value = "Error: ${e.message}"
            } finally {
                _isConverting.value = false
                _conversionProgress.value = ""
            }
        }
    }

    fun convertRawText(text: String, formatHint: String, title: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isConverting.value = true
            _conversionProgress.value = "Converting formatted content..."
            try {
                val doc = MarkItDownEngine.convertRawText(text, formatHint, title)
                val id = repository.saveDocument(doc)
                _currentDocument.value = doc.copy(id = id)
                _snackbarMessage.value = "Converted to Markdown"
                onComplete()
            } catch (e: Exception) {
                _snackbarMessage.value = "Failed: ${e.message}"
            } finally {
                _isConverting.value = false
                _conversionProgress.value = ""
            }
        }
    }

    fun loadSample(sample: ConversionDocument, onComplete: () -> Unit) {
        viewModelScope.launch {
            val id = repository.saveDocument(sample.copy(id = 0, timestamp = System.currentTimeMillis()))
            _currentDocument.value = sample.copy(id = id)
            _snackbarMessage.value = "Loaded ${sample.title}"
            onComplete()
        }
    }

    fun updateCurrentMarkdown(newMarkdown: String) {
        val current = _currentDocument.value ?: return
        val wordCount = MarkItDownEngine.countWords(newMarkdown)
        val updated = current.copy(
            markdownContent = newMarkdown,
            wordCount = wordCount,
            charCount = newMarkdown.length,
            readingTimeMinutes = (wordCount / 200).coerceAtLeast(1)
        )
        _currentDocument.value = updated
        viewModelScope.launch {
            repository.updateDocument(updated)
        }
    }

    fun toggleFavorite(document: ConversionDocument) {
        viewModelScope.launch {
            repository.toggleFavorite(document)
            if (_currentDocument.value?.id == document.id) {
                _currentDocument.value = document.copy(isFavorite = !document.isFavorite)
            }
        }
    }

    fun deleteDocument(document: ConversionDocument) {
        viewModelScope.launch {
            repository.deleteDocument(document)
            if (_currentDocument.value?.id == document.id) {
                _currentDocument.value = null
            }
            _snackbarMessage.value = "Deleted ${document.title}"
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _snackbarMessage.value = "History cleared"
        }
    }
}
