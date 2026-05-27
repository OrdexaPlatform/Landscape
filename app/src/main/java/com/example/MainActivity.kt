package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.data.repository.InvoiceRepository
import com.example.ui.screens.LandscapeAppUI
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.InvoiceViewModel
import com.example.ui.viewmodel.InvoiceViewModelFactory

class MainActivity : ComponentActivity() {
    
    // Lazy initialize database and repository
    private val database by lazy { 
        AppDatabase.getDatabase(applicationContext, lifecycleScope) 
    }
    private val repository by lazy { 
        InvoiceRepository(
            database.clientDao(),
            database.projectDao(),
            database.financialAccountDao()
        ) 
    }

    // Initialize state-driven ViewModel
    private val viewModel: InvoiceViewModel by viewModels {
        InvoiceViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    LandscapeAppUI(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

