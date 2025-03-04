package com.example.fastaf.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.fastaf.R
import com.example.fastaf.databinding.ActivityLoginActivtyBinding
import com.example.fastaf.ui.home.MainActivity

class LoginActivty : AppCompatActivity() {
    private lateinit var binding: ActivityLoginActivtyBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_activty)


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initView()
        subscribeToLiveData()
    }

    private fun initView() {

        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        binding.vm = viewModel
        binding.lifecycleOwner = this
    }

    private fun subscribeToLiveData() {
        Log.d("funcStart", "Event received:")
        viewModel.event.observe(this) {
            when (it) {
                LoginViewEvent.NavigateToHome -> StartHome()
            }
        }
    }

    private fun StartHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
