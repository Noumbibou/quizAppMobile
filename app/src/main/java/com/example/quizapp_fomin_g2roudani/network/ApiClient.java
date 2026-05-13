package com.example.quizapp_fomin_g2roudani.network;

import android.util.Log;

import com.example.quizapp_fomin_g2roudani.auth.AuthTokenManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "http://192.168.11.101:8000/";
    //private static final String BASE_URL = "http://localhost:8000/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        // Récupération dynamique du token actuel
                        String token = AuthTokenManager.getToken();
                        Log.d("API_TOKEN", token != null ? "TOKEN ENVOYÉ" : "TOKEN NULL");

                        Request.Builder builder = chain.request().newBuilder();
                        if (token != null && !token.isEmpty()) {
                            builder.header("Authorization", "Bearer " + token);
                        }
                        return chain.proceed(builder.build());
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    //.baseUrl("http://localhost:8000/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public static void invalidate() {
        retrofit = null; // Force la recréation du client au prochain appel
    }
}
