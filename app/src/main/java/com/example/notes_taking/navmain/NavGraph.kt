package com.example.notes_taking.navmain

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
        //Splash Screen
        composable(route = Route.Splash.route) {
            SplashScreen(onSplashFinished = {
                navController.navigate(Route.Home.route) {
                    popUpTo(Route.Splash.route) { inclusive = true }
                }
            })
        }
        //Home Screen
        composable(route = Route.Home.route) {
            HomeScreen(onAddNote = {
                navController.navigate(Route.CreateNote.route)
            })
        }
        //Create Note Screen
        composable(route = Route.CreateNote.route) {
            val context = LocalContext.current
            val database = NoteDatabase.getDatabase(context)

            val createNoteViewModel: NoteViewModel = viewModel(
                factory = NoteViewModelFactory(database.noteDao())
            )

            CreateNoteScreen(
                onBack = { navController.popBackStack() }, viewModel = createNoteViewModel
            )
        }
    }
}