TravelBuddy structure:

'ASTravelBuddy' - Root directory of the Android Studio project
  'build.gradle.kts' - Configuration file
    'app' - Resources and the code of the application
       'google-services.json' - Connects the application to the author's Firebase account
	  'src'
	     'androidTest' - Instrumented tests of the application
	     'main'
	        'AndroidManifest' - Manifest file of the application
		'java' - Kotlin (or Java) source code of the application
		  'chm9360.travelbuddy' - Package directory
		     'model' - Data classes
		     'ui' - Subdirectories of screens and ViewModels
		     'utils' - Utiliy files
		     'MainActivity.kt' - Entry point of the application
		     'MyApplication.kt' - Entry point for Firebase
		 'res' - Resources of the application
		   'drawable' - Images, XML files
		   'values' - XML for colors, strings
		 'ic_launcher-playstore.png' - Application's app


Launching the project in Android Strudio:
Project needs to be opened as a project using Android Studio, then Run option launches the app (emulator set up might be required).

Launching the project on an Android phone:
1. Build APK bundle using Android Studio.
2. Upload the file to the phone (using cable or download it from the cloud drive).
3. Open the APK file on the Android device and click on 'Install' button.
4. Open the app.