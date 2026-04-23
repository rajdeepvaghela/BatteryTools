# BatteryTools [![Release](https://img.shields.io/github/v/release/rajdeepvaghela/BatteryTools)](https://github.com/rajdeepvaghela/BatteryTools/releases)

BatteryTools is a security-focused Android application designed to prevent device theft by triggering a loud alarm and notifying emergency contacts the moment the device is unplugged from a power source. It features a seamless integration between the smartphone and Wear OS devices to ensure the user is alerted regardless of where their phone is.

<table>
  <tr>
    <td rowspan="2" width="50%">
      <img src="/screenshots/Battery_Tools.jpg" alt="App">
    </td>
    <td width="50%">
      Wear OS
      <img src="/screenshots/WearOS.png" alt="Watch">
    </td>
  </tr>
  <tr>
    <td>
      Homescreen Widget
      <img src="/screenshots/Widget_Home.png" alt="Widget">
    </td>
  </tr>
</table>

## 🚀 Key Features

- **Anti-Theft Alarm**: Triggers a high-volume alarm sound immediately when the charging cable is disconnected.
- **Emergency Notifications**: 
  - **Auto-SMS**: Sends a predefined alert message to a specified phone number.
  - **Auto-Call**: Initiates an emergency call to a trusted contact.
- **Wear OS Integration**: 
  - Syncs battery statistics to the wearable device.
  - Triggers alert notifications on the watch when the phone is unplugged.
- **Battery Stats Widget**: A modern Home Screen widget powered by Android Glance for quick access to battery health and status.
- **Critical Alert System**: Utilizes full-screen intents and DND (Do Not Disturb) bypass to ensure the alarm is seen and heard.

## 🛠 Technical Stack

- **Language**: Kotlin
- **Architecture**: Multi-module project (`app`, `wearable`, `common`)
- **Jetpack Components**: 
  - `LifecycleService` for robust background monitoring.
  - `Glance` for the home screen widget.
  - `DataStore` for persistent settings management.
- **Connectivity**: Google Play Services (GMS) Wearable API for phone-to-watch communication.
- **System Integration**: `BatteryManager` for real-time power state tracking.

## 📂 Project Structure

- `app`: Main smartphone application containing the alert service, UI, and widget logic.
- `wearable`: Wear OS module for handling remote notifications and battery data display.
- `common`: Shared data models and utilities used by both the mobile and wearable modules.

## ⚙️ Configuration

Users can configure the following settings within the app:
- **SmsOnAlert**: Enable/Disable automatic SMS alerts.
- **SmsNumber**: The destination phone number for SMS alerts.
- **CallOnAlert**: Enable/Disable automatic emergency calls.
- **CallNumber**: The destination phone number for emergency calls.

## 🛡 Security & Permissions

To function correctly, BatteryTools requires the following permissions:
- `RECEIVE_BOOT_COMPLETED`: To restart monitoring after a device reboot.
- `SEND_SMS`: To send emergency alerts.
- `CALL_PHONE`: To initiate emergency calls.
- `FOREGROUND_SERVICE`: To maintain monitoring in the background.
- `VIBRATE`: For haptic feedback during alerts.
