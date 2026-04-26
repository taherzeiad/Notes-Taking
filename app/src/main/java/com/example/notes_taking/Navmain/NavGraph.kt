package com.example.notes_taking.Navmain

import HomeViewModel
import OnboardingViewModel
import android.annotation.SuppressLint
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
import com.example.notes_taking.Repository.GenericViewModelFactory
import com.example.notes_taking.Repository.NoteRepositoryImpl
import com.example.notes_taking.RoomDatabase.NoteDatabase
import com.example.notes_taking.Screens.presentations.Editor.NoteEditorScreen
import com.example.notes_taking.Screens.presentations.Editor.NoteViewModel
import com.example.notes_taking.Screens.presentations.Home.HomeScreen
import com.example.notes_taking.Screens.presentations.Notes.NotesScreen
import com.example.notes_taking.Screens.presentations.Notes.NotesViewModel
import com.example.notes_taking.Screens.presentations.Onboarding.OnboardingScreen
import com.example.notes_taking.Screens.presentations.Settings.SettingsScreen
import com.example.notes_taking.Screens.presentations.Settings.SettingsViewModel
import com.example.notes_taking.Screens.presentations.Splash.SplashScreen
import com.example.notes_taking.Screens.presentations.Tasks.TasksScreen
import com.example.notes_taking.Screens.presentations.Tasks.TasksViewModel

@SuppressLint("NewApi")
@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current

    // 1. إعداد الـ Repository والـ DAO مرة واحدة فقط
    val dao = remember { NoteDatabase.getDatabase(context).noteDao() }
    val repository = remember { NoteRepositoryImpl(dao) }
    val factory = remember { GenericViewModelFactory(repository) }

    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val lang = prefs.getString("language", "en") ?: "en"
    val isRtl = lang == "ar"

    NavHost(
        navController = navController, startDestination = Route.Splash.route
    ) {
        // ======= Splash =======
        composable(route = Route.Splash.route) {
            SplashScreen(onSplashFinished = {
                navController.navigate(Route.Onboarding.route) {
                    popUpTo(Route.Splash.route) { inclusive = true }
                }
            })
        }

        // ======= Onboarding =======
        composable(route = Route.Onboarding.route) {
            val onboardingViewModel: OnboardingViewModel = viewModel()
            OnboardingScreen(
                viewModel = onboardingViewModel, onFinish = {
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.Onboarding.route) { inclusive = true }
                    }
                }, isRtl = isRtl
            )
        }

        // ======= Home =======
        composable(route = Route.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(factory = factory)
            HomeScreen(
                viewModel = homeViewModel,
                navController = navController,
                onAddNote = { navController.navigate(Route.NoteEditor.createRoute(0)) },
                onEditNote = { id -> navController.navigate(Route.NoteEditor.createRoute(id)) },
                onNavigateToTasks = { navController.navigate(Route.Tasks.route) },
              )
        }

        // ======= Note Editor =======
        composable(
            route = Route.NoteEditor.route,
            arguments = listOf(navArgument("noteId") { type = NavType.IntType; defaultValue = 0 })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
            val editorViewModel: NoteViewModel = viewModel(factory = factory)

            NoteEditorScreen(
                noteId = noteId,
                viewModel = editorViewModel,
                onClose = { navController.popBackStack() },
                onSave = { navController.popBackStack() })
        }

        // ======= Notes =======
        composable(route = Route.Notes.route) {
            val notesViewModel: NotesViewModel = viewModel(factory = factory)
            NotesScreen(
                viewModel = notesViewModel,
                navController = navController
            )
        }

        composable(route = Route.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel()
            SettingsScreen(
                viewModel = settingsViewModel, navController = navController
            )
        }
        composable(route = Route.Tasks.route) {
            val tasksViewModel: TasksViewModel = viewModel()
            TasksScreen(
                viewModel = tasksViewModel,
                navController = navController
            )
        }
    }
}