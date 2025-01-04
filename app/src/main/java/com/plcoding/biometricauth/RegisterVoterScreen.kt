package com.plcoding.biometricauth

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

// Data model for a Voter
data class Voter(val id: Long, val name: String, val phone: String, val voterId: String)

// Custom Text Field Function
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = { onValueChange(it) },
        textStyle = TextStyle(color = Color.Black),
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        modifier = modifier
            .background(Color.White)
            .padding(16.dp)
            .fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.padding(8.dp)) {
                if (value.isEmpty()) {
                    Text(
                        text = label,
                        style = TextStyle(color = Color.Gray),
                    )
                }
                innerTextField()
            }
        }
    )
}


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
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // SQL query to create the table
        val CREATE_TABLE = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_PHONE TEXT NOT NULL,
                $COLUMN_VOTER_ID TEXT NOT NULL
            );
        """
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Drop old table if exists and create a new one
        val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(DROP_TABLE)
        onCreate(db)
    }

    // Insert data into the database
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
                    cursor.getString(cursor.getColumnIndex(COLUMN_VOTER_ID))
                )
                voters.add(voter)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return voters
    }
}

@Composable
fun RegisterVoterScreen() {

    val keyboardController = LocalSoftwareKeyboardController.current

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var voterId by remember { mutableStateOf("") }
    var fingerprintVerified by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Database helper instance
    val dbHelper = DatabaseHelper(context)

    // Function to handle fingerprint verification
    /*val verifyFingerprint = {
        // Simulate fingerprint verification success
        fingerprintVerified = true
        Toast.makeText(context, "Fingerprint verified successfully", Toast.LENGTH_SHORT).show()
    }*/

    // Function to handle fingerprint verification
    val verifyFingerprint = {
        // Simulate fingerprint verification success
        fingerprintVerified = true
        keyboardController?.hide() // Hides the keyboard
        Toast.makeText(context, "Fingerprint Registered successfully", Toast.LENGTH_SHORT).show()
    }


    // Register button onClick
    /*val registerVoter = {
        if (name.isNotEmpty() && phone.length == 10 && voterId.isNotEmpty() && fingerprintVerified) {
            // Insert the voter into the database
            val id = dbHelper.insertVoter(name, phone, voterId)
            Toast.makeText(context, "Voter Registered Successfully: $id", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Please fill in all fields and verify fingerprint", Toast.LENGTH_SHORT).show()
        }
    }*/
    val registerVoter = {
        if (name.isNotEmpty() && phone.length == 10 && voterId.isNotEmpty() && fingerprintVerified) {
            // Insert the voter into the database
            val id = dbHelper.insertVoter(name, phone, voterId)
            Toast.makeText(context, "Voter Registered Successfully: $id", Toast.LENGTH_SHORT).show()

            // Clear fields after registration
            name = ""
            phone = ""
            voterId = ""
            fingerprintVerified = false
        } else {
            Toast.makeText(context, "Please fill in all fields and verify fingerprint", Toast.LENGTH_SHORT).show()
        }
    }


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Name field
            CustomTextField(
                value = name,
                onValueChange = { name = it },
                label = "Name",
                modifier = Modifier.padding(8.dp)
            )
            // Phone field (10 digits)
            CustomTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Phone Number (10 digits)",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.padding(8.dp)
            )
            // Voter ID / Aadhar Number
            CustomTextField(
                value = voterId,
                onValueChange = { voterId = it },
                label = "Voter ID / Aadhar Number",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.padding(8.dp)
            )

            // Fingerprint verification button (circular button)
            Button(
                onClick = verifyFingerprint,
                shape = CircleShape,
                modifier = Modifier
                    .size(100.dp)
                    .padding(8.dp)
                    .background(Color.Gray),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Fingerprint", color = Color.White)
            }

            // If fingerprint is verified, show success text
            if (fingerprintVerified) {
                Text("Fingerprint Verified Successfully", color = Color.Green)
            }

            // Register button
            Button(
                onClick = registerVoter,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Register")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterVoterScreenPreview() {
    RegisterVoterScreen()
}
