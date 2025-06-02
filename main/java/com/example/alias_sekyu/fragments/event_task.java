package com.example.alias_sekyu.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.alias_sekyu.BarCodeScan;
import com.example.alias_sekyu.DBHelper;
import com.example.alias_sekyu.EventCardAdapter;
import com.example.alias_sekyu.GlobalData;
import com.example.alias_sekyu.R;
import com.example.alias_sekyu.models.Event;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class event_task extends Fragment {

    private static final int REQUEST_CODE_SCAN = 1001;
    private EventCardAdapter eventCardAdapter;
    private DBHelper dbHelper;  // Declare at class level for reuse

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the DBHelper once.
        dbHelper = new DBHelper(requireContext());

        Button addTaskBtn = view.findViewById(R.id.createEvent);
        if (addTaskBtn != null) {
            addTaskBtn.setOnClickListener(v -> openEventBottomSheet());
        } else {
            Toast.makeText(getContext(), "Create Event button not found", Toast.LENGTH_SHORT).show();
        }

        Button preScanBtn = view.findViewById(R.id.scanBarCode);
        if (preScanBtn != null) {
            preScanBtn.setOnClickListener(v -> openPreScanBottomSheet());
        } else {
            Toast.makeText(getContext(), "Scan Barcode button not found", Toast.LENGTH_SHORT).show();
        }

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventCardAdapter = new EventCardAdapter(requireContext(), getEventList());
        recyclerView.setAdapter(eventCardAdapter);
    }

    private void openEventBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(requireContext()).inflate(R.layout.activity_task_assist, null);
        dialog.setContentView(sheetView);

        // Retrieve input fields
        TextInputEditText titleInput = sheetView.findViewById(R.id.eventTitleInput);
        TextInputEditText venueInput = sheetView.findViewById(R.id.eventVenueInput);
        TextInputEditText dateInput = sheetView.findViewById(R.id.dateInput);
        TextInputEditText timeInput = sheetView.findViewById(R.id.timeInput);

        // Set up date and time pickers
        if (dateInput != null) {
            dateInput.setOnClickListener(v -> showDatePicker(dateInput));
        }
        if (timeInput != null) {
            timeInput.setOnClickListener(v -> showTimePicker(timeInput));
        }

        // Use the cancel and proceed buttons (now declared as Button)
        Button cancelBtn = sheetView.findViewById(R.id.goBack);
        if (cancelBtn != null) {
            cancelBtn.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Event creation cancelled", Toast.LENGTH_SHORT).show();
                clearFields(titleInput, venueInput, dateInput, timeInput);
                dialog.dismiss();
            });
        }

        Button proceedBtn = sheetView.findViewById(R.id.nxtstep);
        if (proceedBtn != null) {
            proceedBtn.setOnClickListener(v -> {
                String title = safeText(titleInput);
                String venue = safeText(venueInput);
                String date = safeText(dateInput);
                String time = safeText(timeInput);

                if (title.isEmpty() || venue.isEmpty() || date.isEmpty() || time.isEmpty()) {
                    Toast.makeText(requireContext(), "Please complete all fields!", Toast.LENGTH_SHORT).show();
                    return;
                }

                long result = dbHelper.insertEvent(title, venue, date, time);

                if (result != -1) {
                    Toast.makeText(requireContext(), "Event created: " + title, Toast.LENGTH_SHORT).show();
                    clearFields(titleInput, venueInput, dateInput, timeInput);
                    View fragmentView = getView();
                    if (fragmentView != null) {
                        RecyclerView recyclerView = fragmentView.findViewById(R.id.recyclerViewEvents);
                        if (recyclerView.getAdapter() instanceof EventCardAdapter) {
                            ((EventCardAdapter) recyclerView.getAdapter()).updateEvents(getEventList());
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to create event", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            });
        }
        dialog.show();
    }

    private void openPreScanBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(requireContext()).inflate(R.layout.activity_prescan_sel, null);
        dialog.setContentView(sheetView);

        Spinner eventSpinner = sheetView.findViewById(R.id.idEvLoc);

        // Retrieve and store events once.
        List<Event> events = getEventList();
        List<String> eventTitles = new ArrayList<>();
        if (events == null || events.isEmpty()) {
            eventTitles.add("No events available");
        } else {
            for (Event e : events) {
                eventTitles.add(e.getTitle());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                eventTitles
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventSpinner.setAdapter(adapter);

        Button cancelBtn = sheetView.findViewById(R.id.decline);
        Button proceedBtn = sheetView.findViewById(R.id.proceed);

        if (cancelBtn != null) {
            cancelBtn.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Scan task cancelled.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        if (proceedBtn != null) {
            proceedBtn.setOnClickListener(v -> {
                try {
                    int pos = eventSpinner.getSelectedItemPosition();
                    if (pos != Spinner.INVALID_POSITION && pos < events.size()) {
                        Event selectedEvent = events.get(pos);
                        int eventID = selectedEvent.getId();  // Replace with your getter if different
                        String eventTitle = selectedEvent.getTitle();

                        android.util.Log.d("PreScanSel", "Passing eventID: " + eventID + " Title: " + eventTitle);

                        try {
                            GlobalData.EVENT_ID = eventID;
                            GlobalData.EVENT_TITLE = eventTitle;
                        } catch (Exception e) {
                            // Ignore if GlobalData is not defined.
                        }

                        Intent intent = new Intent(requireContext(), BarCodeScan.class);
                        intent.putExtra("EVENT_ID", eventID);
                        intent.putExtra("EVENT_TITLE", eventTitle);
                        startActivity(intent);
                    } else {
                        Toast.makeText(requireContext(), "Please select an event.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Error starting scan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        dialog.show();
    }


    private List<Event> getEventList() {
        List<Event> events = new ArrayList<>();
        Cursor cursor = dbHelper.getAllEvents();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range")
                int id = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_EVENT_ID));
                @SuppressLint("Range")
                String title = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_EVENT_TITLE));
                @SuppressLint("Range")
                String venue = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_EVENT_VENUE));
                @SuppressLint("Range")
                String date = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_EVENT_DATE));
                @SuppressLint("Range")
                String time = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_EVENT_TIME));
                events.add(new Event(id, title, venue, date, time));
            }
            cursor.close();
        }
        return events;
    }

    private void scanBarCode() {
        Intent intent = new Intent(requireContext(), com.example.alias_sekyu.BarCodeScan.class);
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }

    // --- Helper Methods (no redundancy) ---

    private void showDatePicker(TextInputEditText dateInput) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(
                requireContext(),
                (view, year, month, day) -> dateInput.setText(day + "/" + (month + 1) + "/" + year),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        picker.show();
    }

    private void showTimePicker(TextInputEditText timeInput) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog picker = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    int hour = (hourOfDay % 12 == 0) ? 12 : hourOfDay % 12;
                    String amPm = (hourOfDay < 12) ? "AM" : "PM";
                    timeInput.setText(String.format("%02d:%02d %s", hour, minute, amPm));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        picker.show();
    }

    private String safeText(TextInputEditText field) {
        return (field != null && field.getText() != null) ? field.getText().toString().trim() : "";
    }

    private void clearFields(TextInputEditText... fields) {
        for (TextInputEditText field : fields) {
            if (field != null) {
                field.setText("");
            }
        }
    }
}
