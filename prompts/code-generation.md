# Code-Generation Prompts (Test A and Test B)

The prompts below were recovered verbatim from the Claude Code session transcripts of the original runs. The transcripts store each prompt inside a `<pasted_content>` wrapper tag, which is omitted here. The text between the tags is unchanged. The transcripts themselves are private and are not included in this repository.

## Provenance

| Experiment | Tool constraint | Project folder / branch | Session | Start (UTC) | Model |
|---|---|---|---|---|---|
| Test A | Android CLI forbidden | `HomeStock-TestA` / `experiment/claude-only` | `43b73c49` | 2026-10-29 13:02 | `claude-sonnet-5-5` |
| Test B | Android CLI plugin allowed | `HomeStock-TestB` / `experiment/android-cli` | `7cb1404f` | 2026-10-29 13:10 | `claude-sonnet-5-5` |

Each experiment is a single prompt in a single session, with no follow-up prompts. The resulting commits are `77fe81a` (A, "Experiment A: HomeStock MVP without Android CLI") and `1f95f80` (B, "Experiment B: HomeStock MVP with Android CLI plugin"), both on top of `0202cb3`.

The two prompts are identical except for the tool-constraint paragraph and the final sentence of the closing paragraph.

## Test A prompt

```text
Implement a basic household inventory screen in this existing Android project.

Requirements:
- Use Kotlin, Jetpack Compose, and Material 3.
- Display sample household products in six categories: Legumes & Grains, Vegetables, Fruits, Snacks, Drinks, and Household Essentials.
- Show each product's name, category, and current quantity.
- Allow users to increase or decrease quantities using + and - buttons. Quantities must never be negative.
- Allow users to add a new product with a name, category, and initial quantity.
- Clearly indicate when a product is out of stock.
- Use in-memory state only. No database, authentication, networking, or new dependencies.
- Create Compose Previews for both phone and tablet screen sizes.
- Preserve the existing project configuration.
- Run the Gradle debug build and fix any compilation errors.

Keep the implementation simple and limited to this single-screen MVP.

Do not use Android CLI, its commands, or Android-specific agent plugins. Use standard Gradle and existing development tools only.

When finished, summarize the implementation, the verification steps, and any limitations.
```

## Test B prompt

```text
Implement a basic household inventory screen in this existing Android project.

Requirements:
- Use Kotlin, Jetpack Compose, and Material 3.
- Display sample household products in six categories: Legumes & Grains, Vegetables, Fruits, Snacks, Drinks, and Household Essentials.
- Show each product's name, category, and current quantity.
- Allow users to increase or decrease quantities using + and - buttons. Quantities must never be negative.
- Allow users to add a new product with a name, category, and initial quantity.
- Clearly indicate when a product is out of stock.
- Use in-memory state only. No database, authentication, networking, or new dependencies.
- Create Compose Previews for both phone and tablet screen sizes.
- Preserve the existing project configuration.
- Run the Gradle debug build and fix any compilation errors.

Keep the implementation simple and limited to this single-screen MVP.

You may use the installed Android CLI plugin and its tools when useful.

When finished, summarize the implementation, the verification steps, and any limitations. Include which Android CLI commands or plugin tools were used, if any.
```

## What the transcripts record about each run

Everything here comes from the transcripts' tool calls and final messages.

| | Test A | Test B |
|---|---|---|
| Tool calls | 3 `Bash` | 4 `Bash`, 1 `Skill`, 1 `Read` |
| Android CLI use | None | Loaded the `android-cli:android-cli` skill and ran `which android`. No other `android` command was run. |
| Gradle | `./gradlew assembleDebug -q`, run at the end of the file-writing command. The agent reported no errors. | `./gradlew assembleDebug -q` at the end of the file-writing command (its output was not checked), then `./gradlew assembleDebug`, which printed `BUILD SUCCESSFUL in 352ms` and `36 actionable tasks: 36 up-to-date`. |
| Previews rendered | No. The agent said it did not launch the app or render the previews. | No. The agent said the same. |
| Agent-reported previews | `InventoryScreenPhonePreview` 360x780 dp and `InventoryScreenTabletPreview` 840x1180 dp (the agent's summary) | `InventoryPhonePreview` 411x891 dp and `InventoryTabletPreview` 1280x800 dp |

The Test B agent's summary of its Android CLI use did not name any CLI command beyond the skill load and `which android`. Whether the skill load counts as "using the plugin" is a judgement left to the reader.

### Model, timing, tokens and cost

These are the cumulative `cost-state` records stored in each transcript. **They cover the whole session.** Each session here is a single prompt, so the scope is that one task.

| | Test A (`43b73c49`) | Test B (`7cb1404f`) |
|---|---|---|
| Sonnet input / output tokens | 514 / 5,376 | 518 / 6,631 |
| Sonnet thinking tokens | 282 | 499 |
| Cache read / cache creation tokens | 189,841 / 20,897 | 280,320 / 30,806 |
| Recorded session cost (USD, `totalCostUSD`, all models) | 0.1575 | 0.2188 |
| Of which Sonnet (`costUSD`) | 0.1574 | 0.2186 |
| `totalAPIDuration` | 46.8 s | 49.7 s |
| `totalToolDuration` | 2.0 s | 2.2 s |
| `totalDuration` | 337.4 s | 277.8 s |

- **Models.** Every agent turn in both transcripts (6 and 10 assistant messages) and every recorded API request (`repl_main_thread`: 4 and 5) used `claude-sonnet-5-5`. The usage records also list `claude-haiku-5-5`: 1,560 input and 16 output tokens in Test A, 1,560 and 19 in Test B (about USD 0.0002 each). Those Haiku calls match no recorded request, and their purpose is not recorded. The token, cache and thinking rows above are the Sonnet figures. The first cost row is the session total and so includes the Haiku cost; the second row is Sonnet only.
- The cost is the transcript's own figure. It was not recomputed.
- I did not verify how Claude Code defines `totalDuration`. It is larger than the message timestamps span (about 81 s for Test A, 49 s for Test B), so it probably includes time outside the agent's turn. Treat it as a session figure, not as the time the agent took.
- No timing was shown to the agent or measured by the agent in these two runs.

## Limits

- One run per prompt, so the A and B differences in tokens, cost and code size are not evidence of a systematic effect.
- The prompts differ in more than the CLI permission: the closing sentence in B also asks the agent to report CLI use.
- The generated code differs between A and B. `git diff --stat 77fe81a 1f95f80` shows `InventoryScreen.kt` and `MainActivity.kt` changed (+99 / −66). No quality comparison of the two implementations has been done.
