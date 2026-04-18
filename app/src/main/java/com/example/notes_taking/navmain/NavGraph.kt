package com.example.notes_taking.navmain

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.notes_taking.RoomDatabase.NoteDatabase
import com.example.notes_taking.Screens.presentations.CreateNote.CreateNoteScreen
import com.example.notes_taking.Screens.presentations.CreateNote.NoteViewModel
import com.example.notes_taking.Screens.presentations.CreateNote.NoteViewModelFactory
import com.example.notes_taking.Screens.presentations.Home.HomeScreen
import com.example.notes_taking.Screens.presentations.Onboarding.OnboardingScreen
import com.example.notes_taking.Screens.presentations.Splash.SplashScreen
import androidx.compose.ui.platform.LocalLayoutDirection
import com.example.notes_taking.Screens.presentations.Settings.SettingsScreen

@SuppressLint("LocalContextConfigurationRead")
@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val dao = NoteDatabase.getDatabase(context).noteDao()
    val viewModel: NoteViewModel = viewModel(factory = NoteViewModelFactory(dao))
    NavHost(
        navController = navController,
        startDestination = Route.Splash.route
    ) {
        // 1. Splash Screen
        composable(route = Route.Splash.route) {
            SplashScreen(onSplashFinished = {
                navController.navigate(Route.Onboarding.route) {
                    popUpTo(Route.Splash.route) { inclusive = true }
                }
            })
        }

        // 2. Onboarding Screen
        composable(route = Route.Onboarding.route) {
            val currentLayoutDirection = LocalLayoutDirection.current
            val isRtl = currentLayoutDirection == LayoutDirection.Rtl

            OnboardingScreen(
                onFinish = {
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.Onboarding.route) { inclusive = true }
                    }
                },
                isRtl = isRtl
            )
        }

        // 3. Home Screen
        composable(route = Route.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onAddNote = { navController.navigate(Route.CreateNote.route) },
                onEditNote = { noteId ->
                    navController.navigate(Route.EditNote.createRoute(noteId))
                },
                onNavigateToSettings = { // ← أضف
                    navController.navigate(Route.Settings.route)
                }
            )
        }

        // 4. Create/Edit Note Screen
        composable(
            route = Route.EditNote.route,
            arguments = listOf(navArgument("noteId") {
                type = NavType.IntType
                defaultValue = 0
            })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0

            CreateNoteScreen(
                noteId = noteId,
                onBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        // 5. Settings Screen
        composable(route = Route.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}