package com.saulop.ubersafestartfecap.api;

import com.saulop.ubersafestartfecap.model.ApiResponse;
import com.saulop.ubersafestartfecap.model.LoginRequest;
import com.saulop.ubersafestartfecap.model.LoginResponse;
import com.saulop.ubersafestartfecap.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {
    @POST("/api/auth/signup")
    Call<ApiResponse> registerUser(@Body User user);

    @POST("/api/auth/login")
    Call<LoginResponse> loginUser(@Body LoginRequest request);
}
