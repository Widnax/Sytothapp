# Implementation Plan - Add Back Button and Center Title in Chart Screen

This plan details the changes required to add a back button to the `ChartScreen`, center its title, and ensure it correctly handles navigation back to the dashboard, matching the logic and style of the `LoggingScreen`.

## Proposed Changes

### [Component: UI]

#### [MODIFY] [ChartScreen.kt](file:///C:/Users/gabri/AndroidStudioProjects/Sytothapp/app/src/main/java/com/example/sytothapp/ui/chart/ChartScreen.kt)
- Update the `ChartScreen` composable signature to accept an `onNavigateBack` callback.
- Import `Icons.AutoMirrored.Rounded.ArrowBack`.
- Replace `TopAppBar` with `CenterAlignedTopAppBar`.
- Add the navigation icon (back arrow) that triggers `onNavigateBack`.
- Set the title to "Chart" and ensure it's centered (handled by `CenterAlignedTopAppBar`).

#### [MODIFY] [MainActivity.kt](file:///C:/Users/gabri/AndroidStudioProjects/Sytothapp/app/src/main/java/com/example/sytothapp/MainActivity.kt)
- Pass the `onNavigateBack` lambda to the `ChartScreen` call within the `NavHost` equivalent (Adaptive Scaffold detail pane).

## Verification Plan

### Automated Tests
- Run existing unit tests to ensure no regressions in fertility logic.
- (Optional) Add a basic UI test to verify the presence of the back button on the Chart Screen.

### Manual Verification
1. Open the app and navigate to the Chart Screen.
2. Verify that the title "Chart" is centered.
3. Verify that the back button (arrow) is visible.
4. Click the back button and verify it returns to the Dashboard.
5. Verify that the system back button also works (already implemented in `MainActivity`).
