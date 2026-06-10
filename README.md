# TourScan

Android app that identifies Romanian tourist landmarks from photos, entirely on-device. Point your camera at a castle, waterfall, or salt mine and get back its name, history, and visitor info in under 100ms.

Currently recognizes 9 landmarks: Castelul Bran, Castelul Corvinilor, Castelul Peleș, Cascada Bigăr, Delta Dunării, Palatul Parlamentului, Salina Turda, Transfăgărășan, plus an "altele" (other) class that rejects anything the models don't recognize.

## How it works

The app runs two TFLite models (MobileNetV2 and EfficientNet-B0, both INT8 quantized) and lets you switch between them on the fly. The classification pipeline goes like this:

1. Image gets letterbox-resized to 224x224, preserving aspect ratio with black padding
2. Model outputs raw logits (no preprocessing normalization, it's baked into the model)
3. Temperature scaling (T ~ 0.40) calibrates the probabilities
4. Three rejection checks run in order: is the top class "other"? Is the confidence below 50%? Is the gap between top-two predictions under 15%? If any check trips, the image is rejected rather than misclassified

No server needed. Everything runs locally.


## Language support

Romanian and English, switchable at runtime without restarting the app. The choice sticks across sessions (SharedPreferences). Both UI strings and landmark data have separate versions per language.

Each landmark has two JSON files: `landmark_data/corvinilor.json` (EN) and `landmark_data_ro/corvinilor.json` (RO), with name, location, GPS coordinates, a Maps link, quick facts, and a set of info cards covering history, architecture, legends, and visitor tips.

## Database encryption

The Room database is encrypted with SQLCipher. On first launch, a random 48-character passphrase is generated, encrypted via an AES key from Android Keystore, and the ciphertext stored in SharedPreferences. The passphrase itself never touches disk in the clear.

If you're enabling encryption on a device that already has an unencrypted database, clear app data first. SQLCipher will refuse to open a plaintext .db file.

## About the models

Both were trained using progressive transfer learning in three phases (frozen base, partial fine-tuning, full fine-tuning) on ~2,500 images scraped and manually cleaned. Training used MixUp, CutMix, Random Erasing, and label smoothing.

| Model | Test accuracy | Size (INT8) | Inference |
|-------|--------------|-------------|-----------|
| MobileNetV2 | 90.8% | 3.4 MB | ~80ms |
| EfficientNet-B0 | 97.4% | 5.5 MB | ~95ms |

EfficientNet-B0 is better across the board, especially on the harder classes (Delta Dunarii goes from F1 0.80 to 0.98, Palatul Parlamentului from 0.86 to 1.00). MobileNetV2 is a bit faster and smaller, which is why both are kept as options.

Inference times measured on a Huawei P30 Pro (Android 10).

## Building

1. Clone the repo
2. Open in Android Studio (Hedgehog or newer)
3. Add your Supabase URL and anon key in `SupabaseClient.kt`
4. Build and run on a physical device (camera features won't work on emulator)

Models are bundled in `assets/`, no extra downloads needed.
