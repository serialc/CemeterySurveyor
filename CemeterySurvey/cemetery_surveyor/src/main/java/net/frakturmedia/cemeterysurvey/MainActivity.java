package net.frakturmedia.cemeterysurvey;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

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

            // Check if directory structure exists, create if not
            new fileStructureBuilder().execute(new String[]{
                    Utility.dataPaths.TEMPLATE_ARCHIVE,
                    Utility.dataPaths.THUMBNAILS,
                    Utility.dataPaths.THUMBNAILS_SMALL,
                    Utility.dataPaths.PICTURES,
                    Utility.dataPaths.DATA_EXPORT
            });

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
