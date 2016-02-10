package net.frakturmedia.cemeterysurvey;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;

import net.frakturmedia.cemeterysurvey.data.CsDbContract;

public class MainActivity extends ScopeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if( savedInstanceState != null ) {
            mViewingState = savedInstanceState.getString(super.VIEWING_STATE);
        } else {
            // Do only on first load, and not for tablet rotations

            mViewingState = "cemeteries";

            // Check if directory structure exists, create if not
            new fileStructureBuilder().execute(new String[]{
                    Utility.dataPaths.TEMPLATE_ARCHIVE,
                    Utility.dataPaths.THUMBNAILS,
                    Utility.dataPaths.PICTURES,
                    Utility.dataPaths.DATA_EXPORT
            });

            // parse the JSON template
            new templateFileParser().execute();
        }

        // Initialize ScopeActivity variables
        mScope = CsDbContract.PATH_MAIN;
        mFragmentTag = "main_fragment";

        setContentView(R.layout.activity_main);

        // setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // initialize some items
        mFab = (FloatingActionButton) findViewById(R.id.fab);

        // The new cemetery floating button onclick handler
        mFab.setOnClickListener(new fabClick());

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
