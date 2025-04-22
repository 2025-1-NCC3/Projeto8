package com.saulop.ubersafestartfecap.api;

import com.saulop.ubersafestartfecap.model.ApiResponse;
import com.saulop.ubersafestartfecap.model.LoginRequest;
import com.saulop.ubersafestartfecap.model.LoginResponse;
import com.saulop.ubersafestartfecap.model.SafeScoreResponse;
import com.saulop.ubersafestartfecap.model.SafeScoreUpdate;
import com.saulop.ubersafestartfecap.model.ProfileResponse;
import com.saulop.ubersafestartfecap.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AuthService {
    @POST("/api/auth/signup")
    Call<ApiResponse> registerUser(@Body User user);

    @POST("/api/auth/login")
    Call<LoginResponse> loginUser(@Body LoginRequest request);

    @POST("/api/user/safescore/update")
    Call<SafeScoreResponse> updateSafeScore(@Header("Authorization") String token, @Body SafeScoreUpdate update);

    @GET("/api/user/safescore")
    Call<SafeScoreResponse> getSafeScore(@Header("Authorization") String token);

    @GET("/api/profile")
    Call<ProfileResponse> getProfile(
            @Header("Authorization") String bearerToken
    );
    @DELETE("/api/user")
    Call<ApiResponse> deleteUser(
            @Header("Authorization") String bearerToken
    );
}