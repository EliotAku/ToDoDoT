package fr.yashubeta.tododot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import fr.yashubeta.tododot.database.Todo
import fr.yashubeta.tododot.databinding.FragmentBottomSheetTodoBinding

class TodoDialogFragment(
    private val viewModel: MainViewModel,
    private val todo: Todo
) : BottomSheetDialogFragment() {

    private var _binding: FragmentBottomSheetTodoBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomSheetTodoBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setCheckCircle()

        binding.textViewTodoTitle.doAfterTextChanged {
            val newTodo = todo.apply { title = it.toString() }
            if (newTodo.title.isNotEmpty())
                viewModel.updateTodo(newTodo)
        }

        binding.textViewTodoNote.doAfterTextChanged {
            val newTodo = todo.apply { note = it.toString() }
            viewModel.updateTodo(newTodo)
        }

        binding.imageButtonDelete.setOnClickListener {
            viewModel.deleteTodo(todo)
            dismiss()
        }

        binding.imageButtonCheckCircle.setOnClickListener {
            viewModel.updateTodo(todo.apply { isChecked = !isChecked })
            setCheckCircle()
        }

    }

    override fun onStart() {
        super.onStart()
        binding.textViewTodoTitle.setText(todo.title)
        binding.textViewTodoNote.setText(todo.note)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setCheckCircle(){
        if (todo.isChecked)
            binding.imageButtonCheckCircle.setImageResource(R.drawable.ic_check_circle_checked)
        else
            binding.imageButtonCheckCircle.setImageResource(R.drawable.ic_check_circle_unchecked)
    }

}