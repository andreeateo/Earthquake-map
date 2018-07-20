
import java.util.ArrayList;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.EsriProvider;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

/**
 * EarthquakeCityMap An application with an interactive map displaying
 * earthquake data.
 * 
 * @author andreea teodor
 */
public class EarthquakeCityMap extends PApplet
{

  private static final long serialVersionUID = 1L;

  // for working OFFILINE, change the value of this variable to true
  private static final boolean offline = false;
  public static String mbTilesString = "blankLight-1-3.mbtiles";

  // live feed with magnitude 2.5+ Earthquakes
  private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";

  private String cityFile = "city-data.json";
  private String countryFile = "countries.geo.json";

  private UnfoldingMap map;

  private List<Marker> cityMarkers;
  private List<Marker> quakeMarkers;

  private List<Marker> countryMarkers;

  private CommonMarker lastSelected;
  private CommonMarker lastClicked;

  public void setup()
  {
    size(900, 700, OPENGL);
    if (offline)
    {
      map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
      earthquakesURL = "2.5_week.atom"; // The same feed, but saved August 7, 2015
    }
    else
    {
      map = new UnfoldingMap(this, 200, 50, 650, 600, new EsriProvider.NatGeoWorldMap());
    }
    MapUtils.createDefaultEventDispatcher(this, map);

    List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
    countryMarkers = MapUtils.createSimpleMarkers(countries);

    List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
    cityMarkers = new ArrayList<Marker>();
    for (Feature city : cities)
    {
      cityMarkers.add(new CityMarker(city));
    }

    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
    quakeMarkers = new ArrayList<Marker>();

    for (PointFeature feature : earthquakes)
    {
      if (isLand(feature))
      {
        quakeMarkers.add(new LandQuakeMarker(feature));
      }
      else
      {
        quakeMarkers.add(new OceanQuakeMarker(feature));
      }
    }

    printQuakes();

    map.addMarkers(quakeMarkers);
    map.addMarkers(cityMarkers);

  }

  public void draw()
  {
    background(0);
    map.draw();
    addKey();

  }

  @Override
  public void mouseMoved()
  {
    // clear the last selection
    if (lastSelected != null)
    {
      lastSelected.setSelected(false);
      lastSelected = null;
    }
    selectMarkerIfHover(quakeMarkers);
    selectMarkerIfHover(cityMarkers);
  }

  private void selectMarkerIfHover(List<Marker> markers)
  {
    if (lastSelected != null)
    {
      return;
    }

    for (Marker m : markers)
    {
      CommonMarker marker = (CommonMarker) m;
      if (marker.isInside(map, mouseX, mouseY))
      {
        lastSelected = marker;
        marker.setSelected(true);
        return;
      }
    }
  }

  /**
   * display an earthquake and its threat circle of cities if a city is clicked,
   * it will display all the earthquakes where the city is in the threat circle
   */
  @Override
  public void mouseClicked()
  {
    if (lastClicked != null)
    {
      unhideMarkers();
      lastClicked = null;
    }
    else if (lastClicked == null)
    {
      checkEarthquakesForClick();
      if (lastClicked == null)
      {
        checkCitiesForClick();
      }
    }
  }

  private void checkCitiesForClick()
  {
    if (lastClicked != null)
      return;
    for (Marker marker : cityMarkers)
    {
      if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY))
      {
        lastClicked = (CommonMarker) marker;
        // Hide all the other earthquakes and hide
        for (Marker mhide : cityMarkers)
        {
          if (mhide != lastClicked)
          {
            mhide.setHidden(true);
          }
        }
        for (Marker mhide : quakeMarkers)
        {
          EarthquakeMarker quakeMarker = (EarthquakeMarker) mhide;
          if (quakeMarker.getDistanceTo(marker.getLocation()) > quakeMarker.threatCircle())
          {
            quakeMarker.setHidden(true);
          }
        }
        return;
      }
    }
  }

  private void checkEarthquakesForClick()
  {
    if (lastClicked != null)
      return;
    // Loop over the earthquake markers to see if one of them is selected
    for (Marker m : quakeMarkers)
    {
      EarthquakeMarker marker = (EarthquakeMarker) m;
      if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY))
      {
        lastClicked = marker;
        // Hide all the other earthquakes and hide
        for (Marker mhide : quakeMarkers)
        {
          if (mhide != lastClicked)
          {
            mhide.setHidden(true);
          }
        }
        for (Marker mhide : cityMarkers)
        {
          if (mhide.getDistanceTo(marker.getLocation()) > marker.threatCircle())
          {
            mhide.setHidden(true);
          }
        }
        return;
      }
    }
  }

  private void unhideMarkers()
  {
    for (Marker marker : quakeMarkers)
    {
      marker.setHidden(false);
    }

    for (Marker marker : cityMarkers)
    {
      marker.setHidden(false);
    }
  }

  private void addKey()
  {
    fill(255, 250, 240);

    int xbase = 25;
    int ybase = 50;

    rect(xbase, ybase, 150, 250);

    fill(0);
    textAlign(LEFT, CENTER);
    textSize(12);
    text("Earthquake Key", xbase + 25, ybase + 25);

    fill(50, 205, 50);
    int tri_xbase = xbase + 35;
    int tri_ybase = ybase + 50;
    triangle(tri_xbase, tri_ybase - CityMarker.TRI_SIZE, tri_xbase - CityMarker.TRI_SIZE,
        tri_ybase + CityMarker.TRI_SIZE, tri_xbase + CityMarker.TRI_SIZE, tri_ybase + CityMarker.TRI_SIZE);

    fill(0, 0, 0);
    textAlign(LEFT, CENTER);
    text("City Marker", tri_xbase + 15, tri_ybase);

    text("Land Quake", xbase + 50, ybase + 70);
    text("Ocean Quake", xbase + 50, ybase + 90);
    text("Size ~ Magnitude", xbase + 25, ybase + 120);

    fill(255, 255, 255);
    ellipse(xbase + 35, ybase + 72, 10, 10);
    rect(xbase + 35 - 5, ybase + 88, 10, 10);

    fill(color(255, 255, 0));
    ellipse(xbase + 35, ybase + 150, 12, 12);
    fill(color(0, 0, 255));
    ellipse(xbase + 35, ybase + 170, 12, 12);
    fill(color(255, 0, 0));
    ellipse(xbase + 35, ybase + 190, 12, 12);

    textAlign(LEFT, CENTER);
    fill(0, 0, 0);
    text("Shallow", xbase + 50, ybase + 150);
    text("Intermediate", xbase + 50, ybase + 170);
    text("Deep", xbase + 50, ybase + 190);

    text("Past hour", xbase + 50, ybase + 210);

    fill(255, 255, 255);
    int centerx = xbase + 35;
    int centery = ybase + 210;
    ellipse(centerx, centery, 12, 12);

    strokeWeight(2);
    line(centerx - 8, centery - 8, centerx + 8, centery + 8);
    line(centerx - 8, centery + 8, centerx + 8, centery - 8);

  }

  // Checks whether this quake occurred on land
  private boolean isLand(PointFeature earthquake)
  {
    for (Marker country : countryMarkers)
    {
      if (isInCountry(earthquake, country))
      {
        return true;
      }
    }
    // not inside any country
    return false;
  }

  // prints countries with number of earthquakes
  private void printQuakes()
  {
    int totalWaterQuakes = quakeMarkers.size();
    for (Marker country : countryMarkers)
    {
      String countryName = country.getStringProperty("name");
      int numQuakes = 0;
      for (Marker marker : quakeMarkers)
      {
        EarthquakeMarker eqMarker = (EarthquakeMarker) marker;
        if (eqMarker.isOnLand())
        {
          if (countryName.equals(eqMarker.getStringProperty("country")))
          {
            numQuakes++;
          }
        }
      }
      if (numQuakes > 0)
      {
        totalWaterQuakes -= numQuakes;
        System.out.println(countryName + ": " + numQuakes + " earthquake(s)");
      }
    }
    System.out.println("OCEAN QUAKES: " + totalWaterQuakes + " earthquake(s)");
  }

  // helper method to test whether a given earthquake is in a given country
  private boolean isInCountry(PointFeature earthquake, Marker country)
  {
    Location checkLoc = earthquake.getLocation();
    if (country.getClass() == MultiMarker.class)
    {
      for (Marker marker : ((MultiMarker) country).getMarkers())
      {
        if (((AbstractShapeMarker) marker).isInsideByLocation(checkLoc))
        {
          earthquake.addProperty("country", country.getProperty("name"));
          return true;
        }
      }
    }

    else if (((AbstractShapeMarker) country).isInsideByLocation(checkLoc))
    {
      earthquake.addProperty("country", country.getProperty("name"));

      return true;
    }
    return false;
  }

}
