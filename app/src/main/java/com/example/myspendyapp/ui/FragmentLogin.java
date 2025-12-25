package com.example.myspendyapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myspendyapp.R;
import com.example.myspendyapp.databinding.FragmentLoginBinding;

public class FragmentLogin extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel authViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        if (getArguments() != null) {
            String username = getArguments().getString("username");
            String password = getArguments().getString("password");
            binding.edtUsername.setText(username);
            binding.edtPassword.setText(password);
        }
        setupClickListeners();
        observeLoginResult();
    }

    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String username = binding.edtUsername.getText().toString().trim();
            String password = binding.edtPassword.getText().toString().trim();

            if (!isInputValid(username, password)) {
                return;
            }
            authViewModel.login(username, password);
        });
        binding.txtRegister.setOnClickListener(v -> {
            NavHostFragment.findNavController(FragmentLogin.this)
                    .navigate(R.id.action_login_to_register);
        });
        binding.forgotPassword.setOnClickListener(v -> {
            NavHostFragment.findNavController(FragmentLogin.this)
                    .navigate(R.id.action_login_to_enter_phone_number);
        });
    }

    private void observeLoginResult() {
        authViewModel.getLoginResult().observe(getViewLifecycleOwner(), result -> {
            if (result instanceof AuthResult.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnLogin.setEnabled(false);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
            }
            if (result instanceof AuthResult.Success) {
                String successMessage = ((AuthResult.Success) result).getMessage();
                Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(FragmentLogin.this)
                        .navigate(R.id.action_login_to_home);
                authViewModel.resetLoginResult();
            } else if (result instanceof AuthResult.Error) {
                String errorMessage = ((AuthResult.Error) result).getMessage();
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                authViewModel.resetLoginResult();
            }
        });
    }

    private boolean isInputValid(String username, String password) {
        if (username.isEmpty()) {
            binding.edtUsername.setError("Tên đăng nhập không được để trống");
            binding.edtUsername.requestFocus();
            return false;
        }
        if (password.isEmpty()) {
            binding.edtPassword.setError("Mật khẩu không được để trống");
            binding.edtPassword.requestFocus();
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
