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
import com.example.notes_taking.Screens.presentations.Tasks.TasksScreen

@SuppressLint("LocalContextConfigurationRead")
@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val dao = NoteDatabase.getDatabase(context).noteDao()

    // تعريف الـ ViewModel هنا ليكون متاحاً لكل الشاشات في الـ NavGraph
    val viewModel: NoteViewModel = viewModel(factory = NoteViewModelFactory(dao))

    NavHost(
        navController = navController,
        startDestination = Route.Splash.route
    ) {
        // 1. شاشة البداية (Splash)
        composable(route = Route.Splash.route) {
            SplashScreen(onSplashFinished = {
                navController.navigate(Route.Onboarding.route) {
                    popUpTo(Route.Splash.route) { inclusive = true }
                }
            })
        }

        // 2. شاشة التعريف (Onboarding)
        composable(route = Route.Onboarding.route) {
            val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.Onboarding.route) { inclusive = true }
                    }
                },
                isRtl = isRtl
            )
        }

        // 3. الشاشة الرئيسية (Home)
        composable(route = Route.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                navController = navController, // نمرر الـ navController ليتم استخدامه في BottomNavBar
                onAddNote = {
                    // نمرر 0 لإنشاء ملاحظة جديدة
                    navController.navigate(Route.EditNote.createRoute(0))
                },
                onEditNote = { noteId ->
                    navController.navigate(Route.EditNote.createRoute(noteId))
                },
                onNavigateToSettings = {
                    navController.navigate(Route.Settings.route)
                },
                onNavigateToTasks = {
                    navController.navigate(Route.Tasks.route)
                }
            )
        }

        // 4. شاشة إنشاء/تعديل الملاحظة
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

        // 5. شاشة الإعدادات
        composable(route = Route.Settings.route) {
            SettingsScreen(navController = navController)
        }

        // 6. شاشة المهام
        composable(route = Route.Tasks.route) {
            TasksScreen(navController = navController)
        }
    }
}