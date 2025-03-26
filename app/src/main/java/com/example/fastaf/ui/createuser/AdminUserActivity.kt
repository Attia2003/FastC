package com.example.fastaf.ui.createuser

import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.fastaf.R
import com.example.fastaf.databinding.ActivityAdminUserBinding

class AdminUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminUserBinding
    private lateinit var viewModel: CreateUserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_admin_user)
        viewModel = ViewModelProvider(this)[CreateUserViewModel::class.java]

        binding.vm = viewModel
        binding.lifecycleOwner = this

        setupSpinner()
        observeStatus()
    }

    private fun setupSpinner() {
        val spinner = binding.spinnerrole
        val roles = resources.getStringArray(R.array.roles)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {

                viewModel.selectedRole.value = roles[position].uppercase()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeStatus() {
        viewModel.createUserStatus.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            if (message == "User created successfully!") {
                finish()
            }
        }
    }
}