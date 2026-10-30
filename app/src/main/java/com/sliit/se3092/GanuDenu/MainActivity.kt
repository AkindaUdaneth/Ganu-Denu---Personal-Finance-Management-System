package com.sliit.se3092.GanuDenu

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sliit.se3092.GanuDenu.ui.auth.AuthSession
import com.sliit.se3092.GanuDenu.ui.auth.AuthScreen
import com.sliit.se3092.GanuDenu.ui.auth.AuthViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.sliit.se3092.GanuDenu.ui.navigation.AppNavHost
import com.sliit.se3092.GanuDenu.ui.theme.GanuDenuTheme

class MainActivity : FragmentActivity() {
    private val authViewModel: AuthViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GanuDenuTheme {
                val authState by authViewModel.uiState.collectAsStateWithLifecycle()
                if (authState.session == AuthSession.SIGNED_IN) {
                    key(authState.profile?.uid) {
                        val navController = rememberNavController()
                        AppNavHost(navController = navController, onLogout = authViewModel::signOut)
                    }
                } else {
                    AuthScreen(viewModel = authViewModel)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) authViewModel.lockIfEnabled()
    }
}
