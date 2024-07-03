package com.example.prueba.ui.inicio;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prueba.R;
import com.example.prueba.Recipe;
import com.example.prueba.RecipeAdapter;
import com.example.prueba.databinding.FragmentInicioBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class InicioFragment extends Fragment {

    private FragmentInicioBinding binding;
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private List<Recipe> recipeList;
    private List<Recipe> filteredRecipeList;
    private DatabaseReference databaseReference;
    private ChipGroup chipGroup;

    // IDs for the chips
    private int chipAllId;
    private int chipPeruanasId;
    private int chipInternacionalesId;
    private int chipBebidasId;

    // Declare Handler and Runnable
    private Handler handler;
    private Runnable runnable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout using View Binding
        binding = FragmentInicioBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize views
        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        chipGroup = binding.chipGroup;

        recipeList = new ArrayList<>();
        filteredRecipeList = new ArrayList<>();
        adapter = new RecipeAdapter(filteredRecipeList);
        recyclerView.setAdapter(adapter);

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("recipes");

        // Initialize Handler and Runnable for periodic task
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                // Reload recipes
                loadRecipes();
                // Schedule the next execution
                handler.postDelayed(this, 300000); // 300000 ms = 5 minutes
            }
        };

        loadRecipes();

        // Create and add chips programmatically
        createChips();

        return root;
    }

    private void createChips() {
        Chip chipAll = new Chip(getContext());
        chipAllId = View.generateViewId();
        chipAll.setId(chipAllId);
        chipAll.setText("All");
        chipAll.setCheckable(true);
        chipGroup.addView(chipAll);

        Chip chipPeruanas = new Chip(getContext());
        chipPeruanasId = View.generateViewId();
        chipPeruanas.setId(chipPeruanasId);
        chipPeruanas.setText("Peruanas");
        chipPeruanas.setCheckable(true);
        chipGroup.addView(chipPeruanas);

        Chip chipInternacionales = new Chip(getContext());
        chipInternacionalesId = View.generateViewId();
        chipInternacionales.setId(chipInternacionalesId);
        chipInternacionales.setText("Internacionales");
        chipInternacionales.setCheckable(true);
        chipGroup.addView(chipInternacionales);

        Chip chipBebidas = new Chip(getContext());
        chipBebidasId = View.generateViewId();
        chipBebidas.setId(chipBebidasId);
        chipBebidas.setText("Bebidas");
        chipBebidas.setCheckable(true);
        chipGroup.addView(chipBebidas);

        // Set listener for the ChipGroup
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> filterRecipes(checkedId));

        // Select the "All" chip by default
        chipGroup.check(chipAllId);
    }

    private void loadRecipes() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipeList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Recipe recipe = postSnapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        recipeList.add(recipe);
                    }
                }
                filterRecipes(chipGroup.getCheckedChipId());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Error loading recipes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterRecipes(int checkedId) {
        filteredRecipeList.clear();
        if (checkedId == chipAllId) {
            filteredRecipeList.addAll(recipeList);
        } else {
            String selectedCategory = "";
            if (checkedId == chipPeruanasId) {
                selectedCategory = "Peruanas";
            } else if (checkedId == chipInternacionalesId) {
                selectedCategory = "Internacionales";
            } else if (checkedId == chipBebidasId) {
                selectedCategory = "Bebidas";
            }

            for (Recipe recipe : recipeList) {
                if (selectedCategory.equals(recipe.getCategory())) {
                    filteredRecipeList.add(recipe);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(runnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Release binding reference
    }
}
