# Toggle

A JetBrains IDE plugin that toggles the thing under the caret with one keystroke.

- **Booleans:** `true` ↔ `false`, `yes` ↔ `no`, `on` ↔ `off`, …
- **Single-character pairs:** `+` ↔ `-`, `>` ↔ `<`, `&` ↔ `|`, `*` ↔ `/`
- **String quote styles** (language-aware): `"` ↔ `'` (Ruby, Python), `"` ↔ `'` ↔ `` ` `` (JS/TS), `"` ↔ `"""` (Kotlin)
- **Arbitrary-length cycles:** `[nil, false, true]` advances on each press

Configurable globally and per language via **Preferences → Tools → Toggle**.

## Install

Until the plugin is on the JetBrains Marketplace, build locally:

```bash
./gradlew buildPlugin
```

The resulting plugin zip is at `build/distributions/toggle-*.zip`. Install via **Settings → Plugins → Install Plugin from Disk**.

## Usage

1. Bind the action to a shortcut via **Settings → Keymap → Toggle the Thing Under Caret**.
2. Position the caret on a boolean, operator, or quote and press your bound key.
3. Customize toggles via **Preferences → Tools → Toggle**.

Action id: `dev.bartoszmaka.toggle.ToggleAction`.

### IdeaVim

Add a mapping in `~/.ideavimrc`:

```vim
nnoremap <leader>t :action dev.bartoszmaka.toggle.ToggleAction<CR>
```

## Configuration

### Global Settings

Default word groups include `[true, false]`, `[yes, no]`, `[on, off]`, and more. Default character groups include `[+, -]`, `[>, <]`, `[&, |]`, `[*, /]`.

### Per-Language Overrides

Ruby gets `[nil, false, true]` and `[unless, if]` by default. JavaScript gets `[let, const]`, `[var, let]`, `[null, undefined]`. Python gets `[True, False]`, `[None, True, False]`, `[and, or]`. All languages inherit global groups unless you override.

## Development

Built with Kotlin 2.0.21, Gradle 8.10, and the IntelliJ Platform Gradle Plugin 2.1.0 targeting IntelliJ Platform 2024.2.5 Community.

Run tests with `./gradlew test`. Build the plugin with `./gradlew buildPlugin`.

## License

MIT
