package com.example.prueba;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {
    private static final String PREF_NAME = "FavoriteRecipes";
    private static final String KEY_FAVORITE_PREFIX = "favorite_";

    private SharedPreferences sharedPreferences;

    public SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isRecipeFavorite(String recipeId) {
        return sharedPreferences.getBoolean(KEY_FAVORITE_PREFIX + recipeId, false);
    }

    public void setRecipeFavorite(String recipeId, boolean isFavorite) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_FAVORITE_PREFIX + recipeId, isFavorite);
        editor.apply();
    }
}
