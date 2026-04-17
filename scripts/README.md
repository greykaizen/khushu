# Khushu5 Content Toolchain

Tools to verify hadith/ayah references and generate the pre-packaged `khushu.db`.

---

## Step 0 — Download datasets into `scripts/data/`

### Quran (Tanzil Uthmani)

```bash
curl -L "https://raw.githubusercontent.com/semarketir/quranjson/master/source/surah/surah_1.json" -o scripts/data/test.json
# Full dataset:
curl -L "https://raw.githubusercontent.com/risan/quran-json/main/data/quran.json" -o scripts/data/quran-uthmani.json
```

Expected format:
```json
[ { "id": 1, "name": "Al-Fatihah", "verses": [ { "id": 1, "text": "بِسْمِ..." } ] } ]
```

> If the format differs, adjust the `load_tanzil()` indexing in `verify_manifest.py` to match.

### Hadith (Fawaz Ahmed CDN)

Download only the collections referenced in the topic files:

```bash
mkdir -p scripts/data

# Sahih al-Bukhari
curl -L "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/eng-bukhari.min.json" -o scripts/data/en.bukhari.json

# Sahih Muslim
curl -L "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/eng-muslim.min.json" -o scripts/data/en.muslim.json

# Abu Dawud
curl -L "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/eng-abudawud.min.json" -o scripts/data/en.abudawud.json

# Tirmidhi
curl -L "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/eng-tirmidhi.min.json" -o scripts/data/en.tirmidhi.json

# Ibn Majah
curl -L "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/eng-ibnmajah.min.json" -o scripts/data/en.ibnmajah.json
```

Expected format:
```json
{ "hadiths": [ { "hadithnumber": 1, "text": "...", "grades": [ { "grade": "Sahih" } ] } ] }
```

---

## Step 1 — Verify references

```bash
python3 scripts/verify_manifest.py
```

- Reads all `app/src/main/assets/learn/*.json` files
- For each `hadith` block: looks up in local Fawaz Ahmed JSON, checks grade
  - ✅ Sahih → flips `"verified": true`, prints first 150 chars of text
  - ⚠️  Other grade → prints warning, leaves `verified: false`
  - ❌ Not found → prints error
- For each `ayah` block: confirms surah/ayah exists in Tanzil JSON → flips `"verified": true`
- Overwrites JSON files in place

**After running:** read the printed hadith text snippets and confirm each is contextually
relevant to its topic. This human step cannot be automated — relevance requires understanding.

---

## Step 2 — Seed the database

```bash
python3 scripts/seeder.py
```

- Hard-fails if any `hadith` block has `"verified": false` — fix before proceeding
- Extracts only the referenced ayahs + surahs from Tanzil (not the full Quran)
- Extracts only referenced hadiths from Fawaz Ahmed, enforces `grade === "Sahih"`
- Applies tajweed markup overrides from `scripts/data/tajweed_overrides.json` if present
- Output: `app/src/main/assets/khushu.db`

---

## Tajweed markup overrides

The `tajweed_markup` column in the `ayahs` table is populated from
`scripts/data/tajweed_overrides.json`. Format:

```json
{
  "1:1": "بِسْمِ اللَّهِ الرَّحْمَ{madd}ٰ{/madd}نِ الرَّحِيمِ",
  "1:2": "الْحَمْدُ لِلَّهِ رَبِّ الْعَ{madd}ا{/madd}لَمِينَ",
  "surah:ayah": "markup string with {rule}text{/rule} tags"
}
```

This file is hand-curated. The seeder reads it and injects the markup into the DB
alongside the Tanzil text. Without this file, all `tajweed_markup` values are null.

---

## Workflow summary

```
Download datasets → verify_manifest.py → human reads output
     → seeder.py → app/src/main/assets/khushu.db → build app
```

Re-run both scripts whenever topic JSON files are updated.
