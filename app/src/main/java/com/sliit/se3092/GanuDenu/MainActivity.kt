package com.sliit.se3092.GanuDenu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.sliit.se3092.GanuDenu.ui.navigation.AppNavHost
import com.sliit.se3092.GanuDenu.ui.theme.GanuDenuTheme

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

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun MainAppPreview() {
    GanuDenuTheme {
        val navController = rememberNavController()
        AppNavHost(navController = navController)
    }
}