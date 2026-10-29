# Android Agent UI Verification Lab — Case Study 001

An exploratory comparison of how an AI coding agent builds and then verifies an Android UI, with and without the Android CLI. The subject app, **HomeStock**, is a small Jetpack Compose household-inventory screen.

> **This is not a statistically controlled performance study.** Each experiment is a single run on one machine with one agent model (`claude-sonnet-5-5`), with no repetitions, no control for caching or system state, and no blinding. Treat the results as case-study observations, not as measurements of tool or model performance.

## Contents

| Path | Description |
|---|---|
| `app/` | The **runnable** HomeStock Compose app, with the Gradle configuration. The Kotlin code and Gradle files are identical to commit `1f95f80` (Test B). It uses an **original storage-box launcher icon**, because the original icons are not redistributed (see Licenses). |
| `experiments/` | **Archival** source snapshots of Test A (`77fe81a`) and Test B (`1f95f80`). Not runnable and not byte-identical to their commits: each omits 14 launcher icon files, documented with checksums in `OMITTED_ASSETS.md`. |
| `LICENSE` | Apache License 2.0, for source code. |
| `LICENSE-docs` | Creative Commons Attribution 4.0 International, for documentation, prompts, findings and results. |
| `ANDROID_CLI_BENCHMARK_FINDINGS.md` | The **original** findings report for B2, kept unchanged. |
| `prompts/code-generation.md` | Verbatim Test A and Test B prompts, with provenance and session records. |
| `prompts/ui-verification.md` | Verbatim A2 and B2 prompts, kept separate for the interrupted attempts, the completed run, the autonomous attempt and the user-guided retries. |
| `results/comparison.md` | Comparison of the phases, the git-state check, and contradictions between sources. |
| `results/artifacts/` | Screenshots, renders and component-tree JSON, with checksums in [`results/artifacts/PROVENANCE.md`](results/artifacts/PROVENANCE.md). |

## Experiments

All sessions used Claude Code. In every session, each recorded agent turn used `claude-sonnet-5-5`. The usage records also list a few small `claude-haiku-5-5` calls (about 1.5k input and under 30 output tokens per session) whose purpose is not recorded. The token tables below are the Sonnet figures, and the cost figures are session totals (see `prompts/code-generation.md`).

| Name | What | Android CLI | Project / branch |
|---|---|---|---|
| Test A | Generate the inventory screen | Forbidden | `experiment/claude-only` (`77fe81a`) |
| Test B | Generate the inventory screen | Allowed | `experiment/android-cli` (`1f95f80`) |
| Test A2 | Verify the UI by rendering the previews | Forbidden | `experiment/preview-no-cli` |
| Test B2 | Verify the UI by rendering the previews | Allowed | `experiment/preview-with-cli` |

The A2 and B2 worktrees were both created from commit `1f95f80`, before their sessions began, so **both reviewed Experiment B's generated code**, not Test A's. B2's branch later gained commit `b62bd15`, which adds only the findings report. A2 has three sessions: two were interrupted by the user (no images produced) and one completed. They are documented separately.

### Autonomous behavior versus user-guided retries

- **Autonomous:** Test A, Test B, the A2 completed run, and the B2 original attempt.
- **User-guided:** B2's two retries. After the autonomous B2 attempt failed, the user supplied the exact `android preview list` and `android preview render` commands, then asked for `--component-tree`. Results from these stages reflect that guidance and do not show that the agent would have found the commands itself.

## The app

`InventoryScreen.kt` shows products grouped into six categories, with −/+ quantity buttons, an out-of-stock state and an "Add product" dialog. It defines `InventoryPhonePreview` (411x891 dp) and `InventoryTabletPreview` (1280x800 dp).

## Setup

### Environment used

Everything below was observed on the machine used for the experiments and rechecked during the pre-release audit (builds and renders rerun in scratch copies on 2026-10-29). "Not tested" marks anything that was not exercised.

| Component | Version / value | Notes |
|---|---|---|
| Operating system | macOS 15.6.1 (build 24G90), Apple silicon (arm64), Darwin 24.6.0 | Other platforms not tested. |
| Claude Code | 2.1.312 | Recorded in every transcript. Effort `medium` in all six sessions. Permission mode varied (see below). |
| Agent model | `claude-sonnet-5-5` | See "Experiments". |
| Gradle | 9.6.0 (wrapper, `distributionSha256Sum` present) | Gradle's embedded Kotlin is 2.3.21. The project's Kotlin plugin is 2.2.10. |
| JDK | Java 25 | `gradle/gradle-daemon-jvm.properties` sets `toolchainVersion=25`. The build ran on Android Studio's bundled JDK (JetBrains build of OpenJDK 25.0.3, first on `PATH`). A JDK 25 archive also sits in `~/.gradle/jdks`, which suggests Gradle auto-provisioned one. **Auto-provisioning on a fresh machine was not tested.** A JDK 21 alone (also installed here) is not the configured toolchain. |
| AGP / Kotlin plugin / Compose BOM | 9.4.1 / 2.2.10 / 2026.02.01 | From `gradle/libs.versions.toml`. |
| compileSdk / targetSdk / minSdk | 37 / 37 / 26 | |
| Android SDK | Platforms 34, 35, 36 and 37.0; build-tools 34.0.0, 35.0.0, 36.0.0 | Platform 37.0 is the one needed for `compileSdk 37`. Which build-tools version AGP selected was not checked. |
| Android CLI (`android --version`) | 1.1.16717390 | At `~/.local/bin/android` on `PATH`. Install steps were not recorded. The Claude plugin directory was `1.0.16625370`. |
| Android Studio | "Rabbit 1 \| 2026.2.1" | Needed only for the Studio route that failed in B2 and for its bundled JDK. |
| adb / emulator | adb 1.0.41; emulator 37.2.12.0 | Used in A2 only. AVD `Pixel_9a`. |

### Environment configuration

- **Android SDK location is required.** Neither `ANDROID_HOME` nor `ANDROID_SDK_ROOT` was set on the test machine, and `local.properties` is not in the repository (it is ignored). The build was run with a `local.properties` containing `sdk.dir=<path to your Android SDK>`. Without it, the build fails:

  ```text
  FAILURE: Build failed with an exception.
  * What went wrong:
  Could not determine the dependencies of task ':app:compileDebugJavaWithJavac'.
  > SDK location not found. Define a valid SDK location with an ANDROID_HOME environment variable or by setting the sdk.dir path in your project's local properties file at '<project>/local.properties'.
  ```

  Set `ANDROID_HOME` or create `local.properties` yourself. Do not commit that file.
- **Network.** The audit builds worked both online and with `--offline` once the dependencies were cached. A cold, empty Gradle cache was not tested.
- **JDK on `PATH`.** `JAVA_HOME` pointed at Android Studio's JDK 25 during the audit. With it unset, `./gradlew --version` still started on the same JDK, because that JDK was first on `PATH`.
- **Claude Code settings.** The recorded permission mode was `default` in Test A and A2 attempt 1, `auto` in the completed A2 run and in B2, and changed during Test B and A2 attempt 2. Whether this affected anything is unknown. User-level Claude Code settings, hooks and memory files in effect at the time were not recorded.
- **Android CLI.** `android preview` runs without a device, an emulator or an IDE (per `android preview --help`) and builds the `:app` target itself.

### Build check (verified)

`./gradlew assembleDebug` finished with `BUILD SUCCESSFUL` (36 tasks, no warnings in the log) for the root project, for the Test A source and for the Test B source, each in a scratch copy using the root Gradle configuration. **These runs were made before the launcher icons were replaced and omitted** (see Licenses), so they used the original icons. The root app with the replacement icon was verified afterwards in a temporary working copy: `./gradlew :app:assembleDebug` finished with `BUILD SUCCESSFUL` (36 tasks, exit code 0, no warnings or errors in the log), the built APK contains only the four new XML icon resources and no `.webp` files, and `android preview render` produced `InventoryPhonePreview` and `InventoryTabletPreview` PNGs whose SHA-256 checksums are identical to the archived B2 images (`Phone.png` `d930961e…`, `Tablet.png` `3df3226c…`). The component-tree JSON matched the archived files semantically (267 phone and 365 tablet nodes) with node order differing, as noted below. The snapshots with icons added were not rebuilt.

## Reproducing

The commands below were run in the experiments. Run them from the project root. The CLI commands follow the recorded pattern, so check `android preview render --help` on your version.

**Android CLI Compose Preview renders (B2 retries):**

```bash
android preview list :app
android preview render :app \
  --fqn com.perimamoglu.homestockbenchmark.InventoryScreenKt.InventoryPhonePreview \
  --output=<output-dir>/Phone.png
android preview render :app \
  --fqn com.perimamoglu.homestockbenchmark.InventoryScreenKt.InventoryTabletPreview \
  --output=<output-dir>/Tablet.png

# with the semantics component tree (PNG + JSON)
android preview render :app --component-tree \
  --fqn com.perimamoglu.homestockbenchmark.InventoryScreenKt.InventoryPhonePreview \
  --fqn com.perimamoglu.homestockbenchmark.InventoryScreenKt.InventoryTabletPreview \
  --output=<output-dir>/ct
```

**Emulator screenshots without the CLI (A2):** build with `./gradlew assembleDebug`, start an AVD, `adb install -r` the APK, set `adb shell wm size`/`wm density` to match the preview, launch `androidx.compose.ui.tooling.PreviewActivity` with `--es composable <fully qualified preview name>`, and capture with `adb exec-out screencap -p`. Reset `wm size` and `wm density` afterwards.

**To rerun an experiment**, give the agent the matching prompt from `prompts/` in a fresh project built from the project template state at commit `0202cb3`. That commit is not in this repository, and the template's generator is not recorded, so an exact rerun of the code-generation tests is not possible from this repository alone. To rerun the *verification* experiments, use the root `app/`, which has the Test B code that both A2 and B2 reviewed. The snapshots in `experiments/` are archival and need launcher icons added before they build (`experiments/README.md`). The `--fqn` names above apply to Test B only, because Test A's preview functions have different names (`experiments/README.md`). Results will vary between runs.

## Results

Detail and caveats are in `results/comparison.md`.

| Run | Previews produced? | Agent's own timer | Notes |
|---|---|---|---|
| A2 completed (autonomous) | Yes, **emulator screenshots** | 57 s | No Android CLI used |
| B2 original attempt (autonomous) | **No** | 49 s | `android studio render-compose-preview` failed: `Failed to get module.` / `File does not exist`. Cause: Studio's open-project state. |
| B2 assisted retry 1 (user-guided) | Yes, **Android CLI Compose Preview renders** | 17 s | `list` plus both renders |
| B2 assisted retry 2 (user-guided) | Yes, PNG + component-tree JSON | not timed | |

**The timers must not be compared.** They measure different operations, each run once. **These measurements do not support any claim that the Android CLI is faster than another approach.**

### Token, cost and duration records

These come from the cumulative records stored in each session's transcript. **Each figure covers the whole session**, not a single stage.

| Session | Sonnet in / out tokens | Cache read tokens | Session cost (USD) | Scope |
|---|---|---|---|---|
| Test A | 514 / 5,376 | 189,841 | 0.1575 | one prompt |
| Test B | 518 / 6,631 | 280,320 | 0.2188 | one prompt |
| A2 attempt 1 (interrupted) | 4 / 314 | 65,213 | 0.0547 | interrupted |
| A2 attempt 2 (interrupted) | 8 / 1,153 | 150,543 | 0.0997 | interrupted |
| A2 completed | 524 / 4,262 | 390,107 | 0.1923 | one prompt |
| B2 | 2,590 / 14,759 | 1,888,065 | 0.5719 | **five prompts**: failed attempt, two retries, report, commit |

B2's total cannot be split by stage from these records, so it is not the cost of the failed attempt or of the retry. The cost figures are the transcripts' own and were not recomputed.

### A2 versus B2 renders

The A2 images are on-device screenshots, including status and gesture bars, with the window forced to the preview size. The B2 images are the Android CLI's own Compose Preview renders. They differ in size and framing and should not be compared pixel for pixel.

### B2 findings (from the original report, with the tablet correction)

- Phone: the extended FAB overlaps the end of the visible content at the initial scroll position.
- **Tablet: the grid has three column slots, and the third is empty because each category has at most two products.** The first visual reading, that the tablet used two columns, was corrected by the component tree.
- No `contentDescription` anywhere. The −/+ buttons are labelled only by symbols. Category headers have no `heading` semantic. Out-of-stock is exposed as a disabled Button.
- Component-tree node order varies between runs. A rerun gave byte-identical PNGs, but JSON whose child order and `actions` order differed, with the same 267 phone and 365 tablet nodes after sorting. See `results/artifacts/PROVENANCE.md`.
- Unverified (needs a device, TalkBack or measurement): TalkBack announcements and focus order, 48 dp touch targets, colour contrast, font scale and dark theme, and the Add product dialog.

## Licenses

This repository has two licenses, depending on the kind of file.

| Applies to | License | File |
|---|---|---|
| **Source code and icon written for this project:** `InventoryScreen.kt` and `MainActivity.kt` in `app/` and `experiments/*/app/src/`, and the original storage-box launcher icon in the root `app/` | [Apache License 2.0](LICENSE) | `LICENSE` |
| **Documentation and research artifacts:** `README.md`, `prompts/`, `results/` (including `results/artifacts/`), `experiments/README.md`, `ANDROID_CLI_BENCHMARK_FINDINGS.md` | [Creative Commons Attribution 4.0 International](LICENSE-docs) | `LICENSE-docs` |
| **Third-party and template-derived material:** the 12 other template boilerplate files, the Gradle build configuration, and the Gradle wrapper (the 14 original launcher icon files are **not included**) | **Not simply Apache-2.0.** Own terms or unverified; see [`NOTICE`](NOTICE) and below | `NOTICE` |

**The Apache-2.0 license of this repository does not override any third-party license.** Where a file is under its own terms, those terms apply to it. [`NOTICE`](NOTICE) is informational: it does not change `LICENSE` or `LICENSE-docs`. Each statement below is marked as confirmed or inferred.

Provenance caveats, stated here so that nothing is claimed beyond what is known:

- None of the source files carries a license or copyright header.
- `InventoryScreen.kt` and `MainActivity.kt` were written by an AI agent (Claude Sonnet 5.5) under the author's direction. Copyright in AI-generated code is unsettled in some jurisdictions, so Apache-2.0 here states the author's intent, not a legal conclusion.
- **Original launcher icons: not redistributed.** The original project had 14 launcher icon files (`res/drawable/ic_launcher_{background,foreground}.xml`, `res/mipmap-anydpi/ic_launcher{,_round}.xml` and ten `res/mipmap-*/ic_launcher{,_round}.webp` files). The viewed xxxhdpi image showed the Android robot head on a green grid. Their redistribution rights could not be verified, so they were removed from this repository:
  - **The runnable root `app/`** uses an **original storage-box launcher icon** created for this project: a green square with a white and pale-green box, drawn as four small XML files. It contains no Google artwork or trademarks and is covered by Apache-2.0. The ten `.webp` files were removed (`minSdk` is 26, so adaptive icons need no raster fallbacks). The Kotlin code and the Gradle files are unchanged, so **the root `app/src/` is no longer byte-identical to Test B** (it adds those four icon files).
  - **Both archival snapshots** (`experiments/test-a/`, `experiments/test-b/`) omit all 14 files. Each snapshot's `OMITTED_ASSETS.md` records the original paths, sizes, SHA-256 checksums, Git blob IDs and the full source commit hash, and these were verified against the commits. **The snapshots match their original commits only for the files they retain**, and they are not byte-identical to the complete commits. They are archival, not runnable as they are.
  - **Archived results are unchanged.** Everything in `results/`, the prompts and the findings report were produced with the original icons present. The icons are not drawn by the Compose previews.
  - *Why:* the template provenance of the icons is unproven, the two official Google pages disagree on the robot's Creative Commons version (2.5 and 3.0) and limit its free use to marketing communications, and Android Studio's terms do not address generated assets. The sources, the attribution line Google gives, and the confirmed-versus-inferred breakdown are in [`NOTICE`](NOTICE).
  - *Trademark is separate from copyright:* Google keeps its trademark rights in the Android robot and the Android name, and the brand guidelines forbid claiming trademark rights in them. Nothing here suggests endorsement by Google.
- **Other template boilerplate: 12 files, individual licensing provenance not verified.** These are `AndroidManifest.xml`, `Color.kt`, `Theme.kt`, `Type.kt`, `rules.keep`, `colors.xml`, `strings.xml`, `themes.xml`, `backup_rules.xml`, `data_extraction_rules.xml`, `ExampleUnitTest.kt` and `ExampleInstrumentedTest.kt`. They came from the initial commit `0202cb3`, whose `.idea/` files show the project was opened in Android Studio, but the generating tool is not recorded. No official statement was found that licenses the output of Android Studio's templates. Several files are minimal or comment-only. They are kept unchanged so that the files the snapshots retain match their commits, and Apache-2.0 covers them only to the extent the author may license them.
- **Gradle build configuration, same caveat.** `build.gradle.kts`, `app/build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `gradle/gradle-daemon-jvm.properties` and the `.gitignore` files were also in commit `0202cb3`. They look generated by the project template, and their individual licensing provenance has not been verified either. They are not claimed as the author's own work.
- `gradlew`, `gradlew.bat` and `gradle/wrapper/gradle-wrapper.jar` come from Gradle and carry their own license (the script is headed Apache-2.0, and the JAR's manifest declares `SPDX-License-Identifier: Apache-2.0`). They are not the author's work. The JAR is Gradle 9.6.0's official wrapper JAR (SHA-256 `497c8c2a7e5031f6aa847f88104aa80a93532ec32ee17bdb8d1d2f67a194a9c7`, matching the checksum Gradle publishes at `services.gradle.org/distributions/gradle-9.6.0-wrapper.jar.sha256`).
- The screenshots and renders show the app UI, and the component-tree JSON is tool output about that UI. They are treated as research artifacts under CC BY 4.0.
- The CC BY 4.0 text is a legal text. It is not a statement that the author holds rights over third-party material.
- This is not legal advice.

## Limitations

- Exploratory. One run per experiment, one model, one machine, no statistics.
- The A2 and B2 prompts differ only in their tool constraints, but the sessions had different numbers of prompts and different tools, so tokens and cost are not comparable.
- The B2 failure was environmental. It does not reflect the CLI's capability in a clean setup.
- The B2 success was user-guided. Unaided discovery of `android preview` was not demonstrated either way.
- The A2 findings come from the agent's reading of two screenshots and were not independently verified here.
- Visual judgements come from a model reading downscaled images, not pixel measurements. Contrast was judged by eye.
- Only two static previews, the default theme and the top of the screen were reviewed. Previews do not scroll.
- Token and duration records are whole-session figures. The meaning of `totalDuration` was not verified. Per-request records exist in the transcripts but were not analysed.
- The Git state of the A2 and B2 worktrees was checked as it is now, plus the reflog entry for their creation.

## Missing information for full reproducibility

- The session transcripts, which are private and not published. The prompts, commands, timers and usage figures here were extracted from them by hand.
- Per-stage token usage for B2.
- The inline Python scripts used on the component tree. Only their effects are documented.
- The full text of the `android-cli:android-cli` skill as it was loaded, and its version. The report records the plugin cache directory name `1.0.16625370`.
- Raw `android preview` output and build logs beyond the last lines the agent captured.
- The reasons the two A2 attempts were interrupted.
- Any run on a clean machine, a different model, or a repeated run for variance.
- The 14 original launcher icon files. They are intentionally not in this repository (see Licenses) and exist only in the author's local repository, so the exact original app cannot be rebuilt from this repository alone. Their checksums are in each snapshot's `OMITTED_ASSETS.md`.
- Source of the initial template project (commit `0202cb3`), and the license status of its remaining assets. The commit and its history are not in this repository.
- The repository has no remote and no public Git history. The source commit hashes in `experiments/README.md` were read from the author's local repository.

## Further reading

- [`ANDROID_CLI_BENCHMARK_FINDINGS.md`](ANDROID_CLI_BENCHMARK_FINDINGS.md): the original B2 report (unchanged).
- [`results/comparison.md`](results/comparison.md)
- [`results/artifacts/PROVENANCE.md`](results/artifacts/PROVENANCE.md)
- [`prompts/ui-verification.md`](prompts/ui-verification.md)
- [`prompts/code-generation.md`](prompts/code-generation.md)
