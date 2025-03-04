package com.example.fastaf.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fastaf.R
import com.example.fastaf.databinding.ActivityMainBinding
import com.example.fastaf.ui.home.searchable.SearchRecAdapter

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var viewModel: MainViewModel
    lateinit var adapter: SearchRecAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        enableEdgeToEdge()
        println("MainActivity onCreate started!")
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initview()
        SubcribeToLiveData()
        viewModel.fetchDrugs()
        Log.d("drugCall", "Event received:")
        sctollpagna()
        search()
        StatusSpinner()
    }
    fun initview(){
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        binding.vm = viewModel
        binding.lifecycleOwner = this
        adapter = SearchRecAdapter()
        binding.RecylerDrugs.adapter = adapter


    }

    fun SubcribeToLiveData() {
        Log.d("funcStartAPI", "Event received:")
        viewModel.filteredDrugs.observe(this) { drugs ->
            Log.d("LIVEDATA_UPDATE", "New drugs list: ${drugs?.size}")
            adapter.updateData(drugs)
        }
        viewModel.loadMore.observe(this) {
            binding.Progresspar.visibility = if (it) View.VISIBLE else View.GONE
        }
    }


    private fun sctollpagna() {
        binding.RecylerDrugs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                Log.d("SCROLL_EVENT", "Last visible: $lastVisibleItem, Total: $totalItemCount")

                if (!viewModel.isLoading && lastVisibleItem >= totalItemCount - 1) {
                    Log.d("LOAD_MORE_TRIGGER", "Loading more data")
                    viewModel.onLoadMoreNeeded()
                }
            }
        })
    }

    private fun search() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                Log.d("SEARCH_EVENT", "User typed: ${s.toString()}")
                viewModel.searchDrugs(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    private fun StatusSpinner() {
        val statuses = listOf( "AVAILABLE", "DISCARDED", "POSTPONED")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = adapter

        binding.spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedStatus = statuses[position]
                Log.d("STATUS_FILTER", "Selected status: $selectedStatus")
                viewModel.updateStatus(selectedStatus)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }



    }