# Installing Day Counter on Android

Written for the **Samsung Galaxy A36 5G** (Android 15, One UI 7). The same steps work on any phone running Android 8.0 or later; menu names can differ slightly on other brands.

The app is not on Google Play. You install it straight from this repository's GitHub Releases page. It takes about two minutes.

## 1. Download

On the phone, open this link in Chrome or Samsung Internet, or scan the QR code with the camera:

**https://github.com/okdaithi/app-counter/releases/latest/download/DayCounter.apk**

<img src="docs/img/download-qr.png" alt="QR code for the DayCounter.apk download" width="200">

The link always points to the newest release. If the browser warns *"File might be harmful"*, tap **Download anyway**.

## 2. Install

1. When the download finishes, tap **Open**. You can also open the file later from **My Files → Downloads → DayCounter.apk**.
2. If Android says *"For your security, your phone is not allowed to install unknown apps from this source"*, tap **Settings**, turn on **Allow from this source**, then press Back.
3. Tap **Install**.
4. If **Google Play Protect** shows *"Unsafe app blocked"* or offers to scan the app, tap **More details → Install anyway**. Play Protect warns about every app that doesn't come from Google Play.
5. Tap **Open**, or find **Counter** (hourglass icon) in the app drawer.

**If Samsung blocks the install with "Auto Blocker":** go to **Settings → Security and privacy → Auto Blocker**, turn it off, install the app, then turn Auto Blocker back on. The installed app keeps working.

## 3. Create a counter

1. Open **Counter** and tap **+**.
2. Enter a title, pick a date, and choose the unit (days, weeks, months or years). Optionally turn on **Include today** or **Repeat yearly** (for birthdays and anniversaries).
3. Tap **Save**.

## 4. Add the widget to the home screen

1. Touch and hold an empty spot on the home screen and tap **Widgets**.
2. Search for or scroll to **Counter** (subtitle *"1×1 · Days since or until a date"*), tap it, then tap **Add**. You can also drag it onto the screen.
3. The **Choose a counter** sheet opens. Tap the counter the widget should show.

Tapping the widget opens that counter's editor. To show a different counter, touch and hold the widget and tap **Settings** (the gear icon). You can place as many widgets as you like, including several for the same counter.

## 5. Keep the widget up to date (recommended on Samsung)

The number updates just after midnight. Samsung's battery management can put rarely opened apps to sleep, which can delay that update. To prevent this:

- **Settings → Apps → Counter → Battery → Unrestricted**
- If **Settings → Battery → Background usage limits** lists Counter under *Sleeping apps* or *Deep sleeping apps*, remove it.

## Updating

Download and install the APK again from the same link. It installs over the old version and keeps your counters.

To get updates automatically, install [Obtainium](https://github.com/ImranR98/Obtainium) and add `https://github.com/okdaithi/app-counter` as an app source.

## Uninstalling

**Settings → Apps → Counter → Uninstall.** This deletes your counters. Widgets disappear with the app.

## Troubleshooting

| Problem | Fix |
| --- | --- |
| *"App not installed as package conflicts with an existing package"* | A build signed with a different key is already installed, for example a test APK from the Actions tab. Uninstall Counter, then install the release APK. |
| *"There was a problem parsing the package"* | The download is incomplete. Delete it and download again. |
| Widget shows a dash (–) | Its counter was deleted. Tap it to open the app, or touch and hold the widget → **Settings** to pick another counter. |
| Widget number is a day behind | Open the app once, then apply the battery settings in step 5. |
| Counter isn't listed under Widgets | Restart the phone once after installing, or touch and hold the home screen again. Some launchers refresh the widget list lazily. |

Check the download's integrity by comparing it with `DayCounter.apk.sha256`, which is attached to each release.

---

## Maintainer: publishing a release

### One-time signing setup

Every release must be signed with the same key, otherwise phones refuse to install it as an update. The key lives only in GitHub Actions secrets; this public repository never contains it.

1. Create a keystore (skip this if you already have one):
   ```
   keytool -genkeypair -v -storetype PKCS12 -keystore release.jks -alias daycounter \
     -keyalg RSA -keysize 4096 -validity 10000 -dname "CN=Day Counter"
   base64 -w0 release.jks > release.jks.b64
   ```
2. Open **Settings → Secrets and variables → Actions → New repository secret** and add:

   | Secret | Value |
   | --- | --- |
   | `DAYCOUNTER_KEYSTORE_BASE64` | contents of `release.jks.b64` |
   | `DAYCOUNTER_KEYSTORE_PASSWORD` | keystore password |
   | `DAYCOUNTER_KEY_ALIAS` | `daycounter` |
   | `DAYCOUNTER_KEY_PASSWORD` | key password (the same as the keystore password for PKCS12) |

3. Keep a private backup of `release.jks` and its password.

### Publishing

**Actions → Release → Run workflow**, then enter a version such as `0.1.0`. Pushing a tag like `v0.1.0` does the same thing.

The workflow:
- runs the unit tests and builds a minified release APK signed with the key
- checks the signature and 16 KB page alignment
- creates the GitHub Release with `DayCounter.apk` attached

The download link and QR code above then serve the new version. The build number increases with every run, so each release installs as an update.

### Test builds

Every push runs the **Android** workflow. Its `app-debug-apk` artifact is a debug build signed with a throwaway key. It is only for testing: uninstall it before you install a release.
