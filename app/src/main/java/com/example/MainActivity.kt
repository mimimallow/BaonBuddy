package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.BaonBuddyApp
import com.example.ui.BaonViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BaonViewModel by viewModels {
        BaonViewModel.factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BaonBuddyApp(viewModel = viewModel)
        }
    }
}
