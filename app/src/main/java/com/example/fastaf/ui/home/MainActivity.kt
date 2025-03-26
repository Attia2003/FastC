package com.example.fastaf.ui.home

import android.content.Intent
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
import com.example.fastaf.ui.cam.CamActivity
import com.example.fastaf.ui.createuser.AdminUserActivity
import com.example.fastaf.ui.home.searchable.ResponseSearchRecItem
import com.example.fastaf.ui.home.searchable.SearchRecAdapter
import com.example.fastaf.ui.input.InputNewDrugActivity
import com.example.fastaf.ui.util.PrefsUtils


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
        setupAddDrugButton()
        navigateToAdminUser()
        setupSwipeToRefresh()

        val loggedInUser = PrefsUtils.getUserFromPrefs(this)
        viewModel.setUser(loggedInUser)

        val isAdmin = intent.getBooleanExtra("isAdmin", false)
        viewModel.setAdminStatus(isAdmin)

        val shouldRefresh = intent.getBooleanExtra("REFRESH_DRUGS", false)
        if (shouldRefresh) viewModel.resetData()

        viewModel.fetchDrugs(viewModel._currentSearchQuery)
    }

    private fun setupRecyclerView() {
        adapter = SearchRecAdapter(
            onCameraClicked = { drugId ->
                openCamera(drugId)
            }
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

                if (!viewModel.isLoading && !viewModel.isLastPage &&
                    visibleItemCount + firstVisibleItemPosition >= totalItemCount - 5 && firstVisibleItemPosition >= 0
                ) {
                    viewModel.fetchDrugs(viewModel._currentSearchQuery)
                }
            }
        })
    }

    private fun setupObservers() {
        viewModel.drugsList.observe(this) { drugs ->
            drugs?.let {
                if (viewModel.currentPage == 0) {
                    adapter.updateData(it)
                } else {
                    adapter.updateData(it)
                }
            }
        }

        viewModel.selectedDrugId.observe(this) { id ->
            id?.let {
                openCamera(it)
                viewModel.setSelectedDrugId(null)
            }
        }

        viewModel.isRefreshing.observe(this) { isRefreshing ->
            binding.swipeRefreshLayout.isRefreshing = isRefreshing
        }
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.onSearchQueryChanged(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun openCamera(drugId: Int) {
        val loggedInUser = PrefsUtils.getUserFromPrefs(this) ?: "Unknown"
        val intent = Intent(this, CamActivity::class.java).apply {
            putExtra("DRUG_ID", drugId)
            putExtra("USER_NAME", loggedInUser)
        }
        startActivity(intent)
    }

    private fun setupAddDrugButton() {
        binding.GoToInput.setOnClickListener {
            val intent = Intent(this, InputNewDrugActivity::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToAdminUser() {
        val isAdmin = intent.getBooleanExtra("isAdmin", false)
        Log.d("MainActivity", "Admin status: $isAdmin")

        if (isAdmin) {
            binding.IconAdminCreateUser.visibility = View.VISIBLE
        } else {
            binding.IconAdminCreateUser.visibility = View.GONE
        }
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshDrugs()
        }
    }

}