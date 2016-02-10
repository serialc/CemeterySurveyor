package net.frakturmedia.cemeterysurvey.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by cyrille on 25/01/16.
 */
public class CsDbContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "net.frakturmedia.cemeterysurvey";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sunshine.app/weather/ is a valid path for
    // looking at weather data. content://com.example.android.sunshine.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.

    public static final String PATH_MAIN = "main"; // not implemented in table - used as constant only
    public static final String PATH_SURVEY = "survey"; // not implemented in table - used as constant only
    public static final String PATH_CEMETERY = "cemetery";
    public static final String PATH_SECTION = "section";
    public static final String PATH_GRAVE = "grave";

    public static final String PATH_CEMETERY_ATTRIBUTES = "cemeteryattributes";
    public static final String PATH_SECTION_ATTRIBUTES = "sectionattributes";
    public static final String PATH_GRAVE_ATTRIBUTES = "graveattributes";

    public static final String PATH_BOOKMARK = "bookmark";
    public static final String PATH_PICTURE = "picture";
    public static final String PATH_SURVEY_CATEGORY = "surveycategories";
    public static final String PATH_SURVEY_ATTRIBUTE = "surveyattributes";

    public static final class CemeteryEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CEMETERY).build();

        // CURSOR_DIR_BASE_TYPE can contain 0 or more rows
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CEMETERY;
        // CURSOR_ITEM_BASE_TYPE must contain exactly one row
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CEMETERY;

        // Table name
        public static final String TABLE_NAME = PATH_CEMETERY;

        // Define columns
        public static final String COLUMN_CEMETERY_NAME = "cemetery_name";

        // Allows us to use the _id to generate a Uri
        public static Uri buildCemeteryIdUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }
        public static long getCemeteryIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }
    }

    public static final class SectionEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SECTION).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CEMETERY + "/" + PATH_SECTION;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CEMETERY + "/" + PATH_SECTION;

        // Table name
        public static final String TABLE_NAME = PATH_SECTION;

        // Define columns
        public static final String COLUMN_CEMETERY_ID = "cemetery_id";
        public static final String COLUMN_SECTION_NAME = "section_name";

        // Allows us to use the _id to generate a Uri
        public static Uri buildSectionIdUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }
        public static long getSectionIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }

        // Creates uri for getting all sections in a cemetery id
        public static Uri buildSectionsInCemeteryIdUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(PATH_CEMETERY).appendPath(Long.toString(id)).build();
        }
        public static long getCemeteryIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(2));
        }
    }

    public static final class GraveEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_GRAVE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CEMETERY + "/" + PATH_SECTION + "/" + PATH_GRAVE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CEMETERY + "/" + PATH_SECTION + "/" + PATH_GRAVE;

        // Table name
        public static final String TABLE_NAME = PATH_GRAVE;

        // Define columns
        public static final String COLUMN_CEMETERY_ID = "cemetery_id";
        public static final String COLUMN_SECTION_ID = "section_id";
        public static final String COLUMN_GRAVE_NAME = "grave_name";

        // Creates uri for getting all graves in a section id
        public static Uri buildGravesInSectionIdUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(PATH_SECTION).appendPath(Long.toString(id)).build();
        }
        // Allows us to use the _id to generate a Uri
        public static Uri buildGraveIdUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }
        public static Uri buildClearGraveIdUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).appendPath("clear").build();
        }
        public static long getGraveIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }
        public static long getSectionIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(2));
        }
    }

    public static final class CemeteryAttributesEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CEMETERY_ATTRIBUTES).build();

        // Table name
        public static final String TABLE_NAME = PATH_CEMETERY_ATTRIBUTES;

        // Define columns
        public static final String COLUMN_CEMETERY_ID = "cemetery_id";
        public static final String COLUMN_CATEGORY_NAME = "category_name";
        public static final String COLUMN_ATTRIBUTE_NAME = "attribute_name";

        public static Uri buildCemeteryIdUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }
        public static Uri buildCemeteryIdCategoryUri(long id, String category) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).appendPath(category).build();
        }
        public static String[] getCemeteryIdCategoryFromUri(Uri uri) {
            return new String[]{uri.getPathSegments().get(1), uri.getPathSegments().get(2)};
        }
        public static String[] getCemeteryIdCategoryAttributeFromUri(Uri uri) {
            return new String[]{uri.getPathSegments().get(1), uri.getPathSegments().get(2), uri.getPathSegments().get(3)};
        }
    }

    public static final class SectionAttributesEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SECTION_ATTRIBUTES).build();

        // Table name
        public static final String TABLE_NAME = PATH_SECTION_ATTRIBUTES;

        // Define columns
        public static final String COLUMN_SECTION_ID = "section_id";
        public static final String COLUMN_CATEGORY_NAME = "category_name";
        public static final String COLUMN_ATTRIBUTE_NAME = "attribute_name";

        public static Uri buildSectionIdUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }
        public static Uri buildSectionIdCategoryUri(long id, String category) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).appendPath(category).build();
        }
        public static String[] getSectionIdCategoryFromUri(Uri uri) {
            return new String[]{uri.getPathSegments().get(1), uri.getPathSegments().get(2)};
        }
        public static String[] getSectionIdCategoryAttributeFromUri(Uri uri) {
            return new String[]{uri.getPathSegments().get(1), uri.getPathSegments().get(2), uri.getPathSegments().get(3)};
        }
    }

    public static final class GraveAttributesEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_GRAVE_ATTRIBUTES).build();

        // Table name
        public static final String TABLE_NAME = PATH_GRAVE_ATTRIBUTES;

        // Define columns
        public static final String COLUMN_GRAVE_ID = "grave_id";
        public static final String COLUMN_CATEGORY_NAME = "category_name";
        public static final String COLUMN_ATTRIBUTE_NAME = "attribute_name";

        public static Uri buildGraveIdUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }
        public static Uri buildGraveIdCategoryUri(long id, String category) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).appendPath(category).build();
        }
        public static String getGraveIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
        public static String[] getGraveIdCategoryFromUri(Uri uri) {
            return new String[]{uri.getPathSegments().get(1), uri.getPathSegments().get(2)};
        }
        public static String[] getGraveIdCategoryAttributeFromUri(Uri uri) {
            return new String[]{uri.getPathSegments().get(1), uri.getPathSegments().get(2), uri.getPathSegments().get(3)};
        }
    }

    public static final class BookmarkEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOKMARK).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKMARK;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKMARK;

        // Table name
        public static final String TABLE_NAME = PATH_BOOKMARK;

        // Define columns
        public static final String COLUMN_CEMETERY_ID = "cemetery_id";
        public static final String COLUMN_SECTION_ID = "section_id";
        public static final String COLUMN_GRAVE_ID = "grave_id";
        public static final String COLUMN_SCOPE = "scope";

        // Allows us to use the _id to generate a Uri
        public static Uri buildBookmarkIdUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }
        // build uri to request existence of bookmark at scope
        public static Uri buildBookmarkUriFromScopeId(long id, String scope) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).appendPath(scope).build();
        }
        public static long getBookmarkIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }
    }

    public static final class PictureEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PICTURE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PICTURE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE+ "/" + CONTENT_AUTHORITY + "/" + PATH_PICTURE;

        // Table name
        public static final String TABLE_NAME = PATH_PICTURE;

        // Define columns
        public static final String COLUMN_CEMETERY_ID = "cemetery_id";
        public static final String COLUMN_SECTION_ID = "section_id";
        public static final String COLUMN_GRAVE_ID = "grave_id";
        public static final String COLUMN_CATEGORY_NAME = "category_name";
        public static final String COLUMN_ATTRIBUTE_NAME = "attribute_name";
        public static final String COLUMN_SCOPE = "scope";
        public static final String COLUMN_FILE_NAME = "picture_filename";

        // Allows us to use the _id to generate a Uri
        public static Uri buildPictureIdUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }
        // build uri to request existence of bookmark at scope
        public static Uri buildPictureUriFromScopeId(long id, String scope) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).appendPath(scope).build();
        }
        public static long getPictureIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }
    }

    public static final class SurveyCategoryEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SURVEY_CATEGORY).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SURVEY_CATEGORY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE+ "/" + CONTENT_AUTHORITY + "/" + PATH_SURVEY_CATEGORY;

        // Table name
        public static final String TABLE_NAME = PATH_SURVEY_CATEGORY;

        // Define columns
        public static final String COLUMN_SCOPE = "category_scope";
        public static final String COLUMN_TAB_NUMBER = "tab_num";
        public static final String COLUMN_GROUP_NUMBER = "group_num";
        public static final String COLUMN_CATEGORY_NUMBER = "category_num";
        public static final String COLUMN_TAB_NAME = "tab_name";
        public static final String COLUMN_GROUP_NAME = "group_name";

        public static final String COLUMN_NAME = "category_name";
        public static final String COLUMN_TITLE= "category_title";
        public static final String COLUMN_TYPE = "category_type";
        public static final String COLUMN_PICTURE = "camera_enabled";
        public static final String COLUMN_ATTRIBUTE_PICTURE = "attribute_camera_enabled";
        public static final String COLUMN_REQUIRED = "required";
        public static final String COLUMN_THUMBNAILS_PATH = "thumbnails_path";

        public static long getSurveyIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }
        public static String getSurveyScopeFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
        public static String[] getSurveyScopeAndTabFromUri(Uri uri) {
            // the second segment is the word 'tab'
            return new String[]{uri.getPathSegments().get(1), uri.getPathSegments().get(3)};
        }
        public static Uri buildSurveyCategoryUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }
        public static Uri buildSurveyCategoryScopeUri(String scope) {
            return CONTENT_URI.buildUpon().appendPath(scope).build();
        }
        public static Uri buildSurveyCategoryScopeTypeScopeId(String scope, Long scopeId) {
            return CONTENT_URI.buildUpon().appendPath(scope).appendPath(Long.toString(scopeId)).build();
        }
        public static Uri buildSurveyCategoryScopeTypeScopeIdTabUri(String scope, Long scopeId, int tab) {
            return CONTENT_URI.buildUpon().appendPath(scope).appendPath(Long.toString(scopeId)).appendPath(Integer.toString(tab)).build();
        }
        public static Uri buildSurveyJoinedCategoryAttibuteScopeUri(String scope) {
            return CONTENT_URI.buildUpon().appendPath(scope).appendPath("joined").build();
        }
    }

    public static final class SurveyAttributeEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SURVEY_ATTRIBUTE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SURVEY_ATTRIBUTE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE+ "/" + CONTENT_AUTHORITY + "/" + PATH_SURVEY_ATTRIBUTE;

        // Table name
        public static final String TABLE_NAME = PATH_SURVEY_ATTRIBUTE;

        // Define columns
        public static final String COLUMN_CATEGORY_ID = "category_id";
        public static final String COLUMN_NAME = "attribute_name";
        public static final String COLUMN_ORDER = "attribute_order";

        public static long getSurveyCategoryIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }
        public static Uri buildSurveyAttributeCategoryIdUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }
//        public static Uri buildSurveyScopeCategoryIdUri(String scope, long id) {
//            return CONTENT_URI.buildUpon().appendPath(scope).appendPath(Long.toString(id)).build();
//        }
    }
}
