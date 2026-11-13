package com.alice.task.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.alice.task.R
import com.alice.task.data.model.Status
import com.alice.task.data.model.Task
import com.alice.task.databinding.FragmentFormTaskBinding

class FormTaskFragment : Fragment() {
    private val database = FirebaseDatabase.getInstance().reference
    private var _binding: FragmentFormTaskBinding? = null
    private val binding get() = _binding!!
    private lateinit var task: Task
    private var newTask: Boolean = true
    private var status: Status = Status.TODO

    private val viewModel: TaskViewModel by activityViewModels()
    private val args: FormTaskFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFormTaskBinding.inflate(inflater,container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getArgs()
        initListener()
    }

    private fun getArgs(){
        args.task.let{
            if (it != null){
                this.task = it
                configTask()
            }
        }
    }

    private fun configTask(){
        newTask=false
        status=task.status
        binding.textToolbar.setText(R.string.text_toolbar_update_form_task_fragment)

        binding.textToolbar.text="Editando..."
        binding.editTextDescricao.setText(task.description)
        setStatus()
    }

    private fun setStatus(){
        val id=when(task.status){
            Status.TODO -> R.id.rbTodo
            Status.DOING -> R.id.rbDoing
            else -> R.id.rbDone
        }
        binding.radioGroup.check(id)
    }

    private fun initListener(){
        binding.buttonSave.setOnClickListener{
            validateData()
        }
        binding.radioGroup.setOnCheckedChangeListener { _, id -> status = when(id){
            R.id.rbTodo -> Status.TODO
            R.id.rbDoing -> Status.DOING
            else -> Status.DONE

        } }
    }

    private fun validateData(){
        val description = binding.editTextDescricao.text.toString().trim()
        if(description.isNotBlank()){
            binding.progressBar.isVisible = true

            if(newTask) task = Task()

            task.description=description
            task.status=status
            saveTask()
        }else{
            Toast.makeText(requireContext(),"Preencha uma descrição!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveTask() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            binding.progressBar.isVisible = false
            Toast.makeText(requireContext(), "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newTask) {
            task.id = database.child("task").child(userId).push().key ?: ""
        }

        if (task.id.isBlank()) {
            binding.progressBar.isVisible = false
            Toast.makeText(requireContext(), "Não foi possível gerar o ID da tarefa.", Toast.LENGTH_SHORT).show()
            return
        }

        database.child("task")
            .child(userId)
            .child(task.id)
            .setValue(task)
            .addOnCompleteListener { result ->
                binding.progressBar.isVisible = false

                if (result.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        if (newTask) "Tarefa cadastrada com sucesso." else "Tarefa atualizada com sucesso.",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(
                        requireContext(),
                        result.exception?.message ?: getString(R.string.error_generic),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
