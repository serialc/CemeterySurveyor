package net.frakturmedia.cemeterysurvey;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import net.frakturmedia.cemeterysurvey.data.CsDbContract;

public class SectionActivity extends ScopeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if( savedInstanceState != null ) {
            mViewingState = savedInstanceState.getString(super.VIEWING_STATE);
            //mCurrentPhotoFileName = savedInstanceState.getString(super.PHOTO_FILE_NAME);
        } else {
            // Do only on first load, and not for tablet rotations
            mViewingState = "graves";
            //mViewingState = "survey";
        }

        // Initialize ScopeActivity variables
        mScope = CsDbContract.PATH_SECTION;
        mFragmentTag = "section_fragment";

        // Set the layout file
        setContentView(R.layout.activity_section);

        // Get the intent data passed for this section
        Intent intent = getIntent();
        mSectionId = intent.getLongExtra("id", -1);
        mScopeId = mSectionId;

        // Check if the bookmark is passing us through
        if( intent.getBooleanExtra("bookmark", false) ) {
            processBookmark(intent, mScope);
        }

        // returns cemetery name[0], section name[1], cemetery id[2], section id[3]
        String[] scopeNameIds = getScopeNameFromScopeId(mScope, mScopeId);
        mCemeteryId = Long.parseLong(scopeNameIds[2]);
        setTitle("Section " + scopeNameIds[0] + " : " + scopeNameIds[1]);

        // setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back/up button to toolbar/actionbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // initialize some items
        mFab = (FloatingActionButton) findViewById(R.id.fab);

        // The new grave floating button onclick handler
        mFab.setOnClickListener(new fabClick());
        // The long click creates an editable field
        mFab.setOnLongClickListener(new fabLongClick());

        // Initialize left menu buttons
        Button buttonViewGraves = (Button) findViewById(R.id.button_sec_show_graves);
        ImageButton imageButtonViewSurvey = (ImageButton) findViewById(R.id.imagebutton_sec_show_survey);
        ImageButton imageButtonViewPictures = (ImageButton) findViewById(R.id.imagebutton_sec_show_pictures);
        buttonViewGraves.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                displayGraves(v);
            }
        });
        imageButtonViewSurvey.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                displaySurvey(v);
            }
        });
        imageButtonViewPictures.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                displayPictures(v);
            }
        });

        resetCurrentView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cemetery, menu);
        return true;
    }
}
