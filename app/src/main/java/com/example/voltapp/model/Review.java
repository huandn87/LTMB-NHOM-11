package com.example.voltapp.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Review implements Serializable {
    @SerializedName("review_id")
    public int reviewId;

    @SerializedName("station_name") // Link bằng tên trạm theo cách app hiện tại làm
    public String stationName;

    @SerializedName("username")
    public String username;

    @SerializedName("rating")
    public int rating;

    @SerializedName("comment")
    public String comment;

    @SerializedName("created_at")
    public String createdAt;

    public Review() {}

    public Review(String stationName, String username, int rating, String comment) {
        this.stationName = stationName;
        this.username = username;
        this.rating = rating;
        this.comment = comment;
    }
}
