package com.alice.task.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.alice.task.R
import com.alice.task.databinding.FragmentRecoverAccountBinding

class RecoverAccountFragment : Fragment() {

    private var _binding: FragmentRecoverAccountBinding?=null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentRecoverAccountBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        initListener()
    }

    private fun initListener(){
        binding.btnRegistrar.setOnClickListener{
            validateData()
        }
    }

    private fun validateData(){
        val email = binding.digiteEmail.text.toString().trim()

        if(email.isNotBlank()){
            binding.progressBar.isVisible = true
            recoverAccountUser(email)
        }else{
            Toast.makeText(requireContext(), getString(R.string.email_empty), Toast.LENGTH_SHORT).show()
        }
    }

    private fun recoverAccountUser(email: String ){
        try {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    binding.progressBar.isVisible = false
                    if (task.isSuccessful){
                        Toast.makeText(requireContext(), "Um e-mail de recuperação foi enviado.", Toast.LENGTH_LONG).show()
                    }else{
                        Toast.makeText(
                            requireContext(),
                            task.exception?.localizedMessage ?: getString(R.string.error_generic),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        } catch (e: Exception) {
            binding.progressBar.isVisible = false
            Toast.makeText(requireContext(), e.message ?: getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding =null
    }

}