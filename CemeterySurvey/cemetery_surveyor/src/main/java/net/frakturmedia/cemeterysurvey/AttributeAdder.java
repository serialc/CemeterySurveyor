package net.frakturmedia.cemeterysurvey;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.frakturmedia.cemeterysurvey.data.CsDbContract;

public class AttributeAdder extends ScopeActivity {
    public static final String LOG_TAG = AttributeAdder.class.getSimpleName();

    private static final String FRAGMENT_TYPE_KEY = "fragment_type";
    private static final String FRAGMENT_HEADING = "gridview_heading";

    private static final String CLICK_BEHAVIOUR = "short_click_behaviour";
    private static final String LONG_CLICK_BEHAVIOUR = "long_click_behaviour";

    private static final String PASS_IDENTIFIER = "identifier";

    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attribute_adder);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show the up button in action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Display the list of categories in the left pane
        Bundle bundle = new Bundle();
        bundle.putString(FRAGMENT_TYPE_KEY, CsDbContract.PATH_SURVEY_CATEGORY);
        bundle.putString(FRAGMENT_HEADING, "Categories");
        bundle.putString(CLICK_BEHAVIOUR, "select");
        bundle.putString(LONG_CLICK_BEHAVIOUR, "none");

        ListFragment listFragment = new ListFragment();
        listFragment.setArguments(bundle);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.framelayout_categories_list, listFragment);
        ft.commit();
    }

    protected void displayAttributes(final Long catId, String catName) {
        final Long passCatId = catId;
        final String passCatName = catName;

        // display FAB now that we have the catId we would like to add an attribute to
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pop up dialog asking for new attribute name

                // create Alert Dialogue Builder
                AlertDialog.Builder adb = new AlertDialog.Builder(context);

                LayoutInflater li = LayoutInflater.from(context);
                View promptView = li.inflate(R.layout.input_dialog, null);

                final TextView textViewHeading = (TextView) promptView.findViewById(R.id.edit_text_dialog_heading);
                textViewHeading.setText("Enter new attribute name");
                final EditText userInput = (EditText) promptView.findViewById(R.id.edit_text_dialog_user_input);

                adb.setView(promptView);
                adb.setCancelable(false).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        int attOrder = 99;

                        // get the maximum order number for this cat
                        Cursor cursor = getContentResolver().query(CsDbContract.SurveyAttributeEntry.CONTENT_URI,
                                new String[]{" MAX(" + CsDbContract.SurveyAttributeEntry.COLUMN_ORDER + ") "},
                                CsDbContract.SurveyAttributeEntry.COLUMN_CATEGORY_ID + " = ? ",
                                new String[]{Long.toString(passCatId)},
                                null
                        );
                        if( cursor.moveToFirst() ) {
                            attOrder = cursor.getInt(0);
                        }
                        cursor.close();

                        // User clicked OK button
                        ContentValues cv = new ContentValues();
                        cv.put(CsDbContract.SurveyAttributeEntry.COLUMN_CATEGORY_ID, passCatId);
                        cv.put(CsDbContract.SurveyAttributeEntry.COLUMN_NAME, userInput.getText().toString());
                        cv.put(CsDbContract.SurveyAttributeEntry.COLUMN_ORDER, attOrder);
                        getContentResolver().insert(CsDbContract.SurveyAttributeEntry.CONTENT_URI, cv);

                        // Backup the JSON template file (parse it as well to make sure it's well formed
                        // Update the JSON template file
                        new templateWriter().execute();
                    }
                });
                adb.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.cancel();
                        Toast.makeText(context, "Creation of new attribute for category '" + passCatName + "' cancelled", Toast.LENGTH_SHORT).show();
                    }
                });

                // build/prompt user
                AlertDialog alertDialog = adb.create();
                alertDialog.show();
            }
        });

        // Display the list of categories in the left pane
        Bundle bundle = new Bundle();
        bundle.putString(FRAGMENT_TYPE_KEY, CsDbContract.PATH_SURVEY_ATTRIBUTE);
        bundle.putString(FRAGMENT_HEADING, "Category '" + catName + "' attributes");
        bundle.putString(CLICK_BEHAVIOUR, "none");
        bundle.putString(LONG_CLICK_BEHAVIOUR, "none");
        bundle.putLong(PASS_IDENTIFIER, catId);

        ListFragment listFragment = new ListFragment();
        listFragment.setArguments(bundle);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.framelayout_attributes_list, listFragment);
        ft.commit();
    }
}
