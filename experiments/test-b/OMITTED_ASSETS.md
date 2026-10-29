# Omitted assets: Test B snapshot

This snapshot is **intentionally not byte-identical to the complete original commit**. It omits the 14 launcher icon files listed below. They appeared to be Google's Android robot artwork, and the right to redistribute them under this repository's licenses could not be verified (see the repository's `NOTICE`). Every other file in `app/src/` matches the commit.

| | |
|---|---|
| Experiment | Test B |
| Source commit | `1f95f80274552117509a047bd8f1981985df4be4` ("Experiment B: HomeStock MVP with Android CLI plugin") |
| Original repository | the author's local Git repository `HomeStockBenchmark` (no remote) |
| Files omitted | 14 (paths relative to `app/src/`) |

## Omitted files

| Path | Bytes | SHA-256 | Git blob ID |
|---|---|---|---|
| `main/res/drawable/ic_launcher_background.xml` | 5606 | `ed423c73a6f40a4d2909f0901e60527b3a807cd59e1b5593bcaae1808b1c6321` | `07d5da9cbf141911847041df5d7b87f0dd5ef9d4` |
| `main/res/drawable/ic_launcher_foreground.xml` | 1702 | `01d1a6a6c1234eb7fe270d097eb283d72b9c95ae5118886f1b6573aad280f1f7` | `2b068d11462a4b96669193de13a711a3a36220a0` |
| `main/res/mipmap-anydpi/ic_launcher.xml` | 343 | `88f7653499ef524126ea5018a99baf9cc3269e7e584d6205dfdd76db39f39cc0` | `6f3b755bf50c6b03d8714a9c6184705e6a08389f` |
| `main/res/mipmap-anydpi/ic_launcher_round.xml` | 343 | `88f7653499ef524126ea5018a99baf9cc3269e7e584d6205dfdd76db39f39cc0` | `6f3b755bf50c6b03d8714a9c6184705e6a08389f` |
| `main/res/mipmap-hdpi/ic_launcher.webp` | 1404 | `dd00996198640ed28fbc09cdcd7a3807cf8707f3eb255b659634da3ca6a6ff01` | `c209e78ecd372343283f4157dcfd918ec5165bb3` |
| `main/res/mipmap-hdpi/ic_launcher_round.webp` | 2898 | `1ed73f5341a69d3b41c7e02e126803f50cc8c3284adf4bbb737f0c93577aef07` | `b2dfe3d1ba5cf3ee31b3ecc1ced89044a1f3b7a9` |
| `main/res/mipmap-mdpi/ic_launcher.webp` | 982 | `846219e6f72fe9a6c104ca8919cbee36a101e7d2ff8da9da67b689a5888f060d` | `4f0f1d64e58ba64d180ce43ee13bf9a17835fbca` |
| `main/res/mipmap-mdpi/ic_launcher_round.webp` | 1772 | `4e2c58b91de01130e6479e00cbbaaf6e77cd961dd2c8e303cf13cd077fa92632` | `62b611da081676d42f6c3f78a2c91e7bcedddedb` |
| `main/res/mipmap-xhdpi/ic_launcher.webp` | 1900 | `398340dad816fc9a6338cb151a8cf1e45b926f9bfb70628b24c4bd2523cc94d4` | `948a3070fe34c611c42c0d3ad3013a0dce358be0` |
| `main/res/mipmap-xhdpi/ic_launcher_round.webp` | 3918 | `91b490aef86574901137f0a252272e1f1add99c1ad709e0696ed68929b88d261` | `1b9a6956b3acdc11f40ce2bb3f6efbd845cc243f` |
| `main/res/mipmap-xxhdpi/ic_launcher.webp` | 2884 | `58ae87fa0c5b5d1562d27fd648d2c061553fe20e3ed570bde588162d01ea7a27` | `28d4b77f9f036a47549d47db79c16788749dca10` |
| `main/res/mipmap-xxhdpi/ic_launcher_round.webp` | 5914 | `3009fad079f5772f30ecd767f98924367fbe0f81c30048d672d4fda2d5ca7d12` | `9287f5083623b375139afb391af71cc533a7dd37` |
| `main/res/mipmap-xxxhdpi/ic_launcher.webp` | 3844 | `f98fef5bc3bfe5b65692c40ad1cbae2bec4faf9f1b249c397626740db71b62d8` | `aa7d6427e6fa1074b79ccd52ef67ac15c5637e85` |
| `main/res/mipmap-xxxhdpi/ic_launcher_round.webp` | 7778 | `5faf033745c8c882c43bb1b372d4e4a5890caec63d9892a2e756c954108b85dc` | `9126ae37cbc3587421d6889eadd1d91fbf1994d4` |

Two of the files have identical content (the same SHA-256 appears twice), so there are 13 distinct hashes.

## Verifying against the original commit

Anyone with the original repository can check an omitted file, for example:

```bash
git show 1f95f80274552117509a047bd8f1981985df4be4:app/src/main/res/mipmap-hdpi/ic_launcher.webp | shasum -a 256
```

The output should equal the SHA-256 in the table. The hashes and blob IDs identify the files and do not reproduce them.

## Consequences

- This snapshot is source only. The Gradle files are not included, so it was never buildable on its own.
- To build it with the root project's Gradle configuration, add any launcher icon of your own. The manifest refers to `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round`. The root `app/` of this repository does exactly that, using an original icon.
- The icons are not drawn by the Compose previews. The benchmark's rendered images and component trees do not depend on them (see `experiments/README.md`).
