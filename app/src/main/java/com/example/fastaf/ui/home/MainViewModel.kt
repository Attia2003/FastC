package com.example.fastaf.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fastaf.ui.api.ApiManager
import com.example.fastaf.ui.home.searchable.ResponseSearchRecItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _drugsList = MutableLiveData<List<ResponseSearchRecItem>?>()
    val drugsList: MutableLiveData<List<ResponseSearchRecItem>?> = _drugsList

    private val _filteredDrugs = MutableLiveData<List<ResponseSearchRecItem>?>()
    val filteredDrugs: MutableLiveData<List<ResponseSearchRecItem>?> = _filteredDrugs

    private val _formFilter = MutableLiveData<Set<String>>(setOf("ALL"))
    val formFilter: LiveData<Set<String>> = _formFilter

    private val _statusFilter = MutableLiveData<String>("AVAILABLE")
    val statusFilter: LiveData<String> = _statusFilter

    private val _selectedDrugId = MutableLiveData<Int?>()
    val selectedDrugId: LiveData<Int?> get() = _selectedDrugId

    private val _hasImageFilter = MutableLiveData<Boolean?>()
    val hasImageFilter: LiveData<Boolean?> = _hasImageFilter

    private val filterCache = mutableMapOf<String, List<ResponseSearchRecItem>>()

    var _currentSearchQuery = ""

    private var lastLoadedPage = 0

    private var searchJob: Job? = null

    private var lastDataCache: List<ResponseSearchRecItem>? = null

    var isLoading = false
        private set

    private var allPage = 0
    private var filteredPage = 0

    var isFiltered = false
    var isLastPage = false


    fun resetPages() {
        allPage = 0
        filteredPage = 0
        isLastPage = false
        isLoading = false
        lastLoadedPage = 0
        lastDataCache = null
        filterCache.clear()

        _drugsList.postValue(emptyList())
        _filteredDrugs.postValue(emptyList())
        val cacheKey = "${_formFilter.value}_${_statusFilter.value}_$_currentSearchQuery"
        filterCache.remove(cacheKey)
    }

    fun updateStatusFilter(newStatus: String) {
        _statusFilter.value = newStatus
        resetPages()
        getDrugs(isFiltered = true, searchQuery = _currentSearchQuery)
    }

    fun updateHasImageFilter(option: String) {
        _hasImageFilter.value = when (option) {
            "seen" -> true
            "nonseen" -> false
            else -> null
        }
    }

    fun updateDrugForm(newForm: String) {
        val currentForms = _formFilter.value?.toMutableSet() ?: mutableSetOf()

        if (newForm == "ALL") {
            currentForms.clear()
            currentForms.add("ALL")
        } else {
            currentForms.remove("ALL")
            if (currentForms.contains(newForm)) currentForms.remove(newForm)
            else currentForms.add(newForm)
        }

        _formFilter.value = currentForms

        isFiltered = currentForms.isNotEmpty() && !currentForms.contains("ALL")
        resetPages()
        getDrugs(isFiltered)
    }


    fun getDrugs(isFiltered: Boolean, searchQuery: String? = null) {
        val formFilterValue = _formFilter.value ?: setOf("ALL")
        val statusFilterValue = _statusFilter.value ?: "ALL"
        val cacheKey = "${formFilterValue}_${statusFilterValue}_$searchQuery"


        if (filterCache.containsKey(cacheKey)) {
            val cachedData = filterCache[cacheKey]
            if (isFiltered) _filteredDrugs.postValue(cachedData)
            else _drugsList.postValue(cachedData)

            Log.d("PAGINATION_DEBUG", "Loaded from cache for key: $cacheKey")
        }

        viewModelScope.launch {
            try {
                isLoading = true
                Log.d("PAGINATION_DEBUG", "Starting API call...")


                val formQuery = if (_formFilter.value?.contains("ALL") == true) null
                else _formFilter.value?.joinToString("|")

                val statusQuery = _statusFilter.value?.takeIf { it.isNotEmpty() } ?: "ALL"
                val pageQuery = if (isFiltered) filteredPage else allPage


                val urlPreview = buildString {
                    append("page=$pageQuery&size=20")
                    formQuery?.let { append("&form=$it") }
                    append("&status=$statusQuery")
                    searchQuery?.let { append("&name=$it") }
                }

                Log.d("PAGINATION_DEBUG", "Final API URL: $urlPreview")
                Log.d("PAGINATION_DEBUG", "Filters -> Form: $formQuery | Status: $statusQuery | Page: $pageQuery | isFiltered: $isFiltered")


                val response = ApiManager.getWebService().getDrugs(
                    page = pageQuery.toString(),
                    size = 20,
                    form = formQuery,
                    name = searchQuery ?: "",
                    status = statusQuery,
                    hasImage = _hasImageFilter.value
                )

                if (response.isSuccessful) {
                    val newDrugs = response.body() ?: emptyList()
                    Log.d("PAGINATION_DEBUG", "API Success: Received ${newDrugs.size} items")

                    if (newDrugs.isNotEmpty()) {
                        val updatedList = if (isFiltered) {
                            (_filteredDrugs.value ?: emptyList()) + newDrugs
                        } else {
                            (_drugsList.value ?: emptyList()) + newDrugs
                        }


                        if (isFiltered) {
                            _filteredDrugs.postValue(updatedList)
                            filteredPage++
                            Log.d("PAGINATION_DEBUG", "Filtered page incremented to $filteredPage")
                        } else {
                            _drugsList.postValue(updatedList)
                            allPage++
                            Log.d("PAGINATION_DEBUG", "All page incremented to $allPage")
                        }

                        // ✅ Cache the result properly
                        filterCache[cacheKey] = updatedList
                    } else {
                        if (pageQuery == 0) {
                            isLastPage = true
                            Log.d("PAGINATION_DEBUG", "No more data. Marked last page.")
                        }
                    }
                } else {
                    Log.e("PAGINATION_DEBUG", "API Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PAGINATION_DEBUG", "Exception: ${e.message}")
            } finally {
                isLoading = false
                Log.d("PAGINATION_DEBUG", "Loading state reset")
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        if (_currentSearchQuery != newQuery) {
            _currentSearchQuery = newQuery


            searchJob?.cancel()


            searchJob = viewModelScope.launch {
                delay(300)

                allPage = 0
                filteredPage = 0
                isLastPage = false
                _drugsList.value = emptyList()
                _filteredDrugs.value = emptyList()

                Log.d("SEARCH_DEBUG", "New search query: $newQuery — Resetting pagination and data")

                getDrugs(
                    isFiltered = _formFilter.value?.isNotEmpty() == true || _statusFilter.value?.isNotEmpty() == true,
                    searchQuery = newQuery
                )
            }
        }
    }


    fun setSelectedDrugId(id: Int?) {
        _selectedDrugId.value = id
    }


    fun updateDrugStatus(item: ResponseSearchRecItem, newStatus: String) {
        viewModelScope.launch {
            try {
                val response = ApiManager.getWebService().getUpdateStatus(item.id, newStatus)
                if (response.isSuccessful) {
                    item.status = newStatus
                    Log.d("STATUS_UPDATE", "Drug ${item.id} moved to $newStatus")

                    resetPages()
                    getDrugs(isFiltered = isFiltered, searchQuery = _currentSearchQuery)

                    when (newStatus) {
                        "DISCARDED" -> Log.d("STATUS", "Drug moved to DISCARDED")
                        "POSTPONED" -> Log.d("STATUS", "Drug moved to POSTPONED")
                    }
                } else {
                    Log.e("STATUS_UPDATE_ERROR", "Failed to update status: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("STATUS_UPDATE_ERROR", "Exception: ${e.message}")
            }
        }
    }


    fun cachePageData(page: Int, data: List<ResponseSearchRecItem>) {
        lastLoadedPage = page
        lastDataCache = data
    }

    fun getCachedData(): List<ResponseSearchRecItem>? = lastDataCache
    fun getLastPage(): Int = lastLoadedPage
}