@echo off
echo Packaging UniMate Android Studio project into UniMate_Android_Project.zip...
powershell -Command "Compress-Archive -Path 'app', 'gradle', 'build.gradle.kts', 'settings.gradle.kts', 'gradle.properties', 'gradlew', 'gradlew.bat', 'local.properties', '.gitignore', 'README.md' -DestinationPath 'UniMate_Android_Project.zip' -Force"
echo Done! Created UniMate_Android_Project.zip successfully.
pause
