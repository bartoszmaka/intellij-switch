# Switch

A JetBrains IDE plugin that switches the thing under the caret with one keystroke.
A port of Andrew Radev's [switch.vim](https://github.com/AndrewRadev/switch.vim) to
the IntelliJ Platform.

- **Booleans:** `true` ↔ `false`, `yes` ↔ `no`, `on` ↔ `off`, …
- **Single-character pairs:** `+` ↔ `-`, `>` ↔ `<`, `&` ↔ `|`, `*` ↔ `/`
- **String quote styles** (language-aware): `'` ↔ `"` ↔ `:symbol` (Ruby), `"` ↔ `'` (Python), `"` ↔ `'` ↔ `` ` `` (JS/TS), `"` ↔ `"""` (Kotlin)
- **Arbitrary-length cycles:** `[nil, false, true]` advances on each press
- **Forward and backwards:** a second action cycles in reverse for 3+ item cycles

Configurable globally and per language via **Preferences → Tools → Switch**.

## Install

Until the plugin is on the JetBrains Marketplace, build locally:

```bash
./gradlew buildPlugin
```

The resulting plugin zip is at `build/distributions/switch-*.zip`. Install via **Settings → Plugins → Install Plugin from Disk**.

## Usage

1. Bind the action to a shortcut via **Settings → Keymap → Switch the Thing Under Caret**.
2. Position the caret on a boolean, operator, or quote and press your bound key.
3. Customize groups via **Preferences → Tools → Switch**.

Action ids: `dev.bartoszmaka.switch.SwitchAction` (forward) and
`dev.bartoszmaka.switch.SwitchBackwardsAction` (reverse).

## Example mappings

### IdeaVim

Add a mapping in `~/.ideavimrc`:

```vim
" Single keystroke
nnoremap <leader>s :action dev.bartoszmaka.switch.SwitchAction<CR>
nnoremap <leader>S :action dev.bartoszmaka.switch.SwitchBackwardsAction<CR>

" Or the switch.vim default
nnoremap gs :action dev.bartoszmaka.switch.SwitchAction<CR>
nnoremap gS :action dev.bartoszmaka.switch.SwitchBackwardsAction<CR>
```

### Native JetBrains keymap

**Settings → Keymap → Switch the Thing Under Caret**, then assign a shortcut.
Common picks:

- macOS: `⌥⇧S`
- Linux / Windows: `Alt+Shift+S`

## Configuration

### Global

Default word groups include `[true, false]`, `[yes, no]`, `[on, off]`, and more.
Default character groups include `[+, -]`, `[>, <]`, `[&, |]`, `[*, /]`.

### Per-Language Overrides

Ruby gets `[nil, false, true]` and `[unless, if]` by default. JavaScript gets
`[let, const]`, `[var, let]`, `[null, undefined]`. Python gets `[True, False]`,
`[None, True, False]`, `[and, or]`. All languages inherit global groups unless
you override.

## Development

Built with Kotlin 2.0.21, Gradle 8.10, and the IntelliJ Platform Gradle Plugin
2.1.0 targeting IntelliJ Platform 2024.2.5 Community.

Run tests with `./gradlew test`. Build the plugin with `./gradlew buildPlugin`.

## License

MIT
