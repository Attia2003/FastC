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

    private val _selectedDrugId = MutableLiveData<Int?>()
    val selectedDrugId: LiveData<Int?> get() = _selectedDrugId

    private val _openCameraEvent = MutableLiveData<Int>()
    val openCameraEvent: LiveData<Int> get() = _openCameraEvent

    var _currentSearchQuery = ""
    var isLoading = false
        private set

    private var allPage = 1
    private var filteredPage = 1

    private var isFiltered = false


    fun setFilterState(filtered: Boolean) {
        isFiltered = filtered
    }


    fun resetPages() {
        allPage = 1
        filteredPage = 1
        _drugsList.postValue(emptyList())
        _filteredDrugs.postValue(emptyList())
    }


    fun getDrugs(isFiltered: Boolean, searchQuery: String? = null) {
        if (isLoading) return

        viewModelScope.launch {
            try {
                isLoading = true

                val formFilterValue = _formFilter.value


                val formQuery: String? = if (isFiltered && formFilterValue != "ALL") formFilterValue else null

                Log.d("API_CALL", "Fetching page=${if (isFiltered) filteredPage else allPage}, form=$formQuery, search=$searchQuery")

                val response = ApiManager.getWebService().getDrugs(
                    page = if (isFiltered) filteredPage else allPage,
                    size = 20,
                    form = formQuery,
                    name = searchQuery ?: ""
                )

                if (response.isSuccessful) {
                    val newDrugs = response.body() ?: emptyList()

                    if (newDrugs.isNotEmpty()) {
                        if (isFiltered) {
                            _filteredDrugs.postValue((_filteredDrugs.value ?: emptyList()) + newDrugs)
                            filteredPage++
                        } else {
                            _drugsList.postValue((_drugsList.value ?: emptyList()) + newDrugs)
                            allPage++
                        }
                    } else {
                        Log.w("API_WARNING", "No more data on page ${if (isFiltered) filteredPage else allPage}.")
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


        if (newForm == "ALL") {
            isFiltered = false
            allPage = 1
            _drugsList.postValue(emptyList())
        } else {
            isFiltered = true
            filteredPage = 1
            _filteredDrugs.postValue(emptyList())
        }


        getDrugs(isFiltered = isFiltered)
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


    fun setSelectedDrugId(id: Int) {
        _selectedDrugId.value = id
    }

  }
