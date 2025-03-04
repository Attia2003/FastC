package com.example.fastaf.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fastaf.ui.api.ApiManager
import com.example.fastaf.ui.api.WebServices
import com.example.fastaf.ui.home.searchable.ResponseSearchRec
import com.example.fastaf.ui.home.searchable.ResponseSearchRecItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    private val _drugs = MutableLiveData<List<ResponseSearchRecItem?>?>()
    val Strdrugs: LiveData<List<ResponseSearchRecItem?>?> get() = _drugs
    private var searchdrugs: MutableList<ResponseSearchRecItem?> = mutableListOf()
    private val loadMorePages = MutableLiveData<Boolean>()

    private val statusFilter = MutableLiveData<List<String>>()

    val loadMore: LiveData<Boolean> get() = loadMorePages

    private var page = 1
    private val size = 20
    var isLoading = false

    private var currentStatus: String? = null

    init {


        statusFilter.value = listOf("All", "AVAILABLE", "DISCARDED", "POSTPONED")
    }
    fun fetchDrugs(loadMore: Boolean = false) {
        if (isLoading) return

        isLoading = true
        loadMorePages.value = true

        viewModelScope.launch {
            try {
                Log.d("API_DEBUG", "Fetching page: $page")

                val response = withContext(Dispatchers.IO) {
                    ApiManager.getWebService().getDrugs(page, size)
                }

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        Log.d("API_RESPONSE", "Received ${responseBody.size} items")
                        val updatedList = _drugs.value.orEmpty().toMutableList().apply {
                            addAll(responseBody)
                        }
                        searchdrugs = updatedList // Store for search
                        _drugs.postValue(updatedList)
                        page++
                    } else {
                        Log.e("API_ERROR", "Response body is null")
                    }
                } else {
                    val errorMessage = response.errorBody()?.string()
                    Log.e("API_ERROR", "Error: $errorMessage")
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Exception: ${e.message}")
            } finally {
                isLoading = false
                loadMorePages.postValue(false)
            }
        }
    }


    fun onLoadMoreNeeded() {
        fetchDrugs(loadMore = true)
    }


    fun searchDrugs(query: String) {
        Log.d("SEARCH_FUNCTION", "Searching for: $query")

        val filteredList = if (query.isEmpty()) {
            searchdrugs
        } else {
            searchdrugs.filter { it?.name?.contains(query, ignoreCase = true) == true }
        }

        Log.d("SEARCH_RESULTS", "Found ${filteredList.size} results")
        _drugs.postValue(filteredList)
    }
}


