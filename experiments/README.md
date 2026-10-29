# Experiment Source Snapshots

These folders hold the application source produced by the two code-generation experiments (see `../prompts/code-generation.md`). Each is a snapshot of `app/src/` taken from a Git commit with `git archive`, so it contains no Git metadata.

**They are archival snapshots, not runnable apps, and they are not byte-identical to the complete original commits.** Each omits 14 launcher icon files (see "Omitted launcher icons" below). They match their commits only for the files they retain. The runnable app is the root `app/` of this repository.

## Snapshots

| Folder | Experiment | Source commit (full hash) | Commit message | Purpose |
|---|---|---|---|---|
| `test-a/` | Test A, Android CLI forbidden | `77fe81a9f4aaa069ba39898f1812a98f7806c919` | Experiment A: HomeStock MVP without Android CLI | The code produced by the Test A session. Not reviewed by A2 or B2. |
| `test-b/` | Test B, Android CLI allowed | `1f95f80274552117509a047bd8f1981985df4be4` | Experiment B: HomeStock MVP with Android CLI plugin | The code produced by the Test B session. **The starting code of both A2 and B2.** |

**Original repository:** the author's local Git repository `HomeStockBenchmark`, which has no remote configured, so there is no public URL to cite. Its history is `0202cb3` (initial template project), then `77fe81a` (Test A), then `1f95f80` (Test B), then `b62bd15` (docs only, B2's findings report). The A2 and B2 worktrees were created from `1f95f80`. These hashes were read from that repository with read-only commands. The repository itself was not modified.

## Omitted launcher icons

Each snapshot omits the same 14 launcher icon files: `res/drawable/ic_launcher_{background,foreground}.xml`, `res/mipmap-anydpi/ic_launcher{,_round}.xml`, and the ten `res/mipmap-*dpi/ic_launcher{,_round}.webp` files.

- **Why:** the icon appears to be Google's Android robot artwork. The right to redistribute these files under this repository's licenses could not be verified. Template provenance is unproven, the official pages disagree on the robot's Creative Commons version (2.5 or 3.0) and limit its free use to marketing communications, and Android Studio's terms do not address generated assets. The reasoning and sources are in [`NOTICE`](../NOTICE).
- **Documentation of what was omitted:** [`test-a/OMITTED_ASSETS.md`](test-a/OMITTED_ASSETS.md) and [`test-b/OMITTED_ASSETS.md`](test-b/OMITTED_ASSETS.md) list every omitted file with its original byte size, SHA-256 and Git blob ID, and the full source commit hash. Two of the files have identical content, so there are 13 distinct hashes. The hashes identify the files and do not reproduce them.
- **The original files** exist only in the author's local `HomeStockBenchmark` repository. They are not in this repository, and nothing from them was copied.
- **Effect on the experiments:** none on the Kotlin code or the archived results. The icons are not drawn by the Compose previews. Everything under `results/` was produced with the original icons present and is unchanged.

## What the snapshots contain

- **Included:** the 14 retained files of `app/src/` at the source commit. These are the Kotlin sources, the manifest, the non-icon resources and the template test files. Each snapshot has 14 files, from 28 in its commit.
- **Excluded:** the 14 launcher icon files above, the Gradle files and wrapper, `.idea/`, `build/`, `local.properties`, caches and credentials.
- **Verification performed (after the icons were removed):** each snapshot's file list is exactly its commit's `app/src` file list minus the 14 icon files, with no extra files, and each of the 14 retained files was compared with the same path at its commit (`git show <commit>:<path>`). There were 0 mismatches. Each manifest row was also verified against the commit's own objects (SHA-256, size and blob ID).

## How the root `app/` relates to the snapshots

The root `app/` is the runnable app. It is based on Test B's code and uses an **original storage-box launcher icon** created for this project (a green square with a white and pale-green box), which contains no Google artwork. The 14 files retained in `test-b/app/src/` also exist, byte for byte, in the root `app/src/`. The root `app/src/` is **not** byte-identical to `test-b/app/src/`: it contains four icon files (`ic_launcher_background.xml`, `ic_launcher_foreground.xml` and the two `mipmap-anydpi` files) that the snapshot lacks, and the ten `.webp` files exist in neither. The Kotlin code and the Gradle files are unchanged from commit `1f95f80`.

## Building a snapshot

The snapshots are source only, and the Gradle configuration is not duplicated. It is the same at all three commits: `git diff --name-only 0202cb3 1f95f80` lists nothing outside `app/src/`, and the root Gradle files of this repository equal those at `1f95f80`.

Because the icons are omitted, a snapshot does not build as it is. `AndroidManifest.xml` refers to `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round`. To build one, copy this repository to a scratch location, replace its `app/src/` with the snapshot's, and add launcher icons of your own, for example the four icon files from the root `app/src/main/res/`. Do not edit the root `app/`. This route was not executed for the snapshots after the icons were removed.

## Differences between the snapshots

Only two retained files differ: `InventoryScreen.kt` and `MainActivity.kt` (`git diff --stat 77fe81a 1f95f80`: +99 / −66 lines).

The preview functions differ, so the documented `android preview render --fqn ...` commands apply to Test B only:

| | Test A | Test B |
|---|---|---|
| Preview functions | `InventoryScreenPhonePreview` (360x780 dp), `InventoryScreenTabletPreview` (840x1180 dp) | `InventoryPhonePreview` (411x891 dp), `InventoryTabletPreview` (1280x800 dp) |

The A2 and B2 verification runs both used the Test B code. No verification run was made on Test A's code.

## Provenance and licensing flags

- **No license headers.** None of the 28 files in either original commit has a copyright or license header.
- **Authored by an AI agent.** `InventoryScreen.kt` and `MainActivity.kt` were written by Claude Sonnet 5.5 in Claude Code sessions, under the author's direction. Copyright in AI-generated code is unsettled in some jurisdictions. The repository applies Apache-2.0 to it as a statement of the author's intent, not as a legal conclusion.
- **Launcher icons: omitted.** The 14 original icon files are not redistributed (see above and [`NOTICE`](../NOTICE)).
- **Other template boilerplate: provenance unverified.** The remaining retained files (the manifest, the three theme Kotlin files, `rules.keep`, `colors.xml`, `strings.xml`, `themes.xml`, the backup and data-extraction rules, and the two example tests) were in the initial commit `0202cb3` ("Initial Android Compose benchmark project") and have the layout of a standard Android Compose project template. The tool that generated them is not recorded, and their individual licensing provenance has not been verified. They are kept so the snapshots match their commits, and Apache-2.0 covers them only to the extent the author may license them.
- **Not covered here:** the Gradle wrapper at the repository root is third-party (it carries its own Apache-2.0 header). It is not in these snapshots.
