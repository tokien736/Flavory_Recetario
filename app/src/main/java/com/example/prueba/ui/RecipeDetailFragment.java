package com.example.prueba.ui;

import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prueba.R;
import com.example.prueba.Recipe;
import com.example.prueba.Review;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RecipeDetailFragment extends Fragment {

    private static final String ARG_RECIPE_ID = "recipe_id";
    private static final String TAG = "RecipeDetailFragment";

    private ImageView recipeImage;
    private TextView recipeTitle, recipeDescription, recipeDetails;
    private RecyclerView galleryRecyclerView, reviewsRecyclerView;
    private EditText reviewText;
    private Button submitReviewButton;
    private DatabaseReference reviewsDatabaseReference;
    private DatabaseReference recipeDatabaseReference;
    private List<Review> reviewList;
    private ReviewAdapter reviewAdapter;

    private Recipe recipe;

    public RecipeDetailFragment() {
        // Required empty public constructor
    }

    public static RecipeDetailFragment newInstance(String recipeId) {
        RecipeDetailFragment fragment = new RecipeDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RECIPE_ID, recipeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String recipeId = getArguments().getString(ARG_RECIPE_ID);
            if (recipeId != null && !recipeId.isEmpty()) {
                Log.d(TAG, "Recipe ID: " + recipeId);
                recipeDatabaseReference = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId);
            } else {
                throw new IllegalArgumentException("Recipe ID cannot be null or empty");
            }
        } else {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recipe_detail, container, false);

        recipeImage = view.findViewById(R.id.recipe_image);
        recipeTitle = view.findViewById(R.id.recipe_title);
        recipeDescription = view.findViewById(R.id.recipe_description);
        recipeDetails = view.findViewById(R.id.recipe_details);
        galleryRecyclerView = view.findViewById(R.id.gallery_recycler_view);
        reviewsRecyclerView = view.findViewById(R.id.reviews_recycler_view);
        reviewText = view.findViewById(R.id.review_edit_text);
        submitReviewButton = view.findViewById(R.id.submit_review_button);

        galleryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reviewsRecyclerView.setAdapter(reviewAdapter);

        submitReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitReview();
            }
        });

        loadRecipeDetails();

        return view;
    }

    private void loadRecipeDetails() {
        recipeDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipe = snapshot.getValue(Recipe.class);
                if (recipe != null) {
                    Glide.with(getContext()).load(recipe.getThumbnail()).into(recipeImage);
                    recipeTitle.setText(recipe.getTitle());
                    recipeDescription.setText(recipe.getDescription());

                    Spanned htmlAsSpanned = Html.fromHtml(recipe.getDetails());
                    recipeDetails.setText(htmlAsSpanned);

                    GalleryAdapter galleryAdapter = new GalleryAdapter(recipe.getGallery());
                    galleryRecyclerView.setAdapter(galleryAdapter);

                    reviewsDatabaseReference = FirebaseDatabase.getInstance().getReference("reviews").child(recipe.getId());
                    loadReviews();
                } else {
                    Toast.makeText(getContext(), "Error: Recipe is null", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading recipe", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadReviews() {
        if (reviewsDatabaseReference != null) {
            reviewsDatabaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    reviewList.clear();
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Review review = postSnapshot.getValue(Review.class);
                        reviewList.add(review);
                    }
                    reviewAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error loading reviews", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Error: Database reference is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitReview() {
        String reviewTextValue = reviewText.getText().toString().trim();
        if (!reviewTextValue.isEmpty()) {
            String reviewId = reviewsDatabaseReference.push().getKey();
            Review review = new Review(reviewId, reviewTextValue);

            reviewsDatabaseReference.child(reviewId).setValue(review).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Review submitted", Toast.LENGTH_SHORT).show();
                    reviewText.setText("");
                } else {
                    Toast.makeText(getContext(), "Error submitting review", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(getContext(), "Please write a review", Toast.LENGTH_SHORT).show();
        }
    }
}
