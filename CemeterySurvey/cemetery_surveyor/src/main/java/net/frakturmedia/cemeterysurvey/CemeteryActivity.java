package net.frakturmedia.cemeterysurvey;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import net.frakturmedia.cemeterysurvey.data.CsDbContract;

public class CemeteryActivity extends ScopeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if( savedInstanceState != null ) {
            mViewingState = savedInstanceState.getString(super.VIEWING_STATE);
            //mCurrentPhotoFileName = savedInstanceState.getString(super.PHOTO_FILE_NAME);
        } else {
            // Do only on first load, and not for tablet rotations
            mViewingState = "sections";
        }

        // Initialize ScopeActivity variables
        mScope = CsDbContract.PATH_CEMETERY;
        mFragmentTag = "cemetery_fragment";

        // Set the layout file
        setContentView(R.layout.activity_cemetery);

        // Get the intent data passed for this cemetery
        Intent intent = getIntent();
        //Uri uri = intent.getData();
        mCemeteryId = intent.getLongExtra("id", -1);
        mScopeId = mCemeteryId;

        // Check if the bookmark is passing us through
        if( intent.getBooleanExtra("bookmark", false) ) {
            processBookmark(intent, mScope);
        }

        // Retrieve the name of this cemetery
        String[] scopeNameIds = getScopeNameFromScopeId(mScope, mScopeId);
        setTitle("Cemetery " + scopeNameIds[0]);

        // setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back/up button to toolbar/actionbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // initialize some items
        mFab = (FloatingActionButton) findViewById(R.id.fab);

        // The new section floating button onclick handler
        mFab.setOnClickListener(new fabClick());

        resetCurrentView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cemetery, menu);
        return true;
    }
}
