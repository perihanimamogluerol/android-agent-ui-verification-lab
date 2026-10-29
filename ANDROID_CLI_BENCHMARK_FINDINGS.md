# Android CLI Benchmark Findings: HomeStock Compose Previews

Review of the phone and tablet `@Preview` functions of the HomeStock inventory screen, using the Android CLI. No application source or build configuration was modified.

## 1. Environment and tool versions

| Item | Value |
|---|---|
| Host OS | macOS 15.6.1 (Darwin 24.6.0) |
| Android CLI (`android --version`) | 1.1.16717390 |
| Android CLI Claude plugin (cache dir name) | 1.0.16625370 |
| Android Studio (`android studio check`) | "Rabbit 1 \| 2026.2.1", running (pid 17558) |
| AGP / Kotlin / Compose BOM | 9.4.1 / 2.2.10 / 2026.02.01 (`gradle/libs.versions.toml`) |
| Project | `HomeStock-PreviewB`, a git worktree on branch `experiment/preview-with-cli`, commit `1f95f80` |
| Code under test | `app/src/main/java/com/perimamoglu/homestockbenchmark/InventoryScreen.kt` |
| Previews | `InventoryPhonePreview` (411x891 dp), `InventoryTabletPreview` (1280x800 dp) |
| Render settings (from the component tree) | 420 dpi, fontScale 1.0, uiMode 0, deviceId "Custom" |
| AI model driving the session | Claude Sonnet 5.5 via Claude Code |

No device or emulator was connected (`adb devices` was empty). Two AVDs exist (`Pixel_9a`, `Pixel_9_Pro_Goldie`) but were not started.

## 2. Methodology

The work was done in three sequential stages, each prompted separately.

1. **Original attempt.** Render the previews through the Android Studio integration (`android studio render-compose-preview`).
2. **Assisted retry 1.** Use the standalone `android preview list` and `android preview render`. The user supplied the exact commands, so this stage was not discovered independently. Both PNGs were then read visually.
3. **Assisted retry 2.** Re-render with `--component-tree` and analyse the JSON with a short Python script that lists every node carrying `semantics`. The script was run with `python3 -I`.

Visual review used the image reader on the PNGs. Accessibility review used the unmerged component tree. No TalkBack, emulator, or contrast-measurement tool was used.

## 3. Commands actually executed

```
git ls-files; grep -rn "@Preview" app/src
android studio check
android studio render-compose-preview --help
android studio render-compose-preview --project=HomeStockBenchmark --output-image-file=<scratch>/{Phone,Tablet}.png <worktree file> Inventory{Phone,Tablet}Preview   (x2 variants, see below)
adb devices
android emulator list
android preview --help
android preview list :app
android preview render --help
android preview render :app --fqn com.perimamoglu.homestockbenchmark.InventoryScreenKt.Inventory{Phone,Tablet}Preview --output=<scratch>/{Phone,Tablet}.png
android preview render :app --component-tree --fqn ...InventoryPhonePreview --fqn ...InventoryTabletPreview --output=<scratch>/ct
android --version
```

The two Studio render variants were: the worktree file path, and the file path in the main checkout.

Gradle was not invoked directly. `android preview render` builds the `:app` target itself and writes build logs under `/var/folders/.../T/build-log-*.log`, which were not read.

## 4. Compose Preview rendering results

| Attempt | Phone | Tablet | Result / error |
|---|---|---|---|
| `android studio render-compose-preview` (worktree path) | failed | failed | `Error: Failed to get module.` |
| `android studio render-compose-preview` (main checkout path) | failed | failed | `Error: File does not exist: .../HomeStockBenchmark/app/src/main/java/.../InventoryScreen.kt` |
| `android preview render :app` | **rendered** | **rendered** | PNG 1079x2339 and 3360x2100 |
| `android preview render :app --component-tree` | **rendered** | **rendered** | PNG plus JSON (about 548 KB and 765 KB) |

**Why the Studio route failed.** Studio had only the main checkout open, and it was on `main`, which does not contain `InventoryScreen.kt`. The worktree was not an open Studio project. This is an environment/setup cause, not a defect in the CLI or in the app.

**Elapsed time.** The only time measurements are two wall-clock figures. The original attempt took about 49 s and ended in failure, before any preview was rendered. Assisted retry 1 (list plus both renders) took 17 s. Retry 2 was not timed. The first 49 s measures only the failed Studio route, so it should not be compared against the 17 s. No token usage figure is available in this session, so none is reported.

## 5. Visual findings (from the rendered PNGs)

Both images were read. Only the top of each screen is visible. The previews are not scrollable, so the later categories were not inspected.

### Phone (1 column)
- Grid is correct: one full-width card per row, headers span the row.
- No clipped text, no overlapping text inside cards, no awkward wrapping in the visible area.
- **Add product button covers content.** The extended FAB overlaps the right edge of the last visible card (Bananas) and sits next to the "Snacks" header. The grid reserves 88 dp at the bottom, so this is only the initial scroll position, but it hides content until the user scrolls.
- The preview ends in the Snacks section, so Drinks and Household Essentials were not seen.

### Tablet (3 column slots, 2 used)
- Cards are laid out in a 3-slot adaptive grid. The component tree shows card x-positions of about 84 px and 1187 px, plus room for a third. Each category has 1 or 2 products, so the right-hand third of the screen is empty.
- **Uneven card heights within a row.** The out-of-stock card is taller than its in-stock neighbour (Red Lentils about 125 px tall vs Basmati Rice about 187 px, measured from the displayed image), because of the extra chip row. Cards in a row therefore have different heights.
- Title bar and FAB do not overlap anything visible. The preview is cut off in the Drinks row, so Household Essentials was not seen.

### Contrast (visual impression only)
- The disabled "−" button (grey on pink) and the "Out of stock" chip (red on pink) look low in contrast. These were judged by eye, not measured.

## 6. Component tree accessibility findings

Source: unmerged semantics tree. The only semantic keys present in either file were `text`, `role`, `actions` and `disabled`. There was no `contentDescription`, `stateDescription`, `heading`, or `testTag` anywhere.

| # | Finding | Evidence | Severity |
|---|---|---|---|
| A1 | The −/+ buttons have no content description. Each is `role: Button, OnClick` whose only label is a child Text `"−"` (U+2212) or `"+"`. All cards have identical buttons, none names its product. | `Box{role:Button}` followed by `Text{"−"}` / `Text{"+"}` for every card | High |
| A2 | "Add product" has an accessible name via its text (`"+ Add product"`), `role: Button`. The leading "+" may be spoken oddly. | `Box{role:Button}` then `Text{"+ Add product"}` | Low |
| A3 | Name, category, quantity and out-of-stock state are all present and match the data (e.g. Apples/Fruits/8, Spinach/Vegetables/0), but each is a separate node. The quantity is a bare number with no label. Cards are not grouped, so each card produces about 6 to 7 separate nodes. | Nodes listed per card | Medium |
| A4 | The "Out of stock" chip is exposed as a **disabled Button** with `OnClick`, not as a status. The "−" on those cards is `disabled: true` with no reason given. | `Box{disabled:true, role:Button}` with `Text{"Out of stock"}` | Medium |
| A5 | Category headers are plain Text, with no `heading` semantic. | No `heading` key anywhere | Medium |
| A6 | Button node bounds are 105x105 px at 420 dpi, which is 40 dp, under the 48 dp guideline. Material may enlarge the real touch area, so this is only a possible issue. | Bounds counter: 14 (phone) / 20 (tablet) nodes at 105x105 | Low |

## 7. Confirmed issues vs unverified observations

### Confirmed (directly observed in the rendered PNG or component tree)
- Phone: the FAB overlaps the end of the visible content at the initial scroll position.
- Tablet: the third grid column is empty because categories have at most 2 products; cards in a row differ in height.
- No content descriptions anywhere; the −/+ buttons are labelled only by symbols (A1).
- Category headers have no heading semantic (A5).
- Out-of-stock is exposed as a disabled Button (A4); the quantity is an unlabelled number and cards are not grouped (A3).

### Unverified (needs a device, TalkBack, or measurement)
- What TalkBack actually announces for "−", "+" and "+ Add product".
- The merged semantics TalkBack uses, and so the real number of swipe stops per card.
- TalkBack focus order. The tree lists lazy items in composition order, not necessarily reading order.
- Whether the 40 dp buttons are tappable at the full 48 dp.
- Colour contrast of the disabled "−" and "Out of stock" chip. Only judged by eye.
- Behaviour of the disabled "−" in TalkBack, and the effect of large font scale or dark theme.
- The Add product dialog, the Household Essentials section, the "No products yet" empty state, and anything below the preview height. The previews do not render them.

## 8. Limitations and sources of bias

- **Assisted retries.** The successful CLI commands were given by the user after the first attempt failed. The result does not show that the model would have found `android preview render` unaided.
- **Not a benchmark of speed.** There are two wall-clock figures, from different operations, with no repeats. They are not comparable and no general timing claim should be drawn from them. Build-log contents and cold-vs-warm Gradle state are unknown.
- **No token usage** was available.
- **Single model run.** One session, one model, one machine, no repetition.
- **A mistake I made along the way.** In the first visual review I said the tablet used only 2 columns. The component tree later showed three column slots and that the empty space is due to the data. Section 5 uses the corrected reading. Visual size estimates from the displayed image (e.g. card heights) are approximate.
- **Preview coverage.** Only two static previews of one data set were rendered, with the default theme only. Previews are not scrollable, so only the top part of the screen was reviewed.
- **Image reading.** Visual judgements come from a model reading downscaled images, not from a pixel measurement.
- **Environment.** The Studio failure came from Studio's open-project state. The result does not reflect the CLI's capability in a clean setup.
