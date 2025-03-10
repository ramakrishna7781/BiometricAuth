package com.plcoding.biometricauth

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Composable
fun RegisterVoterScreen() {

    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var voterId by remember { mutableStateOf("") }
    var fingerprintVerified by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }
    var isHandicappedSelected by remember { mutableStateOf(false) }

    val dbHelper = DatabaseHelper(context)

    fun showBiometricPrompt() {
        val executor: Executor = Executors.newSingleThreadExecutor()
        val biometricPrompt = BiometricPrompt(
            (context as androidx.fragment.app.FragmentActivity),
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    fingerprintVerified = true
                    keyboardController?.hide()
                    Toast.makeText(context, "Fingerprint Registered successfully", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(context, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Fingerprint Authentication")
            .setSubtitle("Place your finger on the sensor")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    val registerVoter = {
        if (name.isNotEmpty() && phone.length == 10 && voterId.isNotEmpty() && fingerprintVerified) {
            val id = dbHelper.insertVoter(name, phone, voterId)
            Toast.makeText(context, "Voter Registered Successfully: $id", Toast.LENGTH_SHORT).show()
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
            CustomTextField(value = name, onValueChange = { name = it }, label = "Name", modifier = Modifier.padding(8.dp))
            CustomTextField(value = phone, onValueChange = { phone = it }, label = "Phone Number (10 digits)", keyboardType = KeyboardType.Number, modifier = Modifier.padding(8.dp))
            CustomTextField(value = voterId, onValueChange = { voterId = it }, label = "Voter ID / Aadhar Number", keyboardType = KeyboardType.Number, modifier = Modifier.padding(8.dp))

            Button(
                onClick = { showBiometricPrompt() },
                shape = CircleShape,
                modifier = Modifier
                    .size(100.dp)
                    .padding(8.dp)
                    .background(Color.Gray),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Fingerprint", color = Color.White)
            }

            Button(
                onClick = { isHandicappedSelected = !isHandicappedSelected },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Physically Handicapped? If yes, click here")
            }

            if (isHandicappedSelected) {
                CustomTextField(value = pin, onValueChange = { pin = it }, label = "Enter 6-digit Pin", keyboardType = KeyboardType.Number, modifier = Modifier.padding(8.dp))
            }

            if (fingerprintVerified) {
                Text("Fingerprint Verified Successfully", color = Color.Green)
            }

            Button(
                onClick = registerVoter,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Register")
            }
        }
    }
}
