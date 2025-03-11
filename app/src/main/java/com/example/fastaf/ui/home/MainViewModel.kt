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

    private val _drugsList = MutableLiveData<List<ResponseSearchRecItem>>()  // Holds all drugs
    val drugsList: LiveData<List<ResponseSearchRecItem>> = _drugsList

    private val _filteredDrugs = MutableLiveData<List<ResponseSearchRecItem>>()  // Filtered based on status
    val filteredDrugs: LiveData<List<ResponseSearchRecItem>> = _filteredDrugs

    private val _statusFilter = MutableLiveData<String>("AVAILABLE")  // Default filter
    val statusFilter: LiveData<String> = _statusFilter

    private val _formFilter = MutableLiveData<String>("ALL")  // Default filter: "ALL" means no form filter
    val formFilter: LiveData<String> = _formFilter

    private var _currentSearchQuery = ""
    var isLoading = false
        private set

    private var allPage = 1
    private var filteredPage = 1

    fun getDrugs(isFiltered: Boolean) {
        if (isLoading) return

        viewModelScope.launch {
            try {
                isLoading = true

                val formFilterValue = _formFilter.value
                val isUsingFilter = isFiltered && formFilterValue != "ALL"

                val currentPage = if (isUsingFilter) filteredPage else allPage
                val formQuery: String? = if (isUsingFilter) formFilterValue else null

                Log.d("API_CALL", "Fetching page=$currentPage, form=$formQuery")

                val response = ApiManager.getWebService().getDrugs(
                    page = currentPage,
                    size = 20,
                    form = formQuery
                )

                Log.d("API_CALL", "Request URL: ${response.raw().request.url}")

                if (response.isSuccessful) {
                    val newDrugs = response.body() ?: emptyList()
                    Log.d("API_RESPONSE", "Fetched ${newDrugs.size} items for page $currentPage")

                    if (newDrugs.isNotEmpty()) {
                        if (isUsingFilter) {
                            val currentList = _filteredDrugs.value ?: emptyList()
                            _filteredDrugs.postValue(currentList + newDrugs)
                            filteredPage++
                        } else {
                            val currentList = _drugsList.value ?: emptyList()
                            _drugsList.postValue(currentList + newDrugs)
                            allPage++
                        }
                    } else {
                        Log.w("API_WARNING", "No more data available!")
                    }
                } else {
                    Log.e("API_ERROR", "API failed: ${response.code()} -> ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error fetching drugs: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun updateDrugForm(newForm: String) {
        _formFilter.value = newForm
        filteredPage = 1
        _filteredDrugs.postValue(emptyList())
        getDrugs(isFiltered = true)
    }

    fun updateStatus(newStatus: String) {
        _statusFilter.value = newStatus
        Log.d("FILTER_UPDATE", "Filtering drugs by status: $newStatus")
        getDrugs(isFiltered = _formFilter.value != "ALL")
    }

    fun resetAndFetchAllData() {
        _formFilter.value = "ALL"
        allPage = 1
        _drugsList.postValue(emptyList())
        getDrugs(isFiltered = false)
    }

    fun searchDrugs(query: String) {
        _currentSearchQuery = query
    }
}