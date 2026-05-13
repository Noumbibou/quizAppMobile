package com.example.quizapp_fomin_g2roudani.auth;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Singleton pour la gestion persistante du token Firebase et du statut Admin.
 */
public class AuthTokenManager {

    private static final String PREF_NAME = "AuthPrefs";
    private static final String KEY_TOKEN = "firebaseToken";
    private static final String KEY_IS_ADMIN = "isAdmin";

    private static String firebaseToken = null;
    private static boolean isAdmin = false;

    public static void init(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        firebaseToken = prefs.getString(KEY_TOKEN, null);
        isAdmin = prefs.getBoolean(KEY_IS_ADMIN, false);
    }

    public static void saveToken(Context context, String token) {
        firebaseToken = token;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public static void saveAdminStatus(Context context, boolean status) {
        isAdmin = status;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_IS_ADMIN, status).apply();
    }

    public static String getToken() {
        return firebaseToken;
    }

    public static boolean isAdmin() {
        return isAdmin;
    }

    public static boolean hasToken() {
        return firebaseToken != null && !firebaseToken.isEmpty();
    }

    /**
     * ✅ NETTOYAGE COMPLET DE LA SESSION
     * Supprime les données en mémoire et dans les SharedPreferences.
     */
    public static void clear(Context context) {
        firebaseToken = null;
        isAdmin = false;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
