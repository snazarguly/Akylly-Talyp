# Bundle Qwen 3 (`qwen3.task`) Directly Inside App Assets

Place `qwen3.task` back into app assets (`app/src/main/assets/models/qwen3.task`) so that the model is bundled directly inside the app, and update `LlamaCppEngine.kt` to copy it efficiently in chunks from assets to internal storage (`filesDir/models/qwen3.task`) on first launch with progress handling.

## User Review Required

> [!WARNING]
> - **APK Size & Storage:** Bundling a 2.1 GB model inside APK assets will result in a large APK (>2.1 GB). When installing on emulators or physical devices with limited storage, ensure the device has sufficient storage space.

## Proposed Changes

### Assets & Inference Component

#### [NEW/RESTORE] [qwen3.task](file:///C:/remix-akylly-talyp/app/src/main/assets/models/qwen3.task)
- Restore `qwen3.task` in `app/src/main/assets/models/`.

#### [MODIFY] [LlamaCppEngine.kt](file:///C:/remix-akylly-talyp/app/src/main/java/com/example/features/offline_ai/inference/LlamaCppEngine.kt)
- Update `initializeFromAssets` to stream `qwen3.task` from assets to `filesDir/models/qwen3.task` using optimized buffer chunking (e.g. 8KB-64KB buffers) to prevent memory issues during initial copy.

## Verification Plan

### Automated Tests
- Gradle build (`app:assembleDebug`).

### Manual Verification
- Deploy and verify model extraction from assets to internal storage on startup.
