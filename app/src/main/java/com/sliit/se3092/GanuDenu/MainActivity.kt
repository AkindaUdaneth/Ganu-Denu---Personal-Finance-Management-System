package com.sliit.se3092.GanuDenu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.sliit.se3092.GanuDenu.ui.navigation.AppNavHost
import com.sliit.se3092.GanuDenu.ui.theme.GanuDenuTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GanuDenuTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }
}