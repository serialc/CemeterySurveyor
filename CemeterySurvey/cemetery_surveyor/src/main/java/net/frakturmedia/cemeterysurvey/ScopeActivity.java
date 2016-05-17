package net.frakturmedia.cemeterysurvey;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonWriter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.frakturmedia.cemeterysurvey.data.CsDbContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;
import java.util.regex.Pattern;

public class ScopeActivity extends AppCompatActivity {

    public static final String LOG_TAG = ScopeActivity.class.getSimpleName();

    private static final String FRAGMENT_TYPE_KEY = "fragment_type";
    private static final String FRAGMENT_HEADING = "gridview_heading";
    private static final String FRAGMENT_SCOPE = "fragment_scope";
    private static final String PASS_IDENTIFIER = "identifier";

    // For when the fragment is inside a pager
    private static final String ARG_PAGE_NUMBER = "page_number";

    private static final String CLICK_BEHAVIOUR = "short_click_behaviour";
    private static final String LONG_CLICK_BEHAVIOUR = "long_click_behaviour";

    public static final String VIEWING_STATE = "current_view_state";
    public static final String PHOTO_FILE_NAME = "current_photo_file_name";

    public String mFragmentTag;
    public FloatingActionButton mFab;
    public RelativeLayout mProgressBarWheel;

    final Context context = this;

    String mScope;

    Long mScopeId = null;
    Long mCemeteryId = null;
    Long mSectionId = null;
    Long mGraveId = null;

    public String mViewingState;

//        @Override
//    protected void onStart() {
//        Log.d(LOG_TAG, "STARTED " + mScope);
//        super.onStart();
//    }
//
//    @Override
//    protected void onStop() {
//        Log.d(LOG_TAG, "STOPPED " + mScope);
//        super.onStop();
//    }
//    @Override
//    protected void onPause() {
//        Log.d(LOG_TAG, "PAUSED " + mScope);
//        super.onPause();
//    }
//
//    @Override
//    protected void onDestroy() {
//        Log.d(LOG_TAG, "DESTROYED " + mScope);
//        super.onDestroy();
//    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(VIEWING_STATE, mViewingState);
        super.onSaveInstanceState(outState);
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
                displayPictures(null);
                return true;
            case R.id.action_display_survey:
                displaySurvey(null);
                return true;
            case R.id.action_take_picture:
                takePicture();
                return true;
            case R.id.action_create_bookmark:
                createBookmark();
                return true;

            // in main
            case R.id.action_reload_template:
                // parse the JSON template
                showLoadingScreen(true);
                new templateFileParser().execute();
                return true;
            case R.id.action_export_data:
                exportData();
                return true;

            // in all
            case R.id.action_add_attribute:
                Intent intent = new Intent(context, AttributeAdder.class);
                startActivity(intent);
                return true;

            // Respond to the action bar's Up/Home button
            // Goes to parent activity (activity that called this activity)
            // rather than to specified parent in AndroidManifest.xml
            case android.R.id.home:
                finish();
                return true;
        }
        // back button?
        return super.onOptionsItemSelected(item);
    }

    public void resetCurrentView() {

        // Display the content accordingly
        switch (mViewingState) {
            case "cemeteries":
                displayCemeteries(null);
                break;
            case "bookmarks":
                displayBookmarks(null);
                break;
            case "sections":
                displaySections(null);
                break;
            case "graves":
                displayGraves(null);
                break;
            case "survey":
                displaySurvey(null);
                break;
            case "pictures":
                displayPictures(null);
                break;
            default:
                throw new UnsupportedOperationException("Unknown cemetery view state: " + mViewingState);
        }
    }

    public void displayCemeteries(View view) {

        mViewingState = "cemeteries";
        if( view != null ) {
            view.setSelected(true);
        }

        Bundle bundle = new Bundle();
        bundle.putString(FRAGMENT_TYPE_KEY, CsDbContract.PATH_CEMETERY);
        bundle.putString(FRAGMENT_HEADING, "Cemeteries");
        bundle.putString(CLICK_BEHAVIOUR, "select");
        bundle.putString(LONG_CLICK_BEHAVIOUR, "edit");

        // set Fragment class Arguments
        ListFragment listFragment = new ListFragment();
        //ListFragment listFragment = new ListFragment();
        listFragment.setArguments(bundle);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.framelayout_list_container, listFragment, mFragmentTag);
        ft.commit();
    }

    public void displaySections(View view) {
        mViewingState = "sections";
        mFab.setVisibility(View.VISIBLE);

        Bundle bundle = new Bundle();
        bundle.putString(FRAGMENT_TYPE_KEY, CsDbContract.PATH_SECTION);
        bundle.putString(FRAGMENT_HEADING, "Sections");
        bundle.putString(CLICK_BEHAVIOUR, "select");
        bundle.putString(LONG_CLICK_BEHAVIOUR, "edit");
        bundle.putLong(PASS_IDENTIFIER, mCemeteryId); // display sections for this cemetery

        // set Fragmentclass Arguments
        ListFragment listFragment = new ListFragment();
        listFragment.setArguments(bundle);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.framelayout_list_container, listFragment, mFragmentTag);
        ft.commit();
    }

    public void displayGraves(View view) {
        mViewingState = "graves";
        mFab.setVisibility(View.VISIBLE);

        Bundle bundle = new Bundle();
        bundle.putString(FRAGMENT_TYPE_KEY, CsDbContract.PATH_GRAVE);
        bundle.putString(FRAGMENT_HEADING, "Graves");
        bundle.putString(CLICK_BEHAVIOUR, "select");
        bundle.putString(LONG_CLICK_BEHAVIOUR, "edit");
        bundle.putLong(PASS_IDENTIFIER, mSectionId); // display graves for this section

        // set Fragmentclass Arguments
        ListFragment listFragment = new ListFragment();
        listFragment.setArguments(bundle);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.framelayout_list_container, listFragment, mFragmentTag);
        ft.commit();
    }

    public void displaySurvey(View view) {
        mViewingState = "survey";
        mFab.setVisibility(View.GONE);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        // params (where to put it, what to put in, what the id of this fragment is)
        ft.replace(R.id.framelayout_list_container, getSurveyListFragment(1), mFragmentTag);
        ft.commit();
    }

    public Fragment getSurveyListFragment(int tabNumber) {
        Bundle bundle = new Bundle();
        bundle.putString(FRAGMENT_TYPE_KEY, CsDbContract.PATH_SURVEY);
        bundle.putString(FRAGMENT_HEADING, "_none");
        bundle.putString(FRAGMENT_SCOPE, mScope);
        bundle.putLong(PASS_IDENTIFIER, mScopeId);
        bundle.putString(CLICK_BEHAVIOUR, "none");
        bundle.putString(LONG_CLICK_BEHAVIOUR, "none");
        bundle.putInt(ARG_PAGE_NUMBER, tabNumber);

        // set Fragmentclass Arguments
        ListFragment listFragment = new ListFragment();
        listFragment.setArguments(bundle);

        return listFragment;
    }

    public void displayBookmarks(View view) {
        mViewingState = "bookmarks";
        mFab.setVisibility(View.GONE);

        Bundle bundle = new Bundle();
        bundle.putString(FRAGMENT_TYPE_KEY, CsDbContract.PATH_BOOKMARK);
        bundle.putString(FRAGMENT_HEADING, "Bookmarks");
        bundle.putString(CLICK_BEHAVIOUR, "select");
        bundle.putString(LONG_CLICK_BEHAVIOUR, "delete");

        // set Fragmentclass Arguments
        ListFragment listFragment = new ListFragment();
        listFragment.setArguments(bundle);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.framelayout_list_container, listFragment, mFragmentTag);
        ft.commit();
    }

    public void displayPictures(View view) {
        mViewingState = "pictures";
        mFab.setVisibility(View.GONE);

        Bundle bundle = new Bundle();
        bundle.putString(FRAGMENT_TYPE_KEY, CsDbContract.PATH_PICTURE);
        bundle.putString(FRAGMENT_HEADING, "Pictures");
        bundle.putString(FRAGMENT_SCOPE, mScope);
        bundle.putString(CLICK_BEHAVIOUR, "select");
        bundle.putString(LONG_CLICK_BEHAVIOUR, "none");
        bundle.putLong(PASS_IDENTIFIER, mScopeId);

        // set Fragmentclass Arguments
        ListFragment listFragment = new ListFragment();
        listFragment.setArguments(bundle);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.framelayout_list_container, listFragment, mFragmentTag);
        ft.commit();
    }

    public void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = new File(Utility.pictures.TEMPORARY_SAVE_FILE_PATH);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            startActivityForResult(takePictureIntent, Utility.resultCodes.REQUEST_IMAGE_CAPTURE);
        }
    }

    public void processBookmark(Intent intent, String curScope) {
        // get the scope and then request appropriate data
        String targetScope = intent.getStringExtra("bookmark_scope");

        // If we are at our destination then return
        if (targetScope.equals(curScope)) {
            return;
        }

//        Log.d(LOG_TAG, "Current Scope = " + curScope + ", target=" + targetScope);
        Intent nextActivity;
        switch (curScope) {
            case CsDbContract.PATH_CEMETERY:
                nextActivity = new Intent(context, SectionActivity.class);
                nextActivity.putExtra("bookmark", true);
                nextActivity.putExtra("id", intent.getLongExtra("bookmark_section_id", -1));
                nextActivity.putExtra("bookmark_grave_id", intent.getLongExtra("bookmark_grave_id", -1));
                nextActivity.putExtra("bookmark_scope", targetScope);
                startActivity(nextActivity);
                break;
            case CsDbContract.PATH_SECTION:
                nextActivity = new Intent(context, GraveActivity.class);
                nextActivity.putExtra("bookmark", true);
                nextActivity.putExtra("id", intent.getLongExtra("bookmark_grave_id", -1));
                nextActivity.putExtra("bookmark_scope", targetScope);
                // Need to call grave required attribute checking when we quit the activity
                startActivityForResult(nextActivity, Utility.resultCodes.GRAVE_ID_RESULT_CODE);
                break;
            default:
                throw new UnsupportedOperationException("Unknown scope type: " + curScope);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        switch (requestCode) {
            case Utility.resultCodes.GRAVE_ID_RESULT_CODE:
                switch (resultCode) {
                    case RESULT_OK:
                        Long graveId = intent.getLongExtra(Utility.resultCodes.GRAVE_IDENTIFIER, -1);
                        checkSurveyRequiredFields(CsDbContract.PATH_GRAVE, graveId);
                        break;
                    default:
                        Log.e(LOG_TAG, "The grave should always return an id for required field checking. Likely the Grave Activity did not close nicely.");
                }
            case Utility.resultCodes.REQUEST_IMAGE_CAPTURE:
                switch (resultCode) {
                    case RESULT_OK:
                        // Picture is here
                        File photoFile = new File(Utility.pictures.TEMPORARY_SAVE_FILE_PATH);

                        // Move to
                        File photoNewFile = null;
                        try {
                            photoNewFile = Utility.pictures.createImageFile();
                            // rename/move
                            photoFile.renameTo(photoNewFile);
                        } catch (IOException ex) {
                            Toast.makeText(this, "Unable to create picture for saving", Toast.LENGTH_SHORT);
                            Log.e(LOG_TAG, ex.toString());
                        }

                        SharedPreferences settings = getPreferences(0);

                        String categoryName = settings.getString(Utility.pictures.CATEGORY_NAME, null);
                        String attributeName = settings.getString(Utility.pictures.ATTRIBUTE_NAME, null);
                        // Clear the shared preferences!
                        settings.edit().clear().commit();

                        // Save to DB
                        // add the photo name to the ContentValues
                        ContentValues cv = new ContentValues();
                        cv.put(CsDbContract.PictureEntry.COLUMN_CEMETERY_ID, mCemeteryId);
                        cv.put(CsDbContract.PictureEntry.COLUMN_SECTION_ID, mSectionId);
                        cv.put(CsDbContract.PictureEntry.COLUMN_GRAVE_ID, mGraveId);
                        cv.put(CsDbContract.PictureEntry.COLUMN_CATEGORY_NAME, categoryName);
                        cv.put(CsDbContract.PictureEntry.COLUMN_ATTRIBUTE_NAME, attributeName);
                        cv.put(CsDbContract.PictureEntry.COLUMN_SCOPE, mScope);
                        cv.put(CsDbContract.PictureEntry.COLUMN_FILE_NAME, photoNewFile.getName());
                        // Need to add to DB
                        getContentResolver().insert(CsDbContract.PictureEntry.CONTENT_URI, cv);

                        //notify();
                        break;
                    case RESULT_CANCELED:
                        Toast.makeText(this, "Taking picture cancelled", Toast.LENGTH_SHORT);
                        break;
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown onActivityResult requesCode: " + requestCode);
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    public void createBookmark() {
        // Check if it already exists in DB
        Cursor cursor = getContentResolver().query(CsDbContract.BookmarkEntry.buildBookmarkUriFromScopeId(mScopeId, mScope),
                null, null, null, null);

        if (cursor.getCount() == 1) {
            // exists already - don't do anything
            Toast.makeText(this, Character.toUpperCase(mScope.charAt(0)) + mScope.substring(1) + " has already been bookmarked", Toast.LENGTH_SHORT).show();
        } else {
            // Add to DB
            ContentValues cv = new ContentValues();
            cv.put(CsDbContract.BookmarkEntry.COLUMN_CEMETERY_ID, mCemeteryId);
            cv.put(CsDbContract.BookmarkEntry.COLUMN_SECTION_ID, mSectionId);
            cv.put(CsDbContract.BookmarkEntry.COLUMN_GRAVE_ID, mGraveId);
            cv.put(CsDbContract.BookmarkEntry.COLUMN_SCOPE, mScope);
            getContentResolver().insert(CsDbContract.BookmarkEntry.CONTENT_URI, cv);
            // show Toast!
            Toast.makeText(this, Character.toUpperCase(mScope.charAt(0)) + mScope.substring(1) + " has been bookmarked", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }

    public String[] getScopeNameFromScopeId(String scope, long id) {

        Cursor cursor;

        switch (scope) {
            case CsDbContract.PATH_CEMETERY:
                cursor = getContentResolver().query(CsDbContract.CemeteryEntry.buildCemeteryIdUri(id),
                        new String[]{CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME}, null, null, null);
                break;
            case CsDbContract.PATH_SECTION:
                cursor = getContentResolver().query(CsDbContract.SectionEntry.CONTENT_URI.buildUpon().appendPath("joined").build(),
                        null, " S._ID = ? ", new String[]{Long.toString(id)}, null);
                break;
            case CsDbContract.PATH_GRAVE:
                cursor = getContentResolver().query(CsDbContract.GraveEntry.CONTENT_URI.buildUpon().appendPath("joined").build(),
                        null, " G._ID = ? ", new String[]{Long.toString(id)}, null);
                break;
            default:
                throw new UnsupportedOperationException("Unknown scope name: " + scope);
        }

        if (!cursor.moveToFirst()) {
            throw new UnsupportedOperationException("Failed to find a scope row for scope: " + scope + ", id: " + id);
        }
        String[] scopeNameIds;
        switch (scope) {
            case CsDbContract.PATH_CEMETERY:
                scopeNameIds = new String[]{cursor.getString(0)}; // don't need id in Cemetery activity, we already have it
                break;
            case CsDbContract.PATH_SECTION:
                scopeNameIds = new String[]{cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)};
                break;
            case CsDbContract.PATH_GRAVE:
                scopeNameIds = new String[]{cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)};
                break;
            default:
                throw new UnsupportedOperationException("Unknown scope name: " + scope);
        }
        cursor.close();

        return scopeNameIds;
    }

    public class fabClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Log.d(LOG_TAG, "From scope " + mScope + ", item creation button clicked");

            // Creating a new grave does not
            if (mScope.equals(CsDbContract.PATH_SECTION)) {

                // Find out what the maximum integer grave id is for this section
                Cursor cursor = getContentResolver().query(CsDbContract.GraveEntry.buildGravesInSectionIdUri(mScopeId),
                        new String[]{"MAX(CAST(" + CsDbContract.GraveEntry.COLUMN_GRAVE_NAME + " AS INT))"}, // projection (select the max int(name)
                        null, // selection
                        null, // selectionArgs
                        null); // sortOrder

                String newGraveId = "1";
                if (cursor.moveToFirst()) {
                    newGraveId = Integer.toString(cursor.getInt(0) + 1);
                }
                cursor.close();

                // create new grave with the next largest int
                ContentValues cv = new ContentValues();
                cv.put(CsDbContract.GraveEntry.COLUMN_CEMETERY_ID, mCemeteryId);
                cv.put(CsDbContract.GraveEntry.COLUMN_SECTION_ID, mSectionId);
                cv.put(CsDbContract.GraveEntry.COLUMN_GRAVE_NAME, newGraveId);
                getContentResolver().insert(CsDbContract.GraveEntry.CONTENT_URI, cv);

                return;
            }

            // create Alert Dialogue Builder
            AlertDialog.Builder adb = new AlertDialog.Builder(context);

            LayoutInflater li = LayoutInflater.from(context);
            View promptView = li.inflate(R.layout.input_dialog, null);

            final TextView textViewHeading = (TextView) promptView.findViewById(R.id.edit_text_dialog_heading);
            switch (mScope) {
                case CsDbContract.PATH_MAIN:
                    textViewHeading.setText("Enter new " + CsDbContract.PATH_CEMETERY + " name");
                    break;
                case CsDbContract.PATH_CEMETERY:
                    textViewHeading.setText("Enter new " + CsDbContract.PATH_SECTION + " name");
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown scope creation type: " + mScope);
            }

            final EditText userInput = (EditText) promptView
                    .findViewById(R.id.edit_text_dialog_user_input);

            adb.setView(promptView);
            adb.setCancelable(false).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    ContentValues cv = new ContentValues();
                    switch (mScope) {
                        case CsDbContract.PATH_MAIN:
                            cv.put(CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME, userInput.getText().toString());
                            getContentResolver().insert(CsDbContract.CemeteryEntry.CONTENT_URI, cv);
                            break;
                        case CsDbContract.PATH_CEMETERY:
                            cv.put(CsDbContract.SectionEntry.COLUMN_CEMETERY_ID, mCemeteryId);
                            cv.put(CsDbContract.SectionEntry.COLUMN_SECTION_NAME, userInput.getText().toString());
                            getContentResolver().insert(CsDbContract.SectionEntry.CONTENT_URI, cv);
                            break;
                        default:
                            throw new UnsupportedOperationException("Unknown scope creation type: " + mScope);
                    }

                    // reload the list of cemeteries
                    //resetCurrentView();
                }
            });
            adb.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    dialog.cancel();
                    Toast.makeText(ScopeActivity.this, "Creation cancelled", Toast.LENGTH_SHORT).show();
                }
            });

            // build/prompt user
            AlertDialog alertDialog = adb.create();
            alertDialog.show();
        }
    }

    public class fabLongClick implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View view) {
            Log.d(LOG_TAG, "From scope " + mScope + ", item creation button clicked");

            // Only the section button can do long click
            if (!mScope.equals(CsDbContract.PATH_SECTION)) {
                return false;
            }

            // create Alert Dialogue Builder
            AlertDialog.Builder adb = new AlertDialog.Builder(context);

            LayoutInflater li = LayoutInflater.from(context);
            View promptView = li.inflate(R.layout.input_dialog, null);

            final TextView textViewHeading = (TextView) promptView.findViewById(R.id.edit_text_dialog_heading);
            textViewHeading.setText("Enter new " + CsDbContract.PATH_GRAVE + " name");
            final EditText userInput = (EditText) promptView.findViewById(R.id.edit_text_dialog_user_input);

            adb.setView(promptView);

            adb.setCancelable(false).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    ContentValues cv = new ContentValues();
                    cv.put(CsDbContract.GraveEntry.COLUMN_CEMETERY_ID, mCemeteryId);
                    cv.put(CsDbContract.GraveEntry.COLUMN_SECTION_ID, mSectionId);
                    cv.put(CsDbContract.GraveEntry.COLUMN_GRAVE_NAME, userInput.getText().toString());
                    getContentResolver().insert(CsDbContract.GraveEntry.CONTENT_URI, cv);

                    // reload the list of cemeteries
                    //resetCurrentView();
                }
            });

            adb.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    dialog.cancel();
                    Toast.makeText(ScopeActivity.this, "Creation cancelled", Toast.LENGTH_SHORT).show();
                }
            });

            // build/prompt user
            AlertDialog alertDialog = adb.create();
            alertDialog.show();

            // true means we are consuming the longClick - do not pass to Click
            return true;
        }
    }

    public class fileStructureBuilder extends AsyncTask<String[], Void, Void> {

        @Override
        protected Void doInBackground(String[]... stringArray) {
            String state = Environment.getExternalStorageState();

            // Check that it's mounted
            if (Environment.MEDIA_MOUNTED.equals(state)) {

                // create all the paths
                // stringArray[0] contains a String[]!
                for (String path : stringArray[0]) {
                    //Log.d(LOG_TAG, "Checking and maybe creating " + path);

                    // create file path

                    File file = new File(path);

                    // Does the file already exist?
                    if (!file.exists()) {
                        // No, so try and make it
                        if (!file.mkdirs()) {
                            throw new UnsupportedOperationException("Failed to make directory: " + path);
                        }
                    }
                }

                // check if the .nomedia file exists in Utility.dataPaths.PICTURES, else create
                File file = new File(Utility.dataPaths.EXPORT_NOMEDIA);
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (java.io.IOException ex) {
                        Log.e(LOG_TAG, "Failed creation of .nomedia file in " + Utility.dataPaths.EXPORT_NOMEDIA + ".");
                    }
                }

                // check if the .nomedia file exists in Utility.dataPaths.THUMBNAILS_NOMEDIA, else it
                file = new File(Utility.dataPaths.THUMBNAILS_NOMEDIA);
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (java.io.IOException ex) {
                        Log.e(LOG_TAG, "Failed creation of .nomedia file in " + Utility.dataPaths.THUMBNAILS_NOMEDIA + ".");
                    }
                }

                // check if the .nomedia file exists in Utility.dataPaths.THUMBNAILS_NOMEDIA, else it
                file = new File(Utility.dataPaths.THUMBNAILS_SMALL_NOMEDIA);
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (java.io.IOException ex) {
                        Log.e(LOG_TAG, "Failed creation of .nomedia file in " + Utility.dataPaths.THUMBNAILS_SMALL_NOMEDIA + ".");
                    }
                }
            }
            return null;
        }
    }

    public class templateWriter extends AsyncTask<Void, Void, String> {

        static final int COL_SCOPE = 0;
        static final int COL_TAB_NAME = 1;
        static final int COL_GROUP_NAME = 2;
        static final int COL_CATEGORY_NAME =3;
        static final int COL_CATEGORY_ID =4;
        static final int COL_TITLE = 5;
        static final int COL_REQUIRED = 6;
        static final int COL_TYPE = 7;
        static final int COL_PICTURE = 8;
        static final int COL_ATTRIBUTE_PICTURE = 9;
        static final int COL_ATTRIBUTES = 10;

        String currentScope = null;
        String currentTab = null;
        String currentGroup = null;

        @Override
        protected String doInBackground(Void... params) {

            // backup existing template to archive
            String result = Utility.backupTemplateFile();
            if (result != null) return result;

            // Create new JSON
            try {
                BufferedOutputStream buf = new BufferedOutputStream(new FileOutputStream(Utility.dataPaths.TEMPLATE_FILE));

                try {
                    // build it for the three base items
                    result = writeJsonStream(buf);
                } catch (IOException io) {
                    Log.e(LOG_TAG, "IOException when trying to create new JSON template." + io.toString(), io);
                    return "Failed to rebuild JSON template! See log errors.";
                }
            } catch (FileNotFoundException ex) {
                Log.e(LOG_TAG, "File not found to write new JSON template." + ex.toString(), ex);
                return "Failed to rebuild JSON template! See log errors.";
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Survey template updated", Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(result);
        }

        protected String writeJsonStream(OutputStream out) throws IOException {
            String results = null;
            Cursor catCursor = getContentResolver().query(CsDbContract.SurveyCategoryEntry.CONTENT_URI,
                    new String[]{CsDbContract.SurveyCategoryEntry.COLUMN_SCOPE,
                            CsDbContract.SurveyCategoryEntry.COLUMN_TAB_NAME,
                            CsDbContract.SurveyCategoryEntry.COLUMN_GROUP_NAME,
                            CsDbContract.SurveyCategoryEntry.COLUMN_NAME,
                            CsDbContract.SurveyCategoryEntry._ID,
                            CsDbContract.SurveyCategoryEntry.COLUMN_TITLE,
                            CsDbContract.SurveyCategoryEntry.COLUMN_REQUIRED,
                            CsDbContract.SurveyCategoryEntry.COLUMN_TYPE,
                            CsDbContract.SurveyCategoryEntry.COLUMN_PICTURE,
                            CsDbContract.SurveyCategoryEntry.COLUMN_ATTRIBUTE_PICTURE,
                            CsDbContract.SurveyCategoryEntry.COLUMN_THUMBNAILS_PATH
                    },
                    null, null,
                    CsDbContract.SurveyCategoryEntry.COLUMN_SCOPE + ", " +
                            CsDbContract.SurveyCategoryEntry.COLUMN_TAB_NUMBER + ", " +
                            CsDbContract.SurveyCategoryEntry.COLUMN_GROUP_NUMBER + ", " +
                            CsDbContract.SurveyCategoryEntry.COLUMN_CATEGORY_NUMBER + " ASC"
            );

            if ( !catCursor.moveToFirst() ) {
                Log.e(LOG_TAG, "Failed to retrieve survey from database.");
                return "Failed to rebuild JSON template! See log errors.";
            }

            JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.setIndent("  ");
            writer.beginObject();
            writer.name("_type").value("root");

            while( catCursor.getPosition() < catCursor.getCount() ) {
                if( currentScope == null || !currentScope.equals(catCursor.getString(COL_SCOPE)) ) {
                    results = writeScope(writer, catCursor);
                }
//                Log.d(LOG_TAG, "Changing Scope");
            }

            // close root
            writer.endObject();
            writer.close();

            catCursor.close();

            return results;
        }

        protected String writeScope(JsonWriter writer, Cursor cursor) throws IOException {
            String results = null;

            currentScope = cursor.getString(COL_SCOPE);
            currentTab = null;
            writer.name(currentScope);
            writer.beginArray();

            while( cursor.getPosition() < cursor.getCount() &&
                    currentScope.equals(cursor.getString(COL_SCOPE)) ) {

                if( currentTab == null || !currentTab.equals(cursor.getString(COL_TAB_NAME)) ) {
                    results = writeTab(writer, cursor);
                }
//                Log.d(LOG_TAG, "Changing Tabs");
            }

            writer.endArray();

            return results;
        }

        protected String writeTab(JsonWriter writer, Cursor cursor) throws IOException {
            String results = null;

            currentTab = cursor.getString(COL_TAB_NAME);
            currentGroup = null;
            writer.beginObject();
            writer.name("_type").value("tab");
            writer.name("title").value(currentTab);
            writer.name("contents");
            writer.beginArray();

            while( cursor.getPosition() < cursor.getCount() &&
                    currentTab.equals(cursor.getString(COL_TAB_NAME)) &&
                    currentScope.equals(cursor.getString(COL_SCOPE)) ) {

                if( currentGroup == null || !currentGroup.equals(cursor.getString(COL_GROUP_NAME)) ) {
                    results = writeGroup(writer, cursor);
                }
//                Log.d(LOG_TAG, "Changing Group");
            }
            writer.endArray();
            writer.endObject();

            return results;
        }

        protected String writeGroup(JsonWriter writer, Cursor cursor) throws IOException {
            String results = null;

            currentGroup = cursor.getString(COL_GROUP_NAME);
            writer.beginObject();
            writer.name("_type").value("group");
            writer.name("title").value(currentGroup);
            writer.name("contents");
            writer.beginArray();

            while( cursor.getPosition() < cursor.getCount() &&
                    currentGroup.equals(cursor.getString(COL_GROUP_NAME)) &&
                    currentTab.equals(cursor.getString(COL_TAB_NAME)) &&
                    currentScope.equals(cursor.getString(COL_SCOPE)) ) {

//                Log.d(LOG_TAG, "Changing Categories");
                results = writeCategory(writer, cursor);
                cursor.moveToNext();
            }
            writer.endArray();
            writer.endObject();

            return results;
        }

        protected String writeCategory(JsonWriter writer, Cursor cursor) throws IOException {
            String results = null;

            writer.beginObject();
            writer.name("_type").value("category");
            writer.name("name").value(cursor.getString(COL_CATEGORY_NAME));
            writer.name("title").value(cursor.getString(COL_TITLE));
            writer.name("camera").value(cursor.getLong(COL_PICTURE) != 0);
            writer.name("attrib_camera").value(cursor.getLong(COL_ATTRIBUTE_PICTURE) != 0);
            writer.name("required").value(cursor.getLong(COL_REQUIRED) != 0);

            // the data type determines if we need to retrieve the attributes or not
            String catType = cursor.getString(COL_TYPE);
            writer.name("data_type").value(catType);

            switch(catType) {
                case Utility.surveyDataTypes.RADIO_THUMBNAIL:
                case Utility.surveyDataTypes.THUMBNAIL:
                    writer.name("attributes").value(cursor.getString(COL_ATTRIBUTES));
                    break;
                case Utility.surveyDataTypes.SET:
                case Utility.surveyDataTypes.RADIO:
                    writer.name("attributes");
                    writer.beginArray();

                    // get data to populate the arrays
                    // Get the set/radio option names for this category id
                    Cursor attCursor = getContentResolver().query(CsDbContract.SurveyAttributeEntry.CONTENT_URI,
                            new String[]{CsDbContract.SurveyAttributeEntry.COLUMN_NAME},
                            CsDbContract.SurveyAttributeEntry.COLUMN_CATEGORY_ID + " = ? ",
                            new String[]{Long.toString(cursor.getLong(COL_CATEGORY_ID))},
                            CsDbContract.SurveyAttributeEntry.COLUMN_ORDER + " ASC ");

                    if ( !attCursor.moveToFirst() ) {
                        Log.e(LOG_TAG, "Failed to retrieve survey from database.");
                        return "Failed to rebuild JSON template! See log errors.";
                    }

                    while( attCursor.getPosition() < attCursor.getCount() ) {
                        writer.value(attCursor.getString(0));
                        attCursor.moveToNext();
                    }

                    writer.endArray();
                    break;
                default:
                    // do nothing for other data types
            }

            writer.endObject();
            return results;
        }
    }

    public class templateFileParser extends AsyncTask<Void, Void, String> {

        // Data columns in scope tables, name prefix
        final String DATA_COL_PREFIX = Utility.colNamesPrefix.DATA_COL_PREFIX;

        // The three base types
        final String CEMETERY = CsDbContract.PATH_CEMETERY;
        final String SECTION = CsDbContract.PATH_SECTION;
        final String GRAVE = CsDbContract.PATH_GRAVE;

        // hierarchical descriptors
        final String ROOT = "root";
        final String GROUP = "group";
        final String TAB = "tab";
        final String CATEGORY = "category";

        // Category descriptor
        final String TYPE = "_type";
        final String DATA_TYPE = "data_type";
        final String CAMERA = "camera";
        final String ATTRIB_CAMERA = "attrib_camera";
        final String NAME = "name";
        final String TITLE = "title";
        final String ATTRIBUTES = "attributes";
        final String REQUIRED = "required";

        // Misc. descriptors
        final String CONTENTS = "contents";

        @Override
        protected String doInBackground(Void... empty) {

            // backup existing template to archive
            String result = Utility.backupTemplateFile();
            if (result != null) return result;

            // Read file and convert to String
            File jsonTemplate = new File(Utility.dataPaths.TEMPLATE_FILE);
            String jsonTemplateString = null;
            try {
                FileInputStream fin = new FileInputStream(jsonTemplate);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                // Close buffer and stream
                reader.close();
                fin.close();

                jsonTemplateString = sb.toString();

            } catch (java.io.IOException ex) {
                Log.e(LOG_TAG, "Error occurred when reading survey template.");
                Log.e(LOG_TAG, ex.toString(), ex);
                return "Error occurred when reading survey template.";
            }

            // Parse and populate DB
            try {
                return parseJsonSurveyTemplate(jsonTemplateString);
            } catch (org.json.JSONException jsonex) {
                Log.e(LOG_TAG, "JSON parsing failed. The file is incorrectly formatted.");
                Log.e(LOG_TAG, jsonex.toString(), jsonex);
                return "Warning - JSON parsing failed. The file is incorrectly formatted.";
            }
        }

        protected void onPostExecute(String result) {
            if (result != null) {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Survey template loaded", Toast.LENGTH_SHORT).show();
            }

            showLoadingScreen(false);

            super.onPostExecute(result);
        }

        private String parseJsonSurveyTemplate(String surveyTemplateString) throws JSONException {

            // Before we start saving template in DB,
            // we delete the rows from CsDbContract.SurveyCategoryEntry.CONTENT_URI and CsDbContract.SurveyAttributeEntry.CONTENT_URI
            int rowsDeleted;
            rowsDeleted = getContentResolver().delete(CsDbContract.SurveyCategoryEntry.CONTENT_URI, null, null);
            //Log.d(LOG_TAG, "Rows deleted from SurveyCategory table: " + rowsDeleted);
            rowsDeleted = getContentResolver().delete(CsDbContract.SurveyAttributeEntry.CONTENT_URI, null, null);
            //Log.d(LOG_TAG, "Rows deleted from SurveyAttribute table: " + rowsDeleted);

            // Get the the JSON root containig the top three nodes
            JSONObject templateJson = new JSONObject(surveyTemplateString);

            // Call the JSON recursive parsing function for each base node: cemetery, section, grave
            String parentJson = templateJson.getString(TYPE); // should be root
            String errors = null;
            errors = recursiveJsonParser(parentJson, templateJson.getJSONArray(CEMETERY), CEMETERY, 0, 0, 0, "", "");
            if (errors != null) return errors;
            errors = recursiveJsonParser(parentJson, templateJson.getJSONArray(SECTION), SECTION, 0, 0, 0, "", "");
            if (errors != null) return errors;
            errors = recursiveJsonParser(parentJson, templateJson.getJSONArray(GRAVE), GRAVE, 0, 0, 0, "", "");
            return errors;
        }

        private String recursiveJsonParser(String parentJson, JSONArray jsonArray, String scope, int tabNum, int groupNum, int categoryNum, String tabName, String groupName) throws JSONException {

            // Go through JSONArray of groups, categories or tabs
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                if (!jsonObject.has(TYPE)) {
                    return "Unable to parse the survey Template. The '" + TYPE + "' descriptor is missing.";
                }

                String itemType = jsonObject.getString(TYPE).toLowerCase();
                String results = null;

                //Log.d(LOG_TAG, "parent:" + parentJson + ", element=" + i + "/" + jsonArray.length() + ", Scope " + scope.toUpperCase() + " item type: " + itemType);

                switch (itemType) {
                    case TAB:
                        if (!parentJson.equals(ROOT)) {
                            return "Template loading failed: Tabs MUST be the first element in each scope. Error in " + scope;
                        }

                        tabNum += 1;
                        tabName = jsonObject.has(TITLE) ? jsonObject.getString(TITLE) : Integer.toString(i); // uses the order of the tab as the name

                        results = recursiveJsonParser(itemType, jsonObject.getJSONArray(CONTENTS), scope, tabNum, groupNum, categoryNum, tabName, groupName);
                        if (results != null) return results;
                        break;
                    case GROUP:
                        if ( !parentJson.equals(TAB) ) {
                            return "Template loading failed: Scope 'group' elements must be in tabs. Error in " + scope;
                        }

                        groupNum += 1;
                        groupName = (jsonObject.has(TITLE) && !jsonObject.getString(TITLE).equals("")) ? jsonObject.getString(TITLE) : "_blank";

                        results = recursiveJsonParser(itemType, jsonObject.getJSONArray(CONTENTS), scope, tabNum, groupNum, categoryNum, tabName, groupName);
                        if (results != null) return results;
                        break;
                    case CATEGORY:
                        if (!parentJson.equals(GROUP)) {
                            return "Template loading failed: Category elements MUST be in 'group'. Error in " + scope;
                        }

                        categoryNum += 1;
                        // check required fields
                        if (!jsonObject.has(DATA_TYPE) || !jsonObject.has(NAME)) {
                            return "Template loading failed: Unable to parse the survey Template. The '" + DATA_TYPE + "' or '" + NAME + "' descriptor is missing.";
                        }
                        String itemDataType = jsonObject.getString(DATA_TYPE).toLowerCase();
                        String itemName = jsonObject.getString(NAME).toLowerCase();

                        // optional fields
                        Boolean itemCamera = jsonObject.has(CAMERA) && jsonObject.getBoolean(CAMERA);
                        Boolean itemAttribCamera = jsonObject.has(ATTRIB_CAMERA) && jsonObject.getBoolean(ATTRIB_CAMERA);
                        Boolean itemRequired = jsonObject.has(REQUIRED) && jsonObject.getBoolean(REQUIRED);
                        String itemTitle = jsonObject.has(TITLE) ? jsonObject.getString(TITLE) : Character.toUpperCase(itemName.charAt(0)) + itemName.substring(1);

                        // If the itemDataType is THUMBNAIL:
                        // get the directory name containing the pictures in Utility.dataPaths.THUMBNAILS
                        String dirName = "";
                        if (itemDataType.equals(Utility.surveyDataTypes.THUMBNAIL) || itemDataType.equals(Utility.surveyDataTypes.RADIO_THUMBNAIL)) {
                            dirName = jsonObject.getString(ATTRIBUTES);
                        }

                        // VALIDATION
                        // Check the name of each variable to see if it is valid
                        if (!Arrays.asList(Utility.surveyDataTypes.typeList).contains(itemDataType)) {
                            return "Template loading failed: Unknown data type: " + itemDataType;
                        }

                        Pattern p;
                        p = Pattern.compile("\\w+");
                        if (!p.matcher(itemName).matches()) {
                            return "Template loading failed: Badly formatted name: " + itemName;
                        }

                        p = Pattern.compile("[\\w\\s\\.\\(\\),\\-]*");
                        if (!p.matcher(itemTitle).matches()) {
                            return "Template loading failed: Badly formatted category title: " + itemTitle;
                        }
                        if (!p.matcher(tabName).matches()) {
                            return "Template loading failed: Badly formatted tab name: " + tabName;
                        }
                        if (!p.matcher(groupName).matches()) {
                            return "Template loading failed: Badly formatted group name: " + groupName;
                        }

                        // We have retrieved: itemDataType, itemName, itemCamera, itemAttribCamera, itemRequired, itemTitle
                        // and also have: scope, tabNum, groupNum, categoryNum, tabName, groupName
                        ContentValues cv = new ContentValues();
                        cv.put(CsDbContract.SurveyCategoryEntry.COLUMN_TYPE, itemDataType);
                        cv.put(CsDbContract.SurveyCategoryEntry.COLUMN_NAME, itemName);
                        cv.put(CsDbContract.SurveyCategoryEntry.COLUMN_PICTURE, itemCamera);
                        cv.put(CsDbContract.SurveyCategoryEntry.COLUMN_ATTRIBUTE_PICTURE, itemAttribCamera);
                        cv.put(CsDbContract.SurveyCategoryEntry.COLUMN_REQUIRED, itemRequired);
                        cv.put(CsDbContract.SurveyCategoryEntry.COLUMN_TITLE, itemTitle);
                        cv.put(CsDbContract.SurveyCategoryEntry.COLUMN_SCOPE, scope);
                        cv.put(CsDbContract.SurveyCategoryEntry.COLUMN_TAB_NUMBER, tabNum);
                        cv.put(CsDbContract.SurveyCategoryEntry.COLUMN_GROUP_NUMBER, groupNum);
                        cv.put(CsDbContract.SurveyCategoryEntry.COLUMN_CATEGORY_NUMBER, categoryNum);
                        cv.put(CsDbContract.SurveyCategoryEntry.COLUMN_TAB_NAME, tabName);
                        cv.put(CsDbContract.SurveyCategoryEntry.COLUMN_GROUP_NAME, groupName);
                        cv.put(CsDbContract.SurveyCategoryEntry.COLUMN_THUMBNAILS_PATH, dirName);

                        Uri surveyUri = getContentResolver().insert(CsDbContract.SurveyCategoryEntry.CONTENT_URI, cv);
                        Long surveyId = CsDbContract.SurveyCategoryEntry.getSurveyIdFromUri(surveyUri);

                        // Add column to scope data table for items with one value (radio, measurement, text, radio_thumbnail)
                        if( itemDataType.equals(Utility.surveyDataTypes.TEXT) ||
                                itemDataType.equals(Utility.surveyDataTypes.MEASUREMENT) ||
                                itemDataType.equals(Utility.surveyDataTypes.RADIO) ||
                                itemDataType.equals(Utility.surveyDataTypes.RADIO_THUMBNAIL)
                                ) {
                            // check column name exists (with prefix 'd_' and postfix '_' + itemDataType
                            String newColName = DATA_COL_PREFIX + itemName + "_" + itemDataType;
                            switch (scope) {
                                case CEMETERY:
                                    if (!tableColumnExists(CsDbContract.CemeteryEntry.CONTENT_URI, newColName)) {
                                        createColumn(CsDbContract.CemeteryEntry.TABLE_NAME, newColName, itemDataType);
                                        Log.d(LOG_TAG, "The column " + newColName + " DOES NOT exist in cemetery. CREATED.");
                                    }
                                    break;
                                case SECTION:
                                    if (!tableColumnExists(CsDbContract.SectionEntry.CONTENT_URI, newColName)) {
                                        createColumn(CsDbContract.SectionEntry.TABLE_NAME, newColName, itemDataType);
                                        Log.d(LOG_TAG, "The column " + newColName + " DOES NOT exist in section. CREATED.");
                                    }
                                    break;
                                case GRAVE:
                                    if (!tableColumnExists(CsDbContract.GraveEntry.CONTENT_URI, newColName)) {
                                        createColumn(CsDbContract.GraveEntry.TABLE_NAME, newColName, itemDataType);
                                        Log.d(LOG_TAG, "The column " + newColName + " DOES NOT exist in grave. CREATED.");
                                    }
                                    break;
                                default:
                                    throw new UnsupportedOperationException("Unexpected scope type: " + scope);
                            }
                        }

                        // Submit to SurveyCategory table
                        // check if the attributes field, required for some data types, is present
                        if (!itemDataType.equals(Utility.surveyDataTypes.TEXT) &&
                                !itemDataType.equals(Utility.surveyDataTypes.MEASUREMENT) &&
                                !itemDataType.equals(Utility.surveyDataTypes.BINARY)) {
                            // set, thumbnails, radio, binary
                            if (!jsonObject.has(ATTRIBUTES)) {
                                return "Template loading failed: Unable to parse the survey Template. The " + itemName + "'s '" + ATTRIBUTES + "' descriptor is missing";
                            }

                            // For radio, set, radio_thumbnails and thumbnails save the attribute values
                            Vector<ContentValues> contentValuesVector;
                            if (itemDataType.equals(Utility.surveyDataTypes.THUMBNAIL) || itemDataType.equals(Utility.surveyDataTypes.RADIO_THUMBNAIL)) {

                                // get list of files in Utility.dataPaths.THUMBNAILS + "/" + dirName
                                File folder = new File(Utility.dataPaths.THUMBNAILS + "/" + dirName);
                                if (!folder.exists()) {
                                    return "Template loading failed: The thumbnail folder named " + dirName + " was not found";
                                }
                                contentValuesVector = new Vector<ContentValues>(folder.listFiles().length);

                                int indexCounter = 0;
                                for (final File file : folder.listFiles()) {
                                    indexCounter += 1;

                                    // VALIDATION
                                    if (!p.matcher(file.getName()).matches()) {
                                        return "Template loading failed: Badly formatted file name: " + file.getName();
                                    }

                                    ContentValues attributeCv = new ContentValues();
                                    attributeCv.put(CsDbContract.SurveyAttributeEntry.COLUMN_CATEGORY_ID, surveyId);
                                    attributeCv.put(CsDbContract.SurveyAttributeEntry.COLUMN_NAME, file.getName());
                                    attributeCv.put(CsDbContract.SurveyAttributeEntry.COLUMN_ORDER, indexCounter);

                                    contentValuesVector.add(attributeCv);

                                    // Check that a resized version of the file exists in Utility.dataPaths.THUMBNAILS_SMALL + "/" + dirName + "/" + file
                                    File small_folder = new File(Utility.dataPaths.THUMBNAILS_SMALL + "/" + dirName);
                                    // Does the file already exist? Create it if not
                                    if (!small_folder.exists()) {
                                        // No, so try and make it
                                        if (!small_folder.mkdirs()) {
                                            throw new UnsupportedOperationException("Failed to make directory: " + small_folder.toString());
                                        }
                                    }

                                    // Check if the small version exists already - if so don't regenerate it
                                    String src_path = Utility.dataPaths.THUMBNAILS + "/" + dirName + "/" + file.getName(); // 'file' is full path but do this for similarity/clarity
                                    String dst_path = Utility.dataPaths.THUMBNAILS_SMALL + "/" + dirName + "/" + file.getName();

                                    File small_picture = new File(dst_path);
                                    if( !small_picture.exists() ) {

                                        // check if we can read and write to 'external' media
                                        if( !Utility.isExternalStorageReadable() || !Utility.isExternalStorageWritable() ) {
                                            return "Template loading failed. Unable to read/write to storage media.";
                                        }

                                        // Get the width/height of the image
                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        options.inJustDecodeBounds = true;

                                        // Returns null, sizes are in the options variable
                                        BitmapFactory.decodeFile(src_path, options);

                                        options.inSampleSize = Utility.calculateInSampleSize(options, context.getResources().getInteger(R.integer.picture_size), context.getResources().getInteger(R.integer.picture_size));
                                        options.inJustDecodeBounds = false;

                                        Bitmap smaller_bm = BitmapFactory.decodeFile(src_path, options);
                                        if( smaller_bm == null ) {
                                            return "Template loading failed. Device unable to load image '" + file.getName() + "' as it is too large.";
                                        }

                                        FileOutputStream fOut;
                                        try {
                                            fOut = new FileOutputStream(small_picture);
                                            smaller_bm.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
                                            fOut.flush();
                                            fOut.close();
                                            smaller_bm.recycle();
                                        } catch (Exception e) {
                                            return "Template loading failed. Couldn't save smaller thumbnail due to: " + e.toString();
                                        }
                                    }
                                }

                            } else {
                                // RADIO and SET
                                // Get JSONArray with attributes
                                JSONArray jsonAttributes = jsonObject.getJSONArray(ATTRIBUTES);

                                // Create a Vector of ContentValues to hold data for batch insert
                                contentValuesVector = new Vector<ContentValues>(jsonAttributes.length());

                                for (int attrib_index = 0; attrib_index < jsonAttributes.length(); attrib_index++) {

                                    String attName = jsonAttributes.getString(attrib_index);

                                    // VALIDATION
                                    if (!p.matcher(attName).matches()) {
                                        return "Template loading failed: Badly formatted file name: " + attName;
                                    }

                                    ContentValues attributeCv = new ContentValues();
                                    attributeCv.put(CsDbContract.SurveyAttributeEntry.COLUMN_CATEGORY_ID, surveyId);
                                    attributeCv.put(CsDbContract.SurveyAttributeEntry.COLUMN_NAME, attName);
                                    attributeCv.put(CsDbContract.SurveyAttributeEntry.COLUMN_ORDER, attrib_index + 1);

                                    contentValuesVector.add(attributeCv);
                                }
                            }
                            // Submit to SurveyAttributes table
                            int inserted = 0;

                            // add to database
                            if (contentValuesVector.size() > 0) {
                                // make ContentValues array the same size as our contentValuesVector
                                ContentValues[] contentValuesArray = new ContentValues[contentValuesVector.size()];
                                // convert contentValuesVector to Array
                                contentValuesVector.toArray(contentValuesArray);
                                inserted = getContentResolver().bulkInsert(CsDbContract.SurveyAttributeEntry.CONTENT_URI, contentValuesArray);
                            }
                        }

                        break;
                    default:
                        throw new UnsupportedOperationException("JSON Parsing encountered unexpected element type: " + itemType);
                }
            }

            return null;
        }
    }

    private boolean createColumn(String tableName, String newColName, String itemDataType) {
        if (itemDataType.equals(Utility.surveyDataTypes.BINARY) || itemDataType.equals(Utility.surveyDataTypes.MEASUREMENT)) {
            getContentResolver().insert(Uri.parse(CsDbContract.BASE_CONTENT_URI + "/" + CsDbContract.PATH_MAIN + "/new_column/" + tableName + "/" + newColName + "/INTEGER"), null);
        } else {
            getContentResolver().insert(Uri.parse(CsDbContract.BASE_CONTENT_URI + "/" + CsDbContract.PATH_MAIN + "/new_column/" + tableName + "/" + newColName + "/TEXT"), null);
        }
        return false;
    }

    private boolean tableColumnExists(Uri uri, String colName) {
        Cursor checkCursor = getContentResolver().query(uri, null, null, null, " NULL LIMIT 1");
        boolean exists = Arrays.asList(checkCursor.getColumnNames()).contains(colName);
        checkCursor.close();
        return exists;
    }

    public void exportData() {
        // export all the data to a date-time stamped file in export

        String[][] exportList = new String[][]{
                {CsDbContract.CemeteryEntry.CONTENT_URI.toString() + "/export", "cemetery.csv"},
                {CsDbContract.SectionEntry.CONTENT_URI.toString() + "/export", "section.csv"},
                {CsDbContract.GraveEntry.CONTENT_URI.toString() + "/export", "grave.csv"},
                {CsDbContract.CemeteryAttributesEntry.CONTENT_URI.toString() + "/export", "cemetery_attributes.csv"},
                {CsDbContract.SectionAttributesEntry.CONTENT_URI.toString() + "/export", "section_attributes.csv"},
                {CsDbContract.GraveAttributesEntry.CONTENT_URI.toString() + "/export", "grave_attributes.csv"},
                {CsDbContract.PictureEntry.CONTENT_URI.toString() + "/export", "pictures.csv"}
        };

        for (int f = 0; f < exportList.length; f++) {
            // Create a timestamped directory for export
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String exportDir = Utility.dataPaths.DATA_EXPORT + "/" + timeStamp;
            File folder = new File(exportDir);
            if (!folder.exists()) {
                // No, it shouldn't, so try and make it
                if (!folder.mkdirs()) {
                    throw new UnsupportedOperationException("Failed to make directory: " + exportDir);
                }
            }

            // Create the file to place the results in
            File file = new File(exportDir + "/" + exportList[f][1]);
            if (!file.exists()) {
                // Create file
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Failed to create file '" + file.toString() + "'" + e.toString(), e);
                }
            }

            // Now try to write to file
            try {
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));

                // retrieve results from cursor
                Cursor cursor = getContentResolver().query(Uri.parse(exportList[f][0]), null, null, null, null);

                // Are there any results?
                if (cursor.moveToFirst()) {

                    String[] headings = cursor.getColumnNames();

                    // iterate through rows and columns
                    for (int row = -1; row < cursor.getCount(); row++) {
                        String separator = ",";
                        for (int col = 0; col < cursor.getColumnCount(); col++) {
                            // remove comma separator for last item
                            if (col == (cursor.getColumnCount() - 1)) {
                                separator = "";
                            }

                            // print headings and data to file
                            if (row == -1) {
                                // print headings
                                buf.append("\"" + headings[col] + "\"" + separator);
                            } else {
                                // print data
                                buf.append("\"" + cursor.getString(col) + "\"" + separator);
                            }
                        }
                        buf.newLine();
                        // don't move cursor for header
                        if (row >= 0) cursor.moveToNext();
                    }
                }

                buf.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error writing to file '" + file.toString() + "'" + e.toString(), e);
            }
        }

        Toast.makeText(this.context, "Data export completed succesfully", Toast.LENGTH_LONG).show();
    }

    public void checkSurveyRequiredFields(String scope, Long scopeIdentifier) {

        Cursor catCursor = getContentResolver().query(CsDbContract.SurveyCategoryEntry.CONTENT_URI,
                null, // projection
                CsDbContract.SurveyCategoryEntry.COLUMN_REQUIRED + " = ? AND " +
                        CsDbContract.SurveyCategoryEntry.COLUMN_SCOPE + " = ? ", // selection
                new String[]{"1", scope}, // args
                null); // sortOrder

        if( !catCursor.moveToFirst() ) {
            // there are no required fields
            return;
        }

        // If we want to handle requirement checking for other scopes need to make changes here
        Cursor graveCursor = getContentResolver().query(CsDbContract.GraveEntry.CONTENT_URI,
                null, // projection
                CsDbContract.GraveEntry._ID + " = ? ", // selection
                new String[]{Long.toString(scopeIdentifier)}, // args
                null); // sortOrder

        if( !graveCursor.moveToFirst() ) {
            Log.e(LOG_TAG, "Error retrieving the Grave for which we are supposed to check the required fields.");
            return;
        }

        String scopeName = graveCursor.getString(graveCursor.getColumnIndex(CsDbContract.GraveEntry.COLUMN_GRAVE_NAME));
        String itemName;
        String itemTitle;
        String itemDataType;
        String combinedColName;
        String prefix = Utility.colNamesPrefix.DATA_COL_PREFIX;

        String missingRequirements = "";
        int totalMissingRequirements = 0;
        int displayMissingReqLimit = 4;

        while( catCursor.getPosition() < catCursor.getCount() ) {
            itemTitle = catCursor.getString(catCursor.getColumnIndex(CsDbContract.SurveyCategoryEntry.COLUMN_TITLE));
            itemName = catCursor.getString(catCursor.getColumnIndex(CsDbContract.SurveyCategoryEntry.COLUMN_NAME));
            itemDataType = catCursor.getString(catCursor.getColumnIndex(CsDbContract.SurveyCategoryEntry.COLUMN_TYPE));

            // This is the column name in the scope data tables
            combinedColName = prefix + itemName + "_" + itemDataType;

            // We only need to check this for THUMBNAILs and SETs
            if( itemDataType.equals(Utility.surveyDataTypes.THUMBNAIL) || itemDataType.equals(Utility.surveyDataTypes.SET) ) {
                // retrieve and check that there is at least one row
                Cursor attCursor = getContentResolver().query( CsDbContract.GraveAttributesEntry.CONTENT_URI,
                        null,
                        CsDbContract.GraveAttributesEntry.COLUMN_GRAVE_ID + " = ? AND " +
                        CsDbContract.GraveAttributesEntry.COLUMN_CATEGORY_NAME + " = ? ",
                        new String[]{Long.toString(scopeIdentifier), combinedColName},
                        " NULL LIMIT 1"
                );

                if( !attCursor.moveToFirst() ) {
                    totalMissingRequirements += 1;
                    // only record the first 3 messages for the user
                    if( totalMissingRequirements <= displayMissingReqLimit ) {
                        // no data for this required field
                        if(!missingRequirements.equals("")) {
                            missingRequirements += "\nThe '" + itemTitle + "' category of type '" + itemDataType + "' has not been completed for " + scope + " " + scopeName + ".";
                        } else {
                            missingRequirements += "The '" + itemTitle + "' category of type '" + itemDataType + "' has not been completed for " + scope + " " + scopeName + ".";
                        }
                    }
                }

                attCursor.close();

            } else {
                // measurement, binary, radio, text
                if (graveCursor.getString(graveCursor.getColumnIndex(combinedColName)) == null) {
                    totalMissingRequirements += 1;
                    // only record the first 3 messages for the user
                    if( totalMissingRequirements <= displayMissingReqLimit ) {
                        if(!missingRequirements.equals("")) {
                            missingRequirements += "\n" + itemTitle + " [" + itemDataType + "]";
                        } else {
                            missingRequirements += itemTitle + " [" + itemDataType + "]";
                        }
                    }
                }
            }

            catCursor.moveToNext();
        }

        if( !missingRequirements.equals("") ) {
            if( totalMissingRequirements > 3 ) {
                missingRequirements += "\n\nAn additional " + (totalMissingRequirements - displayMissingReqLimit) + " categories are also missing.";
            }

            // create Alert Dialogue Builder
            AlertDialog.Builder adb = new AlertDialog.Builder(context);
            LayoutInflater li = LayoutInflater.from(context);
            View infoView = li.inflate(R.layout.info_dialog, null);
            adb.setView(infoView);

            final TextView textViewHeading = (TextView) infoView.findViewById(R.id.edit_text_dialog_heading);
            final TextView textViewMessage = (TextView) infoView.findViewById(R.id.textview_dialog_message);
            textViewHeading.setText("Missing required fields for " + scope + " " + scopeName);
            textViewMessage.setText(missingRequirements);

            adb.setCancelable(false).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button

                }
            });

            // build/prompt user
            AlertDialog alertDialog = adb.create();
            alertDialog.show();

//            Toast.makeText(this, missingRequirements, Toast.LENGTH_LONG).show();
        }

        graveCursor.close();
        catCursor.close();
    }

    public void showLoadingScreen( boolean isLoading ) {
        if( isLoading ) {
            mProgressBarWheel.setVisibility(View.VISIBLE);
            mFab.setVisibility(View.GONE);
        } else {
            mProgressBarWheel.setVisibility(View.GONE);
            mFab.setVisibility(View.VISIBLE);
        }
    }
}
