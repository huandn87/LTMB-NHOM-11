package com.example.voltapp.map;

import com.example.voltapp.R;

import io.github.jan.supabase.SupabaseClient;
import io.github.jan.supabase.SupabaseClientBuilder;
import io.github.jan.supabase.postgrest.Postgrest;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class SupabaseConfig {
    public static final String SUPABASE_URL = "https://xyntcrsfhacvqsuyvbkd.supabase.co";
    public static final String SUPABASE_KEY = "sb_publishable_Ga562F_Z8kOEFmvkpbPYAw_gYus54p7";

    private static SupabaseClient client;

    public static SupabaseClient getClient() {
        if (client == null) {
            try {
                SupabaseClientBuilder builder = new SupabaseClientBuilder(SUPABASE_URL, SUPABASE_KEY);

                // TUYỆT CHIÊU DỨT ĐIỂM:
                // Thay vì ghi PostgrestConfig, mình dùng Object.
                // Java sẽ chấp nhận mọi thứ là Object nên sẽ KHÔNG BÁO ĐỎ nữa.
                builder.install(Postgrest.Companion, new Function1<Object, Unit>() {
                    @Override
                    public Unit invoke(Object config) {
                        // Chúng ta không cần làm gì với config này, chỉ cần trả về Unit
                        return Unit.INSTANCE;
                    }
                });

                client = builder.build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return client;
    }
}
