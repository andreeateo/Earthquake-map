package parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PApplet;
import processing.data.XML;

public class ParseFeed
{
  public static List<PointFeature> parseEarthquake(PApplet p, String fileName)
  {
    List<PointFeature> features = new ArrayList<PointFeature>();

    XML rss = p.loadXML(fileName);
    XML[] itemXML = rss.getChildren("entry");
    PointFeature point;

    for (int i = 0; i < itemXML.length; i++)
    {
      Location location = getLocationFromPoint(itemXML[i]);
      if (location != null)
      {
        point = new PointFeature(location);
        features.add(point);
      }
      else
      {
        continue;
      }
      String titleStr = getStringVal(itemXML[i], "title");
      if (titleStr != null)
      {
        point.putProperty("title", titleStr);
        point.putProperty("magnitude", Float.parseFloat(titleStr.substring(2, 5)));
      }

      float depthVal = getFloatVal(itemXML[i], "georss:elev");

      int interVal = (int) (depthVal / 100);
      depthVal = (float) interVal / 10;
      point.putProperty("depth", Math.abs((depthVal)));

      XML[] catXML = itemXML[i].getChildren("category");
      for (int c = 0; c < catXML.length; c++)
      {
        String label = catXML[c].getString("label");
        if ("Age".equals(label))
        {
          String ageStr = catXML[c].getString("term");
          point.putProperty("age", ageStr);
        }
      }
    }
    return features;
  }

  private static Location getLocationFromPoint(XML itemXML)
  {
    Location loc = null;
    XML pointXML = itemXML.getChild("georss:point");

    if (pointXML != null && pointXML.getContent() != null)
    {
      String pointStr = pointXML.getContent();
      String[] latLon = pointStr.split(" ");
      float lat = Float.valueOf(latLon[0]);
      float lon = Float.valueOf(latLon[1]);

      loc = new Location(lat, lon);
    }

    return loc;
  }

  private static String getStringVal(XML itemXML, String tagName)
  {
    String str = null;
    XML strXML = itemXML.getChild(tagName);

    if (strXML != null && strXML.getContent() != null)
    {
      str = strXML.getContent();
    }
    return str;
  }

  private static float getFloatVal(XML itemXML, String tagName)
  {
    return Float.parseFloat(getStringVal(itemXML, tagName));
  }

  public static List<PointFeature> parseAirports(PApplet p, String fileName)
  {
    List<PointFeature> features = new ArrayList<PointFeature>();

    String[] rows = p.loadStrings(fileName);
    for (String row : rows)
    {
      int i = 0;
      String[] columns = row.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

      float lat = Float.parseFloat(columns[6]);
      float lon = Float.parseFloat(columns[7]);

      Location loc = new Location(lat, lon);
      PointFeature point = new PointFeature(loc);

      point.setId(columns[0]);

      point.addProperty("name", columns[1]);
      point.putProperty("city", columns[2]);
      point.putProperty("country", columns[3]);

      if (!columns[4].equals(""))
      {
        point.putProperty("code", columns[4]);
      }
      else if (!columns[5].equals(""))
      {
        point.putProperty("code", columns[5]);
      }

      point.putProperty("altitude", columns[8 + i]);

      features.add(point);
    }

    return features;

  }

  public static List<ShapeFeature> parseRoutes(PApplet p, String fileName)
  {
    List<ShapeFeature> routes = new ArrayList<ShapeFeature>();

    String[] rows = p.loadStrings(fileName);

    for (String row : rows)
    {
      String[] columns = row.split(",");

      ShapeFeature route = new ShapeFeature(Feature.FeatureType.LINES);

      if (!columns[3].equals("\\N") && !columns[5].equals("\\N"))
      {
        route.putProperty("source", columns[3]);
        route.putProperty("destination", columns[5]);
        routes.add(route);
      }
    }
    return routes;
  }

  public static HashMap<String, Float> loadLifeExpectancyFromCSV(PApplet p, String fileName)
  {
    HashMap<String, Float> lifeExpMap = new HashMap<String, Float>();

    String[] rows = p.loadStrings(fileName);

    for (String row : rows)
    {
      String[] columns = row.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

      for (int i = columns.length - 1; i > 3; i--)
      {
        if (!columns[i].equals(".."))
        {
          lifeExpMap.put(columns[3], Float.parseFloat(columns[i]));
          break;
        }
      }
    }
    return lifeExpMap;
  }

}