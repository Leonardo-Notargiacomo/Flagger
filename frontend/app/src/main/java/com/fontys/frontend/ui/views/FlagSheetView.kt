package com.fontys.frontend.ui.views

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fontys.frontend.ui.viewmodels.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlagSheet(
    viewmodel: ReviewViewModel = viewModel()
) {
    val selected = remember { mutableIntStateOf(0) }


    ModalBottomSheet(
        onDismissRequest = { TODO() }
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
    }
}
