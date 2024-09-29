package com.gity.feliyaattendance.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gity.feliyaattendance.R
import com.gity.feliyaattendance.databinding.FragmentHomeBinding
import com.gity.feliyaattendance.helper.CommonHelper
import com.gity.feliyaattendance.repository.Repository
import com.gity.feliyaattendance.utils.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    private lateinit var repository: Repository
    private lateinit var viewModel: HomeViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
//        Start Of Your Code

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        repository = Repository(firebaseAuth, firebaseFirestore)
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        binding.apply {
            btnNotification.setOnClickListener {
                Toast.makeText(requireContext(), "Clicked", Toast.LENGTH_SHORT).show()
            }

            tvTopDay.text = CommonHelper.getCurrentDayOnly()
            tvTopDate.text = CommonHelper.getCurrentDateOnly()
            tvGreetings.text = CommonHelper.getGreetingsMessage(
                getString(R.string.greetings_good_morning),
                getString(R.string.greetings_good_afternoon),
                getString(R.string.greetings_good_evening)
            )
        }
        viewModel.fetchName()
        viewModel.nameResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { name ->
                binding.tvName.text = name
            }
            result.onFailure { exception ->
                Toast.makeText(
                    requireContext(),
                    "Failed to load name: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

//        // Real-time snapshot listener from Firestore
//        val userId = firebaseAuth.currentUser?.uid
//        if (userId != null) {
//            firebaseFirestore.collection("users").document(userId)
//                .addSnapshotListener { documentSnapshot, error ->
//                    if (error != null) {
//                        Toast.makeText(requireContext(), "Error fetching data", Toast.LENGTH_SHORT)
//                            .show()
//                        return@addSnapshotListener
//                    }
//
//                    if (documentSnapshot != null && documentSnapshot.exists()) {
//                        val name = documentSnapshot.getString("name")
//                        // Update the UI in real-time if the name changes in Firestore
//                        binding.tvName.text = name
//                    }
//                }
//        } else {
//            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
//        }
//        End of Your Code
        return binding.root
    }
}