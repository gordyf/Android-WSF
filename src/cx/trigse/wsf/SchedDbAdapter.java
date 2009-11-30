package cx.trigse.wsf;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SchedDbAdapter {

	//public static final String KEY_NAME = "name";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_STARTDOCK = "startDock";
	public static final String KEY_ENDDOCK = "endDock";
	public static final String KEY_STARTTIME = "startTime";
	public static final String KEY_NOTES = "notes";
	
	private static final String TAG = "SchedDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    private static final String DATABASE_NAME = "rundata";
    private static final String DATABASE_TABLE = "runs";
    private static final int DATABASE_VERSION = 1;
    
    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table "+ DATABASE_TABLE +" (_id integer primary key autoincrement, "
                    + "startDock integer not null, "
                    + "endDock integer not null, "
                    + "startTime text not null, "
                    + "notes text not null);";

    

    private final Context mCtx;
	
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }
    
    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public SchedDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }
    
    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public SchedDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }
    
    /**
     * Create a new run using the content provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param id the id of the dock
     * @param startDock the starting dock
     * @param endDock the ending dock
     * @param startTime the starting time
     * @param notes any notes, if applicable
     * @return rowId or -1 if failed
     */
    public long createRun(int startDock, int endDock, String startTime, String notes) {
        ContentValues initialValues = new ContentValues();
        //initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_STARTDOCK, startDock);
        initialValues.put(KEY_STARTDOCK, endDock);
        initialValues.put(KEY_STARTTIME, startTime);
        initialValues.put(KEY_NOTES, notes);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }
    
    /**
     * Delete the run with the given rowId
     * 
     * @param rowId id of run to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteRun(long rowId) {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    
    public void deleteAllRuns() {
    	mDb.execSQL("DELETE FROM " + DATABASE_TABLE);
    }
    
    /**
     * Return a Cursor over the list of all runs in the database
     * 
     * @return Cursor over all runs
     */
    public Cursor fetchAllRuns() {
        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_STARTDOCK, KEY_ENDDOCK, KEY_STARTTIME, KEY_NOTES}, null, null, null, null, "name ASC");
    }
    
    /**
     * Return a Cursor positioned at the run that matches the given rowId
     * 
     * @param rowId id of run to retrieve
     * @return Cursor positioned to matching run, if found
     * @throws SQLException if run could not be found/retrieved
     */
    public Cursor fetchRun(long rowId) throws SQLException {
        Cursor mCursor =
                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_STARTDOCK, KEY_ENDDOCK, KEY_STARTTIME, KEY_NOTES}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    /**
     * Update the run using the details provided. The run to be updated is
     * specified using the rowId, and it is altered to use the content
     * values passed in
     * 
     * @param rowId id of run to update
     * @param startDock the starting dock
     * @param endDock the ending dock
     * @param startTime the starting time
     * @param notes any notes, if applicable
     * @return true if the run was successfully updated, false otherwise
     */
    public boolean updateRun(long rowId, int startDock, int endDock, String startTime, String notes) {
        ContentValues args = new ContentValues();
        args.put(KEY_STARTDOCK, startDock);
        args.put(KEY_STARTDOCK, endDock);
        args.put(KEY_STARTTIME, startTime);
        args.put(KEY_NOTES, notes);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
