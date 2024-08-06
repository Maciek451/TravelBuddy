package uk.ac.aber.dcs.chm9360.travelbuddy.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
import uk.ac.aber.dcs.chm9360.travelbuddy.model.Trip
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object Utils {
    var trip: Trip? = null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePicker(
    value: LocalDate,
    onValueChange: (LocalDate) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val open = rememberSaveable { mutableStateOf(false) }

    if (open.value) {
        CalendarDialog(
            state = rememberUseCaseState(
                visible = true,
                true,
                onCloseRequest = { open.value = false }),
            config = CalendarConfig(
                yearSelection = true,
                style = CalendarStyle.MONTH,
            ),
            selection = CalendarSelection.Date(
                selectedDate = value
            ) { newDate ->
                onValueChange(newDate)
                open.value = false
            },
        )
    }

    Column(
        modifier = modifier
            .clickable { open.value = true }
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .border(1.dp, MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        )
        Text(
            text = value.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

val categoryList = listOf(
    "accommodation" to "Place to stay or live",
    "accommodation.hotel" to "Hotel",
    "accommodation.apartment" to "Apartment",
    "accommodation.guest_house" to "Guest House",
    "accommodation.motel" to "Motel",

    "activity" to "Clubs, community centers",
    "activity.community_center" to "Community Center",
    "activity.sport_club" to "Sport Club",

    "airport" to "Facility for aircraft operations, including takeoffs, landings, and maintenance",
    "airport.military" to "Dedicated to armed forces use",

    "commercial" to "Places where one can buy or sell things",
    "commercial.supermarket" to "Supermarket",
    "commercial.marketplace" to "Marketplace",
    "commercial.shopping_mall" to "Shopping Mall",
    "commercial.department_store" to "Department Store",
    "commercial.elektronics" to "Electronics",
    "commercial.outdoor_and_sport" to "Outdoor and Sport",
    "commercial.vehicle" to "Vehicle",
    "commercial.hobby" to "Hobby",
    "commercial.hobby.model" to "Model",
    "commercial.hobby.anime" to "Anime",
    "commercial.hobby.collecting" to "Collecting",
    "commercial.hobby.games" to "Games",
    "commercial.hobby.photo" to "Photo",
    "commercial.hobby.music" to "Music",
    "commercial.hobby.art" to "Art",
    "commercial.books" to "Books",
    "commercial.gift_and_souvenir" to "Gift and Souvenir",
    "commercial.stationery" to "Stationery",
    "commercial.newsagent" to "Newsagent",
    "commercial.tickets_and_lottery" to "Tickets and Lottery",
    "commercial.clothing" to "Clothing",
    "commercial.clothing.shoes" to "Shoes",
    "commercial.garden" to "Garden",
    "commercial.houseware_and_hardware.doityourself" to "DIY",
    "commercial.florist" to "Florist",
    "commercial.furniture_and_interior" to "Furniture and Interior",
    "commercial.chemist" to "Chemist",
    "commercial.health_and_beauty" to "Health and Beauty",
    "commercial.health_and_beauty.pharmacy" to "Pharmacy",
    "commercial.health_and_beauty.optician" to "Optician",
    "commercial.health_and_beauty.medical_supply" to "Medical Supply",
    "commercial.health_and_beauty.hearing_aids" to "Hearing Aids",
    "commercial.health_and_beauty.herbalist" to "Herbalist",
    "commercial.health_and_beauty.cosmetics" to "Cosmetics",
    "commercial.toy_and_game" to "Toy and Game",
    "commercial.pet" to "Pet",
    "commercial.food_and_drink" to "Food and Drink",
    "commercial.food_and_drink.bakery" to "Bakery",
    "commercial.food_and_drink.ice_cream" to "Ice Cream",
    "commercial.food_and_drink.fruit_and_vegetable" to "Fruit and Vegetable",
    "commercial.food_and_drink.butcher" to "Butcher",
    "commercial.food_and_drink.drinks" to "Drinks",
    "commercial.food_and_drink.coffee_and_tea" to "Coffee and Tea",
    "commercial.weapons" to "Weapons",
    "commercial.pyrotechnics" to "Pyrotechnics",
    "commercial.wedding" to "Wedding",
    "commercial.jewelry" to "Jewelry",
    "commercial.watches" to "Watches",
    "commercial.art" to "Art",
    "commercial.antiques" to "Antiques",
    "commercial.video_and_music" to "Video and Music",

    "catering" to "Places of public catering: restaurants, cafes, bars, etc.",
    "catering.restaurant" to "Restaurant",
    "catering.restaurant.pizza" to "Pizza Restaurant",
    "catering.restaurant.burger" to "Burger Restaurant",
    "catering.cafe" to "Cafe",
    "catering.bar" to "Bar",
    "catering.pub" to "Pub",
    "catering.fast_food" to "Fast Food",

    "entertainment" to "Entertainment venues and activities",
    "entertainment.cinema" to "Cinema",
    "entertainment.theater" to "Theater",
    "entertainment.park" to "Park",
    "entertainment.museum" to "Museum",
    "entertainment.gallery" to "Gallery",
    "entertainment.casino" to "Casino",
    "entertainment.zoo" to "Zoo",
    "entertainment.culture.arts_center" to "Cultural Center",

    "healthcare" to "Health-related facilities and services",
    "healthcare.hospital" to "Hospital",
    "healthcare.clinic" to "Clinic",
    "healthcare.dental" to "Dental",
    "healthcare.pharmacy" to "Pharmacy",
    "healthcare.veterinary" to "Veterinary",

    "service" to "Public service facilities",
    "service.police" to "Police Station",
    "service.post_office" to "Post Office",

    "transport" to "Transport services and facilities",
    "transport.bus_station" to "Bus Station",
    "transport.train_station" to "Train Station",
    "transport.taxi" to "Taxi",
    "transport.subway" to "Subway",
    "transport.tram" to "Tram",
    "transport.ferry" to "Ferry",
    "transport.car_rental" to "Car Rental",
    "transport.bike_rental" to "Bike Rental",
    "transport.parking" to "Parking",
    "transport.airport" to "Airport",
    "transport.charging_station" to "Charging Station",

    "tourism" to "Tourist attractions and services",
    "tourism.attraction" to "Attraction",
    "tourism.information" to "Information Center",
    "touristic.souvenir_shop" to "Souvenir Shop",
    "tourism.attraction.viewpoint" to "Viewpoint",
    "tourism.sights.memorial" to "Memorial Sights",
    "tourism.sights.memorial.monument" to "Monument",

    "religion" to "Religious places and facilities"
)