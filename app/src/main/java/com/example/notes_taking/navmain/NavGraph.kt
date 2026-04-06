package com.example.notes_taking.navmain

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
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
import com.example.notes_taking.Screens.presentations.Splash.SplashScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController, startDestination = Route.Splash.route
    ) {
        // 1. Splash Screen
        composable(route = Route.Splash.route) {
            SplashScreen(onSplashFinished = {
                navController.navigate(Route.Home.route) {
                    popUpTo(Route.Splash.route) { inclusive = true }
                }
            })
        }

        // 2. Home Screen
        composable(route = Route.Home.route) {
            val context = LocalContext.current
            val database = NoteDatabase.getDatabase(context)
            val homeViewModel: NoteViewModel = viewModel(
                factory = NoteViewModelFactory(database.noteDao())
            )

            HomeScreen(viewModel = homeViewModel, onAddNote = {
                navController.navigate(Route.CreateNote.passId(0))
            }, onEditNote = { noteId ->
                navController.navigate(Route.CreateNote.passId(noteId))
            })
        }

        // 3. Create/Edit Note Screen
        composable(
            route = Route.CreateNote.route, arguments = listOf(
            navArgument("noteId") { type = NavType.IntType })) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0

            val context = LocalContext.current
            val database = NoteDatabase.getDatabase(context)
            val createNoteViewModel: NoteViewModel = viewModel(
                factory = NoteViewModelFactory(database.noteDao())
            )

            CreateNoteScreen(
                noteId = noteId,
                onBack = { navController.popBackStack() },
                viewModel = createNoteViewModel
            )
        }
    }
    
}