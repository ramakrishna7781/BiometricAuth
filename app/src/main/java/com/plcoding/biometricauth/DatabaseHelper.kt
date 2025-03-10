package com.plcoding.biometricauth

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// DatabaseHelper class for interacting with SQLite
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // Database name and version
    companion object {
        const val DATABASE_NAME = "VoterDatabase.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "voter_details"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_VOTER_ID = "voter_id"
        const val FINGERPRINT_COUNT_TABLE = "fingerprint_count"
        const val COLUMN_COUNT = "count"
    }

    // Voter data class inside DatabaseHelper
    data class Voter(
        val id: Long,
        val name: String,
        val phone: String,
        val voterId: String,
        val verificationCount: Int // Added verification count field
    )

    override fun onCreate(db: SQLiteDatabase?) {
        // SQL query to create the voter details table
        val CREATE_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_NAME TEXT NOT NULL,
            $COLUMN_PHONE TEXT NOT NULL,
            $COLUMN_VOTER_ID TEXT NOT NULL
        );
    """

        // SQL query to create the fingerprint count table
        val CREATE_FINGERPRINT_COUNT_TABLE = """
        CREATE TABLE $FINGERPRINT_COUNT_TABLE (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_COUNT INTEGER DEFAULT 0
        );
    """

        db?.execSQL(CREATE_TABLE)
        db?.execSQL(CREATE_FINGERPRINT_COUNT_TABLE)

        // Insert initial value for global count if not exists
        val insertInitialCount = "INSERT INTO $FINGERPRINT_COUNT_TABLE ($COLUMN_ID, $COLUMN_COUNT) VALUES (1, 0)"
        db?.execSQL(insertInitialCount)
    }


    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Drop old tables if exists and create new ones
        val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
        val DROP_FINGERPRINT_COUNT_TABLE = "DROP TABLE IF EXISTS $FINGERPRINT_COUNT_TABLE"

        db?.execSQL(DROP_TABLE)
        db?.execSQL(DROP_FINGERPRINT_COUNT_TABLE)
        onCreate(db)
    }

    // Function to insert voter into the database
    fun insertVoter(name: String, phone: String, voterId: String): Long {
        val db = this.writableDatabase
        val values = android.content.ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_PHONE, phone)
            put(COLUMN_VOTER_ID, voterId)
        }

        val id = db.insert(TABLE_NAME, null, values)
        db.close()
        return id
    }

    // Retrieve all voter details
    @SuppressLint("Range")
    fun getAllVoters(): List<Voter> {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)
        val voters = mutableListOf<Voter>()

        if (cursor.moveToFirst()) {
            do {
                val voter = Voter(
                    cursor.getLong(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_PHONE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_VOTER_ID)),
                    getFingerprintVerificationCount() // Add the verification count from the count table
                )
                voters.add(voter)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return voters
    }

    // Function to increment the fingerprint verification count
    fun incrementFingerprintVerificationCount() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // Update the global count in the fingerprint_count table
            val query = "UPDATE $FINGERPRINT_COUNT_TABLE SET $COLUMN_COUNT = $COLUMN_COUNT + 1 WHERE $COLUMN_ID = 1"
            db.execSQL(query)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }


    // Function to get the current fingerprint verification count
    @SuppressLint("Range")
    fun getFingerprintVerificationCount(): Int {
        val db = this.readableDatabase
        val cursor = db.query(FINGERPRINT_COUNT_TABLE, null, null, null, null, null, null)
        var count = 0

        if (cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndex(COLUMN_COUNT))
        }
        cursor.close()
        db.close()
        return count
    }
}
