package net.frakturmedia.cemeterysurvey;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.frakturmedia.cemeterysurvey.data.CsDbContract;

import java.io.File;

/**
 * Created by cyrille on 27/01/16.
 */
public class ListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = ListFragment.class.getSimpleName();

    // the CursorAdapter
    private CsCursorAdapter mCursorAdapter;

    // save position of scroll for device rotation
    private static final String LIST_STATE = "listState";
    private static Parcelable mlistState;

    // Loader ids
    //private static final int LOADER_ID_MAIN = 1;
    private static final int LOADER_ID_CEMETERY = 2;
    private static final int LOADER_ID_SECTION = 3;
    private static final int LOADER_ID_GRAVE = 4;
    private static final int LOADER_ID_PICTURE = 5;
    private static final int LOADER_ID_BOOKMARK = 6;
    private static final int LOADER_ID_SURVEY = 7;
    private static final int LOADER_ID_SURVEY_CATEGORY = 8;
    private static final int LOADER_ID_SURVEY_ATTRIBUTE = 9;

    // what we are displaying
    private static final String FRAGMENT_TYPE_KEY = "fragment_type";
    private static final String FRAGMENT_HEADING = "gridview_heading";
    private static final String FRAGMENT_SCOPE = "fragment_scope";
    private static final String PASS_IDENTIFIER = "identifier";

    private static final String CLICK_BEHAVIOUR = "short_click_behaviour";
    private static final String LONG_CLICK_BEHAVIOUR = "long_click_behaviour";

    // For when the fragment is inside a pager
    private static final String ARG_PAGE_NUMBER = "page_number";

    private String mFragmentType;
    private String mClickBehaviour;
    private String mLongClickBehaviour;

    // The list views
    private ListView mlistView = null;
    private GridView mgridView = null;

    // the loader id, set onCreateView
    private int mLoaderTypeId;

    public ListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Retrieve the passed message
        mFragmentType = getArguments().getString(FRAGMENT_TYPE_KEY);
        String layoutViewHeading = getArguments().getString(FRAGMENT_HEADING);
        String fragmentScope = getArguments().getString(FRAGMENT_SCOPE);  // can be "cemetery", "section", "grave"
        mClickBehaviour = getArguments().getString(CLICK_BEHAVIOUR);
        mLongClickBehaviour = getArguments().getString(LONG_CLICK_BEHAVIOUR);

        // specify the purpose of the loader in the first parameter - this will be used by
        // a switch statement in OnCreateLoader
        Bundle bundle = new Bundle();
        bundle.putString(FRAGMENT_SCOPE, fragmentScope);

        //Log.d(LOG_TAG, "ListFragment onCreateView for mFragmentType: " + mFragmentType);

        switch (mFragmentType) {
            case CsDbContract.PATH_CEMETERY:
                getLoaderManager().initLoader(LOADER_ID_CEMETERY, bundle, this); // calls OnCreateLoader
                break;
            case CsDbContract.PATH_SECTION:
                getLoaderManager().initLoader(LOADER_ID_SECTION, bundle, this); // calls OnCreateLoader
                break;
            case CsDbContract.PATH_GRAVE:
                getLoaderManager().initLoader(LOADER_ID_GRAVE, bundle, this); // calls OnCreateLoader
                break;
            case CsDbContract.PATH_PICTURE:
                getLoaderManager().initLoader(LOADER_ID_PICTURE, bundle, this); // calls OnCreateLoader
                break;
            case CsDbContract.PATH_BOOKMARK:
                getLoaderManager().initLoader(LOADER_ID_BOOKMARK, bundle, this); // calls OnCreateLoader
                break;
            case CsDbContract.PATH_SURVEY:
                getLoaderManager().initLoader(LOADER_ID_SURVEY, bundle, this); // calls OnCreateLoader
                break;
            case CsDbContract.PATH_SURVEY_CATEGORY:
                getLoaderManager().initLoader(LOADER_ID_SURVEY_CATEGORY, bundle, this); // calls OnCreateLoader
                break;
            case CsDbContract.PATH_SURVEY_ATTRIBUTE:
                getLoaderManager().initLoader(LOADER_ID_SURVEY_ATTRIBUTE, bundle, this); // calls OnCreateLoader
                break;
            default:
                throw new UnsupportedOperationException("Unknown mFragmentType: " + mFragmentType);
        }


        // inflate list_fragment in passed/defined container and hold as rootView
        View rootView;

        // For the SURVEY load a different layout than the rest
        switch(mFragmentType) {
            // If we want a list view instead of a gridview
            case CsDbContract.PATH_SURVEY_CATEGORY: {
                rootView = inflater.inflate(R.layout.survey_list_fragment, container, false);

                // set heading
                TextView headingTextView = (TextView) rootView.findViewById(R.id.fragment_textview_heading);
                if( layoutViewHeading.equals("_none") ) {
                    headingTextView.setVisibility(View.GONE);
                } else {
                    headingTextView.setText(layoutViewHeading);
                }

                // Create empty cursor adapter - we have to create the cursor first
                // We will reset it in the loader!
                mCursorAdapter = new CsCursorAdapter(getActivity(), null, 0);
                mCursorAdapter.setFragmentType(mFragmentType);

                // mount CursorAdapter to ListView
                mlistView = (ListView) rootView.findViewById(R.id.survey_listview_list);
                mlistView.setAdapter(mCursorAdapter);

                // OnClick
                switch (mClickBehaviour) {
                    case "select":
                        mlistView.setOnItemClickListener(new shortClick());
                        break;
                    case "none":
                        break;
                    default:
                        throw new UnsupportedOperationException("Unknown click behaviour: " + mClickBehaviour);
                }

                break;
            }
            case CsDbContract.PATH_SURVEY: {

                rootView = inflater.inflate(R.layout.survey_list_fragment, container, false);

                // set heading
                TextView headingTextView = (TextView) rootView.findViewById(R.id.fragment_textview_heading);
                if( layoutViewHeading.equals("_none") ) {
                    headingTextView.setVisibility(View.GONE);
                } else {
                    headingTextView.setText(layoutViewHeading);
                }

                // Create empty cursor adapter - we have to create the cursor first
                // We will reset it in the loader!
                mCursorAdapter = new CsCursorAdapter(getActivity(), null, 0);
                mCursorAdapter.setFragmentType(mFragmentType);

                // mount CursorAdapter to ListView
                mlistView = (ListView) rootView.findViewById(R.id.survey_listview_list);
                mlistView.setAdapter(mCursorAdapter);

                break;
            }
            default: {
                // All the other items operate on a different layout and use GridView

                // inflate list_fragment in passed/defined container and hold as rootView
                rootView = inflater.inflate(R.layout.list_fragment, container, false);

                // set heading
                TextView headingTextView = (TextView) rootView.findViewById(R.id.fragment_textview_heading);
                if( layoutViewHeading.equals("_none") ) {
                    headingTextView.setVisibility(View.GONE);
                } else {
                    headingTextView.setText(layoutViewHeading);
                }

                // Create empty cursor adapter - we have to create the cursor first
                // We will reset it in the loader!
                mCursorAdapter = new CsCursorAdapter(getActivity(), null, 0);
                mCursorAdapter.setFragmentType(mFragmentType);

                // mount CursorAdapter to GridView
                mgridView = (GridView) rootView.findViewById(R.id.gridview_list);
                mgridView.setAdapter(mCursorAdapter);

                // if this list item is editable
                switch (mLongClickBehaviour) {
                    case "edit":
                        mgridView.setOnItemLongClickListener(new longClickEdit());
                        break;
                    case "delete":
                        mgridView.setOnItemLongClickListener(new longClickDelete());
                        break;
                    case "none":
                        break;
                    default:
                        throw new UnsupportedOperationException("Unknown long click behaviour: " + mLongClickBehaviour);
                }

                // OnClick
                switch (mClickBehaviour) {
                    case "select":
                        mgridView.setOnItemClickListener(new shortClick());
                        break;
                    case "none":
                        break;
                    default:
                        throw new UnsupportedOperationException("Unknown click behaviour: " + mClickBehaviour);
                }

                break;
            }
        }

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // make the queries here

        // extract variables from bundle args
        String fragmentScope = args.getString(FRAGMENT_SCOPE);

        // Only ask for the cemetery, section or grave id when appropriate/expected
        Long scopeTypeIdentifier;
        CursorLoader loader = null;

        //Log.d(LOG_TAG, "Requesting cursor loader for " + mFragmentType + " with CURSOR LOADER ID: " + id);

        switch(id) {
            // these are the different types of content provider requests
            // The ForecastAdapter will take data from a source and
            // use it to populate the ListView it's attached to.

            case LOADER_ID_CEMETERY:
                loader = new CursorLoader(
                        this.getActivity(),
                        CsDbContract.CemeteryEntry.CONTENT_URI,
                        new String[]{CsDbContract.CemeteryEntry._ID, CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME},
                        null, null, null);
                break;
            case LOADER_ID_SECTION:
                scopeTypeIdentifier = getArguments().getLong(PASS_IDENTIFIER); // this should be the cemetery _id
                loader = new CursorLoader(
                        this.getActivity(),
                        CsDbContract.SectionEntry.buildSectionsInCemeteryIdUri(scopeTypeIdentifier),
                        new String[]{CsDbContract.SectionEntry._ID, CsDbContract.SectionEntry.COLUMN_SECTION_NAME},
                        null, null, null);
                break;
            case LOADER_ID_GRAVE:
                scopeTypeIdentifier = getArguments().getLong(PASS_IDENTIFIER); // this should be the section _id
                loader = new CursorLoader(
                        this.getActivity(),
                        CsDbContract.GraveEntry.buildGravesInSectionIdUri(scopeTypeIdentifier),
                        new String[]{CsDbContract.SectionEntry._ID, CsDbContract.GraveEntry.COLUMN_GRAVE_NAME, CsDbContract.GraveEntry.COLUMN_GRAVE_STATUS},
                        null, null, CsDbContract.SectionEntry._ID);
                break;
            case LOADER_ID_BOOKMARK:
                loader = new CursorLoader(
                        this.getActivity(),
                        CsDbContract.BookmarkEntry.CONTENT_URI.buildUpon().appendPath("joined").build(),
                        null, null, null, null);
                break;
            case LOADER_ID_PICTURE:
                String selection;
                scopeTypeIdentifier = getArguments().getLong(PASS_IDENTIFIER);
                String[] selectionArgs = new String[]{fragmentScope, Long.toString(scopeTypeIdentifier)};

                switch(fragmentScope) {
                    case CsDbContract.PATH_CEMETERY:
                        selection = "P." + CsDbContract.PictureEntry.COLUMN_SCOPE + " = ? AND P." + CsDbContract.PictureEntry.COLUMN_CEMETERY_ID + " = ? ";
                        break;
                    case CsDbContract.PATH_SECTION:
                        selection = "P." + CsDbContract.PictureEntry.COLUMN_SCOPE + " = ? AND P." + CsDbContract.PictureEntry.COLUMN_SECTION_ID + " = ? ";
                        break;
                    case CsDbContract.PATH_GRAVE:
                        selection = "P." + CsDbContract.PictureEntry.COLUMN_SCOPE + " = ? AND P." + CsDbContract.PictureEntry.COLUMN_GRAVE_ID + " = ? ";
                        break;
                    default:
                        throw new UnsupportedOperationException("Unknown fragment scope request: " + fragmentScope);
                }
                loader = new CursorLoader(
                        this.getActivity(),
                        CsDbContract.PictureEntry.CONTENT_URI.buildUpon().appendPath("joined").build(),
                        null, // projection
                        selection,
                        selectionArgs,
                        null);
                break;
            case LOADER_ID_SURVEY:
                // This will be used in the CursorAdapter
                scopeTypeIdentifier = getArguments().getLong(PASS_IDENTIFIER);
                int tabNumber = getArguments().getInt(ARG_PAGE_NUMBER, -1);

                loader = new CursorLoader(
                        this.getActivity(),
                        CsDbContract.SurveyCategoryEntry.buildSurveyCategoryScopeTypeScopeIdTabUri(fragmentScope, scopeTypeIdentifier, tabNumber),
                        null,
                        null,
                        null,
                        null);
                break;
            case LOADER_ID_SURVEY_CATEGORY:
                loader = new CursorLoader(
                        this.getActivity(),
                        CsDbContract.SurveyCategoryEntry.CONTENT_URI,
                        new String[]{CsDbContract.SurveyCategoryEntry._ID,
                                CsDbContract.SurveyCategoryEntry.COLUMN_TITLE,
                                CsDbContract.SurveyCategoryEntry.COLUMN_TYPE,
                                CsDbContract.SurveyCategoryEntry.COLUMN_SCOPE}
                        , CsDbContract.SurveyCategoryEntry.COLUMN_TYPE + " = ? OR " +
                                CsDbContract.SurveyCategoryEntry.COLUMN_TYPE + " = ? ",
                        new String[]{Utility.surveyDataTypes.SET,
                                Utility.surveyDataTypes.RADIO},
                        CsDbContract.SurveyCategoryEntry.COLUMN_SCOPE + ", " +
                                CsDbContract.SurveyCategoryEntry.COLUMN_TITLE + " ASC");
                break;
            case LOADER_ID_SURVEY_ATTRIBUTE:
                // This will be used in the CursorAdapter
                Long catId = getArguments().getLong(PASS_IDENTIFIER);

                loader = new CursorLoader(
                        this.getActivity(),
                        CsDbContract.SurveyAttributeEntry.CONTENT_URI,
                        new String[]{CsDbContract.SurveyAttributeEntry._ID,
                                CsDbContract.SurveyAttributeEntry.COLUMN_NAME}
                        , CsDbContract.SurveyAttributeEntry.COLUMN_CATEGORY_ID + " = ? ",
                        new String[]{Long.toString(catId)},
                        CsDbContract.SurveyAttributeEntry.COLUMN_NAME + " ASC");
                break;
            default:
                throw new UnsupportedOperationException("Did not find Loader id: " + id);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // handle the cursor loading results
        //Log.d(LOG_TAG, "Finished loading contents for cursor with CURSOR LOADER ID: " + loader.getId());
        // If you get an error here it may be because your query result does not have a row called '_id'
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
        //Log.d(LOG_TAG, "Disconnected cursor from CursorAdapter for " + mFragmentType);
    }

    @Override
    public void onPause() {
        // Save List/Grid View state @ onPause

        // as we have listviews and gridviews we need to have a variable for each
        if( mgridView != null ) {
            mlistState = mgridView.onSaveInstanceState();
        } else if( mlistView != null ) {
            mlistState = mlistView.onSaveInstanceState();
        }
        super.onPause();
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);

        if( mlistState != null ) {
            // as we have listviews and gridviews we need to have a variable for each
            if( mgridView != null ) {
                mgridView.onRestoreInstanceState(mlistState);
            } else if( mlistView != null) {
                mlistView.onRestoreInstanceState(mlistState);
            }
        }
    }

    public class longClickDelete implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {

            Cursor innerCursor = (Cursor) adapterView.getItemAtPosition(position);
            final long itemId = innerCursor.getLong(0);
            final String itemName = innerCursor.getString(1);

            // Modify/update cemetery/scope name
            // create Alert Dialogue Builder
            AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());

            LayoutInflater li = LayoutInflater.from(getActivity());
            View promptView = li.inflate(R.layout.input_dialog, null);

            final EditText editText = (EditText) promptView.findViewById(R.id.edit_text_dialog_user_input);
            editText.setVisibility(TextView.GONE);
            final TextView textViewHeading = (TextView) promptView.findViewById(R.id.edit_text_dialog_heading);
            textViewHeading.setText(R.string.delete_item_heading);

            // mount the view to the alert dialog builder
            adb.setView(promptView);
            adb.setCancelable(false).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    // User clicked OK button, now update!
                    ContentValues cv = new ContentValues();

                    int count = 0;
                    switch (mFragmentType) {
                        case CsDbContract.PATH_BOOKMARK:
                            // delete
                            count = getActivity().getContentResolver().delete(
                                    CsDbContract.BookmarkEntry.CONTENT_URI,
                                    CsDbContract.BookmarkEntry._ID + "= ?",
                                    new String[]{Long.toString(itemId)});
                            break;
                        default:
                            throw new UnsupportedOperationException("Delete fragment type request not found (not allowed?): " + mFragmentType);
                    }
                    if( count != 1 ) {
                        throw new UnsupportedOperationException("Failed to delete item: " + itemName);
                    }
                    // The cursor adapter should automatically update but we're doing something wrong
                    mCursorAdapter.notifyDataSetChanged();
                }
            });
            adb.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    dialog.cancel();
                    Toast.makeText(getActivity(), "Deletion cancelled", Toast.LENGTH_SHORT).show();
                }
            });

            // build/prompt user
            AlertDialog alertDialog = adb.create();
            alertDialog.show();

            // Consume the click - returning false would then call the regular onClick
            return true;
        }
    }

    public class longClickEdit implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {

            Cursor innerCursor = (Cursor) adapterView.getItemAtPosition(position);
            final long itemId = innerCursor.getLong(0);
            final String itemName = innerCursor.getString(1);

            // Modify/update cemetery/scope name
            // create Alert Dialogue Builder
            AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());

            LayoutInflater li = LayoutInflater.from(getActivity());
            View promptView = li.inflate(R.layout.input_dialog, null);

            final EditText editText = (EditText) promptView.findViewById(R.id.edit_text_dialog_user_input);
            editText.setText(itemName);
            final TextView textViewHeading = (TextView) promptView.findViewById(R.id.edit_text_dialog_heading);
            textViewHeading.setText("Edit " + mFragmentType + " name");

            // mount the view to the alert dialog builder
            adb.setView(promptView);
            adb.setCancelable(false).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    // User clicked OK button, now update!
                    ContentValues cv = new ContentValues();

                    int count = 0;
                    switch (mFragmentType) {
                        case CsDbContract.PATH_CEMETERY:
                            cv.put(CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME, editText.getText().toString());

                            // Update the DB
                            count = getActivity().getContentResolver().update(
                                    CsDbContract.CemeteryEntry.CONTENT_URI,
                                    cv,
                                    CsDbContract.CemeteryEntry._ID + "= ?",
                                    new String[] { Long.toString(itemId) });
                            break;
                        case CsDbContract.PATH_SECTION:
                            cv.put(CsDbContract.SectionEntry.COLUMN_SECTION_NAME, editText.getText().toString());

                            // Update the DB
                            count = getActivity().getContentResolver().update(
                                    CsDbContract.SectionEntry.CONTENT_URI,
                                    cv,
                                    CsDbContract.SectionEntry._ID + "= ?",
                                    new String[] { Long.toString(itemId) });
                            break;
                        case CsDbContract.PATH_GRAVE:
                            cv.put(CsDbContract.GraveEntry.COLUMN_GRAVE_NAME, editText.getText().toString());

                            // Update the DB
                            count = getActivity().getContentResolver().update(
                                    CsDbContract.GraveEntry.CONTENT_URI,
                                    cv,
                                    CsDbContract.GraveEntry._ID + "= ?",
                                    new String[] { Long.toString(itemId) });
                            break;
                        default:
                            throw new UnsupportedOperationException("Unknown fragment type request: " + mFragmentType);
                    }
                    if( count != 1 ) {
                        throw new UnsupportedOperationException("Failed to update scope name: " + itemName);
                    }
                    mCursorAdapter.notifyDataSetChanged();
                }
            });
            adb.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    dialog.cancel();
                    Toast.makeText(getActivity(), "Edit cancelled", Toast.LENGTH_SHORT).show();
                }
            });

            // build/prompt user
            AlertDialog alertDialog = adb.create();
            alertDialog.show();

            // Consume the click - returning false would then call the regular onClick
            return true;
        }
    }

    public class shortClick implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

            // CursorAdapter returns a cursor at the correct position for getItem(), or null
            // if it cannot seek to that position.
            Cursor innerCursor = (Cursor) adapterView.getItemAtPosition(position);
            Intent scopeActivity;

            if (innerCursor != null) {

                // call intent
                switch (mFragmentType) {
                    case CsDbContract.PATH_CEMETERY:
                        scopeActivity = new Intent(getActivity(), CemeteryActivity.class);
                        // pass the URI for this cemetery
                        scopeActivity.setData(CsDbContract.CemeteryEntry.buildCemeteryIdUri(innerCursor.getLong(0)));

                        scopeActivity.putExtra("id", innerCursor.getLong(0));
                        startActivity(scopeActivity);
                        return;

                    case CsDbContract.PATH_SECTION:
                        scopeActivity = new Intent(getActivity(), SectionActivity.class);
                        // pass the URI for this cemetery
                        scopeActivity.setData(CsDbContract.SectionEntry.buildSectionIdUri(innerCursor.getLong(0)));

                        scopeActivity.putExtra("id", innerCursor.getLong(0));
                        startActivity(scopeActivity);
                        return;

                    case CsDbContract.PATH_GRAVE:
                        scopeActivity = new Intent(getActivity(), GraveActivity.class);
                        // pass the URI for this cemetery
                        scopeActivity.setData(CsDbContract.GraveEntry.buildGraveIdUri(innerCursor.getLong(0)));

                        scopeActivity.putExtra("id", innerCursor.getLong(0));
                        // Unlike the others, start activity and get result
                        // Note we specify the startActivity from the ACTIVITY
                        // this will call the ACTIVITY's onActivityResult()
                        // rather than the FRAGMENT's onActivityResult()
                        getActivity().startActivityForResult(scopeActivity, Utility.resultCodes.GRAVE_ID_RESULT_CODE);
                        return;

                    case CsDbContract.PATH_BOOKMARK:
                        scopeActivity = new Intent(getActivity(), CemeteryActivity.class);

                        //Log.d(LOG_TAG, "Passing " + innerCursor.getLong(1) + ", " + innerCursor.getLong(2) + ", " + innerCursor.getLong(3) + ", " + innerCursor.getString(7));
                        scopeActivity.putExtra("bookmark", true);
                        scopeActivity.putExtra("id", innerCursor.getLong(1));
                        scopeActivity.putExtra("bookmark_section_id", innerCursor.getLong(2));
                        scopeActivity.putExtra("bookmark_grave_id", innerCursor.getLong(3));
                        scopeActivity.putExtra("bookmark_scope", innerCursor.getString(7));
                        startActivity(scopeActivity);
                        return;

                    case CsDbContract.PATH_PICTURE:
                        scopeActivity = new Intent(Intent.ACTION_VIEW);
                        File file = new File(Utility.dataPaths.PICTURES + "/" + innerCursor.getString(innerCursor.getColumnIndex(CsDbContract.PictureEntry.COLUMN_FILE_NAME)));
                        scopeActivity.setDataAndType(Uri.fromFile(file), "image/*");
                        startActivity(scopeActivity);
                        return;

                    case CsDbContract.PATH_SURVEY_CATEGORY:
                        view.setSelected(true);

                        ((AttributeAdder) getActivity()).displayAttributes(
                                innerCursor.getLong(innerCursor.getColumnIndex(CsDbContract.SurveyCategoryEntry._ID)),
                                innerCursor.getString(innerCursor.getColumnIndex(CsDbContract.SurveyCategoryEntry.COLUMN_TITLE))
                        );
                        return;

                    default:
                        throw new UnsupportedOperationException("Don't know how to handle onClick request 'select' for: " + mFragmentType);
                }
            }
        }
    }
}
