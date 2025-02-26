package com.example.medievalnotes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext


@Composable
fun SongSelectionPage(
    navController: NavHostController,
    vm: MainViewModel
) {
    val listState = rememberLazyListState()

    val bgColor = if (vm.isDarkTheme) Color.Black else Color.White
    val textColor = if (vm.isDarkTheme) Color.White else Color.Black
    val btnContainer = if (vm.isDarkTheme) Color.DarkGray else Color.LightGray
    val btnContent = if (vm.isDarkTheme) Color.White else Color.Black

    // scroll line colors
    val trackColor = if (vm.isDarkTheme) Color.DarkGray else Color.LightGray
    val handleColor = if (vm.isDarkTheme) Color.LightGray else Color.DarkGray

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // -- список
        LazyColumn(
            modifier= Modifier
                .fillMaxSize()
                .padding(end=20.dp),
            state= listState
        ) {
            item {
                Text(
                    text="Songs:",
                    color=textColor,
                    modifier=Modifier.padding(16.dp)
                )
            }
            items(vm.songList.size){ index ->
                val songItem = vm.songList[index]
                val isSelected = (index == vm.selectedSongIndex)
                val bg = if(isSelected) btnContainer else Color.Transparent

                val context = LocalContext.current

                Box(
                    modifier= Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .clickable {
                            vm.selectSong(context, index)
                        }
                        .padding(8.dp)
                ) {
                    Text(songItem.title, color=textColor)
                }
            }
        }

        // -- Кнопки в правом нижнем углу
        Column(
            modifier= Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement= Arrangement.spacedBy(16.dp)  // Расстояние между кнопками
        ) {
            // Кнопка Play (на стр.2)
            Button(
                onClick= {
                    vm.togglePlayOnPage2(navController.context)
                },
                colors=ButtonDefaults.buttonColors(
                    containerColor=btnContainer,
                    contentColor=btnContent
                )
            ) {
                Text("Play")
                // при повторном нажатии,
                // если песня playing => pause,
                // если paused => resume,
                // если другая песня => start c начала
            }

            // Кнопка Back (ниже)
            Button(
                onClick = {
                    // Останавливаем музыку, затем уходим на стр.1
                    vm.stopMusic()
                    navController.popBackStack("page1", false)
                },
                colors=ButtonDefaults.buttonColors(
                    containerColor=btnContainer,
                    contentColor=btnContent
                )
            ) {
                Text("Back")
            }
        }

        // -- scroll line
        val layoutInfo = listState.layoutInfo
        val visibleItems = layoutInfo.visibleItemsInfo
        val total = vm.songList.size + 1

        val avgSize = if(visibleItems.isNotEmpty()){
            visibleItems.sumOf { it.size.toDouble() }.toFloat()/visibleItems.size
        } else 1f

        val vpHeightPx = layoutInfo.viewportSize.height.toFloat().coerceAtLeast(1f)
        val visibleCount = vpHeightPx/avgSize

        val firstItem = visibleItems.firstOrNull()
        val scrolledPx = if(firstItem!=null){
            val offsetPx= if(firstItem.offset<0) -firstItem.offset.toFloat()
            else firstItem.offset.toFloat()
            (firstItem.index*avgSize)+offsetPx
        } else 0f

        val maxScrollPx = (total - visibleCount)*avgSize
        val fraction = if(maxScrollPx>0f) (scrolledPx/maxScrollPx).coerceIn(0f,1f) else 0f

        val fractionVisible= if(total>0) (visibleCount/total.toFloat()).coerceIn(0f,1f)
        else 1f

        val trackHeightPx=300f
        val handleHeightPx=(trackHeightPx*fractionVisible).coerceAtLeast(20f)
        val handleTopPx= fraction*(trackHeightPx-handleHeightPx)

        Box(
            modifier=Modifier
                .align(Alignment.CenterEnd)
                .width(8.dp)
                .height(300.dp)
                .background(trackColor)
        ){
            Box(
                modifier=Modifier
                    .offset(y=handleTopPx.dp)
                    .fillMaxWidth()
                    .height(handleHeightPx.dp)
                    .background(handleColor)
            )
        }
    }
}
