package com.rigen.rigen.lib;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Direction
{
	private final String URL					= "http://maps.googleapis.com/maps/api/directions/xml?";

	public final static String MODE_DRIVING		= "driving";
	public final static String MODE_WALKING		= "walking";
	public final static String MODE_BYCICLING		= "bicycling";

	private final String KEY_DURATION		= "duration";
	private final String KEY_DURATION_TEXT	= "text";
	private final String KEY_DURATION_VALUE	= "value";
	private final String KEY_DISTANCE		= "distance";
	private final String KEY_DISTANCE_TEXT	= "text";
	private final String KEY_DISTANCE_VALUE	= "value";
	private final String KEY_START_ADDRESS	= "start_address";
	private final String KEY_END_ADDRESS		= "end_address";
	private final String KEY_STEPS			= "step";
	private final String KEY_START_LOCATION	= "start_location";
	private final String KEY_LAT				= "lat";
	private final String KEY_LNG				= "lng";
	private final String KEY_POLYLINE		= "polyline";
	private final String KEY_POINTS			= "points";
	private final String KEY_END_LOCATION	= "end_location";

	public Direction()
	{
		// TODO Auto-generated constructor stub
	}

	/*
	 * 
	 */
	public Document getDocument(LatLng start, LatLng end, String mode)
	{
		String uri = URL + "origin=" + start.latitude + "," + start.longitude
				+ "&destination=" + end.latitude + "," + end.longitude
				+ "&sensor=false&units=metric&mode=" + mode;

		Log.e(Direction.class.getSimpleName(), uri);
		try
		{
			URL url = new URL(uri);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			String responseString = readStream(con.getInputStream());
			InputStream in =con.getInputStream();
			Log.e(Direction.class.getSimpleName(), responseString);
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(in);
			return doc;
		} catch (Exception e)
		{
			// TODO: handle exception
		}
		return null;
	}

	private String readStream(InputStream in) {
		BufferedReader reader = null;
		StringBuffer response = new StringBuffer();
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			String line = "";
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return response.toString();
	}
	/*
	 * Method untuk mengambil durasi text
	 * 
	 * @params Document
	 * @return String
	 */
	public String getDurationText(Document doc)
	{
		NodeList nodeList = doc.getElementsByTagName(KEY_DURATION);
		Node node = nodeList.item(0);
		NodeList nodeList2 = node.getChildNodes();
		Node node2 = nodeList2.item(getNodeIndex(nodeList2, KEY_DURATION_TEXT));
		Log.d("get duration text", node2.getTextContent());

		return node2.getTextContent();

	}

	/*
	 * Method untuk mengambil durasi value
	 * 
	 * @params Document
	 * @return int
	 */
	public int getDurationValue(Document doc)
	{
		NodeList nodeList = doc.getElementsByTagName(KEY_DURATION);
		Node node = nodeList.item(0);
		NodeList nodeList2 = node.getChildNodes();
		Node node2 = nodeList2.item(getNodeIndex(nodeList2, KEY_DURATION_VALUE));
		Log.d("get duration val", node2.getTextContent());
		return Integer.parseInt(node2.getTextContent());
	}

	/*
	 * Method untuk mengambil distance text
	 * 
	 * @params Document
	 * @return String
	 */
	public String getDistanceText(Document doc)
	{
		NodeList nodeList = doc.getElementsByTagName(KEY_DISTANCE);
		Node node = nodeList.item(0);
		NodeList nodeList2 = node.getChildNodes();
		Node node2 = nodeList2.item(getNodeIndex(nodeList2, KEY_DISTANCE_TEXT));
		Log.d("get distance text", node2.getTextContent());
		return node2.getTextContent();

	}

	/*
	 * Method untuk mengambil distance value
	 * 
	 * @params Document
	 * @return int
	 */
	public int getDistanceValue(Document doc)
	{
		NodeList nodeList = doc.getElementsByTagName(KEY_DISTANCE);
		Node node = nodeList.item(0);
		NodeList nodeList2 = node.getChildNodes();
		Node node2 = nodeList2.item(getNodeIndex(nodeList2, KEY_DISTANCE_VALUE));
		Log.d("get distance value", node2.getTextContent());
		return Integer.parseInt(node2.getTextContent());
	}

	/*
	 * Method untuk mengambil start address 
	 * 
	 * @params Document
	 * @return String
	 */
	public String getStartAddress(Document doc)
	{
		NodeList nodeList = doc.getElementsByTagName(KEY_START_ADDRESS);
		Node node = nodeList.item(0);
		Log.d("get start address", node.getTextContent());
		return node.getTextContent();

	}


	/*
	 * Method untuk mengambil end address 
	 * 
	 * @params Document
	 * @return String
	 */
	public String getEndAddress(Document doc)
	{
		NodeList nodeList = doc.getElementsByTagName(KEY_END_ADDRESS);
		Node node = nodeList.item(0);
		Log.d("get end address", node.getTextContent());
		return node.getTextContent();

	}


	/*
	 * Method untuk mengambil direction 
	 * 
	 * @params Document
	 * @return ArrayList<LatLng>
	 */
	public ArrayList<LatLng> getDirection(Document doc)
	{
		NodeList nl_step, nl_start_location, nl_lat_lng_poly;
		ArrayList<LatLng> listLatLng = new ArrayList<LatLng>();

		nl_step = doc.getElementsByTagName(KEY_STEPS);
		if (nl_step.getLength() > 0)
		{
			for (int i = 0; i < nl_step.getLength(); i++)
			{
				Node node = nl_step.item(i);
				nl_start_location = node.getChildNodes();

				Node nodeStartLocation = nl_start_location.item(getNodeIndex(nl_start_location, KEY_START_LOCATION));
				nl_lat_lng_poly = nodeStartLocation.getChildNodes();
				Node nodeLat = nl_lat_lng_poly.item(getNodeIndex(nl_lat_lng_poly, KEY_LAT));
				double lat = Double.parseDouble(nodeLat.getTextContent());
				Node nodeLng = nl_lat_lng_poly.item(getNodeIndex(nl_lat_lng_poly, KEY_LNG));
				double lng = Double.parseDouble(nodeLng.getTextContent());
				listLatLng.add(new LatLng(lat, lng));

				nodeStartLocation = nl_start_location.item(getNodeIndex(nl_start_location, KEY_POLYLINE));
				nl_lat_lng_poly = nodeStartLocation.getChildNodes();
				nodeLat = nl_lat_lng_poly.item(getNodeIndex(nl_lat_lng_poly, KEY_POINTS));

				ArrayList<LatLng> arr = decodePoly(nodeLat.getTextContent());
				for (int j = 0; j < arr.size(); j++)
				{
					listLatLng.add(new LatLng(arr.get(j).latitude, arr.get(j).longitude));
				}

				nodeStartLocation = nl_start_location.item(getNodeIndex(nl_start_location, KEY_END_LOCATION));
				nl_lat_lng_poly = nodeStartLocation.getChildNodes();
				nodeLat = nl_lat_lng_poly.item(getNodeIndex(nl_lat_lng_poly, KEY_LAT));
				lat = Double.parseDouble(nodeLat.getTextContent());
				nodeLng = nl_lat_lng_poly.item(getNodeIndex(nl_lat_lng_poly, KEY_LNG));
				lng = Double.parseDouble(nodeLng.getTextContent());

				listLatLng.add(new LatLng(lat, lng));
			}
		}
		return listLatLng;
	}

	private int getNodeIndex(NodeList nodeList, String name)
	{
		for (int i = 0; i < nodeList.getLength(); i++)
		{
			if (nodeList.item(i).getNodeName().equals(name))
				return i;
		}
		return -1;
	}

	/*
	 * untuk decode polyline point example :
	 * czq~Flh{uO?tA?P?P?B@V@|J@fA?xA@xA?h@?B?F?vA@xD?h@?lB?n@@n@
	 * 
	 * lihat :
	 * http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google
	 * -maps-direction-api-with-java
	 */
	private ArrayList<LatLng> decodePoly(String encoded)
	{
		ArrayList<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;
		while (index < len)
		{
			int b, shift = 0, result = 0;
			do
			{
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;
			shift = 0;
			result = 0;
			do
			{
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
			poly.add(position);
		}
		return poly;

	}
}