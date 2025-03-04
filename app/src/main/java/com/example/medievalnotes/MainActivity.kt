package com.example.medievalnotes

import android.os.Bundle
import android.util.Log
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

    // --- 1) onPause (переворот экрана или переход в фон) ---
    override fun onPause() {
        super.onPause()
        // Если это просто смена конфигурации (переворот экрана), то выходим
        if (isChangingConfigurations) return

        Log.d("MainActivity", "onPause - not changing config => app is going to background")

        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (currentRoute == "page3" && mainViewModel.isPlaying) {
            Log.d("MainActivity", "onPause - page 3")
            mainViewModel.togglePauseResume() // ставим музыку/ноты на паузу
        }
        if (currentRoute == "page2" && mainViewModel.isPlaying) {
            Log.d("MainActivity", "onPause - page 2")
            mainViewModel.togglePlayOnPage2PauseOnly() // тоже пауза
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
