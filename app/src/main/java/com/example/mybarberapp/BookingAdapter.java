package com.example.mybarberapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybarberapp.dataClasses.Booking;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    private List<Booking> bookings;
    private OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onCancelClick(String bookingId);
    }

    public BookingAdapter(List<Booking> bookings, OnBookingClickListener listener) {
        this.bookings = bookings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.booking_item, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.dateText.setText("Date: " + booking.getDate());
        holder.timeText.setText("Time: " + booking.getTime());
        holder.stylistText.setText("Stylist: " + booking.getStylist());

        holder.cancelButton.setOnClickListener(v -> listener.onCancelClick(booking.getId()));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, timeText, stylistText;
        Button cancelButton;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.bookingDateText);
            timeText = itemView.findViewById(R.id.bookingTimeText);
            stylistText = itemView.findViewById(R.id.bookingStylistText);
            cancelButton = itemView.findViewById(R.id.cancelBookingButton);
        }
    }
}
