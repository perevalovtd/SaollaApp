package com.example.medievalnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel.loadSongList(this)

        setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "page1"
            ) {
                composable("page1") {
                    MainPage(navController, mainViewModel)
                }
                composable("page2") {
                    SongSelectionPage(navController, mainViewModel)
                }
                composable("page3") {
                    NotesPage(navController, mainViewModel)
                }
            }
        }
    }
}
