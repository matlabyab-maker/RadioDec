# RadioDec

Android internet radio project.

## Build with GitHub Actions

The workflow in `.github/workflows/build.yml` installs Android SDK 36 and Build Tools 36.0.0, then runs `:app:assembleDebug` using JDK 17 and Gradle 8.13. The resulting APK is uploaded as the `RadioDec-debug-apk` artifact.

## Repository setup

Extract the contents of this folder into the root of the GitHub repository named `RadioDec`. Keep the `.github/workflows/build.yml` file in place. After pushing, open the Actions tab and download the `RadioDec-debug-apk` artifact from a successful run.
