package fr.yashubeta.tododot.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import fr.yashubeta.tododot.MainViewModel
import fr.yashubeta.tododot.R
import fr.yashubeta.tododot.database.Todo
import fr.yashubeta.tododot.databinding.FragmentBottomSheetAddTodoBinding
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch


// TODO: Customize parameter argument names
const val ARG_ITEM_COUNT = "item_count"

class AddTodoDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentBottomSheetAddTodoBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomSheetAddTodoBinding.inflate(inflater, container, false)

        binding.editTextTitle.requestFocus()
        requireDialog().window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        setIfButtonSaveEnable()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //val itemCount = arguments?.getInt(ARG_ITEM_COUNT)

        binding.buttonLeft.setOnClickListener { dismiss() }

        binding.buttonSave.setOnClickListener {
            if (!binding.editTextTitle.text.isNullOrEmpty()) {
                val todoTitle = binding.editTextTitle.text.toString()
                val todoNote = binding.editTextNote.text.toString()
                addTodo(todoTitle, todoNote)
            }
        }

        binding.editTextTitle.doOnTextChanged { _, _, _, _ ->
            setIfButtonSaveEnable()
        }

    }

    private fun setIfButtonSaveEnable() {
        binding.buttonSave.isEnabled = !binding.editTextTitle.text.isNullOrEmpty()
    }

    private fun addTodo(title: String, note: String) {

        lifecycleScope.launch(IO) {
            val todo = Todo(0, 0, title, note)
            viewModel.insertTodo(todo)
            dismiss()
        }

    }

    companion object {
        // TODO: Customize parameters
        fun newInstance(itemCount: Int): AddTodoDialogFragment =
            AddTodoDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ITEM_COUNT, itemCount)
                }
            }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireActivity().findViewById<ExtendedFloatingActionButton>(R.id.floating_action_button)
            .show()
    }
}