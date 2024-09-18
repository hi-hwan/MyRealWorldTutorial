package com.realworld.android.petsave.animalsnearyou.presentation.animaldetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.realworld.android.petsave.animalsnearyou.R
import com.realworld.android.petsave.animalsnearyou.databinding.FragmentDetailsBinding
import com.realworld.android.petsave.animalsnearyou.presentation.animaldetails.model.UIAnimalDetailed
import com.realworld.android.petsave.common.utils.setImage
import com.realworld.android.petsave.common.utils.toEnglish
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

import com.realworld.android.petsave.common.R as commonR

@AndroidEntryPoint
class AnimalDetailsFragment : Fragment() {

    companion object {
        const val ANIMAL_ID = "id"
    }

    private val binding get() = _binding!!
    private var _binding: FragmentDetailsBinding? = null

    private val viewModel: AnimalDetailsFragmentViewModel by viewModels()

    private var animalId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        animalId = requireArguments().getLong(ANIMAL_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addShareMenu()
        subscribeToStateUpdates()
        val event = AnimalDetailsEvent.LoadAnimalDetails(animalId!!)
        viewModel.handleEvent(event)
    }

    private fun addShareMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_share, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return if (menuItem.itemId == R.id.share) {
                    navigateToSharing()
                    true
                } else {
                    false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun navigateToSharing() {
        val animalId = requireArguments().getLong(ANIMAL_ID)

        val deepLink = NavDeepLinkRequest.Builder
            .fromUri("petsave://sharing/$animalId".toUri())
            .build()

        findNavController().navigate(deepLink)
    }

    private fun subscribeToStateUpdates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is AnimalDetailsViewState.Loading -> {
                            displayLoading()
                        }

                        is AnimalDetailsViewState.Failure -> {
                            displayError()
                        }

                        is AnimalDetailsViewState.AnimalDetails -> {
                            displayPetDetails(state.animal)
                        }
                    }
                }
            }
        }
    }

    private fun displayPetDetails(animalDetails: UIAnimalDetailed) {
        binding.group.isVisible = true
        binding.name.text = animalDetails.name
        binding.description.text = animalDetails.description
        binding.image.setImage(animalDetails.photo)
        binding.sprayedNeutered.text = animalDetails.sprayNeutered.toEnglish()
        binding.specialNeeds.text = animalDetails.specialNeeds.toEnglish()
    }

    private fun displayError() {
        binding.group.isVisible = false
        Snackbar.make(requireView(), commonR.string.an_error_occurred, Snackbar.LENGTH_SHORT).show()
    }

    private fun displayLoading() {
        binding.group.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}