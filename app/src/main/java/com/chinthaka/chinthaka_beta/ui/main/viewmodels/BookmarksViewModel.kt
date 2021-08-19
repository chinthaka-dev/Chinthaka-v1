package com.chinthaka.chinthaka_beta.ui.main.viewmodels

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.chinthaka.chinthaka_beta.data.pagingsource.BookmarkPostsPagingSource
import com.chinthaka.chinthaka_beta.data.pagingsource.FollowPostsPagingSource
import com.chinthaka.chinthaka_beta.other.Constants
import com.chinthaka.chinthaka_beta.repositories.MainRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val repository: MainRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : BasePostViewModel(repository, dispatcher) {

    val pagingFlow = Pager(PagingConfig(Constants.PAGE_SIZE)){
        BookmarkPostsPagingSource(FirebaseFirestore.getInstance())
    }.flow.cachedIn(viewModelScope)
}