package com.example.alias_sekyu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class AdminLogIn extends AppCompatActivity {

    private TextInputEditText orgUname, orgPword;
    private Button loginBtn, appInfoBtn;
    private CheckBox rememberMe;
    private DBHelper dbHelper;

    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_REMEMBER = "rememberMe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check saved preference
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean(KEY_REMEMBER, false)) {
            startActivity(new Intent(this, HomePage.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_login);

        orgUname = findViewById(R.id.orgUname);
        orgPword = findViewById(R.id.orgPword);
        loginBtn = findViewById(R.id.login);
        appInfoBtn = findViewById(R.id.appInfo);
        rememberMe = findViewById(R.id.dontForget);
        dbHelper = new DBHelper(this);

        loginBtn.setOnClickListener(v -> checkLogin());
        appInfoBtn.setOnClickListener(v -> Toast.makeText(this, "Easter Egg -- Mabutlaw na po", Toast.LENGTH_SHORT).show());
    }

    private void checkLogin() {
        String username = orgUname.getText() != null ? orgUname.getText().toString().trim() : "";
        String password = orgPword.getText() != null ? orgPword.getText().toString().trim() : "";

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM org_ident WHERE orgUname = ? AND orgPword = ?",
                new String[]{username, password}
        );

        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();

            if (rememberMe.isChecked()) {
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putBoolean(KEY_REMEMBER, true);
                editor.apply();
            }

            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomePage.class));
            finish();
        } else {
            Toast.makeText(this, "Incorrect credentials, try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
