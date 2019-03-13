
# CemeterySurveyor
A flexible cemetery research surveying tool.

**Cemetery Surveyor is designed to be part of a survey workflow consisting of**:
* the creation of an ontology,
* the designing of a survey consisting of categories and attributes defined in a JSON template,
* the designating of cemetery, cemetery sections and graves in a GIS with unique identifiers,
* the actual surveying of the cemetery using the application,
* the exporting of gathered data back into a GIS or statistical analysis program.

The application is useless without the appropriate [documentation](https://raw.githubusercontent.com/serialc/CemeterySurveyor/master/Documentation/documentation.pdf).
The documentation is currently located on GitHub with the source code: https://github.com/serialc/CemeterySurveyor

This application was funded by the University of Luxembourg (http://wwwen.uni.lu/).

See the [Project Page](https://transmortality.uni.lu/Survey-Tool) for this application as well as the [Web-based version of CemeterySurveyor](https://github.com/serialc/WebCemeterySurveyor).

[WebCemeterySurveyor](https://github.com/serialc/WebCemeterySurveyor) (WCS) is a collaborative web-based version of the CemeterySurveyor Application (CSA). WCS has some differences to CSA.

CSA advantages:
* No internet connection required
* Association of pictures to features is easy

CSA disadvantages compared to WCS:
* Customization of JSON survey template must be created in a text-editor (or in WCS).

## Deployment tutorial
So you wish to use CSA. I strongly recommend the use of a tablet for this application. A phone will only be adequate for a simpler survey.

Here are a few steps to it working on your device:

 1. Install the application from the [Google Play Store](https://play.google.com/store/apps/details?id=net.frakturmedia.cemeterysurvey).

 2. Upload a JSON survey template to your device. [Here's a template](https://github.com/serialc/WebCemeterySurveyor/Resources) that you can use as is or as a foundation for your own. The [documentation](https://raw.githubusercontent.com/serialc/CemeterySurveyor/master/Documentation/documentation.pdf) details the syntax to create your survey template.
	 1. To upload this to your device and the correct location, either download it to your device or connect your device to a computer. Move the JSON template to the directory path **cemetery_survey_application/template/survey_template.json** on the root level of the device storage.
	 2. If you **Reload JSON template** from within CSA menu in the top right you should no longer see the **"Missing JSON survey template file! See Help menu option."** message.
	 You will however likely see the message **"Template loading failed: The thumbnail folder named XXXXX was not found."**. The JSON template provided uses thumbnails rather than text for some survey questions responses. CSA can't find those thumbnails as we haven't uploaded them yet. We'll fix that next.
	 
 3. Upload the properly formatted/named thumbnails to your device. [Here are the thumbnails](https://github.com/serialc/WebCemeterySurveyor/Resources) used by the JSON template used above.
	 1. Unzip the thumbnails.
	 2. As was done for the JSON template, upload the unzipped **thumbnail** folder to your device into the directory **cemetery_survey_application/**. This will replace the existing thumbnail directory.
	 3. As was done for the JSON template, **Reload JSON template**. This may take a little longer (5-10 seconds) as the thumbnails will be resized.
 
 4. Test the application. Everything should be working normally.

