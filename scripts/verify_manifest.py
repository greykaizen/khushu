import json
import os
import sys

LEARN_DIR = "app/src/main/assets/learn"
DATA_DIR = "scripts/data"

def load_fawaz_ahmed(collection):
    file_path = os.path.join(DATA_DIR, f"en.{collection}.json")
    if not os.path.exists(file_path):
        return None
    with open(file_path, 'r', encoding='utf-8') as f:
        return json.load(f)

def load_tanzil():
    file_path = os.path.join(DATA_DIR, "quran-uthmani.json")
    if not os.path.exists(file_path):
        return None
    with open(file_path, 'r', encoding='utf-8') as f:
        return json.load(f)

def verify_topics():
    tanzil_data = load_tanzil()
    if not tanzil_data:
        print("❌ quran-uthmani.json not found in scripts/data/")
        return

    # Index Tanzil for fast lookup: (surah, ayah) -> text
    quran_index = {}
    for surah_data in tanzil_data:
        surah_num = surah_data['number']
        for ayah_data in surah_data['ayahs']:
            quran_index[(surah_num, ayah_data['number'])] = ayah_data['text']

    collections = {}

    for filename in os.listdir(LEARN_DIR):
        if not filename.endswith(".json"):
            continue
        
        file_path = os.path.join(LEARN_DIR, filename)
        with open(file_path, 'r', encoding='utf-8') as f:
            topic = json.load(f)
        
        changed = False
        print(f"\nChecking {filename}: {topic['title']}")
        
        for block in topic.get("blocks", []):
            if block['type'] == 'hadith':
                coll_name = block['collection']
                if coll_name not in collections:
                    data = load_fawaz_ahmed(coll_name)
                    if data:
                        # Index by number
                        collections[coll_name] = {h['number']: h for h in data}
                    else:
                        collections[coll_name] = None
                
                coll_data = collections[coll_name]
                if not coll_data:
                    print(f"  ❌ Collection {coll_name} not found locally")
                    continue
                
                hadith_num = block['number']
                hadith = coll_data.get(hadith_num)
                
                if hadith:
                    grade = hadith.get('grade', '').lower()
                    text = hadith.get('text', '')[:150].replace('\n', ' ')
                    
                    if grade == "sahih":
                        print(f"  ✅ {block['display']}: sahih | {text}...")
                        if not block.get("verified"):
                            block["verified"] = True
                            changed = True
                    else:
                        print(f"  ⚠️ {block['display']}: {grade} | {text}...")
                else:
                    print(f"  ❌ {block['display']}: NOT FOUND")
            
            elif block['type'] == 'ayah':
                ref = (block['surah'], block['ayah'])
                if ref in quran_index:
                    if not block.get("verified"):
                        block["verified"] = True
                        changed = True
                    print(f"  ✅ {block['display']} verified")
                else:
                    print(f"  ❌ {block['display']} NOT FOUND in Quran data")

        if changed:
            with open(file_path, 'w', encoding='utf-8') as f:
                json.dump(topic, f, indent=2, ensure_ascii=False)
            print(f"  💾 Updated {filename}")

if __name__ == "__main__":
    if not os.path.exists(DATA_DIR):
        print(f"❌ Data directory {DATA_DIR} missing. Please download datasets first.")
        sys.exit(1)
    verify_topics()
