package com.baptistaz.taskwave.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.baptistaz.taskwave.ui.auth.AuthViewModel
import com.baptistaz.taskwave.ui.auth.LoginScreen
import com.baptistaz.taskwave.ui.auth.SignupScreen
import com.baptistaz.taskwave.ui.home.HomeScreen

object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
}

@Composable
fun AppNavGraph(navController: NavHostController, authViewModel: AuthViewModel) {
    NavHost(navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { navController.navigate(Routes.HOME) },
                onNavigateToSignup = { navController.navigate(Routes.SIGNUP) }
            )
        }
        composable(Routes.SIGNUP) {
            SignupScreen(
                authViewModel = authViewModel,
                navController = navController,
                onSignupSuccess = { navController.navigate(Routes.LOGIN) }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(navController = navController, authViewModel = authViewModel)
        }
    }
}
