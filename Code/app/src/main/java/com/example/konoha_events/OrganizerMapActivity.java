package com.example.konoha_events;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
/**
 * OrganizerMapActivity
 * --------------------
 * Displays a map with markers for entrant locations using OSMDroid.
 * <p>
 * Data source:
 * <ul>
 *     <li>Reads documents from the "entrants" collection in Firestore.</li>
 *     <li>Each document is expected to contain fields:
 *         <ul>
 *             <li><b>deviceId</b> - the device identifier for the entrant</li>
 *             <li><b>latitude</b> - last known latitude</li>
 *             <li><b>longitude</b> - last known longitude</li>
 *         </ul>
 *     </li>
 * </ul>
 * <p>
 * This activity is launched from the organizer dashboard and is intended to support
 * the geolocation-related user stories such as US 02.02.02 (view entrant locations on a map).
 */

public class OrganizerMapActivity extends AppCompatActivity {

    private MapView mapView;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_map);

        // Configure OSMDroid
        Configuration.getInstance().setUserAgentValue(getPackageName());

        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        db = FirebaseFirestore.getInstance();

        // Load entrant locations
        loadEntrantLocations();
    }

    /**
     * Attach a Firestore snapshot listener to the "entrants" collection and
     * place a marker on the map for each entrant document with latitude/longitude.
     * <p>
     * When the underlying Firestore data changes, the markers are refreshed.
     */
    private void loadEntrantLocations() {
        db.collection("entrants").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable com.google.firebase.firestore.FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("OSMMap", "Firestore listener failed.", e);
                    Toast.makeText(OrganizerMapActivity.this, "Failed to load entrants.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshots == null || snapshots.isEmpty()) {
                    Toast.makeText(OrganizerMapActivity.this, "No entrant locations found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                mapView.getOverlays().clear();

                for (QueryDocumentSnapshot doc : snapshots) {
                    Double lat = doc.getDouble("latitude");
                    Double lng = doc.getDouble("longitude");
                    String id = doc.getString("deviceId");

                    if (lat != null && lng != null) {
                        GeoPoint point = new GeoPoint(lat, lng);
                        Marker marker = new Marker(mapView);
                        marker.setPosition(point);
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        marker.setTitle("Entrant: " + (id != null ? id : "Unknown"));
                        mapView.getOverlays().add(marker);
                    }
                }

                // Center camera on the first entrant (optional)
                DocumentSnapshot firstDoc = snapshots.getDocuments().get(0);
                Double lat = firstDoc.getDouble("latitude");
                Double lng = firstDoc.getDouble("longitude");
                if (lat != null && lng != null) {
                    mapView.getController().setZoom(10.0);
                    mapView.getController().setCenter(new GeoPoint(lat, lng));
                }


                mapView.invalidate();
            }
        });
    }
}
