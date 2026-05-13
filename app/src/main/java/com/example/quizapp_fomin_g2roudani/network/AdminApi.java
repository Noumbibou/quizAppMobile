package com.example.quizapp_fomin_g2roudani.network;

import com.example.quizapp_fomin_g2roudani.models.AdminQuestion;
import com.example.quizapp_fomin_g2roudani.models.AdminUser;
import com.example.quizapp_fomin_g2roudani.models.QuestionCreateRequest;
import com.example.quizapp_fomin_g2roudani.models.QuestionUpdateRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AdminApi {

    @GET("admin/users")
    Call<List<AdminUser>> getAllUsers();

    @GET("admin/users/{uid}")
    Call<AdminUser> getUserDetails(@Path("uid") String uid);

    @PUT("admin/users/{uid}/make-admin")
    Call<AdminUser> makeAdmin(@Path("uid") String uid);

    @PUT("admin/users/{uid}/revoke-admin")
    Call<AdminUser> revokeAdmin(@Path("uid") String uid);

    @PUT("admin/users/{uid}/disable")
    Call<AdminUser> disableUser(@Path("uid") String uid);

    @PUT("admin/users/{uid}/enable")
    Call<AdminUser> enableUser(@Path("uid") String uid);

    // ✅ Question Management (C3)
    @GET("admin/questions")
    Call<List<AdminQuestion>> getAllQuestions();

    @POST("admin/questions")
    Call<AdminQuestion> createQuestion(@Body QuestionCreateRequest request);

    @PUT("admin/questions/{id}")
    Call<AdminQuestion> updateQuestion(@Path("id") int id, @Body QuestionUpdateRequest request);

    @DELETE("admin/questions/{id}")
    Call<Void> deactivateQuestion(@Path("id") int id);
}
