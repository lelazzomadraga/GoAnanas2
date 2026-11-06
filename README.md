# Go Ananas! – Android App, Web Dashboard, and Model Training

Detection of Pineapple (*Ananas comosus*) Diseases Using Digital Image Processing and Convolutional Neural Network
BS Computer Engineering, Asia Technological School of Science and Arts

| Folder | Contents |
|---|---|
| `app/` | Android app (Kotlin, Jetpack Compose) for five user roles |
| `web_dashboard/` | Real-time web dashboard (HTML/JavaScript + Firebase) |
| `model_training/` | Google Colab notebook and scripts that build the dataset and train the model |
| `firebase/` | Security rules for Cloud Firestore and Cloud Storage |
| `.github/workflows/` | Builds the APK and publishes the web dashboard automatically on GitHub |
| `docs/` | Screens and diagrams used in the paper |

---

## 1. What the app does

**Every user**
- Chooses **English or Tagalog**, sees a **4-page tutorial**, then **registers or signs in**.
- Fills in a short **profile**: name, sex, age range, years of experience, location, optional **profile picture** (a default avatar is shown otherwise).
- Takes a photo (CameraX) or picks one from the gallery, chooses the plant part, and gets the **diagnosis, confidence of each class, and recommended action** – on the phone, without internet.
- Gets an **error instead of a result** when the photo has **no pineapple** ("No pineapple detected"), when the file cannot be read ("No image found"), or when the result is too uncertain. Blurred, dark, and overexposed photos get a warning.
- Saves scans (photo, plant part, block, notes) to the **local database**; they are uploaded when online.
- Receives **notifications**: re-check reminders, monthly inspection, outbreak alerts, review results, announcements, account approval.
- Can change language, edit the profile, and **log out** from the Profile tab.

**Role functions**

| Role | Android app | Web dashboard |
|---|---|---|
| Farmer / Farm worker | Scan, re-check plants, records, guide | Own scans, announcements, profile |
| Vendor | Check fruits, **batches** with accepted/rejected counts | Own scans, announcements, profile |
| Agricultural Technician | **Review queue** (confirm/correct + advice), farm dashboard, send advice, **dataset** | Overview, scans, review, announcements, dataset |
| Farm Owner | Farm dashboard + CSV export, **Team** (approve workers, set roles), announcements, review | Overview, scans, review, team, announcements |
| Administrator | **All accounts and farms**, dataset, model info | Overview of all farms, scans, user accounts, dataset |

Owners register first and create a **farm code**. Other users register with that code and wait for the owner's approval. Administrator accounts cannot be chosen during registration (see step 4.6).

**Offline mode.** Until Firebase is connected, the app keeps accounts on the phone and creates five sample accounts so every role can be tried on one phone (password `goananas123`): `farmer@demo.ph`, `vendor@demo.ph`, `tech@demo.ph`, `owner@demo.ph`, `admin@demo.ph`.

---

## 2. Train the real model (Google Colab, no strong computer needed)

The app ships with an **untrained placeholder model**. Train the real one:

1. Create a free account at **roboflow.com** → *Settings → API Keys* → copy the **Private API Key**.
2. (Optional, more images) Download with **Download All** and upload the zip files to Google Drive folder `GoAnanas/mendeley/`:
   - https://data.mendeley.com/datasets/gc5f8dfgrt/2
   - https://data.mendeley.com/datasets/tv7zfd6yzz/1
3. Open **colab.research.google.com** → *File → Upload notebook* → `model_training/Go_Ananas_Training.ipynb`.
4. *Runtime → Change runtime type → T4 GPU*. Paste the API key in the cell that says `ROBOFLOW_API_KEY = ''`.
5. *Runtime → Run all*. It takes about 20–40 minutes. The notebook downloads the CC BY 4.0 datasets, maps their labels to the six classes, adds 300 "not a pineapple" images, removes duplicates, keeps 300 images per class, splits 70/15/15, trains MobileNetV2 with the Table 3.5 settings, and saves the results to Google Drive → `GoAnanas/`.
6. Upload `go_ananas_model.tflite`, `labels.txt`, and `model_info.json` to **`app/src/main/assets/`** on GitHub (replace the old files). GitHub builds a new APK.
7. Copy the counts from `dataset_summary.json` into **Table 3.2** and the results (`classification_report.txt`, `confusion_matrix.csv`, `training_history.csv`) into **Tables 4.1–4.3** of the paper.

Label mapping: Crown_Rot → heart rot; Fruit_Fasciation and Multiple_Crown → multiple crown disorder; No_Disease/Healthy → healthy. If a class has fewer than 300 images, the notebook says so; add photos of that class (for example, photos confirmed in the app's Dataset screen) and run it again.

---

## 3. Build the APK on GitHub (no Android Studio needed)

1. Upload this folder to a GitHub repository (or edit the changed files).
2. **Actions → Build Go Ananas! APK → Run workflow**. When it is green, download **GoAnanas-debug-apk** under *Artifacts*, unzip, and install `app-debug.apk` (Android 7.0 or newer; BlueStacks works too).

If it fails, open the failed run → *Build debug APK* → search for `e: ` and send the lines.

---

## 4. Connect the app and the web dashboard (Firebase, free plan)

1. Go to **console.firebase.google.com** → *Add project* (Google Analytics not needed).
2. **Build → Authentication → Get started → Email/Password → Enable**.
3. **Build → Firestore Database → Create database** → production mode → region `asia-southeast1`.
   Open the **Rules** tab, paste the content of `firebase/firestore.rules`, and click **Publish**.
4. *(Optional)* **Build → Storage → Get started** – only if your project allows it on the free plan (new projects may ask for the Blaze plan). If you skip it, the app stores a small copy of each photo and profile picture inside the Firestore record, so the dashboard still shows them. If you enable it, paste `firebase/storage.rules` in its **Rules** tab and **Publish**.
5. **Android app:** *Project settings → Add app → Android*, package name `ph.edu.asiatech.goananas` → download `google-services.json`.
   On GitHub: **Settings → Secrets and variables → Actions → New repository secret**, name `GOOGLE_SERVICES_JSON`, paste the whole file. Run **Build Go Ananas! APK** again and reinstall the APK.
6. **First administrator:** register in the app as any role, then in *Firestore → users → (your account)* change `role` to `admin` and `status` to `active`.
7. **Web dashboard:** *Project settings → Add app → Web* → copy the `firebaseConfig` values into a new file **`web_dashboard/firebase-config.js`** (copy the format from `firebase-config.example.js`) and upload it to GitHub. These values are not secret; access is controlled by the security rules.
8. **Publish the dashboard:** GitHub → *Settings → Pages → Source: GitHub Actions*. The workflow **Publish web dashboard** runs and gives the link, e.g. `https://USERNAME.github.io/GoAnanas/`.
9. In Firebase → *Authentication → Settings → Authorized domains*, add `USERNAME.github.io`.

Now scans, photos, reviews, approvals, announcements, and notifications move between the phones and the dashboard in real time. When the app is closed, it checks for new notifications about every 15 minutes (free plan; no Cloud Functions needed).

---

## 5. Database

**Phone (Room, `go_ananas.db`)**: `scans`, `accounts`, `notifications`, `announcements`, `dataset`, `batches`; settings in DataStore.

**Cloud Firestore**
- `users/{uid}` – profile, role, farmCode, status, language, photoUrl
- `users/{uid}/inbox/{id}` – notifications (title, body, type, scanUuid, read)
- `farms/{farmCode}` – farm name and owner
- `farms/{farmCode}/scans/{uuid}` – diagnosis, probabilities, photo link, status, technician review
- `farms/{farmCode}/announcements/{id}`
- `dataset/{scanUuid}` – images confirmed by technicians for retraining

**Cloud Storage**: `farms/{farm}/scans/{uuid}.jpg`, `avatars/{uid}.jpg`

---

## 6. Dataset sources (cite in the paper)

- ECE 4 Workspace. (n.d.). *PinyaSuri AI 2* [Data set]. Roboflow Universe. CC BY 4.0.
- Pineapple Diseases. (n.d.). *Dataset-50, Dataset-340, and Dataset-1050* [Data sets]. Roboflow Universe. CC BY 4.0.
- Malik, N. (2026). *Pineapple leaf disease dataset for deep learning-based classification and precision agriculture* (Version 2). Mendeley Data. https://doi.org/10.17632/gc5f8dfgrt.2 (CC BY 4.0)
- Islam, A. (2026). *PineappleLeaf-Fruit dataset* (Version 1). Mendeley Data. https://doi.org/10.17632/tv7zfd6yzz.1 (CC BY 4.0)
- Hughes & Salathé (2015), PlantVillage; Howard (2019), Imagenette – "not a pineapple" images.

## Limitations

- The app reads only visible symptoms; internal conditions such as "kalawang" cannot be detected. Results are a screening aid, not a laboratory diagnosis.
- Public training images come from other farms and countries; retrain with local photos (collected through the app's Dataset function) for best accuracy in Tagaytay.
- Status changes made in the web dashboard reach the phone of the person who made the scan; other edits are one-way from phone to cloud.
