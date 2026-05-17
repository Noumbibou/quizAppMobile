package com.example.quizapp_fomin_g2roudani.auth;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Singleton pour la gestion persistante du token Firebase et du statut Admin.
 * Gère la persistance locale via SharedPreferences pour éviter les pertes de session.
 */
public class AuthTokenManager {

    private static final String PREF_NAME = "AuthPrefs";
    private static final String KEY_TOKEN = "firebaseToken";
    private static final String KEY_IS_ADMIN = "isAdmin";

    private static String firebaseToken = null;
    private static boolean isAdmin = false;
    private static Context appContext = null; // ✅ Stockage du contexte pour accès global

    /**
     * Initialise le manager en chargeant les données depuis les SharedPreferences.
     * À appeler au démarrage de l'app (ex: MainActivity).
     */
    public static void init(Context context) {
        appContext = context.getApplicationContext(); // ✅ Sauvegarde du contexte global

        SharedPreferences prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        firebaseToken = prefs.getString(KEY_TOKEN, null);
        isAdmin = prefs.getBoolean(KEY_IS_ADMIN, false);
    }

    /**
     * Sauvegarde le jeton Firebase en mémoire et de façon persistante.
     */
    public static void saveToken(Context context, String token) {
        firebaseToken = token;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    /**
     * Sauvegarde le statut Admin en mémoire et de façon persistante.
     */
    public static void saveAdminStatus(Context context, boolean status) {
        isAdmin = status;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_IS_ADMIN, status).apply();
    }

    /**
     * ✅ Récupération robuste du token.
     * Si la variable static est effacée par Android, on recharge depuis les SharedPreferences.
     */
    public static String getToken() {
        if (firebaseToken != null && !firebaseToken.isEmpty()) {
            return firebaseToken;
        }

        // Si la mémoire est vide, on tente de recharger depuis le disque
        if (appContext != null) {
            SharedPreferences prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            firebaseToken = prefs.getString(KEY_TOKEN, null);
        }

        return firebaseToken;
    }

    public static boolean isAdmin() {
        return isAdmin;
    }

    public static boolean hasToken() {
        String token = getToken();
        return token != null && !token.isEmpty();
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
