package com.plcoding.biometricauth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.plcoding.biometricauth.ui.theme.BiometricAuthTheme
import kotlinx.coroutines.flow.collect

class SearchVoterActivity : AppCompatActivity() {
    private val promptManager by lazy {
        BiometricPromptManager(this) // Use 'this' to get AppCompatActivity context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BiometricAuthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass promptManager here
                    SearchVoterScreen(
                        biometricPromptManager = promptManager,
                        viewModel = TODO()
                    )
                }
            }
        }
    }
}

@Composable
fun SearchVoterScreen(viewModel: SharedViewModel,biometricPromptManager: BiometricPromptManager) {
    var searchQuery by remember { mutableStateOf("") }
    var voterDetails by remember { mutableStateOf<Voter?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var verificationMessage by remember { mutableStateOf<String?>(null) } // State to hold verification message
    val context = LocalContext.current
    val dbHelper = DatabaseHelper(context)

    val count by viewModel.count

    // Get the keyboard controller
    val keyboardController = LocalSoftwareKeyboardController.current

    // Function to search for voter details
    /*val searchVoter = {
        if (searchQuery.isNotEmpty()) {
            val voters = dbHelper.getAllVoters()
            val voter = voters.find { it.voterId == searchQuery }
            if (voter != null) {
                voterDetails = voter
                errorMessage = null // Clear error message if voter is found
            } else {
                voterDetails = null // Clear voter details if not found
                errorMessage = "Voter not found with the entered details" // Set error message
            }
        } else {
            Toast.makeText(context, "Please enter a Voter ID or Aadhar number", Toast.LENGTH_SHORT).show()
        }
        // Hide the keyboard after search
        keyboardController?.hide()
    }*/

    val searchVoter = {
        if (searchQuery.isNotEmpty()) {
            // Check if the voter has already been verified
            if (viewModel.isVoterAlreadyVerified(searchQuery)) {
                errorMessage = "ALREADY VOTED"
            } else {
                val voters = dbHelper.getAllVoters()
                val voter = voters.find { it.voterId == searchQuery }
                if (voter != null) {
                    voterDetails = voter
                    errorMessage = null
                } else {
                    voterDetails = null
                    errorMessage = "Voter not found with the entered details"
                }
            }
        } else {
            Toast.makeText(context, "Please enter a Voter ID or Aadhar number", Toast.LENGTH_SHORT).show()
        }
        keyboardController?.hide()
    }

    // Function to initiate fingerprint authentication
    val verifyFingerprint = {
        biometricPromptManager.showBiometricPrompt(
            title = "Fingerprint Authentication",
            description = "Please authenticate using your fingerprint"
        )
    }

    // Collect biometric result as a state (using StateFlow or Channel)
    val biometricResult by biometricPromptManager.promptResults.collectAsState(initial = null)

    // Handling biometric result
    LaunchedEffect(biometricResult) {
        biometricResult?.let { result ->
            when (result) {
                is BiometricPromptManager.BiometricResult.AuthenticationSuccess -> {
                    viewModel.incrementCount()
                    viewModel.addVoterId(searchQuery)
                    verificationMessage = "Voter verified Successfully, Eligible for voting" // Success message
                }
                is BiometricPromptManager.BiometricResult.AuthenticationFailed -> {
                    verificationMessage = "Fingerprint authentication failed. Please try again." // Error message
                }
                is BiometricPromptManager.BiometricResult.HardwareUnavailable -> {
                    verificationMessage = "Biometric hardware unavailable."
                }
                is BiometricPromptManager.BiometricResult.FeatureUnavailable -> {
                    verificationMessage = "Biometric feature is not available."
                }
                is BiometricPromptManager.BiometricResult.AuthenticationNotSet -> {
                    verificationMessage = "Authentication method not set."
                }
                is BiometricPromptManager.BiometricResult.AuthenticationError -> {
                    verificationMessage = "Authentication error: ${result.error}"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Set background based on system theme
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Basic TextField without highlighting
        BasicTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color.LightGray, shape = MaterialTheme.shapes.small) // Light gray background for text field
                .padding(16.dp),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Simple Button to trigger search
        Button(
            onClick = {
                searchVoter() // Call the searchVoter function inside the lambda
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Search")
        }

        // Display the error message if voter is not found
        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red, // Display the error message in red
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        // Display the voter details if found
        voterDetails?.let {
            Text("Voter Details:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Text("Name: ${it.name}", color = MaterialTheme.colorScheme.onBackground)
            Text("Phone: ${it.phone}", color = MaterialTheme.colorScheme.onBackground)
            Text("Voter ID: ${it.voterId}", color = MaterialTheme.colorScheme.onBackground)

            // Show the "Verify" button after finding the voter
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    verifyFingerprint() // Trigger fingerprint authentication
                }
            ) {
                Text("Verify")
            }
        }

        // Display the verification result message
        verificationMessage?.let {
            Text(
                text = it,
                color = if (it.contains("Success")) Color.Green else Color.Red, // Display in green or red
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
    Text(
        text = "Verification Count: $count", // Display the count
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(top = 16.dp),
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Preview(showBackground = true)
@Composable
fun SearchVoterScreenPreview() {
    SearchVoterScreen(
        biometricPromptManager = BiometricPromptManager(LocalContext.current as AppCompatActivity),
        viewModel = TODO()
    )
}
