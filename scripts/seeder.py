import json
import os
import sys
import sqlite3

LEARN_DIR = "app/src/main/assets/learn"
DATA_DIR = "scripts/data"
OUTPUT_DB = "app/src/main/assets/khushu.db"

def build_db():
    if os.path.exists(OUTPUT_DB):
        os.remove(OUTPUT_DB)
    
    conn = sqlite3.connect(OUTPUT_DB)
    cursor = conn.cursor()

    # Create tables
    cursor.execute("""
        CREATE TABLE surahs (
            number INTEGER PRIMARY KEY,
            name_arabic TEXT,
            name_en TEXT,
            name_translation TEXT,
            ayah_count INTEGER,
            revelation_type TEXT
        )
    """)

    cursor.execute("""
        CREATE TABLE ayahs (
            surah INTEGER,
            ayah INTEGER,
            text_uthmani TEXT,
            tajweed_markup TEXT,
            PRIMARY KEY (surah, ayah),
            FOREIGN KEY (surah) REFERENCES surahs(number)
        )
    """)
    cursor.execute("CREATE INDEX idx_ayahs_ref ON ayahs(surah, ayah)")

    cursor.execute("""
        CREATE TABLE hadiths (
            collection TEXT,
            number INTEGER,
            text_arabic TEXT,
            text_en TEXT,
            grade TEXT,
            narrator TEXT,
            PRIMARY KEY (collection, number)
        )
    """)
    cursor.execute("CREATE INDEX idx_hadiths_ref ON hadiths(collection, number)")

    cursor.execute("""
        CREATE TABLE translations (
            surah INTEGER,
            ayah INTEGER,
            lang TEXT,
            text TEXT,
            edition TEXT,
            PRIMARY KEY (surah, ayah, lang, edition)
        )
    """)

    # Collect unique references from topic JSONs
    ayah_refs = set()
    hadith_refs = set()

    for filename in os.listdir(LEARN_DIR):
        if not filename.endswith(".json"):
            continue
        
        with open(os.path.join(LEARN_DIR, filename), 'r', encoding='utf-8') as f:
            topic = json.load(f)
        
        for block in topic.get("blocks", []):
            if block['type'] == 'ayah':
                ayah_refs.add((block['surah'], block['ayah']))
            elif block['type'] == 'hadith':
                if not block.get("verified"):
                    print(f"❌ ERROR: Hadith {block['display']} in {filename} is NOT verified!")
                    sys.exit(1)
                hadith_refs.add((block['collection'], block['number']))

    # Load Tanzil data
    print("📦 Processing Quran data...")
    with open(os.path.join(DATA_DIR, "quran-uthmani.json"), 'r', encoding='utf-8') as f:
        tanzil_data = json.load(f)

    # Process Surahs and Ayahs
    surahs_to_insert = set()
    for surah_data in tanzil_data:
        surah_num = surah_data['number']
        surahs_to_insert.add(surah_num) # In a real app we might want all surahs, but spec says "extracts only referenced ayahs + surah metadata"
        
        for ayah_data in surah_data['ayahs']:
            ayah_num = ayah_data['number']
            if (surah_num, ayah_num) in ayah_refs:
                cursor.execute(
                    "INSERT INTO ayahs (surah, ayah, text_uthmani) VALUES (?, ?, ?)",
                    (surah_num, ayah_num, ayah_data['text'])
                )
    
    # Metadata for Surahs (assuming available in same file or a companion file)
    # If not in uthmani file, we'd need a separate metadata source.
    # For now, let's insert placeholders for name_en/translation if not present.
    for surah_num in surahs_to_insert:
        # Find surah in data
        s = next((x for x in tanzil_data if x['number'] == surah_num), None)
        if s:
            cursor.execute(
                "INSERT INTO surahs (number, name_arabic, name_en, name_translation, ayah_count, revelation_type) VALUES (?, ?, ?, ?, ?, ?)",
                (s['number'], s.get('name_arabic', ''), s.get('name_en', ''), s.get('name_translation', ''), s.get('ayah_count', 0), s.get('revelation_type', ''))
            )

    # Load Hadith data
    print("📦 Processing Hadith data...")
    collections = {}
    for coll_name, _ in hadith_refs:
        if coll_name not in collections:
            file_path = os.path.join(DATA_DIR, f"en.{coll_name}.json")
            if os.path.exists(file_path):
                with open(file_path, 'r', encoding='utf-8') as f:
                    data = json.load(f)
                    collections[coll_name] = {h['number']: h for h in data}
    
    for coll_name, hadith_num in hadith_refs:
        h = collections.get(coll_name, {}).get(hadith_num)
        if h:
            if h.get('grade', '').lower() != 'sahih':
                print(f"❌ ERROR: Hadith {coll_name} {hadith_num} is not Sahih!")
                sys.exit(1)
            
            cursor.execute(
                "INSERT INTO hadiths (collection, number, text_en, grade, narrator) VALUES (?, ?, ?, ?, ?)",
                (coll_name, hadith_num, h['text'], h['grade'], h.get('narrator'))
            )

    conn.commit()
    conn.close()
    print(f"✅ Database built successfully: {OUTPUT_DB}")

if __name__ == "__main__":
    if not os.path.exists(LEARN_DIR):
        print(f"❌ Learn directory {LEARN_DIR} missing.")
        sys.exit(1)
    if not os.path.exists(DATA_DIR):
        print(f"❌ Data directory {DATA_DIR} missing.")
        sys.exit(1)
    build_db()
