package com.example.myspendyapp.ui.spending;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myspendyapp.R;
import com.example.myspendyapp.databinding.FragmentSpendingBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.example.myspendyapp.data.db.entity.Transaction;
import com.example.myspendyapp.ui.dashboard.CategoryViewModel;
import com.example.myspendyapp.data.db.entity.Category;
import android.widget.ArrayAdapter;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public class SpendingFragment extends Fragment {

    private FragmentSpendingBinding binding;
    private SpendingViewModel viewModel;
    private CategoryViewModel categoryViewModel;
    private TransactionAdapter transactionAdapter;
    private TransactionAdapter incomeAdapter;
    private TransactionAdapter expenseAdapter;
    private boolean isIncomeListVisible = false;
    private boolean isExpenseListVisible = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSpendingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SpendingViewModel.class);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        setupRecyclerView();
        setupIncomeExpenseRecyclerView();
        setupIncomeExpenseClickListeners();
        setupFab();
        observeViewModel();
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter();
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvTransactions.setAdapter(transactionAdapter);
    }

    private void setupIncomeExpenseRecyclerView() {
        incomeAdapter = new TransactionAdapter();
        binding.rvIncomeTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvIncomeTransactions.setAdapter(incomeAdapter);

        expenseAdapter = new TransactionAdapter();
        binding.rvExpenseTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvExpenseTransactions.setAdapter(expenseAdapter);
    }

    private void setupIncomeExpenseClickListeners() {
        // Click vào Income
        binding.layoutIncome.setOnClickListener(v -> {
            isIncomeListVisible = !isIncomeListVisible;
            if (isIncomeListVisible) {
                binding.layoutIncomeList.setVisibility(View.VISIBLE);
                binding.layoutExpenseList.setVisibility(View.GONE);
                isExpenseListVisible = false;
            } else {
                binding.layoutIncomeList.setVisibility(View.GONE);
            }
        });

        // Click vào Expense
        binding.layoutExpense.setOnClickListener(v -> {
            isExpenseListVisible = !isExpenseListVisible;
            if (isExpenseListVisible) {
                binding.layoutExpenseList.setVisibility(View.VISIBLE);
                binding.layoutIncomeList.setVisibility(View.GONE);
                isIncomeListVisible = false;
            } else {
                binding.layoutExpenseList.setVisibility(View.GONE);
            }
        });

        // Click "Xem tất cả" cho Income
        binding.tvViewAllIncome.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean("isIncome", true);
            NavHostFragment.findNavController(this).navigate(R.id.action_spending_to_all_transactions, bundle);
        });

        // Click "Xem tất cả" cho Expense
        binding.tvViewAllExpense.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean("isIncome", false);
            NavHostFragment.findNavController(this).navigate(R.id.action_spending_to_all_transactions, bundle);
        });
    }


    private void setupFab() {
        binding.fabAddTransaction.setOnClickListener(v -> {
            showAddTransactionBottomSheet();
        });
    }

    private void observeViewModel() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        viewModel.transactions.observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                // Hiển thị tất cả transactions
                transactionAdapter.submitList(transactions);

                // Filter income transactions (chỉ lấy 5 mục đầu tiên)
                List<com.example.myspendyapp.data.models.TransactionWithCategory> incomeList = new ArrayList<>();
                for (com.example.myspendyapp.data.models.TransactionWithCategory transaction : transactions) {
                    if (transaction.getTransaction().isIncome()) {
                        incomeList.add(transaction);
                    }
                }
                // Sắp xếp theo ngày mới nhất
                incomeList.sort((a, b) -> b.getTransaction().getDate().compareTo(a.getTransaction().getDate()));
                // Chỉ lấy 5 mục đầu tiên
                if (incomeList.size() > 5) {
                    incomeList = incomeList.subList(0, 5);
                }
                incomeAdapter.submitList(incomeList);

                // Filter expense transactions (chỉ lấy 5 mục đầu tiên)
                List<com.example.myspendyapp.data.models.TransactionWithCategory> expenseList = new ArrayList<>();
                for (com.example.myspendyapp.data.models.TransactionWithCategory transaction : transactions) {
                    if (!transaction.getTransaction().isIncome()) {
                        expenseList.add(transaction);
                    }
                }
                // Sắp xếp theo ngày mới nhất
                expenseList.sort((a, b) -> b.getTransaction().getDate().compareTo(a.getTransaction().getDate()));
                // Chỉ lấy 5 mục đầu tiên
                if (expenseList.size() > 5) {
                    expenseList = expenseList.subList(0, 5);
                }
                expenseAdapter.submitList(expenseList);
            } else {
                transactionAdapter.submitList(new ArrayList<>());
                incomeAdapter.submitList(new ArrayList<>());
                expenseAdapter.submitList(new ArrayList<>());
            }
        });

        viewModel.totalIncome.observe(getViewLifecycleOwner(), totalIncome -> {
            if (totalIncome != null) {
                binding.tvIncomeValue.setText(currencyFormat.format(totalIncome));
            } else {
                binding.tvIncomeValue.setText(currencyFormat.format(0.0));
            }
            updateBalance();
        });

        viewModel.totalExpense.observe(getViewLifecycleOwner(), totalExpense -> {
            if (totalExpense != null) {
                binding.tvExpenseValue.setText(currencyFormat.format(totalExpense));
            } else {
                binding.tvExpenseValue.setText(currencyFormat.format(0.0));
            }
            updateBalance();
        });
    }

    private void updateBalance() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        Double totalIncome = viewModel.totalIncome.getValue();
        Double totalExpense = viewModel.totalExpense.getValue();
        
        double income = totalIncome != null ? totalIncome : 0.0;
        double expense = totalExpense != null ? totalExpense : 0.0;
        double balance = income - expense;
        
        binding.tvBalanceValue.setText(currencyFormat.format(balance));
    }

    private void showAddTransactionBottomSheet() {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_add_transaction, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        bottomSheetDialog.setContentView(bottomSheetView);

        bottomSheetView.findViewById(R.id.tvNewIncome).setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_spending_to_create_income);
            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.tvNewExpense).setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_spending_to_create_expense);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
