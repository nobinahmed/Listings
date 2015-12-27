package com.nobin.swipetest;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.adapter.ListViewAdapter;
import com.nobin.swipetest.db.TaskContract;
import com.nobin.swipetest.db.TaskDBHelper;

import java.util.ArrayList;
import java.util.List;

public class ListViewActivity extends AppCompatActivity {
    private ListAdapter listAdapter;
    private TaskDBHelper helper;
   public ListView myList;
   


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view_activity);
        myList = (ListView) findViewById(R.id.list_view);
    //    ListView mylist = (ListView) findViewById(R.id.list_view);

        updateUI();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addList();
            }
        });
        ////////////////////////////////////////Swipe Delete////////////////////////////////
        final SwipeToDismissTouchListener<ListViewAdapter> touchListener =
                new SwipeToDismissTouchListener<>(
                        new ListViewAdapter(myList),
                        new SwipeToDismissTouchListener.DismissCallbacks<ListViewAdapter>() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }
                            @Override
                            public void onDismiss(ListViewAdapter view, int position) {
                                //listAdapter.remove(position);
                                //View v = myList.getChildAt(position);
                                //View v = view.getChildAt(position);
                                //TextView taskTextView = (TextView) v.findViewById(R.id.txt_data);
                                //String task = taskTextView.getText().toString();
                                //String task = task.getText().toString();
                                //Toast.makeText(ListViewActivity.this, "Position " + task, Toast.LENGTH_SHORT).show();
                                //View v = myList.getChildAt(position);
                                View v = myList.getChildAt(position - myList .getFirstVisiblePosition());
                                String task= ((TextView)v.findViewById(R.id.txt_data)).getText().toString();

                                String sql = String.format("DELETE FROM %s WHERE %s = '%s'",
                                        TaskContract.TABLE,
                                        TaskContract.Columns.TASK,
                                        task);

                                helper = new TaskDBHelper(ListViewActivity.this);
                                SQLiteDatabase sqlDB = helper.getWritableDatabase();
                                sqlDB.execSQL(sql);
                                updateUI();
                            }

                        });
        myList.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        myList.setOnScrollListener((AbsListView.OnScrollListener) touchListener.makeScrollListener());
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (touchListener.existPendingDismisses()) {
                    touchListener.undoPendingDismiss();
                } else {

                }
            }

        });
        ////////////////////////////////////Swipe Delete//////////////////////////////////////
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ListViewActivity.this);

                builder.setMessage("Developer: \nNobin Ahmed\n"
                        + "Version: 1.0")
                        .setTitle("About");

                android.app.AlertDialog dialog = builder.create();
                dialog.show();
                return true;

            default:
                return false;
        }

    }

    private void updateUI() {


        helper = new TaskDBHelper(ListViewActivity.this);
        SQLiteDatabase sqlDB = helper.getReadableDatabase();
        Cursor cursor = sqlDB.query(TaskContract.TABLE,
                new String[]{TaskContract.Columns._ID, TaskContract.Columns.TASK},
                null, null, null, null, null);

        listAdapter = new SimpleCursorAdapter(
                this,
                R.layout.list_item,
                cursor,
                new String[]{TaskContract.Columns.TASK},
                new int[]{R.id.txt_data},
                0

        );

        myList.setAdapter(listAdapter);
    }


    public void addList() {
        final EditText inputField = new EditText(this);

        // Now create the Dialog itself.
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Add Task")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String task = inputField.getText().toString();

                        helper = new TaskDBHelper(ListViewActivity.this);
                        SQLiteDatabase db = helper.getWritableDatabase();
                        ContentValues values = new ContentValues();

                        values.clear();
                        values.put(TaskContract.Columns.TASK, task);

                        db.insertWithOnConflict(TaskContract.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                        updateUI();
                    }


                }).setCancelable(true).setView(inputField)
                .setNegativeButton("Cancel", null).setCancelable(true).setView(inputField)

                .create();

        // The TextWatcher will look for changes to the Dialogs field.
        inputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence c, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence c, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Will be called AFTER text has been changed.
                if (editable.toString().length() == 0) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });


        // Show the Dialog:
        dialog.show();

        // The button is initially deactivated, as the field is initially empty:
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

}




