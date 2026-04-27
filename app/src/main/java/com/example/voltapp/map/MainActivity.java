package com.example.voltapp.map;

import com.example.voltapp.R;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.List;

import io.github.jan.supabase.postgrest.Postgrest;
import io.github.jan.supabase.postgrest.PostgrestKt;
import io.github.jan.supabase.postgrest.result.PostgrestResult;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.EmptyCoroutineContext;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(16.0544, 108.2022), 15));
        
        mMap.setOnMarkerClickListener(marker -> {
            Station s = (Station) marker.getTag();
            if (s != null) {
                android.content.Intent intent = new android.content.Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("station_data", s);
                startActivity(intent);
            }
            return false; // Trả về false để Camera vẫn tự động di chuyển giữa màn hình
        });

        loadStationsFromSupabase();
    }

    private void loadStationsFromSupabase() {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(SupabaseConfig.SUPABASE_URL + "/rest/v1/tramsac?select=*");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey", SupabaseConfig.SUPABASE_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + SupabaseConfig.SUPABASE_KEY);
                conn.setRequestProperty("Accept", "application/json");

                java.io.InputStream in = conn.getInputStream();
                java.util.Scanner scanner = new java.util.Scanner(in).useDelimiter("\\A");
                String json = scanner.hasNext() ? scanner.next() : "";

                com.google.gson.Gson gson = new com.google.gson.Gson();
                final java.util.List<Station> stations = gson.fromJson(json, new com.google.gson.reflect.TypeToken<java.util.List<Station>>(){}.getType());

                runOnUiThread(() -> {
                    if (stations != null && !stations.isEmpty()) {
                        for (Station s : stations) {
                            LatLng position = new LatLng(s.latitude, s.longitude);
                            com.google.android.gms.maps.model.Marker m = mMap.addMarker(new MarkerOptions()
                                    .position(position)
                                    .title(s.name)
                                    .snippet("Trạm sạc xe điện"));
                            
                            if (m != null) {
                                m.setTag(s); // Gắn Station vào Marker để truy xuất sau khi click
                            }
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
