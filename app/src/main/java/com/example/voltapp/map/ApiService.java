package com.example.voltapp.map;

import com.example.voltapp.R;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("api/stations/")
    Call<List<Station>> getStations();
}
