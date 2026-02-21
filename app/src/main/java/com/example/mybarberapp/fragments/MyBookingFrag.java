package com.example.mybarberapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mybarberapp.BookingAdapter;
import com.example.mybarberapp.R;
import com.example.mybarberapp.dataClasses.Booking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyBookingFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyBookingFrag extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MyBookingFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyBookingFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static MyBookingFrag newInstance(String param1, String param2) {
        MyBookingFrag fragment = new MyBookingFrag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView bookingsRecyclerView;
    private TextView noBookingsText;
    private BookingAdapter adapter;
    private FirebaseFirestore firestore;
    private List<Booking> bookings;

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
        return inflater.inflate(R.layout.fragment_my_booking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bookingsRecyclerView = view.findViewById(R.id.bookingsRecyclerView);
        noBookingsText = view.findViewById(R.id.noBookingsText);
        firestore = FirebaseFirestore.getInstance();
        bookings = new ArrayList<>();

        adapter = new BookingAdapter(bookings, bookingId -> deleteBooking(bookingId));
        bookingsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        bookingsRecyclerView.setAdapter(adapter);

        loadBookings();
    }

    private void loadBookings() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore.collection("bookings")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookings.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Booking booking = new Booking(
                                document.getString("date"),
                                document.getString("time"),
                                document.getString("stylist"),
                                document.getId()
                        );
                        bookings.add(booking);
                    }

                    adapter.notifyDataSetChanged();
                    noBookingsText.setVisibility(bookings.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteBooking(String bookingId) {
        firestore.collection("bookings").document(bookingId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Booking canceled", Toast.LENGTH_SHORT).show();
                    loadBookings();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to cancel booking: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}