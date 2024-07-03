package com.example.prueba;

import java.util.ArrayList;
import java.util.List;

public class RecipeManager {

    private static RecipeManager instance;
    private List<RecipeItem> recipes;

    private RecipeManager() {
        recipes = new ArrayList<>();
        // Aquí puedes agregar recetas de ejemplo o cargar desde alguna fuente
    }

    public static RecipeManager getInstance() {
        if (instance == null) {
            instance = new RecipeManager();
        }
        return instance;
    }

    public interface OnRecipesLoadedListener {
        void onRecipesLoaded(List<RecipeItem> recipeItems);
        void onRecipesLoadFailed(Exception e);
    }

    public void loadRecipes(OnRecipesLoadedListener listener) {
        // Aquí puedes implementar la lógica para cargar recetas
        // Por ejemplo, puedes cargar recetas desde una base de datos o API
        try {
            // Simulación de carga de recetas
            recipes.add(new RecipeItem("1", "Receta 1"));
            recipes.add(new RecipeItem("2", "Receta 2"));
            listener.onRecipesLoaded(recipes);
        } catch (Exception e) {
            listener.onRecipesLoadFailed(e);
        }
    }
}
