
import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PConstants;
import processing.core.PGraphics;

public abstract class EarthquakeMarker extends CommonMarker
{
  protected boolean isOnLand;
  protected float radius;

  protected static final float kmPerMile = 1.6f;

  // moderate earthquake
  public static final float THRESHOLD_MODERATE = 5;
  // light earthquake
  public static final float THRESHOLD_LIGHT = 4;
  // intermediate depth
  public static final float THRESHOLD_INTERMEDIATE = 70;
  // deep depth
  public static final float THRESHOLD_DEEP = 300;

  public abstract void drawEarthquake(PGraphics pg, float x, float y);

  public EarthquakeMarker(PointFeature feature)
  {
    super(feature.getLocation());
    // Add a radius property and then set the properties
    java.util.HashMap<String, Object> properties = feature.getProperties();
    float magnitude = Float.parseFloat(properties.get("magnitude").toString());
    properties.put("radius", 2 * magnitude);
    setProperties(properties);
    this.radius = 1.75f * getMagnitude();
  }

  public int compareTo(EarthquakeMarker otherMarker)
  {
    if (this.getMagnitude() < otherMarker.getMagnitude())
    {
      return -1;
    }
    else if (this.getMagnitude() < otherMarker.getMagnitude())
    {
      return 1;
    }
    else
    {
      return 0;
    }
  }

  @Override
  public void drawMarker(PGraphics pg, float x, float y)
  {
    pg.pushStyle();

    colorDetermine(pg);
    drawEarthquake(pg, x, y);

    String age = getStringProperty("age");
    if ("Past Hour".equals(age) || "Past Day".equals(age))
    {

      pg.strokeWeight(2);
      int buffer = 2;
      pg.line(x - (radius + buffer), y - (radius + buffer), x + radius + buffer, y + radius + buffer);
      pg.line(x - (radius + buffer), y + (radius + buffer), x + radius + buffer, y - (radius + buffer));

    }
    pg.popStyle();
  }

  public void showTitle(PGraphics pg, float x, float y)
  {
    String title = getTitle();
    pg.pushStyle();

    pg.rectMode(PConstants.CORNER);

    pg.stroke(110);
    pg.fill(255, 255, 255);
    pg.rect(x, y + 15, pg.textWidth(title) + 6, 18, 5);

    pg.textAlign(PConstants.LEFT, PConstants.TOP);
    pg.fill(0);
    pg.text(title, x + 3, y + 18);

    pg.popStyle();
  }

  /**
   * "threat circle" radius - distance up to which this earthquake can affect
   * things DISCLAIMER: this formula is for illustration purposes only and is not
   * intended to be used for safety-critical or predictive applications.
   */
  public double threatCircle()
  {
    double miles = 20.0f * Math.pow(1.8, 2 * getMagnitude() - 5);
    double km = (miles * kmPerMile);
    return km;
  }

  private void colorDetermine(PGraphics pg)
  {
    float depth = getDepth();

    if (depth < THRESHOLD_INTERMEDIATE)
    {
      pg.fill(255, 255, 0); // shallow = yellow
    }
    else if (depth < THRESHOLD_DEEP)
    {
      pg.fill(0, 0, 255); // intermediate = blue
    }
    else
    {
      pg.fill(255, 0, 0); // deep = red
    }
  }

  public String toString()
  {
    return getTitle();
  }

  public float getMagnitude()
  {
    return Float.parseFloat(getProperty("magnitude").toString());
  }

  public float getDepth()
  {
    return Float.parseFloat(getProperty("depth").toString());
  }

  public String getTitle()
  {
    return (String) getProperty("title");

  }

  public float getRadius()
  {
    return Float.parseFloat(getProperty("radius").toString());
  }

  public boolean isOnLand()
  {
    return isOnLand;
  }
}
