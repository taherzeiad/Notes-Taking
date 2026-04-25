package com.example.notes_taking.Navmain

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.notes_taking.RoomDatabase.NoteDatabase
import com.example.notes_taking.Screens.presentations.CreateNote.NoteViewModel
import com.example.notes_taking.Screens.presentations.CreateNote.NoteViewModelFactory
import com.example.notes_taking.Screens.presentations.Editor.NoteEditorScreen
import com.example.notes_taking.Screens.presentations.Home.HomeScreen
import com.example.notes_taking.Screens.presentations.Notes.NotesScreen
import com.example.notes_taking.Screens.presentations.Onboarding.OnboardingScreen
import com.example.notes_taking.Screens.presentations.Settings.SettingsScreen
import com.example.notes_taking.Screens.presentations.Splash.SplashScreen
import com.example.notes_taking.Screens.presentations.Tasks.TasksScreen

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val dao = remember { NoteDatabase.getDatabase(context).noteDao() }
    val viewModel: NoteViewModel = viewModel(factory = NoteViewModelFactory(dao))

    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val lang = prefs.getString("language", "en") ?: "en"
    val isRtl = lang == "ar"

    NavHost(
        navController = navController,
        startDestination = Route.Splash.route
    ) {

        // ======= Splash =======
        composable(route = Route.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Route.Onboarding.route) {
                        popUpTo(Route.Splash.route) { inclusive = true }
                        launchSingleTop = true

                    }
                }
            )
        }

        // ======= Onboarding =======
        composable(route = Route.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.Onboarding.route) { inclusive = true }
                    }
                },
                isRtl = isRtl
            )
        }

        // ======= Home =======
        composable(route = Route.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                navController = navController,
                onAddNote = {
                    navController.navigate(Route.NoteEditor.createRoute(0))
                },
                onEditNote = { noteId ->
                    navController.navigate(Route.NoteEditor.createRoute(noteId))
                },
                onNavigateToSettings = {
                    navController.navigate(Route.Settings.route)
                },
                onNavigateToTasks = {
                    navController.navigate(Route.Tasks.route)
                },
                onNavigateToNotes = {
                    navController.navigate(Route.Notes.route)
                }
            )
        }

        // ======= Note Editor (إنشاء + تعديل) =======
        composable(
            route = Route.NoteEditor.route,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
            NoteEditorScreen(
                noteId = noteId,
                viewModel = viewModel,
                onClose = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
            )
        }

        // ======= Settings =======
        composable(route = Route.Settings.route) {
            SettingsScreen(navController = navController)
        }

        // ======= Tasks =======
        composable(route = Route.Tasks.route) {
            TasksScreen(navController = navController)
        }

        // ======= Notes =======
        composable(route = Route.Notes.route) {
            NotesScreen(
                navController = navController,
                onAddNote = {
                    navController.navigate(Route.NoteEditor.createRoute(0))
                },
                onEditNote = { noteId: Int ->
                    navController.navigate(Route.NoteEditor.createRoute(noteId))
                }
            )
        }
    }
}