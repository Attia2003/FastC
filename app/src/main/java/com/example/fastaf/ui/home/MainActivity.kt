package com.example.fastaf.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fastaf.databinding.ActivityMainBinding
import com.example.fastaf.ui.home.searchable.ResponseSearchRecItem
import com.example.fastaf.ui.home.searchable.SearchRecAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: SearchRecAdapter
    private var searchJob: Job? = null
    private var isFiltered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        setupRecyclerView()
        observeViewModel()
        setupSearch()
        setupSpinner()
        viewModel.getDrugs(isFiltered = false) // Initial fetch with no filter
    }

    private fun setupRecyclerView() {
        adapter = SearchRecAdapter()
        binding.RecylerDrugs.adapter = adapter

        binding.RecylerDrugs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (!viewModel.isLoading && lastVisibleItemPosition + 5 >= totalItemCount) {
                    viewModel.getDrugs(isFiltered)
                }
            }
        })
    }

    private fun observeViewModel() {
        viewModel.drugsList.observe(this) { drugs ->
            if (!isFiltered) updateRecyclerView(drugs)
        }

        viewModel.filteredDrugs.observe(this) { drugs ->
            if (isFiltered) updateRecyclerView(drugs)
        }
    }

    private fun updateRecyclerView(drugs: List<ResponseSearchRecItem>) {
        Log.d("OBSERVE", "RecyclerView update: ${drugs.size} items")

        if (drugs.isEmpty()) {
            Log.w("OBSERVE", "No data available! Check API response.")
        }

        drugs.forEach { Log.d("OBSERVE_ITEM", "ID: ${it.id}, Name: ${it.name}, Status: ${it.status}") }
        adapter.updateData(drugs)
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchJob?.cancel()
                searchJob = MainScope().launch {
                    delay(300)
                    viewModel.searchDrugs(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupSpinner() {
        
        val drugForms = listOf(
            "all", "Granules", "Lotion", "Tablet", "Nose Drops", "Injection", "Infusion",
            "Film", "Other", "Spray", "Syrup", "Hair Treatment",
            "Gargle", "Oral Drop", "Paint", "Inhalations", "Cream", "Sachets",
            "Powder", "Eye Drops", "Suppositories", "Patch", "Ear Drops",
            "Solution", "Effervescent", "Gel", "Lozenges", "Capsule"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, drugForms)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = adapter

        binding.spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedForm = parent.getItemAtPosition(position).toString()
                isFiltered = selectedForm != "all"
                if (isFiltered) {
                    viewModel.updateDrugForm(selectedForm)
                } else {
                    viewModel.resetAndFetchAllData()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}
