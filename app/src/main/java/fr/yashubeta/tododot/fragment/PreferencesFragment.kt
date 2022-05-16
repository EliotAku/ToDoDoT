package fr.yashubeta.tododot.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import fr.yashubeta.tododot.UserPreferencesRepository
import fr.yashubeta.tododot.databinding.FragmentPreferencesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PreferencesFragment(private val userPrefsRepo: UserPreferencesRepository) : Fragment() {

    private var _binding: FragmentPreferencesBinding? = null
    private val binding get() = _binding!!

    private val preferencesFlow = userPrefsRepo.userPreferencesFlow.asLiveData()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreferencesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        preferencesFlow.observe(requireActivity()) {
            binding.prefSwitch.isChecked = it.showSubTasks
        }
        binding.prefSwitch.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                userPrefsRepo.updateShowSubTasks(binding.prefSwitch.isChecked)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        preferencesFlow.removeObservers(requireActivity())

        _binding = null
    }

}