package com.unimate.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.unimate.model.Opportunity;

import java.time.LocalDate;

public class SeedData {

    private static final String TAG = "UniMate_SeedData";

    public static void seedInitialData(SQLiteDatabase db) {
        try {
            Log.i(TAG, "Seeding initial sample places and opportunities...");
            seedStudyPlaces(db);
            seedOpportunities(db);
            seedSampleTasksAndEvents(db);
        } catch (Exception e) {
            Log.e(TAG, "Error seeding database: " + e.getMessage(), e);
        }
    }

    private static void seedStudyPlaces(SQLiteDatabase db) {
        // Place 1: Campus Central Library
        ContentValues p1 = new ContentValues();
        p1.put(DatabaseHelper.COLUMN_PLACE_NAME, "Campus Central Library");
        p1.put(DatabaseHelper.COLUMN_PLACE_ADDRESS, "Main Campus, Building A");
        p1.put(DatabaseHelper.COLUMN_PLACE_PRICE_LEVEL, 1); // Free
        p1.put(DatabaseHelper.COLUMN_PLACE_NOISE_LEVEL, 1); // Very Quiet
        p1.put(DatabaseHelper.COLUMN_PLACE_WIFI, 1);
        p1.put(DatabaseHelper.COLUMN_PLACE_OUTLETS, 1);
        p1.put(DatabaseHelper.COLUMN_PLACE_HOURS, "08:00 - 20:00");
        p1.put(DatabaseHelper.COLUMN_PLACE_LAT, 36.8065);
        p1.put(DatabaseHelper.COLUMN_PLACE_LNG, 10.1815);
        p1.put(DatabaseHelper.COLUMN_PLACE_CATEGORY, "Library");
        db.insert(DatabaseHelper.TABLE_STUDY_PLACES, null, p1);

        // Place 2: Tech Study Cafe
        ContentValues p2 = new ContentValues();
        p2.put(DatabaseHelper.COLUMN_PLACE_NAME, "Tech Study Cafe");
        p2.put(DatabaseHelper.COLUMN_PLACE_ADDRESS, "University Avenue 12");
        p2.put(DatabaseHelper.COLUMN_PLACE_PRICE_LEVEL, 2); // Moderate
        p2.put(DatabaseHelper.COLUMN_PLACE_NOISE_LEVEL, 2); // Quiet
        p2.put(DatabaseHelper.COLUMN_PLACE_WIFI, 1);
        p2.put(DatabaseHelper.COLUMN_PLACE_OUTLETS, 1);
        p2.put(DatabaseHelper.COLUMN_PLACE_HOURS, "07:30 - 22:00");
        p2.put(DatabaseHelper.COLUMN_PLACE_LAT, 36.8080);
        p2.put(DatabaseHelper.COLUMN_PLACE_LNG, 10.1830);
        p2.put(DatabaseHelper.COLUMN_PLACE_CATEGORY, "Café");
        db.insert(DatabaseHelper.TABLE_STUDY_PLACES, null, p2);

        // Place 3: Quiet Corner Lounge
        ContentValues p3 = new ContentValues();
        p3.put(DatabaseHelper.COLUMN_PLACE_NAME, "Quiet Corner Lounge");
        p3.put(DatabaseHelper.COLUMN_PLACE_ADDRESS, "Science Block, Floor 2");
        p3.put(DatabaseHelper.COLUMN_PLACE_PRICE_LEVEL, 1);
        p3.put(DatabaseHelper.COLUMN_PLACE_NOISE_LEVEL, 1);
        p3.put(DatabaseHelper.COLUMN_PLACE_WIFI, 1);
        p3.put(DatabaseHelper.COLUMN_PLACE_OUTLETS, 0);
        p3.put(DatabaseHelper.COLUMN_PLACE_HOURS, "08:00 - 18:00");
        p3.put(DatabaseHelper.COLUMN_PLACE_LAT, 36.8050);
        p3.put(DatabaseHelper.COLUMN_PLACE_LNG, 10.1800);
        p3.put(DatabaseHelper.COLUMN_PLACE_CATEGORY, "Lounge");
        db.insert(DatabaseHelper.TABLE_STUDY_PLACES, null, p3);

        // Place 4: Innovation Hub Coworking
        ContentValues p4 = new ContentValues();
        p4.put(DatabaseHelper.COLUMN_PLACE_NAME, "Innovation Hub Coworking");
        p4.put(DatabaseHelper.COLUMN_PLACE_ADDRESS, "Tech Park Plaza");
        p4.put(DatabaseHelper.COLUMN_PLACE_PRICE_LEVEL, 3); // Premium
        p4.put(DatabaseHelper.COLUMN_PLACE_NOISE_LEVEL, 3); // Moderate
        p4.put(DatabaseHelper.COLUMN_PLACE_WIFI, 1);
        p4.put(DatabaseHelper.COLUMN_PLACE_OUTLETS, 1);
        p4.put(DatabaseHelper.COLUMN_PLACE_HOURS, "24/7");
        p4.put(DatabaseHelper.COLUMN_PLACE_LAT, 36.8100);
        p4.put(DatabaseHelper.COLUMN_PLACE_LNG, 10.1860);
        p4.put(DatabaseHelper.COLUMN_PLACE_CATEGORY, "Coworking");
        db.insert(DatabaseHelper.TABLE_STUDY_PLACES, null, p4);
    }

    private static void seedOpportunities(SQLiteDatabase db) {
        LocalDate today = LocalDate.now();

        // Opportunity 1: Mobile App Hackathon
        ContentValues o1 = new ContentValues();
        o1.put(DatabaseHelper.COLUMN_OPP_TITLE, "National Mobile App Hackathon 2025");
        o1.put(DatabaseHelper.COLUMN_OPP_DESCRIPTION, "48-hour challenge to build innovative Android solutions for university life.");
        o1.put(DatabaseHelper.COLUMN_OPP_CATEGORY, Opportunity.Category.HACKATHON.name());
        o1.put(DatabaseHelper.COLUMN_OPP_DEADLINE, today.plusDays(10).toString());
        o1.put(DatabaseHelper.COLUMN_OPP_LINK, "https://hackathon.university.edu");
        o1.put(DatabaseHelper.COLUMN_OPP_ORG, "Computer Science Department");
        db.insert(DatabaseHelper.TABLE_OPPORTUNITIES, null, o1);

        // Opportunity 2: Android Dev Workshop
        ContentValues o2 = new ContentValues();
        o2.put(DatabaseHelper.COLUMN_OPP_TITLE, "Advanced Android Architecture Workshop");
        o2.put(DatabaseHelper.COLUMN_OPP_DESCRIPTION, "Master MVC, clean architecture, and SQLite optimization with industry mentors.");
        o2.put(DatabaseHelper.COLUMN_OPP_CATEGORY, Opportunity.Category.WORKSHOP.name());
        o2.put(DatabaseHelper.COLUMN_OPP_DEADLINE, today.plusDays(5).toString());
        o2.put(DatabaseHelper.COLUMN_OPP_LINK, "https://workshop.devclub.org");
        o2.put(DatabaseHelper.COLUMN_OPP_ORG, "Google Developer Student Club");
        db.insert(DatabaseHelper.TABLE_OPPORTUNITIES, null, o2);

        // Opportunity 3: Summer Software Engineering Internship
        ContentValues o3 = new ContentValues();
        o3.put(DatabaseHelper.COLUMN_OPP_TITLE, "Summer Mobile Engineering Internship");
        o3.put(DatabaseHelper.COLUMN_OPP_DESCRIPTION, "Paid 3-month summer position developing native Android apps.");
        o3.put(DatabaseHelper.COLUMN_OPP_CATEGORY, Opportunity.Category.INTERNSHIP.name());
        o3.put(DatabaseHelper.COLUMN_OPP_DEADLINE, today.plusDays(21).toString());
        o3.put(DatabaseHelper.COLUMN_OPP_LINK, "https://careers.techcorp.com");
        o3.put(DatabaseHelper.COLUMN_OPP_ORG, "TechCorp Solutions");
        db.insert(DatabaseHelper.TABLE_OPPORTUNITIES, null, o3);

        // Opportunity 4: Merit Scholarship
        ContentValues o4 = new ContentValues();
        o4.put(DatabaseHelper.COLUMN_OPP_TITLE, "STEM Excellence Excellence Grant");
        o4.put(DatabaseHelper.COLUMN_OPP_DESCRIPTION, "Financial award for outstanding computer science and engineering students.");
        o4.put(DatabaseHelper.COLUMN_OPP_CATEGORY, Opportunity.Category.SCHOLARSHIP.name());
        o4.put(DatabaseHelper.COLUMN_OPP_DEADLINE, today.plusDays(30).toString());
        o4.put(DatabaseHelper.COLUMN_OPP_LINK, "https://grants.stemfoundation.org");
        o4.put(DatabaseHelper.COLUMN_OPP_ORG, "STEM Foundation");
        db.insert(DatabaseHelper.TABLE_OPPORTUNITIES, null, o4);
    }

    private static void seedSampleTasksAndEvents(SQLiteDatabase db) {
        LocalDate today = LocalDate.now();

        // Sample Task 1
        ContentValues t1 = new ContentValues();
        t1.put(DatabaseHelper.COLUMN_STUDENT_ID, 1);
        t1.put(DatabaseHelper.COLUMN_TASK_TITLE, "Finish Android Mobile Project");
        t1.put(DatabaseHelper.COLUMN_TASK_DESCRIPTION, "Complete MVC implementation, SQLite storage, and UI testing.");
        t1.put(DatabaseHelper.COLUMN_TASK_CATEGORY, "PROJECT");
        t1.put(DatabaseHelper.COLUMN_TASK_PRIORITY, "HIGH");
        t1.put(DatabaseHelper.COLUMN_TASK_DEADLINE, today.plusDays(3).toString());
        t1.put(DatabaseHelper.COLUMN_TASK_DURATION, 120);
        t1.put(DatabaseHelper.COLUMN_TASK_STATUS, "PENDING");
        long taskId1 = db.insert(DatabaseHelper.TABLE_TASKS, null, t1);

        // Sample Task 2
        ContentValues t2 = new ContentValues();
        t2.put(DatabaseHelper.COLUMN_STUDENT_ID, 1);
        t2.put(DatabaseHelper.COLUMN_TASK_TITLE, "Revise Operating Systems Chapter 4");
        t2.put(DatabaseHelper.COLUMN_TASK_DESCRIPTION, "Study process synchronization and semaphores.");
        t2.put(DatabaseHelper.COLUMN_TASK_CATEGORY, "EXAM");
        t2.put(DatabaseHelper.COLUMN_TASK_PRIORITY, "MEDIUM");
        t2.put(DatabaseHelper.COLUMN_TASK_DEADLINE, today.plusDays(5).toString());
        t2.put(DatabaseHelper.COLUMN_TASK_DURATION, 90);
        t2.put(DatabaseHelper.COLUMN_TASK_STATUS, "PENDING");
        db.insert(DatabaseHelper.TABLE_TASKS, null, t2);

        // Sample Event 1: Today Lecture
        ContentValues e1 = new ContentValues();
        e1.put(DatabaseHelper.COLUMN_STUDENT_ID, 1);
        e1.put(DatabaseHelper.COLUMN_EVENT_TITLE, "Mobile Architecture Lecture");
        e1.put(DatabaseHelper.COLUMN_EVENT_DATE, today.toString());
        e1.put(DatabaseHelper.COLUMN_EVENT_START_TIME, "09:00");
        e1.put(DatabaseHelper.COLUMN_EVENT_END_TIME, "11:00");
        e1.put(DatabaseHelper.COLUMN_EVENT_LOCATION, "Amphi 3");
        e1.put(DatabaseHelper.COLUMN_EVENT_TYPE, "LECTURE");
        e1.put(DatabaseHelper.COLUMN_EVENT_TASK_ID, -1);
        db.insert(DatabaseHelper.TABLE_EVENTS, null, e1);

        // Sample Event 2: Linked Study Event
        ContentValues e2 = new ContentValues();
        e2.put(DatabaseHelper.COLUMN_STUDENT_ID, 1);
        e2.put(DatabaseHelper.COLUMN_EVENT_TITLE, "Study: Finish Android Mobile Project");
        e2.put(DatabaseHelper.COLUMN_EVENT_DATE, today.toString());
        e2.put(DatabaseHelper.COLUMN_EVENT_START_TIME, "14:00");
        e2.put(DatabaseHelper.COLUMN_EVENT_END_TIME, "15:30");
        e2.put(DatabaseHelper.COLUMN_EVENT_LOCATION, "Campus Library");
        e2.put(DatabaseHelper.COLUMN_EVENT_TYPE, "STUDY");
        e2.put(DatabaseHelper.COLUMN_EVENT_TASK_ID, taskId1);
        db.insert(DatabaseHelper.TABLE_EVENTS, null, e2);
    }
}
