package com.example.myspendyapp.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myspendyapp.R
import com.example.myspendyapp.databinding.FragmentCategoryBinding

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Thay thế ViewPager bằng CategoryListFragment trực tiếp
        if (childFragmentManager.findFragmentById(R.id.viewPager) == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.viewPager, CategoryListFragment())
                .commit()
        }

        setupFab()
    }

    private fun setupFab() {
        binding.fabAddCategory.setOnClickListener {
            findNavController().navigate(R.id.action_category_to_create_category)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
