package net.frakturmedia.cemeterysurvey.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by cyrille on 25/01/16.
 */
public class CsProvider extends ContentProvider {

    public static final String LOG_TAG = CsProvider.class.getSimpleName();

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private CsDbHelper mOpenHelper;

    // List here the int codes for the different request types
    static final int NEW_COLUMN = 66;

    static final int CEMETERY = 100;
    static final int CEMETERY_ID = 101;
    static final int CEMETERY_EXPORT = 109;
    static final int CEMETERY_DELETE = 110;

    static final int CEMETERY_ATTRIBUTE = 150;
    static final int CEMETERY_ATTRIBUTE_FOR_CEMETERYID_CATEGORY = 151;
    static final int CEMETERY_ATTRIBUTE_FOR_CEMETERYID = 152;
    static final int CEMETERY_ATTRIBUTE_FOR_CEMETERYID_CATEGORY_ATTRIBUTE = 153;
    static final int CEMETERY_ATTRIBUTE_EXPORT = 159;

    static final int SECTION = 200;
    static final int SECTION_ID = 201;
    static final int SECTIONS_FROM_CEMETERY_ID = 202;
    static final int SECTION_ID_NAMES = 203;
    static final int SECTION_EXPORT = 209;
    static final int SECTION_DELETE = 210;

    static final int SECTION_ATTRIBUTE = 250;
    static final int SECTION_ATTRIBUTE_FOR_SECTIONID_CATEGORY = 251;
    static final int SECTION_ATTRIBUTE_FOR_SECTIONID = 252;
    static final int SECTION_ATTRIBUTE_FOR_SECTIONID_CATEGORY_ATTRIBUTE = 253;
    static final int SECTION_ATTRIBUTE_EXPORT = 259;

    static final int GRAVE = 300;
    static final int GRAVE_ID = 301;
    static final int GRAVES_FROM_SECTION_ID = 302;
    static final int GRAVE_ID_NAMES = 303;
    static final int GRAVE_EXPORT = 309;
    static final int GRAVE_DELETE = 310;

    static final int GRAVE_ATTRIBUTE = 350;
    static final int GRAVE_ATTRIBUTE_FOR_GRAVEID_CATEGORY = 351;
    static final int GRAVE_ATTRIBUTE_FOR_GRAVEID = 352;
    static final int GRAVE_CLEAR_FOR_GRAVEID = 353;
    static final int GRAVE_ATTRIBUTE_FOR_GRAVEID_CATEGORY_ATTRIBUTE = 354;
    static final int GRAVE_ATTRIBUTE_EXPORT = 359;

    static final int BOOKMARK = 400;
    static final int BOOKMARK_WITH_NAMES = 401;
    static final int BOOKMARK_ID_SCOPE = 402;

    static final int PICTURE = 500;
    static final int PICTURE_WITH_NAMES = 501;
    static final int PICTURE_ID_SCOPE = 502;
    static final int PICTURE_EXPORT = 509;

    static final int SURVEY = 600;
    static final int SURVEY_FOR_SCOPE_ID_TAB_NUM = 601;
    static final int SURVEY_FOR_SCOPE = 602;
    static final int SURVEY_FOR_SCOPE_TAB = 603;
    static final int SURVEY_FOR_SCOPE_JOIN_ATTRIBUTES = 604;
    static final int SURVEY_ATTRIBUTES = 650;
    static final int SURVEY_ATTRIBUTES_FOR_ID = 651;
    static final int SURVEY_ATTRIBUTES_FOR_ID_SCOPE_JOINED = 652;

    private static final SQLiteQueryBuilder sBookmarksContainingNames;

    static {
        sBookmarksContainingNames = new SQLiteQueryBuilder();

        // Join the bookmark cemetery, section and grave ids to their table's name for the id
        sBookmarksContainingNames.setTables(
                CsDbContract.BookmarkEntry.TABLE_NAME + " AS B " +
                        " LEFT JOIN " +
                        CsDbContract.CemeteryEntry.TABLE_NAME + " AS C " +
                        " ON B." + CsDbContract.BookmarkEntry.COLUMN_CEMETERY_ID +
                        " = C." + CsDbContract.CemeteryEntry._ID +
                        " LEFT JOIN " +
                        CsDbContract.SectionEntry.TABLE_NAME + " AS S " +
                        " ON B." + CsDbContract.BookmarkEntry.COLUMN_SECTION_ID +
                        " = S." + CsDbContract.SectionEntry._ID +
                        " LEFT JOIN " +
                        CsDbContract.GraveEntry.TABLE_NAME + " AS G " +
                        " ON B." + CsDbContract.BookmarkEntry.COLUMN_GRAVE_ID +
                        " = G." + CsDbContract.GraveEntry._ID);
    }

    private static final SQLiteQueryBuilder sPicturesContainingNames;

    static {
        sPicturesContainingNames = new SQLiteQueryBuilder();

        // Join the bookmark cemetery, section and grave ids to their table's name for the id
        sPicturesContainingNames.setTables(
                CsDbContract.PictureEntry.TABLE_NAME + " AS P " +
                        " LEFT JOIN " +
                        CsDbContract.CemeteryEntry.TABLE_NAME + " AS C " +
                        " ON P." + CsDbContract.PictureEntry.COLUMN_CEMETERY_ID +
                        " = C." + CsDbContract.CemeteryEntry._ID +
                        " LEFT JOIN " +
                        CsDbContract.SectionEntry.TABLE_NAME + " AS S " +
                        " ON P." + CsDbContract.PictureEntry.COLUMN_SECTION_ID +
                        " = S." + CsDbContract.SectionEntry._ID +
                        " LEFT JOIN " +
                        CsDbContract.GraveEntry.TABLE_NAME + " AS G " +
                        " ON P." + CsDbContract.PictureEntry.COLUMN_GRAVE_ID +
                        " = G." + CsDbContract.GraveEntry._ID
        );
    }

    private static final SQLiteQueryBuilder sCemeterySectionNamesIds;

    static {
        sCemeterySectionNamesIds = new SQLiteQueryBuilder();

        // Join cemetery and section tables to get their ids and names
        sCemeterySectionNamesIds.setTables(
                CsDbContract.SectionEntry.TABLE_NAME + " AS S " +
                        " INNER JOIN " +
                        CsDbContract.CemeteryEntry.TABLE_NAME + " AS C " +
                        " ON S." + CsDbContract.SectionEntry.COLUMN_CEMETERY_ID +
                        " = C." + CsDbContract.CemeteryEntry._ID);
    }

    private static final SQLiteQueryBuilder sCemeterySectionGraveNamesIds;

    static {
        sCemeterySectionGraveNamesIds = new SQLiteQueryBuilder();

        // Join cemetery and section tables to get their ids and names
        sCemeterySectionGraveNamesIds.setTables(
                CsDbContract.GraveEntry.TABLE_NAME + " AS G " +
                        " INNER JOIN " +
                        CsDbContract.CemeteryEntry.TABLE_NAME + " AS C " +
                        " ON G." + CsDbContract.GraveEntry.COLUMN_CEMETERY_ID +
                        " = C." + CsDbContract.CemeteryEntry._ID +
                        " INNER JOIN " +
                        CsDbContract.SectionEntry.TABLE_NAME + " AS S " +
                        " ON G." + CsDbContract.GraveEntry.COLUMN_SECTION_ID +
                        " = S." + CsDbContract.SectionEntry._ID);
    }

//    private static final SQLiteQueryBuilder sSurveyCategoryAttributes;
//
//    static{
//        sSurveyCategoryAttributes = new SQLiteQueryBuilder();
//
//        // Join cemetery and section tables to get their ids and names
//        sSurveyCategoryAttributes.setTables(
//                CsDbContract.SurveyCategoryEntry.TABLE_NAME + " AS C " +
//                        " LEFT JOIN " +
//                        CsDbContract.SurveyAttributeEntry.TABLE_NAME + " AS A " +
//                        " ON C." + CsDbContract.SurveyCategoryEntry._ID +
//                        " = A." + CsDbContract.SurveyAttributeEntry.COLUMN_CATEGORY_ID +
//                        " UNION ALL " +
//                        CsDbContract.SurveyAttributeEntry.TABLE_NAME + " AS A " +
//                        " LEFT JOIN " +
//                        CsDbContract.SurveyCategoryEntry.TABLE_NAME + " AS C " +
//                        " ON C." + CsDbContract.SurveyCategoryEntry._ID +
//                        " = A." + CsDbContract.SurveyAttributeEntry.COLUMN_CATEGORY_ID);
//    }

    private static final SQLiteQueryBuilder sSurveyForCemeteryId;
    private static final SQLiteQueryBuilder sSurveyForSectionId;
    private static final SQLiteQueryBuilder sSurveyForGraveId;

    static {
        sSurveyForCemeteryId = new SQLiteQueryBuilder();

        // CROSS JOIN cemetery survey questions with duplicates of cemetery id row
        sSurveyForCemeteryId.setTables(
                CsDbContract.SurveyCategoryEntry.TABLE_NAME + " AS S CROSS JOIN " +
                        CsDbContract.CemeteryEntry.TABLE_NAME + " AS D "
        );
    }

    static {
        sSurveyForSectionId = new SQLiteQueryBuilder();

        // CROSS JOIN cemetery survey questions with duplicates of cemetery id row
        sSurveyForSectionId.setTables(
                CsDbContract.SurveyCategoryEntry.TABLE_NAME + " AS S CROSS JOIN " +
                        CsDbContract.SectionEntry.TABLE_NAME + " AS D "
        );
    }

    static {
        sSurveyForGraveId = new SQLiteQueryBuilder();

        // CROSS JOIN cemetery survey questions with duplicates of cemetery id row
        sSurveyForGraveId.setTables(
                CsDbContract.SurveyCategoryEntry.TABLE_NAME + " AS S CROSS JOIN " +
                        CsDbContract.GraveEntry.TABLE_NAME + " AS D "
        );
    }

    private static final String[] SurveyCategoryProjection = new String[]{
            CsDbContract.SurveyCategoryEntry._ID,
            CsDbContract.SurveyCategoryEntry.COLUMN_TYPE,
            CsDbContract.SurveyCategoryEntry.COLUMN_SCOPE,
            CsDbContract.SurveyCategoryEntry.COLUMN_TAB_NUMBER,
            CsDbContract.SurveyCategoryEntry.COLUMN_GROUP_NUMBER,
            CsDbContract.SurveyCategoryEntry.COLUMN_CATEGORY_NUMBER,
            CsDbContract.SurveyCategoryEntry.COLUMN_TAB_NAME,
            CsDbContract.SurveyCategoryEntry.COLUMN_GROUP_NAME,
            CsDbContract.SurveyCategoryEntry.COLUMN_NAME,
            CsDbContract.SurveyCategoryEntry.COLUMN_TITLE,
            CsDbContract.SurveyCategoryEntry.COLUMN_PICTURE,
            CsDbContract.SurveyCategoryEntry.COLUMN_ATTRIBUTE_PICTURE,
            CsDbContract.SurveyCategoryEntry.COLUMN_REQUIRED,
            CsDbContract.SurveyCategoryEntry.COLUMN_THUMBNAILS_PATH
    };

    private static final String[] SurveyCategoryProjectionCrossJoin = new String[]{
            "S." + CsDbContract.SurveyCategoryEntry._ID,
            CsDbContract.SurveyCategoryEntry.COLUMN_TYPE,
            CsDbContract.SurveyCategoryEntry.COLUMN_SCOPE,
            CsDbContract.SurveyCategoryEntry.COLUMN_TAB_NUMBER,
            CsDbContract.SurveyCategoryEntry.COLUMN_GROUP_NUMBER,
            CsDbContract.SurveyCategoryEntry.COLUMN_CATEGORY_NUMBER,
            CsDbContract.SurveyCategoryEntry.COLUMN_TAB_NAME,
            CsDbContract.SurveyCategoryEntry.COLUMN_GROUP_NAME,
            CsDbContract.SurveyCategoryEntry.COLUMN_NAME,
            CsDbContract.SurveyCategoryEntry.COLUMN_TITLE,
            CsDbContract.SurveyCategoryEntry.COLUMN_PICTURE,
            CsDbContract.SurveyCategoryEntry.COLUMN_ATTRIBUTE_PICTURE,
            CsDbContract.SurveyCategoryEntry.COLUMN_REQUIRED,
            CsDbContract.SurveyCategoryEntry.COLUMN_THUMBNAILS_PATH,
            "D._id AS scopeId"
    };

    private static final String[] SurveyAttributesProjection = new String[]{
            CsDbContract.SurveyAttributeEntry._ID,
            CsDbContract.SurveyAttributeEntry.COLUMN_ORDER,
            CsDbContract.SurveyAttributeEntry.COLUMN_NAME
    };

    /**
     * Define the selection syntax for different queries
     * The '?' represent where the argument will be placed
     */

    // Get the cemetery for a cemetery id
    private static final String sSelectCemeteryById =
            CsDbContract.CemeteryEntry._ID + " = ? ";

    // Get the sections for a cemetery id
    private static final String sSelectSectionsByCemeteryId =
            CsDbContract.SectionEntry.COLUMN_CEMETERY_ID + " = ? ";
    // Get the section with a section id
    private static final String sSelectSectionById =
            CsDbContract.SectionEntry._ID + " = ? ";

    // Get the graves for a section id
    private static final String sSelectGravesBySectionId =
            CsDbContract.GraveEntry.COLUMN_SECTION_ID + " = ? ";
    // Get the grave with a grave id
    private static final String sSelectGraveById =
            CsDbContract.GraveEntry._ID + " = ? ";

    // Get the survey for a scope
    private static final String sSelectSurveyQuestionsByScope =
            CsDbContract.SurveyCategoryEntry.COLUMN_SCOPE + " = ? ";
    // Get the survey for a scope tab
    private static final String sSelectSurveyQuestionsByScopeTab =
            CsDbContract.SurveyCategoryEntry.COLUMN_SCOPE + " = ? " +
                    CsDbContract.SurveyCategoryEntry.COLUMN_TAB_NUMBER + " = ? ";
    // Get the attributes for a specific survey question
    private static final String sSelectSurveyQuestionAttributesById =
            CsDbContract.SurveyAttributeEntry.COLUMN_CATEGORY_ID + " = ? ";

    // C,S,G Attribute table selections
    private static final String sSelectCemeteryAttributesByCemeteryId =
            CsDbContract.CemeteryAttributesEntry.COLUMN_CEMETERY_ID + " = ? ";
    private static final String sSelectSectionAttributesBySectionId =
            CsDbContract.SectionAttributesEntry.COLUMN_SECTION_ID + " = ? ";
    private static final String sSelectGraveAttributesByGraveId =
            CsDbContract.GraveAttributesEntry.COLUMN_GRAVE_ID + " = ? ";

    private static final String sSelectCemeteryAttributesCategoryByIdAndName =
            CsDbContract.CemeteryAttributesEntry.COLUMN_CEMETERY_ID + " = ? " +
                    " AND " + CsDbContract.CemeteryAttributesEntry.COLUMN_CATEGORY_NAME + " = ? " +
                    " AND " + CsDbContract.CemeteryAttributesEntry.COLUMN_ATTRIBUTE_NAME + " = ? ";
    private static final String sSelectSectionAttributesCategoryByIdAndName =
            CsDbContract.SectionAttributesEntry.COLUMN_SECTION_ID + " = ? " +
                    " AND " + CsDbContract.SectionAttributesEntry.COLUMN_CATEGORY_NAME + " = ? " +
                    " AND " + CsDbContract.SectionAttributesEntry.COLUMN_ATTRIBUTE_NAME + " = ? ";
    private static final String sSelectGraveAttributesCategoryByIdAndName =
            CsDbContract.GraveAttributesEntry.COLUMN_GRAVE_ID + " = ? " +
                    " AND " + CsDbContract.GraveAttributesEntry.COLUMN_CATEGORY_NAME + " = ? " +
                    " AND " + CsDbContract.GraveAttributesEntry.COLUMN_ATTRIBUTE_NAME + " = ? ";

    // Get the Cemetery, Section and Grave Names for bookmarks
    private Cursor getBookmarkScopeNames(Uri uri, String[] projection, String sortOrder) {
        if (projection != null || sortOrder != null) {
            throw new UnsupportedOperationException("Content provider is ignoring the input fields projection: " + projection.toString() + "\n and sortOrder:" + sortOrder);
        }

        return sBookmarksContainingNames.query(mOpenHelper.getReadableDatabase(),
                new String[]{
                        "B." + CsDbContract.BookmarkEntry._ID,
                        "B." + CsDbContract.BookmarkEntry.COLUMN_CEMETERY_ID,
                        "B." + CsDbContract.BookmarkEntry.COLUMN_SECTION_ID,
                        "B." + CsDbContract.BookmarkEntry.COLUMN_GRAVE_ID,
                        "C." + CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME,
                        "S." + CsDbContract.SectionEntry.COLUMN_SECTION_NAME,
                        "G." + CsDbContract.GraveEntry.COLUMN_GRAVE_NAME,
                        "B." + CsDbContract.BookmarkEntry.COLUMN_SCOPE
                },
                null,
                null,
                null,
                null,
                "C." + CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME + " ASC");

    }

    // Get the Cemetery, Section, Grave, Category and Attribute names for pictures
    private Cursor getPictureScopeNames(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (projection != null || sortOrder != null) {
            throw new UnsupportedOperationException("Content provider is ignoring the input fields projection: " + projection.toString() + "\n and sortOrder:" + sortOrder);
        }

        return sPicturesContainingNames.query(mOpenHelper.getReadableDatabase(),
                new String[]{
                        "P." + CsDbContract.PictureEntry._ID,
                        "P." + CsDbContract.PictureEntry.COLUMN_CEMETERY_ID,
                        "P." + CsDbContract.PictureEntry.COLUMN_SECTION_ID,
                        "P." + CsDbContract.PictureEntry.COLUMN_GRAVE_ID,
                        "C." + CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME,
                        "S." + CsDbContract.SectionEntry.COLUMN_SECTION_NAME,
                        "G." + CsDbContract.GraveEntry.COLUMN_GRAVE_NAME,
                        "P." + CsDbContract.PictureEntry.COLUMN_CATEGORY_NAME,
                        "P." + CsDbContract.PictureEntry.COLUMN_ATTRIBUTE_NAME,
                        "P." + CsDbContract.PictureEntry.COLUMN_FILE_NAME,
                        "P." + CsDbContract.PictureEntry.COLUMN_SCOPE
                },
                selection, // selection
                selectionArgs, // selectionArgs
                null, // group by
                null, // having
                "C." + CsDbContract.PictureEntry._ID + " ASC"); // sort order
    }

    // Get the Cemetery and Section details joined
    private Cursor getJoinedCemeterySection(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (projection != null) {
            throw new UnsupportedOperationException("Content provider is ignoring the projection field: " + projection.toString());
        }

        return sCemeterySectionNamesIds.query(mOpenHelper.getReadableDatabase(),
                new String[]{
                        "C." + CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME,
                        "S." + CsDbContract.SectionEntry.COLUMN_SECTION_NAME,
                        "S." + CsDbContract.SectionEntry.COLUMN_CEMETERY_ID,
                        "S." + CsDbContract.SectionEntry._ID
                },
                selection, // selection
                selectionArgs, // selectionArgs
                null, // group by
                null, // having
                "S." + CsDbContract.SectionEntry._ID + " ASC"); // sort order
    }

    // Get the Cemetery and Section details joined
    private Cursor getJoinedCemeterySectionGrave(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (projection != null) {
            throw new UnsupportedOperationException("Content provider is ignoring the projection field: " + projection.toString());
        }

        return sCemeterySectionGraveNamesIds.query(mOpenHelper.getReadableDatabase(),
                new String[]{
                        "C." + CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME,
                        "S." + CsDbContract.SectionEntry.COLUMN_SECTION_NAME,
                        "G." + CsDbContract.GraveEntry.COLUMN_GRAVE_NAME,
                        "G." + CsDbContract.GraveEntry.COLUMN_CEMETERY_ID,
                        "G." + CsDbContract.GraveEntry.COLUMN_SECTION_ID,
                        "G." + CsDbContract.GraveEntry._ID
                },
                selection, // selection
                selectionArgs, // selectionArgs
                null, // group by
                null, // having
                "S." + CsDbContract.SectionEntry._ID + " ASC"); // sort order
    }

    //  Get the Survey category and attributes joined
//    private Cursor getJoinedSurveyCategoryAttributes(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
//        if( projection != null && sortOrder != null ) {
//            throw new UnsupportedOperationException("Content provider is ignoring the projection, sortOrder, selection fields for the Uri: " + uri);
//        }
//
//        String surveyScope = CsDbContract.SurveyCategoryEntry.getSurveyScopeFromUri(uri);
//
//        return sSurveyCategoryAttributes.query(mOpenHelper.getReadableDatabase(),
//                new String[]{
//                        "C." + CsDbContract.SurveyCategoryEntry._ID,
//                        "C." + CsDbContract.SurveyCategoryEntry.COLUMN_TYPE,
//                        "C." + CsDbContract.SurveyCategoryEntry.COLUMN_SCOPE,
//                        "C." + CsDbContract.SurveyCategoryEntry.COLUMN_TAB_NUMBER,
//                        "C." + CsDbContract.SurveyCategoryEntry.COLUMN_GROUP_NUMBER,
//                        "C." + CsDbContract.SurveyCategoryEntry.COLUMN_CATEGORY_NUMBER,
//                        "C." + CsDbContract.SurveyCategoryEntry.COLUMN_TAB_NAME,
//                        "C." + CsDbContract.SurveyCategoryEntry.COLUMN_GROUP_NAME,
//                        "C." + CsDbContract.SurveyCategoryEntry.COLUMN_NAME,
//                        "C." + CsDbContract.SurveyCategoryEntry.COLUMN_TITLE,
//                        "C." + CsDbContract.SurveyCategoryEntry.COLUMN_PICTURE,
//                        "C." + CsDbContract.SurveyCategoryEntry.COLUMN_ATTRIBUTE_PICTURE,
//                        "C." + CsDbContract.SurveyCategoryEntry.COLUMN_REQUIRED,
//                        "A." + CsDbContract.SurveyAttributeEntry._ID,
//                        "A." + CsDbContract.SurveyAttributeEntry.COLUMN_ORDER,
//                        "A." + CsDbContract.SurveyAttributeEntry.COLUMN_NAME
//                },
//                sSelectSurveyQuestionsByScope, // selection
//                new String[]{surveyScope}, // selectionArgs
//                null, // group by
//                null, // having
//                // sortOrder
//                "C." + CsDbContract.SurveyCategoryEntry.COLUMN_TAB_NUMBER + ", " +
//                        "C." + CsDbContract.SurveyCategoryEntry.COLUMN_GROUP_NUMBER + ", " +
//                        "C." + CsDbContract.SurveyCategoryEntry.COLUMN_CATEGORY_NUMBER + ", " +
//                        "A." + CsDbContract.SurveyAttributeEntry.COLUMN_ORDER + " ASC"
//        );
//    }

    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CsDbContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, CsDbContract.PATH_MAIN + "/new_column/*/*/*", NEW_COLUMN);

        matcher.addURI(authority, CsDbContract.PATH_CEMETERY, CEMETERY);
        matcher.addURI(authority, CsDbContract.PATH_CEMETERY + "/#", CEMETERY_ID);
        matcher.addURI(authority, CsDbContract.PATH_CEMETERY + "/export", CEMETERY_EXPORT);
        matcher.addURI(authority, CsDbContract.PATH_CEMETERY + "/delete/#", CEMETERY_DELETE);

        matcher.addURI(authority, CsDbContract.PATH_CEMETERY_ATTRIBUTES, CEMETERY_ATTRIBUTE);
        matcher.addURI(authority, CsDbContract.PATH_CEMETERY_ATTRIBUTES + "/#", CEMETERY_ATTRIBUTE_FOR_CEMETERYID);
        matcher.addURI(authority, CsDbContract.PATH_CEMETERY_ATTRIBUTES + "/#/*/*", CEMETERY_ATTRIBUTE_FOR_CEMETERYID_CATEGORY_ATTRIBUTE);
        matcher.addURI(authority, CsDbContract.PATH_CEMETERY_ATTRIBUTES + "/export", CEMETERY_ATTRIBUTE_EXPORT);

        matcher.addURI(authority, CsDbContract.PATH_SECTION, SECTION);
        matcher.addURI(authority, CsDbContract.PATH_SECTION + "/#", SECTION_ID);
        matcher.addURI(authority, CsDbContract.PATH_SECTION + "/" + CsDbContract.PATH_CEMETERY + "/#", SECTIONS_FROM_CEMETERY_ID);
        matcher.addURI(authority, CsDbContract.PATH_SECTION + "/joined", SECTION_ID_NAMES);
        matcher.addURI(authority, CsDbContract.PATH_SECTION + "/export", SECTION_EXPORT);
        matcher.addURI(authority, CsDbContract.PATH_SECTION + "/delete/#/#", SECTION_DELETE);

        matcher.addURI(authority, CsDbContract.PATH_SECTION_ATTRIBUTES, SECTION_ATTRIBUTE);
        matcher.addURI(authority, CsDbContract.PATH_SECTION_ATTRIBUTES + "/#", SECTION_ATTRIBUTE_FOR_SECTIONID);
        matcher.addURI(authority, CsDbContract.PATH_SECTION_ATTRIBUTES + "/#/*/*", SECTION_ATTRIBUTE_FOR_SECTIONID_CATEGORY_ATTRIBUTE);
        matcher.addURI(authority, CsDbContract.PATH_SECTION_ATTRIBUTES + "/export", SECTION_ATTRIBUTE_EXPORT);

        matcher.addURI(authority, CsDbContract.PATH_GRAVE, GRAVE);
        matcher.addURI(authority, CsDbContract.PATH_GRAVE + "/#", GRAVE_ID);
        matcher.addURI(authority, CsDbContract.PATH_GRAVE + "/" + CsDbContract.PATH_SECTION + "/#", GRAVES_FROM_SECTION_ID);
        matcher.addURI(authority, CsDbContract.PATH_GRAVE + "/joined", GRAVE_ID_NAMES);
        matcher.addURI(authority, CsDbContract.PATH_GRAVE + "/#/clear", GRAVE_CLEAR_FOR_GRAVEID);
        matcher.addURI(authority, CsDbContract.PATH_GRAVE + "/export", GRAVE_EXPORT);
        matcher.addURI(authority, CsDbContract.PATH_GRAVE + "/delete/#/#/#", GRAVE_DELETE);

        matcher.addURI(authority, CsDbContract.PATH_GRAVE_ATTRIBUTES, GRAVE_ATTRIBUTE);
        matcher.addURI(authority, CsDbContract.PATH_GRAVE_ATTRIBUTES + "/#", GRAVE_ATTRIBUTE_FOR_GRAVEID);
        matcher.addURI(authority, CsDbContract.PATH_GRAVE_ATTRIBUTES + "/#/*/*", GRAVE_ATTRIBUTE_FOR_GRAVEID_CATEGORY_ATTRIBUTE);
        matcher.addURI(authority, CsDbContract.PATH_GRAVE_ATTRIBUTES + "/export", GRAVE_ATTRIBUTE_EXPORT);

        matcher.addURI(authority, CsDbContract.PATH_BOOKMARK, BOOKMARK);
        matcher.addURI(authority, CsDbContract.PATH_BOOKMARK + "/joined", BOOKMARK_WITH_NAMES);
        matcher.addURI(authority, CsDbContract.PATH_BOOKMARK + "/#/*", BOOKMARK_ID_SCOPE);

        matcher.addURI(authority, CsDbContract.PATH_PICTURE, PICTURE);
        matcher.addURI(authority, CsDbContract.PATH_PICTURE + "/joined", PICTURE_WITH_NAMES);
        matcher.addURI(authority, CsDbContract.PATH_PICTURE + "/#/*", PICTURE_ID_SCOPE);
        matcher.addURI(authority, CsDbContract.PATH_PICTURE + "/export", PICTURE_EXPORT);

        matcher.addURI(authority, CsDbContract.PATH_SURVEY_CATEGORY, SURVEY);
//        matcher.addURI(authority, CsDbContract.PATH_SURVEY_CATEGORY + "/*/joined", SURVEY_FOR_SCOPE_JOIN_ATTRIBUTES);
        matcher.addURI(authority, CsDbContract.PATH_SURVEY_CATEGORY + "/*/#/#", SURVEY_FOR_SCOPE_ID_TAB_NUM);
        matcher.addURI(authority, CsDbContract.PATH_SURVEY_CATEGORY + "/*", SURVEY_FOR_SCOPE);
        matcher.addURI(authority, CsDbContract.PATH_SURVEY_CATEGORY + "/*/tab/#", SURVEY_FOR_SCOPE_TAB);
        matcher.addURI(authority, CsDbContract.PATH_SURVEY_ATTRIBUTE, SURVEY_ATTRIBUTES);
        matcher.addURI(authority, CsDbContract.PATH_SURVEY_ATTRIBUTE + "/#", SURVEY_ATTRIBUTES_FOR_ID);
        matcher.addURI(authority, CsDbContract.PATH_SURVEY_ATTRIBUTE + "/#/#/*/*", SURVEY_ATTRIBUTES_FOR_ID_SCOPE_JOINED);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new CsDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;

        Log.d(LOG_TAG, "Content provider query: " + uri.toString());

        long cemeteryId;
        long sectionId;
        long graveId;

        switch (sUriMatcher.match(uri)) {
            // CEMETERIES
            case CEMETERY:
                if (sortOrder == null) {
                    sortOrder = "UPPER(" + CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME + ") ASC ";
                }

                retCursor = mOpenHelper.getReadableDatabase().query(
                        CsDbContract.CemeteryEntry.TABLE_NAME,
                        projection, // columns
                        selection, selectionArgs, // selection, selectionArgs
                        null, null, // group by, having
                        sortOrder
                );
                break;

            case CEMETERY_ID:
                cemeteryId = CsDbContract.CemeteryEntry.getCemeteryIdFromUri(uri);

                retCursor = mOpenHelper.getReadableDatabase().query(
                        CsDbContract.CemeteryEntry.TABLE_NAME,
                        projection, // columns
                        sSelectCemeteryById, // selection
                        new String[]{Long.toString(cemeteryId)}, // selectionArgs
                        null, null, // group by, having
                        sortOrder
                );
                break;

            // CEMETERY ATTRIBUTES
            case CEMETERY_ATTRIBUTE:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CsDbContract.CemeteryAttributesEntry.TABLE_NAME,
                        projection, // columns
                        selection, // selection
                        selectionArgs, // selectionArgs
                        null, null, // group by, having
                        sortOrder
                );
                break;
            case CEMETERY_ATTRIBUTE_FOR_CEMETERYID_CATEGORY:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CsDbContract.CemeteryAttributesEntry.TABLE_NAME,
                        projection, // columns
                        sSelectCemeteryAttributesCategoryByIdAndName, // selection
                        CsDbContract.CemeteryAttributesEntry.getCemeteryIdCategoryFromUri(uri), // selectionArgs
                        null, null, // group by, having
                        sortOrder
                );
                break;

            // SECTIONS
            case SECTION:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CsDbContract.SectionEntry.TABLE_NAME,
                        projection, // columns
                        selection, // selection
                        selectionArgs, // selectionArgs
                        null, null, // group by, having
                        sortOrder
                );
                break;
            case SECTIONS_FROM_CEMETERY_ID:
                retCursor = getCemeterySectionsListUsingId(uri, projection, sortOrder);
                break;
            case SECTION_ID:
                sectionId = CsDbContract.SectionEntry.getSectionIdFromUri(uri);

                retCursor = mOpenHelper.getReadableDatabase().query(
                        CsDbContract.SectionEntry.TABLE_NAME,
                        projection, // columns
                        sSelectSectionById, // selection
                        new String[]{Long.toString(sectionId)}, // selectionArgs
                        null, null, // group by, having
                        sortOrder
                );
                break;
            case SECTION_ID_NAMES:
                retCursor = getJoinedCemeterySection(uri, projection, selection, selectionArgs, sortOrder);
                break;

            // SECTION ATTRIBUTES
            case SECTION_ATTRIBUTE:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CsDbContract.SectionAttributesEntry.TABLE_NAME,
                        projection, // columns
                        selection, // selection
                        selectionArgs, // selectionArgs
                        null, null, // group by, having
                        sortOrder
                );
                break;
            case SECTION_ATTRIBUTE_FOR_SECTIONID_CATEGORY:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CsDbContract.SectionAttributesEntry.TABLE_NAME,
                        projection, // columns
                        sSelectSectionAttributesCategoryByIdAndName, // selection
                        CsDbContract.SectionAttributesEntry.getSectionIdCategoryFromUri(uri), // selectionArgs
                        null, null, // group by, having
                        sortOrder
                );
                break;

            // GRAVES
            case GRAVE:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CsDbContract.GraveEntry.TABLE_NAME,
                        projection, // columns
                        selection, // selection
                        selectionArgs, // selectionArgs
                        null, null, // group by, having
                        sortOrder
                );
                break;
            case GRAVES_FROM_SECTION_ID:
                retCursor = getSectionGraveListUsingId(uri, projection, sortOrder);
                break;
            case GRAVE_ID:
                graveId = CsDbContract.GraveEntry.getGraveIdFromUri(uri);

                retCursor = mOpenHelper.getReadableDatabase().query(
                        CsDbContract.GraveEntry.TABLE_NAME,
                        projection, // columns
                        sSelectGraveById, // selection
                        new String[]{Long.toString(graveId)}, // selectionArgs
                        null, null, // group by, having
                        sortOrder
                );
                break;
            case GRAVE_ID_NAMES:
                retCursor = getJoinedCemeterySectionGrave(uri, projection, selection, selectionArgs, sortOrder);
                break;

            // GRAVE ATTRIBUTES
            case GRAVE_ATTRIBUTE:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CsDbContract.GraveAttributesEntry.TABLE_NAME,
                        projection, // columns
                        selection, // selection
                        selectionArgs, // selectionArgs
                        null, null, // group by, having
                        sortOrder
                );
                break;
            case GRAVE_ATTRIBUTE_FOR_GRAVEID_CATEGORY:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CsDbContract.GraveAttributesEntry.TABLE_NAME,
                        projection, // columns
                        sSelectGraveAttributesCategoryByIdAndName, // selection
                        CsDbContract.GraveAttributesEntry.getGraveIdCategoryFromUri(uri), // selectionArgs
                        null, null, // group by, having
                        sortOrder
                );
                break;

            // BOOKMARKS
            case BOOKMARK:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CsDbContract.BookmarkEntry.TABLE_NAME,
                        projection, // columns
                        selection, selectionArgs, // selection, selectionArgs
                        null, null, // group by, having
                        sortOrder
                );
                break;
            case BOOKMARK_WITH_NAMES:
                retCursor = getBookmarkScopeNames(uri, projection, sortOrder);
                break;
            case BOOKMARK_ID_SCOPE:
                retCursor = getBookmarkWithScopeId(uri, projection, sortOrder);
                break;

            // PICTURES
            case PICTURE:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CsDbContract.PictureEntry.TABLE_NAME,
                        projection,
                        selection, selectionArgs, // selection, selectionArgs
                        null, null, // group by, having
                        sortOrder
                );
                break;
            case PICTURE_WITH_NAMES:
                retCursor = getPictureScopeNames(uri, projection, selection, selectionArgs, sortOrder);
                break;
            case PICTURE_ID_SCOPE:
                retCursor = getPictureWithScopeId(uri, projection, sortOrder);
                break;

            // SURVEY TEMPLATE
            case SURVEY:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CsDbContract.SurveyCategoryEntry.TABLE_NAME,
                        projection,
                        selection, selectionArgs, // selection selectionArgs
                        null, null, // group by, having
                        sortOrder
                );
                break;
            case SURVEY_FOR_SCOPE_ID_TAB_NUM: {
                if (sortOrder != null || projection != null || selection != null) {
                    Log.e(LOG_TAG, "The survey category table query uses pre-defined projection, selection and sort order. Ignored one or more for Uri: " + uri);
                }

                String surveyScope = uri.getPathSegments().get(1);
                String scopeId = uri.getPathSegments().get(2);
                String tabNum = uri.getPathSegments().get(3);

                String overrideSortOrder = CsDbContract.SurveyCategoryEntry.COLUMN_TAB_NUMBER + ", " +
                        CsDbContract.SurveyCategoryEntry.COLUMN_GROUP_NUMBER + ", " +
                        CsDbContract.SurveyCategoryEntry.COLUMN_CATEGORY_NUMBER + " ASC";

                switch (surveyScope) {
                    case CsDbContract.PATH_CEMETERY:
                        retCursor = sSurveyForCemeteryId.query(mOpenHelper.getReadableDatabase(),
                                SurveyCategoryProjectionCrossJoin,
                                "S." + CsDbContract.SurveyCategoryEntry.COLUMN_SCOPE + " = ? " +
                                        "AND D." + CsDbContract.CemeteryEntry._ID + " = ? " +
                                        "AND S." + CsDbContract.SurveyCategoryEntry.COLUMN_TAB_NUMBER + " = ? ",
                                new String[]{surveyScope, scopeId, tabNum},
                                null, null, overrideSortOrder);
                        break;
                    case CsDbContract.PATH_SECTION:
                        retCursor = sSurveyForSectionId.query(mOpenHelper.getReadableDatabase(),
                                SurveyCategoryProjectionCrossJoin,
                                "S." + CsDbContract.SurveyCategoryEntry.COLUMN_SCOPE + " = ? " +
                                        "AND D." + CsDbContract.SectionEntry._ID + " = ? " +
                                        "AND S." + CsDbContract.SurveyCategoryEntry.COLUMN_TAB_NUMBER + " = ? ",
                                new String[]{surveyScope, scopeId, tabNum},
                                null, null, overrideSortOrder);
                        break;
                    case CsDbContract.PATH_GRAVE:
                        retCursor = sSurveyForGraveId.query(mOpenHelper.getReadableDatabase(),
                                SurveyCategoryProjectionCrossJoin,
                                "S." + CsDbContract.SurveyCategoryEntry.COLUMN_SCOPE + " = ? " +
                                        "AND D." + CsDbContract.GraveEntry._ID + " = ? " +
                                        "AND S." + CsDbContract.SurveyCategoryEntry.COLUMN_TAB_NUMBER + " = ? ",
                                new String[]{surveyScope, scopeId, tabNum},
                                null, null, overrideSortOrder);
                        break;
                    default:
                        throw new UnsupportedOperationException("Unknown scope type: " + surveyScope);
                }
                break;
            }
            case SURVEY_FOR_SCOPE: {
                String surveyScope = CsDbContract.SurveyCategoryEntry.getSurveyScopeFromUri(uri);

                if (sortOrder != null || projection != null) {
                    Log.e(LOG_TAG, "The survey category table query uses pre-defined projection, selection and sort order. Ignored one or more for Uri: " + uri);
                }

                retCursor = mOpenHelper.getReadableDatabase().query(
                        CsDbContract.SurveyCategoryEntry.TABLE_NAME,
                        SurveyCategoryProjection,
                        sSelectSurveyQuestionsByScope,
                        new String[]{surveyScope},
                        null, null, // group by, having
                        CsDbContract.SurveyCategoryEntry.COLUMN_TAB_NUMBER + ", " +
                                CsDbContract.SurveyCategoryEntry.COLUMN_GROUP_NUMBER + ", " +
                                CsDbContract.SurveyCategoryEntry.COLUMN_CATEGORY_NUMBER + " ASC"
                );
                Log.d(LOG_TAG, "Results of survey for scope " + surveyScope + ". Rows=" + retCursor.getCount());
                break;
            }
            case SURVEY_FOR_SCOPE_TAB:
                String[] surveyScopeTab = CsDbContract.SurveyCategoryEntry.getSurveyScopeAndTabFromUri(uri);

                retCursor = mOpenHelper.getReadableDatabase().query(
                        CsDbContract.SurveyCategoryEntry.TABLE_NAME,
                        projection,
                        sSelectSurveyQuestionsByScopeTab,
                        surveyScopeTab,
                        null, null, // group by, having
                        CsDbContract.SurveyCategoryEntry.COLUMN_GROUP_NUMBER + ", " + CsDbContract.SurveyCategoryEntry.COLUMN_CATEGORY_NUMBER + " ASC"
                );
                break;

            case SURVEY_ATTRIBUTES:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CsDbContract.SurveyAttributeEntry.TABLE_NAME,
                        projection,
                        selection, selectionArgs, // selection selectionArgs
                        null, null, // group by, having
                        null // sortOrder
                );
                break;
            case SURVEY_ATTRIBUTES_FOR_ID_SCOPE_JOINED: {
                String catId = uri.getPathSegments().get(1);
                String scopeId = uri.getPathSegments().get(2);
                String scope = uri.getPathSegments().get(3);
                String catFullName = uri.getPathSegments().get(4);

                if (sortOrder != null || projection != null || selection != null) {
                    Log.e(LOG_TAG, "The survey attribute table query uses pre-defined projection, selection and sort order. Ignored one or more for Uri: " + uri);
                }

                // These SQL queries are BEASTS! but haven't found another way yet
                switch (scope) {
                    case CsDbContract.PATH_CEMETERY:
                        retCursor = mOpenHelper.getReadableDatabase().rawQuery(
                                "SELECT S." + CsDbContract.SurveyAttributeEntry._ID +
                                        ", S." + CsDbContract.SurveyAttributeEntry.COLUMN_ORDER +
                                        ", S." + CsDbContract.SurveyAttributeEntry.COLUMN_NAME +
                                        ", D." + CsDbContract.CemeteryAttributesEntry.COLUMN_ATTRIBUTE_NAME +
                                        " FROM " + CsDbContract.SurveyAttributeEntry.TABLE_NAME + " AS S " +
                                        " LEFT JOIN " +
                                        " (SELECT " + CsDbContract.CemeteryAttributesEntry.COLUMN_ATTRIBUTE_NAME + " FROM " +
                                        CsDbContract.CemeteryAttributesEntry.TABLE_NAME + " WHERE " +
                                        CsDbContract.CemeteryAttributesEntry.COLUMN_CEMETERY_ID + " = ? " +
                                        " AND " + CsDbContract.CemeteryAttributesEntry.COLUMN_CATEGORY_NAME + " = ?) AS D " +
                                        " ON S." + CsDbContract.SurveyAttributeEntry.COLUMN_NAME + " = " +
                                        " D." + CsDbContract.CemeteryAttributesEntry.COLUMN_ATTRIBUTE_NAME +
                                        " WHERE S." + CsDbContract.SurveyAttributeEntry.COLUMN_CATEGORY_ID + " = ? " +
                                        " GROUP BY S." + CsDbContract.SurveyAttributeEntry.COLUMN_NAME +
                                        " ORDER BY S." + CsDbContract.SurveyAttributeEntry.COLUMN_ORDER + " ASC",
                                new String[]{scopeId, catFullName, catId}
                        );
                        break;
                    case CsDbContract.PATH_SECTION:
                        retCursor = mOpenHelper.getReadableDatabase().rawQuery(
                                "SELECT S." + CsDbContract.SurveyAttributeEntry._ID +
                                        ", S." + CsDbContract.SurveyAttributeEntry.COLUMN_ORDER +
                                        ", S." + CsDbContract.SurveyAttributeEntry.COLUMN_NAME +
                                        ", D." + CsDbContract.SectionAttributesEntry.COLUMN_ATTRIBUTE_NAME +
                                        " FROM " + CsDbContract.SurveyAttributeEntry.TABLE_NAME + " AS S " +
                                        " LEFT JOIN " +
                                        " (SELECT " + CsDbContract.SectionAttributesEntry.COLUMN_ATTRIBUTE_NAME + " FROM " +
                                        CsDbContract.SectionAttributesEntry.TABLE_NAME + " WHERE " +
                                        CsDbContract.SectionAttributesEntry.COLUMN_SECTION_ID + " = ? " +
                                        " AND " + CsDbContract.SectionAttributesEntry.COLUMN_CATEGORY_NAME + " = ?) AS D " +
                                        " ON S." + CsDbContract.SurveyAttributeEntry.COLUMN_NAME + " = " +
                                        " D." + CsDbContract.SectionAttributesEntry.COLUMN_ATTRIBUTE_NAME +
                                        " WHERE S." + CsDbContract.SurveyAttributeEntry.COLUMN_CATEGORY_ID + " = ? " +
                                        " GROUP BY S." + CsDbContract.SurveyAttributeEntry.COLUMN_NAME +
                                        " ORDER BY S." + CsDbContract.SurveyAttributeEntry.COLUMN_ORDER + " ASC",
                                new String[]{scopeId, catFullName, catId}
                        );
                        break;
                    case CsDbContract.PATH_GRAVE:
                        retCursor = mOpenHelper.getReadableDatabase().rawQuery(
                                "SELECT S." + CsDbContract.SurveyAttributeEntry._ID +
                                        ", S." + CsDbContract.SurveyAttributeEntry.COLUMN_ORDER +
                                        ", S." + CsDbContract.SurveyAttributeEntry.COLUMN_NAME +
                                        ", D." + CsDbContract.GraveAttributesEntry.COLUMN_ATTRIBUTE_NAME +
                                        " FROM " + CsDbContract.SurveyAttributeEntry.TABLE_NAME + " AS S " +
                                        " LEFT JOIN " +
                                        " (SELECT " + CsDbContract.GraveAttributesEntry.COLUMN_ATTRIBUTE_NAME + " FROM " +
                                        CsDbContract.GraveAttributesEntry.TABLE_NAME + " WHERE " +
                                        CsDbContract.GraveAttributesEntry.COLUMN_GRAVE_ID + " = ? " +
                                        " AND " + CsDbContract.GraveAttributesEntry.COLUMN_CATEGORY_NAME + " = ?) AS D " +
                                        " ON S." + CsDbContract.SurveyAttributeEntry.COLUMN_NAME + " = " +
                                        " D." + CsDbContract.GraveAttributesEntry.COLUMN_ATTRIBUTE_NAME +
                                        " WHERE S." + CsDbContract.SurveyAttributeEntry.COLUMN_CATEGORY_ID + " = ? " +
                                        " GROUP BY S." + CsDbContract.SurveyAttributeEntry.COLUMN_NAME +
                                        " ORDER BY S." + CsDbContract.SurveyAttributeEntry.COLUMN_ORDER + " ASC",
                                new String[]{scopeId, catFullName, catId}
                        );
                        break;
                    default:
                        throw new UnsupportedOperationException("Unknown scope type: " + scope);
                }

                break;
            }

            case SURVEY_ATTRIBUTES_FOR_ID:
                Long catId = CsDbContract.SurveyAttributeEntry.getSurveyCategoryIdFromUri(uri);

                if (sortOrder != null || projection != null || selection != null) {
                    Log.e(LOG_TAG, "The survey attribute table query uses pre-defined projection, selection and sort order. Ignored one or more for Uri: " + uri);
                }

                //Log.d(LOG_TAG, "Trying to retrieve the attributes for survey category id=" + surveyId);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CsDbContract.SurveyAttributeEntry.TABLE_NAME,
                        SurveyAttributesProjection,
                        sSelectSurveyQuestionAttributesById, new String[]{Long.toString(catId)}, // selection selectionArgs
                        null, null, // group by, having
                        CsDbContract.SurveyAttributeEntry.COLUMN_ORDER + " ASC"
                );
                break;

            // Exporting of data, replaces ids with names
            case CEMETERY_EXPORT:
                // This is normal, a duplicate of the simple CEMETERY case but added for consitency
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CsDbContract.CemeteryEntry.TABLE_NAME,
                        projection, // columns
                        selection, selectionArgs, // selection, selectionArgs
                        null, null, // group by, having
                        sortOrder
                );

                break;
            case SECTION_EXPORT:
                retCursor = mOpenHelper.getReadableDatabase().rawQuery(
                        "SELECT  C." + CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME + " || '_' || " +
                                "S." + CsDbContract.SectionEntry.COLUMN_SECTION_NAME + " AS fullid, " +
                                "C." + CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME + ", " +
                                //"S." + CsDbContract.SectionEntry.COLUMN_SECTION_NAME + ", " +
                                " S.* " +
                                " FROM " + CsDbContract.SectionEntry.TABLE_NAME + " AS S " +
                                " LEFT JOIN " +
                                " (SELECT " + CsDbContract.CemeteryEntry._ID + ", " +
                                CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME + " " +
                                " FROM " +
                                CsDbContract.CemeteryEntry.TABLE_NAME + ") AS C " +
                                " ON S." + CsDbContract.SectionEntry.COLUMN_CEMETERY_ID + " = " +
                                " C." + CsDbContract.CemeteryEntry._ID +
                                " ORDER BY C." + CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME + ", " +
                                " S." + CsDbContract.SectionEntry.COLUMN_SECTION_NAME + " ASC",
                        null // '?' values to pass into query
                );
                break;
            case GRAVE_EXPORT:
                retCursor = mOpenHelper.getReadableDatabase().rawQuery(
                        "SELECT   C." + CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME + " || '_' || " +
                                " S." + CsDbContract.SectionEntry.COLUMN_SECTION_NAME + " || '_' || " +
                                " G." + CsDbContract.GraveEntry.COLUMN_GRAVE_NAME + " AS fullid, " +
                                "C." + CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME + ", " +
                                " S." + CsDbContract.SectionEntry.COLUMN_SECTION_NAME + ", " +
                                //" G." + CsDbContract.GraveEntry.COLUMN_GRAVE_NAME + ", " +
                                " G.* FROM " + CsDbContract.GraveEntry.TABLE_NAME + " AS G " +

                                " LEFT JOIN " +
                                " (SELECT " + CsDbContract.CemeteryEntry._ID + ", " +
                                CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME + " " +
                                " FROM " +
                                CsDbContract.CemeteryEntry.TABLE_NAME + ") AS C " +
                                " ON G." + CsDbContract.GraveEntry.COLUMN_CEMETERY_ID + " = " +
                                " C." + CsDbContract.CemeteryEntry._ID +

                                " LEFT JOIN " +
                                " (SELECT " + CsDbContract.SectionEntry._ID + ", " +
                                CsDbContract.SectionEntry.COLUMN_SECTION_NAME + " " +
                                " FROM " +
                                CsDbContract.SectionEntry.TABLE_NAME + ") AS S " +
                                " ON G." + CsDbContract.GraveEntry.COLUMN_SECTION_ID + " = " +
                                " S." + CsDbContract.SectionEntry._ID +

                                " ORDER BY C." + CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME + ", " +
                                " S." + CsDbContract.SectionEntry.COLUMN_SECTION_NAME + ", " +
                                " G." + CsDbContract.GraveEntry.COLUMN_GRAVE_NAME + " ASC",
                        null // '?' values to pass into query
                );
                break;
            case CEMETERY_ATTRIBUTE_EXPORT:
                retCursor = mOpenHelper.getReadableDatabase().rawQuery(
                        "SELECT C." + CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME + ", " +
                                " CA.* " +
                                " FROM " + CsDbContract.CemeteryAttributesEntry.TABLE_NAME + " AS CA " +

                                " LEFT JOIN " +
                                " (SELECT " + CsDbContract.CemeteryEntry._ID + ", " +
                                CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME + " " +
                                " FROM " +
                                CsDbContract.CemeteryEntry.TABLE_NAME + ") AS C " +
                                " ON CA." + CsDbContract.CemeteryAttributesEntry.COLUMN_CEMETERY_ID + " = " +
                                " C." + CsDbContract.CemeteryEntry._ID +

                                " ORDER BY C." + CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME + ", " +
                                " CA." + CsDbContract.CemeteryAttributesEntry.COLUMN_CATEGORY_NAME + " ASC",
                        null // '?' values to pass into query
                );
                break;
            case SECTION_ATTRIBUTE_EXPORT:
                retCursor = mOpenHelper.getReadableDatabase().rawQuery(
                        "SELECT  SC." + CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME + " || '_' || " +
                                "SC." + CsDbContract.SectionEntry.COLUMN_SECTION_NAME + " AS fullid, " +
                                "SC." + CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME + ", " +
                                " SC." + CsDbContract.SectionEntry.COLUMN_SECTION_NAME + ", " +
                                " SA.* " +
                                " FROM " + CsDbContract.SectionAttributesEntry.TABLE_NAME + " AS SA " +

                                " LEFT JOIN " +
                                " (SELECT S." + CsDbContract.SectionEntry._ID + ", " +
                                " C." + CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME + ", " +
                                " S." + CsDbContract.SectionEntry.COLUMN_SECTION_NAME + " " +
                                " FROM " +
                                CsDbContract.SectionEntry.TABLE_NAME + " AS S " +
                                " LEFT JOIN " +
                                CsDbContract.CemeteryEntry.TABLE_NAME + " AS C " +
                                " ON S." + CsDbContract.SectionEntry.COLUMN_CEMETERY_ID + " = " +
                                " C." + CsDbContract.CemeteryEntry._ID + ") AS SC " +
                                " ON SA." + CsDbContract.SectionAttributesEntry.COLUMN_SECTION_ID + " = " +
                                " SC." + CsDbContract.SectionEntry._ID +

                                " ORDER BY SC." + CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME + ", " +
                                " SC." + CsDbContract.SectionEntry.COLUMN_SECTION_NAME + ", " +
                                " SA." + CsDbContract.SectionAttributesEntry.COLUMN_CATEGORY_NAME + " ASC",
                        null // '?' values to pass into query
                );
                break;
            case GRAVE_ATTRIBUTE_EXPORT:
                retCursor = mOpenHelper.getReadableDatabase().rawQuery(
                        "SELECT   SCG." + CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME + " || '_' || " +
                                " SCG." + CsDbContract.SectionEntry.COLUMN_SECTION_NAME + " || '_' || " +
                                " SCG." + CsDbContract.GraveEntry.COLUMN_GRAVE_NAME + " AS fullid, " +
                                "SCG." + CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME + ", " +
                                " SCG." + CsDbContract.SectionEntry.COLUMN_SECTION_NAME + ", " +
                                " SCG." + CsDbContract.GraveEntry.COLUMN_GRAVE_NAME + ", " +
                                " GA.* " +
                                " FROM " + CsDbContract.GraveAttributesEntry.TABLE_NAME + " AS GA " +

                                " LEFT JOIN " +
                                " (SELECT G." + CsDbContract.GraveEntry._ID + ", " +
                                " C." + CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME + ", " +
                                " S." + CsDbContract.SectionEntry.COLUMN_SECTION_NAME + ", " +
                                " G." + CsDbContract.GraveEntry.COLUMN_GRAVE_NAME + " " +
                                " FROM " +
                                CsDbContract.GraveEntry.TABLE_NAME + " AS G " +
                                " LEFT JOIN " +
                                CsDbContract.CemeteryEntry.TABLE_NAME + " AS C " +
                                " ON G." + CsDbContract.GraveEntry.COLUMN_CEMETERY_ID + " = " +
                                " C." + CsDbContract.CemeteryEntry._ID + " " +
                                " LEFT JOIN " +
                                CsDbContract.SectionEntry.TABLE_NAME + " AS S " +
                                " ON G." + CsDbContract.GraveEntry.COLUMN_SECTION_ID + " = " +
                                " S." + CsDbContract.SectionEntry._ID + ") AS SCG " +
                                " ON GA." + CsDbContract.GraveAttributesEntry.COLUMN_GRAVE_ID + " = " +
                                " SCG." + CsDbContract.GraveEntry._ID +

                                " ORDER BY SCG." + CsDbContract.CemeteryEntry.COLUMN_CEMETERY_NAME + ", " +
                                " SCG." + CsDbContract.SectionEntry.COLUMN_SECTION_NAME + ", " +
                                " SCG." + CsDbContract.GraveEntry.COLUMN_GRAVE_NAME + ", " +
                                " GA." + CsDbContract.SectionAttributesEntry.COLUMN_CATEGORY_NAME + " ASC",
                        null // '?' values to pass into query
                );
                break;
            case PICTURE_EXPORT:
                retCursor = getPictureScopeNames(uri, projection, selection, selectionArgs, sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Uknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        /**
         * Use CONTENT_TYPE when 0 or more rows expected.
         * Use CONTENT_ITEM_TYPE when exactly 1 row is expected.
         */
        switch (match) {
            case CEMETERY:
                return CsDbContract.CemeteryEntry.CONTENT_TYPE;
            case CEMETERY_ID:
                return CsDbContract.CemeteryEntry.CONTENT_ITEM_TYPE;

            case SECTION:
                return CsDbContract.SectionEntry.CONTENT_TYPE;
            case SECTION_ID:
                return CsDbContract.SectionEntry.CONTENT_ITEM_TYPE;
            case SECTIONS_FROM_CEMETERY_ID:
                return CsDbContract.SectionEntry.CONTENT_TYPE;
            case SECTION_ID_NAMES:
                return CsDbContract.SectionEntry.CONTENT_TYPE;

            case GRAVE:
                return CsDbContract.GraveEntry.CONTENT_TYPE;
            case GRAVE_ID:
                return CsDbContract.GraveEntry.CONTENT_ITEM_TYPE;
            case GRAVES_FROM_SECTION_ID:
                return CsDbContract.GraveEntry.CONTENT_TYPE;
            case GRAVE_ID_NAMES:
                return CsDbContract.GraveEntry.CONTENT_TYPE;

            case BOOKMARK:
                return CsDbContract.BookmarkEntry.CONTENT_TYPE;
            case BOOKMARK_WITH_NAMES:
                return CsDbContract.BookmarkEntry.CONTENT_TYPE;
            case BOOKMARK_ID_SCOPE:
                return CsDbContract.BookmarkEntry.CONTENT_ITEM_TYPE;

            case PICTURE:
                return CsDbContract.PictureEntry.CONTENT_TYPE;
            case PICTURE_WITH_NAMES:
                return CsDbContract.PictureEntry.CONTENT_TYPE;
            case PICTURE_ID_SCOPE:
                return CsDbContract.PictureEntry.CONTENT_ITEM_TYPE;

            case SURVEY:
                return CsDbContract.SurveyCategoryEntry.CONTENT_TYPE;
            case SURVEY_FOR_SCOPE:
                return CsDbContract.SurveyCategoryEntry.CONTENT_TYPE;
            case SURVEY_FOR_SCOPE_TAB:
                return CsDbContract.SurveyCategoryEntry.CONTENT_TYPE;
            case SURVEY_ATTRIBUTES:
                return CsDbContract.SurveyAttributeEntry.CONTENT_TYPE;
            case SURVEY_ATTRIBUTES_FOR_ID:
                return CsDbContract.SurveyAttributeEntry.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;
        long _id = 0;

        //Log.d(LOG_TAG, "Inserting: " + uri.toString() + ", UriMatcher=" + sUriMatcher.match(uri));

        switch (sUriMatcher.match(uri)) {
            case NEW_COLUMN:
                // Add a new column to the scope table
                String scope = uri.getPathSegments().get(2);
                String colName = uri.getPathSegments().get(3);
                String colType = uri.getPathSegments().get(4);
                // ALTER TABLE mytable ADD COLUMN mycolumn TEXT
                db.execSQL("ALTER TABLE " + scope + " ADD COLUMN " + colName + " " + colType);
                return null;

            case CEMETERY:
                _id = db.insert(CsDbContract.CemeteryEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = CsDbContract.CemeteryEntry.buildCemeteryIdUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            case CEMETERY_ATTRIBUTE:
                _id = db.insert(CsDbContract.CemeteryAttributesEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = CsDbContract.CemeteryAttributesEntry.buildCemeteryIdUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            case SECTION:
                _id = db.insert(CsDbContract.SectionEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = CsDbContract.SectionEntry.buildSectionIdUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            case SECTION_ATTRIBUTE:
                _id = db.insert(CsDbContract.SectionAttributesEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = CsDbContract.SectionAttributesEntry.buildSectionIdUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            case GRAVE:
                _id = db.insert(CsDbContract.GraveEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = CsDbContract.GraveEntry.buildGraveIdUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            case GRAVE_ATTRIBUTE:
                _id = db.insert(CsDbContract.GraveAttributesEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = CsDbContract.GraveAttributesEntry.buildGraveIdUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            case BOOKMARK:
                _id = db.insert(CsDbContract.BookmarkEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = CsDbContract.BookmarkEntry.buildBookmarkIdUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            case PICTURE:
                _id = db.insert(CsDbContract.PictureEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = CsDbContract.PictureEntry.buildPictureIdUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            case SURVEY:
                _id = db.insert(CsDbContract.SurveyCategoryEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = CsDbContract.SurveyCategoryEntry.buildSurveyCategoryUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            case SURVEY_ATTRIBUTES:
                _id = db.insert(CsDbContract.SurveyAttributeEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = CsDbContract.SurveyAttributeEntry.buildSurveyAttributeCategoryIdUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted = 0;

        Log.d(LOG_TAG, "Content provider delete: " + uri.toString());

        switch (sUriMatcher.match(uri)) {
            case CEMETERY_DELETE:
                if( selection != null || selectionArgs != null ) {
                    Log.w(LOG_TAG, "Deletion call ignores selection and arguments");
                }

                String [] cemetery_id = CsDbContract.CemeteryEntry.getCemeteryDeletionIdArray(uri);

                // Delete in all the tables
                // Primary tables
                rowsDeleted = db.delete(CsDbContract.CemeteryEntry.TABLE_NAME,
                        CsDbContract.CemeteryEntry._ID + " = ?" ,
                        cemetery_id);
                rowsDeleted += db.delete(CsDbContract.SectionEntry.TABLE_NAME,
                        CsDbContract.SectionEntry.COLUMN_CEMETERY_ID + " = ?" ,
                        cemetery_id);
                rowsDeleted += db.delete(CsDbContract.GraveEntry.TABLE_NAME,
                        CsDbContract.GraveEntry.COLUMN_CEMETERY_ID + " = ?" ,
                        cemetery_id);
                // Attribute tables
                rowsDeleted += db.delete(CsDbContract.CemeteryAttributesEntry.TABLE_NAME,
                        CsDbContract.CemeteryAttributesEntry.COLUMN_CEMETERY_ID + " = ?",
                        cemetery_id);
                // Can't delete section and grave attributes without first getting their ids - don't worry about it.

                // Pictures
                rowsDeleted += db.delete(CsDbContract.PictureEntry.TABLE_NAME,
                        CsDbContract.PictureEntry.COLUMN_CEMETERY_ID + " = ?",
                        cemetery_id);
                // Bookmarks
                rowsDeleted += db.delete(CsDbContract.BookmarkEntry.TABLE_NAME,
                        CsDbContract.BookmarkEntry.COLUMN_CEMETERY_ID + " = ?",
                        cemetery_id);

                break;
            case SECTION_DELETE:
                if( selection != null || selectionArgs != null ) {
                    Log.w(LOG_TAG, "Deletion call ignores selection and arguments");
                }

                String [] section_ids = CsDbContract.SectionEntry.getSectionDeletionIdArray(uri);

                // Delete in all the tables
                // Primary tables
                rowsDeleted = db.delete(CsDbContract.SectionEntry.TABLE_NAME,
                        CsDbContract.SectionEntry.COLUMN_CEMETERY_ID + " = ? AND " + CsDbContract.SectionEntry._ID + " = ?",
                        section_ids);
                rowsDeleted += db.delete(CsDbContract.GraveEntry.TABLE_NAME,
                        CsDbContract.GraveEntry.COLUMN_CEMETERY_ID + " = ? AND " + CsDbContract.GraveEntry.COLUMN_SECTION_ID + " = ?",
                        section_ids);
                // Attribute tables
                rowsDeleted += db.delete(CsDbContract.SectionAttributesEntry.TABLE_NAME,
                        CsDbContract.SectionAttributesEntry.COLUMN_SECTION_ID + " = ?",
                        new String[]{section_ids[1]});
                // Can't delete grave attributes without first getting their ids - don't worry about it.

                // Pictures
                rowsDeleted += db.delete(CsDbContract.PictureEntry.TABLE_NAME,
                        CsDbContract.PictureEntry.COLUMN_CEMETERY_ID + " = ? AND " + CsDbContract.PictureEntry.COLUMN_SECTION_ID + " = ?",
                        section_ids);
                // Bookmarks
                rowsDeleted += db.delete(CsDbContract.BookmarkEntry.TABLE_NAME,
                        CsDbContract.BookmarkEntry.COLUMN_CEMETERY_ID + " = ? AND " + CsDbContract.BookmarkEntry.COLUMN_SECTION_ID + " = ?",
                        section_ids);

                break;
            case GRAVE_DELETE:
                if( selection != null || selectionArgs != null ) {
                    Log.w(LOG_TAG, "Deletion call ignores selection and arguments");
                }

                String [] grave_ids = CsDbContract.GraveEntry.getGraveDeletionIdArray(uri);

                // Delete in all the tables
                // Primary tables
                rowsDeleted += db.delete(CsDbContract.GraveEntry.TABLE_NAME,
                        CsDbContract.GraveEntry.COLUMN_CEMETERY_ID + " = ? AND " + CsDbContract.GraveEntry.COLUMN_SECTION_ID + " = ? AND " + CsDbContract.GraveEntry._ID + " = ?",
                        grave_ids);
                // Attribute tables
                rowsDeleted += db.delete(CsDbContract.GraveAttributesEntry.TABLE_NAME,
                        CsDbContract.GraveAttributesEntry.COLUMN_GRAVE_ID + " = ?",
                        new String[]{grave_ids[2]});

                // Pictures
                rowsDeleted += db.delete(CsDbContract.PictureEntry.TABLE_NAME,
                        CsDbContract.PictureEntry.COLUMN_CEMETERY_ID + " = ? AND " + CsDbContract.PictureEntry.COLUMN_SECTION_ID + " = ? AND " + CsDbContract.PictureEntry.COLUMN_GRAVE_ID + " = ?",
                        grave_ids);
                // Bookmarks
                rowsDeleted += db.delete(CsDbContract.BookmarkEntry.TABLE_NAME,
                        CsDbContract.BookmarkEntry.COLUMN_CEMETERY_ID + " = ? AND " + CsDbContract.BookmarkEntry.COLUMN_SECTION_ID + " = ? AND " + CsDbContract.BookmarkEntry.COLUMN_GRAVE_ID + " = ?",
                        grave_ids);

                break;

            case CEMETERY_ATTRIBUTE_FOR_CEMETERYID_CATEGORY_ATTRIBUTE:
                rowsDeleted = db.delete(CsDbContract.CemeteryAttributesEntry.TABLE_NAME,
                        sSelectCemeteryAttributesCategoryByIdAndName,
                        CsDbContract.CemeteryAttributesEntry.getCemeteryIdCategoryAttributeFromUri(uri));
                break;

            case SECTION_ATTRIBUTE_FOR_SECTIONID_CATEGORY_ATTRIBUTE:
                rowsDeleted = db.delete(CsDbContract.SectionAttributesEntry.TABLE_NAME,
                        sSelectSectionAttributesCategoryByIdAndName,
                        CsDbContract.SectionAttributesEntry.getSectionIdCategoryAttributeFromUri(uri));
                break;

            case GRAVE_ATTRIBUTE_FOR_GRAVEID_CATEGORY_ATTRIBUTE:
                rowsDeleted = db.delete(CsDbContract.GraveAttributesEntry.TABLE_NAME,
                        sSelectGraveAttributesCategoryByIdAndName,
                        CsDbContract.GraveAttributesEntry.getGraveIdCategoryAttributeFromUri(uri));
                break;
            // Only for grave do we want the ability to delete all the attribute data associated
            case GRAVE_ATTRIBUTE_FOR_GRAVEID:
                rowsDeleted = db.delete(CsDbContract.GraveAttributesEntry.TABLE_NAME,
                        sSelectGraveAttributesByGraveId,
                        new String[]{CsDbContract.GraveAttributesEntry.getGraveIdFromUri(uri)});
                break;


            case BOOKMARK:
                rowsDeleted = db.delete(CsDbContract.BookmarkEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case SURVEY:
                rowsDeleted = db.delete(CsDbContract.SurveyCategoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SURVEY_ATTRIBUTES:
                rowsDeleted = db.delete(CsDbContract.SurveyAttributeEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted == 0) {
            Log.w(LOG_TAG, "Failed to delete any rows for request " + uri);
        } else {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

//        Log.d(LOG_TAG, "Update Uri=" + uri);
//        Log.d(LOG_TAG, "Content values =" + values.toString());
//        Log.d(LOG_TAG, "Selection =" + selection);
//        Log.d(LOG_TAG, "Selection Args =" + Arrays.toString(selectionArgs));

        switch (match) {
            case CEMETERY:
                //db.execSQL("UPDATE " + CsDbContract.CemeteryEntry.TABLE_NAME + " SET d_surrounds_cemetery_radio='Test'");
                rowsUpdated = db.update(CsDbContract.CemeteryEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;

            case SECTION:
                rowsUpdated = db.update(CsDbContract.SectionEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case GRAVE:
                rowsUpdated = db.update(CsDbContract.GraveEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case GRAVE_CLEAR_FOR_GRAVEID:
                Cursor cursor = db.rawQuery("SELECT * FROM " + CsDbContract.GraveEntry.TABLE_NAME + " WHERE 0", null);
                String[] colNames = cursor.getColumnNames();

                ContentValues cv = new ContentValues();
                for( int i = 0; i < colNames.length; i++ ) {
                    if( colNames[i].equals(CsDbContract.GraveEntry._ID) ||
                            colNames[i].equals(CsDbContract.GraveEntry.COLUMN_CEMETERY_ID) ||
                            colNames[i].equals(CsDbContract.GraveEntry.COLUMN_SECTION_ID) ||
                            colNames[i].equals(CsDbContract.GraveEntry.COLUMN_GRAVE_NAME) ) {
                        // do nothing
                    } else {
                        cv.putNull(colNames[i]);
                    }
                }

                // Update grave row
                rowsUpdated = db.update(CsDbContract.GraveEntry.TABLE_NAME, cv, CsDbContract.GraveEntry._ID + " = ? ",
                        new String[]{Long.toString(CsDbContract.GraveEntry.getGraveIdFromUri(uri))} );
                break;
            default:
                throw new UnsupportedOperationException("Unknown editing uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    private Cursor getCemeterySectionsListUsingId(Uri uri, String[] projection, String sortOrder) {
        // extract the cemetery rowId from the Uri
        String cemeteryRowId = Long.toString(CsDbContract.SectionEntry.getCemeteryIdFromUri(uri));

        // set sort order if not set
        if (sortOrder == null) {
            sortOrder = " UPPER(" + CsDbContract.SectionEntry.COLUMN_SECTION_NAME + ") ASC";
        }

        return mOpenHelper.getReadableDatabase().query(
                CsDbContract.SectionEntry.TABLE_NAME,
                projection, // columns
                sSelectSectionsByCemeteryId, // selection string with placeholders
                new String[]{cemeteryRowId}, // selectionArgs[]
                null, null, // group by, having
                sortOrder
        );
    }

    private Cursor getSectionGraveListUsingId(Uri uri, String[] projection, String sortOrder) {
        // extract the cemetery rowId from the Uri
        String sectionRowId = Long.toString(CsDbContract.GraveEntry.getSectionIdFromUri(uri));

        // set sort order if not set
        if (sortOrder == null) {
            sortOrder = " UPPER(" + CsDbContract.GraveEntry.COLUMN_GRAVE_NAME + ") ASC";
        }

        return mOpenHelper.getReadableDatabase().query(
                CsDbContract.GraveEntry.TABLE_NAME,
                projection, // columns
                sSelectGravesBySectionId, // selection string with placeholders
                new String[]{sectionRowId}, // selectionArgs[]
                null, null, // group by, having
                sortOrder
        );
    }

    private Cursor getBookmarkWithScopeId(Uri uri, String[] projection, String sortOrder) {
        String scopeId = uri.getPathSegments().get(1);
        String scopeName = uri.getPathSegments().get(2);

        String scopeColumnName;
        switch (scopeName) {
            case CsDbContract.PATH_CEMETERY:
                scopeColumnName = CsDbContract.BookmarkEntry.COLUMN_CEMETERY_ID;
                break;
            case CsDbContract.PATH_SECTION:
                scopeColumnName = CsDbContract.BookmarkEntry.COLUMN_SECTION_ID;
                break;
            case CsDbContract.PATH_GRAVE:
                scopeColumnName = CsDbContract.BookmarkEntry.COLUMN_GRAVE_ID;
                break;
            default:
                throw new UnsupportedOperationException("An incorrect scope name (CsDbContract.PATH_*) is used: " + scopeName);
        }

        return mOpenHelper.getReadableDatabase().query(
                CsDbContract.BookmarkEntry.TABLE_NAME,
                projection,
                CsDbContract.BookmarkEntry.COLUMN_SCOPE + " = ? AND " + scopeColumnName + " = ? ",
                new String[]{scopeName, scopeId},
                null, null, sortOrder
        );
    }

    private Cursor getPictureWithScopeId(Uri uri, String[] projection, String sortOrder) {
        String scopeId = uri.getPathSegments().get(1);
        String scopeName = uri.getPathSegments().get(2);

        String scopeColumnName;
        switch (scopeName) {
            case CsDbContract.PATH_CEMETERY:
                scopeColumnName = CsDbContract.PictureEntry.COLUMN_CEMETERY_ID;
                break;
            case CsDbContract.PATH_SECTION:
                scopeColumnName = CsDbContract.PictureEntry.COLUMN_SECTION_ID;
                break;
            case CsDbContract.PATH_GRAVE:
                scopeColumnName = CsDbContract.PictureEntry.COLUMN_GRAVE_ID;
                break;
            default:
                throw new UnsupportedOperationException("An incorrect scope name (CsDbContract.PATH_*) is used: " + scopeName);
        }

        return mOpenHelper.getReadableDatabase().query(
                CsDbContract.PictureEntry.TABLE_NAME,
                projection,
                CsDbContract.PictureEntry.COLUMN_SCOPE + " = ? AND " + scopeColumnName + " = ? ",
                new String[]{scopeName, scopeId},
                null, null, sortOrder
        );
    }

}
