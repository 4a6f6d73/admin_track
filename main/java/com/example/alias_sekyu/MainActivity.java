package com.example.alias_sekyu;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import android.widget.AutoCompleteTextView;

public class MainActivity extends AppCompatActivity {

    private DBHelper dbHelper;

    @Override
    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DBHelper(this);

        // Check if there is already an organization registered.
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM org_ident", null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            startActivity(new Intent(MainActivity.this, AdminLogIn.class));
            finish();
            return;
        }
        if (cursor != null) {
            cursor.close();
        }

        // No account registered; proceed with registration UI.
        setContentView(R.layout.activity_main);

        TextInputEditText orgName = findViewById(R.id.orgName);
        AutoCompleteTextView colSel = findViewById(R.id.colSel);
        TextInputEditText progSel = findViewById(R.id.progSel);
        TextInputEditText uname = findViewById(R.id.makeUname);
        TextInputEditText pword = findViewById(R.id.makePword);

        Button regApp = findViewById(R.id.RegApp);
        regApp.setOnClickListener(view -> {
            String name = orgName.getText().toString().trim();
            String college = colSel.getText().toString().trim();
            String program = progSel.getText().toString().trim();
            String username = uname.getText().toString().trim();
            String password = pword.getText().toString().trim();

            if (name.isEmpty() || college.isEmpty() || program.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            long result = dbHelper.insertOrgIdentity(name, college, program, username, password);
            if (result != -1) {
                Toast.makeText(MainActivity.this, "Organization registered!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, AdminLogIn.class));
                finish();
            } else {
                Toast.makeText(MainActivity.this, "Registration failed. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
