package com.example.voltapp.account.api;

import okhttp3.*;
import java.io.IOException;

public class SupabaseService {
    public static final String SUPABASE_URL = "https://xyntcrsfhacvqsuyvbkd.supabase.co";
    // Doi key nay neu ban tao key moi trong Supabase > Project Settings > API > Publishable key
    public static final String SUPABASE_KEY = "sb_publishable_Ga562F_Z8kOEFmvkpbPYAw_gYus54p7";
    private final OkHttpClient client = new OkHttpClient();

    public interface ApiCallback {
        void onSuccess(String json);
        void onError(String message);
    }

    public void get(String table, ApiCallback callback) {
        String connector = table.contains("?") ? "&" : "?";
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/" + table + connector + "select=*")
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { callback.onError(e.getMessage()); }
            @Override public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) callback.onSuccess(body); else callback.onError("HTTP " + response.code() + ": " + body);
            }
        });
    }

    public void post(String table, String json, ApiCallback callback) {
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/" + table)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { callback.onError(e.getMessage()); }
            @Override public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) callback.onSuccess(responseBody); else callback.onError("HTTP " + response.code() + ": " + responseBody);
            }
        });
    }

    public void patch(String table, String json, ApiCallback callback) {
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/" + table)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .patch(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { callback.onError(e.getMessage()); }
            @Override public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) callback.onSuccess(responseBody); else callback.onError("HTTP " + response.code() + ": " + responseBody);
            }
        });
    }

    public void deleteVehicle(int vehicleId, ApiCallback callback) {
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/phuongtien?vehicle_id=eq." + vehicleId)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Prefer", "return=minimal")
                .delete()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { callback.onError(e.getMessage()); }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) callback.onSuccess("ok"); else callback.onError("HTTP " + response.code());
            }
        });
    }
}
