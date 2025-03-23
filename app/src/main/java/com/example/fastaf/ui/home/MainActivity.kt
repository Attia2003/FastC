package com.example.fastaf.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fastaf.R
import com.example.fastaf.databinding.ActivityMainBinding
import com.example.fastaf.ui.home.searchable.ResponseSearchRecItem
import com.example.fastaf.ui.home.searchable.SearchRecAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: SearchRecAdapter

    private var isLoadingMore = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        setupFilters()
        setupSearch()

        viewModel.getDrugs(false)
    }

    private fun setupRecyclerView() {
        adapter = SearchRecAdapter(
            onCameraClicked = { drugId -> viewModel.setSelectedDrugId(drugId) },
            onDeleteClick = { drug -> showConfirmDiscardDialog(drug) }
        )

        binding.RecylerDrugs.adapter = adapter
        binding.RecylerDrugs.layoutManager = LinearLayoutManager(this)

        binding.RecylerDrugs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!viewModel.isLoading && !viewModel.isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount &&
                        firstVisibleItemPosition >= 0 &&
                        totalItemCount >= 20
                    ) {
                        viewModel.getDrugs(isFiltered = viewModel.isFiltered, searchQuery = viewModel._currentSearchQuery)
                    }
                }
            }
        })

    }

    private fun setupObservers() {
        viewModel.drugsList.observe(this) { drugs ->
            if (!viewModel.isFiltered) adapter.updateData(drugs)
            isLoadingMore = false
        }

        viewModel.filteredDrugs.observe(this) { drugs ->
            if (viewModel.isFiltered) adapter.updateData(drugs)
            isLoadingMore = false
        }

        viewModel.formFilter.observe(this) { form ->
            Log.d("FILTER", "Form filter set to: $form")
        }

        viewModel.statusFilter.observe(this) { status ->
            Log.d("FILTER", "Status filter set to: $status")
        }

        viewModel.selectedDrugId.observe(this) { id ->
            id?.let {
                Toast.makeText(this, "Selected drug ID: $it", Toast.LENGTH_SHORT).show()
                viewModel.setSelectedDrugId(null)
            }
        }
    }

    private fun setupFilters() {

        ArrayAdapter.createFromResource(
            this,
            R.array.form_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerform.adapter = adapter
        }

        binding.spinnerform.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedForm = parent.getItemAtPosition(position).toString()
                viewModel.updateDrugForm(selectedForm)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.status_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerstatus.adapter = adapter
        }

        binding.spinnerstatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedStatus = parent.getItemAtPosition(position).toString()
                viewModel.updateStatusFilter(selectedStatus)
                viewModel.resetPages()
                viewModel.getDrugs(isFiltered = true, searchQuery = viewModel._currentSearchQuery)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.searchDrugs(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun showConfirmDiscardDialog(drug: ResponseSearchRecItem) {
        AlertDialog.Builder(this)
            .setTitle("Move to Discarded?")
            .setMessage("Are you sure you want to move ${drug.name} to DISCARDED?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.updateDrugStatus(drug, "DISCARDED")
                Toast.makeText(this, "${drug.name} moved to DISCARDED", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

}