package com.example.voltapp.wallet.model;

import com.example.voltapp.R;

import org.json.JSONObject;

public class Pttt {
    private final int methodId;
    private final String name;
    private final String description;
    private final String status;

    public Pttt(int methodId, String name, String description, String status) {
        this.methodId = methodId;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public static Pttt fromJson(JSONObject json) {
        return new Pttt(
                json.optInt("method_id"),
                json.optString("name", ""),
                json.optString("description", ""),
                json.optString("status", "active")
        );
    }

    public int getMethodId() {
        return methodId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Pttt{" +
                "methodId=" + methodId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
