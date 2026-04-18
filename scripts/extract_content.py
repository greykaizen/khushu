#!/usr/bin/env python3
"""
Khushu5 Content Extractor
=========================
Downloads Quran text, tajweed, translations, audio manifests, and hadith
from quran.com and sunnah.com APIs for offline use in the app.

Usage (from project root):
    python3 scripts/extract_content.py

Outputs to scripts/data/ — does NOT write to app/src/main/assets/ directly.
Resumable: skips files that already exist.

Sources:
  - Quran text, tajweed, translations, audio → api.quran.com (OAuth2)
  - Hadith (Bukhari, Muslim) → api.sunnah.com
"""

import json
import os
import re
import sys
import time
from pathlib import Path

try:
    import requests
except ImportError:
    print("Installing requests...")
    import subprocess
    subprocess.check_call([sys.executable, "-m", "pip", "install", "requests", "-q"])
    import requests

# ── .env loader (no python-dotenv required) ────────────────────────────────────

def load_env():
    env_file = Path(__file__).parent.parent / ".env"
    if not env_file.exists():
        print("ERROR: .env not found at project root")
        sys.exit(1)
    for line in env_file.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            k, _, v = line.partition("=")
            os.environ.setdefault(k.strip(), v.strip())

load_env()

QURAN_CLIENT_ID     = os.environ["QURAN_CLIENT_ID"]
QURAN_CLIENT_SECRET = os.environ["QURAN_CLIENT_SECRET"]
QURAN_OAUTH_BASE    = os.environ["QURAN_ENDPOINT"]       # https://oauth2.quran.foundation
QURAN_API_BASE      = "https://api.quran.com/api/v4"
SUNNAH_API_BASE     = "https://api.sunnah.com/v1"

# Public demo key from sunnah.com docs — fine for one-time bulk extraction
SUNNAH_API_KEY = "SqD712P3E82xnwOAEOkGd5JZH8s9wRR24TqNFzjk"

DATA_DIR  = Path(__file__).parent / "data"
QURAN_DIR = DATA_DIR / "quran"
TRANS_DIR = QURAN_DIR / "translations"
AUDIO_DIR = QURAN_DIR / "audio"
HAD_DIR   = DATA_DIR / "hadith"

# Translation catalog: file_key -> quran.com numeric ID
TRANSLATIONS = {
    "en_20":  ("Sahih International",         "en"),
    "en_19":  ("Pickthall",                   "en"),
    "ur_54":  ("Junagarhi (Urdu)",            "ur"),
    "ur_234": ("Jalandhri (Urdu)",            "ur"),
    "tr_77":  ("Diyanet (Turkish)",           "tr"),
    "fr_31":  ("Hamidullah (French)",         "fr"),
    "de_27":  ("Bubenheim (German)",          "de"),
    "id_33":  ("Kemenag (Indonesian)",        "id"),
    "bn_213": ("Dr. Abu Bakr (Bengali)",      "bn"),
    "ru_45":  ("Kuliev (Russian)",            "ru"),
}

# Reciters: file_key -> quran.com reciter_id
RECITERS = {
    "mishari":    7,
    "abdulbaset": 2,
    "sudais":     3,
    "husary":     6,
    "minshawi":   9,
}

HADITH_COLLECTIONS = ["bukhari", "muslim"]

# ── HTTP helpers ───────────────────────────────────────────────────────────────

def http_get(url: str, headers: dict = None, params: dict = None,
             retry: int = 4, delay: float = 0.2) -> dict:
    for attempt in range(retry):
        try:
            r = requests.get(url, headers=headers or {}, params=params or {}, timeout=60)
            if r.status_code == 429:
                wait = 10 * (attempt + 1)
                print(f"\n  Rate limited — waiting {wait}s...")
                time.sleep(wait)
                continue
            if r.status_code in (401, 403):
                raise RuntimeError(f"Auth error {r.status_code} on {url}")
            r.raise_for_status()
            time.sleep(delay)
            return r.json()
        except RuntimeError:
            raise
        except Exception as e:
            if attempt == retry - 1:
                raise
            time.sleep(2 ** attempt)
    return {}

def save(path: Path, data, label: str = None):
    path.parent.mkdir(parents=True, exist_ok=True)
    text = json.dumps(data, ensure_ascii=False, separators=(",", ":"))
    path.write_text(text, encoding="utf-8")
    kb = path.stat().st_size // 1024
    name = label or path.name
    print(f"  ✓ {name} — {kb}KB")

def skip(path: Path, label: str = None) -> bool:
    if path.exists():
        print(f"  → {label or path.name} exists, skipping")
        return True
    return False

def strip_html(text: str) -> str:
    text = re.sub(r"<[^>]+>", "", text)
    text = re.sub(r"\s+", " ", text)
    return text.strip()

# ── quran.com auth (auto-refresh before expiry) ────────────────────────────────

_token: str = None
_token_expiry: float = 0.0

def get_token() -> str:
    global _token, _token_expiry
    if _token and time.time() < _token_expiry - 60:
        return _token
    r = requests.post(
        f"{QURAN_OAUTH_BASE}/oauth2/token",
        auth=(QURAN_CLIENT_ID, QURAN_CLIENT_SECRET),
        data={"grant_type": "client_credentials"},
        timeout=30,
    )
    r.raise_for_status()
    data = r.json()
    _token = data["access_token"]
    _token_expiry = time.time() + data.get("expires_in", 3600)
    return _token

def qapi(path: str, params: dict = None) -> dict:
    return http_get(
        f"{QURAN_API_BASE}/{path}",
        headers={"Authorization": f"Bearer {get_token()}"},
        params=params,
    )

# ── 1. Chapter metadata ────────────────────────────────────────────────────────

def extract_chapters():
    out = QURAN_DIR / "chapters.json"
    if skip(out, "chapters.json"): return
    print("Downloading chapter metadata...")
    data = qapi("chapters", {"language": "en"})
    chapters = [
        {
            "id":              c["id"],
            "nameSimple":      c["name_simple"],
            "nameArabic":      c["name_arabic"],
            "versesCount":     c["verses_count"],
            "revelationPlace": c.get("revelation_place", ""),
            "revelationOrder": c.get("revelation_order", 0),
            "pages":           c.get("pages", []),
        }
        for c in data.get("chapters", [])
    ]
    save(out, chapters, "chapters.json")

# ── 2. Quran text scripts ──────────────────────────────────────────────────────

SCRIPT_CONFIGS = {
    "uthmani":         ("uthmani",         "text_uthmani"),
    "indopak":         ("indopak",         "text_indopak"),
    "uthmani_tajweed": ("uthmani_tajweed", "text_uthmani_tajweed"),
    "uthmani_simple":  ("uthmani_simple",  "text_uthmani_simple"),
    "imlaei":          ("imlaei",          "text_imlaei"),
}

def extract_script(script_key: str):
    endpoint, field = SCRIPT_CONFIGS[script_key]
    out = QURAN_DIR / f"{script_key}.json"
    if skip(out, f"{script_key}.json"): return
    print(f"Downloading {script_key} script...")
    result = {}
    for ch in range(1, 115):
        data = qapi(f"quran/verses/{endpoint}", {"chapter_number": ch, "per_page": 300})
        for v in data.get("verses", []):
            result[v["verse_key"]] = v.get(field, "")
        print(f"  {ch}/114 ", end="\r", flush=True)
    print()
    save(out, result, f"{script_key}.json")

# ── 3. Translations ────────────────────────────────────────────────────────────

def extract_translations():
    for key, (name, lang) in TRANSLATIONS.items():
        out = TRANS_DIR / f"{key}.json"
        if skip(out, f"translations/{key}.json"): continue
        translation_id = int(key.split("_")[1])
        print(f"Downloading {name}...")
        result = {}
        for ch in range(1, 115):
            data = qapi(
                f"verses/by_chapter/{ch}",
                {"translations": translation_id, "fields": "verse_key", "per_page": 300},
            )
            for v in data.get("verses", []):
                key_verse = v["verse_key"]
                translations = v.get("translations", [{}])
                text = translations[0].get("text", "") if translations else ""
                result[key_verse] = strip_html(text)
            print(f"  {ch}/114 ", end="\r", flush=True)
        print()
        save(out, result, f"translations/{key}.json")

# ── 4. Audio manifests (chapter-level MP3 URLs) ────────────────────────────────

def extract_audio():
    for name, reciter_id in RECITERS.items():
        out = AUDIO_DIR / f"{name}.json"
        if skip(out, f"audio/{name}.json"): continue
        print(f"Downloading audio URLs — {name}...")
        result = {}
        for ch in range(1, 115):
            data = qapi(f"chapter_recitations/{reciter_id}/{ch}")
            url = data.get("audio_file", {}).get("audio_url")
            if url:
                result[str(ch)] = url
            print(f"  {ch}/114 ", end="\r", flush=True)
        print()
        save(out, result, f"audio/{name}.json")

# ── 5. Hadith (sunnah.com) ─────────────────────────────────────────────────────

def convert_hadith_from_fawaz(collection: str):
    """Convert pre-existing Fawaz Ahmed hadith JSON to our flat format."""
    out = HAD_DIR / f"{collection}.json"
    if skip(out, f"hadith/{collection}.json"): return
    src = DATA_DIR / f"en.{collection}.json"
    if not src.exists():
        print(f"  ✗ Source {src.name} not found — skipping")
        return
    print(f"Converting {collection} from Fawaz dataset...")
    raw = json.loads(src.read_text(encoding="utf-8"))
    sections = raw.get("metadata", {}).get("sections", {})
    result = []
    for h in raw.get("hadiths", []):
        ref = h.get("reference", {})
        book_num = str(ref.get("book", ""))
        chapter_name = sections.get(book_num, "")
        grades = h.get("grades", [])
        grade = grades[0].get("grade", "Sahih") if grades else "Sahih"
        result.append({
            "id":         h.get("hadithnumber"),
            "collection": collection,
            "chapter":    chapter_name,
            "textEn":     strip_html(h.get("text", "")),
            "textAr":     "",
            "grade":      grade,
            "narrator":   None,
        })
    print(f"  {len(result)} hadiths")
    save(out, result, f"hadith/{collection}.json")

# ── Main ───────────────────────────────────────────────────────────────────────

def main():
    print("=" * 50)
    print("  Khushu5 Content Extractor")
    print("=" * 50)
    print()

    print("Authenticating with quran.com (production)...")
    try:
        get_token()
        print("  ✓ Token obtained\n")
    except Exception as e:
        print(f"  ✗ Auth failed: {e}")
        sys.exit(1)

    steps = [
        ("Chapter metadata",       extract_chapters),
        ("Uthmani script",         lambda: extract_script("uthmani")),
        ("IndoPak script",         lambda: extract_script("indopak")),
        ("Uthmani Tajweed",        lambda: extract_script("uthmani_tajweed")),
        ("Uthmani Simple",         lambda: extract_script("uthmani_simple")),
        ("Imlaei script",          lambda: extract_script("imlaei")),
        ("Translations (10 lang)", extract_translations),
        ("Audio manifests",        extract_audio),
        ("Hadith — Bukhari",       lambda: convert_hadith_from_fawaz("bukhari")),
        ("Hadith — Muslim",        lambda: convert_hadith_from_fawaz("muslim")),
    ]

    for label, fn in steps:
        print(f"── {label} ──")
        try:
            fn()
        except Exception as e:
            print(f"  ✗ FAILED: {e}")
            print("  Continuing with next step...\n")
            continue
        print()

    print("=" * 50)
    print("  Done! Content saved to scripts/data/")
    print("=" * 50)

    # Summary
    for f in sorted(DATA_DIR.rglob("*.json")):
        kb = f.stat().st_size // 1024
        print(f"  {f.relative_to(DATA_DIR)}  ({kb}KB)")

if __name__ == "__main__":
    main()
