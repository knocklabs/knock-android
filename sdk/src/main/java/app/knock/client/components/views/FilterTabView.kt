package app.knock.client.components.views

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.knock.client.components.InAppFeedViewModel
import app.knock.client.components.InAppFeedViewModelFactory
import app.knock.client.components.KnockColor

@Composable
fun FilterTabView(viewModel: InAppFeedViewModel) {
    val filterOptions by viewModel.filterOptions.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
        HorizontalDivider(color = KnockColor.Gray.gray4(LocalContext.current), thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            filterOptions.forEach { option ->
                val isSelected = option == currentFilter
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) KnockColor.Accent.accent11(LocalContext.current) else KnockColor.Gray.gray11(LocalContext.current),
                    label = ""
                )
                val underlineColor by animateColorAsState(
                    targetValue = if (isSelected) KnockColor.Accent.accent11(LocalContext.current) else Color.Transparent,
                    label = ""
                )

                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable {
                            viewModel.setCurrentFilter(option)
                        }
                        .width(IntrinsicSize.Min), // Ensure each column has minimal intrinsic width
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = option.title,
                        color = textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .height(1.dp)
                            .fillMaxWidth()
                            .background(underlineColor)
                    )
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewFilterTabView() {
    val viewModel: InAppFeedViewModel = viewModel(
    factory = InAppFeedViewModelFactory()
    )
    FilterTabView(viewModel = viewModel)
}
