package com.realworld.android.petsave.easteregg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.realworld.android.petsave.easteregg.databinding.FragmentSecretBinding

class SecretFragment : Fragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentSecretBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecretBinding.inflate(inflater, container, false)

        return binding.root
    }
}