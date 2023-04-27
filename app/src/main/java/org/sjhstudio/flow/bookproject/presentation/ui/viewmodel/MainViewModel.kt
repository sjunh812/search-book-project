package org.sjhstudio.flow.bookproject.presentation.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.sjhstudio.flow.bookproject.data.exception.ClientErrorException
import org.sjhstudio.flow.bookproject.data.exception.EmptyBodyException
import org.sjhstudio.flow.bookproject.data.exception.NetworkErrorException
import org.sjhstudio.flow.bookproject.domain.model.BookList
import org.sjhstudio.flow.bookproject.domain.repository.BookRepository
import org.sjhstudio.flow.bookproject.presentation.base.UiState
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private var _bookList = MutableSharedFlow<UiState<BookList>>()
    val bookList = _bookList.asSharedFlow()

    private var _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    var lastBookList: BookList? = null  // 마지막 검색결과

    companion object {
        private val LOG = "MainViewModel"
    }

    init {
        // todo. DB 호출
    }

    fun searchBook(query: String, start: Int) = viewModelScope.launch {
        Log.e(LOG, "query : $query, start : $start")
        bookRepository.getBookList(query, start)
            .onStart { emit(UiState.Loading) }
            .onCompletion { }
            .catch { e ->
                e.printStackTrace()
                when (e) {
                    is NetworkErrorException -> _errorMessage.emit("네트워크 연결이 원할하지 않습니다.")
                    is EmptyBodyException -> _errorMessage.emit("서버 상태가 원할하지 않습니다.")
                    is ClientErrorException -> _errorMessage.emit("예상치 못한 오류가 발생하였습니다.")
                }
            }
            .collectLatest { uiState ->
                if (uiState is UiState.Success) {
                    lastBookList = uiState.data
                }
                _bookList.emit(uiState)
            }
    }
}