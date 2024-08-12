package uk.ac.aber.dcs.chm9360.travelbuddy.ui.explore

import android.content.Context
import android.location.Geocoder
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import java.util.Locale

data class MapMarker(
    val title: String,
    val location: GeoPoint
)

fun createMapMarkerFromDestination(
    name: String,
    context: Context
): MapMarker {
    val geocoder = Geocoder(context, Locale.getDefault())
    geocoder.getFromLocationName(name, 1)?.firstOrNull()?.let {
        val point = GeoPoint(it.latitude, it.longitude)
        return MapMarker(name, point)
    }
    return MapMarker("", GeoPoint(0.0, 0.0))
}

@Composable
fun MapComposable(
    innerPadding: PaddingValues = PaddingValues(),
    context: Context,
    mapZoom: Double = 15.0,
    centerMarker: MapMarker,
    placesMarkers: List<MapMarker> = emptyList()
) {
    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        factory = {
            org.osmdroid.views.MapView(context).apply {
                Configuration.getInstance()
                    .load(
                        context,
                        context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE)
                    )
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(mapZoom)
                controller.setCenter(centerMarker.location)
            }
        },
        update = { mapView ->
            mapView.controller.setCenter(centerMarker.location)
            mapView.controller.setZoom(mapZoom)

            mapView.overlays.removeIf { it is Marker }

//                 Add marker for the destination
            val destinationMarker = Marker(mapView).apply {
                position = centerMarker.location
                icon = ContextCompat.getDrawable(context, R.drawable.ic_location_marker)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = centerMarker.title
            }
            mapView.overlays.add(destinationMarker)

            // Add markers for the fetched places
            placesMarkers.forEach { currentMarker ->
                val marker = Marker(mapView).apply {
                    position = currentMarker.location
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_marker)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = currentMarker.title
                }
                mapView.overlays.add(marker)
            }
            mapView.invalidate()
        }
    )
}