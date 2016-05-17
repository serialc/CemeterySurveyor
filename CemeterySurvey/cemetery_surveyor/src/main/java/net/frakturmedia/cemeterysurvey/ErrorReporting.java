package net.frakturmedia.cemeterysurvey;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

@ReportsCrashes(
        formUri = "http://frakturmedia.net/oswp/android_crash_reporting/submit.php",
        httpMethod = HttpSender.Method.POST,
        customReportContent = {
                ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PACKAGE_NAME,
                ReportField.REPORT_ID,
                ReportField.BUILD,
                ReportField.STACK_TRACE
        },
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.acra_message
)
public class ErrorReporting extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }

    // To disable simply add/uncommnet as necessary the following lines from AndroidManifest.xml:
    // <uses-permission android:name="android.permission.INTERNET" />
    // android:name=".ErrorReporting"
    // <service android:name="org.acra.sender.SenderService" ... />
    // Also the <activity android:name=".ErrorReporting" .... ?>

    // In build.gradle (Module), uncomment:
    //    compile fileTree(dir: 'libs', include: ['*.jar'])
    //    compile files('libs/acra-4.8.2.jar')
}
