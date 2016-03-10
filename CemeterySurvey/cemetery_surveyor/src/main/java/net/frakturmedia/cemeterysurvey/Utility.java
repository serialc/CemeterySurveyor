package net.frakturmedia.cemeterysurvey;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cyrille on 28/01/16.
 */
public abstract class Utility {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static class dataPaths {
        public static final String BASE = Environment.getExternalStorageDirectory() + "/cemetery_survey_application";

        public static final String TEMPLATE = BASE + "/template";
        public static final String TEMPLATE_ARCHIVE = TEMPLATE + "/archive";
        public static final String TEMPLATE_FILE = TEMPLATE + "/survey_template.json";

        public static final String THUMBNAILS = BASE + "/thumbnails";
        public static final String THUMBNAILS_NOMEDIA = THUMBNAILS + "/.nomedia";
        public static final String THUMBNAILS_SMALL = BASE + "/thumbnails_small";
        public static final String THUMBNAILS_SMALL_NOMEDIA = THUMBNAILS_SMALL + "/.nomedia";

        public static final String EXPORT = BASE + "/export";
        public static final String PICTURES = EXPORT + "/pictures";
        public static final String EXPORT_NOMEDIA = EXPORT + "/.nomedia";
        public static final String DATA_EXPORT = EXPORT + "/data";
    }

    public static class surveyDataTypes {
        public static final String SET = "set";
        public static final String THUMBNAIL = "set_thumbnail";
        public static final String RADIO = "radio";
        public static final String BINARY = "binary";
        public static final String TEXT = "text";
        public static final String MEASUREMENT = "measurement";
        public static final String[] typeList = new String[]{
            SET, THUMBNAIL, RADIO, BINARY, TEXT, MEASUREMENT};
    }

    public static class resultCodes {
        public static final int GRAVE_ID_RESULT_CODE = 54253; // GRAVE
        public static final String GRAVE_IDENTIFIER = "grave_identifier";

        public static final int REQUEST_IMAGE_CAPTURE = 9167; // PICT
    }

    public static class colNamesPrefix {
        public static final String DATA_COL_PREFIX = "d_";
    }

    public static class pictures {
        public static final String CATEGORY_NAME = "category_name";
        public static final String ATTRIBUTE_NAME = "attribute_name";
        public static final String TEMPORARY_SAVE_FILE_PATH = dataPaths.PICTURES + "/new_picture_temporary.jpg";

        public static File createImageFile() throws IOException {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp;

            // Specify the location
            File storageDir = new File(Utility.dataPaths.PICTURES);

            //Log.d(LOG_TAG, storageDir.toString());

            // Create the image file
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            // Check this isn't null otherwise taking a picture will fail
            if (image.getName() == null) {
                return null;
            }

            // Save a file: path for use with ACTION_VIEW intents
            //mCurrentPhotoPath = "file:" + image.getAbsolutePath();
            return image;
        }
    }

    // Called on boot or when the template is modified by user in appliaction
    public static String backupTemplateFile() {
        File JsonTemplate = new File(Utility.dataPaths.TEMPLATE_FILE);

        if (!JsonTemplate.exists()) {
            return "WARNING - No JSON survey template file was found!";
        }

        // File exists, copy it
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        try {
            InputStream in = new FileInputStream(Utility.dataPaths.TEMPLATE_FILE);
            OutputStream out = new FileOutputStream(Utility.dataPaths.TEMPLATE_ARCHIVE + "/survey_template_" + timeStamp + ".json");

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

        } catch (java.io.IOException ex) {
            Log.e(LOG_TAG, "Backing up of JSON survey template file failed.");
            Log.e(LOG_TAG, ex.toString(), ex);
            return "WARNING - Backing up of JSON survey template file failed.";
        }

        // all good
        return null;
    }
}
