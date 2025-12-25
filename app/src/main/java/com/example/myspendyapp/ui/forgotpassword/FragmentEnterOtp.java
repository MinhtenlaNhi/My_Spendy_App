package com.example.myspendyapp.ui.forgotpassword;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.myspendyapp.R;

public class FragmentEnterOtp extends Fragment {

    private EditText edtOtp;
    private Button btnVerifyOtp;
    private String phoneNumber;
    private static final String DEFAULT_OTP = "123456";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_enter_otp, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtOtp = view.findViewById(R.id.edtOtp);
        btnVerifyOtp = view.findViewById(R.id.btnVerifyOtp);

        if (getArguments() != null) {
            phoneNumber = getArguments().getString("phoneNumber");
        }

        // Tự động điền OTP mặc định
        edtOtp.setText(DEFAULT_OTP);

        btnVerifyOtp.setOnClickListener(v -> {
            String otp = edtOtp.getText().toString().trim();

            if (TextUtils.isEmpty(otp)) {
                Toast.makeText(getContext(), "Vui lòng nhập OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra OTP (mặc định là 123456)
            if (!otp.equals(DEFAULT_OTP)) {
                Toast.makeText(getContext(), "Mã OTP không đúng. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                return;
            }

            // OTP đúng, chuyển sang màn hình reset password
            Bundle bundle = new Bundle();
            bundle.putString("phoneNumber", phoneNumber);

            Navigation.findNavController(view)
                    .navigate(R.id.action_enterOtp_to_resetPassword, bundle);
        });
    }
}

