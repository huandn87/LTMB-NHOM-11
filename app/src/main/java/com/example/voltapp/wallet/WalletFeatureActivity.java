package com.example.voltapp.wallet;

import com.example.voltapp.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.voltapp.wallet.data.WalletRepository;
import com.example.voltapp.databinding.ActivityWalletFeatureBinding;
import com.example.voltapp.wallet.ui.WalletViewModel;
import com.example.voltapp.wallet.ui.WalletViewModelFactory;
import com.example.voltapp.wallet.ui.fragment.SelectPaymentMethodFragment;
import com.example.voltapp.wallet.ui.fragment.TopUpAmountFragment;
import com.example.voltapp.wallet.ui.fragment.TopUpSuccessFragment;
import com.example.voltapp.wallet.ui.fragment.WalletHomeFragment;

public class WalletFeatureActivity extends AppCompatActivity {
    private ActivityWalletFeatureBinding binding;
    private WalletViewModel walletViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWalletFeatureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        walletViewModel = new ViewModelProvider(
                this,
                new WalletViewModelFactory(new WalletRepository())
        ).get(WalletViewModel.class);

        if (savedInstanceState == null) {
            openWalletHome(false);
        }

        setupBottomNav();
    }

    private void setupBottomNav() {
        // Ánh xạ View từ include
        View navView = findViewById(R.id.BOTTOM_NAV_CONTAINER);
        if (navView == null) return;

        // Highlight "Ví tiền"
        TextView tvWallet = navView.findViewById(R.id.NAV_WALLET_TEXT);
        ImageView ivWallet = navView.findViewById(R.id.NAV_WALLET_ICON);
        int neonColor = ContextCompat.getColor(this, R.color.XANH_NEON);
        
        if (tvWallet != null) tvWallet.setTextColor(neonColor);
        if (ivWallet != null) ivWallet.setColorFilter(neonColor);

        navView.findViewById(R.id.NAV_HOME).setOnClickListener(v -> startActivity(new Intent(this, com.example.voltapp.home.ManHinhChinh.class)));
        navView.findViewById(R.id.NAV_SAVED).setOnClickListener(v -> startActivity(new Intent(this, com.example.voltapp.home.TramSacDaLuu.class)));
        navView.findViewById(R.id.NAV_WALLET).setOnClickListener(v -> { /* Đang ở Wallet */ });
        navView.findViewById(R.id.NAV_ACCOUNT).setOnClickListener(v -> startActivity(new Intent(this, com.example.voltapp.account.TaiKhoanActivity.class)));
        navView.findViewById(R.id.NAV_SCAN).setOnClickListener(v -> Toast.makeText(this, "Mở trình quét mã QR", Toast.LENGTH_SHORT).show());
    }

    public WalletViewModel getWalletViewModel() {
        return walletViewModel;
    }

    public void openWalletHome() {
        openWalletHome(false);
    }

    public void openWalletHome(boolean clearBackStack) {
        if (clearBackStack) {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new WalletHomeFragment())
                .commit();
    }

    public void openTopUpAmount() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new TopUpAmountFragment())
                .addToBackStack(TopUpAmountFragment.class.getSimpleName())
                .commit();
    }

    public void openSelectMethod() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new SelectPaymentMethodFragment())
                .addToBackStack(SelectPaymentMethodFragment.class.getSimpleName())
                .commit();
    }

    public void openSuccess() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new TopUpSuccessFragment())
                .addToBackStack(TopUpSuccessFragment.class.getSimpleName())
                .commit();
    }
}
