package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.AppDatabase
import com.example.data.GymRepository
import com.example.ui.GymApp
import com.example.ui.GymViewModel
import com.example.ui.GymViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize SQLite Room DB
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = GymRepository(database.gymDao())

        // 2. Instantiate GymViewModel with clean constructor injection via a Factory
        val factory = GymViewModelFactory(application, repository)
        val viewModel: GymViewModel by viewModels { factory }

        setContent {
            MyApplicationTheme {
                // 3. Render master views
                GymApp(viewModel = viewModel)
            }
        }
    }
}
