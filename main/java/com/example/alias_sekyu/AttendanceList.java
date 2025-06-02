package com.example.alias_sekyu;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.app.AlertDialog;
import android.view.Gravity;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alias_sekyu.databinding.ActivityAttendanceListBinding;

public class AttendanceList extends AppCompatActivity {

    private ActivityAttendanceListBinding binding;
    private String initialScan = null;
    private static final int REQUEST_CODE_SCAN = 2001;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout using view binding.
        binding = ActivityAttendanceListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Instantiate "shortcut" DBHelper and load all student records.
        dbHelper = new DBHelper(this);
        loadAttendanceRecordsFromDB();

        // Optionally, handle an initial scan result if provided by the launching intent.
        initialScan = getIntent().getStringExtra("SCAN_RESULT");
        if (initialScan != null && !initialScan.isEmpty()) {
            showConfirmationDialog(initialScan);
        }

        // Set up the Exit button to close the application.
        binding.exitBtn.setOnClickListener(v -> {
            Intent homeIntent = new Intent(AttendanceList.this, HomePage.class);
            // Clear the back stack so that no previous activities are visible when the HomeActivity is started.
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish(); // Optionally finish the current activity.
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK && data != null) {
            String scanResult = data.getStringExtra("SCAN_RESULT");
            if (scanResult != null && !scanResult.isEmpty()) {
                showConfirmationDialog(scanResult);
            }
        }
    }

    /**
     * Loads all student records from the database using shortcut.getAllStudents()
     * and populates the attendance table.
     */
    private void loadAttendanceRecordsFromDB() {
        // Using the new schema – fetching all student records from the "students" table.
        Cursor cursor = dbHelper.getAllStudents();
        if (cursor != null) {
            try {
                // Pre-fetch column indices from the new schema.
                int indexStudentNum   = cursor.getColumnIndex(DBHelper.COLUMN_STUDENT_NUM);
                int indexStudentFname = cursor.getColumnIndex(DBHelper.COLUMN_STUDENT_FNAME);
                int indexProgYrSec    = cursor.getColumnIndex(DBHelper.COLUMN_PROG_YR_SEC);

                // Check that required columns were found.
                if (indexStudentNum == -1 || indexStudentFname == -1 || indexProgYrSec == -1) {
                    Toast.makeText(this, "Database columns not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Iterate over the cursor rows.
                while (cursor.moveToNext()) {
                    String studentNum   = cursor.getString(indexStudentNum);
                    String studentFname = cursor.getString(indexStudentFname);
                    String progYrSec    = cursor.getString(indexProgYrSec);

                    // Format the row data as a comma-delimited string.
                    String rowData = studentNum + ", " + studentFname + ", " + progYrSec;
                    addTableRow(rowData);
                }
            } catch (Exception ex) {
                Toast.makeText(this, "Error loading attendance records", Toast.LENGTH_SHORT).show();
            } finally {
                cursor.close();
            }
        } else {
            Toast.makeText(this, "No records found in the database.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Displays a confirmation dialog for a scanned entry.
     *
     * @param scanResult The scanned data.
     */
    private void showConfirmationDialog(final String scanResult) {
        new AlertDialog.Builder(AttendanceList.this)
                .setTitle("Confirm Entry")
                .setMessage("Confirm scanned entry:\n" + scanResult)
                .setCancelable(false)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Optionally, add the scanned entry to the table.
                        addTableRow(scanResult);
                        Toast.makeText(AttendanceList.this, "Entry Recorded", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Scan Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(AttendanceList.this, BarCodeScan.class);
                        startActivityForResult(intent, REQUEST_CODE_SCAN);
                    }
                })
                .show();
    }

    /**
     * Adds a new TableRow to the attendance table using the supplied entry text.
     * The entry text is assumed to be a comma-delimited string (e.g., "studentNum, studentFname, progYrSec").
     *
     * @param entryText The text for the new row.
     */
    private void addTableRow(String entryText) {
        // Create a new TableRow.
        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        // Split the entry text into columns.
        String[] columns = entryText.split(",");
        for (String col : columns) {
            TextView textView = new TextView(this);
            textView.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            textView.setText(col.trim());
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setPadding(16, 16, 16, 16);
            tableRow.addView(textView);
        }
        tableRow.setBackgroundResource(android.R.drawable.list_selector_background);
        // Add the row to the TableLayout through the binding.
        binding.attendanceTable.addView(tableRow);
    }
}
