package com.example.fastaf.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.fastaf.R
import com.example.fastaf.ui.login.LoginActivty
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class splashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        startsplash()

    }
    private fun startsplash(){
        lifecycleScope.launch {
            delay(3000)
            val intent = Intent(this@splashActivity, LoginActivty::class.java)
            startActivity(intent)
            finish()
        }
    }
}