package uk.ac.aber.dcs.chm9360.travelbuddy.ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack

@Composable
fun AboutScreen(
    navController: NavHostController,
) {
    val title = R.string.about

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppBarWithArrowBack(navController, title.toString(), false)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp, start = 10.dp, end = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,

                ) {
                Image(
                    modifier = Modifier.size(70.dp),
                    painter = painterResource(id = R.drawable.luggage),
                    contentDescription = stringResource(id = R.string.app_icon),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = stringResource(id = R.string.app_name),
                    fontSize = 35.sp
                )
            }
            Text(
                text = stringResource(id = R.string.app_description),
                modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
            )
            Divider(thickness = 1.dp)
            Text(
                text = stringResource(id = R.string.version),
                modifier = Modifier.padding(top = 10.dp, bottom = 5.dp)
            )
            Text(
                text = "1.0",
                modifier = Modifier.padding(bottom = 10.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Divider(thickness = 1.dp)
            Text(
                text = stringResource(id = R.string.creator),
                modifier = Modifier.padding(top = 10.dp, bottom = 5.dp)
            )
            Text(
                text = "Maciej Traczyk",
                modifier = Modifier.padding(bottom = 20.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

        }
    }
}