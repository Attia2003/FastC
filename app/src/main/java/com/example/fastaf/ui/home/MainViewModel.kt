package com.example.fastaf.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fastaf.ui.api.ApiManager
import com.example.fastaf.ui.home.searchable.ResponseSearchRecItem
import com.example.fastaf.ui.util.PrefsUtils
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _drugsList = MutableLiveData<List<ResponseSearchRecItem>?>()
    val drugsList: LiveData<List<ResponseSearchRecItem>?> get() = _drugsList

    private val _selectedDrugId = MutableLiveData<Int?>()
    val selectedDrugId: LiveData<Int?> get() = _selectedDrugId

    private val _isAdmin = MutableLiveData<Boolean>()
    val isAdmin: LiveData<Boolean> get() = _isAdmin

    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> get() = _isRefreshing

    var currentPage = 0
    var isLastPage = false
    var isLoading = false
    private var loggedInUser: String? = null
    var _currentSearchQuery = ""

    private val _excludedDrugs = mutableSetOf<Int>()

    fun setUser(user: String?) {
        loggedInUser = user
    }

    fun resetData() {
        currentPage = 0
        isLastPage = false
        isLoading = false
        _drugsList.postValue(emptyList())
        _excludedDrugs.clear()
    }

    fun fetchDrugs(searchQuery: String = _currentSearchQuery) {
        if (isLoading || isLastPage) return

        isLoading = true
        _currentSearchQuery = searchQuery

        val user = loggedInUser ?: return

        viewModelScope.launch {
            try {
                Log.d("API_DEBUG", "Fetching drugs: page=$currentPage, query=$searchQuery, user=$user")
                val response = ApiManager.getWebService().getDrugs(
                    page = currentPage.toString(),
                    size = 20,
                    name = searchQuery,
                    username = user
                )

                if (response.isSuccessful) {
                    val newDrugs = response.body().orEmpty()

                    if (newDrugs.isNotEmpty()) {
                        val updatedList = newDrugs.filter { it.id !in _excludedDrugs }
                        val currentList = _drugsList.value.orEmpty().toMutableList()
                        currentList.addAll(updatedList)
                        _drugsList.postValue(currentList)
                        currentPage++
                    } else {
                        isLastPage = true
                    }
                } else {
                    Log.e("API_DEBUG", "API Error: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.e("API_DEBUG", "Exception: ${e.message}")
            } finally {
                isLoading = false
                _isRefreshing.postValue(false)
            }
        }
    }

    fun refreshDrugs() {
        _isRefreshing.value = true
        resetData()
        fetchDrugs(_currentSearchQuery)
    }

    fun onSearchQueryChanged(newQuery: String) {
        if (_currentSearchQuery != newQuery) {
            _currentSearchQuery = newQuery
            resetData()
            fetchDrugs(searchQuery = newQuery)
        }
    }

    fun setSelectedDrugId(id: Int?) {
        _selectedDrugId.value = id
    }

    fun setAdminStatus(isAdmin: Boolean) {
        _isAdmin.value = isAdmin
    }
}
