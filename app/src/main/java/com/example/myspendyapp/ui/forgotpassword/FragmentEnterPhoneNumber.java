package com.example.myspendyapp.ui.forgotpassword;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.myspendyapp.R;
import com.example.myspendyapp.databinding.FragmentEnterPhoneNumberBinding;

public class FragmentEnterPhoneNumber extends Fragment {

    private FragmentEnterPhoneNumberBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Khởi tạo Binding
        binding = FragmentEnterPhoneNumberBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnConfirm.setOnClickListener(v -> {
            String phoneNumber = binding.edtPhoneNumber.getText().toString().trim();

            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(getContext(), "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo Bundle để truyền phone number sang màn hình OTP
            Bundle bundle = new Bundle();
            bundle.putString("phoneNumber", phoneNumber);

            // Chuyển hướng sang màn hình OTP
            Navigation.findNavController(view).navigate(R.id.action_enterPhone_to_enterOtp, bundle);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
