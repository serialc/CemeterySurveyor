package net.frakturmedia.cemeterysurvey;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import net.frakturmedia.cemeterysurvey.data.CsDbContract;

import java.io.File;

/**
 * Created by cyrille on 06/02/16.
 */
public class CsCursorAdapter extends CursorAdapter {

    public static final String LOG_TAG = CsCursorAdapter.class.getSimpleName();

    final String DATA_COL_PREFIX = Utility.colNamesPrefix.DATA_COL_PREFIX;

    private String mFragmentType;

    // See SURVEY_COLUMNS in CsProvider for order
    private static final int COL_ID = 0;
    private static final int COL_TYPE = 1;
    private static final int COL_SCOPE = 2;
    private static final int COL_TABNUM = 3;
    private static final int COL_GROUPNUM = 4;
    private static final int COL_CATNUM = 5;
    private static final int COL_TABNAME = 6;
    private static final int COL_GROUPNAME = 7;
    private static final int COL_CATNAME = 8;
    private static final int COL_TITLE = 9;
    private static final int COL_PICTURE = 10;
    private static final int COL_ATTPICTURE = 11;
    private static final int COL_REQUIRED = 12;
    private static final int COL_THUMBNAIL_PATH = 13;
    private static final int COL_ATTID = 0;
    private static final int COL_ATTORDER = 1;
    private static final int COL_ATTNAME = 2;
    private static final int COL_SCOPE_ATTNAME = 3;

    public static class ViewHolder {
        public final LinearLayout listItemParent;
        public final TextView headingView;
        public final ImageView imageView;

        public ViewHolder(View view) {
            listItemParent = (LinearLayout) view.findViewById(R.id.linearlayout_list_item);
            headingView = (TextView) view.findViewById(R.id.textview_item_heading);
            imageView = (ImageView) view.findViewById(R.id.imageview_item_picture);
        }
    }

    public static class ViewHolderStyled {
        public final TextView headingView;
        public final TextView subTextView;

        public ViewHolderStyled(View view) {
            headingView = (TextView) view.findViewById(R.id.textview_item_heading);
            subTextView = (TextView) view.findViewById(R.id.textview_item_subtext);
        }
    }

    public static class SurveyViewHolder {
        public final TextView headingTextView;
        public final TextView categoryNameTextView;
        public final EditText measurementEditView;
        public final EditText textEditView;
        public final ImageView cameraIcon;
        public final ToggleButton attToggleView;
        public final LinearLayout attLayout;
        public final TextView requiredField;

        public SurveyViewHolder(View view) {
            headingTextView = (TextView) view.findViewById(R.id.textview_survey_group_heading);
            categoryNameTextView = (TextView) view.findViewById(R.id.textview_survey_catname);
            measurementEditView = (EditText) view.findViewById(R.id.edittext_survey_measurement);
            textEditView = (EditText) view.findViewById(R.id.edittext_survey_text);
            cameraIcon = (ImageView) view.findViewById(R.id.imageview_camera_action);
            attToggleView = (ToggleButton) view.findViewById(R.id.togglebutton_survey_binary);
            attLayout = (LinearLayout) view.findViewById(R.id.linearlayout_attribute_list);
            requiredField = (TextView) view.findViewById(R.id.textview_survey_required);
        }
    }

    public CsCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view;

        // Use different xml layout for the different list types
        // point out what layout file will be the template
        switch (mFragmentType) {
            case CsDbContract.PATH_SURVEY_CATEGORY:
                view = LayoutInflater.from(context).inflate(R.layout.list_item_text_styled, parent, false);

                // attach to the view the variables so we can load them again rather then search each time
                ViewHolderStyled viewHolderStyled = new ViewHolderStyled(view);
                view.setTag(viewHolderStyled);
                break;

            case CsDbContract.PATH_SURVEY:
                view = LayoutInflater.from(context).inflate(R.layout.survey_list_item, parent, false);

                // attach to the view the variables so we can load them again rather then search each time
                SurveyViewHolder surveyViewHolder = new SurveyViewHolder(view);
                view.setTag(surveyViewHolder);
                break;

            default:
                view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

                // attach to the view the variables so we can load them again rather then search each time
                ViewHolder viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);
                break;
        }

        return view;
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //Log.d(LOG_TAG, "For " + mFragmentType + " at postion " + cursor.getPosition());
        // Retrieve the R.id from our view holder

        // Use different xml layout for the different list types
        switch (mFragmentType) {
            case CsDbContract.PATH_SURVEY:
                populateSurvey(view, context, cursor);
                break;

            case CsDbContract.PATH_SURVEY_CATEGORY:
                ViewHolderStyled viewHolderStyled = (ViewHolderStyled) view.getTag();
                viewHolderStyled.headingView.setText(cursor.getString(cursor.getColumnIndex(CsDbContract.SurveyCategoryEntry.COLUMN_TITLE)));
                viewHolderStyled.subTextView.setText(
                        (cursor.getString(cursor.getColumnIndex(CsDbContract.SurveyCategoryEntry.COLUMN_SCOPE)) + " - " +
                                cursor.getString(cursor.getColumnIndex(CsDbContract.SurveyCategoryEntry.COLUMN_TYPE))).toUpperCase());
                break;

            // All except the survey use the default view type
            default:
                ViewHolder viewHolder = (ViewHolder) view.getTag();

                String body = "";

                // Extract properties from cursor
                switch (mFragmentType) {
                    case CsDbContract.PATH_CEMETERY:
                        body = cursor.getString(cursor.getColumnIndex(CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME));
                        break;
                    case CsDbContract.PATH_SECTION:
                        body = cursor.getString(cursor.getColumnIndex(CsDbContract.SectionEntry.COLUMN_SECTION_NAME));
                        break;
                    case CsDbContract.PATH_GRAVE:
                        body = cursor.getString(cursor.getColumnIndex(CsDbContract.GraveEntry.COLUMN_GRAVE_NAME));
                        if (cursor.getInt(cursor.getColumnIndex(CsDbContract.GraveEntry.COLUMN_GRAVE_STATUS)) == 0) {
                            viewHolder.listItemParent.setBackgroundColor(0x66ff0000); // First byte is ALPHA, then RGB
                        }

                        break;
                    case CsDbContract.PATH_BOOKMARK:
                        String[] bookmark = new String[]{cursor.getString(4), cursor.getString(5), cursor.getString(6)};
                        if (bookmark[1] == null && bookmark[2] == null) {
                            body = bookmark[0];
                        } else if (bookmark[2] == null) {
                            body = bookmark[0] + ":" + bookmark[1];
                        } else {
                            body = bookmark[0] + ":" + bookmark[1] + ":" + bookmark[2];
                        }
                        break;
                    case CsDbContract.PATH_PICTURE:
                        // cemetery, section, grave, category name, attribute name
                        // cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getString(8)

                        // Cemetery
                        body += cursor.getString(4) == null ? "" : cursor.getString(4);
                        // Section
                        body += cursor.getString(5) == null ? "" : " " + cursor.getString(5);
                        // Grave
                        body += cursor.getString(6) == null ? "" : " " + cursor.getString(6);

                        String categoryAttribute = "";
                        // Category
                        categoryAttribute += cursor.getString(7) == null ? "" : cursor.getString(7);
                        // Attribute
                        if( categoryAttribute != "" && cursor.getString(8) != null ) {
                            categoryAttribute += ": " + cursor.getString(8);
                        }

                        if( categoryAttribute != "" ) {
                            body += " (" + categoryAttribute + " )";
                        }

                        // create path Uri to picture
                        String picturePath = Utility.dataPaths.PICTURES + "/" + cursor.getString(9);
                        String pictureFullPath = new File(picturePath).getAbsolutePath();
                        File pictureFile = new File(picturePath);

                        //viewHolder.imageView.setVisibility(View.VISIBLE);
                        //viewHolder.imageView.setImageURI(Uri.parse(picturePath));

                        // Resample picture to required size, otherwise we run out of memory
                        if (pictureFile.exists()) {
                            // define the resolution options
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeFile(pictureFullPath, options);
                            // Calculate inSampleSize
                            options.inSampleSize = Utility.calculateInSampleSize(options, context.getResources().getInteger(R.integer.picture_size), context.getResources().getInteger(R.integer.picture_size));
                            // Decode bitmap with inSampleSize set
                            options.inJustDecodeBounds = false;

                            viewHolder.imageView.setVisibility(View.VISIBLE);
                            viewHolder.imageView.setImageBitmap(BitmapFactory.decodeFile(pictureFullPath, options));
                        } else {
                            Log.e(LOG_TAG, "Could not find picture " + pictureFile.toString());
                        }

                        break;

                    case CsDbContract.PATH_SURVEY_ATTRIBUTE:
                        body = cursor.getString(cursor.getColumnIndex(CsDbContract.SurveyAttributeEntry.COLUMN_NAME));
                        break;
                    case "delete_cemetery_items":
                        body = cursor.getString(cursor.getColumnIndex(CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME));
                        break;

                    default:
                        throw new UnsupportedOperationException("Unknown cusrorAdapter mFragmentType: " + mFragmentType);
                }

                // Populate fields with extracted properties
                viewHolder.headingView.setText(body);
        }
    }

    private void populateSurvey(View view, final Context context, Cursor cursor) {
        /*
        Remember that views are RECYCLED!
        This means that parts are reused! If I set a part of a view to visible it will stay that
        way when it is reused! Don't take the visibility/hidden nature of a view for granted - Be EXPLICIT!
         */

        // Retrieve the object holding the permenant references the xml layout elements
        final SurveyViewHolder surveyViewHolder = (SurveyViewHolder) view.getTag();

        //Log.d(LOG_TAG, "Cursor COLUMN NAMES= " + Arrays.toString(cursor.getColumnNames()));

        // Hide all the views! Remember they are REUSED!
        // Also need to remove any views we have added!
        surveyViewHolder.headingTextView.setVisibility(View.GONE);
        surveyViewHolder.categoryNameTextView.setVisibility(View.GONE);
        surveyViewHolder.measurementEditView.setVisibility(View.GONE);
        surveyViewHolder.textEditView.setVisibility(View.GONE);
        surveyViewHolder.cameraIcon.setVisibility(View.GONE);
        surveyViewHolder.attToggleView.setVisibility(View.GONE);
        surveyViewHolder.attLayout.setVisibility(View.GONE);
        surveyViewHolder.requiredField.setVisibility(View.GONE);
        surveyViewHolder.attLayout.removeAllViews();

        // Get the general parts from the cursor
        final Long scopeId = cursor.getLong(cursor.getColumnIndex("scopeId"));
        final String scopeIdStr = Long.toString(scopeId);
        final Long catId = cursor.getLong(COL_ID);
        final String scope = cursor.getString(COL_SCOPE);
        final Boolean catRequired = cursor.getInt(COL_REQUIRED) != 0;
        final String catType = cursor.getString(COL_TYPE);
        final String catName = cursor.getString(COL_CATNAME);
        final String catFullName = prefixColumnName(catName, catType);
        String catTitle = cursor.getString(COL_TITLE);

        // Get the current/updated state of the scope data for this scopeId
        final Cursor scopeCursor = context.getContentResolver().query(getScopeUri(scope).buildUpon().appendPath(scopeIdStr).build(),
                null, null, null, null);

        if( !scopeCursor.moveToFirst() ) {
            Log.e(LOG_TAG, "Unable to retrieve the scope data for this scopeId. Expect other errors.");
        }

        // Do we display the GROUP heading
        // Show Group heading if we are the first category in this group
        if (cursor.getInt(COL_CATNUM) == 1) {
            String groupTitle = cursor.getString(COL_GROUPNAME);

            if (!groupTitle.equals("_blank")) {
                surveyViewHolder.headingTextView.setText(groupTitle);
                surveyViewHolder.headingTextView.setVisibility(View.VISIBLE);
            }
        }

        // Show whether the category is required
        if( catRequired ) {
            surveyViewHolder.requiredField.setVisibility(View.VISIBLE);
        }

        // How do we display the CATEGORY heading and,
        // do attribute generation for set, thumbnail and radio
        switch (catType) {
            case Utility.surveyDataTypes.TEXT:
                // Show catTitle as hint
                surveyViewHolder.textEditView.setHint(catTitle);
                surveyViewHolder.textEditView.setVisibility(View.VISIBLE);
                // set state/value
                surveyViewHolder.textEditView.setText(scopeCursor.getString(scopeCursor.getColumnIndex(catFullName)));

                // Add on lose focus, save
                surveyViewHolder.textEditView.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if(!hasFocus){
                            // save
                            ContentValues cv = new ContentValues();
                            cv.put(catFullName, surveyViewHolder.textEditView.getText().toString());

                            // update the column
                            int updatedRows = v.getContext().getContentResolver().update(getScopeUri(scope),
                                    cv,  // content values
                                    " _id = ? ", // select
                                    new String[]{scopeIdStr}); // selectArgs
                            if( updatedRows != 1 ) {
                                Log.e(LOG_TAG, "Updated number of rows is problematic=" + updatedRows);
                            }

                            v.clearFocus();
                        }
                    }
                });

                break;

            case Utility.surveyDataTypes.MEASUREMENT:
                // Show catTitle as hint
                surveyViewHolder.measurementEditView.setHint(catTitle);
                surveyViewHolder.measurementEditView.setVisibility(View.VISIBLE);
                // set state/value
                surveyViewHolder.measurementEditView.setText(scopeCursor.getString(scopeCursor.getColumnIndex(catFullName)));

                // Add on lose focus, save
                surveyViewHolder.measurementEditView.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if( !hasFocus ) {
                            // save
                            ContentValues cv = new ContentValues();
                            cv.put(catFullName,
                                    surveyViewHolder.measurementEditView.getText().toString());

                            // update the column
                            int updatedRows = v.getContext().getContentResolver().update(getScopeUri(scope),
                                    cv,  // content values
                                    " _id = ? ", // select
                                    new String[]{scopeIdStr}); // selectArgs
                            if( updatedRows != 1 ) {
                                Log.e(LOG_TAG, "Updated number of rows is problematic=" + updatedRows);
                            }

                            v.clearFocus();
                        }
                    }
                });

                break;

            case Utility.surveyDataTypes.BINARY:
                // Show category catTitle heading
                surveyViewHolder.categoryNameTextView.setText(catTitle);
                surveyViewHolder.categoryNameTextView.setVisibility(View.VISIBLE);
                surveyViewHolder.attToggleView.setVisibility(View.VISIBLE);
                // set state
                surveyViewHolder.attToggleView.setChecked(scopeCursor.getInt(scopeCursor.getColumnIndex(catFullName)) != 0);

                // set onclick handler
                surveyViewHolder.attToggleView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ContentValues cv = new ContentValues();
                        cv.put(catFullName, surveyViewHolder.attToggleView.isChecked());

                        // update the column
                        int updatedRows = v.getContext().getContentResolver().update(getScopeUri(scope),
                                cv,  // content values
                                " _id = ? ", // select
                                new String[]{scopeIdStr}); // selectArgs
                        if( updatedRows != 1 ) {
                            Log.e(LOG_TAG, "Updated number of rows is problematic=" + updatedRows);
                        }
                    }
                });

                break;

            case Utility.surveyDataTypes.RADIO:
            case Utility.surveyDataTypes.SET:
            case Utility.surveyDataTypes.THUMBNAIL:
            case Utility.surveyDataTypes.RADIO_THUMBNAIL:
                // Show category catTitle heading
                surveyViewHolder.categoryNameTextView.setText(catTitle);
                surveyViewHolder.categoryNameTextView.setVisibility(View.VISIBLE);

                // Get the survey attributes for the category
                final Cursor attCursor = context.getContentResolver().query(
                        CsDbContract.SurveyAttributeEntry.buildSurveyAttributeCategoryIdUri(catId).buildUpon()
                                .appendPath(scopeIdStr)
                                .appendPath(scope)
                                .appendPath(catFullName)
                                .build(),
                        null, null, null, null);

                int attCursorRowCount = attCursor.getCount();

                // Have we retrieved any attributes - we should have
                if (attCursor.moveToFirst()) {

                    // The radio data type is different as it only stores ONE value in the main cursor - not the attCursor
                    String radioSelection = null;
                    if( catType.equals(Utility.surveyDataTypes.RADIO) || catType.equals(Utility.surveyDataTypes.RADIO_THUMBNAIL) ) {
                        // get the attribute name for the selected radio attribute
                        radioSelection = scopeCursor.getString(scopeCursor.getColumnIndex(catFullName));
                    }

                    // we will inflate some xml and put it into our containers
                    LayoutInflater inflater = LayoutInflater.from(context);

                    // make the LinearLayout attribute container visible
                    surveyViewHolder.attLayout.setVisibility(View.VISIBLE);

                    int colNum = context.getResources().getInteger(R.integer.grid_view_colnum);
                    // Use different number of columns for thumbnails
                    if( catType.equals(Utility.surveyDataTypes.THUMBNAIL) || catType.equals(Utility.surveyDataTypes.RADIO_THUMBNAIL)) {
                        colNum = context.getResources().getInteger(R.integer.grid_view_colnum_dense);
                    }

                    // See if we can reduce the colNum by redistributing:
                    // With six elements, have 3 on each row rather than 4 and 2
                    int resultingRowNum  = (int) ((attCursorRowCount / (double)colNum) + .99);

                    while( ((int) ((attCursorRowCount / (double)(colNum - 1)) + .99) ) == resultingRowNum ) {
                        colNum -= 1;
                    }

                    // Do the attributes for this category need the camera action?
                    boolean attCameraEnabled = cursor.getInt(COL_ATTPICTURE) != 0;

                    // Get the thumbnails dirName
                    String catThumbDir = cursor.getString(COL_THUMBNAIL_PATH);

                    LinearLayout attributesLayout;

                    while (attCursor.getPosition() < attCursorRowCount) {
                        attributesLayout = new LinearLayout(context);
                        attributesLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                        int col = 0;
                        while (col < colNum && attCursor.getPosition() < attCursorRowCount) {

                            View attView = null;
                            final String attName = attCursor.getString(COL_ATTNAME);
                            final Long attId = attCursor.getLong(COL_ATTID);

                            // inflate the survey_list_item_attribute and attach it to the linearLayout
                            switch (catType) {

                                // RADIO_THUMBNAIL
                                case Utility.surveyDataTypes.RADIO_THUMBNAIL: {
                                    attView = inflater.inflate(R.layout.survey_list_item_attribute_image, attributesLayout, false);
                                    final ImageButton attImage = (ImageButton) attView.findViewById(R.id.imagebutton_survey_attribute_thumbnail);

                                    final String picturePath = Utility.dataPaths.THUMBNAILS_SMALL + "/" + catThumbDir + "/" + attName;
                                    final File pictureFile = new File(picturePath);
                                    final File pictureFileFullSize = new File(Utility.dataPaths.THUMBNAILS + "/" + catThumbDir + "/" + attName);

                                    // Resample image to get bit for destined size
                                    if (pictureFile.exists()) {
                                        // define the resolution options
//                                        BitmapFactory.Options options = new BitmapFactory.Options();
//                                        options.inJustDecodeBounds = true;
//                                        BitmapFactory.decodeFile(pictureFullPath, options);
//                                        // Calculate inSampleSize
//                                        options.inSampleSize = Utility.calculateInSampleSize(options, context.getResources().getInteger(R.integer.picture_size), context.getResources().getInteger(R.integer.picture_size));
//                                        // Decode bitmap with inSampleSize set
//                                        options.inJustDecodeBounds = false;
//
//                                        attImage.setImageBitmap(BitmapFactory.decodeFile(pictureFullPath, options));
                                        // Use the raw image
                                        attImage.setImageURI(Uri.parse(picturePath));
                                    } else {
                                        Log.e(LOG_TAG, "Could not find picture " + pictureFile.toString());
                                    }

                                    // Check if it should be displayed as selected
                                    if( radioSelection != null && radioSelection.equals(attName)) {
                                        attImage.setSelected(true);
                                        attImage.setColorFilter(view.getResources().getColor(R.color.sunshine_light_blue), PorterDuff.Mode.MULTIPLY);
                                    } else {
                                        attImage.clearColorFilter();
                                    }

                                    final boolean sIsSelected = attImage.isSelected();

                                    // set onclick handler
                                    attImage.setOnClickListener(new View.OnClickListener() {

                                        @Override
                                        public void onClick(View v) {

                                            // UPDATE
                                            ContentValues cv = new ContentValues();


                                            int updatedRows;
                                            if( sIsSelected ) {
                                                // update the column
                                                cv.putNull(catFullName);
                                                updatedRows = v.getContext().getContentResolver().update(getScopeUri(scope),
                                                        cv,  // content values
                                                        " _id = ? ", // select
                                                        new String[]{scopeIdStr}); // selectArgs
                                            } else {
                                                // update the column
                                                cv.put(catFullName, attName);
                                                updatedRows = v.getContext().getContentResolver().update(getScopeUri(scope),
                                                        cv,  // content values
                                                        " _id = ? ", // select
                                                        new String[]{scopeIdStr}); // selectArgs
                                            }

                                            if (updatedRows != 1) {
                                                Log.e(LOG_TAG, "Updated number of rows is problematic=" + updatedRows);
                                            }
                                            Log.d(LOG_TAG, "Rows updated for radio_thumbnail button = " + updatedRows);

                                            notifyDataSetChanged();
                                        }
                                    });

                                    attImage.setOnLongClickListener(new View.OnLongClickListener() {

                                        @Override
                                        public boolean onLongClick(View v) {

                                            Intent showThumbnail = new Intent(Intent.ACTION_VIEW);
                                            showThumbnail.setDataAndType(Uri.fromFile(pictureFileFullSize), "image/*");
                                            v.getContext().startActivity(showThumbnail);

                                            return true;
                                        }
                                    });

                                    break;
                                }

                                // THUMBNAIL
                                case Utility.surveyDataTypes.THUMBNAIL: {

                                    attView = inflater.inflate(R.layout.survey_list_item_attribute_image, attributesLayout, false);
                                    final ImageButton attImage = (ImageButton) attView.findViewById(R.id.imagebutton_survey_attribute_thumbnail);

                                    final String picturePath = Utility.dataPaths.THUMBNAILS_SMALL + "/" + catThumbDir + "/" + attName;
                                    final File pictureFile = new File(picturePath);
                                    final File pictureFileFullSize = new File(Utility.dataPaths.THUMBNAILS + "/" + catThumbDir + "/" + attName);

                                    // Resample image to get bit for destined size
                                    if (pictureFile.exists()) {
                                        // define the resolution options (no longer needed as we generate the small thumbnails now)
//                                        BitmapFactory.Options options = new BitmapFactory.Options();
//                                        options.inJustDecodeBounds = true;
//                                        BitmapFactory.decodeFile(picturePath, options);
//                                        // Calculate inSampleSize
//                                        options.inSampleSize = Utility.calculateInSampleSize(options, context.getResources().getInteger(R.integer.picture_size), context.getResources().getInteger(R.integer.thumbnail_size_dense));
//                                        // Decode bitmap with inSampleSize set
//                                        options.inJustDecodeBounds = false;
//
//                                        attImage.setImageBitmap(BitmapFactory.decodeFile(picturePath, options));

                                        // Use the raw image
                                        attImage.setImageURI(Uri.parse(picturePath));
                                    } else {
                                        Log.e(LOG_TAG, "Could not find picture " + pictureFile.toString());
                                    }

                                    // Check if it should be displayed as selected
                                    if( attCursor.getString(COL_SCOPE_ATTNAME) != null ) {
                                        attImage.setSelected(true);
                                        attImage.setColorFilter(view.getResources().getColor(R.color.sunshine_light_blue), PorterDuff.Mode.MULTIPLY);
                                    } else {
                                        attImage.clearColorFilter();
                                    }

                                    final boolean sIsSelected = attImage.isSelected();

                                    // set onclick handler
                                    attImage.setOnClickListener(new View.OnClickListener() {

                                        @Override
                                        public void onClick(View v) {

                                            if( sIsSelected ) {
                                                // DELETE
                                                int deletedRows;

                                                switch(scope) {
                                                    case CsDbContract.PATH_CEMETERY:
                                                        deletedRows = v.getContext().getContentResolver().delete(CsDbContract.CemeteryAttributesEntry.CONTENT_URI
                                                                        .buildUpon().appendPath(scopeIdStr).appendPath(catFullName).appendPath(attName).build(),
                                                                null, null);
                                                        break;
                                                    case CsDbContract.PATH_SECTION:
                                                        deletedRows = v.getContext().getContentResolver().delete(CsDbContract.SectionAttributesEntry.CONTENT_URI
                                                                        .buildUpon().appendPath(scopeIdStr).appendPath(catFullName).appendPath(attName).build(),
                                                                null, null);
                                                        break;
                                                    case CsDbContract.PATH_GRAVE:
                                                        deletedRows = v.getContext().getContentResolver().delete(CsDbContract.GraveAttributesEntry.CONTENT_URI
                                                                        .buildUpon().appendPath(scopeIdStr).appendPath(catFullName).appendPath(attName).build(),
                                                                null, null);
                                                        break;
                                                    default:
                                                        throw new UnsupportedOperationException("Unknown scope type: " + scope);
                                                }
                                                Log.d(LOG_TAG, "Deleted rows=" + deletedRows);
                                            } else {
                                                // INSERT
                                                ContentValues cv = new ContentValues();
                                                Uri uri;
                                                switch(scope) {
                                                    case CsDbContract.PATH_CEMETERY:
                                                        cv.put(CsDbContract.CemeteryAttributesEntry.COLUMN_CEMETERY_ID, scopeIdStr);
                                                        cv.put(CsDbContract.CemeteryAttributesEntry.COLUMN_CATEGORY_NAME, catFullName);
                                                        cv.put(CsDbContract.CemeteryAttributesEntry.COLUMN_ATTRIBUTE_NAME, attName);
                                                        uri = v.getContext().getContentResolver().insert(CsDbContract.CemeteryAttributesEntry.CONTENT_URI, cv);
                                                        break;
                                                    case CsDbContract.PATH_SECTION:
                                                        cv.put(CsDbContract.SectionAttributesEntry.COLUMN_SECTION_ID, scopeIdStr);
                                                        cv.put(CsDbContract.SectionAttributesEntry.COLUMN_CATEGORY_NAME, catFullName);
                                                        cv.put(CsDbContract.SectionAttributesEntry.COLUMN_ATTRIBUTE_NAME, attName);
                                                        uri = v.getContext().getContentResolver().insert(CsDbContract.SectionAttributesEntry.CONTENT_URI, cv);
                                                        break;
                                                    case CsDbContract.PATH_GRAVE:
                                                        cv.put(CsDbContract.GraveAttributesEntry.COLUMN_GRAVE_ID, scopeIdStr);
                                                        cv.put(CsDbContract.GraveAttributesEntry.COLUMN_CATEGORY_NAME, catFullName);
                                                        cv.put(CsDbContract.GraveAttributesEntry.COLUMN_ATTRIBUTE_NAME, attName);
                                                        uri = v.getContext().getContentResolver().insert(CsDbContract.GraveAttributesEntry.CONTENT_URI, cv);
                                                        break;
                                                    default:
                                                        throw new UnsupportedOperationException("Unknown scope type: " + scope);
                                                }

                                                Log.d(LOG_TAG, "New uri for SET creation: " + uri);
                                            }
                                            notifyDataSetChanged();
                                        }
                                    });

                                    attImage.setOnLongClickListener(new View.OnLongClickListener() {

                                        @Override
                                        public boolean onLongClick(View v) {

                                            Intent showThumbnail = new Intent(Intent.ACTION_VIEW);
                                            showThumbnail.setDataAndType(Uri.fromFile(pictureFileFullSize), "image/*");
                                            v.getContext().startActivity(showThumbnail);

                                            return true;
                                        }
                                    });

                                    break;
                                }

                                // RADIO
                                case Utility.surveyDataTypes.RADIO: {

                                    attView = inflater.inflate(R.layout.survey_list_item_attribute, attributesLayout, false);
                                    Button attButton = (Button) attView.findViewById(R.id.button_survey_attribute);
                                    attButton.setText(attName);

                                    if( radioSelection != null && radioSelection.equals(attName)) {
                                        attButton.setSelected(true);
                                    }

                                    // set onclick handler
                                    attButton.setOnClickListener(new View.OnClickListener() {

                                        @Override
                                        public void onClick(View v) {

                                            // UPDATE
                                            ContentValues cv = new ContentValues();
                                            cv.put(catFullName, attName);

                                            // update the column
                                            int updatedRows = v.getContext().getContentResolver().update(getScopeUri(scope),
                                                    cv,  // content values
                                                    " _id = ? ", // select
                                                    new String[]{scopeIdStr}); // selectArgs
                                            if (updatedRows != 1) {
                                                Log.e(LOG_TAG, "Updated number of rows is problematic=" + updatedRows);
                                            }
                                            Log.d(LOG_TAG, "Rows updated for radio button = " + updatedRows);

                                            notifyDataSetChanged();
                                        }
                                    });

                                    // long click clears radio button
                                    attButton.setOnLongClickListener(new View.OnLongClickListener() {

                                        @Override
                                        public boolean onLongClick(View v) {
                                            // if this button is active
                                            if (v.isSelected()) {
                                                // UPDATE
                                                ContentValues cv = new ContentValues();
                                                cv.putNull(catFullName);

                                                // update the column
                                                int updatedRows = v.getContext().getContentResolver().update(getScopeUri(scope),
                                                        cv,  // content values
                                                        " _id = ? ", // select
                                                        new String[]{scopeIdStr}); // selectArgs
                                                if (updatedRows != 1) {
                                                    Log.e(LOG_TAG, "Updated number of rows is problematic=" + updatedRows);
                                                }
                                                Log.d(LOG_TAG, "Rows updated for set button = " + updatedRows);

                                                notifyDataSetChanged();
                                            }
                                            return true;
                                        }
                                    });

                                    break;
                                }

                                // SET
                                case Utility.surveyDataTypes.SET: {

                                    attView = inflater.inflate(R.layout.survey_list_item_attribute, attributesLayout, false);
                                    Button attButton = (Button) attView.findViewById(R.id.button_survey_attribute);
                                    attButton.setText(attName);

                                    // If this variable is not equal to null, it is 'on'
                                    if( attCursor.getString(COL_SCOPE_ATTNAME) != null ) {
                                        attButton.setSelected(true);
                                    }

                                    final boolean sIsSelected = attButton.isSelected();

                                    // set onclick handler
                                    attButton.setOnClickListener(new View.OnClickListener() {

                                        @Override
                                        public void onClick(View v) {

                                            if (sIsSelected) {
                                                // DELETE
                                                int deletedRows;

                                                switch (scope) {
                                                    case CsDbContract.PATH_CEMETERY:
                                                        deletedRows = v.getContext().getContentResolver().delete(CsDbContract.CemeteryAttributesEntry.CONTENT_URI
                                                                        .buildUpon().appendPath(scopeIdStr).appendPath(catFullName).appendPath(attName).build(),
                                                                null, null);
                                                        break;
                                                    case CsDbContract.PATH_SECTION:
                                                        deletedRows = v.getContext().getContentResolver().delete(CsDbContract.SectionAttributesEntry.CONTENT_URI
                                                                        .buildUpon().appendPath(scopeIdStr).appendPath(catFullName).appendPath(attName).build(),
                                                                null, null);
                                                        break;
                                                    case CsDbContract.PATH_GRAVE:
                                                        deletedRows = v.getContext().getContentResolver().delete(CsDbContract.GraveAttributesEntry.CONTENT_URI
                                                                        .buildUpon().appendPath(scopeIdStr).appendPath(catFullName).appendPath(attName).build(),
                                                                null, null);
                                                        break;
                                                    default:
                                                        throw new UnsupportedOperationException("Unknown scope type: " + scope);
                                                }
                                                Log.d(LOG_TAG, "Deleted rows=" + deletedRows);
                                            } else {
                                                // INSERT
                                                ContentValues cv = new ContentValues();
                                                Uri uri;
                                                switch (scope) {
                                                    case CsDbContract.PATH_CEMETERY:
                                                        cv.put(CsDbContract.CemeteryAttributesEntry.COLUMN_CEMETERY_ID, scopeIdStr);
                                                        cv.put(CsDbContract.CemeteryAttributesEntry.COLUMN_CATEGORY_NAME, catFullName);
                                                        cv.put(CsDbContract.CemeteryAttributesEntry.COLUMN_ATTRIBUTE_NAME, attName);
                                                        uri = v.getContext().getContentResolver().insert(CsDbContract.CemeteryAttributesEntry.CONTENT_URI, cv);
                                                        break;
                                                    case CsDbContract.PATH_SECTION:
                                                        cv.put(CsDbContract.SectionAttributesEntry.COLUMN_SECTION_ID, scopeIdStr);
                                                        cv.put(CsDbContract.SectionAttributesEntry.COLUMN_CATEGORY_NAME, catFullName);
                                                        cv.put(CsDbContract.SectionAttributesEntry.COLUMN_ATTRIBUTE_NAME, attName);
                                                        uri = v.getContext().getContentResolver().insert(CsDbContract.SectionAttributesEntry.CONTENT_URI, cv);
                                                        break;
                                                    case CsDbContract.PATH_GRAVE:
                                                        cv.put(CsDbContract.GraveAttributesEntry.COLUMN_GRAVE_ID, scopeIdStr);
                                                        cv.put(CsDbContract.GraveAttributesEntry.COLUMN_CATEGORY_NAME, catFullName);
                                                        cv.put(CsDbContract.GraveAttributesEntry.COLUMN_ATTRIBUTE_NAME, attName);
                                                        uri = v.getContext().getContentResolver().insert(CsDbContract.GraveAttributesEntry.CONTENT_URI, cv);
                                                        break;
                                                    default:
                                                        throw new UnsupportedOperationException("Unknown scope type: " + scope);
                                                }

                                                Log.d(LOG_TAG, "New uri for SET creation: " + uri);
                                            }
                                            notifyDataSetChanged();
                                        }
                                    });

                                    break;
                                }
                                default:
                                    throw new UnsupportedOperationException("Unknown catType: " + catType);

                            } // end of catType switch

                            // check in the cursor category table if the camera should be available for the attributes
                            if ( attCameraEnabled ) {

                                ImageView attCamera = (ImageView) attView.findViewById(R.id.imageview_attribute_camera_action);
                                attCamera.setVisibility(View.VISIBLE);

                                // click listener - launch camera intent
                                attCamera.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {

                                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                                        // See if we have permission to write to disk and then create dir structure if needed
                                        // Check if we have permission to write to device (required for Android 6 and greater)
                                        if (ContextCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                            // Permission is not granted
                                            Log.d(LOG_TAG, "Permission has not yet been granted to take a picture!");

                                            // Request permission
                                            ActivityCompat.requestPermissions((Activity) v.getContext(), new String[]{Manifest.permission.CAMERA}, 2);

                                        } else {

                                            // Permission has already been granted
                                            if (takePictureIntent.resolveActivity(v.getContext().getPackageManager()) != null) {
                                                File photoFile = new File(Utility.pictures.TEMPORARY_SAVE_FILE_PATH);
                                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(v.getContext(), BuildConfig.APPLICATION_ID + ".provider", photoFile));
                                                ((Activity) v.getContext()).startActivityForResult(takePictureIntent, Utility.resultCodes.REQUEST_IMAGE_CAPTURE);
                                            }

                                            SharedPreferences settings = ((Activity) v.getContext()).getPreferences(Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = settings.edit();
                                            editor.putString(Utility.pictures.CATEGORY_NAME, catFullName);
                                            editor.putString(Utility.pictures.ATTRIBUTE_NAME, attName);

                                            // Commit the edits!
                                            editor.commit();
                                        }
                                    }
                                });
                            }

                            // Add to the linearlayout row
                            attributesLayout.addView(attView);

                            // increment cursor and counter
                            attCursor.moveToNext();
                            col += 1;
                        }

                        // add the row of attributes to the vertical linear layout
                        surveyViewHolder.attLayout.addView(attributesLayout);
                    }
                    break;

                }
                attCursor.close();

                break;

            default:
                throw new UnsupportedOperationException("Unknown category type: " + catType);
        }

        scopeCursor.close();

        //scopeCursor.close();

        // Show CAMERA ICON for pictures
        if (cursor.getInt(COL_PICTURE) != 0) {
            surveyViewHolder.cameraIcon.setVisibility(View.VISIBLE);
            surveyViewHolder.cameraIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    // See if we have permission to write to disk and then create dir structure if needed
                    // Check if we have permission to write to device (required for Android 6 and greater)
                    if (ContextCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // Permission is not granted
                        Log.d(LOG_TAG, "Permission has not yet been granted to take a picture!");

                        // Request permission
                        ActivityCompat.requestPermissions((Activity) v.getContext(), new String[]{Manifest.permission.CAMERA}, 2);

                    } else {

                        // Permission has already been granted
                        if (takePictureIntent.resolveActivity(v.getContext().getPackageManager()) != null) {
                            File photoFile = new File(Utility.pictures.TEMPORARY_SAVE_FILE_PATH);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(v.getContext(), BuildConfig.APPLICATION_ID + ".provider", photoFile));
                            ((Activity) v.getContext()).startActivityForResult(takePictureIntent, Utility.resultCodes.REQUEST_IMAGE_CAPTURE);
                        }

                        SharedPreferences settings = ((Activity) v.getContext()).getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(Utility.pictures.CATEGORY_NAME, catFullName);

                        // Commit the edits!
                        editor.commit();
                    }
                }
            });
        }
    }

    public void setFragmentType(String fragmentType) {
        mFragmentType = fragmentType;
    }

    private Uri getScopeUri(String scope) {
        switch (scope) {
            case CsDbContract.PATH_CEMETERY:
                return CsDbContract.CemeteryEntry.CONTENT_URI;
            case CsDbContract.PATH_SECTION:
                return CsDbContract.SectionEntry.CONTENT_URI;
            case CsDbContract.PATH_GRAVE:
                return CsDbContract.GraveEntry.CONTENT_URI;
            default:
                throw new UnsupportedOperationException("Unknown scope type: " + scope);
        }
    }

    private String prefixColumnName(String colName, String dataType) {
        return DATA_COL_PREFIX + colName + "_" + dataType;
    }
}
