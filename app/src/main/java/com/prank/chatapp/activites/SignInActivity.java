package com.prank.chatapp.activites;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prank.chatapp.databinding.ActivitySignInBinding;
import com.prank.chatapp.utilities.Constans;
import com.prank.chatapp.utilities.PreferenceManager;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constans.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListener();
    }

    private void setListener()
    {
        binding.textCreateNewAccount.setOnClickListener(view ->
                startActivity(new Intent(getApplicationContext(),SignUpActivity.class)));
        binding.buttonSignIn.setOnClickListener(view -> {
            if (isValidSignInDetails()){
                signIn();
            }
        });
    }

    private void signIn()
    {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constans.KEY_COLLECTION_USERS)
                .whereEqualTo(Constans.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(Constans.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size()>0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constans.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constans.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constans.KEY_NAME, documentSnapshot.getString(Constans.KEY_NAME));
                        preferenceManager.putString(Constans.KEY_IMAGE, documentSnapshot.getString(Constans.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else
                    {
                        loading(false);
                        showToast("Unable to sign in");
                    }
                });
    }

    private void loading(Boolean isLoading)
    {
        if (isLoading)
        {
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else
        {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
    private  Boolean isValidSignInDetails()
    {
        if (binding.inputEmail.getText().toString().trim().isEmpty())
        {
            showToast("Enter email");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches())
        {
            showToast("Enter valid email");
            return false;
        }
        else if(binding.inputPassword.getText().toString().trim().isEmpty())
        {
            showToast("Enter password");
            return false;
        }
        else
        {
            return true;
        }
    }


}