@file:Suppress(
    "INFERRED_TYPE_VARIABLE_INTO_EMPTY_INTERSECTION_WARNING", "TYPE_INTERSECTION_AS_REIFIED_WARNING"
)

package com.example.notes_taking.Navmain


import TasksViewModel
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
import com.example.notes_taking.Screens.presentations.About.AboutScreen
import com.example.notes_taking.Screens.presentations.Editor.NoteEditorScreen
import com.example.notes_taking.Screens.presentations.Editor.NoteViewModel
import com.example.notes_taking.Screens.presentations.Home.HomeScreen
import com.example.notes_taking.Screens.presentations.Home.HomeViewModel
import com.example.notes_taking.Screens.presentations.Notes.NotesScreen
import com.example.notes_taking.Screens.presentations.Notes.NotesViewModel
import com.example.notes_taking.Screens.presentations.Onboarding.OnboardingScreen
import com.example.notes_taking.Screens.presentations.Onboarding.OnboardingViewModel
import com.example.notes_taking.Screens.presentations.Privacy.PrivacyPolicyScreen
import com.example.notes_taking.Screens.presentations.Privacy.PrivacyScreen
import com.example.notes_taking.Screens.presentations.Settings.SettingsScreen
import com.example.notes_taking.Screens.presentations.Settings.SettingsViewModel
import com.example.notes_taking.Screens.presentations.Splash.SplashScreen
import com.example.notes_taking.Screens.presentations.Summary.SummaryScreen
import com.example.notes_taking.Screens.presentations.Summary.SummaryViewModel
import com.example.notes_taking.Screens.presentations.Tasks.TasksScreen

@SuppressLint("NewApi")
@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun NavGraph(
    navController: NavHostController, settingsViewModel: SettingsViewModel
) {
    val context = LocalContext.current

    val dao = remember { NoteDatabase.getDatabase(context).noteDao() }
    val taskDao = remember { NoteDatabase.getDatabase(context).taskDao() }
    val repository = remember { NoteRepositoryImpl(dao, taskDao) }

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
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.IntType
                    defaultValue = 0
                },
                navArgument("openAudio") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
            val openAudio = backStackEntry.arguments?.getBoolean("openAudio") ?: false
            val editorViewModel: NoteViewModel = viewModel(factory = factory)

            NoteEditorScreen(
                noteId = noteId,
                openAudio = openAudio,
                viewModel = editorViewModel,
                onClose = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
            )
        }

        // ======= Notes =======
        composable(route = Route.Notes.route) {
            val notesViewModel: NotesViewModel = viewModel(factory = factory)
            NotesScreen(
                viewModel = notesViewModel, navController = navController
            )
        }

        composable(route = Route.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel, navController = navController
            )
        }
        composable(route = Route.Tasks.route) {
            val tasksViewModel: TasksViewModel = viewModel(factory = factory)
            TasksScreen(viewModel = tasksViewModel, navController = navController)
        }

        composable(route = Route.AboutApp.route) {
            AboutScreen(
                navController = navController
            )
        }
        // ======= Summary =======
        composable(route = Route.Summary.route) {
            val summaryViewModel: SummaryViewModel = viewModel(factory = factory)

            SummaryScreen(
                viewModel = summaryViewModel, onBack = { navController.popBackStack() })
        }
        composable(route = Route.Privacy.route) {
            PrivacyPolicyScreen(navController = navController)
        }
        composable(route = Route.PrivacyCenter.route){
            PrivacyScreen(navController = navController)
        }
    }
}