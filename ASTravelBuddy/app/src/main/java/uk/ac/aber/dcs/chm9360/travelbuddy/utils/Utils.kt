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
    "accommodation.hut" to "Hut",
    "accommodation.apartment" to "Apartment",
    "accommodation.chalet" to "Chalet",
    "accommodation.guest_house" to "Guest House",
    "accommodation.hostel" to "Hostel",
    "accommodation.motel" to "Motel",

    "activity" to "Clubs, community centers",
    "activity.community_center" to "Community Center",
    "activity.sport_club" to "Sport Club",

    "airport" to "Facility for aircraft operations, including takeoffs, landings, and maintenance",
    "airport.private" to "Owned by individuals or entities, not open to the public, for private aviation",
    "airport.international" to "Handles international flights with customs and immigration facilities",
    "airport.military" to "Dedicated to armed forces use",
    "airport.gliding" to "Designed for glider operations, may include winches or tow planes",
    "airport.airfield" to "Area for aircraft operations, can range from simple to complex facilities",

    "commercial" to "Places where one can buy or sell things",
    "commercial.supermarket" to "Supermarket",
    "commercial.marketplace" to "Marketplace",
    "commercial.shopping_mall" to "Shopping Mall",
    "commercial.department_store" to "Department Store",
    "commercial.elektronics" to "Electronics",
    "commercial.outdoor_and_sport" to "Outdoor and Sport",
    "commercial.outdoor_and_sport.water_sports" to "Water Sports",
    "commercial.outdoor_and_sport.ski" to "Ski",
    "commercial.outdoor_and_sport.diving" to "Diving",
    "commercial.outdoor_and_sport.hunting" to "Hunting",
    "commercial.outdoor_and_sport.bicycle" to "Bicycle",
    "commercial.outdoor_and_sport.fishing" to "Fishing",
    "commercial.outdoor_and_sport.golf" to "Golf",
    "commercial.vehicle" to "Vehicle",
    "commercial.hobby" to "Hobby",
    "commercial.hobby.model" to "Model",
    "commercial.hobby.anime" to "Anime",
    "commercial.hobby.collecting" to "Collecting",
    "commercial.hobby.games" to "Games",
    "commercial.hobby.brewing" to "Brewing",
    "commercial.hobby.photo" to "Photo",
    "commercial.hobby.music" to "Music",
    "commercial.hobby.sewing_and_knitting" to "Sewing and Knitting",
    "commercial.hobby.art" to "Art",
    "commercial.books" to "Books",
    "commercial.gift_and_souvenir" to "Gift and Souvenir",
    "commercial.stationery" to "Stationery",
    "commercial.newsagent" to "Newsagent",
    "commercial.tickets_and_lottery" to "Tickets and Lottery",
    "commercial.clothing" to "Clothing",
    "commercial.clothing.shoes" to "Shoes",
    "commercial.clothing.clothes" to "Clothes",
    "commercial.clothing.underwear" to "Underwear",
    "commercial.clothing.sport" to "Sport",
    "commercial.clothing.men" to "Men",
    "commercial.clothing.women" to "Women",
    "commercial.clothing.kids" to "Kids",
    "commercial.clothing.accessories" to "Accessories",
    "commercial.bag" to "Bag",
    "commercial.baby_goods" to "Baby Goods",
    "commercial.agrarian" to "Agrarian",
    "commercial.garden" to "Garden",
    "commercial.houseware_and_hardware" to "Houseware and Hardware",
    "commercial.houseware_and_hardware.doityourself" to "DIY",
    "commercial.houseware_and_hardware.hardware_and_tools" to "Hardware and Tools",
    "commercial.houseware_and_hardware.building_materials" to "Building Materials",
    "commercial.houseware_and_hardware.building_materials.paint" to "Paint",
    "commercial.houseware_and_hardware.building_materials.glaziery" to "Glaziery",
    "commercial.houseware_and_hardware.building_materials.doors" to "Doors",
    "commercial.houseware_and_hardware.building_materials.tiles" to "Tiles",
    "commercial.houseware_and_hardware.building_materials.windows" to "Windows",
    "commercial.houseware_and_hardware.building_materials.flooring" to "Flooring",
    "commercial.houseware_and_hardware.fireplace" to "Fireplace",
    "commercial.houseware_and_hardware.swimming_pool" to "Swimming Pool",
    "commercial.florist" to "Florist",
    "commercial.furniture_and_interior" to "Furniture and Interior",
    "commercial.furniture_and_interior.lighting" to "Lighting",
    "commercial.furniture_and_interior.curtain" to "Curtain",
    "commercial.furniture_and_interior.carpet" to "Carpet",
    "commercial.furniture_and_interior.kitchen" to "Kitchen",
    "commercial.furniture_and_interior.bed" to "Bed",
    "commercial.furniture_and_interior.bathroom" to "Bathroom",
    "commercial.chemist" to "Chemist",
    "commercial.health_and_beauty" to "Health and Beauty",
    "commercial.health_and_beauty.pharmacy" to "Pharmacy",
    "commercial.health_and_beauty.optician" to "Optician",
    "commercial.health_and_beauty.medical_supply" to "Medical Supply",
    "commercial.health_and_beauty.hearing_aids" to "Hearing Aids",
    "commercial.health_and_beauty.herbalist" to "Herbalist",
    "commercial.health_and_beauty.cosmetics" to "Cosmetics",
    "commercial.health_and_beauty.wigs" to "Wigs",
    "commercial.toy_and_game" to "Toy and Game",
    "commercial.pet" to "Pet",
    "commercial.food_and_drink" to "Food and Drink",
    "commercial.food_and_drink.bakery" to "Bakery",
    "commercial.food_and_drink.deli" to "Deli",
    "commercial.food_and_drink.frozen_food" to "Frozen Food",
    "commercial.food_and_drink.pasta" to "Pasta",
    "commercial.food_and_drink.spices" to "Spices",
    "commercial.food_and_drink.organic" to "Organic",
    "commercial.food_and_drink.honey" to "Honey",
    "commercial.food_and_drink.rice" to "Rice",
    "commercial.food_and_drink.nuts" to "Nuts",
    "commercial.food_and_drink.health_food" to "Health Food",
    "commercial.food_and_drink.ice_cream" to "Ice Cream",
    "commercial.food_and_drink.seafood" to "Seafood",
    "commercial.food_and_drink.fruit_and_vegetable" to "Fruit and Vegetable",
    "commercial.food_and_drink.farm" to "Farm",
    "commercial.food_and_drink.confectionery" to "Confectionery",
    "commercial.food_and_drink.chocolate" to "Chocolate",
    "commercial.food_and_drink.butcher" to "Butcher",
    "commercial.food_and_drink.cheese_and_dairy" to "Cheese and Dairy",
    "commercial.food_and_drink.drinks" to "Drinks",
    "commercial.food_and_drink.coffee_and_tea" to "Coffee and Tea",
    "commercial.convenience" to "Convenience",
    "commercial.discount_store" to "Discount Store",
    "commercial.smoking" to "Smoking",
    "commercial.second_hand" to "Second Hand",
    "commercial.gas" to "Gas",
    "commercial.weapons" to "Weapons",
    "commercial.pyrotechnics" to "Pyrotechnics",
    "commercial.energy" to "Energy",
    "commercial.wedding" to "Wedding",
    "commercial.jewelry" to "Jewelry",
    "commercial.watches" to "Watches",
    "commercial.art" to "Art",
    "commercial.antiques" to "Antiques",
    "commercial.video_and_music" to "Video and Music",
    "commercial.erotic" to "Erotic",
    "commercial.trade" to "Trade",
    "commercial.kiosk" to "Kiosk",

    "catering" to "Places of public catering: restaurants, cafes, bars, etc.",
    "catering.restaurant" to "Restaurant",
    "catering.restaurant.pizza" to "Pizza Restaurant",
    "catering.restaurant.burger" to "Burger Restaurant",
    "catering.restaurant.regional" to "Regional Restaurant",
    "catering.restaurant.italian" to "Italian Restaurant",
    "catering.restaurant.chinese" to "Chinese Restaurant",
    "catering.restaurant.sandwich" to "Sandwich Restaurant",
    "catering.restaurant.chicken" to "Chicken Restaurant",
    "catering.restaurant.mexican" to "Mexican Restaurant",
    "catering.restaurant.japanese" to "Japanese Restaurant",
    "catering.restaurant.vietnamese" to "Vietnamese Restaurant",
    "catering.restaurant.french" to "French Restaurant",
    "catering.restaurant.indian" to "Indian Restaurant",
    "catering.restaurant.thai" to "Thai Restaurant",
    "catering.cafe" to "Cafe",
    "catering.bar" to "Bar",
    "catering.pub" to "Pub",
    "catering.tavern" to "Tavern",
    "catering.snack" to "Snack Bar",
    "catering.fast_food" to "Fast Food",
    "catering.tapas" to "Tapas Bar",
    "catering.chocolatier" to "Chocolatier",
    "catering.bakery" to "Bakery",

    "entertainment" to "Entertainment venues and activities",
    "entertainment.cinema" to "Cinema",
    "entertainment.theater" to "Theater",
    "entertainment.park" to "Park",
    "entertainment.fair" to "Fair",
    "entertainment.museum" to "Museum",
    "entertainment.gallery" to "Gallery",
    "entertainment.casino" to "Casino",
    "entertainment.sport" to "Sport",
    "entertainment.night_club" to "Night Club",
    "entertainment.amusement_park" to "Amusement Park",
    "entertainment.concert" to "Concert",
    "entertainment.zoo" to "Zoo",
    "entertainment.aquarium" to "Aquarium",
    "entertainment.cultural_center" to "Cultural Center",
    "entertainment.ice_rink" to "Ice Rink",

    "health" to "Health-related facilities and services",
    "health.hospital" to "Hospital",
    "health.clinic" to "Clinic",
    "health.dental" to "Dental",
    "health.ophthalmologist" to "Ophthalmologist",
    "health.pharmacy" to "Pharmacy",
    "health.veterinary" to "Veterinary",
    "health.rehabilitation" to "Rehabilitation",
    "health.counseling" to "Counseling",
    "health.nursing_home" to "Nursing Home",
    "health.home_health_care" to "Home Health Care",

    "public_service" to "Public service facilities",
    "public_service.police" to "Police Station",
    "public_service.fire_station" to "Fire Station",
    "public_service.government_office" to "Government Office",
    "public_service.post_office" to "Post Office",
    "public_service.library" to "Library",
    "public_service.municipal_office" to "Municipal Office",
    "public_service.social_service" to "Social Service",
    "public_service.recycling_center" to "Recycling Center",

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

    "touristic" to "Tourist attractions and services",
    "touristic.landmark" to "Landmark",
    "touristic.tour" to "Tour",
    "touristic.attraction" to "Attraction",
    "touristic.info_center" to "Information Center",
    "touristic.souvenir_shop" to "Souvenir Shop",
    "touristic.viewpoint" to "Viewpoint",
    "touristic.monument" to "Monument",
    "touristic.historic_site" to "Historic Site"
)