package com.example.alias_sekyu.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.alias_sekyu.AttendanceList;
import com.example.alias_sekyu.DBHelper;
import com.example.alias_sekyu.R;
import com.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class admin_event extends Fragment {

    private View view;
    private ImageButton importData, checkData, archiveDel;
    private ActivityResultLauncher<Intent> launcher;
    // This variable holds the raw CSV rows in case you want to use it later.
    private List<String[]> parsedCsvData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_admin_event, container, false);

        importData = view.findViewById(R.id.importData);
        checkData = view.findViewById(R.id.checkData);
        archiveDel = view.findViewById(R.id.archiveDel);

        setupCSVPicker();

        // Set up the "Import CSV" button. When pressed, it launches the file picker.
        importData.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("text/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            launcher.launch(Intent.createChooser(intent, "Select CSV File"));
        });

        // When "Check Data" is clicked, display the imported CSV data.
        checkData.setOnClickListener(v -> {
            StringBuilder sb = new StringBuilder();
            if (parsedCsvData != null && !parsedCsvData.isEmpty()) {
                for (String[] row : parsedCsvData) {
                    sb.append(String.join(",", row)).append("\n");
                }
            }
            Intent intent = new Intent(getActivity(), AttendanceList.class);
            intent.putExtra("CSV_DATA", sb.toString());
            startActivity(intent);
        });


        archiveDel.setOnClickListener(v ->
                Toast.makeText(getContext(), "Archiving data and ending an organization's session will be available soon.", Toast.LENGTH_SHORT).show()
        );

        return view;
    }

    private void setupCSVPicker() {
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            readCSVFromUri(uri);
                        } else {
                            Toast.makeText(getContext(), "No file selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void readCSVFromUri(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                throw new Exception("Unable to open input stream");
            }

            CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream));
            parsedCsvData = csvReader.readAll();
            csvReader.close();

            // Now seamlessly insert CSV data into the database using DBHelper.
            DBHelper dbHelper = new DBHelper(getContext());
            int insertedCount = 0;
            // Assuming the CSV data is arranged as: studentID, fullName, progYrSec
            for (String[] row : parsedCsvData) {
                // Validate that the row contains at least three columns.
                if (row.length < 3) continue;
                String studentID = row[0].trim();
                String fullName = row[1].trim();
                String progYrSec = row[2].trim();
                long result = dbHelper.insertStudent(studentID, fullName, progYrSec);
                if (result != -1) {
                    insertedCount++;
                }
            }
            Toast.makeText(getContext(), "CSV imported successfully. " + insertedCount + " records inserted.", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("CSV_IMPORT_ERROR", "Failed to read CSV", e);
            Toast.makeText(getContext(), "Error reading CSV file", Toast.LENGTH_LONG).show();
        }
    }
}
