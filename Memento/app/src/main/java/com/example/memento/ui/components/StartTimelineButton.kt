import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.memento.LifeGridRoute

@Composable
fun StartTimelineButton(navController: NavController, enabled: Boolean) {
    var isLoading by remember { mutableStateOf(false) }

    Button(
        onClick = {
            isLoading = true
            navController.navigate(LifeGridRoute)
        },
        enabled = !isLoading && enabled,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .fillMaxHeight(0.1f)
    ) {
        if (isLoading) {
            Text("Loading Timeline...")
        } else {
            Text("Start My Timeline")
        }
    }
}