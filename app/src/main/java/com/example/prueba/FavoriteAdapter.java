package com.example.prueba;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    private List<Recipe> favoriteRecipes;
    private Context context;

    public FavoriteAdapter(List<Recipe> favoriteRecipes, Context context) {
        this.favoriteRecipes = favoriteRecipes;
        this.context = context;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_recipe, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Recipe recipe = favoriteRecipes.get(position);
        holder.tvTitle.setText(recipe.getTitle());
        holder.btnRemoveFavorite.setOnClickListener(v -> {
            removeRecipeFromFavorites(recipe.getId(), position);
        });
    }

    @Override
    public int getItemCount() {
        return favoriteRecipes.size();
    }

    private void removeRecipeFromFavorites(String recipeId, int position) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            databaseReference.child(currentUser.getUid()).child("favoriteRecipes").child(recipeId).removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            favoriteRecipes.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, getItemCount());
                            Toast.makeText(context, "Recipe removed from favorites", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Error removing recipe from favorites", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView btnRemoveFavorite;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            btnRemoveFavorite = itemView.findViewById(R.id.btnRemoveFavorite);
        }
    }
}
