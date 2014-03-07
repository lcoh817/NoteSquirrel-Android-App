package com.mhealthdigital.android.notesquirrel;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class PointCollector implements OnTouchListener {

	// declare an instance variable of interface type PointCollectorListener
	private PointCollecterListener listener;
	
	public static final int NUM_POINTS = 4;
	/*
	 * Declare a reference to point to a new instance of ArrayList holding Point
	 * objects. Android 'Point' class is a simple class that stores 2D integer
	 * co-ordinates
	 */
	private List<Point> points = new ArrayList<Point>();

	// This method will be called when the user touches the screen
	public boolean onTouch(View v, MotionEvent event) {

		// private List<Point>

		// Get the pixel coordinates of where was touched on mobile screen
		// Use Math.round to get integer pixel co-ords
		int x = Math.round(event.getX());
		int y = Math.round(event.getY());

		String message = String.format("Cordinates: (%d, %d)", x, y);

		Log.d(MainActivity.DEBUGTAG, message);
		
		points.add(new Point(x,y));

		if(points.size()==NUM_POINTS)
		{
			if(listener!=null)
			{
				/* call pointsCollected interface method (implemented in ImageActivity) 
				 * 
				 * Note: using the interface method as a way of decoupling PointsCollector class
				 * from the ImageActivity class
				 * 
				 * */
				listener.pointsCollected(points);
				
			}
			
		}
		
		return false;
	}

	public PointCollecterListener getListener() {
		return listener;
	}

	public void setListener(PointCollecterListener listener) {
		this.listener = listener;
	}
	
	public void clear()
	{
		// clear points from the list
		points.clear();
		
	}
	

}
