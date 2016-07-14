package net.frakturmedia.cemeterysurvey.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.frakturmedia.cemeterysurvey.data.CsDbContract.SurveyCategoryEntry;
import net.frakturmedia.cemeterysurvey.data.CsDbContract.SurveyAttributeEntry;
import net.frakturmedia.cemeterysurvey.data.CsDbContract.BookmarkEntry;
import net.frakturmedia.cemeterysurvey.data.CsDbContract.CemeteryEntry;
import net.frakturmedia.cemeterysurvey.data.CsDbContract.GraveEntry;
import net.frakturmedia.cemeterysurvey.data.CsDbContract.PictureEntry;
import net.frakturmedia.cemeterysurvey.data.CsDbContract.SectionEntry;
import net.frakturmedia.cemeterysurvey.data.CsDbContract.CemeteryAttributesEntry;
import net.frakturmedia.cemeterysurvey.data.CsDbContract.SectionAttributesEntry;
import net.frakturmedia.cemeterysurvey.data.CsDbContract.GraveAttributesEntry;
/**
 * Created by cyrille on 25/01/16.
 */
public class CsDbHelper extends SQLiteOpenHelper {

    // If you change the DB schema, must increment the version
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "cemetery_survey.db";

    public CsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_CEMETERY_TABLE = "CREATE TABLE " + CemeteryEntry.TABLE_NAME + " (" +
                CemeteryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CemeteryEntry.COLUMN_CEMETERY_NAME + " TEXT UNIQUE NOT NULL " +
                " );";

        final String SQL_CREATE_SECTION_TABLE = "CREATE TABLE " + SectionEntry.TABLE_NAME + " (" +
                SectionEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SectionEntry.COLUMN_CEMETERY_ID + " INTEGER NOT NULL, " +
                SectionEntry.COLUMN_SECTION_NAME + " TEXT NOT NULL, " +
                " FOREIGN KEY (" + SectionEntry.COLUMN_CEMETERY_ID + ") REFERENCES " +
                CemeteryEntry.TABLE_NAME + " (" + CemeteryEntry._ID + ")" +
                " );";

        final String SQL_CREATE_GRAVE_TABLE = "CREATE TABLE " + GraveEntry.TABLE_NAME + " (" +
                GraveEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                GraveEntry.COLUMN_CEMETERY_ID + " INTEGER NOT NULL, " +
                GraveEntry.COLUMN_SECTION_ID + " INTEGER NOT NULL, " +
                GraveEntry.COLUMN_GRAVE_NAME + " TEXT NOT NULL, " +
                GraveEntry.COLUMN_GRAVE_SURVEY_DATE + " DATE NOT NULL, " +
                GraveEntry.COLUMN_GRAVE_STATUS + " INTEGER NOT NULL, " +
                " FOREIGN KEY (" + GraveEntry.COLUMN_CEMETERY_ID + ") REFERENCES " +
                CemeteryEntry.TABLE_NAME + " (" + CemeteryEntry._ID + ")," +
                " FOREIGN KEY (" + GraveEntry.COLUMN_SECTION_ID + ") REFERENCES " +
                SectionEntry.TABLE_NAME + " (" + SectionEntry._ID + ")" +
                " );";

        final String SQL_CREATE_CEMETERY_ATTRIBUTES_TABLE = "CREATE TABLE " + CemeteryAttributesEntry.TABLE_NAME + " (" +
                CemeteryAttributesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CemeteryAttributesEntry.COLUMN_CEMETERY_ID + " INTEGER NOT NULL, " +
                CemeteryAttributesEntry.COLUMN_CATEGORY_NAME + " TEXT NOT NULL, " +
                CemeteryAttributesEntry.COLUMN_ATTRIBUTE_NAME + " TEXT NOT NULL, " +
                " FOREIGN KEY (" + CemeteryAttributesEntry.COLUMN_CEMETERY_ID + ") REFERENCES " +
                CemeteryEntry.TABLE_NAME + " (" + CemeteryEntry._ID + ")," +
                " FOREIGN KEY (" + CemeteryAttributesEntry.COLUMN_CATEGORY_NAME + ") REFERENCES " +
                SurveyCategoryEntry.TABLE_NAME + " (" + SurveyCategoryEntry.COLUMN_NAME + ")," +
                " FOREIGN KEY (" + CemeteryAttributesEntry.COLUMN_ATTRIBUTE_NAME + ") REFERENCES " +
                SurveyAttributeEntry.TABLE_NAME + " (" + SurveyAttributeEntry.COLUMN_NAME + ")" +
                " );";

        final String SQL_CREATE_SECTION_ATTRIBUTES_TABLE = "CREATE TABLE " + SectionAttributesEntry.TABLE_NAME + " (" +
                SectionAttributesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SectionAttributesEntry.COLUMN_SECTION_ID + " INTEGER NOT NULL, " +
                SectionAttributesEntry.COLUMN_CATEGORY_NAME + " TEXT NOT NULL, " +
                SectionAttributesEntry.COLUMN_ATTRIBUTE_NAME + " TEXT NOT NULL, " +
                " FOREIGN KEY (" + SectionAttributesEntry.COLUMN_SECTION_ID + ") REFERENCES " +
                SectionEntry.TABLE_NAME + " (" + SectionEntry._ID + ")," +
                " FOREIGN KEY (" + SectionAttributesEntry.COLUMN_CATEGORY_NAME + ") REFERENCES " +
                SurveyCategoryEntry.TABLE_NAME + " (" + SurveyCategoryEntry.COLUMN_NAME + ")," +
                " FOREIGN KEY (" + SectionAttributesEntry.COLUMN_ATTRIBUTE_NAME + ") REFERENCES " +
                SurveyAttributeEntry.TABLE_NAME + " (" + SurveyAttributeEntry.COLUMN_NAME + ")" +
                " );";

        final String SQL_CREATE_GRAVE_ATTRIBUTES_TABLE = "CREATE TABLE " + GraveAttributesEntry.TABLE_NAME + " (" +
                GraveAttributesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                GraveAttributesEntry.COLUMN_GRAVE_ID + " INTEGER NOT NULL, " +
                GraveAttributesEntry.COLUMN_CATEGORY_NAME + " TEXT NOT NULL, " +
                GraveAttributesEntry.COLUMN_ATTRIBUTE_NAME + " TEXT NOT NULL, " +
                " FOREIGN KEY (" + GraveAttributesEntry.COLUMN_GRAVE_ID + ") REFERENCES " +
                GraveEntry.TABLE_NAME + " (" + GraveEntry._ID + ")," +
                " FOREIGN KEY (" + GraveAttributesEntry.COLUMN_CATEGORY_NAME + ") REFERENCES " +
                SurveyCategoryEntry.TABLE_NAME + " (" + SurveyCategoryEntry.COLUMN_NAME + ")," +
                " FOREIGN KEY (" + GraveAttributesEntry.COLUMN_ATTRIBUTE_NAME + ") REFERENCES " +
                SurveyAttributeEntry.TABLE_NAME + " (" + SurveyAttributeEntry.COLUMN_NAME + ")" +
                " );";

        final String SQL_CREATE_BOOKMARK_TABLE = "CREATE TABLE " + BookmarkEntry.TABLE_NAME + " (" +
                BookmarkEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BookmarkEntry.COLUMN_CEMETERY_ID + " INTEGER NOT NULL, " +
                BookmarkEntry.COLUMN_SECTION_ID + " INTEGER, " +
                BookmarkEntry.COLUMN_GRAVE_ID + " INTEGER, " +
                BookmarkEntry.COLUMN_SCOPE + " TEXT NOT NULL, " +
                " FOREIGN KEY (" + BookmarkEntry.COLUMN_CEMETERY_ID + ") REFERENCES " +
                CemeteryEntry.TABLE_NAME + " (" + CemeteryEntry._ID + ")," +
                " FOREIGN KEY (" + BookmarkEntry.COLUMN_SECTION_ID + ") REFERENCES " +
                SectionEntry.TABLE_NAME + " (" + SectionEntry._ID + ")" +
                " FOREIGN KEY (" + BookmarkEntry.COLUMN_GRAVE_ID + ") REFERENCES " +
                GraveEntry.TABLE_NAME + " (" + GraveEntry._ID + ")" +
                " );";

        final String SQL_CREATE_PICTURE_TABLE = "CREATE TABLE " + PictureEntry.TABLE_NAME + " (" +
                PictureEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PictureEntry.COLUMN_CEMETERY_ID + " INTEGER NOT NULL, " +
                PictureEntry.COLUMN_SECTION_ID + " INTEGER, " +
                PictureEntry.COLUMN_GRAVE_ID + " INTEGER, " +
                PictureEntry.COLUMN_CATEGORY_NAME + " TEXT, " +
                PictureEntry.COLUMN_ATTRIBUTE_NAME + " TEXT, " +
                PictureEntry.COLUMN_SCOPE + " TEXT NOT NULL, " +
                PictureEntry.COLUMN_FILE_NAME + " TEXT UNIQUE NOT NULL, " +
                " FOREIGN KEY (" + PictureEntry.COLUMN_CEMETERY_ID + ") REFERENCES " +
                CemeteryEntry.TABLE_NAME + " (" + CemeteryEntry._ID + ")," +
                " FOREIGN KEY (" + PictureEntry.COLUMN_SECTION_ID + ") REFERENCES " +
                SectionEntry.TABLE_NAME + " (" + SectionEntry._ID + ")" +
                " FOREIGN KEY (" + PictureEntry.COLUMN_GRAVE_ID + ") REFERENCES " +
                GraveEntry.TABLE_NAME + " (" + GraveEntry._ID + ")" +
                " );";

        final String SQL_CREATE_CATEGORY_TABLE =  "CREATE TABLE " + SurveyCategoryEntry.TABLE_NAME + " (" +
                SurveyCategoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                // Location of category question
                SurveyCategoryEntry.COLUMN_SCOPE + " TEXT NOT NULL, " +
                SurveyCategoryEntry.COLUMN_TAB_NUMBER + " INTEGER NOT NULL, " +
                SurveyCategoryEntry.COLUMN_GROUP_NUMBER + " INTEGER NOT NULL, " +
                SurveyCategoryEntry.COLUMN_CATEGORY_NUMBER + " INTEGER NOT NULL, " +
                SurveyCategoryEntry.COLUMN_TAB_NAME + " TEXT NOT NULL, " +
                SurveyCategoryEntry.COLUMN_GROUP_NAME + " TEXT NOT NULL, " +
                // Question type and options
                SurveyCategoryEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                SurveyCategoryEntry.COLUMN_TITLE + " TEXT NOT NULL, " + // will be set to name if empty
                SurveyCategoryEntry.COLUMN_TYPE + " TEXT NOT NULL, " +
                SurveyCategoryEntry.COLUMN_PICTURE + " INTEGER NOT NULL, " +
                SurveyCategoryEntry.COLUMN_ATTRIBUTE_PICTURE + " INTEGER NOT NULL, " +
                SurveyCategoryEntry.COLUMN_REQUIRED + " INTEGER NOT NULL, " +
                SurveyCategoryEntry.COLUMN_THUMBNAILS_PATH + " TEXT NOT NULL " +
                " );";

        final String SQL_CREATE_ATTRIBUTE_TABLE =  "CREATE TABLE " + SurveyAttributeEntry.TABLE_NAME + " (" +
                SurveyAttributeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SurveyAttributeEntry.COLUMN_CATEGORY_ID + " INTEGER NOT NULL, " +
                SurveyAttributeEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                SurveyAttributeEntry.COLUMN_ORDER + " INTEGER NOT NULL, " +
                " FOREIGN KEY (" + SurveyAttributeEntry.COLUMN_CATEGORY_ID + ") REFERENCES " +
                SurveyCategoryEntry.TABLE_NAME + " (" + SurveyCategoryEntry._ID + ")" +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_CEMETERY_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SECTION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_GRAVE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_CEMETERY_ATTRIBUTES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SECTION_ATTRIBUTES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_GRAVE_ATTRIBUTES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_BOOKMARK_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_PICTURE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_CATEGORY_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_ATTRIBUTE_TABLE);

        // Create INDEX based on joins on table 'attribute_name' columns
        final String SQL_CREATE_INDEX_CEMETERY = "CREATE INDEX cemetery_index ON " +
                CemeteryAttributesEntry.TABLE_NAME + "(" +  CemeteryAttributesEntry.COLUMN_ATTRIBUTE_NAME + ")";
        final String SQL_CREATE_INDEX_SECTION = "CREATE INDEX section_index ON " +
                SectionAttributesEntry.TABLE_NAME + "(" +  SectionAttributesEntry.COLUMN_ATTRIBUTE_NAME + ")";
        final String SQL_CREATE_INDEX_GRAVE = "CREATE INDEX grave_index ON " +
                GraveAttributesEntry.TABLE_NAME + "(" +  GraveAttributesEntry.COLUMN_ATTRIBUTE_NAME + ")";
        final String SQL_CREATE_INDEX_SURVEY_ATTRIBUTES = "CREATE INDEX survey_attribute_index ON " +
                SurveyAttributeEntry.TABLE_NAME + "(" +  SurveyAttributeEntry.COLUMN_NAME + ")";

        sqLiteDatabase.execSQL(SQL_CREATE_INDEX_CEMETERY);
        sqLiteDatabase.execSQL(SQL_CREATE_INDEX_SECTION);
        sqLiteDatabase.execSQL(SQL_CREATE_INDEX_GRAVE);
        sqLiteDatabase.execSQL(SQL_CREATE_INDEX_SURVEY_ATTRIBUTES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CemeteryEntry.TABLE_NAME);
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SectionEntry.TABLE_NAME);
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GraveEntry.TABLE_NAME);
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BookmarkEntry.TABLE_NAME);
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PictureEntry.TABLE_NAME);
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SurveyCategoryEntry.TABLE_NAME);
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SurveyAttributeEntry.TABLE_NAME);
        // recreate the tables but not necessary as it does it automatically
        //onCreate(sqLiteDatabase);
    }
}
