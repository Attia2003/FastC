package com.example.fastaf.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fastaf.ui.api.ApiManager
import com.example.fastaf.ui.home.searchable.ResponseSearchRecItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {

    private val _filteredDrugs = MutableLiveData<List<ResponseSearchRecItem?>?>()
    val filteredDrugs: LiveData<List<ResponseSearchRecItem?>?> get() = _filteredDrugs

    private var allDrugs: MutableList<ResponseSearchRecItem?> = mutableListOf()
    private val loadMorePages = MutableLiveData<Boolean>()
    val loadMore: LiveData<Boolean> get() = loadMorePages

    private var page = 1
    private val size = 20
    var isLoading = false

    private var currentStatus: String? = null
    private var searchQuery: String = ""

    fun fetchDrugs(loadMore: Boolean = false) {
        if (isLoading) return

        isLoading = true
        loadMorePages.value = true

        viewModelScope.launch {
            try {
                Log.d("API_DEBUG", "Fetching page: $page with status: $currentStatus")

                val response = withContext(Dispatchers.IO) {
                    ApiManager.getWebService().getDrugs(page, size)
                }

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        Log.d("API_RESPONSE", "Received ${responseBody.size} items")
                        if (!loadMore) allDrugs.clear()
                        allDrugs.addAll(responseBody)
                        page++
                        applyFilters()
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

    fun updateStatus(status: String?) {
        currentStatus = if (status == "AVAILABLE") null else status
        Log.d("", "Updated status: $currentStatus")
        applyFilters()
    }

    fun searchDrugs(query: String) {
        searchQuery = query
        Log.d("SEARCH_FUNCTION", "Searching for: $query")
        applyFilters()
    }

    private fun applyFilters() {
        val filteredList = allDrugs.filter { item ->
            val matchesStatus = currentStatus == null || item?.status == currentStatus
            val matchesQuery = searchQuery.isEmpty() || item?.name?.contains(searchQuery, ignoreCase = true) == true
            matchesStatus && matchesQuery
        }
        Log.d("FILTER_RESULTS", "Filtered items count: ${filteredList.size}")
        _filteredDrugs.postValue(filteredList)
    }

    fun onLoadMoreNeeded() {
        fetchDrugs(loadMore = true)
    }
}