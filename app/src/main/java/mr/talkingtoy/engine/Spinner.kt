package mr.talkingtoy.engine

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Spinner(items: List<String>,property: String, context: Context) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(SettingsManager.getString(property, items[0], context)?:items[0])}

    Column  {
        Text(
            text = selectedOption,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { expanded = true })
                .padding(16.dp),
            color = Color.White,
            fontSize = 16.sp
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.Black.copy(alpha = 0.6f)).border(
                width = 1.dp,
                color = Color.White,
                shape = RectangleShape
            )
        ) {
            items.forEach { item ->
            Text(text = item, modifier = Modifier.clickable {
                SettingsManager.putString(property, item, context)
                selectedOption = item
            }.padding(4.dp).fillMaxWidth(), color = Color.White, fontSize = 16.sp)
            }
        }
    }
}