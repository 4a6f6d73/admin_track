package com.example.alias_sekyu.fragments;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.alias_sekyu.DBHelper;
import com.example.alias_sekyu.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class event_summary extends Fragment {

    private DBHelper dbHelper;
    private TableLayout tableLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            dbHelper = new DBHelper(requireContext());
            tableLayout = view.findViewById(R.id.Table01);

            if (tableLayout == null) {
                Toast.makeText(getContext(), "Table layout not found.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Initially load all records (unfiltered)
            loadAttendanceSummaryFiltered("");

            // Setup filter button for sort bottom sheet
            Button filterButton = view.findViewById(R.id.filterBtn);
            if (filterButton != null) {
                filterButton.setOnClickListener(v -> openSortBottomSheet());
            }

            // Setup Get Summary button (unchanged, for now)
            Button getSum = view.findViewById(R.id.getSummaryButton);
            if (getSum != null) {
                getSum.setOnClickListener(v -> openGetSumSheet());
            }

            // --- Add Search Functionality ---
            EditText searchField = view.findViewById(R.id.look4);
            Button searchBtn = view.findViewById(R.id.searchBtn);
            if (searchField != null && searchBtn != null) {
                searchBtn.setOnClickListener(v -> {
                    String query = searchField.getText().toString();
                    loadAttendanceSummaryFiltered(query);
                });
            }
            // ---------------------------------

        } catch (Exception e) {
            Log.e("event_summary", "onViewCreated crash: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Something went wrong while loading summary.", Toast.LENGTH_LONG).show();
        }
    }

    private void loadAttendanceSummaryFiltered(String filter) {
        try {
            tableLayout.removeAllViews();

            // Add header row.
            TableRow header = new TableRow(getContext());
            addCellToRow(header, "Event", true);
            addCellToRow(header, "Student №", true);
            addCellToRow(header, "Full Name", true);
            addCellToRow(header, "Prog/Yr/Sec", true);
            tableLayout.addView(header);

            Cursor cursor = dbHelper.getAttendanceSummary();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String eventTitle = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_EVENT_TITLE));
                    if (eventTitle == null || eventTitle.trim().isEmpty()) {
                        int eventId = cursor.getInt(cursor.getColumnIndexOrThrow("event_id"));
                        eventTitle = "Event " + eventId;
                    }
                    String studentNum = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_STUDENT_NUM));
                    String studentName = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_STUDENT_FNAME));
                    String progYrSec = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_PROG_YR_SEC));

                    // Check filter criteria: if filter is empty or any cell contains the filter (ignoring case).
                    if (filter == null || filter.trim().isEmpty() ||
                            (eventTitle != null && eventTitle.toLowerCase().contains(filter.toLowerCase())) ||
                            (studentNum != null && studentNum.toLowerCase().contains(filter.toLowerCase())) ||
                            (studentName != null && studentName.toLowerCase().contains(filter.toLowerCase())) ||
                            (progYrSec != null && progYrSec.toLowerCase().contains(filter.toLowerCase()))
                    ) {
                        TableRow row = new TableRow(getContext());
                        addCellToRow(row, eventTitle, false);
                        addCellToRow(row, studentNum, false);
                        addCellToRow(row, studentName, false);
                        addCellToRow(row, progYrSec, false);
                        tableLayout.addView(row);
                    }
                } while (cursor.moveToNext());
                cursor.close();
            } else {
                Toast.makeText(getContext(), "No attendance records found.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("event_summary", "loadAttendanceSummaryFiltered crash: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Failed to load attendance summary.", Toast.LENGTH_LONG).show();
        }
    }

    private void addCellToRow(TableRow row, String text, boolean isHeader) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setPadding(16, 16, 16, 16);
        tv.setGravity(Gravity.CENTER);
        if (isHeader) {
            tv.setTypeface(Typeface.DEFAULT_BOLD);
        }
        row.addView(tv);
    }

    private void openGetSumSheet() {
        // Create and inflate the bottom sheet dialog.
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = LayoutInflater.from(getContext()).inflate(R.layout.activity_getsum, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Retrieve views from the bottom sheet layout.
        RadioButton rbOnlyEvent = bottomSheetView.findViewById(R.id.onlyEvent);
        RadioButton rbGenSum = bottomSheetView.findViewById(R.id.genSum);
        Spinner eventListSpinner = bottomSheetView.findViewById(R.id.eventList);
        Button declineBtn = bottomSheetView.findViewById(R.id.decline);
        Button proceedBtn = bottomSheetView.findViewById(R.id.proceed);

        // Set default selection: "Event‑Only" is checked so that the spinner is enabled.
        if (rbOnlyEvent != null) {
            rbOnlyEvent.setChecked(true);
        }
        if (eventListSpinner != null) {
            eventListSpinner.setEnabled(true);
            // Populate the spinner with event names (using your helper getEventNames())
            List<String> eventNames = getEventNames();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, eventNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            eventListSpinner.setAdapter(adapter);
        }

        // Enforce mutual exclusivity between the radio buttons.
        if (rbOnlyEvent != null && rbGenSum != null) {
            rbOnlyEvent.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    rbGenSum.setChecked(false);
                    if (eventListSpinner != null) {
                        eventListSpinner.setEnabled(true);
                    }
                }
            });
            rbGenSum.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    rbOnlyEvent.setChecked(false);
                    if (eventListSpinner != null) {
                        eventListSpinner.setEnabled(false);
                    }
                }
            });
        }

        // Setup the decline button (dismiss the bottom sheet).
        if (declineBtn != null) {
            declineBtn.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Get Summary cancelled.", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            });
        }

        // Setup the proceed button.
        if (proceedBtn != null) {
            proceedBtn.setOnClickListener(v -> {
                if (rbGenSum != null && rbGenSum.isChecked()) {
                    // When "General Summary" is selected, export general summary Excel.
                    exportGeneralSummaryToExcel();
                } else if (rbOnlyEvent != null && rbOnlyEvent.isChecked()) {
                    // "Event‑Only" selected: require an event to export.
                    if (eventListSpinner != null && eventListSpinner.getSelectedItem() != null) {
                        String selectedEvent = eventListSpinner.getSelectedItem().toString();
                        exportAttendanceToExcel(selectedEvent);
                    } else {
                        Toast.makeText(requireContext(), "Please select an event.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                bottomSheetDialog.dismiss();
            });
        }

        // Finally, show the bottom sheet.
        bottomSheetDialog.show();
    }

    private void openSortBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = LayoutInflater.from(getContext()).inflate(R.layout.activity_sort_assist, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Cancel button logic.
        ImageButton cancelBtn = bottomSheetView.findViewById(R.id.decline);
        if (cancelBtn != null) {
            cancelBtn.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Sort operation cancelled", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            });
        }

        // Proceed button logic.
        ImageButton proceedBtn = bottomSheetView.findViewById(R.id.proceed);
        if (proceedBtn != null) {
            proceedBtn.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Data sorted successfully", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            });
        }

        // Spinner setup
        Spinner eventSpinner = bottomSheetView.findViewById(R.id.event_title);
        Spinner courseSpinner = bottomSheetView.findViewById(R.id.studProg);
        Spinner yearSectionSpinner = bottomSheetView.findViewById(R.id.yrSec);

        // Retrieve event names from the database.
        List<String> eventNames = getEventNames();

        ArrayAdapter<String> eventAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, eventNames);
        eventAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (eventSpinner != null) {
            eventSpinner.setAdapter(eventAdapter);
        }

        // Programs / Courses
        String[] programs = {"CCS-BSIT", "CCS-BSCS", "CCS-BSEMC", "CCS-ACT"};
        Map<String, List<String>> courseMap = new HashMap<>();
        courseMap.put("CCS-BSIT", Arrays.asList("1A", "1B", "1C", "1D", "2A", "2B", "2C", "3A", "3B", "3C", "4A", "4B"));
        courseMap.put("CCS-BSCS", Arrays.asList("1A", "1B", "1C", "2A", "2B", "3A", "4A"));
        courseMap.put("CCS-BSEMC", Arrays.asList("1A", "1B", "2A", "2B", "3A", "4A"));
        courseMap.put("CCS-ACT", Arrays.asList("1A", "1B", "2A", "2B"));

        ArrayAdapter<String> programAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, programs);
        programAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (courseSpinner != null) {
            courseSpinner.setAdapter(programAdapter);
        }

        // Year-section dynamic adapter.
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>());
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (yearSectionSpinner != null) {
            yearSectionSpinner.setAdapter(yearAdapter);
        }

        if (courseSpinner != null) {
            courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedProgram = parent.getItemAtPosition(position).toString();
                    List<String> yearSections = courseMap.get(selectedProgram);
                    if (yearSections != null && yearAdapter != null) {
                        yearAdapter.clear();
                        yearAdapter.addAll(yearSections);
                        yearAdapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
        }

        bottomSheetDialog.show();
    }

    private List<String> getEventNames() {
        List<String> eventNames = new ArrayList<>();
        Cursor cursor = dbHelper.getAllEvents();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_EVENT_TITLE));
                if (title == null || title.trim().isEmpty()) {
                    int eventId = cursor.getInt(cursor.getColumnIndexOrThrow("event_id"));
                    title = "Event " + eventId;
                }
                eventNames.add(title);
            }
            cursor.close();
        }
        if (eventNames.isEmpty()) {
            eventNames.add("No events available");
        }
        return eventNames;
    }

    public void exportAttendanceToExcel(final String eventTitle) {
        // Create and show a loading popup.
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Run export operation in a background thread.
        new Thread(() -> {
            Cursor cursor = dbHelper.getAttendanceRecordsByEvent(eventTitle);
            if (cursor == null || cursor.getCount() == 0) {
                if (cursor != null) {
                    cursor.close();
                }
                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "No records to export", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            Workbook workbook = new XSSFWorkbook();
            try {
                Sheet sheet = workbook.createSheet("Attendance");
                // Create header row.
                Row headerRow = sheet.createRow(0);
                String[] headers = {"Event", "Student Number", "Full Name", "Prog/Yr/Sec"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    CellStyle headerStyle = workbook.createCellStyle();
                    Font font = workbook.createFont();
                    font.setBold(true);
                    headerStyle.setFont(font);
                    cell.setCellStyle(headerStyle);
                }
                int rowNum = 1;
                while (cursor.moveToNext()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(
                            cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_EVENT_TITLE))
                    );
                    row.createCell(1).setCellValue(
                            cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_STUDENT_NUM))
                    );
                    row.createCell(2).setCellValue(
                            cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_STUDENT_FNAME))
                    );
                    row.createCell(3).setCellValue(
                            cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_PROG_YR_SEC))
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Error while building Excel sheet: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
                cursor.close();
                return;
            } finally {
                cursor.close();
            }

            // Set fixed column widths instead of using autoSizeColumn().
            Sheet sheet = workbook.getSheetAt(0);
            String[] headers = {"Event", "Student Number", "Full Name", "Prog/Yr/Sec"};
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 5000);
            }

            try {
                // Save the file to the public Documents directory.
                File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                if (!documentsDir.exists()) {
                    documentsDir.mkdirs();
                }
                String fileName = "attendance_" + eventTitle.replaceAll("\\s+", "_") + ".xlsx";
                File file = new File(documentsDir, fileName);
                FileOutputStream fos = new FileOutputStream(file);
                workbook.write(fos);
                fos.close();
                workbook.close();
                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Exported Excel: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Failed to export Excel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }


    public void exportGeneralSummaryToExcel() {
        // Create and show a loading popup.
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            // 1. Retrieve the full list of event titles from the Events table.
            List<String> eventTitles = dbHelper.getAllEventNames();
            if (eventTitles == null) {
                eventTitles = new ArrayList<>();
            }

            // 2. Open a cursor for all students.
            Cursor studentCursor = dbHelper.getAllStudents();
            if (studentCursor == null || studentCursor.getCount() == 0) {
                if (studentCursor != null) {
                    studentCursor.close();
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(getContext(), "No student records to export", Toast.LENGTH_SHORT).show();
                    });
                }
                return;
            }

            // 3. Create a new workbook and sheet.
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("General Summary");

            // 4. Create the header row.
            // The header columns will be:
            // [Student Number, Full Name, Prog/Yr/Sec, <one column per event>, Fine]
            int columnCount = 3 + eventTitles.size() + 1;  // 3 student fields, one per event, then the Fine
            Row headerRow = sheet.createRow(0);
            int colIndex = 0;
            headerRow.createCell(colIndex++).setCellValue("Student Number");
            headerRow.createCell(colIndex++).setCellValue("Full Name");
            headerRow.createCell(colIndex++).setCellValue("Prog/Yr/Sec");
            for (String event : eventTitles) {
                headerRow.createCell(colIndex++).setCellValue(event);
            }
            headerRow.createCell(colIndex).setCellValue("Fine");

            // Optionally, style the header row (bold font)
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            for (int i = 0; i < columnCount; i++) {
                headerRow.getCell(i).setCellStyle(headerStyle);
            }

            // 5. Create a style for attended events (green background).
            CellStyle presentStyle = workbook.createCellStyle();
            presentStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            presentStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // 6. Process each student.
            int rowNum = 1;
            DecimalFormat df = new DecimalFormat("0.00");
            while (studentCursor.moveToNext()) {
                Row row = sheet.createRow(rowNum++);
                colIndex = 0;

                // Extract student details.
                String studNum = studentCursor.getString(studentCursor.getColumnIndexOrThrow(DBHelper.COLUMN_STUDENT_NUM));
                String fullName = studentCursor.getString(studentCursor.getColumnIndexOrThrow(DBHelper.COLUMN_STUDENT_FNAME));
                String progYrSec = studentCursor.getString(studentCursor.getColumnIndexOrThrow(DBHelper.COLUMN_PROG_YR_SEC));

                row.createCell(colIndex++).setCellValue(studNum);
                row.createCell(colIndex++).setCellValue(fullName);
                row.createCell(colIndex++).setCellValue(progYrSec);

                // For each event, check if the student attended.
                int attendedCount = 0;
                for (String eventTitle : eventTitles) {
                    Cell cell = row.createCell(colIndex++);
                    boolean attended = checkAttendanceRecord(studNum, eventTitle);
                    if (attended) {
                        cell.setCellValue("Present");
                        cell.setCellStyle(presentStyle);
                        attendedCount++;
                    } else {
                        cell.setCellValue("X"); // Mark as absent using "X"
                    }
                }
                // Compute fine: Fine = (# of absent events) * 50.00
                int absentCount = eventTitles.size() - attendedCount;
                double fine = absentCount * 50.00;
                row.createCell(colIndex).setCellValue("₱ " + df.format(fine));
            }
            studentCursor.close();

            // 7. Set fixed column widths.
            for (int i = 0; i < columnCount; i++) {
                sheet.setColumnWidth(i, 5000);
            }

            // 8. Save the workbook to the public Documents folder.
            try {
                File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                if (!documentsDir.exists()) {
                    documentsDir.mkdirs(); // Ensure the folder exists.
                }
                String fileName = "GeneralSummary.xlsx";
                File file = new File(documentsDir, fileName);
                FileOutputStream fos = new FileOutputStream(file);
                workbook.write(fos);
                fos.close();
                workbook.close();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(getContext(), "Exported Excel: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(getContext(), "Failed to export Excel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).start();
    }



    /**
     * Helper method to check if a student (identified by studentNum) attended an event (by eventTitle).
     */
    private boolean checkAttendanceRecord(String studentNum, String eventTitle) {
        // Query the attendance table for a matching record.
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT 1 FROM " + DBHelper.TABLE_ATTENDANCE +
                        " WHERE " + DBHelper.COLUMN_STUDENT_NUM + " = ? AND " + DBHelper.COLUMN_EVENT_TITLE + " = ?",
                new String[]{ studentNum, eventTitle }
        );
        boolean exists = (cursor != null && cursor.moveToFirst());
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }
}