package org.softwaremaestro.presenter.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.softwaremaestro.presenter.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}