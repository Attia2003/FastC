package com.example.fastaf.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.fastaf.R
import com.example.fastaf.databinding.ActivityLoginActivtyBinding
import com.example.fastaf.ui.home.MainActivity
import com.example.fastaf.ui.util.PrefsUtils

class LoginActivty : AppCompatActivity() {
    private lateinit var binding: ActivityLoginActivtyBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginActivtyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        subscribeToLiveData()


        binding.loginbtn.setOnClickListener {
            viewModel.userName.value = binding.UserName.text.toString()
            viewModel.login(this)
        }
    }

    private fun initView() {
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
    }

    private fun subscribeToLiveData() {
        viewModel.errorUserName.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }

        viewModel.loggedInUser.observe(this) { user ->
            if (user.isNotEmpty()) startHome()
        }

    }

    private fun startHome() {
        val isAdmin = PrefsUtils.getUserRole(this) == "ADMIN"
        val user = PrefsUtils.getUserFromPrefs(this)

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("isAdmin", isAdmin)
            putExtra("user", user)
        }
        startActivity(intent)
        finish()
    }

}

