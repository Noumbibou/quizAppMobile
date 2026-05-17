package com.example.quizapp_fomin_g2roudani.network;

import android.util.Log;

import com.example.quizapp_fomin_g2roudani.auth.AuthTokenManager;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "http://192.168.11.101:8000/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        String token = AuthTokenManager.getToken();
                        
                        // ✅ PROTECTION : On bloque la requête si pas de token
                        if (token == null || token.isEmpty()) {
                            Log.e("API_ERROR", "❌ Requête bloquée : Token manquant");
                            throw new IOException("Authentification requise - Token manquant");
                        }

                        Log.d("TOKEN_DEBUG", "🚀 Envoi du Token : " + token);

                        Request.Builder builder = chain.request().newBuilder();
                        builder.header("Authorization", "Bearer " + token);
                        return chain.proceed(builder.build());
                    })
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public static void invalidate() {
        retrofit = null;
    }
}
