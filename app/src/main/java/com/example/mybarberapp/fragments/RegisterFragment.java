package com.example.mybarberapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mybarberapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegisterFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterFragment newInstance(String param1, String param2) {
        RegisterFragment fragment = new RegisterFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText, phoneEditText, adminPasswordEditText;
    private CheckBox isAdminCheckBox;
    private Button registerButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private final String ADMIN_PASSWORD = "Admin123";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.register_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        nameEditText = view.findViewById(R.id.registerName);
        emailEditText = view.findViewById(R.id.registerEmailAddress);
        passwordEditText = view.findViewById(R.id.registerPassword);
        confirmPasswordEditText = view.findViewById(R.id.registerConfirmPassword);
        phoneEditText = view.findViewById(R.id.registerPhone);
        isAdminCheckBox = view.findViewById(R.id.isAdminCheckBox);
        adminPasswordEditText = view.findViewById(R.id.adminPassword);
        registerButton = view.findViewById(R.id.regBtn);

        isAdminCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                adminPasswordEditText.setVisibility(View.VISIBLE);
            } else {
                adminPasswordEditText.setVisibility(View.GONE);
            }
        });

        registerButton.setOnClickListener(v -> handleRegistration());
    }

    private void handleRegistration() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String adminPassword = adminPasswordEditText.getText().toString().trim();
        boolean isAdmin = isAdminCheckBox.isChecked();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(phone)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] role = {"user"};
        if (isAdmin) {
            if (adminPassword.equals(ADMIN_PASSWORD)) {
                role[0] = "admin";
            } else {
                Toast.makeText(getContext(), "Invalid admin password", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = firebaseAuth.getCurrentUser().getUid();

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", name);
                        userData.put("email", email);
                        userData.put("phone", phone);
                        userData.put("role", role[0]);

                        firestore.collection("users").document(userId)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Registration successful!", Toast.LENGTH_SHORT).show();

                                    NavController navController = Navigation.findNavController(requireView());
                                    navController.navigate(R.id.action_registerFragment_to_logInFragment);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(getContext(), "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}