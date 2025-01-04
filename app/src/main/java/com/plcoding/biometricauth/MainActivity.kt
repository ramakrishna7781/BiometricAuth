package com.plcoding.biometricauth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.plcoding.biometricauth.ui.theme.BiometricAuthTheme

class MainActivity : AppCompatActivity() {  // Change to AppCompatActivity
    private val promptManager by lazy {
        BiometricPromptManager(this) // Now it's valid to use this as AppCompatActivity
    }
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BiometricAuthTheme {
                val navController = rememberNavController()
                AppNavigation(navController, promptManager, sharedViewModel) // Pass the promptManager here
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController, promptManager: BiometricPromptManager, sharedViewModel: SharedViewModel ) {
    NavHost(navController = navController, startDestination = "MainMenuScreen") {
        composable("MainMenuScreen") {
            MainMenuScreen { destination ->
                navController.navigate(destination)
            }
        }
        composable("RegisterVoterScreen") {
            RegisterVoterScreen()
        }
        composable("SearchVoterScreen") {
            SearchVoterScreen(viewModel = sharedViewModel, biometricPromptManager = promptManager) // Pass promptManager
        }
        composable("DemographicDataScreen") {
            DemographicDataScreen(viewModel = sharedViewModel)
        }
    }
}
