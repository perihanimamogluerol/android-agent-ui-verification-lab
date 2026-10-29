# Artifact Provenance

Files are copied unmodified, with their original filenames, from the session scratch folders of the original runs. Session IDs are the Claude Code session identifiers (first 8 characters). Times are UTC, from the session transcripts.

| File | Experiment | Session | Produced by | Created (local, UTC+1) |
|---|---|---|---|---|
| `preview-a-no-cli/phone.png` | A2 (no Android CLI) | `6fbc5116` | `adb exec-out screencap -p` on the `Pixel_9a` emulator, with `wm size 1080x2339` | 14:32 |
| `preview-a-no-cli/tablet.png` | A2 (no Android CLI) | `6fbc5116` | same, with `wm size 1680x1050`, `wm density 210` | 14:32 |
| `preview-b-android-cli/Phone.png` | B2, assisted retry 1 | `5626be34` | `android preview render :app --fqn ...InventoryPhonePreview` | 14:38 |
| `preview-b-android-cli/Tablet.png` | B2, assisted retry 1 | `5626be34` | same, for `InventoryTabletPreview` | 14:38 |
| `preview-b-android-cli/component-tree/InventoryPhonePreview_0.json` | B2, assisted retry 2 | `5626be34` | `android preview render :app --component-tree` | 14:41 |
| `preview-b-android-cli/component-tree/InventoryTabletPreview_0.json` | B2, assisted retry 2 | `5626be34` | same | 14:41 |

SHA-256 checksums:

```
b1b5f1f52bfa0744f6a21d9e2d8e1132188ec861f80c0b4e3493db319223a938  preview-a-no-cli/phone.png
b010b1aaaa7481a3659c0d2603dbad84cba56d0101decc003d109ecd96593c28  preview-a-no-cli/tablet.png
d930961ef6b729862053aa7e8349b141b991522303a5f5c71d5bfe2b371f03ea  preview-b-android-cli/Phone.png
3df3226c21b217d882b93f2e4ef29aad6f7baef9c7c99e96abd73a8ae5f5f8bb  preview-b-android-cli/Tablet.png
0773329b8dc379498ed34108753b8e6bc04b05af6df308fda235ff8534887ec4  preview-b-android-cli/component-tree/InventoryPhonePreview_0.json
6b6da9ac48039fb72009af53a4028f46e8bf3996d2373abf488894f33a50dceb  preview-b-android-cli/component-tree/InventoryTabletPreview_0.json
```

## Notes

- **The two image sets are not the same kind of render.** The A2 images are on-device screenshots of `PreviewActivity` on an emulator, so they include the status bar and gesture bar, and the window size was set by `wm size`/`wm density`. The B2 images come from the Android CLI renderer (1079x2339 and 3360x2100 px). Do not compare them pixel for pixel.
- **Reproducibility.** On 2026-10-29 the render commands were rerun in a scratch copy of the Test B project. The new `Phone.png` and `Tablet.png` have the same SHA-256 as the stored ones. The two JSON files had the same size but different bytes: the order of child nodes and of `actions` entries varies between runs. After sorting the action lists and ignoring child order, all 267 (phone) and 365 (tablet) nodes matched. Treat node order in the JSON as unstable, even when the semantic nodes match.
- The `--component-tree` run also wrote `InventoryPhonePreview_0.png` and `InventoryTabletPreview_0.png`. They are byte-identical to `Phone.png` and `Tablet.png` and were not copied again.
- **Privacy check.** The JSON files contain no absolute paths, usernames, e-mail addresses or credential-like strings. The PNGs have no embedded text metadata, and the A2 screenshots show only the emulator status bar (clock and system icons).
- The A2 screenshots show the app as built in `HomeStock-PreviewA`, which is at commit `1f95f80` (Experiment B code). The A2 run therefore reviewed the same code as B2, and differs only in the tools allowed.
