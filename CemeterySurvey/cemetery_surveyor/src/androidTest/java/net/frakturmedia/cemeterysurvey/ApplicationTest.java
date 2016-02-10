package net.frakturmedia.cemeterysurvey;

import android.app.Application;
import android.test.ApplicationTestCase;

import java.io.File;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);

        // Delete all the pictures
        deleteDir(new File(Utility.dataPaths.BASE));
    }

    private void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        // rename then delete - can prevent resource lock
        final File to = new File(file.getAbsolutePath() + System.currentTimeMillis());
        file.renameTo(to);
        to.delete();
    }
}