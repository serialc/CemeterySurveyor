package net.frakturmedia.cemeterysurvey;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.frakturmedia.cemeterysurvey.data.CsDbContract;

public class GraveActivity extends ScopeActivity {

    TabLayout mTabs;
    ViewPager mPager;
    FrameLayout mFrame;
    TabsPagerAdapter mAdapter;

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

        // Need this for grave as we hide the tabs
        mFrame = (FrameLayout) findViewById(R.id.framelayout_list_container);

        // TABS
        mTabs = (TabLayout) findViewById(R.id.tabs);
        mPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        mPager.setAdapter(mAdapter);
        mTabs.setupWithViewPager(mPager);

        // initialize floating button even though we do not use it - super references it
        mFab = (FloatingActionButton) findViewById(R.id.fab);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Which button was it
        switch (id) {
            case R.id.action_display_photos:
                mPager.setAdapter(null);

                mTabs.setVisibility(View.GONE);
                mPager.setVisibility(View.GONE);
                mFrame.setVisibility(View.VISIBLE);

                displayPictures(null);
                return true;

            case R.id.action_display_survey:
                mPager.setAdapter(mAdapter);
                mTabs.setupWithViewPager(mPager);

                mTabs.setVisibility(View.VISIBLE);
                mPager.setVisibility(View.VISIBLE);
                mFrame.setVisibility(View.GONE);

                return true;

            // in grave
            case R.id.action_clear_grave:
                // create Alert Dialogue Builder
                AlertDialog.Builder adb = new AlertDialog.Builder(context);

                LayoutInflater li = LayoutInflater.from(context);
                View promptView = li.inflate(R.layout.input_dialog, null);
                final TextView textViewHeading = (TextView) promptView.findViewById(R.id.edit_text_dialog_heading);
                textViewHeading.setText("Confirm that you want to reset/delete all the data for this grave!");
                final EditText userInput = (EditText) promptView.findViewById(R.id.edit_text_dialog_user_input);
                userInput.setVisibility(View.GONE);

                adb.setView(promptView);
                adb.setCancelable(false).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Delete all the attributes
                        getContentResolver().delete(CsDbContract.GraveAttributesEntry.buildGraveIdUri(mGraveId), null, null);
                        int updated = getContentResolver().update(CsDbContract.GraveEntry.buildClearGraveIdUri(mGraveId), null, null, null);
                        Toast.makeText(GraveActivity.this, "Grave data cleared", Toast.LENGTH_SHORT).show();

                        mPager.setAdapter(mAdapter);
                    }
                });
                adb.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.cancel();
                        Toast.makeText(GraveActivity.this, "Grave data clearing cancelled", Toast.LENGTH_SHORT).show();
                    }
                });

                // build/prompt user
                AlertDialog alertDialog = adb.create();
                alertDialog.show();

                return true;
        }

        // bookmark and camera will fall to super

        // back button?
        return super.onOptionsItemSelected(item);
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

    public class TabsPagerAdapter extends FragmentPagerAdapter {

        String[] tabTitles = null;

        public TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return getSurveyListFragment(position + 1);
        }

        @Override
        public int getCount() {
            if( tabTitles == null ) {
                Cursor tabCursor = getContentResolver().query(CsDbContract.SurveyCategoryEntry.CONTENT_URI,
                        new String[]{CsDbContract.SurveyCategoryEntry.COLUMN_TAB_NAME}, // projection
                        CsDbContract.SurveyCategoryEntry.COLUMN_SCOPE + " = ? " + // selection
                                " GROUP BY " + CsDbContract.SurveyCategoryEntry.COLUMN_TAB_NAME, // HACK to get GROUP BY
                        new String[]{CsDbContract.PATH_GRAVE}, // args
                        CsDbContract.SurveyCategoryEntry.COLUMN_TAB_NUMBER + " ASC"
                );

                if (tabCursor.moveToFirst()) {
                    tabTitles = new String[tabCursor.getCount()];
                    for (int index = 0; index < tabCursor.getCount(); index++) {
                        tabTitles[index] = tabCursor.getString(0);
                        tabCursor.moveToNext();
                    }
                }
                tabCursor.close();
            }

            return tabTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }
}