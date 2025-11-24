package com.example.floworbit


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.floworbit.ui.theme.FlowOrbitTheme
import androidx.navigation.compose.rememberNavController
import com.example.floworbit.navigation.NavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlowOrbitTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}