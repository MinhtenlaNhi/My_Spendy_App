package com.example.myspendyapp;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myspendyapp.databinding.ActivityMainBinding;
import com.example.myspendyapp.data.preferences.SessionManager;
import com.example.myspendyapp.ui.AuthViewModel;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AuthViewModel authViewModel;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Khởi tạo SessionManager và AuthViewModel
        sessionManager = new SessionManager(this);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_spending,
                R.id.navigation_notifications, R.id.navigation_account)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(navView, navController);

        // Kiểm tra phiên đăng nhập khi khởi động (sau khi view đã sẵn sàng)
        binding.getRoot().post(() -> checkLoginSession(navController));

        // === LOGIC ẨN/HIỆN BOTTOM NAVIGATION ===
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destinationId = destination.getId();
            // Ẩn bottom nav cho các màn hình không có menu
            if (destinationId == R.id.navigation_login || 
                destinationId == R.id.navigation_register ||
                destinationId == R.id.changePasswordFragment ||
                destinationId == R.id.fragmentEnterPhoneNumber ||
                destinationId == R.id.fragmentEnterOtp ||
                destinationId == R.id.fragmentResetPassword ||
                destinationId == R.id.createIncomeFragment ||
                destinationId == R.id.createExpenseFragment ||
                destinationId == R.id.createCategoryFragment ||
                destinationId == R.id.allTransactionsFragment) {
                binding.navView.setVisibility(View.GONE);
                binding.fabContainer.setVisibility(View.GONE);
            } else {
                binding.navView.setVisibility(View.VISIBLE);
                binding.fabContainer.setVisibility(View.VISIBLE);
                binding.fabContainer.bringToFront();
            }
        });

        // === CLICK LISTENER CHO FAB HOME ===
        binding.fabHome.setOnClickListener(v -> {
            // Navigate về trang chủ
            navController.navigate(R.id.navigation_home);
        });
    }

    /**
     * Kiểm tra phiên đăng nhập và tự động điều hướng
     */
    private void checkLoginSession(NavController navController) {
        if (sessionManager.isLoggedIn()) {
            // User đã đăng nhập, kiểm tra lại trong ViewModel
            authViewModel.checkLoginSession();
            // Tự động điều hướng đến Home nếu đang ở màn hình Login
            if (navController.getCurrentDestination() != null &&
                    navController.getCurrentDestination().getId() == R.id.navigation_login) {
                navController.navigate(R.id.navigation_home);
            }
        }
    }
}
