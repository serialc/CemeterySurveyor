package net.frakturmedia.cemeterysurvey;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import net.frakturmedia.cemeterysurvey.data.CsDbContract;

public class GraveActivityNoTabs extends ScopeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mViewingState = savedInstanceState.getString(super.VIEWING_STATE);
            //mCurrentPhotoFileName = savedInstanceState.getString(super.PHOTO_FILE_NAME);
        } else {
            // Do only on first load, and not for tablet rotations
            mViewingState = "survey";
        }

        // Initialize ScopeActivity variables
        mScope = CsDbContract.PATH_GRAVE;
        mFragmentTag = "grave_fragment";

        // Set the layout file
        setContentView(R.layout.activity_grave);

        // Get the intent data passed for this grave
        Intent intent = getIntent();
        mGraveId = intent.getLongExtra("id", -1);
        mScopeId = mGraveId;

        // returns cemetery name[0], section name[1], grave name[2], cemetery id[3], section id[4], grave id[5]
        String[] scopeNameIds = getScopeNameFromScopeId(mScope, mScopeId);
        mCemeteryId = Long.parseLong(scopeNameIds[3]);
        mSectionId = Long.parseLong(scopeNameIds[4]);
        setTitle("Grave " + scopeNameIds[0] + " : " + scopeNameIds[1] + " : " + scopeNameIds[2]);

        // setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back/up button to toolbar/actionbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // initialize floating button even though we do not use it - super references it
        mFab = (FloatingActionButton) findViewById(R.id.fab);

        // The new section floating button onclick handler - we don't need to do for grave
        //mFab.setOnClickListener(new fabClick());

        resetCurrentView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_grave, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        // mimic newer action bar
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        Intent results = new Intent();
        results.putExtra(Utility.resultCodes.GRAVE_IDENTIFIER, mGraveId);
        setResult(RESULT_OK, results);
        super.onBackPressed();
        finish();
    }

}
