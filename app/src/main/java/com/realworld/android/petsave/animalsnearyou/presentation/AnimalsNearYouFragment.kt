package com.realworld.android.petsave.animalsnearyou.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.realworld.android.petsave.common.presentation.AnimalsAdapter
import com.realworld.android.petsave.databinding.FragmentAnimalsNearYouBinding

class AnimalsNearYouFragment : Fragment() {
    companion object {
        private const val ITEMS_PER_ROW = 2
    }

    private val binding get() = _binding!!

    private var _binding: FragmentAnimalsNearYouBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnimalsNearYouBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupUi()
    }

    // View가 파괴될 때 RecyclerView도 함께 파괴되는데 Fragment가 Adapter를 참조하는 경우,
    // Adapter와 RecyclerView가 순환 종속성을 가지므로 가비지 컬렉터가 RecyclerView 인스턴스를 수집할 수 없다
    private fun setupUi() {
        val adapter = createAdapter()
        setupRecyclerView(adapter)
    }

    private fun createAdapter(): AnimalsAdapter = AnimalsAdapter()

    private fun setupRecyclerView(animalsNearYouAdapter: AnimalsAdapter) {
        binding.animalsRecyclerView.apply {
            adapter = animalsNearYouAdapter
            layoutManager = GridLayoutManager(requireContext(), ITEMS_PER_ROW)
            setHasFixedSize(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}