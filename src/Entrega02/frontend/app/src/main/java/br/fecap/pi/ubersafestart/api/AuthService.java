package br.fecap.pi.ubersafestart.api;

import br.fecap.pi.ubersafestart.model.ApiResponse;
import br.fecap.pi.ubersafestart.model.LoginRequest;
import br.fecap.pi.ubersafestart.model.LoginResponse;
import br.fecap.pi.ubersafestart.model.SafeScoreResponse;
import br.fecap.pi.ubersafestart.model.SafeScoreUpdate;
import br.fecap.pi.ubersafestart.model.ProfileResponse;
import br.fecap.pi.ubersafestart.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
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