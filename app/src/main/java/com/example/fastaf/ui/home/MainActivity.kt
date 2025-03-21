package com.example.fastaf.ui.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fastaf.databinding.ActivityMainBinding
import com.example.fastaf.ui.cam.CamActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        setupSearch()
        setupFilterSpinner()

        viewModel.getDrugs(isFiltered = false)
    }
    private fun setupRecyclerView() {
        adapter = SearchRecAdapter(
            onDeleteClick = { viewModel.setSelectedDrugId(it) },
            onCameraClicked = { viewModel.setSelectedDrugId(it) }

        )
        binding.RecylerDrugs.layoutManager = LinearLayoutManager(this)
        binding.RecylerDrugs.adapter = adapter


        binding.RecylerDrugs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount


                val isAllFilter = viewModel.formFilter.value == "ALL"
                val currentList = if (isAllFilter) viewModel.drugsList.value else viewModel.filteredDrugs.value

                if (!viewModel.isLoading && lastVisibleItem >= totalItemCount - 5 && currentList?.isNotEmpty() == true) {
                    viewModel.getDrugs(
                        isFiltered = !isAllFilter,
                        searchQuery = viewModel._currentSearchQuery
                    )
                }
            }
        })
    }

    private fun setupObservers() {
        viewModel.drugsList.observe(this, Observer { drugs ->
            adapter.updateData(drugs ?: emptyList())
        })

        viewModel.filteredDrugs.observe(this, Observer { drugs ->
            adapter.updateData(drugs ?: emptyList())
        })

        viewModel.openCameraEvent.observe(this, Observer { drugId ->
            openCam(drugId)
        })
    }


    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.searchDrugs(s.toString())
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {}
        })
    }


    private fun setupFilterSpinner() {
        val spinner: Spinner = binding.spinnerform
        val filters = listOf(
            "ALL", "Granules", "Lotion", "Tablet", "Nose Drops", "Injection", "Infusion",
            "Film", "Other", "Spray", "Syrup", "Hair Treatment",
            "Gargle", "Oral Drop", "Paint", "Inhalations", "Cream", "Sachets",
            "Powder", "Eye Drops", "Suppositories", "Patch", "Ear Drops",
            "Solution", "Effervescent", "Gel", "Lozenges", "Capsule")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filters)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                val selectedForm = parent.getItemAtPosition(position).toString()
                viewModel.updateDrugForm(selectedForm)
            }            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }
    private fun openCam(drugId: Int) {
        startActivity(Intent(this, CamActivity::class.java).apply { putExtra("DRUG_ID", drugId) })
    }

}