package com.example.prueba.ui.perfil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.prueba.LoginActivity;
import com.example.prueba.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class PerfilFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImage;
    private EditText profileName;
    private TextView profileEmail;
    private EditText profilePhone;
    private EditText profileLanguage;
    private Spinner accountTypeSpinner;
    private Button changePasswordButton;
    private Button logoutButton;

    private Uri imageUri;

    private DatabaseReference databaseReference;
    private FirebaseUser user;
    private StorageReference storageReference;

    public static PerfilFragment newInstance() {
        return new PerfilFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.profile_name);
        profileEmail = view.findViewById(R.id.profile_email);
        profilePhone = view.findViewById(R.id.profile_phone);
        profileLanguage = view.findViewById(R.id.profile_language);
        accountTypeSpinner = view.findViewById(R.id.account_type_spinner);
        changePasswordButton = view.findViewById(R.id.change_password_button);
        logoutButton = view.findViewById(R.id.logout_button);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.account_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accountTypeSpinner.setAdapter(adapter);
        accountTypeSpinner.setEnabled(false);  // Deshabilitar el Spinner
        accountTypeSpinner.setFocusable(false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        if (user != null) {
            String uid = user.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("profiles").child(uid);

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String email = user.getEmail(); // Obtener el correo electrónico de Firebase Authentication
                        String phone = snapshot.child("phone").getValue(String.class);
                        String language = snapshot.child("language").getValue(String.class);
                        String accountType = snapshot.child("accountType").getValue(String.class);
                        String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                        profileName.setText(name);
                        profileEmail.setText(email);
                        profilePhone.setText(phone);
                        profileLanguage.setText(language);
                        if (accountType != null) {
                            int spinnerPosition = adapter.getPosition(accountType);
                            accountTypeSpinner.setSelection(spinnerPosition);
                        } else {
                            accountTypeSpinner.setSelection(adapter.getPosition("Free"));  // Valor por defecto
                        }
                        if (imageUrl != null) {
                            Glide.with(getContext())
                                    .load(imageUrl)
                                    .into(profileImage);
                        }
                    } else {
                        databaseReference.child("accountType").setValue("Free");  // Asignar valor por defecto
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Manejar posibles errores.
                }
            });
        }

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        saveProfileData();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            uploadImageToFirebase();
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            String uid = user.getUid();
            StorageReference fileReference = storageReference.child(uid + ".jpg");

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();
                                    databaseReference.child("imageUrl").setValue(downloadUrl);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Fallo al subir la imagen", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void saveProfileData() {
        if (user != null) {
            String uid = user.getUid();
            String name = profileName.getText().toString();
            String email = user.getEmail(); // Obtener el correo electrónico de Firebase Authentication
            String phone = profilePhone.getText().toString();
            String language = profileLanguage.getText().toString();
            String accountType = "Free";  // Asegurar que el valor por defecto sea Free

            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(phone) && !TextUtils.isEmpty(language)) {
                DatabaseReference profileRef = FirebaseDatabase.getInstance().getReference("profiles").child(uid);

                profileRef.child("name").setValue(name);
                profileRef.child("email").setValue(email);
                profileRef.child("phone").setValue(phone);
                profileRef.child("language").setValue(language);
                profileRef.child("accountType").setValue(accountType);
            }
        }
    }

    private void changePassword() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (user != null) {
            String emailAddress = user.getEmail();

            auth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Se ha enviado un correo para cambiar la contraseña", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Error al enviar el correo de cambio de contraseña", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        // Navegar a la actividad de inicio de sesión después de cerrar sesión
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}
