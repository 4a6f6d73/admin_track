package com.example.alias_sekyu;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    // Database constants
    private static final String DATABASE_NAME = "centralizedDB.db";
    private static final int DATABASE_VERSION = 4;

    // Table names
    public static final String TABLE_ORG_IDENT = "org_ident";
    public static final String TABLE_EVENTS = "events";
    public static final String TABLE_STUDENTS = "students";
    public static final String TABLE_ATTENDANCE = "attendance";

    //Columns for Organization Identity
    public static final String COLUMN_ORG_ID = "org_id";
    public static final String COLUMN_ORG_NAME = "org_name";
    public static final String COLUMN_ORG_COLLEGE = "orgCollege";
    public static final String COLUMN_ORG_PROG = "orgProg";
    public static final String COLUMN_ORG_UNAME = "orgUname";
    public static final String COLUMN_ORG_PWORD = "orgPword";


    // Columns for Created Events Table
    public static final String COLUMN_EVENT_ID = "event_id";
    public static final String COLUMN_EVENT_TITLE = "event_title";  // formerly eventName
    public static final String COLUMN_EVENT_VENUE = "event_venue";
    public static final String COLUMN_EVENT_DATE = "event_date";
    public static final String COLUMN_EVENT_TIME = "event_time";

    // Columns for Student Dataset Table
    public static final String COLUMN_STUDENT_NUM = "student_num";   // formerly studentID
    public static final String COLUMN_STUDENT_FNAME = "student_fname"; // formerly fullName
    public static final String COLUMN_PROG_YR_SEC = "prog_yr_sec";

    // Column for Recorded Student Attendance
    public static final String COLUMN_REC_ID = "rec_id";
    // Attendance table will store: event_id, student_num, student_fname, prog_yr_sec

    // SQL Statement to Crate Organization Identity Table
    private static final String CREATE_TABLE_ORG_IDENT =
            "CREATE TABLE " + TABLE_ORG_IDENT + " (" +
                    COLUMN_ORG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_ORG_NAME + " VARCHAR(64), " +
                    COLUMN_ORG_COLLEGE + " TEXT, " +
                    COLUMN_ORG_PROG + " TEXT, " +
                    COLUMN_ORG_UNAME + " VARCHAR(12), " +
                    COLUMN_ORG_PWORD + " VARCHAR(12)" +
                    ");";

    // SQL statement to Create Events table
    private static final String CREATE_TABLE_EVENTS =
            "CREATE TABLE " + TABLE_EVENTS + " (" +
                    COLUMN_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_EVENT_TITLE + " TEXT, " +
                    COLUMN_EVENT_VENUE + " TEXT, " +
                    COLUMN_EVENT_DATE + " TEXT, " +
                    COLUMN_EVENT_TIME + " TEXT" +
                    ");";

    // SQL statement to Create Student Dataset Table
    private static final String CREATE_TABLE_STUDENTS =
            "CREATE TABLE " + TABLE_STUDENTS + " (" +
                    COLUMN_STUDENT_NUM + " TEXT PRIMARY KEY, " +
                    COLUMN_STUDENT_FNAME + " TEXT, " +
                    COLUMN_PROG_YR_SEC + " TEXT" +
                    ");";

    // SQL statement to Create Recorded Attendance Table
    private static final String CREATE_TABLE_ATTENDANCE =
            "CREATE TABLE " + TABLE_ATTENDANCE + " (" +
                    COLUMN_REC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_EVENT_ID + " INTEGER, " +
                    COLUMN_EVENT_TITLE + " TEXT, " +    // Make sure this column exists!
                    COLUMN_STUDENT_NUM + " TEXT, " +
                    COLUMN_STUDENT_FNAME + " TEXT, " +
                    COLUMN_PROG_YR_SEC + " TEXT" +
                    ");";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database is created for the first time.
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ORG_IDENT);
        db.execSQL(CREATE_TABLE_EVENTS);
        db.execSQL(CREATE_TABLE_STUDENTS);
        db.execSQL(CREATE_TABLE_ATTENDANCE);
    }

    // Called when the database needs to be upgraded.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For simplicity, drop the old tables and recreate new ones.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORG_IDENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTENDANCE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(db);
    }

    // ----------------------- Insertion Methods ----------------------- //

    //Insert a New Organization Identity (only one)
    public long insertOrgIdentity(String orgName, String orgCollege, String orgProg, String orgUname, String orgPword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORG_NAME, orgName);
        values.put(COLUMN_ORG_COLLEGE, orgCollege);
        values.put(COLUMN_ORG_PROG, orgProg);
        values.put(COLUMN_ORG_UNAME, orgUname);
        values.put(COLUMN_ORG_PWORD, orgPword);

        long result = db.insert(TABLE_ORG_IDENT, null, values);
        if (result == -1) {
            Log.e("DBHelper", "insertOrgIdentity FAILED: " + values.toString());
        }
        return result;
    }

    // Insert a new event.
    public long insertEvent(String eventTitle, String eventVenue, String eventDate, String eventTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_TITLE, eventTitle);
        values.put(COLUMN_EVENT_VENUE, eventVenue);
        values.put(COLUMN_EVENT_DATE, eventDate);
        values.put(COLUMN_EVENT_TIME, eventTime);
        return db.insert(TABLE_EVENTS, null, values);
    }

    // Insert a new student record.
    public long insertStudent(String studentNum, String studentFname, String progYrSec) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STUDENT_NUM, studentNum);
        values.put(COLUMN_STUDENT_FNAME, studentFname);
        values.put(COLUMN_PROG_YR_SEC, progYrSec);
        return db.insert(TABLE_STUDENTS, null, values);
    }

    // Insert a new attendance record.
    public long insertAttendance(int eventId, String eventTitle, String studentNum, String studentFname, String progYrSec) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_ID, eventId);
        values.put(COLUMN_EVENT_TITLE, eventTitle);  // Make sure COLUMN_EVENT_TITLE matches the schema exactly.
        values.put(COLUMN_STUDENT_NUM, studentNum);
        values.put(COLUMN_STUDENT_FNAME, studentFname);
        values.put(COLUMN_PROG_YR_SEC, progYrSec);
        long id = db.insert(TABLE_ATTENDANCE, null, values);
        android.util.Log.d("DBHelper", "insertAttendance returned id: " + id);
        return id;
    }

    // ----------------------- GET Methods ----------------------- //

    // Get all events.
    public Cursor getAllEvents() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_EVENTS, null);
    }

    public List<String> getAllEventNames() {
        List<String> titles = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_EVENT_TITLE + " FROM " + TABLE_EVENTS;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    @SuppressLint("Range")
                    String title = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_TITLE));
                    titles.add(title);
                }
            } finally {
                cursor.close();
            }
        }
        return titles;
    }

    // Get all students.
    public Cursor getAllStudents() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_STUDENTS, null);
    }

    // Returns a Cursor with attendance summary data by joining attendance, students, and events.
    public Cursor getAttendanceSummary() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query =
                "SELECT att." + COLUMN_REC_ID + " AS attendance_id, " +
                        "att." + COLUMN_EVENT_TITLE + " AS event_title, " +
                        "att." + COLUMN_EVENT_ID + " AS event_id, " +
                        "stu." + COLUMN_STUDENT_NUM + " AS student_num, " +
                        "stu." + COLUMN_STUDENT_FNAME + " AS student_fname, " +
                        "stu." + COLUMN_PROG_YR_SEC + " AS prog_yr_sec " +
                        "FROM " + TABLE_ATTENDANCE + " att " +
                        "LEFT JOIN " + TABLE_STUDENTS + " stu " +
                        "ON att." + COLUMN_STUDENT_NUM + " = stu." + COLUMN_STUDENT_NUM + " " +
                        "ORDER BY att." + COLUMN_REC_ID + " ASC";
        android.util.Log.d("DBHelper", "Attendance Summary Query: " + query);
        return db.rawQuery(query, null);
    }

    public String getEnrolledStudentByBarcode(String barcode) {
        SQLiteDatabase db = this.getReadableDatabase();
        String studentRecord = null;

        // Query the students table where student_num equals the scanned barcode.
        String query = "SELECT * FROM " + TABLE_STUDENTS +
                " WHERE " + COLUMN_STUDENT_NUM + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{barcode});

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    @SuppressLint("Range")
                    String studentNum = cursor.getString(cursor.getColumnIndex(COLUMN_STUDENT_NUM));
                    @SuppressLint("Range")
                    String studentFname = cursor.getString(cursor.getColumnIndex(COLUMN_STUDENT_FNAME));
                    @SuppressLint("Range")
                    String progYrSec = cursor.getString(cursor.getColumnIndex(COLUMN_PROG_YR_SEC));
                    // Build a comma-delimited record string.
                    studentRecord = studentNum + "," + studentFname + "," + progYrSec;
                }
            } finally {
                cursor.close();
            }
        }
        return studentRecord;
    }

    public Cursor getAttendanceRecordsByEvent(String eventTitle) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_ATTENDANCE + " WHERE " + COLUMN_EVENT_TITLE + " = ?";
        return db.rawQuery(query, new String[]{ eventTitle });
    }

    // ----------------------- UPDATE Methods ----------------------- //

    // Update an event.
    public int updateEvent(int eventId, String eventTitle, String eventVenue, String eventDate, String eventTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_TITLE, eventTitle);
        values.put(COLUMN_EVENT_VENUE, eventVenue);
        values.put(COLUMN_EVENT_DATE, eventDate);
        values.put(COLUMN_EVENT_TIME, eventTime);
        return db.update(TABLE_EVENTS, values, COLUMN_EVENT_ID + " = ?",
                new String[]{String.valueOf(eventId)});
    }

    // Update a student's record.
    public int updateStudent(String studentNum, String studentFname, String progYrSec) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STUDENT_FNAME, studentFname);
        values.put(COLUMN_PROG_YR_SEC, progYrSec);
        return db.update(TABLE_STUDENTS, values, COLUMN_STUDENT_NUM + " = ?",
                new String[]{studentNum});
    }

    // Update an attendance record.
    public int updateAttendance(int recId, int eventId, String studentNum, String studentFname, String progYrSec) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_ID, eventId);
        values.put(COLUMN_STUDENT_NUM, studentNum);
        values.put(COLUMN_STUDENT_FNAME, studentFname);
        values.put(COLUMN_PROG_YR_SEC, progYrSec);
        return db.update(TABLE_ATTENDANCE, values, COLUMN_REC_ID + " = ?",
                new String[]{String.valueOf(recId)});
    }

    // ----------------------- DELETE Methods ----------------------- //

    // Delete an event.
    public int deleteEvent(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_EVENTS, COLUMN_EVENT_ID + " = ?",
                new String[]{String.valueOf(eventId)});
    }

    // Delete an attendance record.
    public int deleteAttendance(int recId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_ATTENDANCE, COLUMN_REC_ID + " = ?",
                new String[]{String.valueOf(recId)});
    }
}
