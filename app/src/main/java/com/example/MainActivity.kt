package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.SalmonDatabase
import com.example.data.repository.SalmonRepository
import com.example.ui.SalmonSamplingApp
import com.example.ui.SalmonViewModel
import com.example.ui.SalmonViewModelFactory
import com.example.ui.theme.SalmonAppTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SalmonViewModel by viewModels {
        val database = SalmonDatabase.getDatabase(applicationContext)
        val repository = SalmonRepository(database.salmonSampleDao(), database.cageTargetDao())
        SalmonViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            SalmonAppTheme(darkTheme = isDarkMode) {
                SalmonSamplingApp(viewModel = viewModel)
            }
        }
    }
}

