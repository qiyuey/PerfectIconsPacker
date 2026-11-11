# Repository Guide

## Hard rules

1. **Never execute shell scripts from the reference `Perfect-Icons-APP` project.** They depend on injected variables and can contain destructive commands.
2. Treat `Perfect-Icons-Completion-Project` as a read-only upstream resource repository. Do not modify, commit, or run scripts from it.
3. Do not commit `tmp/` or any reference theme archives.
4. Only target HyperOS 4 theme structure. Do not add MIUI / EMUI / HarmonyOS compatibility.
5. Use Kotlin for application logic. Shell is allowed only where the platform requires it.
6. Commit messages must not contain Co-Authored-By trailers.

## Layout

```text
app/                 Android application (single module, layered packages)
app/src/main/java/io/github/perfecticons/
  core/              models, pure utilities
  data/              repository fetch, icon-style preferences, root
  domain/            upstream assembly, deployable archives
  ui/                Compose + Miuix screens, theme, navigation, viewmodels
docs/                design and format documentation
tmp/                 local reference files (ignored)
```

## Build

```bash
./gradlew :app:assembleDebug
```

## Device

Prefer the device designated by the user for the current session or by
`ANDROID_SERIAL`. If neither is set, use the read-only `adb devices -l` inventory:
when exactly one device is listed, select its serial automatically without asking;
when multiple devices are listed (including offline or unauthorized entries), ask
the user to choose. If the sole device is unauthorized, keep that serial selected
and ask the user to approve USB debugging. Pass the selected serial explicitly
with `-s` on every subsequent device command.

```bash
adb -s "$ANDROID_SERIAL" install -r app/build/outputs/apk/debug/app-debug.apk
adb -s "$ANDROID_SERIAL" shell am start -n io.github.perfecticons/io.github.perfecticons.MainActivity
```

Root installation and anything that reboots or modifies system state must be tested
only with explicit user approval.

Use `adb devices -l` only for read-only device selection. Do not use `adb -e` or
send operations to multiple devices. If the selected serial disconnects or fails,
stop and report; do not fall back to another device.

## Documentation

Read `docs/ARCHITECTURE.md` first. Update the relevant doc when changing architecture, output format, or customization rules.
