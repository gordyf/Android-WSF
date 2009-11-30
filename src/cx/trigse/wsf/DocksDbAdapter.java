package cx.trigse.wsf;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DocksDbAdapter {

	public static final String KEY_NAME = "name";
	public static final String KEY_ROWID = "_id";
	
	private static final String TAG = "DocksDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    private static final String DATABASE_NAME = "dockdata";
    private static final String DATABASE_TABLE = "docks";
    private static final int DATABASE_VERSION = 1;
    
    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table "+DATABASE_TABLE+" (_id integer primary key, "
                    + "name text not null);";



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
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
            onCreate(db);
        }
    }
    
    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public DocksDbAdapter(Context ctx) {
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
    public DocksDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }
    
    /**
     * Create a new dock using the id and name provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param id the id of the dock
     * @param name the name of the dock
     * @return rowId or -1 if failed
     */
    public long createDock(int id, String name) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_NAME, name);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }
    
    /**
     * Delete the dock with the given rowId
     * 
     * @param rowId id of dock to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteDock(long rowId) {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    
    public void deleteAllDocks() {
    	mDb.execSQL("DELETE FROM " + DATABASE_TABLE);
    }
    
    /**
     * Return a Cursor over the list of all docks in the database
     * 
     * @return Cursor over all docks
     */
    public Cursor fetchAllDocks() {
        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_NAME}, null, null, null, null, "name ASC");
    }
    
    /**
     * Return a Cursor positioned at the dock that matches the given rowId
     * 
     * @param rowId id of dock to retrieve
     * @return Cursor positioned to matching dock, if found
     * @throws SQLException if dock could not be found/retrieved
     */
    public Cursor fetchDock(long rowId) throws SQLException {
        Cursor mCursor =
                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                        KEY_NAME}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    /**
     * Update the dock using the details provided. The dock to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of dock to update
     * @param name value to set dock name to
     * @return true if the dock was successfully updated, false otherwise
     */
    public boolean updateDock(long rowId, String name) {
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, name);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
