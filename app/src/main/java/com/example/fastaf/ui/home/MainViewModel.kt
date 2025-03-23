package com.example.fastaf.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fastaf.ui.api.ApiManager
import com.example.fastaf.ui.home.searchable.ResponseSearchRecItem
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _drugsList = MutableLiveData<List<ResponseSearchRecItem>>()
    val drugsList: LiveData<List<ResponseSearchRecItem>> = _drugsList

    private val _filteredDrugs = MutableLiveData<List<ResponseSearchRecItem>>()
    val filteredDrugs: LiveData<List<ResponseSearchRecItem>> = _filteredDrugs

    private val _formFilter = MutableLiveData<String>("ALL")
    val formFilter: LiveData<String> = _formFilter

    private val _statusFilter = MutableLiveData<String>("ALL")
    val statusFilter: LiveData<String> = _statusFilter

    private val _selectedDrugId = MutableLiveData<Int?>()
    val selectedDrugId: LiveData<Int?> get() = _selectedDrugId

    private val filterCache = mutableMapOf<String, List<ResponseSearchRecItem>>()

   var _currentSearchQuery = ""

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
        _drugsList.postValue(emptyList())
        _filteredDrugs.postValue(emptyList())
    }


    fun updateStatusFilter(newStatus: String) {
        _statusFilter.value = newStatus
        resetPages()
        getDrugs(isFiltered)
    }


    fun updateDrugForm(newForm: String) {
        _formFilter.value = newForm

        isFiltered = newForm != "ALL"
        resetPages()
        getDrugs(isFiltered)
    }


    fun getDrugs(isFiltered: Boolean, searchQuery: String? = null) {
        val cacheKey = "${_formFilter.value}_${_statusFilter.value}_$searchQuery"

        if (filterCache.containsKey(cacheKey)) {
            if (isFiltered) _filteredDrugs.postValue(filterCache[cacheKey])
            else _drugsList.postValue(filterCache[cacheKey])
            return
        }

        viewModelScope.launch {
            try {
                isLoading = true

                val formFilterValue = _formFilter.value
                val formQuery: String? = if (isFiltered && formFilterValue != "ALL") formFilterValue else null

                val pageQuery = if (isFiltered) filteredPage.toString() else allPage.toString()

                val response = ApiManager.getWebService().getDrugs(
                    page = pageQuery,
                    size = 20,
                    form = formQuery,
                    name = searchQuery ?: ""
                )

                if (response.isSuccessful) {
                    val newDrugs = response.body() ?: emptyList()

                    if (newDrugs.isNotEmpty()) {
                        if (isFiltered) {
                            val updatedList = (_filteredDrugs.value ?: emptyList()) + newDrugs
                            _filteredDrugs.postValue(updatedList)
                            filterCache[cacheKey] = updatedList
                            filteredPage++
                        } else {
                            val updatedList = (_drugsList.value ?: emptyList()) + newDrugs
                            _drugsList.postValue(updatedList)
                            filterCache[cacheKey] = updatedList
                            allPage++
                        }
                    } else {
                        isLastPage = true
                    }
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error fetching drugs: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }


    fun searchDrugs(query: String) {
        _currentSearchQuery = query

        val allDrugs = if (isFiltered) _filteredDrugs.value else _drugsList.value

        val filteredResults = allDrugs?.filter { drug ->
            drug.name!!.contains(query, ignoreCase = true) || drug.id.toString() == query
        } ?: emptyList()

        Log.d("SEARCH", "Query: $query | Results: ${filteredResults.size}")

        if (isFiltered) {
            _filteredDrugs.postValue(filteredResults)
        } else {
            _drugsList.postValue(filteredResults)
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
                } else {
                    Log.e("STATUS_UPDATE_ERROR", "Failed to update status: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("STATUS_UPDATE_ERROR", "Exception: ${e.message}")
            }
        }
    }
}