package com.example.mybarberapp.fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mybarberapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserFrag extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UserFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static UserFrag newInstance(String param1, String param2) {
        UserFrag fragment = new UserFrag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

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
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button myBookingsBtn = view.findViewById(R.id.myBookingsBtn);
        Button makeNewBookingBtn = view.findViewById(R.id.makeNewBookingBtn);
        FloatingActionButton logoutFab = view.findViewById(R.id.logoutFab);

        fetchUserNameAndUpdateUI();

        NavController navController = Navigation.findNavController(view);

        setupFlipAction(view.findViewById(R.id.service1Front), view.findViewById(R.id.service1Back));
        setupFlipAction(view.findViewById(R.id.service2Front), view.findViewById(R.id.service2Back));
        setupFlipAction(view.findViewById(R.id.service3Front), view.findViewById(R.id.service3Back));
        setupFlipAction(view.findViewById(R.id.service4Front), view.findViewById(R.id.service4Back));

        myBookingsBtn.setOnClickListener(v -> navController.navigate(R.id.action_userFrag_to_myBookingFrag));

        makeNewBookingBtn.setOnClickListener(v -> navController.navigate(R.id.action_userFrag_to_makeBookingFrag));

        logoutFab.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();

                        navController.navigate(R.id.action_userFrag_to_logInFragment);

                        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    private void fetchUserNameAndUpdateUI() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("name");
                        updateWelcomeText(userName);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UserFrag", "Failed to fetch user name: " + e.getMessage());
                });
    }

    private void updateWelcomeText(String userName) {
        TextView welcomeTextView = getView().findViewById(R.id.welcomeTextView);
        if (userName != null && !userName.isEmpty()) {
            welcomeTextView.setText("Welcome, " + userName);
        } else {
            welcomeTextView.setText("Welcome!");
        }
    }

    private void setupFlipAction(Button frontView, ImageView backView) {
        frontView.setOnClickListener(v -> {
            frontView.setVisibility(View.GONE);
            backView.setVisibility(View.VISIBLE);
        });

        backView.setOnClickListener(v -> {
            backView.setVisibility(View.GONE);
            frontView.setVisibility(View.VISIBLE);
        });
    }
}