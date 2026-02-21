package com.example.mybarberapp.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mybarberapp.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MakeBookingFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MakeBookingFrag extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MakeBookingFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MakeBookingFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static MakeBookingFrag newInstance(String param1, String param2) {
        MakeBookingFrag fragment = new MakeBookingFrag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private TextView selectedDateText, selectedTimeText;
    private Button selectDateBtn, selectTimeBtn, confirmBookingBtn;
    private Spinner stylistSpinner;

    private String selectedDate = "";
    private String selectedTime = "";
    private FirebaseFirestore firestore;

    private List<String> availableSlots = new ArrayList<>();
    private List<String> availableHours = new ArrayList<>();

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
        return inflater.inflate(R.layout.fragment_make_booking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();

        selectedDateText = view.findViewById(R.id.selectedDateText);
        selectedTimeText = view.findViewById(R.id.selectedTimeText);
        selectDateBtn = view.findViewById(R.id.selectDateBtn);
        selectTimeBtn = view.findViewById(R.id.selectTimeBtn);
        confirmBookingBtn = view.findViewById(R.id.confirmBookingBtn);
        stylistSpinner = view.findViewById(R.id.stylistSpinner);
        LinearLayout stylistsListLayout = view.findViewById(R.id.stylistsListLayout);

        fetchAndDisplayStylists(stylistsListLayout);

        selectDateBtn.setOnClickListener(v -> showDatePicker());
        selectTimeBtn.setOnClickListener(v -> showTimePicker());
        confirmBookingBtn.setOnClickListener(v -> handleBooking(view));

        populateStylistSpinner();

        stylistSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStylist = stylistSpinner.getSelectedItem().toString();
                if (!selectedStylist.equals("Select Stylist")) {
                    fetchStylistAvailability(selectedStylist);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void showDatePicker() {
        if (availableSlots == null || availableSlots.isEmpty()) {
            Toast.makeText(getContext(), "No available dates for the selected stylist", Toast.LENGTH_SHORT).show();
            return;
        }

        Set<String> availableDates = new HashSet<>(availableSlots);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);
                    String selectedDate = sdf.format(selectedCalendar.getTime());

                    if (availableDates.contains(selectedDate)) {
                        this.selectedDate = selectedDate;
                        selectedDateText.setText(selectedDate);
                    } else {
                        Toast.makeText(getContext(), "This date is not available", Toast.LENGTH_SHORT).show();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        datePickerDialog.getDatePicker().setMinDate(tomorrow.getTimeInMillis());

        datePickerDialog.show();
    }

    private void showTimePicker() {
        if (TextUtils.isEmpty(selectedDate)) {
            Toast.makeText(getContext(), "Please select a date first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (availableHours.isEmpty()) {
            Toast.makeText(getContext(), "No available hours for the selected stylist", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedStylist = stylistSpinner.getSelectedItem().toString();
        if (TextUtils.isEmpty(selectedStylist) || selectedStylist.equals("Select Stylist")) {
            Toast.makeText(getContext(), "Please select a stylist first", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("bookings")
                .whereEqualTo("date", selectedDate)
                .whereEqualTo("stylist", selectedStylist)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> occupiedTimes = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String time = document.getString("time");
                        if (time != null) {
                            occupiedTimes.add(time);
                        }
                    }

                    List<String> availableTimes = new ArrayList<>(availableHours);
                    availableTimes.removeAll(occupiedTimes);

                    if (availableTimes.isEmpty()) {
                        Toast.makeText(getContext(), "No available times for the selected date", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String[] timeArray = availableTimes.toArray(new String[0]);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Select Time");
                    builder.setItems(timeArray, (dialog, which) -> {
                        selectedTime = timeArray[which];
                        selectedTimeText.setText(selectedTime);
                    });
                    builder.show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to fetch bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void populateStylistSpinner() {
        firestore.collection("stylists").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> spinnerItems = new ArrayList<>();
                    spinnerItems .add("Select Stylist");

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("name");
                        spinnerItems.add(name);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, spinnerItems);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    stylistSpinner.setAdapter(adapter);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load stylists: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void handleBooking(View view) {
        String stylist = stylistSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(selectedDate) || TextUtils.isEmpty(selectedTime) || stylist.equals("Select Stylist")) {
            Toast.makeText(getContext(), "Please complete all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("bookings")
                .whereEqualTo("date", selectedDate)
                .whereEqualTo("time", selectedTime)
                .whereEqualTo("stylist", stylist)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            Toast.makeText(getContext(), "This slot is already booked. Please choose a different time or stylist.", Toast.LENGTH_SHORT).show();
                        } else {
                            saveBooking(view, stylist);
                        }
                    } else {
                        Toast.makeText(getContext(), "Error checking availability: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveBooking(View view, String stylist) {
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("date", selectedDate);
        bookingData.put("time", selectedTime);
        bookingData.put("stylist", stylist);
        bookingData.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());

        firestore.collection("bookings")
                .add(bookingData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Booking confirmed!", Toast.LENGTH_SHORT).show();
                    NavController navController = Navigation.findNavController(view);
                    navController.navigate(R.id.action_makeBookingFrag_to_userFrag);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to confirm booking: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void fetchStylistAvailability(String stylist) {
        firestore.collection("stylists")
                .whereEqualTo("name", stylist)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot stylistDoc = queryDocumentSnapshots.getDocuments().get(0);

                        List<String> weeklyAvailability = (List<String>) stylistDoc.get("weeklyAvailability");
                        availableHours = (List<String>) stylistDoc.get("hoursAvailable");
                        List<String> sickDays = (List<String>) stylistDoc.get("sickDays");
                        List<String> holidays = (List<String>) stylistDoc.get("holidays");

                        if (weeklyAvailability != null && availableHours != null) {
                            availableSlots.clear();
                            List<String> generatedDates = generateAvailableDates(weeklyAvailability);

                            Set<String> unavailableDates = new HashSet<>();
                            if (sickDays != null) unavailableDates.addAll(sickDays);
                            if (holidays != null) unavailableDates.addAll(holidays);

                            for (String date : generatedDates) {
                                if (!unavailableDates.contains(date)) {
                                    availableSlots.add(date);
                                }
                            }
                        } else {
                            Toast.makeText(getContext(), "No availability or hours found for this stylist", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "No stylist found with name " + stylist, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error fetching availability: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private List<String> generateAvailableDates(List<String> weeklyAvailability) {
        List<String> availableDates = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Map<String, Integer> dayOfWeekMap = new HashMap<>();
        dayOfWeekMap.put("Sunday", Calendar.SUNDAY);
        dayOfWeekMap.put("Monday", Calendar.MONDAY);
        dayOfWeekMap.put("Tuesday", Calendar.TUESDAY);
        dayOfWeekMap.put("Wednesday", Calendar.WEDNESDAY);
        dayOfWeekMap.put("Thursday", Calendar.THURSDAY);
        dayOfWeekMap.put("Friday", Calendar.FRIDAY);
        dayOfWeekMap.put("Saturday", Calendar.SATURDAY);

        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 30; i++) {
            String dayName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());

            if (weeklyAvailability.contains(dayName)) {
                availableDates.add(sdf.format(calendar.getTime()));
            }

            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        return availableDates;
    }

    private void fetchAndDisplayStylists(LinearLayout stylistsListLayout) {
        FirebaseFirestore.getInstance().collection("stylists")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot stylist : queryDocumentSnapshots) {
                        String name = stylist.getString("name");
                        String specialty = stylist.getString("specialty");

                        addStylistRow(stylistsListLayout, name, specialty);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch stylists: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addStylistRow(LinearLayout parent, String name, String specialty) {
        TextView textView = new TextView(getContext());
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        textView.setText(name + " ........ " + specialty);
        textView.setTextColor(getResources().getColor(R.color.light_blue));
        textView.setTextSize(16);
        textView.setPadding(8, 8, 8, 8);

        parent.addView(textView);
    }

}