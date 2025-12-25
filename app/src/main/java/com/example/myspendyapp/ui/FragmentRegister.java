package com.example.myspendyapp.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myspendyapp.R;
import com.example.myspendyapp.databinding.FragmentRegisterBinding;

public class FragmentRegister extends Fragment {

    private FragmentRegisterBinding binding;
    private AuthViewModel authViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Khởi tạo ViewModel được chia sẻ với Activity
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        setupClickListeners();
        observeRegisterResult();
    }

    private void setupClickListeners() {
        binding.btnRegister.setOnClickListener(v -> {
            String username = binding.edtUsername.getText().toString().trim();
            String phone = binding.edtPhoneNumber.getText().toString().trim();
            String password = binding.edtPassword.getText().toString().trim();
            String confirmPassword = binding.edtRePassword.getText().toString().trim();

            if (!isInputValid(username, phone, password, confirmPassword)) {
                return;
            }

            authViewModel.register(username, phone, password, null);
        });
    }

    private void observeRegisterResult() {
        authViewModel.getRegisterResult().observe(getViewLifecycleOwner(), result -> {
            if (result instanceof AuthResult.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnRegister.setEnabled(false);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnRegister.setEnabled(true);
            }

            if (result instanceof AuthResult.Success) {
                String successMessage = ((AuthResult.Success) result).getMessage();
                Toast.makeText(getContext(), successMessage, Toast.LENGTH_LONG).show();

                String username = binding.edtUsername.getText().toString().trim();
                String password = binding.edtPassword.getText().toString().trim();

                Bundle bundle = new Bundle();
                bundle.putString("username", username);
                bundle.putString("password", password);
                authViewModel.resetRegisterResult();
            }
            else if (result instanceof AuthResult.Error) {
                String errorMessage = ((AuthResult.Error) result).getMessage();
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                authViewModel.resetRegisterResult();
            }
            binding.txtRegister.setOnClickListener(v -> {

                NavHostFragment.findNavController(FragmentRegister.this)
                        .navigate(R.id.action_register_to_login);
            });
        });
    }

    private boolean isInputValid(String username, String phone, String password, String confirmPassword) {
        if (TextUtils.isEmpty(username)) {
            binding.edtUsername.setError("Tên đăng nhập không được để trống.");
            binding.edtUsername.requestFocus();
            return false;
        }
        if (username.length() < 5) {
            binding.edtUsername.setError("Tên đăng nhập phải có ít nhất 5 ký tự.");
            binding.edtUsername.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(phone)) {
            binding.edtPhoneNumber.setError("Số điện thoại không được để trống.");
            binding.edtPhoneNumber.requestFocus();
            return false;
        }
        if (!Patterns.PHONE.matcher(phone).matches() || phone.length() < 10) {
            binding.edtPhoneNumber.setError("Định dạng số điện thoại không hợp lệ.");
            binding.edtPhoneNumber.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            binding.edtPassword.setError("Mật khẩu không được để trống.");
            binding.edtPassword.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            binding.edtPassword.setError("Mật khẩu phải có ít nhất 6 ký tự.");
            binding.edtPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.edtRePassword.setError("Vui lòng xác nhận mật khẩu.");
            binding.edtRePassword.requestFocus();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            binding.edtRePassword.setError("Mật khẩu xác nhận không khớp.");
            binding.edtRePassword.requestFocus();
            return false;
        }

        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
