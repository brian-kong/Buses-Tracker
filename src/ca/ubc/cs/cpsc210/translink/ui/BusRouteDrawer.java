package ca.ubc.cs.cpsc210.translink.ui;

import android.content.Context;
import ca.ubc.cs.cpsc210.translink.BusesAreUs;
import ca.ubc.cs.cpsc210.translink.model.Route;
import ca.ubc.cs.cpsc210.translink.model.RoutePattern;
import ca.ubc.cs.cpsc210.translink.model.Stop;
import ca.ubc.cs.cpsc210.translink.model.StopManager;
import ca.ubc.cs.cpsc210.translink.util.Geometry;
import ca.ubc.cs.cpsc210.translink.util.LatLon;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.PathOverlay;

import java.util.*;

// A bus route drawer
public class BusRouteDrawer extends MapViewOverlay {
    /**
     * overlay used to display bus route legend text on a layer above the map
     */
    private BusRouteLegendOverlay busRouteLegendOverlay;
    /**
     * overlays used to plot bus routes
     */
    private List<Polyline> busRouteOverlays;

    /**
     * Constructor
     *
     * @param context the application context
     * @param mapView the map view
     */
    public BusRouteDrawer(Context context, MapView mapView) {
        super(context, mapView);
        busRouteLegendOverlay = createBusRouteLegendOverlay();
        busRouteOverlays = new ArrayList<>();

    }

    /**
     * Plot each visible segment of each route pattern of each route going through the selected stop.
     */
    //TODO: complete the implementation of this method (Task 7)
    public void plotRoutes(int zoomLevel) {
        updateVisibleArea();
        busRouteLegendOverlay.clear();
        busRouteOverlays.clear();
        Stop stopSelected = StopManager.getInstance().getSelected();

        if (stopSelected != null) {
            for (Route route : stopSelected.getRoutes()) {
                this.busRouteLegendOverlay.add(route.getNumber());
                this.polylineVisible(route, zoomLevel);
            }
        }
    }

    /**
     * for every routePatterns available, we have the path's color and width and the line being overlay for two
     *
     * @param route the route of the stop the user clicks on
     *
     */
    private void polylineVisible(Route route, int zoomLevel) {
        for (RoutePattern routePattern : route.getPatterns()) {
            List<LatLon> path = routePattern.getPath();
            for (int i = 0; i < path.size() - 1; i++) {
                Polyline polyline = new Polyline(mapView.getContext());

                LatLon startingPoint = path.get(i);
                LatLon endingPoint = path.get(i + 1);

                polyline.setWidth(getLineWidth(zoomLevel));
                polyline.setColor(busRouteLegendOverlay.getColor(route.getNumber()));
                polylineCreated(startingPoint, endingPoint, polyline);
                busRouteOverlays.add(polyline);
            }
        }
    }

    /**
     * when the lines intersect, make sure to account for visibility, being the first one on top (busRouteOverlay.add)
     * Do not render the lines only if both ends of the lines are not visible on the screen
     *
     * @param startingPoint the first line that intersects
     * @param endingPoint  the other line that intersects
     * @param polyline the line that is drawn
     */
    private void polylineCreated(LatLon startingPoint, LatLon endingPoint, Polyline polyline) {
        if (Geometry.rectangleIntersectsLine(northWest,southEast,startingPoint, endingPoint)) {
            List<GeoPoint> geoPoints = new ArrayList<>();
            geoPoints.add((new GeoPoint(startingPoint.getLatitude(), startingPoint.getLongitude())));
            geoPoints.add((new GeoPoint(endingPoint.getLatitude(), endingPoint.getLongitude())));
            polyline.setPoints(geoPoints);
        }
    }


    public List<Polyline> getBusRouteOverlays() {
        return Collections.unmodifiableList(busRouteOverlays);
    }

    public BusRouteLegendOverlay getBusRouteLegendOverlay() {
        return busRouteLegendOverlay;
    }


    /**
     * Create text overlay to display bus route colours
     */
    private BusRouteLegendOverlay createBusRouteLegendOverlay() {
        ResourceProxy rp = new DefaultResourceProxyImpl(context);
        return new BusRouteLegendOverlay(rp, BusesAreUs.dpiFactor());
    }

    /**
     * Get width of line used to plot bus route based on zoom level
     *
     * @param zoomLevel the zoom level of the map
     * @return width of line used to plot bus route
     */
    private float getLineWidth(int zoomLevel) {
        if (zoomLevel > 14) {
            return 7.0f * BusesAreUs.dpiFactor();
        } else if (zoomLevel > 10) {
            return 5.0f * BusesAreUs.dpiFactor();
        } else {
            return 2.0f * BusesAreUs.dpiFactor();
        }
    }
}
