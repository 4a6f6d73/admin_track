package com.example.alias_sekyu;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import java.util.List;

public class BarCodeScan extends CaptureActivity {

    private DecoratedBarcodeView barcodeView;
    private boolean hasScannedCode = false;
    private int eventId; // This will hold the event ID passed from PreScanSel (via GlobalData)
    private String eventTitle;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the event ID from GlobalData.
        eventId = GlobalData.EVENT_ID;
        eventTitle = GlobalData.EVENT_TITLE;


        if (eventId == -1) {
            Toast.makeText(BarCodeScan.this, "EventID Not Found, Please Try Again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize our DBHelper.
        dbHelper = new DBHelper(this);
        // Obtain the DecoratedBarcodeView from the layout.
        barcodeView = findViewById(com.google.zxing.client.android.R.id.zxing_barcode_scanner);
        if (barcodeView != null) {
            startBarcodeScanning();
        }
    }

    private void startBarcodeScanning() {
        barcodeView.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null && !hasScannedCode) {
                    hasScannedCode = true;
                    handleScannedBarcode(result.getText());
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                // Not required.
            }
        });
    }

    private void handleScannedBarcode(String scanResult) {
        runOnUiThread(() -> {
            if (barcodeView != null)
                barcodeView.pause();
            AlertDialog.Builder builder = new AlertDialog.Builder(BarCodeScan.this)
                    .setTitle("Scan Result")
                    .setMessage(scanResult)
                    .setCancelable(false)
                    .setPositiveButton("Transmit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            processScannedBarcode(scanResult);
                            resumeScanning();
                        }
                    })
                    .setNegativeButton("Scan Again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            resumeScanning();
                        }
                    });
            builder.create().show();
        });
    }

    private void processScannedBarcode(String scanResult) {
        // Retrieve the student record as a comma-delimited string ("student_num,student_fname,prog_yr_sec")
        String studentRecord = dbHelper.getEnrolledStudentByBarcode(scanResult);
        if (studentRecord != null && !studentRecord.isEmpty()) {
            String[] parts = studentRecord.split(",");
            if (parts.length >= 3) {
                String studentNum   = parts[0].trim();
                String studentFname = parts[1].trim();
                String progYrSec    = parts[2].trim();
                // Now insert the attendance record using the event ID and event title from GlobalData.
                long attendanceId = dbHelper.insertAttendance(eventId, eventTitle, studentNum, studentFname, progYrSec);
                if (attendanceId != -1) {
                    Toast.makeText(BarCodeScan.this, "Attendance Recorded", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BarCodeScan.this, "Failed to Record Attendance", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(BarCodeScan.this, "Incomplete student record", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(BarCodeScan.this, "Student not found", Toast.LENGTH_SHORT).show();
        }
    }


    private void resumeScanning() {
        hasScannedCode = false;
        if (barcodeView != null) {
            startBarcodeScanning();
            barcodeView.resume();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeView != null && !hasScannedCode) {
            barcodeView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
