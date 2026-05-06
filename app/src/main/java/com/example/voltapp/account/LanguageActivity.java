package com.example.voltapp.account;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.voltapp.R;

public class LanguageActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_language_account);
        getWindow().setStatusBarColor(Color.parseColor("#191B21"));
        prefs = getSharedPreferences("evcharge_prefs", MODE_PRIVATE);
        findViewById(R.id.btn_back_language).setOnClickListener(v -> finish());
        bind("Tiếng Việt", R.id.lang_vi);
        bind("Tiếng Anh", R.id.lang_en);
        bind("Tiếng Trung", R.id.lang_cn);
        bind("Tiếng Pháp", R.id.lang_fr);
        bind("Tiếng Hàn", R.id.lang_kr);
        bind("Tiếng Nhật", R.id.lang_jp);
        bind("Tiếng Tây Ban Nha", R.id.lang_es);
        bind("Tiếng Indonesia", R.id.lang_id);
        bind("Tiếng Nga", R.id.lang_ru);
        refreshChecks();
    }
    private void bind(String value, int id) {
        findViewById(id).setOnClickListener(v -> { prefs.edit().putString("language", value).apply(); Toast.makeText(this, "Đã chọn " + value, Toast.LENGTH_SHORT).show(); refreshChecks(); });
    }
    private void refreshChecks() {
        String selected = prefs.getString("language", "Tiếng Việt");
        int[] rows = {R.id.lang_vi,R.id.lang_en,R.id.lang_cn,R.id.lang_fr,R.id.lang_kr,R.id.lang_jp,R.id.lang_es,R.id.lang_id,R.id.lang_ru};
        for (int id: rows) {
            LinearLayout row = findViewById(id);
            TextView tv = (TextView) row.getChildAt(0);
            TextView check = (TextView) row.getChildAt(1);
            check.setText(tv.getText().toString().equals(selected) ? "✓" : "");
        }
    }
}
