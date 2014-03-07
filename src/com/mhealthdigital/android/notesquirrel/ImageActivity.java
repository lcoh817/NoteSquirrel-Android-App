package com.mhealthdigital.android.notesquirrel;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageActivity extends Activity implements PointCollecterListener {

	// Define a string constant to be used a key to access preference data
	private static final String PASSWORD_SET = "PASSWORD_SET";

	// Define constant for maximum closeness (in terms of pixels) between points
	private static final int POINT_CLOSENESS = 40;

	// instantiate a new object of type PointCollector for onClickListener
	private PointCollector pointCollector = new PointCollector();

	// instantiate a new object of type Database, with a constructor reference
	// to 'this' activity
	private Database db = new Database(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set XML file that will determine how this activity looks
		setContentView(R.layout.activity_image);

		addTouchListener();

		/*
		 * getIntent gets the intent that started the activity getExtra's
		 * returns a bundle of data (stored by keys) from the intent
		 */
		Bundle extras = getIntent().getExtras();

		/*
		 * Check if data was attached by the original intent using putExtra()If
		 * data was attached then the bundle object returned will not be null
		 */
		if (extras != null) {
			// get the boolean value in the bundle of data (extras) referenced
			// by key RESET_PASSPOINTS (defined in MainActivity)
			// the resetPasspoints boolean variable represents a flag as to
			// wheather the passpoint reset should or should not occur
			Boolean resetPasspoints = extras
					.getBoolean(MainActivity.RESET_PASSPOINTS);

			if (resetPasspoints) {
				// Reset the passspoints here
				SharedPreferences prefs = getPreferences(MODE_PRIVATE);

				// Create an editor for preference object thru which can make
				// modifications
				SharedPreferences.Editor editor = prefs.edit();

				// set a boolean flag to indicate that the password has been
				// reset
				editor.putBoolean(PASSWORD_SET, false);
				// Commit preferences back from the editor to the
				// SharedPreferences object
				editor.commit();
			}

			// Get the path of image from extra's bundle
			String imagePath = extras.getString(MainActivity.RESET_IMAGE);

			changeViewImage(imagePath);

		}

		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		Boolean passpointSet = prefs.getBoolean(PASSWORD_SET, false);

		if (!passpointSet) {
			showSetPasspointsPrompt();

		}
		else 
		{

			AlertDialog.Builder builder = new Builder(this);

			// use setPositiveButton to show an OK button, or setNegativeButton
			// to
			// show a Cancel button
			builder.setPositiveButton("OK", new OnClickListener() {

				// This method runs when the user clicks the "OK" button
				public void onClick(DialogInterface dialog, int which) {

				}
			});

			builder.setMessage("You can login by re-entering your passpoints.");

			final AlertDialog dlg = builder.create();

			dlg.show();

		}


		/*
		 * On the pointCollector touch listener we are calling setListener &
		 * passing in ImageActivity ('this') - we can do this because
		 * ImageActivity implements the PointCollectorListener interface
		 */

		pointCollector.setListener(this);

	}

	private void changeViewImage(String image_path) {

		// Get a handle on our image to display the photo
		ImageView imageView = (ImageView) findViewById(R.id.touch_image);

		if (image_path == null) {

			// The drawable resource (default passpoint image) referenced by id
			Drawable image = getResources().getDrawable(R.drawable.car);

			// Set the content of image view to be drawable resource
			imageView.setImageDrawable(image);

		} else {
			// Set the content of the image view to be an image at a given URI
			imageView.setImageURI(Uri.parse(image_path));
		}

	}

	private void showSetPasspointsPrompt() {

		AlertDialog.Builder builder = new Builder(this);

		// use setPositiveButton to show an OK button, or setNegativeButton to
		// show a Cancel button
		builder.setPositiveButton("OK", new OnClickListener() {

			// This method runs when the user clicks the "OK" button
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

			}
		});

		builder.setTitle("Create Your Passpoint Sequence");

		builder.setMessage("Touch four points on the image to set the passpoint sequence. You must click the same points in the future to gain access to your notes.");

		AlertDialog dlg = builder.create();

		dlg.show();

	}

	private void addTouchListener() {

		
		// Get a handle on ImageView view using its id
		ImageView image = (ImageView) findViewById(R.id.touch_image);

		image.setOnTouchListener(pointCollector);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image, menu);
		return true;
	}

	// This method saves set points to the database
	private void savePasspoints(final List<Point> points) {

		Log.d(MainActivity.DEBUGTAG, "Collected points:" + points.size());

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage(R.string.storing_data);

		final AlertDialog dlg = builder.create();

		/*
		 * To show a dialogue requires calling show() & then letting the GUI
		 * system - that is: the thread thats running the user interface,
		 * process the dialogue.
		 * 
		 * So can't call show and dismiss in same thread else the diaglo box
		 * wont be displayed.
		 */
		dlg.show();

		/*
		 * Use AsyncTask is a parameterised class to create a background task
		 * that runs in parallel with main thread of execution
		 * 
		 * Set arguments to AsyncTask to avoid as this task does not operate
		 * with Parameter, Progress or Result types
		 * 
		 * AsyncTask is an abstract class so needs to include the annoymous
		 * class definition, which includes abstract class unimplemented method.
		 */
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {

				try {
					// put thread to sleep (idle) for 2 sec so we can definately
					// see the alert dialog displayed
					Thread.sleep(2000);
				} catch (InterruptedException e) {

				}

				// store list of points in the database

				db.storePoints(points);

				Log.d(MainActivity.DEBUGTAG, "Points saved: " + points.size());
				return null;
			}

			// this method runs after your background task has finished
			// executing

			@Override
			protected void onPostExecute(Void result) {

				// returns a SharePrefrences object for accessing prefrences
				SharedPreferences prefs = getPreferences(MODE_PRIVATE);

				// Create an editor for preference object thru which can make
				// modifications
				SharedPreferences.Editor editor = prefs.edit();

				// set a boolean flag to indicate that the password has been set
				editor.putBoolean(PASSWORD_SET, true);
				// Commit preferences back from the editor to the
				// SharedPreferences object
				editor.commit();

				// clear the points collected list
				pointCollector.clear();
				dlg.dismiss();

			}

		};

		// Execute the background task
		task.execute();

	}

	/*
	 * This method checks entered passpoints against those stored in the
	 * database & uses this comparison to decide whether to let user into the
	 * application or not
	 */
	private void verifyPasspoints(final List<Point> touchPoints) {

		AlertDialog.Builder builder = new Builder(this);

		builder.setMessage("Checking passpoints...");

		final AlertDialog dlg = builder.create();

		dlg.show();

		/*
		 * Specify Result type of Asynctask as 'Boolean' This allows you to
		 * return Booleans values back from the background task into the main
		 * application thread
		 */
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

			@Override
			// Note: cant change the GUI in the background task
			protected Boolean doInBackground(Void... params) {

				try {
					// put thread to sleep (idle) for2 sec so we can definately
					// see the alert dialog displayed
					Thread.sleep(2000);
				} catch (InterruptedException e) {

				}

				// Get the points saved in the database previously
				List<Point> savedPoints = db.getPoints();

				Log.d(MainActivity.DEBUGTAG,
						"Loaded points: " + savedPoints.size());

				// Check if number of points = 4 for both touchpoints and
				// savepoints lists

				if (savedPoints.size() != PointCollector.NUM_POINTS
						|| touchPoints.size() != PointCollector.NUM_POINTS) {
					return false;

				}

				// Loop through all the touchPoints and check their distance
				// from
				// the savedPoints
				for (int i = 0; i < PointCollector.NUM_POINTS; i++) {

					// get the corresponding point at index i for each ArrayList
					// of points
					Point savedPoint = savedPoints.get(i);
					Point touchedPoint = touchPoints.get(i);

					// Calculuate horizontal distance between saved and touched
					// point
					int xDiff = savedPoint.x - touchedPoint.x;

					// Calculate vertical distance between saved and touched
					// point
					int yDiff = savedPoint.y - touchedPoint.y;

					// get the distance between the two points using
					// trigenometry
					int distSquared = xDiff * xDiff + yDiff * yDiff;

					Log.d(MainActivity.DEBUGTAG, "Distance squared: "
							+ distSquared);

					// comparing squared distances to see if points are within
					// required range
					if (distSquared > POINT_CLOSENESS * POINT_CLOSENESS) {
						return false; // if any two points are too far away, so
										// return false

					}
				}

				return true; // return true to indicate that touched points are
								// close to saved points
			}

			/*
			 * Whatever is returned from doInBackground (boolean value) is
			 * passed in as the argument of onPostExecute().
			 * 
			 * Note: Can change the GUI in the onPostExecute method()
			 */

			@Override
			protected void onPostExecute(Boolean pass) {

				dlg.dismiss();
				pointCollector.clear();

				if (pass == true) {
					/*
					 * An intent is a way of telling a mobile phone OS you
					 * want/intend to do something. They can be very specific or
					 * general (let the phone figure out what do).
					 * 
					 * An intent is a passive data structure holding abstract
					 * description of an action to be performed (transition
					 * between activities).
					 * 
					 * Here we we create an intent object (called i) and use a
					 * particular constructor with references to the activities.
					 */
					Intent i = new Intent(ImageActivity.this,
							MainActivity.class);

					/*
					 * Launch new activity, which is the MainActivity
					 * startActivity is a method of the Activity class that
					 * triggers an intent
					 */
					startActivity(i);
				} else {

					(Toast.makeText(ImageActivity.this, "Access Denied",
							Toast.LENGTH_LONG)).show();
				}

			}

		};

		task.execute();

	}

	@Override
	// This method is called when 4 touch co-ord points are collected
	public void pointsCollected(final List<Point> points) {

		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		Boolean passpointSet = prefs.getBoolean(PASSWORD_SET, false);

		// If preference flag not set to true, save points in the database
		if (!passpointSet) {
			Log.d(MainActivity.DEBUGTAG, "Saving passpoints...");
			savePasspoints(points);

			// otherwise verify collected points against saved points in
			// database
		} else {
			Log.d(MainActivity.DEBUGTAG, "Verifying passpoints...");
			verifyPasspoints(points);

		}

		/*
		 * Call getPoints() on the database to return an ArrayList object
		 * containing the result of of the SQL query on the database
		 */
		// List<Point> list = db.getPoints();

		// Use a for-each loop to go through the list of points & display them
		// using the LogCat debug output
		/*
		 * for (Point point : list) {
		 * 
		 * Log.d(MainActivity.DEBUGTAG, String.format("Got point: (%d, %d)",
		 * point.x, point.y));
		 * 
		 * }
		 */
	}

}
