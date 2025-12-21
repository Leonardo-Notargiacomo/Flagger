package com.fontys.frontend.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fontys.frontend.ui.viewmodels.FlagSheetViewmodel
import com.fontys.frontend.ui.viewmodels.MapsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlagSheet(
    navController: NavController,
    viewmodel: MapsViewModel = viewModel(),
) {
    val selected = remember { mutableIntStateOf(0) }
    val sheetState = rememberModalBottomSheetState()



    ModalBottomSheet(
        onDismissRequest = { viewmodel.showBottomSheet() },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
        ) {
            SecondaryTabRow(selectedTabIndex = selected.intValue) {
                Tab(
                    selected = selected.intValue == 0,
                    text = { Text("Overview") },
                    onClick = {
                        selected.intValue = 0

                    }
                )
                Tab(
                    selected = selected.intValue == 1,
                    text = { Text("Reviews") },
                    onClick = {
                        selected.intValue = 1
                    }
                )
            }

            if (selected.intValue == 0) {
                OverviewTab()
            }
            if (selected.intValue == 1) {
                ReviewView(navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewTab(
    viewModel: FlagSheetViewmodel = viewModel()
) {


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        HorizontalUncontainedCarousel(
            state = rememberCarouselState() {0},
            itemWidth = 186.dp
        ) { }
    }
}