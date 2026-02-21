package com.example.mybarberapp.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.mybarberapp.BookingAdapter;
import com.example.mybarberapp.R;
import com.example.mybarberapp.dataClasses.Booking;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminFrag extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AdminFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdminFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminFrag newInstance(String param1, String param2) {
        AdminFrag fragment = new AdminFrag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView bookingsRecyclerView;
    private Button addSickDayButton, addHolidayButton, cancelSickDayButton, cancelHolidayButton;
    private FirebaseFirestore firestore;
    private List<Booking> bookings;
    private BookingAdapter adapter;
    private Spinner stylistSpinner;
    private List<String> stylistIds = new ArrayList<>();
    private List<String> stylistNames = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bookingsRecyclerView = view.findViewById(R.id.bookingsRecyclerView);
        addSickDayButton = view.findViewById(R.id.addSickDayButton);
        addHolidayButton = view.findViewById(R.id.addHolidayButton);
        stylistSpinner = view.findViewById(R.id.stylistSpinner);
        cancelSickDayButton = view.findViewById(R.id.cancelSickDayButton);
        cancelHolidayButton = view.findViewById(R.id.cancelHolidayButton);
        FloatingActionButton logoutFab = view.findViewById(R.id.logoutFab);

        firestore = FirebaseFirestore.getInstance();
        bookings = new ArrayList<>();
        adapter = new BookingAdapter(bookings, bookingId -> deleteBooking(bookingId));

        fetchStylists();

        bookingsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        bookingsRecyclerView.setAdapter(adapter);

        loadBookings();

        addSickDayButton.setOnClickListener(v -> {
            String stylistId = getSelectedStylistId();
            if (stylistId != null) {
                addSickDay(stylistId);
            }
        });

        addHolidayButton.setOnClickListener(v -> {
            String stylistId = getSelectedStylistId();
            if (stylistId != null) {
                addHoliday(stylistId);
            }
        });

        cancelSickDayButton.setOnClickListener(v -> {
            String stylistId = getSelectedStylistId();
            if (stylistId != null) {
                showCancelDayDialog(stylistId, "sickDays");
            }
        });

        cancelHolidayButton.setOnClickListener(v -> {
            String stylistId = getSelectedStylistId();
            if (stylistId != null) {
                showCancelDayDialog(stylistId, "holidays");
            }
        });

        logoutFab.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();

                        NavController navController = Navigation.findNavController(view);
                        navController.navigate(R.id.action_adminFrag_to_logInFragment);

                        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    private void loadBookings() {
        firestore.collection("bookings")
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
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteBooking(String bookingId) {
        firestore.collection("bookings").document(bookingId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Booking deleted successfully", Toast.LENGTH_SHORT).show();
                    loadBookings();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete booking: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void addSickDay(String stylistId) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime());

                    firestore.collection("stylists")
                            .document(stylistId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    List<String> sickDays = (List<String>) documentSnapshot.get("sickDays");
                                    List<String> holidays = (List<String>) documentSnapshot.get("holidays");

                                    if (sickDays != null && sickDays.contains(formattedDate)) {
                                        Toast.makeText(getContext(), "This date is already a sick day", Toast.LENGTH_SHORT).show();
                                    } else if (holidays != null && holidays.contains(formattedDate)) {
                                        firestore.collection("stylists")
                                                .document(stylistId)
                                                .update("holidays", FieldValue.arrayRemove(formattedDate))
                                                .addOnSuccessListener(aVoid -> {
                                                    addDateToSickDays(stylistId, formattedDate, "Replaced holiday with sick day");
                                                })
                                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to replace holiday: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                    } else {
                                        addDateToSickDays(stylistId, formattedDate, "Sick day added for the stylist");
                                    }
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to fetch stylist details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void addDateToSickDays(String stylistId, String formattedDate, String successMessage) {
        firestore.collection("stylists")
                .document(stylistId)
                .update("sickDays", FieldValue.arrayUnion(formattedDate))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
                    cancelBookingsForDate(stylistId, formattedDate);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add sick day: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void addHoliday(String stylistId) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime());

                    firestore.collection("stylists")
                            .document(stylistId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    List<String> sickDays = (List<String>) documentSnapshot.get("sickDays");
                                    List<String> holidays = (List<String>) documentSnapshot.get("holidays");

                                    if (holidays != null && holidays.contains(formattedDate)) {
                                        Toast.makeText(getContext(), "This date is already a holiday", Toast.LENGTH_SHORT).show();
                                    } else if (sickDays != null && sickDays.contains(formattedDate)) {
                                        firestore.collection("stylists")
                                                .document(stylistId)
                                                .update("sickDays", FieldValue.arrayRemove(formattedDate))
                                                .addOnSuccessListener(aVoid -> {
                                                    addDateToHolidays(stylistId, formattedDate, "Replaced sick day with holiday");
                                                })
                                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to replace sick day: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                    } else {
                                        addDateToHolidays(stylistId, formattedDate, "Holiday added for the stylist");
                                    }
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to fetch stylist details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void addDateToHolidays(String stylistId, String formattedDate, String successMessage) {
        firestore.collection("stylists")
                .document(stylistId)
                .update("holidays", FieldValue.arrayUnion(formattedDate))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
                    cancelBookingsForDate(stylistId, formattedDate);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add holiday: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void cancelBookingsForDate(String stylistId, String date) {
        firestore.collection("stylists")
                .document(stylistId)
                .get()
                .addOnSuccessListener(stylistDoc -> {
                    if (stylistDoc.exists()) {
                        String stylistName = stylistDoc.getString("name");

                        firestore.collection("bookings")
                                .whereEqualTo("date", date)
                                .whereEqualTo("stylist", stylistName)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                                            firestore.collection("bookings")
                                                    .document(document.getId())
                                                    .delete();
                                        }
                                        Toast.makeText(getContext(), "Bookings canceled for this date", Toast.LENGTH_SHORT).show();
                                        loadBookings();
                                    }
                                });
                    }
                });
    }

    private void fetchStylists() {
        firestore.collection("stylists")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    stylistIds.clear();
                    stylistNames.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        stylistIds.add(document.getId());
                        stylistNames.add(document.getString("name"));
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, stylistNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    stylistSpinner.setAdapter(adapter);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to fetch stylists: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String getSelectedStylistId() {
        int selectedPosition = stylistSpinner.getSelectedItemPosition();
        if (selectedPosition >= 0 && selectedPosition < stylistIds.size()) {
            return stylistIds.get(selectedPosition);
        } else {
            Toast.makeText(getContext(), "Please select a stylist", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void showCancelDayDialog(String stylistId, String dayOffType) {
        firestore.collection("stylists")
                .document(stylistId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> daysOff = (List<String>) documentSnapshot.get(dayOffType);

                        if (daysOff == null || daysOff.isEmpty()) {
                            Toast.makeText(getContext(), "No " + dayOffType + " found to cancel.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        showDaysOffListDialog(stylistId, daysOff, dayOffType);
                    } else {
                        Toast.makeText(getContext(), "Stylist data not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch " + dayOffType + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showDaysOffListDialog(String stylistId, List<String> daysOff, String dayOffType) {
        String[] daysArray = daysOff.toArray(new String[0]);

        new AlertDialog.Builder(getContext())
                .setTitle("Cancel " + dayOffType)
                .setItems(daysArray, (dialog, which) -> {
                    String selectedDate = daysArray[which];
                    confirmCancellation(stylistId, selectedDate, dayOffType);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void confirmCancellation(String stylistId, String selectedDate, String dayOffType) {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirm Cancellation")
                .setMessage("Are you sure you want to cancel " + dayOffType + " on " + selectedDate + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    removeDayOffFromFirestore(stylistId, selectedDate, dayOffType);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void removeDayOffFromFirestore(String stylistId, String selectedDate, String dayOffType) {
        firestore.collection("stylists")
                .document(stylistId)
                .update(dayOffType, FieldValue.arrayRemove(selectedDate))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), dayOffType + " on " + selectedDate + " has been canceled.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to cancel " + dayOffType + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}