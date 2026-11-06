"""Generates Go_Ananas_Training.ipynb (run this file once; the notebook is what the group opens in Google Colab)."""
import json

cells = []
def md(s): cells.append({"cell_type": "markdown", "metadata": {}, "source": s.strip("\n")})
def code(s): cells.append({"cell_type": "code", "metadata": {}, "execution_count": None, "outputs": [], "source": s.strip("\n")})

md("""
# Go Ananas! — Dataset Building and Model Training (Google Colab)

This notebook builds the image dataset from **open, CC BY 4.0 licensed** pineapple datasets, trains the MobileNetV2
model described in Chapter 3, evaluates it, and exports the files used by the Android app and the web application.

**Before you start**
1. `Runtime → Change runtime type → T4 GPU`.
2. Create a free account at **roboflow.com**, then open *Settings → API Keys* and copy your **Private API Key**.
3. (Optional, more images) Download these Mendeley Data datasets with the **Download All** button and upload the zip files
   to your Google Drive folder `GoAnanas/mendeley/`:
   * Pineapple Leaf Disease Dataset (Malik et al., 2026) — https://data.mendeley.com/datasets/gc5f8dfgrt/2
   * PineappleLeaf-Fruit Dataset (Islam, 2026) — https://data.mendeley.com/datasets/tv7zfd6yzz/1
4. Run the cells from top to bottom (`Runtime → Run all`). Training takes about 20–40 minutes on a T4 GPU.

**Classes** (same as the paper) plus a rejection class:

| Folder | Condition | Sources used |
|---|---|---|
| `fruit_rot` | Fruit rot | Roboflow pineapple-diseases (Fruit_Rot), PinyaSuri AI 2 (Fruit Rot Disease) |
| `healthy` | Healthy | Roboflow (No_Disease, Healthy), Mendeley (Healthy Pineapple Leaf, Healthy Leaf, Fruit) |
| `heart_rot` | Heart rot (also called crown or top rot) | Roboflow (Crown_Rot_Disease), PinyaSuri AI 2 (Crown Rot Disease) |
| `mealybug_wilt` | Mealybug wilt | Roboflow (Mealybug_Wilt), PinyaSuri AI 2, Mendeley (Pineapple Mealybug Wilt) |
| `multiple_crown_disorder` | Multiple crown disorder / fasciation | Roboflow (Multiple_Crown_Disorder, Fruit_Fasciation_Disorder), PinyaSuri AI 2 |
| `root_rot` | Root rot | Roboflow (Root_Rot) |
| `not_pineapple` | Not a pineapple (rejection) | PlantVillage leaves of other crops + Imagenette everyday objects (TensorFlow Datasets) |
""")

code("""
!pip -q install roboflow imagehash opencv-python-headless tensorflow-datasets
from google.colab import drive
drive.mount('/content/drive')
import os, json, csv, glob, shutil, random, zipfile, hashlib, re
from pathlib import Path
ROOT = Path('/content/go_ananas'); RAW = ROOT/'raw'; ALL = ROOT/'dataset'/'all'
for p in (RAW, ALL): p.mkdir(parents=True, exist_ok=True)
DRIVE = Path('/content/drive/MyDrive/GoAnanas'); DRIVE.mkdir(parents=True, exist_ok=True)
CLASSES = ['fruit_rot', 'healthy', 'heart_rot', 'mealybug_wilt', 'multiple_crown_disorder', 'not_pineapple', 'root_rot']
MAX_PER_CLASS = 300      # the paper uses 300 images per class; raise it (e.g. 600) to use more images when available
SEED = 42
random.seed(SEED)
for c in CLASSES: (ALL/c).mkdir(exist_ok=True)
""")

md("## 1. Download the Roboflow datasets (CC BY 4.0)")
code("""
ROBOFLOW_API_KEY = ''   # <-- paste your Roboflow Private API Key here
assert ROBOFLOW_API_KEY, 'Paste your Roboflow API key in ROBOFLOW_API_KEY first.'
from roboflow import Roboflow
rf = Roboflow(api_key=ROBOFLOW_API_KEY)
PROJECTS = [
    ('pineapple-diseases', 'dataset-50-yd482'),
    ('pineapple-diseases', 'dataset-340-kyezk'),
    ('pineapple-diseases', 'dataset-1050-z2ebe'),
    ('ece-4-workspace', 'pinyasuri-ai-2'),
]
downloaded = []
for ws, proj in PROJECTS:
    try:
        p = rf.workspace(ws).project(proj)
        versions = p.versions()
        v = versions[0] if versions else None
        if v is None:
            print('no versions:', proj); continue
        ptype = getattr(p, 'type', '')
        fmts = ['folder', 'multiclass'] if 'classification' in str(ptype) else ['coco', 'folder', 'multiclass']
        for fmt in fmts:
            try:
                out = RAW/f'{proj}_{fmt}'
                v.download(fmt, location=str(out), overwrite=True)
                downloaded.append((proj, fmt, out)); print('OK', proj, fmt); break
            except Exception as e:
                print('  format', fmt, 'failed for', proj, '->', e)
    except Exception as e:
        print('Could not open', ws, proj, '->', e)
print(downloaded)
""")

md("## 2. Map the source labels to the seven classes")
code("""
def norm(s): return re.sub(r'[^a-z]', '', s.lower())
LABEL_MAP = {
    'fruitrot': 'fruit_rot', 'fruitrotdisease': 'fruit_rot',
    'nodisease': 'healthy', 'healthy': 'healthy', 'healthypineappleleaf': 'healthy', 'healthyleaf': 'healthy', 'fruit': 'healthy',
    'crownrot': 'heart_rot', 'crownrotdisease': 'heart_rot', 'heartrot': 'heart_rot', 'toprot': 'heart_rot',
    'mealybugwilt': 'mealybug_wilt', 'mealybugwiltdisease': 'mealybug_wilt', 'pineapplemealybugwilt': 'mealybug_wilt',
    'multiplecrowndisorder': 'multiple_crown_disorder', 'fruitfasciationdisorder': 'multiple_crown_disorder', 'fasciation': 'multiple_crown_disorder',
    'rootrot': 'root_rot', 'rootrotdisease': 'root_rot',
}
def map_label(name):
    return LABEL_MAP.get(norm(name))

collected = {c: [] for c in CLASSES}     # class -> list of (path_or_crop_callable, source)
import cv2
CROPS = ROOT/'crops'; CROPS.mkdir(exist_ok=True)

def add_file(cls, path, source):
    if cls: collected[cls].append((str(path), source))

for proj, fmt, out in downloaded:
    out = Path(out)
    if fmt == 'folder':
        for f in out.rglob('*'):
            if f.suffix.lower() in ('.jpg', '.jpeg', '.png'):
                add_file(map_label(f.parent.name), f, proj)
    elif fmt == 'multiclass':
        for csvf in out.rglob('_classes.csv'):
            rows = list(csv.reader(open(csvf)))
            head = [h.strip() for h in rows[0]]
            for r in rows[1:]:
                labels = [head[i] for i in range(1, len(head)) if r[i].strip() == '1']
                mapped = {map_label(l) for l in labels} - {None}
                if len(mapped) == 1:                     # keep images with one clear label only
                    add_file(mapped.pop(), csvf.parent/r[0], proj)
    elif fmt == 'coco':
        for js in out.rglob('_annotations.coco.json'):
            data = json.load(open(js))
            cats = {c['id']: c['name'] for c in data['categories']}
            imgs = {i['id']: i for i in data['images']}
            for a in data['annotations']:
                cls = map_label(cats.get(a['category_id'], ''))
                if not cls: continue
                info = imgs[a['image_id']]; src = js.parent/info['file_name']
                im = cv2.imread(str(src))
                if im is None: continue
                x, y, w, h = [int(v) for v in a['bbox']]
                if w < 40 or h < 40: continue
                pad = int(0.1 * max(w, h))
                crop = im[max(0, y-pad):y+h+pad, max(0, x-pad):x+w+pad]
                dst = CROPS/f"{proj}_{a['id']}.jpg"; cv2.imwrite(str(dst), crop)
                add_file(cls, dst, proj)
for c in CLASSES: print(f'{c:26s}', len(collected[c]))
""")

md("## 3. (Optional) Add the Mendeley datasets from Google Drive")
code("""
MENDELEY = DRIVE/'mendeley'
for z in sorted(MENDELEY.glob('*.zip')) if MENDELEY.exists() else []:
    dst = RAW/('mendeley_' + z.stem); dst.mkdir(exist_ok=True)
    with zipfile.ZipFile(z) as f: f.extractall(dst)
    for inner in list(dst.rglob('*.zip')):            # some Mendeley downloads contain zips inside
        with zipfile.ZipFile(inner) as f: f.extractall(inner.parent/inner.stem)
    for f in dst.rglob('*'):
        if f.suffix.lower() in ('.jpg', '.jpeg', '.png') and 'augment' not in str(f).lower():
            add_file(map_label(f.parent.name), f, 'mendeley_' + z.stem)
for c in CLASSES: print(f'{c:26s}', len(collected[c]))
""")

md("## 4. Add the *not pineapple* class (for the rejection message in the app)")
code("""
import tensorflow_datasets as tfds, numpy as np
def take(name, split, n, prefix):
    ds = tfds.load(name, split=split, shuffle_files=True, as_supervised=True)
    i = 0
    for img, _ in ds.take(n):
        p = CROPS/f'{prefix}_{i}.jpg'; cv2.imwrite(str(p), cv2.cvtColor(img.numpy(), cv2.COLOR_RGB2BGR)); i += 1
        add_file('not_pineapple', p, name)
take('plant_village', 'train', MAX_PER_CLASS // 2, 'pv')         # leaves of other crops
take('imagenette/160px', 'train', MAX_PER_CLASS // 2, 'inet')    # everyday objects, people, animals
print('not_pineapple', len(collected['not_pineapple']))
""")

md("## 5. Remove duplicates, balance the classes, and copy to `dataset/all/`")
code("""
from PIL import Image
import imagehash
summary = []
for c in CLASSES:
    seen, keep = [], []
    items = collected[c][:]; random.shuffle(items)
    for path, src in items:
        try:
            h = imagehash.phash(Image.open(path).convert('RGB'))
        except Exception:
            continue
        if any(h - s <= 4 for s in seen):                 # near-duplicate (e.g. Roboflow augmented copy)
            continue
        seen.append(h); keep.append((path, src))
        if len(keep) >= MAX_PER_CLASS: break
    for i, (path, src) in enumerate(keep):
        Image.open(path).convert('RGB').save(ALL/c/f'{c}_{i:04d}.jpg', quality=92)
    by_src = {}
    for _, s in keep: by_src[s] = by_src.get(s, 0) + 1
    summary.append((c, len(keep), by_src))
    flag = '' if len(keep) >= MAX_PER_CLASS else '   <-- fewer than MAX_PER_CLASS; add more images for this class'
    print(f'{c:26s} {len(keep):5d}  {by_src}{flag}')
json.dump(summary, open(DRIVE/'dataset_summary.json', 'w'), indent=1)
""")

md("## 6. Split 70 / 15 / 15 and train (Table 3.5 settings)")
code("%%writefile /content/split_dataset.py\n" + open("split_dataset.py").read())
code("%%writefile /content/train_model.py\n" + open("train_model.py").read())
code("""
%cd /content
!python split_dataset.py --src /content/go_ananas/dataset/all --dst /content/go_ananas/dataset
!python train_model.py --data /content/go_ananas/dataset --out /content/go_ananas/output
""")

md("## 7. Save the results to Google Drive")
code("""
out = Path('/content/go_ananas/output')
for f in out.iterdir(): shutil.copy(f, DRIVE/f.name)
print(open(out/'classification_report.txt').read())
print('\\nSaved to Google Drive / GoAnanas:', [f.name for f in out.iterdir()])
print('Upload go_ananas_model.tflite, labels.txt, and model_info.json to app/src/main/assets/ on GitHub.')
print('Paste the counts from dataset_summary.json into Table 3.2 and the results into Tables 4.1 to 4.3 of the paper.')
""")

nb = {"cells": cells, "metadata": {"accelerator": "GPU", "colab": {"provenance": []},
      "kernelspec": {"display_name": "Python 3", "name": "python3"}, "language_info": {"name": "python"}},
      "nbformat": 4, "nbformat_minor": 0}
json.dump(nb, open("Go_Ananas_Training.ipynb", "w"), indent=1)
print("written Go_Ananas_Training.ipynb")
