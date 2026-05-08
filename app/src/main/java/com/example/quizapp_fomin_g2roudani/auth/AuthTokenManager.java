package com.example.quizapp_fomin_g2roudani.auth;

/**
 * Singleton centralisé pour la gestion du token Firebase ID.
 * Stocke le token en mémoire pour qu'il soit accessible par l'intercepteur Retrofit.
 */
public class AuthTokenManager {

    private static String firebaseToken = null;

    public static void saveToken(String token) {
        firebaseToken = token;
    }

    public static String getToken() {
        return firebaseToken;
    }

    public static boolean hasToken() {
        return firebaseToken != null && !firebaseToken.isEmpty();
    }

    public static void clear() {
        firebaseToken = null;
    }
}
