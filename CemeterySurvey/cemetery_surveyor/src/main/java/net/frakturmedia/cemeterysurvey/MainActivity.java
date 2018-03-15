package net.frakturmedia.cemeterysurvey;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import net.frakturmedia.cemeterysurvey.data.CsDbContract;


public class MainActivity extends ScopeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Initialize ScopeActivity variables
        mScope = CsDbContract.PATH_MAIN;
        mFragmentTag = "main_fragment";

        // load the content
        setContentView(R.layout.activity_main);
        mProgressBarWheel = (RelativeLayout) findViewById(R.id.json_loading_screen);

        // setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // initialize some items
        mFab = (FloatingActionButton) findViewById(R.id.fab);

        // The new cemetery floating button onclick handler
        mFab.setOnClickListener(new fabClick());

        if( savedInstanceState != null ) {
            Log.d(LOG_TAG, "Saved instance state= " + savedInstanceState.toString());
            mViewingState = savedInstanceState.getString(super.VIEWING_STATE);
        } else {
            // Do only on first load, and not for tablet rotations

            mViewingState = "cemeteries";

            // See if we have permission to write to disk and then create dir structure if needed
            // Check if we have permission to write to device (required for Android 6 and greater)
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                Log.d(LOG_TAG, "Permission has not yet been granted to write to external storage!");

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            } else {
                // Permission has already been granted

                // Check if directory structures exist or create if not
                new fileStructureBuilder().execute(new String[]{
                        Utility.dataPaths.TEMPLATE_ARCHIVE,
                        Utility.dataPaths.THUMBNAILS,
                        Utility.dataPaths.THUMBNAILS_SMALL,
                        Utility.dataPaths.PICTURES,
                        Utility.dataPaths.DATA_EXPORT
                });
            }

            // parse the JSON template
            showLoadingScreen(true);
            new templateFileParser().execute();
        }

        // Initialize left menu buttons
        Button buttonViewCemeteries = (Button) findViewById(R.id.button_main_display_cemeteries);
        ImageButton imageButtonViewBookmarks = (ImageButton) findViewById(R.id.imagebutton_display_bookmarks);
        buttonViewCemeteries.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                displayCemeteries(v);
            }
        });
        imageButtonViewBookmarks.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                displayBookmarks(v);
            }
        });


        // Display the content accordingly
        resetCurrentView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the task you need to do.
                    // Check if directory structures exist or create if not
                    new fileStructureBuilder().execute(new String[]{
                            Utility.dataPaths.TEMPLATE_ARCHIVE,
                            Utility.dataPaths.THUMBNAILS,
                            Utility.dataPaths.THUMBNAILS_SMALL,
                            Utility.dataPaths.PICTURES,
                            Utility.dataPaths.DATA_EXPORT
                    });
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(this, "Cemetery Surveyor cannot function without this permission!", Toast.LENGTH_SHORT);
                }
                return;
            }

            case 2: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do nothing, they must use the camera button again :(

                } else {
                    // permission denied, boo!
                    Toast.makeText(this, "Camera must have permission to take pictures!", Toast.LENGTH_SHORT);
                }

                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
