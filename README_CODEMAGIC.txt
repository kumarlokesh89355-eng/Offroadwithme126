OFFROAD WITH ME - CODEMAGIC READY PACKAGE

This package is the consolidated Offroad With Me V125 Android project.

Codemagic:
- Repository root must contain this project directly.
- codemagic.yaml is already included at the root.
- Workflow: android-native-debug
- Output: app/build/outputs/**/app-debug.apk

Do not upload only this ZIP inside GitHub and expect Codemagic to build it.
Upload the extracted project files/folders so gradlew, settings.gradle, build.gradle,
app/, gradle/, and codemagic.yaml are directly in the repository root.
