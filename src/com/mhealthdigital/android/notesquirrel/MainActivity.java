package com.mhealthdigital.android.notesquirrel;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

// Everything in an android device is considered an activity. Such as displaying icons/apps on a screen, thats one activity.
public class MainActivity extends Activity {

	// Define a constant string called DEBUGTAG, that can be reused for
	// identifying debug messages
	public static final String DEBUGTAG = "LSC";

	// Define a string constant for text file name
	public static final String TEXTFILE = "notesquirrel.txt";

	// Define an arbitrary string constant to use as a key to save and retrieve
	// a boolean value
	public static final String FILESAVED = "FileSaved";

	// Define a string constant to use as key to pass boolean values between
	// activities
	public static final String RESET_PASSPOINTS = "ResetPasspoints";

	// Define a unique request code to identify returning from sub-activity for
	// taking photos for new
	// passpoints image
	private static final int PHOTO_TAKEN = 0;

	// Define a unique request code to identify returning from browsing the
	// image gallery (sub-activity)
	private static final int BROWSE_GALLERY_REQUEST = 1;

	// Define a string constant to use as a key to pass the path of photo taken
	// to the Image activity.
	public static final String RESET_IMAGE = "ResetImage";

	private Uri image;

	@Override
	// onCreate() method is called when the application is started for the first
	// time.
	// It contains alot of initilisation code to set everything up.
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * 'setContentView' will determine what the activity will display
		 * 'acivity_main' constant in R constant Class, is a way of ensure the
		 * contents of an xml file stored in the Res(resources folder) is
		 * displayed
		 */

		setContentView(R.layout.activity_main);

		// call method to listen and detect save button clicks & then write
		// contents of EditText view to a file.
		addSaveButtonListener();

		// call method to listen and detect lock button clicks and transition to
		// the lock button screen
		addLockButtonListener();

		// get preference file for storing application preferences
		// with private data (only accessible by the application)
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);

		// give default value of false if the preference does not exist (i.e
		// file has not been saved)
		boolean fileSaved = prefs.getBoolean(FILESAVED, false);
		//String photoPath = prefs.getString(RESET_IMAGE, null);

		if (fileSaved) {
			// Load the contents of TEXTFILE to the editText view
			loadSavedFile();
		}

	}

	/*
	 * This method invokes the Image activity, telling it to get the user to
	 * enter new passpoints. If an image uri is passed, the Image activity
	 * should also reset the image to the given image
	 */
	private void resetPasspoints(Uri image) {
		// Instantiate a new intent object with action to transition to the
		// image Activity
		Intent i = new Intent(this, ImageActivity.class);

		// Attach data representing the intention to reset the passpoints
		i.putExtra(RESET_PASSPOINTS, true);

		// Attach the path of the image to the intent
		i.putExtra(RESET_IMAGE, image.getPath());

		// Invoke the new activity
		startActivity(i);
	}

	/*
	 * Overide method in the Activity parent class
	 * 
	 * The menu item that was clicked is automatically passed in as an argument,
	 * 'item' (Note: can have as lots of menu items if needed)
	 */

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		/*
		 * Use a switch-case to identify which menu item was clicked/selected &
		 * & respond appropriately. Use the items id for identification
		 */
		switch (item.getItemId()) {
		case R.id.menu_passpoints_reset:

			// Handle passpoints menu item clicks here

			Intent i = new Intent(this, ImageActivity.class);

			/*
			 * Use the putExtra() method to add data to the intent object when
			 * transitioning between activities.
			 * 
			 * Arguments: 'name' (first arg) - represents a key/flag to the
			 * message/data 'value' - is the value of the message
			 */
			i.putExtra(RESET_PASSPOINTS, true);

			// get preference file for storing application preferences
			// with private data (only accessible by the application)
			SharedPreferences prefs = getPreferences(MODE_PRIVATE);

			// give default value of false if the preference does not exist (i.e
			// file has not been saved)
			String photoPath = prefs.getString(RESET_IMAGE, null);

			if (photoPath != null) {
				// Attach the path of the image to the intent
				i.putExtra(RESET_IMAGE, photoPath);

			}

			// Start the new activity (ImageActivity)
			startActivity(i);

			// Display a toast to show the functionality is working
			// (Toast.makeText(this, "Hello", Toast.LENGTH_LONG)).show();

			return true;

		case R.id.menu_replace_image:

			replaceImage();

		default:
			// if can't identify item clicked return super implementation of
			// method
			return super.onMenuItemSelected(featureId, item);

		}

	}

	/*
	 * Offer a choice of methods to replace the image in a dialog. The user can
	 * either take a photo or browse the gallery
	 */
	private void replaceImage() {

		// Instantiate new builder object for AlertDialog
		AlertDialog.Builder builder = new Builder(this);

		// Create/Inflate a new view object using replace_image layout xml file
		View v = getLayoutInflater().inflate(R.layout.replace_image, null);

		// Set title of builder object
		builder.setTitle(R.string.replace_lock_image);

		// set the icon for the dialog
		builder.setIcon(R.drawable.ic_dialog_info);

		// Set the view of builder using view object created
		builder.setView(v);

		// Create the final AlertDialog from the builder
		final AlertDialog dlg = builder.create();

		// Display/show the dialog
		dlg.show();

		// Get a handle on take photo and browse gallery buttons
		Button take_photo_btn = (Button) dlg.findViewById(R.id.take_photo);
		Button browse_gallery_btn = (Button) dlg
				.findViewById(R.id.browse_gallery);

		// Define a click listener for take photo button
		take_photo_btn.setOnClickListener(new OnClickListener() {

			@Override
			// Call takePhoto() when button clicked
			public void onClick(View v) {

				takePhoto();

			}
		});

		browse_gallery_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				browseGallery();

			}
		});

	}

	// This method invokes the browse gallery action
	private void browseGallery() {

		/*
		 * Using explicit intent method
		 * 
		 * 2nd argument specifies the actual activity to run
		 * 
		 * This intent will run the gallery action, and then return a uri, which
		 * can be used an a unique identifier for the image selected.
		 */
		Intent i = new Intent(Intent.ACTION_PICK,
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

		startActivityForResult(i, BROWSE_GALLERY_REQUEST);

	}

	// This method invokes the camera sub-activity
	private void takePhoto() {

		// Get a pointer/reference for a public directory on external storage(SD
		// card) for storing pictures
		File pictureDirectory = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

		// Create new file object for storing images in the pictures directory
		File imageFile = new File(pictureDirectory, "passpoints_image");

		/*
		 * Initialise an intent object with an intent action to capture an image
		 * (within a sub-activity) and then return it
		 * 
		 * Note, before we do this we need to specify in the Android Manifest
		 * file that we need to use the camera hardware feature
		 */
		Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		image = Uri.fromFile(imageFile);

		// Tell the sub-activity (which captures an image) to save the photo to
		// the given file (it will also be added to the gallery)
		i.putExtra(MediaStore.EXTRA_OUTPUT, image);

		// Invoke the sub-activity identified by a request code key
		// (PHOTO_TAKEN)
		startActivityForResult(i, PHOTO_TAKEN);

	}

	/*
	 * This method is automatically invoked upon returning from an activity by
	 * invoked by startActivityForResult. We check the requestCode to see which
	 * activity we returned from
	 */

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {

		if (requestCode == BROWSE_GALLERY_REQUEST) {

			// Create an array of strings (columns) containing required image
			// data stored on disk.
			String[] columns = { MediaStore.Images.Media.DATA };

			// Get the uri of the gallery image selected
			Uri imageUri = intent.getData();

			/*
			 * Query the image gallery - returning a cursor object (a pointer
			 * into a set of results - always pointing before the first result )
			 */
			Cursor cursor = getContentResolver().query(imageUri, columns, null,
					null, null);

			// Point to first result
			cursor.moveToFirst();

			// Get the index of the column in the results
			int columnIndex = cursor.getColumnIndex(columns[0]);

			// get the path of the image (what camera action returned)
			String imagePath = cursor.getString(columnIndex);

			// Always remember to close cursor
			cursor.close();

			// Creates a Uri which parses the given encoded URI string.
			image = Uri.parse(imagePath);

		}

		if (image == null) {
			(Toast.makeText(this, R.string.unable_to_display_image,
					Toast.LENGTH_LONG)).show();

			return;
		}

		Log.d(DEBUGTAG, "Photo is stored at : " + image.getPath());

		SharedPreferences prefs = getPreferences(MODE_PRIVATE);

		// Create an editor for preference object thru which can make
		// modifications
		SharedPreferences.Editor editor = prefs.edit();

		// set a boolean flag to indicate that the password has been
		// reset
		editor.putString(RESET_IMAGE, image.getPath());
		// Commit preferences back from the editor to the
		// SharedPreferences object
		editor.commit();

		resetPasspoints(image);
	}

	private void loadSavedFile() {
		try {
			FileInputStream fis = openFileInput(TEXTFILE);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new DataInputStream(fis)));

			EditText editText = (EditText) findViewById(R.id.text);

			String line;

			// Read file line by line & add/append to editText view
			while ((line = reader.readLine()) != null) {
				editText.append(line);
				editText.append("\n");

			}

			// close the fileInputStream;
			fis.close();

		} catch (Exception e) {

			Log.d(DEBUGTAG, "Unable to read file");
		}

	}

	// Create a method to listen for and detect 'Save' button clicks and save
	// the contents of the Edit text to a text file
	private void addSaveButtonListener() {

		/*
		 * use findViewById() method to find/get-a-handle on the save button &
		 * refer to it by its id stored in R class
		 */
		Button saveBtn = (Button) findViewById(R.id.save);

		// Passing an instances of the OnClickListener interface as an annoymous
		// class (declaring and instantiate class at the same time) to the
		// setOnClickListener method when the
		// save button is clicked/
		saveBtn.setOnClickListener(new View.OnClickListener() {

			// Define some code that is ran when ever the button is clicked.
			@Override
			public void onClick(View v) {

				saveText();
			}
		});

	}
	
	
	/* On pause() method implemented on the way to the activity being stopped or destroyed,
	 * as part of the activity lifecyle */
	
	
	@Override
	protected void onPause() {
		
		// Important to maintain onPause of super class (Activity)
		super.onPause();
		
		/*  By adding saveText() to onPause() the application will try to save its text 
		 *  whenever it goes into pause mode (Eg: application being shutdown, no longer in the 
		 *  foreground because of some system dialog coming up, or it is stopped because the user
		 *  is changing to another program )
		 *  
		 * */
		 saveText();
		
	}

	private void saveText() {

		// assign editText to the return value of findViewId() to get a
		// handle on editText view/widget
		EditText editText = (EditText) findViewById(R.id.text);

		// getTex() method returns something that implements an editable
		// interface, need to run toString() on it to get the actual
		// string within the edit text box.
		String text = editText.getText().toString();

		// Save text to a file

		try {

			FileOutputStream fos = openFileOutput(TEXTFILE,
					Context.MODE_PRIVATE);
			fos.write(text.getBytes());
			fos.close();

			// get preference file for storing application preferences
			// with private data (only accessible by the application)
			SharedPreferences prefs = getPreferences(MODE_PRIVATE);

			// Edit preference file using edit() which returns an Editor
			// object
			SharedPreferences.Editor editor = prefs.edit();

			// Put the preference pair to the preference file via the
			// editor
			editor.putBoolean(FILESAVED, true);

			// commit() saves the values that are put to the editor
			// operating on the SharedPreferences file
			editor.commit();

			// Return and show a Toast object which contains a text view
			// for displaying error message
			// (Toast.makeText(MainActivity.this,
			// getString(R.string.toast_cant_save),
			// Toast.LENGTH_LONG)).show();

		} catch (Exception e) {

			(Toast.makeText(MainActivity.this,getString(R.string.toast_cant_save), Toast.LENGTH_LONG)).show();

		}

	}

	private void addLockButtonListener() {

		/*
		 * use findViewById() method to find/get-a-handle on the lock button &
		 * refer to it by its id stored in R class
		 */
		Button lockBtn = (Button) findViewById(R.id.lock);

		// Passing an instances of the OnClickListener interface as an annoymous
		// class (declaring and instantiate class at the same time) to the
		// setOnClickListener method when the
		// lock button is clicked/
		lockBtn.setOnClickListener(new View.OnClickListener() {

			// Define some code that is ran when ever the lock button is
			// clicked.
			@Override
			public void onClick(View v) {

				// Create a new intent object to signal to the OS the intention
				// to transition to the new activity (ImageActivity)
				Intent i = new Intent(MainActivity.this, ImageActivity.class);

				// get preference file for storing application preferences
				// with private data (only accessible by the application)
				SharedPreferences prefs = getPreferences(MODE_PRIVATE);

				// give default value of false if the preference does not exist
				// (i.e
				// file has not been saved)
				String photoPath = prefs.getString(RESET_IMAGE, null);

				if (photoPath != null) {
					// Attach the path of the image to the intent
					i.putExtra(RESET_IMAGE, photoPath);

				}

				// Trigger the intent to launch the new activity (ImageActivity
				// - lock screen)
				startActivity(i);

			}

		});

	}

	/*
	 * Activities by default have the onCreateOptionsMenu() method, which
	 * overides the same method in the parent.
	 * 
	 * A blank options menu is passed in when the menu button is clicked.
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/*
		 * 
		 * The getMenuInflater() takes the xml definition of the menu
		 * (referenced in R.menu) and use it to configure the UI to create an
		 * options menu.
		 * 
		 * Note, the Android lingo for the action of taking an xml file ref and
		 * turning into a UI widget is called 'inflate'
		 */
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
