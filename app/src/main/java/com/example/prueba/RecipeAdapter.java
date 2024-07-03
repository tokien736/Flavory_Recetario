package com.example.prueba;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prueba.ui.RecipeDetailFragment;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipeList;

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView tvTitle, tvDescription;

        public RecipeViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }

    public RecipeAdapter(List<Recipe> recipeList) {
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        Glide.with(holder.itemView.getContext()).load(recipe.getThumbnail()).into(holder.imageView);
        holder.tvTitle.setText(recipe.getTitle());
        holder.tvDescription.setText(recipe.getDescription());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentActivity activity = (FragmentActivity) v.getContext();
                String recipeId = recipe.getId();
                if (recipeId != null && !recipeId.isEmpty()) {
                    RecipeDetailFragment fragment = RecipeDetailFragment.newInstance(recipeId);

                    activity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.nav_host_fragment_activity_main, fragment)
                            .addToBackStack(null)
                            .commit();
                } else {
                    Toast.makeText(activity, "Error: Recipe ID is null or empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }
}
