package com.example.prueba.ui.favoritos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prueba.FavoriteAdapter;
import com.example.prueba.R;
import com.example.prueba.RecipeItem;
import com.example.prueba.RecipeManager;
import com.example.prueba.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FavoritosFragment extends Fragment {

    private static final String TAG = "FavoritosFragment";
    private RecyclerView recyclerView;
    private FavoriteAdapter adapter;
    private List<Recipe> favoriteRecipes;
    private List<RecipeItem> allRecipes;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private Spinner recipeSpinner;
    private Button btnAddToFavorites;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_favoritos, container, false);

        // Initialize views
        recipeSpinner = root.findViewById(R.id.recipeSpinner);
        btnAddToFavorites = root.findViewById(R.id.btnAddToFavorites);
        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize lists and adapter
        favoriteRecipes = new ArrayList<>();
        allRecipes = new ArrayList<>();
        adapter = new FavoriteAdapter(favoriteRecipes, requireContext());
        recyclerView.setAdapter(adapter);

        // Initialize Firebase references
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Load all recipes for the spinner
        loadAllRecipes();

        // Set up the button to add recipes to favorites
        btnAddToFavorites.setOnClickListener(v -> addRecipeToFavorites());

        // Load favorite recipes
        loadFavoriteRecipes();

        return root;
    }

    private void loadAllRecipes() {
        RecipeManager.getInstance().loadRecipes(new RecipeManager.OnRecipesLoadedListener() {
            @Override
            public void onRecipesLoaded(List<RecipeItem> recipeItems) {
                allRecipes.clear();
                allRecipes.addAll(recipeItems);
                Log.d(TAG, "Recipes loaded: " + allRecipes.size());
                if (getContext() != null) {
                    List<String> recipeNames = new ArrayList<>();
                    for (RecipeItem item : allRecipes) {
                        recipeNames.add(item.getName());
                    }
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, recipeNames);
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    recipeSpinner.setAdapter(spinnerAdapter);
                }
            }

            @Override
            public void onRecipesLoadFailed(Exception e) {
                Log.e(TAG, "Error loading recipes", e);
                Toast.makeText(getContext(), "Error loading recipes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addRecipeToFavorites() {
        int selectedPosition = recipeSpinner.getSelectedItemPosition();
        if (selectedPosition == Spinner.INVALID_POSITION) {
            Toast.makeText(getContext(), "No recipe selected", Toast.LENGTH_SHORT).show();
            return;
        }
        RecipeItem selectedRecipeItem = allRecipes.get(selectedPosition);

        if (currentUser != null) {
            DatabaseReference recipesReference = FirebaseDatabase.getInstance().getReference("recipes");
            recipesReference.child(selectedRecipeItem.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Recipe recipe = snapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        DatabaseReference userFavoritesRef = databaseReference.child(currentUser.getUid()).child("favoriteRecipes");
                        userFavoritesRef.child(recipe.getId()).setValue(recipe).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Recipe added to favorites", Toast.LENGTH_SHORT).show();
                                loadFavoriteRecipes(); // Reload favorite recipes
                            } else {
                                Toast.makeText(getContext(), "Error adding recipe to favorites", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Recipe not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error getting recipe to add to favorites", error.toException());
                }
            });
        }
    }

    private void loadFavoriteRecipes() {
        if (currentUser != null) {
            databaseReference.child(currentUser.getUid()).child("favoriteRecipes")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            favoriteRecipes.clear();
                            for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                                Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                                if (recipe != null) {
                                    favoriteRecipes.add(recipe);
                                }
                            }
                            if (favoriteRecipes.isEmpty()) {
                                Toast.makeText(getContext(), "No favorite recipes", Toast.LENGTH_SHORT).show();
                            }
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Error loading favorite recipes", error.toException());
                        }
                    });
        } else {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
}
