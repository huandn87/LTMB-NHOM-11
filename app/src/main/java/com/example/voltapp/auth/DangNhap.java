package com.example.voltapp.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.voltapp.R;
import com.example.voltapp.home.ManHinhChinh;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.*;

public class DangNhap extends AppCompatActivity {

    private static final String TAG = "VoltApp_Auth";
    private static final String SUPABASE_URL = "https://xyntcrsfhacvqsuyvbkd.supabase.co";
    private static final String SUPABASE_ANON_KEY = "sb_publishable_Ga562F_Z8kOEFmvkpbPYAw_gYus54p7";
    private static final String REST_URL = "https://xyntcrsfhacvqsuyvbkd.supabase.co/rest/v1/taikhoan";

    private GoogleSignInClient mGoogleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        xacThucGoogleVoiSupabase(account.getIdToken());
                    } catch (ApiException e) {
                        Log.e(TAG, "Lỗi Google Sign-In: " + e.getStatusCode());
                        Toast.makeText(this, "Không thể kết nối Google", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_nhap);

        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1047605591707-qn49aj36i3t54mponrav744coqf09177.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        TextInputEditText edtUsername = findViewById(R.id.EDT_USERNAME);
        TextInputEditText edtPass = findViewById(R.id.EDT_PASS);

        // Nút Đăng nhập (username + password → query Supabase)
        findViewById(R.id.BTN_DANG_NHAP_SDT).setOnClickListener(v -> {
            String username = edtUsername.getText() != null ? edtUsername.getText().toString().trim() : "";
            String password = edtPass.getText() != null ? edtPass.getText().toString().trim() : "";

            if (TextUtils.isEmpty(username)) {
                edtUsername.setError("Vui lòng nhập tên đăng nhập");
                edtUsername.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                edtPass.setError("Vui lòng nhập mật khẩu");
                edtPass.requestFocus();
                return;
            }
            dangNhapBangTaiKhoan(username, password);
        });

        // Nút Google
        findViewById(R.id.BTN_GOOGLE).setOnClickListener(v ->
                googleSignInLauncher.launch(mGoogleSignInClient.getSignInIntent()));

        // Quên mật khẩu
        findViewById(R.id.TV_QUEN_MAT_KHAU).setOnClickListener(v ->
                startActivity(new Intent(this, QuenMatKhau.class)));

        // Đăng ký → vào NhapSdt theo mode register
        findViewById(R.id.TV_DANG_KY).setOnClickListener(v -> {
            Intent intent = new Intent(this, NhapSdt.class);
            intent.putExtra("MODE", "register");
            startActivity(intent);
        });
    }

    /**
     * Đăng nhập bằng username + password_hash.
     * Query: SELECT * FROM taikhoan WHERE username=? AND password_hash=? AND status='active'
     */
    private void dangNhapBangTaiKhoan(String username, String password) {
        OkHttpClient client = new OkHttpClient();

        String url = REST_URL
                + "?username=eq." + username
                + "&password_hash=eq." + password
                + "&status=eq.active"
                + "&select=account_id,username,role";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(DangNhap.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";
                Log.d(TAG, "Login response: " + body);

                try {
                    JSONArray arr = new JSONArray(body);
                    if (arr.length() > 0) {
                        // Đăng nhập thành công
                        JSONObject user = arr.getJSONObject(0);
                        String role = user.optString("role", "customer");
                        int accountId = user.optInt("account_id", 0);

                        // Lưu session vào SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("evcharge_prefs", MODE_PRIVATE);
                        prefs.edit()
                                .putBoolean("is_logged_in", true)
                                .putString("username", username)
                                .putString("user_role", role)
                                .putInt("account_id", accountId)
                                .apply();

                        runOnUiThread(() -> {
                            Toast.makeText(DangNhap.this, "Chào mừng, " + username + "!", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(DangNhap.this, ManHinhChinh.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                        });
                    } else {
                        // Sai tài khoản hoặc mật khẩu
                        runOnUiThread(() -> Toast.makeText(DangNhap.this,
                                "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(DangNhap.this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    /**
     * Đăng nhập bằng Google (id_token → Supabase Auth)
     */
    private void xacThucGoogleVoiSupabase(String idToken) {
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("provider", "google");
            json.put("id_token", idToken);
        } catch (Exception e) { e.printStackTrace(); }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/token?grant_type=id_token")
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(DangNhap.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    getSharedPreferences("evcharge_prefs", MODE_PRIVATE)
                            .edit().putBoolean("is_logged_in", true).apply();
                    runOnUiThread(() -> {
                        Toast.makeText(DangNhap.this, "Đăng nhập Google thành công!", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(DangNhap.this, ManHinhChinh.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(DangNhap.this, "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}