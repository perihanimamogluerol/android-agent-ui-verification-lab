# UI-Verification Prompts (Test A2 and Test B2)

The prompts below were recovered verbatim from the Claude Code session transcripts. The transcripts store each prompt inside a `<pasted_content>` wrapper tag, which is omitted here. The text between the tags is unchanged. The transcripts are private and are not part of this repository.

In all six sessions every recorded agent turn used `claude-sonnet-5-5`. The usage records also list a few small `claude-haiku-5-5` calls (about 1.5k input and under 30 output tokens per session) whose purpose is not recorded. Times are UTC (local time was UTC+1).

## Overview and provenance

| Part | Who chose the tools | Project folder / branch | Session | Start (UTC) |
|---|---|---|---|---|
| A2, interrupted attempt 1 | Agent (no tool constraint in the prompt) | `HomeStock-PreviewA` / `experiment/preview-no-cli` | `a61858f6` | 13:24 |
| A2, interrupted attempt 2 | Agent (Android CLI forbidden) | same | `b0ac5547` | 13:28 |
| **A2, completed run** | Agent (Android CLI forbidden) | same | `6fbc5116` | 13:31 |
| **B2, original attempt** | Agent (Android CLI allowed) | `HomeStock-PreviewB` / `experiment/preview-with-cli` | `5626be34` | 13:35 |
| B2, assisted retry 1 | **User supplied the commands** | same session | `5626be34` | 13:37 |
| B2, assisted retry 2 | **User requested `--component-tree`** | same session | `5626be34` | 13:41 |
| B2, report and commit | User | same session | `5626be34` | 13:43, 13:46 |

Both worktrees were created from commit `1f95f80` at 14:22:10 local time (13:22 UTC), before any of the sessions above started. See `results/comparison.md` for what that means for comparing A2 and B2.

The "A2" and "B2" labels come from the experiment naming. They map to the `PreviewA` and `PreviewB` project folders. The findings report (`ANDROID_CLI_BENCHMARK_FINDINGS.md`) covers only B2.

---

## A2: no Android CLI

### Interrupted attempts

These two sessions were interrupted by the user and produced no review. They are listed separately and are **not** the A2 result.

**Attempt 1** (`a61858f6`, 13:24 UTC). The prompt had no tool constraint.

```text
Review the existing HomeStock Jetpack Compose UI for phone and tablet screen sizes.

Tasks:
- Find the existing phone and tablet @Preview functions.
- Attempt to render both previews into PNG images.
- Inspect the actual rendered images, not just the Kotlin source code.
- Check for text clipping, overlapping elements, excessive empty space, incorrect grid layout, and accessibility-related visual issues.
- Report any UI problems you identify and explain how you verified them.
- Do not modify application code unless necessary to resolve a rendering failure.
- Do not add dependencies or change the project configuration.
- If you cannot render or inspect the previews, explain exactly why.

When finished, provide:
1. Tools and commands used.
2. Whether each preview was successfully rendered.
3. Whether the generated images were actually inspected.
4. Any visual issues found.
5. Total execution time and any available token usage information.
```

What the transcript records: two `Bash` calls (the second included `android --help | head -40`), then `[Request interrupted by user for tool use]` at 13:25:29. No image was produced.

**Attempt 2** (`b0ac5547`, 13:28 UTC). The prompt added a ban on the Android CLI.

```text
Review the existing HomeStock Jetpack Compose UI for phone and tablet screen sizes.

Tasks:
- Find the existing phone and tablet @Preview functions.
- Attempt to render both previews into PNG images.
- Inspect the actual rendered images, not just the Kotlin source code.
- Check for text clipping, overlapping elements, excessive empty space, incorrect grid layout, and accessibility-related visual issues.
- Report any UI problems you identify and explain how you verified them.
- Do not modify application code unless necessary to resolve a rendering failure.
- Do not add dependencies or change the project configuration.
- If you cannot render or inspect the previews, explain exactly why.

Tool constraints:
- Do not invoke the Android CLI binary named "android" or use Android CLI plugins or skills.
- You may use Gradle, Android Studio tools, ADB, and other standard development tools.
- If rendering is not possible with the available tools, explain the limitation and complete the report without attempting Android CLI.

When finished, provide:
1. Tools and commands used.
2. Whether each preview was successfully rendered.
3. Whether the generated images were actually inspected.
4. Any visual issues found.
5. Total execution time and any available token usage information.
```

What the transcript records: four `Bash` calls (a Gradle `assembleDebug` build, then starting the `Pixel_9a` emulator), then `[Request interrupted by user for tool use]` at 13:28:57. No image was produced.

The transcripts record that the user interrupted, but not why. No reason is stated here.

### Completed run (`6fbc5116`, 13:31 UTC)

```text
Review the existing HomeStock Jetpack Compose UI for phone and tablet screen sizes.

Tasks:
- Find the existing phone and tablet @Preview functions.
- Attempt to render both previews into PNG images.
- Inspect the actual rendered images, not just the Kotlin source code.
- Check for text clipping, overlapping elements, excessive empty space, incorrect grid layout, and accessibility-related visual issues.
- Report any UI problems you identify and explain how you verified them.
- Do not modify application code unless necessary to resolve a rendering failure.
- Do not add dependencies or change the project configuration.
- If you cannot render or inspect the previews, explain exactly why.

Tool constraints:
- Do not invoke the Android CLI binary named "android" or use Android CLI plugins or skills.
- You may use Gradle, Android Studio tools, ADB, and the Android Emulator.
- If rendering is not possible with the available tools, explain the limitation instead of repeatedly retrying.

When finished, provide:
1. Tools and commands used.
2. Whether each preview was successfully rendered.
3. Whether the generated images were actually inspected.
4. Any visual issues found.
5. Total execution time and any available token usage information.
```

Commands the agent ran, from the transcript (paths shortened):

- Discovery: `git status`, `grep -rn "@Preview" app/src`, `find`, `cat` of Gradle files, `emulator -list-avds`, `adb devices`.
- Build: `./gradlew assembleDebug --offline -q`.
- Emulator: `emulator -avd Pixel_9a -no-snapshot -no-audio -no-boot-anim` (in the background), then `adb wait-for-device` and a boot-completed poll. The first attempt's `timeout` call failed with "command not found".
- Rendering: `adb install -r app-debug.apk`; `adb shell wm size 1080x2339`; `adb shell am start -n com.perimamoglu.homestockbenchmark/androidx.compose.ui.tooling.PreviewActivity --es composable ...InventoryPhonePreview`; `adb exec-out screencap -p > phone.png`; then `wm size 1680x1050`, `wm density 210`, the same `am start -S ... InventoryTabletPreview`, and `screencap` to `tablet.png`.
- Cleanup: `wm size reset`, `wm density reset`, `adb emu kill`, `git status --short`.
- No `android` binary, plugin or skill call. Both images were opened with the `Read` tool.

Result as reported by the agent: both previews captured and viewed, phone layout fine, tablet layout with "real layout problems". These are **emulator screenshots of `PreviewActivity`**, not Compose Preview renders; the agent itself noted they are "not Android Studio's Layoutlib renderer" and that status and gesture bars appear in the capture. This repository does not independently verify the agent's visual findings.

---

## B2: Android CLI allowed

### Original attempt (autonomous, `5626be34`, 13:35 UTC)

```text
Review the existing HomeStock Jetpack Compose UI for phone and tablet screen sizes.

Tasks:
- Find the existing phone and tablet @Preview functions.
- Attempt to render both previews into PNG images.
- Inspect the actual rendered images, not just the Kotlin source code.
- Check for text clipping, overlapping elements, excessive empty space, incorrect grid layout, and accessibility-related visual issues.
- Report any UI problems you identify and explain how you verified them.
- Do not modify application code unless necessary to resolve a rendering failure.
- Do not add dependencies or change the project configuration.
- If you cannot render or inspect the previews, explain exactly why.

Tool constraints:
- You may use the installed Android CLI plugin and Android CLI commands.
- You may also use Gradle, Android Studio tools, ADB, and the Android Emulator.
- If rendering is not possible with the available tools, explain the limitation instead of repeatedly retrying.

When finished, provide:
1. Tools and commands used.
2. Whether each preview was successfully rendered.
3. Whether the generated images were actually inspected.
4. Any visual issues found.
5. Total execution time and any available token usage information.
```

What the transcript records:

- The agent loaded the `android-cli:android-cli` skill first, then chose the Android Studio route itself. The prompt did not name `android studio render-compose-preview`.
- Commands: `android studio check`, `android studio render-compose-preview --help`, then `android studio render-compose-preview --project=HomeStockBenchmark --output-image-file=<scratch>/{Phone,Tablet}.png <file> Inventory{Phone,Tablet}Preview` in four shell invocations, each looping over Phone and Tablet. The first failed with a shell error (`= not found`) and the second with `timeout: command not found`, so neither reached the CLI. The third used the worktree file path and the fourth the main-checkout file path. Then `adb devices` and `android emulator list`.
- Errors: `Error: Failed to get module.` (worktree path) and `Error: File does not exist: ...` (main-checkout path).
- The agent stopped after the failures, as the prompt allowed, and reported no rendered images and no visual findings.
- Its own timer printed **49 s**.
- The agent did not run `android preview ...`. In the transcript, before the user's next prompt, the strings `android preview` and `preview render` do not appear anywhere. The loaded skill text visible in the transcript mentioned `render-compose-preview`. This does not show what the full skill file contained.

### Assisted retry 1 (user-guided, 13:37 UTC)

```text
Please retry the UI verification using the standalone Android CLI preview commands, not the Android Studio integration.

The Android CLI has already successfully rendered these previews from this project using the :app Gradle target.

Use:
- android preview list :app
- android preview render :app --fqn com.perimamoglu.homestockbenchmark.InventoryScreenKt.InventoryPhonePreview
- android preview render :app --fqn com.perimamoglu.homestockbenchmark.InventoryScreenKt.InventoryTabletPreview

Inspect both generated PNG images and report any visual issues.

Do not modify the application code or project configuration.

Report the commands used, whether both images were inspected, and the additional execution time. Treat this as a separate assisted retry, not part of the original benchmark.
```

What the transcript records: `android preview --help`, `android preview list :app`, `android preview render --help`, and `android preview render :app --fqn ...Inventory{Phone,Tablet}Preview --output=<scratch>/{Phone,Tablet}.png`. Both images were read. The agent's timer printed **17 s**, covering `android preview list` plus both renders. A `git status --short` was run in the same command, but the agent noted it did not re-check it afterwards.

### Assisted retry 2 (user-guided, 13:41 UTC)

```text
Please run the Android CLI Compose Preview rendering again with the --component-tree option for both the phone and tablet previews.

Inspect the generated component tree JSON files and check:
- Whether the + and − quantity buttons have meaningful accessibility labels or content descriptions.
- Whether the "Add product" button has an accessible label.
- Whether product names, quantities, and out-of-stock states are represented correctly.
- Whether there are any missing or unclear accessibility semantics.

Do not modify any application code.

Report the accessibility issues you find, explain which findings are confirmed by the component tree, and distinguish them from issues that would require TalkBack testing.
```

What the transcript records: one `android preview render :app --component-tree --fqn ...InventoryPhonePreview --fqn ...InventoryTabletPreview --output=<scratch>/ct` command, then three short `python3 -I` scripts that walked the JSON. The scripts are inline in the transcript and are not preserved as files. This stage was not timed by the agent.

### Report and commit prompts (user)

```text
Write a detailed Markdown report named ANDROID_CLI_BENCHMARK_FINDINGS.md in the project root.

Include:
- Environment and tool versions
- Experiment methodology
- Android CLI commands actually executed
- Compose Preview rendering results
- Visual findings for phone and tablet
- Component tree accessibility findings
- Confirmed issues versus unverified observations
- Limitations and potential sources of bias

Do not modify any application source code or build configuration. Do not invent metrics or results that were not measured.
```

```text
Commit only ANDROID_CLI_BENCHMARK_FINDINGS.md to the current branch.

Use the commit message:
docs: add Android CLI benchmark findings

Do not modify or commit any application source code or build configuration.

Show the final git status and commit hash.
```

The commit is `b62bd15` on `experiment/preview-with-cli`. It adds only the findings file (124 lines).

---

## Notes

- The B2 session has five prompts. Prompts 2 and 3 are user-guided and must not be credited to the agent.
- Token, cost and duration records for these sessions are whole-session totals and are given in `results/comparison.md`. They cannot be split by stage.
- The B2 agent's own timers (49 s and 17 s) and the A2 completed run's timer (57 s) are the agent-measured figures. They were produced with `date +%s` differences inside the session.
