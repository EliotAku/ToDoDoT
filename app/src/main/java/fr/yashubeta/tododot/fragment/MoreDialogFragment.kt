package fr.yashubeta.tododot.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import fr.yashubeta.tododot.R
import fr.yashubeta.tododot.UserPreferencesRepository
import fr.yashubeta.tododot.databinding.FragmentBottomSheetMoreBinding

class MoreDialogFragment(private val userPreferencesRepository: UserPreferencesRepository) : BottomSheetDialogFragment() {

    private var _binding: FragmentBottomSheetMoreBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomSheetMoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            childFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragment_container,
                    PreferencesFragment(userPreferencesRepository),
                    "BottomSheet"
                )
                .commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}