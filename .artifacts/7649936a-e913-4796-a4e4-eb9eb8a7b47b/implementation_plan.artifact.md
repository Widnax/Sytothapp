# Implementation Plan - Settings Screen & Localization

Add a Settings screen to manage application preferences, including language support (French), temperature units, and theme selection.

## User Review Required

> [!IMPORTANT]
> The app language switching will use `AppCompatDelegate.setApplicationLocales()`, which is the standard way to handle per-app language preferences on Android 13+. For older versions, it might require a manual activity recreation or custom context wrapper, but `AppCompatDelegate` handles much of this internally now.

## Proposed Changes

### Data & Persistence

#### [NEW] [SettingsRepository.kt](file:///C:/Users/gabri/AndroidStudioProjects/Sytothapp/app/src/main/java/com/example/sytothapp/data/repository/SettingsRepository.kt)
Create a repository to handle `DataStore` preferences for:
- Temperature Unit (Celsius/Fahrenheit)
- Theme Mode (System, Light, Dark)

### UI & Navigation

#### [MODIFY] [SytothRoute.kt](file:///C:/Users/gabri/AndroidStudioProjects/Sytothapp/app/src/main/java/com/example/sytothapp/ui/navigation/SytothRoute.kt)
Add `Settings` route.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/gabri/AndroidStudioProjects/Sytothapp/app/src/main/java/com/example/sytothapp/MainActivity.kt)
- Register `Settings` route in the `SytothApp` composable.
- Handle navigation to `SettingsScreen`.
- Observe Theme preference to apply it globally.

#### [MODIFY] [DashboardScreen.kt](file:///C:/Users/gabri/AndroidStudioProjects/Sytothapp/app/src/main/java/com/example/sytothapp/ui/dashboard/DashboardScreen.kt)
Add a Settings icon button to the `TopAppBar`.

#### [NEW] [SettingsScreen.kt](file:///C:/Users/gabri/AndroidStudioProjects/Sytothapp/app/src/main/java/com/example/sytothapp/ui/settings/SettingsScreen.kt)
Implement the UI for settings:
- Language picker (English/French).
- Temperature unit toggle.
- Theme selection radio buttons.

#### [NEW] [SettingsViewModel.kt](file:///C:/Users/gabri/AndroidStudioProjects/Sytothapp/app/src/main/java/com/example/sytothapp/ui/settings/SettingsViewModel.kt)
ViewModel to bridge `SettingsRepository` and `SettingsScreen`.

### Localization

#### [MODIFY] [strings.xml](file:///C:/Users/gabri/AndroidStudioProjects/Sytothapp/app/src/main/res/values/strings.xml)
Add English strings for settings.

#### [NEW] [strings.xml (fr)](file:///C:/Users/gabri/AndroidStudioProjects/Sytothapp/app/src/main/res/values-fr/strings.xml)
Add French translations.

## Verification Plan

### Automated Tests
- N/A for this task (UI focused).

### Manual Verification
1. Open the app, navigate to Settings from Dashboard.
2. Change Language to French -> Verify UI updates to French.
3. Change Temperature Unit -> Verify preference is saved (re-open app).
4. Change Theme -> Verify app theme changes immediately.
