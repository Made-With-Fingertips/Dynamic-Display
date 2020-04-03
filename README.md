# S20 Refresh Rate

---

This is an application that allows you to change your refresh rate between 60Hz and 120Hz based on the currently running application.

#### This application is designed to work only on Samsung Galaxy S20, S20+, and S20 Ultra phones.

## Installation

* Download and install the latest release

* Download adb

    * [Windows](https://dl.google.com/android/repository/platform-tools-latest-windows.zip)
    * [Mac](https://dl.google.com/android/repository/platform-tools-latest-darwin.zip)
    * [Linux](https://dl.google.com/android/repository/platform-tools-latest-linux.zip)

* Run the following to grant `WRITE_SECURE_SETTINGS` permission

`adb shell pm grant dev.fingertips.s20refreshrate android.permission.WRITE_SECURE_SETTINGS`

### TODO

* Quick settings toggle
* Investigate changing screen resolution