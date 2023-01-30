package ca.ubc.cs.cpsc210.translink.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import ca.ubc.cs.cpsc210.translink.BusesAreUs;
import ca.ubc.cs.cpsc210.translink.R;
import ca.ubc.cs.cpsc210.translink.model.Route;
import ca.ubc.cs.cpsc210.translink.model.Stop;
import ca.ubc.cs.cpsc210.translink.model.StopManager;
import ca.ubc.cs.cpsc210.translink.util.Geometry;
import ca.ubc.cs.cpsc210.translink.util.LatLon;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// A plotter for bus stop locations
public class BusStopPlotter extends MapViewOverlay {
    /**
     * clusterer
     */
    private RadiusMarkerClusterer stopClusterer;
    /**
     * maps each stop to corresponding marker on map
     */
    private Map<Stop, Marker> stopMarkerMap = new HashMap<>();
    /**
     * marker for stop that is nearest to user (null if no such stop)
     */
    private Marker nearestStnMarker;
    private Activity activity;
    private StopInfoWindow stopInfoWindow;
    private Stop lastNearestStop;

    /**
     * Constructor
     *
     * @param activity the application context
     * @param mapView  the map view on which buses are to be plotted
     */
    public BusStopPlotter(Activity activity, MapView mapView) {
        super(activity.getApplicationContext(), mapView);
        this.activity = activity;
        nearestStnMarker = null;
        stopInfoWindow = new StopInfoWindow((StopSelectionListener) activity, mapView);
        newStopClusterer();
    }

    public RadiusMarkerClusterer getStopClusterer() {
        return stopClusterer;
    }

    /**
     * Mark all visible stops in stop manager onto map.
     */

     /*- main focus to do: if a stop is found in this point, set up a marker on that stop. (get) it
        - probably need to iterate through this (LATITUDE and Longitude needed to get that stop)
        and use the relationship with stop with StopManager
        - Moreover, I probably need a helper function
           - Need for the title - if the route has the stop, then we need to give its RouteNumber: YOIT
           - Afterwards, I would probably need to figure out how this Geometry thing works
        ... and apply it by adding markers to the cluster.*/
    // TODO: complete the implementation of this method (Task 5)
    public void markStops(Location currentLocation) {
        //Drawable stopIconDrawable = activity.getResources().getDrawable(R.drawable.stop_icon);
        updateVisibleArea();
        newStopClusterer();
        stopMarker();
        updateNearestStop(currentLocation);

    }

    /* we are setting the marker's title with the stop number (sorry I don't do JavaDocs)
    *
    * @param stop the stop the user picks
    *
    */
    private String stopInformation(Stop stop) {
        StringBuilder informationTitle = new StringBuilder(stop.getNumber() + ": " + stop.getName());
        for (Route route: stop.getRoutes()) {
            informationTitle.append('\n').append(route.getNumber());
        }
        return informationTitle.toString();
    }

    /*
     *
     * Mark all visible stops in stop manager onto map.
     *
     */
    private void stopMarker() {
        Drawable stopIconDrawable = activity.getResources().getDrawable(R.drawable.stop_icon);
        for (Stop nextStop : StopManager.getInstance()) {
            Marker marker;
            if (Geometry.rectangleContainsPoint(northWest, southEast, nextStop.getLocn())) {
                marker = new Marker(mapView); // setters
                marker.setIcon(stopIconDrawable);
                marker.setPosition(new GeoPoint(nextStop.getLocn().getLatitude(), nextStop.getLocn().getLongitude()));
                marker.setInfoWindow(stopInfoWindow);
                marker.setRelatedObject(nextStop); // I want the next stop -.-
                marker.setTitle(stopInformation(nextStop));
                setMarker(nextStop, marker);
                stopClusterer.add(marker); // Want to update the next marker

            }
        }
        updateMarkerOfNearest(lastNearestStop);
    }

    /*
    * updates the nearest stop
    *
    * @param currentLocation the location of the marker (GPS)
    *
    */
    private void updateNearestStop(Location currentLocation) {
        if (currentLocation != null) {
            Stop nearestKnownStop = StopManager.getInstance().findNearestTo(
                    new LatLon(currentLocation.getLatitude(), currentLocation.getLongitude()));
            updateMarkerOfNearest(nearestKnownStop);
        }
    }

    /**
     * Create a new stop cluster object used to group stops that are close by to reduce screen clutter
     */
    private void newStopClusterer() {
        stopClusterer = new RadiusMarkerClusterer(activity);
        stopClusterer.getTextPaint().setTextSize(20.0F * BusesAreUs.dpiFactor());
        int zoom = mapView == null ? 16 : mapView.getZoomLevel();
        if (zoom == 0) {
            zoom = MapDisplayFragment.DEFAULT_ZOOM;
        }
        int radius = 1000 / zoom;

        stopClusterer.setRadius(radius);
        Drawable clusterIconD = activity.getResources().getDrawable(R.drawable.stop_cluster);
        Bitmap clusterIcon = ((BitmapDrawable) clusterIconD).getBitmap();
        stopClusterer.setIcon(clusterIcon);
    }

    /**
     * Update marker of nearest stop (called when user's location has changed).  If nearest is null,
     * no stop is marked as the nearest stop.
     *
     * @param nearest stop nearest to user's location (null if no stop within StopManager.RADIUS metres)
     */
    // TODO: complete the implementation of this method (Task 6)
    public void updateMarkerOfNearest(Stop nearest) {
        Drawable stopIconDrawable = activity.getResources().getDrawable(R.drawable.stop_icon);
        Drawable closestStopIconDrawable = activity.getResources().getDrawable(R.drawable.closest_stop_icon);

        lastNearestStop = nearest;
        if (nearestStnMarker != null) {
            nearestStnMarker.setIcon(stopIconDrawable);
        }

        if (nearest == null) {
            nearestStnMarker = null;
        } else {
            this.nearestStnMarker = getMarker(nearest);
            nearestStnMarker.setIcon(closestStopIconDrawable);
        }
        updateVisibleArea();
    }

    /**
     * Manage mapping from stops to markers using a map from stops to markers.
     * The mapping in the other direction is done using the Marker.setRelatedObject() and
     * Marker.getRelatedObject() methods.
     */
    private Marker getMarker(Stop stop) {
        return stopMarkerMap.get(stop);
    }

    private void setMarker(Stop stop, Marker marker) {
        stopMarkerMap.put(stop, marker);
    }

    private void clearMarker(Stop stop) {
        stopMarkerMap.remove(stop);
    }

    private void clearMarkers() {
        stopMarkerMap.clear();
    }
}
