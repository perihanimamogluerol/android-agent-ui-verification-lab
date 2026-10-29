# Results Comparison

This benchmark is **exploratory**. Each experiment is a single run on one machine with one agent model (`claude-sonnet-5-5`; the usage records also list a few small Haiku calls of unrecorded purpose), with no repetition. Nothing here is a statistically controlled comparison, and the phases are not comparable to each other on time, tokens or cost.

Sources:
- `ANDROID_CLI_BENCHMARK_FINDINGS.md`: the original report for B2. It is unchanged and remains the primary source for B2's findings.
- Claude Code session transcripts (private, not included): prompts, commands, timers, and the token/cost records. Verbatim prompts are in `prompts/`.
- Artifacts: `results/artifacts/` (provenance and checksums in [`results/artifacts/PROVENANCE.md`](artifacts/PROVENANCE.md)).
- Git metadata of the sibling worktrees, checked read-only.

## Phase summary

| Phase | Tool access | Who chose the tools | Rendered previews | Evidence |
|---|---|---|---|---|
| 1. Code generation, Test A | No Android CLI | Agent | No (not attempted) | Commit `77fe81a`, session `43b73c49` |
| 1. Code generation, Test B | Android CLI allowed | Agent | No (not attempted) | Commit `1f95f80`, session `7cb1404f` |
| 2a. A2 interrupted attempts (2) | No CLI (attempt 1 had no constraint) | Agent | No | Sessions `a61858f6`, `b0ac5547` |
| 2b. A2 completed run | No Android CLI | Agent | Yes, as emulator screenshots | Session `6fbc5116`, `preview-a-no-cli/` |
| 3a. B2 original attempt | Android CLI allowed | Agent | **No** (Studio route failed) | Session `5626be34` |
| 3b. B2 assisted retries | Android CLI | **User supplied the commands** | Yes, as CLI Compose Preview renders | Session `5626be34`, `preview-b-android-cli/` |

## Starting code of the A2 and B2 worktrees

Checked in the git metadata of the sibling worktrees:

- Both `HomeStock-PreviewA` and `HomeStock-PreviewB` were created from commit **`1f95f80`** ("Experiment B: HomeStock MVP with Android CLI plugin"). Their reflogs show the initial entry at 2026-10-29 14:22:10 local time (13:22 UTC), which is before every A2 and B2 session started (earliest 13:24 UTC).
- **Initial state: identical.** Same commit, so the same application code. Both phases reviewed Experiment B's generated code. A2 did **not** review Test A's code (`77fe81a`), which differs from B's (`InventoryScreen.kt` and `MainActivity.kt`, +99 / −66 lines).
- **Later changes.** `HomeStock-PreviewA` is still at `1f95f80`, and `git status` is clean today. `HomeStock-PreviewB` is at `b62bd15`, which adds only `ANDROID_CLI_BENCHMARK_FINDINGS.md`. `git diff --stat 1f95f80 b62bd15` shows that single file (124 lines added). That commit was made at 14:46 local time, after the B2 verification ended.
- I checked the current state, not the state at each moment of the sessions. The sessions' own `git status` outputs were not reviewed in full.
- The `InventoryScreen.kt` in this repository's `app/` is identical to `HomeStock-PreviewA`'s.

## Phase 1: Code generation (Test A and Test B)

| | Test A | Test B |
|---|---|---|
| Prompt | Android CLI forbidden | Android CLI plugin allowed; asks to report CLI use |
| Android CLI use | None | Loaded the `android-cli:android-cli` skill, ran `which android`, no other CLI command |
| Gradle debug build | Run, no errors reported | `BUILD SUCCESSFUL` in the last run |
| Previews rendered | No | No |

Both runs are whole-session records of a single prompt each. The token, cost and duration figures are in `prompts/code-generation.md`. They are one-run observations. They do not show that one condition costs more.

## Phase 2: A2 (no Android CLI)

### Interrupted attempts (not the A2 result)

| | Attempt 1 (`a61858f6`) | Attempt 2 (`b0ac5547`) |
|---|---|---|
| Prompt | No tool constraint | Android CLI forbidden |
| What happened | 2 `Bash` calls, including `android --help`, then interrupted by the user | 4 `Bash` calls up to starting the emulator, then interrupted by the user |
| Images produced | None | None |
| Sonnet in / out tokens | 4 / 314 | 8 / 1,153 |
| Sonnet thinking tokens | 12 | 276 |
| Cache read / creation tokens | 65,213 / 11,215 | 150,543 / 18,231 |
| Session cost (USD) | 0.0547 | 0.0997 |

The transcripts record the interruptions but not the reasons.

### Completed run (`6fbc5116`)

| Item | Value |
|---|---|
| Method | Gradle `assembleDebug`, `Pixel_9a` emulator, `adb install`, `PreviewActivity` launched with `am start`, `adb exec-out screencap -p`, with `wm size`/`wm density` set to match each preview |
| Phone / tablet captured | Yes / Yes (`phone.png`, `tablet.png`) |
| Images inspected | Yes, by the agent |
| Android CLI used | No |
| Agent's own timer | **57 s** (script: `date +%s` difference, from the first command to emulator shutdown) |
| Sonnet in / out tokens | 524 / 4,262 (thinking 581) |
| Cache read / creation tokens | 390,107 / 27,374 |
| Session cost (USD) | 0.1923 |

The agent reported the phone layout as fine and the tablet layout as having layout problems. This repository has not independently verified those findings.

## Phase 3: B2 (Android CLI allowed)

### Original attempt (autonomous): failed

| Item | Value |
|---|---|
| What the agent did | Loaded the `android-cli:android-cli` skill, then **chose** `android studio render-compose-preview`. The prompt did not name it. |
| Phone / tablet rendered | No / No |
| Errors | `Error: Failed to get module.` (worktree file path) and `Error: File does not exist: .../HomeStockBenchmark/app/src/main/java/.../InventoryScreen.kt` (main-checkout path) |
| Cause, per the report | Studio had only the main checkout open, and it was on `main`, which does not contain `InventoryScreen.kt`. The worktree was not an open Studio project. This is an environment/setup cause, not a defect in the CLI or the app. |
| Agent's own timer | **49 s**, ending in failure |
| Outcome | The agent stopped after the failures, as its prompt allowed. It reported no images and no visual findings. |

In the transcript, before the user's retry prompt, the strings `android preview` and `preview render` do not appear. The agent therefore did not try the standalone CLI route itself. This does not show that it could not have found it.

### Assisted retries (user-guided): succeeded

The user's second prompt supplied the exact commands (`android preview list :app` and `android preview render :app --fqn ...`). The third prompt asked for `--component-tree`. **These results reflect user guidance and must not be credited to the agent alone.**

| Item | Retry 1 | Retry 2 |
|---|---|---|
| Commands | `android preview list :app`, `android preview render :app --fqn ... --output=...` | `android preview render :app --component-tree --fqn ... --output=...` |
| Phone / tablet rendered | Yes / Yes | Yes / Yes |
| Output | PNG 1079x2339 and 3360x2100 | PNG plus component-tree JSON (547,696 and 764,993 bytes) |
| Analysis | PNGs read visually by the model | JSON walked by short `python3 -I` scripts (inline, not preserved as files) |
| Agent's own timer | **17 s** (`list` plus both renders) | Not timed |

**Reproducibility check (2026-10-29, scratch copy of Test B).** Rerunning the same `android preview render` commands produced PNGs that are byte-identical to the stored artifacts. The component-tree JSON files had the same size but not the same bytes. Child-node order and the order of items inside `actions` lists differ between runs. After sorting the action lists and ignoring child order, all 267 phone nodes and all 365 tablet nodes matched exactly. So the semantic content is stable, but **the ordering of nodes in the tree can vary between runs**. Anything that reads the JSON in order (for example a traversal or reading-order claim) is therefore not stable. The report already lists TalkBack focus order as unverified.

Findings from this phase follow the original report. They are unchanged here.

**Confirmed** (observed in the PNG or component tree)
- Phone: the extended FAB overlaps the end of the visible content at the initial scroll position.
- **Tablet: the grid has three column slots, and the third is empty because each category has at most two products.** Cards in a row differ in height. This is the corrected reading from the component tree. The model's first impression, that the tablet used only two columns, was wrong, and the original report records the correction.
- No `contentDescription` anywhere. The −/+ buttons are labelled only by symbols (A1, High).
- Category headers have no `heading` semantic (A5). Out-of-stock is exposed as a disabled Button (A4). The quantity is an unlabelled number and cards are not grouped (A3).
- Lower severity: A2 ("+ Add product" name) and A6 (105x105 px button bounds, 40 dp at 420 dpi, possibly enlarged by Material).

**Unverified** (needs a device, TalkBack or measurement)
- TalkBack announcements, merged semantics and focus order; 48 dp touch targets; colour contrast (judged by eye only); font scale and dark theme; the Add product dialog, the Household Essentials section and the empty state.

### Whole-session records for B2

| Item | Value |
|---|---|
| Session | `5626be34`, five prompts (original attempt, two assisted retries, report, commit) |
| Sonnet in / out tokens | 2,590 / 14,759 (thinking 1,677) |
| Cache read / creation tokens | 1,888,065 / 57,526 |
| Session cost (USD) | 0.5719 |
| `totalAPIDuration` / `totalToolDuration` | 166.4 s / 19.9 s |
| `totalDuration` | 760.4 s |

**This is a whole-session total.** It includes the failed original attempt, both assisted retries, the report writing and the commit. It cannot be split by stage from these records, so it must not be read as the cost of the original attempt or of the assisted retry. The per-request records in the transcript might allow a split, but that has not been done.

## Comparing A2 and B2

What differs:
- **Tool access.** A2 forbade the Android CLI, and B2 allowed it.
- **Render method.** A2 produced **on-device emulator screenshots** of `PreviewActivity`. They include the status and gesture bars, and the window size was forced with `wm size`/`wm density`. B2 produced **Android CLI Compose Preview renders** from the CLI's own renderer, without a device or emulator. The images differ in size, framing and scale, so they are not pixel-comparable. The agent in A2 itself noted that its captures are not Layoutlib renders.
- **Autonomy.** The A2 completed run was fully autonomous. The B2 original attempt was autonomous and failed. B2's successful renders needed user-supplied commands.
- **Prompt text.** The two review prompts share their task list and reporting list, and differ in the tool constraints. The A2 final prompt also says "If rendering is not possible ... explain the limitation instead of repeatedly retrying", as does B2.

What can and cannot be concluded:
- **Time.** Agent timers are 57 s (A2 completed), 49 s (B2 failed attempt) and 17 s (B2 retry 1). They measure different operations, each run once, in different states. **No claim is made that the Android CLI is faster or slower than another approach.** Gradle cache state and build-log contents are unknown.
- **Tokens and cost.** The sessions have different numbers of prompts, and B2 includes the report and commit. The totals are not comparable per task.
- **Success.** In this environment, the autonomous A2 run produced screenshots via the emulator, and the autonomous B2 run produced none because of the Studio route's setup failure. The standalone CLI route worked once it was supplied. Nothing here compares how good the resulting verification is under equal conditions.
- **Prior knowledge.** The B2 agent might have found `android preview` unaided given a retry. The experiment did not test this.

## Contradictions between sources

- **Token usage.** The original report says no token usage figure was available in that session. This was true of what the agent could see, but the transcript stores cumulative token and cost records, which are reported above.
- **Report scope.** The original report does not mention A2, the two interrupted A2 attempts, Test A or Test B. It documents B2 only.
- **Report wording.** The report calls the Studio failure the "original attempt" and does not say the agent chose that route. The transcript shows the agent chose it after loading the skill.

## Remaining limitations

See the README for the full list. In short: single runs, whole-session usage only, no per-stage token split, no independent verification of the A2 findings, no clean-environment rerun, and no audit of the loaded skill's full text.
