package com.example.medievalnotes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.util.Locale

@Composable
fun NotesPage(
    navController: NavHostController,
    vm: MainViewModel
) {
    Box(
        modifier=Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val config = LocalConfiguration.current
        val screenWidthDp = config.screenWidthDp
        val screenHeightDp = config.screenHeightDp

        val screenWidthPx = with(LocalDensity.current){ screenWidthDp.dp.toPx() }
        val screenHeightPx = with(LocalDensity.current){ screenHeightDp.dp.toPx() }

        val notesToDraw = vm.getNotesToDraw(screenWidthPx, screenHeightPx)

        Canvas(modifier=Modifier.fillMaxSize()){
            for(note in notesToDraw){
                drawRect(
                    color=Color.White,
                    topLeft= androidx.compose.ui.geometry.Offset(note.x, note.top),
                    size= androidx.compose.ui.geometry.Size(note.width, note.height)
                )
            }
        }

        val timeText = String.format(Locale.US, "%.1f s", vm.currentTime)
        Text(
            text= timeText,
            color=Color.White,
            modifier=Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        )

        // Кнопка Stop
        Column(
            modifier=Modifier
                .align(Alignment.TopEnd)
                .padding(top=32.dp, end=8.dp)
        ){
            Button(
                onClick={
                    vm.stopPlaying()
                    navController.popBackStack("page1", inclusive=false)
                },
                colors=ButtonDefaults.buttonColors(
                    containerColor=Color.White,
                    contentColor=Color.Black
                )
            ){
                Text("Stop")
            }
        }

        val songTitle = vm.currentSongInfo?.title ?: "No song loaded"
        Text(
            text= songTitle,
            color=Color.White,
            modifier=Modifier
                .align(Alignment.TopCenter)
                .padding(8.dp)
        )
    }
}
