package com.example.fastaf.ui.input

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.fastaf.R
import com.example.fastaf.databinding.ActivityInputNewDrugBinding
import com.example.fastaf.ui.home.MainActivity

class InputNewDrugActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputNewDrugBinding
    private val viewModel: InputDrugViewModel by viewModels()
    private var lastSelectedForm: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_input_new_drug)
        binding.vm = viewModel
        binding.lifecycleOwner = this

        setupSpinner()

        binding.submitnewdrug.setOnClickListener {
            viewModel.createDrug()
        }

        observeViewModel()
    }

    private fun setupSpinner() {
        val forms = resources.getStringArray(R.array.form_drug_filter)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, forms)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerform.adapter = adapter


        lastSelectedForm?.let { savedForm ->
            val savedPosition = forms.indexOf(savedForm)
            if (savedPosition != -1) {
                binding.spinnerform.setSelection(savedPosition)
            }
        }

        binding.spinnerform.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedForm = parent.getItemAtPosition(position).toString()
                viewModel.selectedForm.value = selectedForm


                lastSelectedForm = selectedForm
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun observeViewModel() {
        viewModel.isSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(this, "Drug created successfully!", Toast.LENGTH_SHORT).show()

                binding.UserName.text.clear()
                binding.inputNameLabel.requestFocus()
            }
        }
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.submitnewdrug.isEnabled = !isLoading
        }
        viewModel.navigateToMain.observe(this) { shouldNavigate ->
            if (shouldNavigate) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("REFRESH_DRUGS", true)
                startActivity(intent)
                finish()
            }
        }
    }
}