package com.example.medievalnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    lateinit var navController: NavHostController

    override fun onPause() {
        super.onPause()

        // Проверим, не ушёл ли пользователь со страницы 3

        val currentRoute = navController.currentBackStackEntry?.destination?.route

        // Если сейчас мы на "page3"
        if (currentRoute == "page3") {
            // Если isPlaying = true, значит музыка и ноты идут.
            // Нужно их поставить на паузу.
            if (mainViewModel.isPlaying) {
                // Используем вашу функцию togglePauseResume() (или конкретную "pause" логику)
                mainViewModel.togglePauseResume()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel.loadSongList(this)

        // Загрузить конфиг из JSON
        mainViewModel.loadConfigFromFile(this)   // <-- (НОВОЕ)

        setContent {
            navController = rememberNavController()

            // Запустим UDP-сервер (один раз), передав колбеки
            //  - onNavigateToPage3 => navController.navigate("page3")
            //  - onNavigateToPage1 => popBackStack("page1", inclusive=false)
            // Можно обернуть это в remember {}
            LaunchedEffect(Unit) {
                mainViewModel.startUdpServer(
                    context = this@MainActivity,
                    onNavigateToPage3 = {
                        navController.navigate("page3")
                    },
                    onNavigateToPage1 = {
                        // Возвращение на стр.1
                        navController.popBackStack("page1", inclusive=false)
                    }
                )
            }

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
