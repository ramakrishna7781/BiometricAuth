package com.plcoding.biometricauth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DemographicDataScreen(viewModel: SharedViewModel) {
    val count by viewModel.count

    // Layout with background color based on system theme
    Column(
        modifier = Modifier
            .fillMaxSize() // Fill the whole screen
            .background(MaterialTheme.colorScheme.background), // Set background based on system theme
        horizontalAlignment = Alignment.CenterHorizontally, // Center horizontally
        verticalArrangement = Arrangement.Center // Center vertically
    ) {
        // Display the count at the center with text color based on the theme
        Text(
            text = "Demographic Data",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground // Dynamic text color based on theme
        )
        Spacer(modifier = Modifier.height(16.dp)) // Space between texts
        Text(
            text = "Total number of voters voted: $count",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground // Dynamic text color based on theme
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DemographicDataScreenPreview() {
    DemographicDataScreen(viewModel = SharedViewModel()) // Preview with a default ViewModel
}
