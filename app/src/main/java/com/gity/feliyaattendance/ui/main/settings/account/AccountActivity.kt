package com.gity.feliyaattendance.ui.main.settings.account

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.databinding.ActivityAccountBinding
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountBinding
    private val viewModel: AccountViewModel by viewModels {
        ViewModelFactory(Repository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAccountBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

        viewModel.loadUserDetails(userId)

        viewModel.userDetails.observe(this) { result ->
            result.onSuccess { user ->
                binding.apply {
                    tvUserName.text = user.name
                    tvUserEmail.text = user.email

                    user.profileImageUrl?.let { imageUrl ->
                        Glide.with(this@AccountActivity)
                            .load(imageUrl)
                            .placeholder(R.drawable.iv_placeholder) // Gambar placeholder
                            .error(R.drawable.iv_placeholder) // Gambar error jika gagal load
                            .into(ivUserProfile)
                    }
                }
            }
            result.onFailure { e ->
                e.printStackTrace()
            }
        }


        handleBackButton()
    }

    private fun handleBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        onBackPressedDispatcher.addCallback(this) {
            finish()
        }
    }
}