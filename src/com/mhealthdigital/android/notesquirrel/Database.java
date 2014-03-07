package com.mhealthdigital.android.notesquirrel;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Point;

// Create a Class which contains all database functionality 

// SQLiteOpenHelper is an abstract - therefore child class (Database) has to implement its methods (which don't have any implementation within SQLiteOpenHelper)

public class Database extends SQLiteOpenHelper {

	/*
	 * Create a string constant to hold the name of our database table Note: we
	 * wouldn't use a resource string because these are things that are seen
	 * visually
	 */
	private static final String POINTS_TABLE = "POINTS";

	// Make our database column names as string constants so not hardcoding
	// names in sql command string
	private static final String COL_ID = "ID";
	private static final String COL_X = "X";
	private static final String COL_Y = "Y";

	public Database(Context context) {

		/* Use the super() to invoke the parent/super-classes constructor */
		/*
		 * First argument - context: represents the activity that the database
		 * is used from Second argument - name: is the name of the database
		 * file. SQLite stores databases in files with '.db' extension. This
		 * name does not need to be unique because it is private to this
		 * application. Third argument - dont bother - normally set to null
		 * Fourth argument - version : SQLiteOpenHelper has automatic facilities
		 * to upgrade versions of database
		 */

		super(context, "notes.db", null, 1);
		// TODO Auto-generated constructor stub
	}

	// use this method to execute some SQL to create database.
	// This method gets called automatically when use/do-something with the
	// database in an activity
	public void onCreate(SQLiteDatabase db) {

		/*
		 * sql represents an SQL command string code to setup the database table
		 * to hold POINTS (with x and y co-ords and primary key ID
		 */
		String sql = String
				.format("create table %s (%s INTEGER PRIMARY KEY,%s INTEGER NOT NULL, %s INTEGER NOT NULL)",
						POINTS_TABLE, COL_ID, COL_X, COL_Y);

		// call execSQL with sql command string input to setup a database table
		// to store data
		db.execSQL(sql);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	// Create a method to store a list of Android point objects in the newly
	// created database
	public void storePoints(List<Point> points) {

		/*
		 * We need to get a handle on the database that will allow us to write
		 * to the db. getWritableDatabase() opens the database (calling
		 * OnCreate()) and returns a pointer to writable SQLite database object.
		 */
		SQLiteDatabase db = getWritableDatabase();

		/*
		 * Delete all the values in existing database table. Don't use 'WHERE'
		 * clause so set argument to null because we want to delete all values
		 * in POINTS_TABLE
		 */
		db.delete(POINTS_TABLE, null, null);
		
		int i = 0;

		// use a for-each loop to loop through the points
		for (Point point : points) {
			/*
			 * ContentValues object is part of a mechanism that allows us to
			 * write the database in such a way using wildcards. This creates a
			 * new empty set of values
			 */
			ContentValues values = new ContentValues();

			/*use put() to supply a value to a db column (referenced by key - 
			 * the first argument) */
			values.put(COL_ID,i);
			values.put(COL_X,point.x);
			values.put(COL_Y,point.y);
			
			// Insert a row of values into the database
			/*
			 * Parameter List:
			 * table - name of db table to insert the row into
			 * nullColumnHack - allows you to insert column values that are empty (null)
			 * 
			 *
			 */
			db.insert(POINTS_TABLE, null, values);
			
			i++;

		}

		// close the database handle using close()
		db.close();

	}
	
	// Method for retrieving/reading points from the database
	public List<Point> getPoints(){
		
		List<Point> points = new ArrayList<Point>();
		
		/* getReadableDatabsae returns a pointer to get a handle on readible/writable database 
		 * that was previously created. This returns the same object returned by getWritableDatabase()
		 */
		SQLiteDatabase db = getReadableDatabase();
		
		/*
		 *  Create a string containing SQL to query/retrieve data from the database
		 *  
		 *  This SQL query selects all the rows of data (for the COL X & COL Y columns) from the table POINTS_TABLE & 
		 *  order these rows by column ID (COL_ID) - hence all points will be retrieved in the 
		 *  order that they were entered.
		 */
		
		String sql = String.format("SELECT %s,%s FROM %s ORDER BY %s",COL_X,COL_Y, POINTS_TABLE,COL_ID);
		
		/*  Run the SQL query and return a CURSOR on the result set. No WHERE clause, so set selectionArgs to null.
		   
		    Cursor is a interface that points to the result of an SQL query to gain read/write access to the result set.
		    When the query is first run the cursor returned points to before the first result.
		 */
		Cursor cursor = db.rawQuery(sql,null);
	
		/* moveToNext() moves to the next row - and returns false when its past 
		 * the last entry in the result set */
		while(cursor.moveToNext())
		{
			/* columnIndex argument to getInt() starts from 0 (first col within result set
			 * . So get x co-ord from 0 col index & y co-ord from 1 col index.
			 */
			int x = cursor.getInt(0);
			int y = cursor.getInt(1);
			
			points.add(new Point(x,y));
		
		}
		
		db.close();
		
		// return ArrayList object 'points'
		return points;
		
	}

}
