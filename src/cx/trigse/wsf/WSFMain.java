package cx.trigse.wsf;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class WSFMain extends ListActivity {
	
	public static final int UPDATE_ID = Menu.FIRST;
	
	private static final int ACTIVITY_SCHED=0;
	private static final int ACTIVITY_DEST=1;
	private static final int ACTIVITY_THIS=2;
	
	private DocksDbAdapter mDockDbHelper;
	private SchedDbAdapter mSchedDbHelper;

	private Cursor mDocksCursor;
	
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.docks_list);
        mDockDbHelper = new DocksDbAdapter(this);
        mDockDbHelper.open();
        
        mSchedDbHelper = new SchedDbAdapter(this);
        mSchedDbHelper.open();
        
        fillDockList();
        
        // were we started by the user, or by ourselves?
        /*Intent i = getIntent();
        Bundle b = i.getExtras();
        Object dockId = b.get(DocksDbAdapter.KEY_ROWID);
        if (dockId == null) {
        	// we were not called by ourselves
        	fillDockList();
        } else {
        	// we were called by ourselves
        	Integer dId = (Integer)dockId;
        	
        }*/
        
        
    }
	
    private void fillDockList() {
        // Get all of the docks from the database and create the item list
        mDocksCursor = mDockDbHelper.fetchAllDocks();
        startManagingCursor(mDocksCursor);
        if (mDocksCursor.getCount() == 0) {
        	Updater u = new Updater(this, new Handler() {
        		@Override 
        		public void handleMessage(Message m) { 
        			Toast.makeText(getBaseContext(), m.getData().getString("text"), m.getData().getInt("time")).show(); 
        		} 
        	}, new Handler() {
        		@Override 
        		public void handleMessage(Message m) { 
        			fillDockList(); 
        		} 	
        	});
        	Thread t = new Thread(u);
        	t.start();
        	return;
        }

        String[] from = new String[] { DocksDbAdapter.KEY_NAME };
        int[] to = new int[] { R.id.text1 };
        
        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter docks =
            new SimpleCursorAdapter(this, R.layout.docks_row, mDocksCursor, from, to);
        setListAdapter(docks);
    }
    
    // Menu handling
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, UPDATE_ID, 0, R.string.menu_update);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
        case UPDATE_ID:
        	/*Updater upd = new Updater(this);
        	upd.updateData();*/
        	Updater u = new Updater(this, new Handler() {
        		@Override 
        		public void handleMessage(Message m) { 
        			Toast.makeText(getBaseContext(), m.getData().getString("text"), m.getData().getInt("time")).show(); 
        		} 
        	}, new Handler() {
        		@Override 
        		public void handleMessage(Message m) { 
        			fillDockList(); 
        		} 	
        	});
        	Thread t = new Thread(u);
        	t.start();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
        Cursor c = mDocksCursor;
        c.moveToPosition(position);
        //String dockName = c.getString(c.getColumnIndexOrThrow(DocksDbAdapter.KEY_NAME));
        int dockId = c.getInt(c.getColumnIndexOrThrow(DocksDbAdapter.KEY_ROWID));
        //Toast.makeText(getBaseContext(), dockName, Toast.LENGTH_SHORT).show();
        
        Intent i = new Intent(this, WSFMain.class);
        i.putExtra(DocksDbAdapter.KEY_ROWID, dockId);
        startActivity(i);
        
    }
}