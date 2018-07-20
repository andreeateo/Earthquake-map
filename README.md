# Earthquake-map
Earthquake Map is an application that displays an interactive map of the world's earthquakes and enables the user to visualize the threat some cities face.

DISCLAIMER: the "threat circle" radius is for illustration purposes only and is not intended to be used for safety-critical or predictive applications.

The UI is a PApplet (Processing window) that displays the National Geographic world map provided by Esri alongside the map's legend.
As the user hovers the over a city or earthquake, a pop-up appears with information about the location.

**Project Structure**
- src: project source code
- lib_deps: library dependencies
- data: city and countries .json dat
- .project: Eclipse project file
- .classpath

**Prerequisites**

Eclipse IDE - for download and install go to https://www.eclipse.org/

**Set-up**

1. Download the project on your drive.
2. Import the Java Project in your Eclipse workspace.

**Running the app**

1. Run EarthquakeCityMap.java to launch the UI.
2. Observe the world's map, the map legend and zoom in and out using the scroll wheel.
3. Left-click on an earthquake and see if one of the main cities represented on the map if within its "threat circle".
4. Similarly, left-click on a city to see if it is within the "threat circle" of a nearby earthquake.
5. The console displays a list of the countries affected by earthquakes and their number, as well as how many earthquakes occurred in the oceans.

Sample 1
![alt text](https://github.com/andreeateo/Earthquake-map/blob/master/Sample1.png)

Sample 2
![alt text](https://github.com/andreeateo/Earthquake-map/blob/master/Sample2.png)

**Author**

Andreea Teodor

**Credits**

This application was written as part of an assignment for: *Data Structures and Performance – University of California San Diego on Coursera*.
 (the libraries, parsing script and outline of the application were provided as a starter code)
