package com.example.myspendyapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.myspendyapp.R
import com.example.myspendyapp.databinding.FragmentChangePasswordBinding

class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
            return binding.root
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("ChangePasswordFragment", "Error inflating layout", e)
            // Trả về một View đơn giản nếu có lỗi
            return android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                addView(android.widget.TextView(requireContext()).apply {
                    text = "Lỗi tải layout: ${e.message}"
                    setPadding(16, 16, 16, 16)
                })
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]

            setupToolbar()
            setupSaveButton()
            observeViewModel()
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("ChangePasswordFragment", "Error in onViewCreated", e)
            android.widget.Toast.makeText(requireContext(), "Lỗi khởi tạo: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun setupToolbar() {
        try {
            binding.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
        } catch (e: Exception) {
            android.util.Log.e("ChangePasswordFragment", "Error setting up toolbar", e)
        }
    }

    private fun setupSaveButton() {
        try {
            binding.btnSave.setOnClickListener {
                changePassword()
            }
        } catch (e: Exception) {
            android.util.Log.e("ChangePasswordFragment", "Error setting up save button", e)
        }
    }

    private fun changePassword() {
        val currentPassword = binding.edtCurrentPassword.text?.toString()?.trim() ?: ""
        val newPassword = binding.edtNewPassword.text?.toString()?.trim() ?: ""
        val confirmPassword = binding.edtConfirmPassword.text?.toString()?.trim() ?: ""

        // Validation
        if (currentPassword.isEmpty()) {
            binding.edtCurrentPassword.error = "Vui lòng nhập mật khẩu hiện tại"
            return
        }

        if (newPassword.isEmpty()) {
            binding.edtNewPassword.error = "Vui lòng nhập mật khẩu mới"
            return
        }

        if (newPassword.length < 6) {
            binding.edtNewPassword.error = "Mật khẩu mới phải có ít nhất 6 ký tự"
            return
        }

        if (newPassword != confirmPassword) {
            binding.edtConfirmPassword.error = "Mật khẩu xác nhận không khớp"
            return
        }

        if (currentPassword == newPassword) {
            Toast.makeText(requireContext(), "Mật khẩu mới phải khác mật khẩu hiện tại", Toast.LENGTH_SHORT).show()
            return
        }

        // Gọi ViewModel để thay đổi mật khẩu
        authViewModel.changePassword(currentPassword, newPassword)
    }

    private fun observeViewModel() {
        authViewModel.changePasswordResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AuthResult.Loading -> {
                    binding.btnSave.isEnabled = false
                    binding.btnSave.text = "Đang xử lý..."
                }
                is AuthResult.Success -> {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Lưu mật khẩu mới"
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is AuthResult.Error -> {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Lưu mật khẩu mới"
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
                is AuthResult.Idle -> {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Lưu mật khẩu mới"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


