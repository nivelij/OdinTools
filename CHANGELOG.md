# Changelog

## v1.3.2

### Fixes
- **App overrides:** opening an override for an app that can no longer be resolved (e.g. it was uninstalled) no longer crashes — the screen closes gracefully instead.
- **App overrides:** the "Add app override" picker no longer crashes when a single installed app has a broken icon or label; that app is simply skipped.
- **Charge limit:** turning the feature off now immediately ends any active charging separation, instead of leaving the battery bypassed until the next battery event.
- **Charge limit:** a misconfigured range where the lower bound is greater than or equal to the upper bound is now ignored, instead of flipping charging separation on every battery update.
- Fixed an off-by-one in the controller / L2·R2 style selection dialog's "minimum selected" guard.

### Features
- **Charge limit:** a silent notification now appears when charging is automatically paused (limit reached) or resumed, so this background behaviour is visible. Tapping it opens OdinTools.

### Internal
- Release builds are now signed with a stable release key, so future releases can be updated in place.
- Added a debug-only fake device layer so the app can run on a standard Android emulator; on real Odin 2 hardware it uses the real implementation unchanged.
- Added unit tests (app-override mapper, charge-limit decision logic, settings) and wired them into CI.
- The build workflow can now be triggered manually (`workflow_dispatch`).
