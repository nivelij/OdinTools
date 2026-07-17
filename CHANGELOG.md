# Changelog

## v1.3.4

### Features
- **Thumbstick LED:** new *Lighting* section to control the RGB rings around the thumbsticks (Odin 2). Turn them on/off, set the same color for both sticks or a different color per stick, pick from a color wheel or 10 presets, and adjust brightness. Backed by the device's own `joystick_light_enabled` / `joystick_led_light_picker_color` / `led_light_brightness_percent` settings, written per-stick.

## v1.3.3

### Fixes
- **App overrides:** the master toggle now reflects its saved state after relaunch. Previously the switch always showed "on" again on restart (the stored value and the actual behaviour were correct — only the on-screen switch was wrong).
- **Device version:** now shown on the Odin 2 Portal, which doesn't populate `ro.build.odin2.ota.version` — it falls back to the FOTA version property (e.g. "1.0.0.338"). The original Odin 2 is unaffected.

### Other
- New app icon (white wing on black).

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
