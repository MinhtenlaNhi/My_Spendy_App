package com.example.myspendyapp.ui.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myspendyapp.R;
import com.example.myspendyapp.databinding.FragmentNotificationsBinding;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private NotificationsViewModel notificationsViewModel;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

        setupReminderSwitch();
        setupTimePicker();
        setupBudget();
        observeData();

        return root;
    }

    private void setupReminderSwitch() {
        binding.switchReminder.setChecked(notificationsViewModel.isReminderEnabled());
        binding.layoutTimePicker.setVisibility(
            notificationsViewModel.isReminderEnabled() ? View.VISIBLE : View.GONE
        );

        binding.switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            notificationsViewModel.setReminderEnabled(isChecked);
            binding.layoutTimePicker.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            
            if (isChecked) {
                scheduleReminder();
            } else {
                cancelReminder();
            }
        });
    }

    private void setupTimePicker() {
        int hour = notificationsViewModel.getReminderHour();
        int minute = notificationsViewModel.getReminderMinute();
        updateTimeDisplay(hour, minute);

        binding.btnSetTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, selectedHour, selectedMinute) -> {
                    notificationsViewModel.setReminderTime(selectedHour, selectedMinute);
                    updateTimeDisplay(selectedHour, selectedMinute);
                    if (binding.switchReminder.isChecked()) {
                        scheduleReminder();
                    }
                },
                hour,
                minute,
                true
            );
            timePickerDialog.show();
        });
    }

    private void updateTimeDisplay(int hour, int minute) {
        String timeString = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        binding.tvReminderTime.setText(timeString);
    }

    private void setupBudget() {
        notificationsViewModel.getMonthlyBudget().observe(getViewLifecycleOwner(), budget -> {
            if (budget != null && budget > 0) {
                binding.edtBudget.setText(String.format(Locale.getDefault(), "%.0f", budget));
            }
        });

        binding.edtBudget.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String budgetText = binding.edtBudget.getText().toString().trim();
                if (!TextUtils.isEmpty(budgetText)) {
                    try {
                        double budget = Double.parseDouble(budgetText);
                        notificationsViewModel.setMonthlyBudget(budget);
                        Toast.makeText(requireContext(), "Đã lưu ngân sách", Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void observeData() {
        // Observe tổng chi tiêu
        notificationsViewModel.getTotalExpenseThisMonth().observe(getViewLifecycleOwner(), totalExpense -> {
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
            binding.tvTotalExpense.setText(formatter.format(totalExpense) + " đ");
        });

        // Observe cảnh báo
        notificationsViewModel.getWarningMessage().observe(getViewLifecycleOwner(), message -> {
            if (!TextUtils.isEmpty(message)) {
                binding.tvWarning.setText(message);
                binding.tvWarning.setVisibility(View.VISIBLE);
            } else {
                binding.tvWarning.setVisibility(View.GONE);
            }
        });

        // Observe trạng thái quá ngân sách
        notificationsViewModel.getIsOverBudget().observe(getViewLifecycleOwner(), isOver -> {
            if (isOver) {
                binding.tvWarning.setTextColor(requireContext().getColor(android.R.color.holo_red_dark));
            } else {
                binding.tvWarning.setTextColor(requireContext().getColor(android.R.color.holo_orange_dark));
            }
        });
    }

    private void scheduleReminder() {
        // Kiểm tra notification permission
        if (!checkNotificationPermission()) {
            requestNotificationPermission();
            return;
        }

        // Hủy alarm cũ nếu có
        cancelReminder();

        Intent intent = new Intent(requireContext(), ReminderReceiver.class);
        intent.setAction("com.example.myspendyapp.REMINDER_ACTION");
        pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, notificationsViewModel.getReminderHour());
        calendar.set(Calendar.MINUTE, notificationsViewModel.getReminderMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Nếu thời gian đã qua trong ngày, đặt cho ngày mai
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        long triggerTime = calendar.getTimeInMillis();
        Log.d("NotificationsFragment", "Scheduling reminder at: " + calendar.getTime().toString());

        try {
            // Từ Android 12+ sử dụng setExactAndAllowWhileIdle
            // ReminderReceiver sẽ tự động schedule lại alarm cho ngày tiếp theo
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        );
                    } else {
                        // Fallback to inexact alarm
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        );
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    );
                }
            } else {
                // Android < 6.0
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                );
            }

            Toast.makeText(requireContext(), 
                "Đã bật nhắc nhở hàng ngày lúc " + 
                String.format(Locale.getDefault(), "%02d:%02d", 
                    notificationsViewModel.getReminderHour(), 
                    notificationsViewModel.getReminderMinute()), 
                Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("NotificationsFragment", "Error scheduling alarm", e);
            Toast.makeText(requireContext(), "Lỗi khi đặt nhắc nhở: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private boolean checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return NotificationManagerCompat.from(requireContext()).areNotificationsEnabled();
        }
        return true; // Permission not needed for older versions
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), 
                    android.Manifest.permission.POST_NOTIFICATIONS)) {
                Toast.makeText(requireContext(), 
                    "Cần cấp quyền thông báo để nhận nhắc nhở", 
                    Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (binding.switchReminder.isChecked()) {
                    scheduleReminder();
                }
            } else {
                Toast.makeText(requireContext(), 
                    "Cần cấp quyền thông báo để sử dụng tính năng nhắc nhở", 
                    Toast.LENGTH_LONG).show();
            }
        }
    }

    private void cancelReminder() {
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent = null;
        }
        Toast.makeText(requireContext(), "Đã tắt nhắc nhở", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tính lại chi tiêu khi quay lại màn hình
        notificationsViewModel.calculateMonthlyExpense();
        
        // Kiểm tra và schedule lại reminder nếu đã bật
        if (notificationsViewModel.isReminderEnabled() && pendingIntent == null) {
            scheduleReminder();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
