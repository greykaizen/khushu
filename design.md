# Khushu — Design Documentation

> Sourced from `figma/` exported images + `nav-bar.md`. Use this as the single reference to avoid re-hitting Figma MCP limits.
> Last updated: 2026-03-29

---

## 1. Global Design Language

### Theme
- **Background**: Pure black (`#000000`) across all main screens
- **Text (primary)**: White, full opacity for active/prominent content
- **Text (muted)**: White at ~40–50% alpha for secondary/inactive content
- **Overall feel**: Minimal, distraction-free, high contrast

### Typography
- **App bar titles** (Salah, Tasbeeh, Learn): `Antonio` font (in `/res/font/`) — bold display, with a short underline accent beneath
- **Salah rakat picker numbers**: `Antonio` font — large display numerals
- **Immersive counter numbers**: `Antonio` font — varies by preset style (see §6)
- **Nav bar labels**: `Be Vietnam Pro Medium`
- **All other English text**: `Be Vietnam Pro` (body, lists, hints, labels)
- **Hint text**: Small, muted (e.g., "Tap on the screen to Start")

### Colors
- **Not hardcoded** — Figma used purple/teal as placeholders only
- **Dynamic Color (Material You)**: primary/secondary/tertiary derived from wallpaper on Android 12+ (`dynamicColorScheme`)
- **User-selectable palette** in Settings as fallback / override
- Fixed values:
  | Role | Value |
  |---|---|
  | Background | `#000000` |
  | Primary text | `#FFFFFF` |
  | Muted text | White ~40% alpha |
  | Onboarding bg | Deep emerald gradient with glowing bokeh orbs (not user-thenable) |
  | Tasbeeh card colors | Pastel palette per collection — may be user-assigned or auto-generated |
  | Learn card colors | Per-category color — may follow same palette system as Tasbeeh |

### Shared App Bar Pattern (all 3 main screens)
- Left: Page title in `Antonio` font + short underline accent bar beneath
- Right: Rounded-square button (hamburger `≡` icon, color follows theme) → opens bottom sheet menu

---

## 2. Navigation Bar

> Full spec in `nav-bar.md`. Summary here for quick reference.

- **Shape**: Floating pill, centered horizontally, `30.dp` from bottom edge
- **Surface**: True **glassmorphism** — background blur (`RenderEffect` / `BlurMaskFilter`, ~20dp radius) + `surfaceVariant` at low alpha overlay + thin white border at ~12% alpha. `tonalElevation = 6.dp`, `shadowElevation = 16.dp`
- **Tabs**: SALAH · TASBEEH · LEARN
- **Unselected tab**: Icon only (`28.dp`, muted color)
- **Selected tab**: Text label (`Be Vietnam Pro Medium`, primary color) + subtle primary-colored background pill (alpha = 0.12f)
- **Tab transition**: `AnimatedContent` with `slideInVertically` + `fadeIn/Out` — "shooter" effect (up on select, down on deselect)
- **Horizontal padding**: animates `18.dp → 28.dp` on selection (pill expands)
- **Sliding indicator**: `25.dp × 3.dp` rounded bar, slides to active tab center via spring (`DampingRatioLowBouncy` + `StiffnessLow`), positioned absolutely at bottom of surface
- **No third-party nav library** — pure Compose + `rememberSaveable`

---

## 3. Salah Screen

### Layout
```
[Salah]                         [≡ settings btn]
  ─────

          (prev rakat — muted, smaller)   Antonio font
               3
          (current rakat — large, white)
               4
          (next rakat — muted, smaller)
               5

        "Tap on the screen to Start"

[floating pill nav bar]
```

### Details
- **Cupertino-style drum-roll picker** (not a counter): user scrolls to select how many rakats they will pray (e.g. 2, 3, 4)
  - Previous value shown above — muted, smaller
  - Selected value center — large, full white, `Antonio` font
  - Next value shown below — muted, smaller
- "Tap on the screen to Start" hint text shown while on picker
- **Tap anywhere** → exits picker, enters immersive counting mode

### Immersive Mode (Salah counting)
- Fullscreen, no nav bar, pure black
- Counter **starts at 0**, increments on each tap, counts **up to the selected rakat number**
- On reaching target → **5-second completion animation** ("Salah ended" / completion state)
- After animation → returns to Salah screen (or home)
- 5 visual style presets available — see §6

---

## 4. Tasbeeh Screen

### 4a. List View (main screen)

```
[Tasbeeh]                       [≡ purple FAB]
  ─────
                                [+ Create] (pill button, light purple)

┌─────────────────┐  ┌─────────────────┐
│ Post-Prayer     │  │ Tashbeeh .....   │
│ Tasbeeh    33   │  │ Tasbeeh long.. 33│
│ Tasbeeh    33   │  │ Tasbeeh      33  │
│ Tasbeeh    34   │  │ Tasbeeh      34  │
│ ...         ..  │  │ ...          ..  │
└─────────────────┘  └─────────────────┘
┌─────────────────┐  ┌─────────────────┐
│ Tasbeeh long.. 33│  │                 │
│ Tasbeeh      33 │  │   (empty card)  │
│ Tasbeeh      34 │  │                 │
└─────────────────┘  └─────────────────┘
┌─────────────────┐  ┌─────────────────┐
│  (empty card)   │  │   (empty card)  │
└─────────────────┘  └─────────────────┘
┌─────────────────┐  ┌─────────────────┐
│  (empty card)   │  │   (empty card)  │
└─────────────────┘  └─────────────────┘

[floating pill nav bar]
```

### Card Spec
- **Layout**: 2-column grid, equal width cards
- **Corner radius**: ~18–20dp
- **Colors**: Each card has a unique pastel/muted color (warm tan, sage, teal, blue-grey, mauve — assigned per collection)
- **Content** (when populated):
  - Collection name: bold white, top-left
  - List rows: `Tasbeeh name    count` — truncated with `...` if long name
  - Overflow rows indicated with `...` and `..`
- **Empty cards**: Just the background color, no text
- **"+ Create" button**: Pill shape, light purple tint, top-right of content area (not the FAB)

### 4b. Card Active Screen (Immersive Counting)

Full-screen, no nav bar, pure black background.

```
Tasbeeh 1        Done
Tasbeeh 2        33      ← active item (bold)
Tasbeeh 3        34


              4

           out of 33
```

- **Top section**: List of all tasbeeh in the set — name on left, count/status on right
  - Active item shown in **bold**
  - Completed items show "Done"
  - Remaining items show target count
- **Center**: Current count number — very large display numeral
- **Below**: "out of N" subtitle
- **Interaction**: Tap anywhere to increment
- No nav bar, no header buttons

### 4c. Exit Overlay (long press)

Same layout as active screen, but a pill tooltip appears at the top:
```
[ Hold to Reset or Swipe Down to Cancel ]
```
- Pill shape, light surface background, dark text
- Appears on long press during counting

---

## 5. Learn Screen

### 5a. List View

```
[Learn]                         [≡ purple FAB]
  ─────

Prayer ─────────────────────────────────

┌─────────────────┐  ┌─────────────────┐
│ How to Pray     │  │                 │
│ Lorem ipsum     │  │  (teal card)    │
│                 │  │                 │
└─────────────────┘  └─────────────────┘
┌─────────────────┐  ┌─────────────────┐
│  (teal card)    │  │  (teal card)    │
└─────────────────┘  └─────────────────┘

Dua's ──────────────────────────────────

┌─────────────────┐  ┌─────────────────┐
│  (olive card)   │  │  (olive card)   │
└─────────────────┘  └─────────────────┘
┌─────────────────┐
│  (olive card)   │
└─────────────────┘

[floating pill nav bar]
```

### Details
- **Section headers**: Section name + full-width horizontal divider line (white, low alpha)
- **Card colors**:
  - Prayer section: Sage/teal green
  - Dua's section: Olive/khaki
- **First card** shows title ("How to Pray") + subtitle ("Lorem ipsum") — others may be image-only or content-only
- **Corner radius**: ~18–20dp (matching Tasbeeh cards)
- **Categories**: At minimum "Prayer" and "Dua's" — likely more

### 5b. Card Open (Reader View)

Full-screen content reader:
- Shows Arabic text (Quran ayat / hadith)
- Translation text below
- Top navigation bar with back button
- Clean reading layout, likely scrollable
- Content appears to be paginated (e.g., "Ayah 5:1" visible)

---

## 6. Salah Immersive Mode — Style Presets

All presets: pure black background, no nav bar, full-screen.

| Preset | Label | Number style | Layout |
|---|---|---|---|
| **1** | None | Medium-large, centered, solid light grey fill | Minimal — just the number, dead center |
| **2** | "Salah" left-aligned | Outline/stroke only, large, positioned mid-right | Label left, number right |
| **3** | "Salah" top-left corner (small) | Massive solid fill, dominates screen | Number takes up ~70% of screen height |
| **4** | "Salah" top-left (regular size) | Large outline, slightly smaller than Preset 3 | Number center-right area |
| **5** | "Clock" top-left + Arabic "صلاة" center | Large outline, bottom-right corner | Most complex — label + Arabic word + number |

### Exit Overlay (all presets)
- Pill tooltip at top: `"Hold to Reset or Swipe Down to Cancel"`
- Light surface, dark text, rounded pill shape

---

## 7. Bottom Sheet (Menu)

Triggered by the purple `≡` FAB in any main screen header.

```
         ┌─────────────────────────┐
    ─────┤                         ├─────
         │   Setting               │
         │   Customize             │
         │   About                 │
         │                         │
         │   ○  ○  ○               │  ← 3 circular action buttons
         │                         │
         │         v1.8.5          │
         └─────────────────────────┘
```

- **Style**: Light/white rounded modal bottom sheet over dimmed background
- **Items**: Setting · Customize · About (large text, tappable rows)
- **Bottom**: 3 circular icon buttons (likely social links / share / feedback)
- **Version**: `v1.8.5` shown at bottom

---

## 8. Onboarding Flow (3 screens)

All screens share: **deep emerald green gradient background** with glowing bokeh orbs (light leak effect, very lush).

### Screen 1 — Splash/Welcome
```
Khushu•

Your prayer deserves
your full heart.

  "some hadees"


                    [Continue]
```
- "Khushu" large serif/display bold, white
- Dot `•` after "Khushu" in **teal/mint** accent color
- Tagline: "Your _prayer_ deserves your full heart." — "prayer" has italic underline emphasis
- Hadith quote beneath (italic, smaller, muted)
- "Continue" pill button — bottom-right, teal background, white text

### Screen 2 — Setup/Before App
```
Khushu•

Before we get into
the app


                    [Continue]
```
- Smaller "Khushu•" top-left
- Large statement text center
- Same "Continue" button
- (Likely followed by permission requests or madhab/setting setup)

### Screen 3 — Same as Screen 1
- Identical to Screen 1 (may be a repeat or slight variation in flow)

---

## 9. Key Interaction Patterns

| Pattern | Description |
|---|---|
| Tap to count | Salah screen and Tasbeeh active screen — full-screen tap increments counter |
| Long press | Shows "Hold to Reset or Swipe Down to Cancel" overlay |
| Swipe down | Cancels/exits immersive counting mode |
| Drum-roll counter | Salah screen shows prev/current/next rak'ah in vertical scroll style |
| Nav switching | Pill nav bar with spring-animated sliding indicator + shooter tab animation |
| Bottom sheet | Menu via purple `≡` FAB — Setting, Customize, About |

---

## 10. Assets Needed

| Asset | Usage |
|---|---|
| `ic_salah` | Nav bar icon |
| `ic_tasbeeh` | Nav bar icon |
| `ic_learn` | Nav bar icon |
| `ic_menu` (hamburger `≡`) | Header FAB on all 3 screens |
| `ic_add` / `+` | "Create" button in Tasbeeh |
| Arabic font | Preset 5 uses Arabic "صلاة" |
| `Be Vietnam Pro` (Medium, Bold) | Nav labels, page titles |
