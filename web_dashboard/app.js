// Go Ananas! web dashboard — connected in real time to the same Firebase project as the Android app.
// Without firebase-config.js it opens in demo mode with sample records and a role switcher.

const CLASSES = [
  { label: "fruit_rot", en: "Fruit Rot", tl: "Bulok na Bunga", color: "#C62828" },
  { label: "healthy", en: "Healthy", tl: "Malusog", color: "#4C9A2A" },
  { label: "heart_rot", en: "Heart Rot", tl: "Bulok sa Puso", color: "#E65100" },
  { label: "mealybug_wilt", en: "Mealybug Wilt", tl: "Pagkalanta (Kuto-kuto)", color: "#AD1457" },
  { label: "multiple_crown_disorder", en: "Multiple Crown Disorder", tl: "Maraming Korona", color: "#6A1B9A" },
  { label: "root_rot", en: "Root Rot", tl: "Bulok na Ugat", color: "#5D4037" },
];
const ACTIONS = {
  fruit_rot: ["Remove infected fruits immediately to prevent fungal spore dispersal. Apply approved bio-fungicides.", "Alisin agad ang mga apektadong bunga upang hindi kumalat ang fungus. Gumamit ng aprubadong bio-fungicide."],
  healthy: ["Crop status: Optimal. Continue standard irrigation and balanced fertilization schedules.", "Kalagayan ng pananim: Maayos. Ipagpatuloy ang regular na pagdidilig at pag-aabono."],
  heart_rot: ["Remove and destroy infected heart/whorl tissue. Apply copper-based fungicides and improve drainage.", "Alisin at sirain ang apektadong puso ng halaman. Gumamit ng copper-based na fungicide at ayusin ang daluyan ng tubig."],
  mealybug_wilt: ["Quarantine affected plants. Apply systemic insecticides or introduce natural predators.", "Ihiwalay ang apektadong halaman. Gumamit ng systemic insecticide o natural na kaaway ng kuto-kuto."],
  multiple_crown_disorder: ["Monitor calcium and zinc. Conduct routine soil and foliar tests.", "Bantayan ang calcium at zinc. Regular na suriin ang lupa at dahon."],
  root_rot: ["Improve field drainage immediately. Avoid waterlogging and treat soil with fungicides.", "Ayusin agad ang daluyan ng tubig. Iwasang bumaha at gamutin ang lupa ng fungicide."],
};
const ROLES = {
  farmer: ["Farmer / Farm worker", "Magsasaka / Manggagawa"],
  vendor: ["Vendor", "Tindero / Tindera"],
  technician: ["Agricultural Technician", "Agricultural Technician"],
  owner: ["Farm Owner", "May-ari ng Taniman"],
  admin: ["Administrator", "Administrator"],
};
const T = {
  en: {
    tagline: "Pineapple disease detection", signIn: "Sign in", signInHint: "Use the same account as in the Go Ananas! app.", password: "Password",
    forgot: "Forgot password?", logout: "Log out", live: "Live", notifications: "Notifications", markAll: "Mark all read",
    demoBanner: "Demo mode: sample records only. Add firebase-config.js to connect the dashboard to the farm's real scans. Use the selector at the top right to preview each role.",
    pendingBanner: "Your account is waiting for the farm owner's approval.",
    overview: "Overview", scans: "Scan records", myScans: "My scans", review: "Review queue", team: "Team", users: "User accounts",
    news: "Announcements", dataset: "Image dataset", profile: "My profile",
    plants: "Plants scanned", healthyPct: "Healthy", diseased: "With disease", forReview: "For review", open: "Needs action",
    perClass: "Results per condition", perWeek: "Scans per week (last 8 weeks)", blocks: "Diseased plants per block", reviewStatus: "Review status",
    outbreaks: "Possible outbreaks (3 or more cases in one block within 14 days)", noOutbreak: "No possible outbreak in the last 14 days.",
    latest: "Latest scans", searchPh: "Search worker, block, notes…", allClasses: "All conditions", allStatus: "All status", allReview: "All reviews",
    allBlocks: "All blocks", allFarms: "All farms", date: "Date", result: "Result", conf: "Confidence", block: "Block", part: "Part", by: "Scanned by",
    status: "Status", reviewCol: "Review", exportCsv: "Export CSV", showing: "Showing", of: "of", prev: "Previous", next: "Next",
    pending: "For review", confirmed: "Confirmed", corrected: "Corrected", none: "—",
    Open: "Needs action", Rechecked: "Rechecked", Resolved: "Resolved", Healthy: "Healthy",
    whatToDo: "Recommended action", breakdown: "Confidence of each condition", reviewThis: "Review this scan", rightCondition: "Choose the correct condition",
    advice: "Advice to the farmer", confirm: "Confirm result", saveCorrection: "Save correction", setStatus: "Update status",
    addDataset: "Add to training dataset", inDataset: "In the dataset", removeDataset: "Remove from dataset", reviewedBy: "Reviewed by",
    notes: "Notes", close: "Close", appResult: "App result", noScans: "No scans to show.",
    waiting: "Waiting for approval", members: "Members", approve: "Approve", decline: "Decline", disable: "Disable", enable: "Enable",
    role: "Role", farm: "Farm", shareCode: "Share your farm code with your workers so they can register in the app:",
    postTitle: "Title", postBody: "Message", send: "Send to the farm", noNews: "No announcements yet.", chooseFarm: "Farm",
    perLabel: "Confirmed images per condition", exportDataset: "Export dataset list (CSV)", noDataset: "No images yet. Technicians add reviewed scans to the dataset.",
    retrain: "Download the list, then run model_training/Go_Ananas_Training.ipynb to retrain the model with these images.",
    sex: "Sex", age: "Age", exp: "Experience", location: "Location", email: "E-mail", account: "Account", active: "Active", disabledS: "Disabled",
    editInApp: "Edit your profile and photo in the Go Ananas! app.", noNotes: "No notifications yet.", enableDesktop: "Enable desktop alerts",
    sent: "Sent.", saved: "Saved.", resetSent: "A password reset link was sent to your e-mail.", wrong: "Wrong e-mail or password.",
    approvedMsg: ["Your account was approved", "You are now a member of the farm. Your scans can now be synced."],
    roleMsg: ["Your role was changed", "Your role is now "], reviewMsg: ["Your scan was reviewed", " reviewed your scan: "],
    newsMsg: "Announcement from ", noAccess: "This page is not available for your role.", accuracy: "App agreed with the technician",
  },
  tl: {
    tagline: "Pagtukoy ng sakit ng pinya", signIn: "Mag-sign in", signInHint: "Gamitin ang parehong account sa Go Ananas! app.", password: "Password",
    forgot: "Nakalimutan ang password?", logout: "Mag-log out", live: "Live", notifications: "Mga abiso", markAll: "Basa na lahat",
    demoBanner: "Demo mode: sample na talaan lamang. Idagdag ang firebase-config.js para maikonekta sa totoong scan ng taniman. Gamitin ang pagpipilian sa kanang itaas para makita ang bawat role.",
    pendingBanner: "Hinihintay pa ang pag-apruba ng may-ari sa iyong account.",
    overview: "Buod", scans: "Talaan ng scan", myScans: "Aking scan", review: "Susuriin", team: "Mga kasapi", users: "Mga account",
    news: "Mga anunsiyo", dataset: "Dataset ng larawan", profile: "Aking profile",
    plants: "Na-scan na halaman", healthyPct: "Malusog", diseased: "May sakit", forReview: "Susuriin", open: "Kailangang aksyunan",
    perClass: "Resulta bawat kondisyon", perWeek: "Scan bawat linggo (huling 8 linggo)", blocks: "May sakit bawat block", reviewStatus: "Kalagayan ng pagsusuri",
    outbreaks: "Posibleng pagkalat (3 o higit sa isang block sa loob ng 14 araw)", noOutbreak: "Walang posibleng pagkalat sa nakaraang 14 na araw.",
    latest: "Pinakabagong scan", searchPh: "Hanapin ang manggagawa, block, tala…", allClasses: "Lahat ng kondisyon", allStatus: "Lahat ng kalagayan", allReview: "Lahat ng pagsusuri",
    allBlocks: "Lahat ng block", allFarms: "Lahat ng taniman", date: "Petsa", result: "Resulta", conf: "Kumpiyansa", block: "Block", part: "Bahagi", by: "Nag-scan",
    status: "Kalagayan", reviewCol: "Pagsusuri", exportCsv: "I-export ang CSV", showing: "Ipinapakita", of: "sa", prev: "Nakaraan", next: "Susunod",
    pending: "Susuriin", confirmed: "Kinumpirma", corrected: "Itinama", none: "—",
    Open: "Kailangang aksyunan", Rechecked: "Nasuri muli", Resolved: "Naayos na", Healthy: "Malusog",
    whatToDo: "Inirerekomendang gawin", breakdown: "Kumpiyansa sa bawat kondisyon", reviewThis: "Suriin ang scan", rightCondition: "Piliin ang tamang kondisyon",
    advice: "Payo sa magsasaka", confirm: "Kumpirmahin", saveCorrection: "I-save ang pagtatama", setStatus: "I-update ang kalagayan",
    addDataset: "Idagdag sa dataset", inDataset: "Nasa dataset", removeDataset: "Alisin sa dataset", reviewedBy: "Sinuri ni",
    notes: "Tala", close: "Isara", appResult: "Resulta ng app", noScans: "Walang scan na maipapakita.",
    waiting: "Naghihintay ng pag-apruba", members: "Mga kasapi", approve: "Aprubahan", decline: "Tanggihan", disable: "I-disable", enable: "I-enable",
    role: "Role", farm: "Taniman", shareCode: "Ibahagi ang farm code sa mga manggagawa para makapagparehistro sila sa app:",
    postTitle: "Pamagat", postBody: "Mensahe", send: "Ipadala sa taniman", noNews: "Wala pang anunsiyo.", chooseFarm: "Taniman",
    perLabel: "Kumpirmadong larawan bawat kondisyon", exportDataset: "I-export ang listahan ng dataset (CSV)", noDataset: "Wala pang larawan. Idinadagdag ng technician ang mga nasuring scan.",
    retrain: "I-download ang listahan, saka patakbuhin ang model_training/Go_Ananas_Training.ipynb para muling sanayin ang model.",
    sex: "Kasarian", age: "Edad", exp: "Karanasan", location: "Lugar", email: "E-mail", account: "Account", active: "Aktibo", disabledS: "Hindi aktibo",
    editInApp: "I-edit ang profile at larawan sa Go Ananas! app.", noNotes: "Wala pang abiso.", enableDesktop: "Payagan ang abiso sa computer",
    sent: "Naipadala.", saved: "Na-save.", resetSent: "Napadala na ang link sa iyong e-mail.", wrong: "Mali ang e-mail o password.",
    approvedMsg: ["Naaprubahan ang iyong account", "Kasapi ka na ng taniman. Maaari nang ma-sync ang iyong mga scan."],
    roleMsg: ["Binago ang iyong role", "Ang iyong role ay "], reviewMsg: ["Nasuri na ang iyong scan", " ang sumuri sa iyong scan: "],
    newsMsg: "Anunsiyo mula kay ", noAccess: "Hindi available ang pahinang ito sa iyong role.", accuracy: "Tama ang app ayon sa technician",
  },
};

// ---------------------------------------------------------------- helpers
let lang = "en";
try { lang = localStorage.getItem("ga_lang") || "en"; } catch (e) { /* storage blocked */ }
const t = (k) => (T[lang] && T[lang][k]) ?? T.en[k] ?? k;
const $ = (id) => document.getElementById(id);
const esc = (s) => String(s ?? "").replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
const cls = (label) => CLASSES.find((c) => c.label === label) || CLASSES[1];
const cname = (label) => (lang === "tl" ? cls(label).tl : cls(label).en);
const rname = (key) => (ROLES[key] || ROLES.farmer)[lang === "tl" ? 1 : 0];
const pct = (v) => ((v || 0) * 100).toFixed(1) + "%";
const fmt = (ms) => new Date(ms).toLocaleString(lang === "tl" ? "fil-PH" : "en-PH", { month: "short", day: "numeric", year: "numeric", hour: "numeric", minute: "2-digit" });
const day = (ms) => new Date(ms).toLocaleDateString(lang === "tl" ? "fil-PH" : "en-PH", { month: "short", day: "numeric", year: "numeric" });
const ago = (ms) => {
  const m = Math.floor((Date.now() - ms) / 60000);
  if (m < 1) return lang === "tl" ? "ngayon lang" : "just now";
  if (m < 60) return m + (lang === "tl" ? " min ang nakalipas" : " min ago");
  if (m < 1440) return Math.floor(m / 60) + (lang === "tl" ? " oras ang nakalipas" : " h ago");
  return day(ms);
};
const finalLabel = (s) => s.confirmedLabel || s.finalLabel || s.predictedLabel;
const isHealthy = (s) => finalLabel(s) === "healthy";
const avatar = (url) => url && /^(https?:|data:image)/.test(url) ? url : "data:image/svg+xml," + encodeURIComponent(
  "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 96 96'><rect width='96' height='96' fill='#DDF0D3'/><circle cx='48' cy='36' r='16' fill='#4C9A2A'/><path d='M16 96c0-22 14-36 32-36s32 14 32 36z' fill='#4C9A2A'/></svg>");
function toast(msg) { const el = $("toast"); el.textContent = msg; el.hidden = false; clearTimeout(toast._t); toast._t = setTimeout(() => (el.hidden = true), 2600); }
function csvDownload(name, head, rows) {
  const q = (v) => `"${String(v ?? "").replace(/"/g, '""')}"`;
  const blob = new Blob([[head.join(","), ...rows.map((r) => r.map(q).join(","))].join("\n")], { type: "text/csv" });
  const a = document.createElement("a"); a.href = URL.createObjectURL(blob); a.download = name; a.click();
}
function statusTag(st) { const c = st === "Open" ? "t-red" : st === "Rechecked" ? "t-gold" : "t-green"; return `<span class="tag ${c}">${esc(t(st) || st)}</span>`; }
function reviewTag(r) {
  if (r === "pending") return `<span class="tag t-blue">${t("pending")}</span>`;
  if (r === "confirmed") return `<span class="tag t-green">${t("confirmed")}</span>`;
  if (r === "corrected") return `<span class="tag t-gold">${t("corrected")}</span>`;
  return "";
}

// ---------------------------------------------------------------- state
const S = { me: null, scans: [], users: [], news: [], dataset: [], inbox: [], page: "overview", f: { q: "", c: "", st: "", r: "", b: "", farm: "" }, pageNo: 0 };
let api = null, charts = {}, unsub = null, firstInbox = true;

// ---------------------------------------------------------------- Firebase connection
async function firebaseApi(cfg) {
  const base = "https://www.gstatic.com/firebasejs/10.14.1/";
  const { initializeApp } = await import(base + "firebase-app.js");
  const A = await import(base + "firebase-auth.js");
  const F = await import(base + "firebase-firestore.js");
  const app = initializeApp(cfg), auth = A.getAuth(app), db = F.getFirestore(app);
  const scanFrom = (d) => { const x = d.data(); return { id: d.id, _path: d.ref.path, ...x, imageUrl: x.imageUrl || x.thumb || "" }; };
  return {
    onAuth: (cb) => A.onAuthStateChanged(auth, cb),
    login: (e, p) => A.signInWithEmailAndPassword(auth, e, p),
    logout: () => A.signOut(auth),
    reset: (e) => A.sendPasswordResetEmail(auth, e),
    async profile(uid) { const d = await F.getDoc(F.doc(db, "users", uid)); return d.exists() ? { uid, ...d.data() } : null; },
    subscribe(me, on) {
      const offs = [];
      const listen = (q, fn) => offs.push(F.onSnapshot(q, fn, (err) => console.warn(err)));
      listen(F.doc(db, "users", me.uid), (d) => d.exists() && on.me({ uid: me.uid, ...d.data() }));
      listen(F.query(F.collection(db, "users", me.uid, "inbox"), F.orderBy("createdAt", "desc"), F.limit(60)),
        (s) => on.inbox(s.docs.map((d) => ({ id: d.id, ...d.data() }))));
      if (me.status !== "active") return () => offs.forEach((f) => f());
      if (me.role === "admin") {
        listen(F.query(F.collectionGroup(db, "scans"), F.limit(2000)), (s) => on.scans(s.docs.map(scanFrom)));
        listen(F.collection(db, "users"), (s) => on.users(s.docs.map((d) => ({ uid: d.id, ...d.data() }))));
        listen(F.collection(db, "dataset"), (s) => on.dataset(s.docs.map((d) => ({ id: d.id, ...d.data() }))));
      } else if (me.role === "owner" || me.role === "technician") {
        listen(F.query(F.collection(db, "farms", me.farmCode, "scans"), F.orderBy("createdAt", "desc"), F.limit(1500)), (s) => on.scans(s.docs.map(scanFrom)));
        listen(F.query(F.collection(db, "users"), F.where("farmCode", "==", me.farmCode)), (s) => on.users(s.docs.map((d) => ({ uid: d.id, ...d.data() }))));
        if (me.role === "technician") listen(F.collection(db, "dataset"), (s) => on.dataset(s.docs.map((d) => ({ id: d.id, ...d.data() }))));
      } else {
        listen(F.query(F.collection(db, "farms", me.farmCode, "scans"), F.where("userId", "==", me.uid)), (s) => on.scans(s.docs.map(scanFrom)));
      }
      if (me.farmCode) listen(F.query(F.collection(db, "farms", me.farmCode, "announcements"), F.orderBy("createdAt", "desc"), F.limit(50)),
        (s) => on.news(s.docs.map((d) => ({ id: d.id, ...d.data() }))));
      return () => offs.forEach((f) => f());
    },
    updateScan: (s, fields) => F.setDoc(F.doc(db, s._path), { ...fields, updatedAt: Date.now() }, { merge: true }),
    setDataset: async (s, on, me) => {
      const ref = F.doc(db, "dataset", s.uuid || s.id);
      if (on) await F.setDoc(ref, { scanUuid: s.uuid || s.id, imageUrl: s.imageUrl || "", label: finalLabel(s), verifiedBy: me.name, farmCode: s.farmCode, createdAt: Date.now() });
      else await F.deleteDoc(ref);
      await F.setDoc(F.doc(db, s._path), { inDataset: on }, { merge: true });
    },
    updateUser: (uid, fields) => F.setDoc(F.doc(db, "users", uid), fields, { merge: true }),
    postNews: (farm, data) => F.addDoc(F.collection(db, "farms", farm, "announcements"), data),
    sendInbox: async (uids, data) => {
      const b = F.writeBatch(db);
      [...new Set(uids)].forEach((u) => b.set(F.doc(F.collection(db, "users", u, "inbox")), { ...data, createdAt: Date.now(), read: false }));
      await b.commit();
    },
    markRead: (uid, id) => F.setDoc(F.doc(db, "users", uid, "inbox", id), { read: true }, { merge: true }),
  };
}

// ---------------------------------------------------------------- demo mode (sample records only)
function demoApi() {
  let seed = 11; const rnd = () => (seed = (seed * 16807) % 2147483647) / 2147483647;
  const farm = "DAKO-TAGAYTAY";
  const users = [
    { uid: "u-owner", name: "Rosa Mendoza", role: "owner", gender: "Female", ageRange: "46–55", yearsFarming: "More than 10 years", location: "Tagaytay City" },
    { uid: "u-tech", name: "Ramon Villanueva", role: "technician", gender: "Male", ageRange: "26–35", yearsFarming: "6–10 years", location: "Silang, Cavite" },
    { uid: "u-farmer", name: "Juan Dela Cruz", role: "farmer", gender: "Male", ageRange: "36–45", yearsFarming: "6–10 years", location: "Brgy. Iruhin" },
    { uid: "u-farmer2", name: "Pedro Reyes", role: "farmer", gender: "Male", ageRange: "26–35", yearsFarming: "1–5 years", location: "Brgy. Iruhin" },
    { uid: "u-farmer3", name: "Liza Garcia", role: "farmer", gender: "Female", ageRange: "18–25", yearsFarming: "Less than 1 year", location: "Brgy. Sungay", status: "pending" },
    { uid: "u-vendor", name: "Maria Santos", role: "vendor", gender: "Female", ageRange: "36–45", yearsFarming: "1–5 years", location: "Tagaytay Public Market" },
    { uid: "u-admin", name: "System Administrator", role: "admin", gender: "Prefer not to say", ageRange: "26–35", yearsFarming: "Not applicable", location: "ASIATECH" },
  ].map((u) => ({ email: u.uid.replace("u-", "") + "@demo.ph", farmCode: farm, farmName: "Dako Pineapple Farm", status: "active", createdAt: Date.now() - 90 * 864e5, photoUrl: "", ...u }));
  const thumb = (label) => {
    const c = cls(label).color, h = label === "healthy";
    return "data:image/svg+xml," + encodeURIComponent(`<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 120 120'><defs><linearGradient id='g' x1='0' y1='0' x2='1' y2='1'><stop offset='0' stop-color='#3f7a28'/><stop offset='1' stop-color='#9ccc65'/></linearGradient></defs><rect width='120' height='120' fill='url(#g)'/><path d='M60 10l8 30-8-6-8 6z M60 22h24L66 44z M60 22H36l18 22z' fill='#2e5e1f'/><ellipse cx='60' cy='78' rx='28' ry='34' fill='${h ? "#F4B400" : "#d9a640"}'/>${h ? "" : `<circle cx='68' cy='82' r='13' fill='${c}' opacity='.85'/><circle cx='50' cy='70' r='7' fill='${c}' opacity='.6'/>`}</svg>`);
  };
  const weights = [10, 34, 8, 11, 6, 7];
  const pick = () => { let r = rnd() * 76, i = 0; while ((r -= weights[i]) > 0) i++; return CLASSES[i].label; };
  const workers = users.filter((u) => u.role === "farmer" || u.role === "technician");
  const blocks = ["Block A", "Block B", "Block C", "Block D"];
  const parts = { fruit_rot: "Fruit", heart_rot: "Heart / center", mealybug_wilt: "Leaf", multiple_crown_disorder: "Crown", root_rot: "Base / roots", healthy: "Whole plant" };
  const scans = Array.from({ length: 86 }, (_, i) => {
    const label = pick(), healthy = label === "healthy", w = workers[i % workers.length];
    const created = Date.now() - Math.floor(rnd() * 56) * 864e5 - Math.floor(rnd() * 20) * 36e5;
    const conf = 0.58 + rnd() * 0.41;
    const reviewed = !healthy && rnd() < 0.6;
    const corrected = reviewed && rnd() < 0.12;
    const finalL = corrected ? CLASSES[(CLASSES.findIndex((c) => c.label === label) + 2) % 6].label : label;
    const probs = {}; CLASSES.forEach((c) => (probs[c.label] = c.label === label ? conf : (1 - conf) / 5));
    return {
      id: "d" + i, uuid: "d" + i, _path: "demo/" + i, userId: w.uid, userName: w.name, role: w.role, farmCode: farm,
      block: blocks[Math.floor(rnd() * (healthy ? 4 : 3))], plantPart: parts[label], predictedLabel: label, confidence: conf, probabilities: probs,
      status: healthy ? "Healthy" : ["Open", "Open", "Rechecked", "Resolved"][Math.floor(rnd() * 4)], createdAt: created,
      reviewStatus: healthy ? "none" : reviewed ? (corrected ? "corrected" : "confirmed") : "pending",
      confirmedLabel: reviewed ? finalL : "", reviewerName: reviewed ? "Ramon Villanueva" : "", advice: reviewed ? "Follow the recommended action and re-check after one week." : "",
      reviewedAt: reviewed ? created + 36e5 * 5 : null, imageUrl: thumb(label), inferenceMs: 120 + Math.floor(rnd() * 90), notes: "", inDataset: reviewed && rnd() < 0.5,
    };
  }).sort((a, b) => b.createdAt - a.createdAt);
  const dataset = scans.filter((s) => s.inDataset).map((s) => ({ id: s.uuid, scanUuid: s.uuid, imageUrl: s.imageUrl, label: finalLabel(s), verifiedBy: s.reviewerName, farmCode: farm, createdAt: s.reviewedAt }));
  const news = [
    { id: "n1", title: "Spraying in Block B on Friday", body: "Please avoid Block B from 7 to 10 AM. Wear masks if you need to pass.", fromName: "Rosa Mendoza", fromRole: "owner", createdAt: Date.now() - 2 * 864e5 },
    { id: "n2", title: "Check for mealybugs after the rain", body: "Look under the leaves and around the base. Scan any reddish leaves.", fromName: "Ramon Villanueva", fromRole: "technician", createdAt: Date.now() - 6 * 864e5 },
  ];
  const inbox = [
    { id: "i1", title: "New scan: Mealybug Wilt", body: "Juan Dela Cruz scanned a plant in Block C (87% confidence). Please review it.", type: "new_scan", createdAt: Date.now() - 40 * 6e4, read: false },
    { id: "i2", title: "Possible Fruit Rot outbreak", body: "3 plants in Block A were found with Fruit Rot in the last 14 days.", type: "outbreak", createdAt: Date.now() - 5 * 36e5, read: false },
    { id: "i3", title: "New member request", body: "Liza Garcia (Farmer) asked to join your farm.", type: "account", createdAt: Date.now() - 864e5, read: true },
  ];
  let on = null, viewer = null;
  const push = () => {
    if (!on || !viewer) return;
    const staff = ["owner", "technician", "admin"].includes(viewer.role);
    on.scans(staff ? [...scans] : scans.filter((s) => s.userId === viewer.uid));
    on.users(viewer.role === "owner" || viewer.role === "admin" ? [...users] : users.filter((u) => u.uid === viewer.uid));
    on.news([...news]);
    on.dataset(viewer.role === "technician" || viewer.role === "admin" ? [...dataset] : []);
    on.inbox(staff ? [...inbox] : inbox.filter((n) => n.type !== "new_scan" && n.type !== "account"));
  };
  return {
    demo: true, users,
    onAuth: (cb) => cb({ uid: "u-owner" }),
    login: async () => {}, logout: async () => location.reload(), reset: async () => {},
    profile: async (uid) => users.find((u) => u.uid === uid),
    subscribe(me, handlers) { on = handlers; viewer = me; push(); return () => (on = null); },
    updateScan: async (s, f) => { Object.assign(scans.find((x) => x.id === s.id), f); push(); },
    setDataset: async (s, v, me) => {
      Object.assign(scans.find((x) => x.id === s.id), { inDataset: v });
      const i = dataset.findIndex((d) => d.scanUuid === s.uuid);
      if (v && i < 0) dataset.push({ id: s.uuid, scanUuid: s.uuid, imageUrl: s.imageUrl, label: finalLabel(s), verifiedBy: me.name, farmCode: farm, createdAt: Date.now() });
      if (!v && i >= 0) dataset.splice(i, 1);
      push();
    },
    updateUser: async (uid, f) => { Object.assign(users.find((u) => u.uid === uid), f); push(); },
    postNews: async (f, d) => { news.unshift({ id: "n" + Date.now(), ...d }); push(); },
    sendInbox: async () => {},
    markRead: async (uid, id) => { const n = inbox.find((x) => x.id === id); if (n) n.read = true; push(); },
  };
}

// ---------------------------------------------------------------- navigation
const PAGES = {
  overview: { icon: "📊", roles: ["farmer", "vendor", "technician", "owner", "admin"] },
  scans: { icon: "🍍", roles: ["farmer", "vendor", "technician", "owner", "admin"] },
  review: { icon: "✅", roles: ["technician", "owner", "admin"] },
  team: { icon: "👥", roles: ["owner", "admin"] },
  news: { icon: "📣", roles: ["farmer", "vendor", "technician", "owner", "admin"] },
  dataset: { icon: "🖼️", roles: ["technician", "admin"] },
  profile: { icon: "👤", roles: ["farmer", "vendor", "technician", "owner", "admin"] },
};
function pageTitle(p) {
  if (p === "scans" && !["owner", "technician", "admin"].includes(S.me.role)) return t("myScans");
  if (p === "team" && S.me.role === "admin") return t("users");
  return t(p);
}
function renderNav() {
  const pendingCount = S.users.filter((u) => u.status === "pending").length;
  const reviewCount = S.scans.filter((s) => s.reviewStatus === "pending").length;
  $("nav").innerHTML = Object.entries(PAGES).filter(([, v]) => v.roles.includes(S.me.role)).map(([k, v]) => {
    const badge = k === "review" && reviewCount ? `<em>${reviewCount}</em>` : k === "team" && pendingCount ? `<em>${pendingCount}</em>` : "";
    return `<a href="#${k}" class="${S.page === k ? "on" : ""}"><span>${v.icon}</span>${esc(pageTitle(k))}${badge}</a>`;
  }).join("");
}
function go() {
  const p = (location.hash || "#overview").slice(1);
  S.page = PAGES[p] && PAGES[p].roles.includes(S.me.role) ? p : "overview";
  S.pageNo = 0;
  $("side").classList.remove("open");
  render();
}

// ---------------------------------------------------------------- rendering
function applyStatic() {
  document.querySelectorAll("[data-t]").forEach((el) => (el.textContent = t(el.dataset.t)));
  document.querySelectorAll("[data-lang]").forEach((b) => b.classList.toggle("on", b.dataset.lang === lang));
  document.documentElement.lang = lang === "tl" ? "fil" : "en";
}
function render() {
  if (!S.me) return;
  applyStatic();
  renderNav();
  $("pageTitle").textContent = pageTitle(S.page);
  $("meName").textContent = S.me.name || S.me.email;
  $("meRole").textContent = rname(S.me.role);
  $("meAvatar").src = avatar(S.me.photoUrl);
  $("farmLabel").textContent = S.me.role === "admin" ? t("allFarms") : (S.me.farmName || S.me.farmCode || "");
  $("pendingBanner").hidden = S.me.status === "active";
  const unread = S.inbox.filter((n) => !n.read).length;
  $("bellCount").hidden = !unread; $("bellCount").textContent = unread;
  Object.values(charts).forEach((c) => c.destroy()); charts = {};
  const v = $("view");
  ({ overview, scans: scansPage, review: reviewPage, team: teamPage, news: newsPage, dataset: datasetPage, profile: profilePage })[S.page](v);
  renderInbox();
}

function farmScans() { return S.scans.filter((s) => !s.batchId && (!S.f.farm || s.farmCode === S.f.farm)); }

function overview(v) {
  const sc = farmScans();
  const total = sc.length, healthy = sc.filter(isHealthy).length, rev = sc.filter((s) => s.reviewStatus === "pending").length;
  const open = sc.filter((s) => s.status === "Open").length;
  const since = Date.now() - 14 * 864e5, groups = {};
  sc.filter((s) => !isHealthy(s) && s.block && s.createdAt >= since).forEach((s) => { const k = finalLabel(s) + "|" + s.block; groups[k] = (groups[k] || 0) + 1; });
  const outbreaks = Object.entries(groups).filter(([, n]) => n >= 3).sort((a, b) => b[1] - a[1]);
  const reviewed = sc.filter((s) => s.reviewStatus === "confirmed" || s.reviewStatus === "corrected");
  const agree = reviewed.length ? reviewed.filter((s) => s.reviewStatus === "confirmed").length / reviewed.length : null;
  const blocks = [...new Set(sc.map((s) => s.block).filter(Boolean))].sort();
  const farms = [...new Set(S.scans.map((s) => s.farmCode).filter(Boolean))].sort();
  v.innerHTML = `
  ${S.me.role === "admin" ? `<div class="filters"><select id="farmSel"><option value="">${t("allFarms")}</option>${farms.map((f) => `<option ${S.f.farm === f ? "selected" : ""}>${esc(f)}</option>`).join("")}</select></div>` : ""}
  <div class="grid g4">
    ${kpi("🌱", total, t("plants"), "var(--soft-green)")}
    ${kpi("💚", total ? Math.round((healthy * 100) / total) + "%" : "—", t("healthyPct"), "var(--mint)")}
    ${kpi("🩺", total - healthy, t("diseased"), "var(--soft-red)")}
    ${["owner", "technician", "admin"].includes(S.me.role) ? kpi("📝", rev, t("forReview"), "var(--soft-blue)") : kpi("⏰", open, t("open"), "var(--soft-gold)")}
  </div>
  <div class="grid g2" style="margin-top:16px">
    <div class="card"><h3>${t("perClass")}</h3><canvas id="cClass" height="230"></canvas></div>
    <div class="card"><h3>${t("reviewStatus")}</h3><canvas id="cReview" height="230"></canvas>
      <p class="muted" style="margin:8px 0 0">${t("accuracy")}: <b>${agree === null ? "—" : pct(agree)}</b></p></div>
  </div>
  <div class="grid g2" style="margin-top:16px">
    <div class="card"><h3>${t("perWeek")}</h3><canvas id="cWeek" height="200"></canvas></div>
    <div class="card"><h3>⚠️ ${t("outbreaks")}</h3>${outbreaks.length ? outbreaks.map(([k, n]) => {
      const [l, b] = k.split("|"); return `<div class="box t-red"><b>${esc(cname(l))}</b> — ${esc(b)}: ${n}</div>`; }).join("") : `<p class="muted">${t("noOutbreak")}</p>`}</div>
  </div>
  <div class="card" style="margin-top:16px"><h3>${t("blocks")}</h3><div class="table-wrap"><table class="heat"><thead><tr><th>${t("block")}</th>${CLASSES.filter((c) => c.label !== "healthy").map((c) => `<th>${esc(cname(c.label))}</th>`).join("")}<th>Total</th></tr></thead><tbody>
    ${blocks.map((b) => { const row = CLASSES.filter((c) => c.label !== "healthy").map((c) => sc.filter((s) => s.block === b && finalLabel(s) === c.label).length);
      const sum = row.reduce((a, x) => a + x, 0); const max = Math.max(1, ...row);
      return `<tr><td><b>${esc(b)}</b></td>${row.map((n) => `<td class="h" style="background:rgba(198,40,40,${n ? 0.08 + 0.5 * n / max : 0})">${n || ""}</td>`).join("")}<td class="h">${sum}</td></tr>`; }).join("") || `<tr><td colspan="7" class="empty">—</td></tr>`}
  </tbody></table></div></div>
  <div class="card" style="margin-top:16px"><h3>${t("latest")}</h3>${scanTable(sc.slice(0, 8), false)}</div>`;
  const fs = $("farmSel"); if (fs) fs.onchange = () => { S.f.farm = fs.value; render(); };
  bindRows(v);
  if (!window.Chart) return;
  charts.c = new Chart($("cClass"), { type: "bar", data: { labels: CLASSES.map((c) => cname(c.label)), datasets: [{ data: CLASSES.map((c) => sc.filter((s) => finalLabel(s) === c.label).length), backgroundColor: CLASSES.map((c) => c.color), borderRadius: 6 }] },
    options: { aspectRatio: 1.9, indexAxis: "y", plugins: { legend: { display: false } }, scales: { x: { beginAtZero: true, ticks: { precision: 0 } } } } });
  const rs = ["pending", "confirmed", "corrected"].map((k) => sc.filter((s) => s.reviewStatus === k).length);
  charts.r = new Chart($("cReview"), { type: "doughnut", data: { labels: [t("pending"), t("confirmed"), t("corrected")], datasets: [{ data: rs, backgroundColor: ["#1565C0", "#4C9A2A", "#F4B400"] }] },
    options: { aspectRatio: 1.6, plugins: { legend: { position: "bottom" } }, cutout: "62%" } });
  const wk = 7 * 864e5, now = Date.now(), weeks = [7, 6, 5, 4, 3, 2, 1, 0];
  charts.w = new Chart($("cWeek"), { type: "bar", data: { labels: weeks.map((w) => (w ? `-${w}w` : lang === "tl" ? "Ngayon" : "Now")),
    datasets: [{ label: t("healthyPct"), data: weeks.map((w) => sc.filter((s) => isHealthy(s) && s.createdAt > now - (w + 1) * wk && s.createdAt <= now - w * wk).length), backgroundColor: "#4C9A2A", borderRadius: 4 },
      { label: t("diseased"), data: weeks.map((w) => sc.filter((s) => !isHealthy(s) && s.createdAt > now - (w + 1) * wk && s.createdAt <= now - w * wk).length), backgroundColor: "#C62828", borderRadius: 4 }] },
    options: { aspectRatio: 2.2, plugins: { legend: { position: "bottom" } }, scales: { x: { stacked: true }, y: { stacked: true, beginAtZero: true, ticks: { precision: 0 } } } } });
}
function kpi(ic, val, label, bg) { return `<div class="card kpi"><div class="ic" style="background:${bg}">${ic}</div><div><b>${esc(val)}</b><span>${esc(label)}</span></div></div>`; }

function scanTable(list, full = true) {
  if (!list.length) return `<div class="empty">${t("noScans")}</div>`;
  return `<div class="table-wrap"><table><thead><tr><th></th><th>${t("date")}</th><th>${t("result")}</th><th>${t("conf")}</th><th>${t("block")}</th>${full ? `<th>${t("part")}</th>` : ""}<th>${t("by")}</th><th>${t("status")}</th><th>${t("reviewCol")}</th></tr></thead><tbody>
  ${list.map((s) => `<tr class="click" data-id="${esc(s.id)}"><td>${s.imageUrl ? `<img class="thumb" src="${esc(s.imageUrl)}" alt="" loading="lazy">` : '<div class="thumb"></div>'}</td>
    <td>${fmt(s.createdAt)}</td><td class="${isHealthy(s) ? "ok" : "dis"}">${esc(cname(finalLabel(s)))}</td><td>${pct(s.confidence)}</td><td>${esc(s.block || "—")}</td>
    ${full ? `<td>${esc(s.plantPart || "")}</td>` : ""}<td>${esc(s.userName || "")}</td><td>${statusTag(s.status)}</td><td>${reviewTag(s.reviewStatus)}</td></tr>`).join("")}
  </tbody></table></div>`;
}
function bindRows(root) { root.querySelectorAll("tr.click").forEach((tr) => (tr.onclick = () => openScan(tr.dataset.id))); }

function filtered() {
  const f = S.f, q = f.q.toLowerCase();
  return S.scans.filter((s) => (!f.c || finalLabel(s) === f.c) && (!f.st || s.status === f.st) && (!f.r || s.reviewStatus === f.r) && (!f.b || s.block === f.b) &&
    (!f.farm || s.farmCode === f.farm) && (!q || `${s.userName} ${s.block} ${s.notes} ${s.advice} ${s.plantPart}`.toLowerCase().includes(q)));
}
function scansPage(v) {
  const list = filtered(), size = 25, pages = Math.max(1, Math.ceil(list.length / size));
  S.pageNo = Math.min(S.pageNo, pages - 1);
  const blocks = [...new Set(S.scans.map((s) => s.block).filter(Boolean))].sort();
  const farms = [...new Set(S.scans.map((s) => s.farmCode).filter(Boolean))].sort();
  v.innerHTML = `<div class="card">
    <div class="filters">
      <input id="fq" placeholder="${t("searchPh")}" value="${esc(S.f.q)}">
      <select id="fc"><option value="">${t("allClasses")}</option>${CLASSES.map((c) => `<option value="${c.label}" ${S.f.c === c.label ? "selected" : ""}>${esc(cname(c.label))}</option>`).join("")}</select>
      <select id="fst"><option value="">${t("allStatus")}</option>${["Open", "Rechecked", "Resolved", "Healthy"].map((x) => `<option value="${x}" ${S.f.st === x ? "selected" : ""}>${t(x)}</option>`).join("")}</select>
      <select id="fr"><option value="">${t("allReview")}</option>${["pending", "confirmed", "corrected"].map((x) => `<option value="${x}" ${S.f.r === x ? "selected" : ""}>${t(x)}</option>`).join("")}</select>
      <select id="fb"><option value="">${t("allBlocks")}</option>${blocks.map((b) => `<option ${S.f.b === b ? "selected" : ""}>${esc(b)}</option>`).join("")}</select>
      ${S.me.role === "admin" ? `<select id="ff"><option value="">${t("allFarms")}</option>${farms.map((f) => `<option ${S.f.farm === f ? "selected" : ""}>${esc(f)}</option>`).join("")}</select>` : ""}
      <button class="btn ghost" id="csv">⬇ ${t("exportCsv")}</button>
    </div>
    ${scanTable(list.slice(S.pageNo * size, S.pageNo * size + size))}
    <div class="pager"><span>${t("showing")} ${list.length ? S.pageNo * size + 1 : 0}–${Math.min(list.length, (S.pageNo + 1) * size)} ${t("of")} ${list.length}</span>
      <span><button class="btn ghost" id="pp" ${S.pageNo ? "" : "disabled"}>${t("prev")}</button> <button class="btn ghost" id="np" ${S.pageNo < pages - 1 ? "" : "disabled"}>${t("next")}</button></span></div>
  </div>`;
  const bind = (id, key) => { const el = $(id); if (el) el.oninput = () => { S.f[key] = el.value; S.pageNo = 0; if (id === "fq") { clearTimeout(bind._t); bind._t = setTimeout(render, 250); } else render(); }; };
  bind("fq", "q"); bind("fc", "c"); bind("fst", "st"); bind("fr", "r"); bind("fb", "b"); bind("ff", "farm");
  $("pp").onclick = () => { S.pageNo--; render(); }; $("np").onclick = () => { S.pageNo++; render(); };
  $("csv").onclick = () => csvDownload("go_ananas_scans.csv", ["date", "farm", "condition", "app_result", "confidence", "block", "plant_part", "scanned_by", "status", "review", "reviewer", "advice", "notes", "image_url"],
    list.map((s) => [new Date(s.createdAt).toISOString(), s.farmCode, cls(finalLabel(s)).en, cls(s.predictedLabel).en, (s.confidence || 0).toFixed(4), s.block, s.plantPart, s.userName, s.status, s.reviewStatus, s.reviewerName, s.advice, s.notes, s.imageUrl]));
  bindRows(v);
  const q = $("fq"); if (document.activeElement !== q && S.f.q) { q.focus(); q.setSelectionRange(q.value.length, q.value.length); }
}

function reviewPage(v) {
  const queue = S.scans.filter((s) => s.reviewStatus === "pending").sort((a, b) => a.createdAt - b.createdAt);
  const done = S.scans.filter((s) => s.reviewStatus === "confirmed" || s.reviewStatus === "corrected");
  v.innerHTML = `<div class="grid g3">${kpi("⏳", queue.length, t("pending"), "var(--soft-blue)")}${kpi("✅", done.length, t("confirmed") + " / " + t("corrected"), "var(--soft-green)")}
    ${kpi("🎯", done.length ? pct(done.filter((s) => s.reviewStatus === "confirmed").length / done.length) : "—", t("accuracy"), "var(--soft-gold)")}</div>
    <div class="card" style="margin-top:16px">${scanTable(queue)}</div>`;
  bindRows(v);
}

function openScan(id) {
  const s = S.scans.find((x) => x.id === id); if (!s) return;
  const me = S.me, canReview = ["technician", "owner", "admin"].includes(me.role), mine = s.userId === me.uid;
  const fl = finalLabel(s), c = cls(fl), probs = s.probabilities || {};
  let choice = fl;
  const ranked = CLASSES.map((k) => [k, Number(probs[k.label] || 0)]).sort((a, b) => b[1] - a[1]);
  $("detailBody").innerHTML = `<div class="detail">
    <div class="photo">${s.imageUrl ? `<img src="${esc(s.imageUrl)}" alt="">` : ""}</div>
    <div class="info">
      <div class="row" style="justify-content:space-between"><span class="muted">${fmt(s.createdAt)}</span><button class="icon" id="dClose">✕</button></div>
      <h2 style="color:${fl === "healthy" ? "var(--fresh)" : "var(--red)"}">${esc(cname(fl))}</h2>
      ${s.confirmedLabel && s.confirmedLabel !== s.predictedLabel ? `<div class="muted">${t("appResult")}: ${esc(cname(s.predictedLabel))}</div>` : ""}
      <div>${pct(s.confidence)} • ${esc(s.plantPart || "")} • ${esc(s.block || "—")} • ${esc(s.userName || "")}${me.role === "admin" ? " • " + esc(s.farmCode) : ""}</div>
      <div class="row" style="margin:8px 0">${statusTag(s.status)} ${reviewTag(s.reviewStatus)} ${s.inDataset ? `<span class="tag t-plum">${t("inDataset")}</span>` : ""}</div>
      <div class="box t-${fl === "healthy" ? "green" : "red"}"><b>${t("whatToDo")}</b><br>${esc(ACTIONS[fl] ? ACTIONS[fl][lang === "tl" ? 1 : 0] : "")}</div>
      ${s.reviewerName ? `<div class="box t-blue"><b>${t("reviewedBy")} ${esc(s.reviewerName)}</b>${s.advice ? "<br>" + esc(s.advice) : ""}</div>` : ""}
      ${s.notes ? `<p><b>${t("notes")}:</b> ${esc(s.notes)}</p>` : ""}
      <h4 style="margin:12px 0 4px">${t("breakdown")}</h4>
      ${ranked.map(([k, p], i) => `<div class="row" style="justify-content:space-between;font-size:14px"><span>${esc(cname(k.label))}</span><b>${pct(p)}</b></div><div class="bar"><i style="width:${(p * 100).toFixed(1)}%;background:${i === 0 ? k.color : "#9E9E9E"}"></i></div>`).join("")}
      ${canReview ? `<h4 style="margin:14px 0 4px">${t("reviewThis")}</h4><div class="muted">${t("rightCondition")}</div>
        <div class="chips" id="dChips">${CLASSES.map((k) => `<button class="chip ${k.label === fl ? "on" : ""}" data-l="${k.label}">${esc(cname(k.label))}</button>`).join("")}</div>
        <textarea id="dAdvice" rows="3" placeholder="${t("advice")}">${esc(s.advice || "")}</textarea>
        <div class="row" style="margin-top:8px"><button class="btn primary" id="dReview">${t("confirm")}</button>
        ${["technician", "admin"].includes(me.role) && s.reviewStatus !== "pending" && s.reviewStatus !== "none" ? `<button class="btn ghost" id="dData">${s.inDataset ? t("removeDataset") : "🖼️ " + t("addDataset")}</button>` : ""}</div>` : ""}
      ${(canReview || mine) && fl !== "healthy" ? `<h4 style="margin:14px 0 4px">${t("setStatus")}</h4><div class="row">
        ${["Open", "Rechecked", "Resolved"].map((x) => `<button class="btn ${s.status === x ? "primary" : "ghost"}" data-st="${x}">${t(x)}</button>`).join("")}</div>` : ""}
    </div></div>`;
  const dlg = $("detail"); dlg.showModal();
  $("dClose").onclick = () => dlg.close();
  const rb = $("dReview");
  const setBtn = () => { if (rb) rb.textContent = choice === s.predictedLabel ? t("confirm") : t("saveCorrection"); };
  document.querySelectorAll("#dChips .chip").forEach((b) => (b.onclick = () => {
    choice = b.dataset.l; document.querySelectorAll("#dChips .chip").forEach((x) => x.classList.toggle("on", x === b)); setBtn();
  }));
  setBtn();
  if (rb) rb.onclick = async () => {
    const confirmed = choice === s.predictedLabel, advice = $("dAdvice").value.trim(), healthy = choice === "healthy";
    await api.updateScan(s, { reviewStatus: confirmed ? "confirmed" : "corrected", confirmedLabel: choice, finalLabel: choice, isHealthy: healthy,
      reviewerName: me.name, advice, reviewedAt: Date.now(), status: healthy ? "Healthy" : (s.status === "Healthy" ? "Open" : s.status) });
    if (s.userId && s.userId !== me.uid) await api.sendInbox([s.userId], { title: T[langOf(s.userId)].reviewMsg[0], body: me.name + T[langOf(s.userId)].reviewMsg[1] + cls(choice)[langOf(s.userId) === "tl" ? "tl" : "en"] + (advice ? " — " + advice : ""), type: "review", scanUuid: s.uuid || s.id });
    dlg.close(); toast(t("saved"));
  };
  const db = $("dData"); if (db) db.onclick = async () => { await api.setDataset(s, !s.inDataset, me); dlg.close(); toast(t("saved")); };
  document.querySelectorAll("[data-st]").forEach((b) => (b.onclick = async () => { await api.updateScan(s, { status: b.dataset.st }); dlg.close(); toast(t("saved")); }));
}
function langOf(uid) { const u = S.users.find((x) => x.uid === uid); return u && u.language === "tl" ? "tl" : "en"; }

function teamPage(v) {
  const admin = S.me.role === "admin";
  const pending = S.users.filter((u) => u.status === "pending"), others = S.users.filter((u) => u.status !== "pending");
  const roleOpts = (u) => (admin ? Object.keys(ROLES) : ["farmer", "vendor", "technician"]).map((r) => `<option value="${r}" ${u.role === r ? "selected" : ""}>${esc(rname(r))}</option>`).join("");
  const person = (u) => {
    const self = u.uid === S.me.uid, manage = !self && (admin || (u.role !== "owner" && u.role !== "admin"));
    const demo = [u.gender, u.ageRange && (t("age") + " " + u.ageRange), u.yearsFarming, u.location].filter(Boolean).map(esc).join(" • ");
    const scans = S.scans.filter((s) => s.userId === u.uid).length;
    return `<div class="person"><img src="${esc(avatar(u.photoUrl))}" alt=""><div class="who"><b>${esc(u.name || u.email)}</b> ${u.status === "disabled" ? `<span class="tag t-red">${t("disabledS")}</span>` : ""}
      <div class="muted" style="font-size:13px">${esc(u.email || "")}${admin ? " • " + esc(u.farmCode || "") : ""} • ${scans} scans</div><div class="muted" style="font-size:13px">${demo}</div></div>
      ${u.status === "pending" && manage ? `<button class="btn primary" data-ap="${u.uid}">${t("approve")}</button><button class="btn ghost" data-dis="${u.uid}">${t("decline")}</button>` :
      manage ? `<select data-role="${u.uid}">${roleOpts(u)}</select>${u.status === "disabled" ? `<button class="btn ghost" data-en="${u.uid}">${t("enable")}</button>` : `<button class="btn ghost" data-dis="${u.uid}">${t("disable")}</button>`}` :
      `<span class="tag t-green">${esc(rname(u.role))}</span>`}</div>`;
  };
  v.innerHTML = `${!admin ? `<div class="banner" style="margin:0 0 16px">${t("shareCode")} <b>${esc(S.me.farmCode)}</b></div>` : ""}
    ${pending.length ? `<div class="card" style="margin-bottom:16px"><h3>⏳ ${t("waiting")} (${pending.length})</h3>${pending.map(person).join("")}</div>` : ""}
    <div class="card"><h3>${t("members")} (${others.length})</h3>${others.map(person).join("")}</div>`;
  const msg = (u, pair, extra = "") => api.sendInbox([u.uid], { title: T[u.language === "tl" ? "tl" : "en"][pair][0], body: T[u.language === "tl" ? "tl" : "en"][pair][1] + extra, type: "account" });
  v.querySelectorAll("[data-ap]").forEach((b) => (b.onclick = async () => { const u = S.users.find((x) => x.uid === b.dataset.ap); await api.updateUser(u.uid, { status: "active" }); await msg(u, "approvedMsg"); toast(t("saved")); }));
  v.querySelectorAll("[data-dis]").forEach((b) => (b.onclick = async () => { await api.updateUser(b.dataset.dis, { status: "disabled" }); toast(t("saved")); }));
  v.querySelectorAll("[data-en]").forEach((b) => (b.onclick = async () => { await api.updateUser(b.dataset.en, { status: "active" }); toast(t("saved")); }));
  v.querySelectorAll("[data-role]").forEach((s) => (s.onchange = async () => {
    const u = S.users.find((x) => x.uid === s.dataset.role); await api.updateUser(u.uid, { role: s.value });
    await msg(u, "roleMsg", ROLES[s.value][u.language === "tl" ? 1 : 0]); toast(t("saved"));
  }));
}

function newsPage(v) {
  const can = ["owner", "technician", "admin"].includes(S.me.role);
  const farms = [...new Set(S.users.map((u) => u.farmCode).filter(Boolean))].sort();
  v.innerHTML = `${can ? `<div class="card" style="margin-bottom:16px"><h3>📣 ${t("send")}</h3>
      ${S.me.role === "admin" ? `<label><span>${t("chooseFarm")}</span><select id="nFarm">${farms.map((f) => `<option ${f === S.me.farmCode ? "selected" : ""}>${esc(f)}</option>`).join("")}</select></label>` : ""}
      <label><span>${t("postTitle")}</span><input id="nTitle"></label><label><span>${t("postBody")}</span><textarea id="nBody" rows="3"></textarea></label>
      <button class="btn primary" id="nSend">${t("send")}</button></div>` : ""}
    <div class="card">${S.news.length ? S.news.map((n) => `<div class="news"><b>${esc(n.title)}</b><div>${esc(n.body || "")}</div><small>${esc(n.fromName || "")} • ${esc(rname(n.fromRole))} • ${ago(n.createdAt)}</small></div>`).join("") : `<div class="empty">${t("noNews")}</div>`}</div>`;
  const b = $("nSend"); if (!b) return;
  b.onclick = async () => {
    const title = $("nTitle").value.trim(), body = $("nBody").value.trim(); if (!title) return;
    const farm = $("nFarm") ? $("nFarm").value : S.me.farmCode;
    b.disabled = true;
    await api.postNews(farm, { title, body, fromName: S.me.name, fromRole: S.me.role, createdAt: Date.now() });
    const to = S.users.filter((u) => u.farmCode === farm && u.status === "active" && u.uid !== S.me.uid).map((u) => u.uid);
    if (to.length) await api.sendInbox(to, { title: t("newsMsg") + S.me.name, body: title + (body ? " — " + body : ""), type: "announcement" });
    toast(t("sent")); render();
  };
}

function datasetPage(v) {
  const d = S.dataset;
  v.innerHTML = `<div class="grid g2"><div class="card"><h3>${t("perLabel")}</h3><canvas id="cData" height="200"></canvas></div>
      <div class="card"><h3>🧠 Model</h3><p>MobileNetV2 • TensorFlow Lite • 224 × 224 × 3</p><p class="muted">${t("retrain")}</p>
      <button class="btn primary" id="dCsv">⬇ ${t("exportDataset")}</button></div></div>
    <div class="card" style="margin-top:16px">${d.length ? `<div class="gallery">${d.map((x) => `<figure><img src="${esc(x.imageUrl)}" alt="" loading="lazy"><figcaption style="color:${cls(x.label).color}">${esc(cname(x.label))}<br><small>${esc(x.verifiedBy || "")}</small></figcaption></figure>`).join("")}</div>` : `<div class="empty">${t("noDataset")}</div>`}</div>`;
  $("dCsv").onclick = () => csvDownload("go_ananas_dataset.csv", ["label", "image_url", "verified_by", "farm", "date"], d.map((x) => [x.label, x.imageUrl, x.verifiedBy, x.farmCode, new Date(x.createdAt || 0).toISOString()]));
  if (window.Chart) charts.d = new Chart($("cData"), { type: "bar", data: { labels: CLASSES.map((c) => cname(c.label)), datasets: [{ data: CLASSES.map((c) => d.filter((x) => x.label === c.label).length), backgroundColor: CLASSES.map((c) => c.color), borderRadius: 6 }] },
    options: { aspectRatio: 2, plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true, ticks: { precision: 0 } } } } });
}

function profilePage(v) {
  const m = S.me, mine = S.scans.filter((s) => s.userId === m.uid);
  v.innerHTML = `<div class="grid g2"><div class="card"><div class="row"><img src="${esc(avatar(m.photoUrl))}" alt="" style="width:84px;height:84px;border-radius:50%;object-fit:cover">
      <div><h2 style="margin:0">${esc(m.name || "")}</h2><div class="muted">${esc(rname(m.role))} • ${esc(m.farmName || m.farmCode || "")}</div></div></div>
      <table style="margin-top:12px"><tbody>
      ${[[t("email"), m.email], [t("farm"), (m.farmName || "") + " (" + (m.farmCode || "") + ")"], [t("sex"), m.gender], [t("age"), m.ageRange], [t("exp"), m.yearsFarming], [t("location"), m.location],
        [t("account"), m.status === "active" ? t("active") : m.status === "pending" ? t("waiting") : t("disabledS")]].map(([a, b]) => `<tr><th style="width:140px">${esc(a)}</th><td>${esc(b || "—")}</td></tr>`).join("")}
      </tbody></table><p class="muted">${t("editInApp")}</p></div>
      <div class="card"><h3>${t("myScans")}</h3>${scanTable(mine.slice(0, 6), false)}</div></div>`;
  bindRows(v);
}

// ---------------------------------------------------------------- notifications
const NOTE_ICON = { recheck: ["🔁", "var(--soft-gold)"], monthly: ["📅", "var(--soft-green)"], outbreak: ["⚠️", "var(--soft-red)"], new_scan: ["📝", "var(--soft-blue)"],
  review: ["✅", "var(--soft-green)"], announcement: ["📣", "var(--soft-plum)"], account: ["👤", "var(--soft-gold)"] };
function renderInbox() {
  $("inboxList").innerHTML = S.inbox.length ? S.inbox.map((n) => { const [ic, bg] = NOTE_ICON[n.type] || ["🔔", "var(--mint)"];
    return `<div class="note ${n.read ? "" : "unread"}" data-n="${esc(n.id)}"><div class="ic" style="background:${bg}">${ic}</div><div><b>${esc(n.title)}</b><div>${esc(n.body)}</div><small>${ago(n.createdAt)}</small></div></div>`; }).join("")
    + (("Notification" in window) && Notification.permission === "default" ? `<button class="btn ghost wide" id="deskBtn" style="margin-top:10px">${t("enableDesktop")}</button>` : "")
    : `<div class="empty">${t("noNotes")}</div>`;
  document.querySelectorAll("#inboxList .note").forEach((el) => (el.onclick = () => {
    const n = S.inbox.find((x) => x.id === el.dataset.n); if (!n) return;
    if (!n.read) api.markRead(S.me.uid, n.id);
    if (n.scanUuid) { const s = S.scans.find((x) => (x.uuid || x.id) === n.scanUuid); if (s) { $("inbox").hidden = true; openScan(s.id); } }
  }));
  const d = $("deskBtn"); if (d) d.onclick = () => Notification.requestPermission().then(renderInbox);
}
function onInbox(list) {
  const known = new Set(S.inbox.map((n) => n.id));
  const fresh = list.filter((n) => !known.has(n.id) && !n.read);
  S.inbox = list;
  if (!firstInbox && fresh.length) {
    fresh.forEach((n) => { if ("Notification" in window && Notification.permission === "granted") new Notification(n.title, { body: n.body }); });
    toast("🔔 " + fresh[0].title);
  }
  firstInbox = false;
  render();
}

// ---------------------------------------------------------------- start
function setLang(l) { lang = l; try { localStorage.setItem("ga_lang", l); } catch (e) { /* ignore */ } applyStatic(); render(); }
document.querySelectorAll("[data-lang]").forEach((b) => (b.onclick = () => setLang(b.dataset.lang)));
$("menuBtn").onclick = () => $("side").classList.toggle("open");
$("bellBtn").onclick = () => { $("inbox").hidden = !$("inbox").hidden; renderInbox(); };
$("closeInbox").onclick = () => ($("inbox").hidden = true);
$("readAll").onclick = () => S.inbox.filter((n) => !n.read).forEach((n) => api.markRead(S.me.uid, n.id));
$("logoutBtn").onclick = () => { if (unsub) unsub(); api.logout(); };
window.addEventListener("hashchange", () => S.me && go());
$("loginBtn").onclick = async () => {
  $("loginErr").textContent = "";
  try { await api.login($("email").value.trim(), $("password").value); } catch (e) { $("loginErr").textContent = t("wrong"); }
};
$("password").addEventListener("keydown", (e) => e.key === "Enter" && $("loginBtn").click());
$("resetBtn").onclick = async () => { try { await api.reset($("email").value.trim()); $("loginErr").textContent = t("resetSent"); } catch (e) { $("loginErr").textContent = e.message; } };

async function start(user) {
  if (unsub) { unsub(); unsub = null; }
  if (!user) { S.me = null; $("app").hidden = true; $("login").hidden = false; applyStatic(); return; }
  const me = await api.profile(user.uid);
  if (!me || me.status === "disabled") { $("loginErr").textContent = t("noAccess"); api.logout(); return; }
  S.me = me; firstInbox = true;
  let stored = null; try { stored = localStorage.getItem("ga_lang"); } catch (e) { /* ignore */ }
  if (me.language && !stored) lang = me.language;
  $("login").hidden = true; $("app").hidden = false;
  unsub = api.subscribe(me, {
    me: (m) => { const roleChanged = m.role !== S.me.role || m.status !== S.me.status; S.me = { ...S.me, ...m }; if (roleChanged) start(user); else render(); },
    scans: (l) => { S.scans = l.sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0)); render(); },
    users: (l) => { S.users = l; render(); },
    news: (l) => { S.news = l; render(); },
    dataset: (l) => { S.dataset = l; render(); },
    inbox: onInbox,
  });
  go();
}

(async () => {
  const cfg = window.GO_ANANAS_FIREBASE;
  api = cfg ? await firebaseApi(cfg) : demoApi();
  $("demoBanner").hidden = !api.demo;
  if (api.demo) {
    const sel = $("demoRole"); sel.hidden = false;
    sel.innerHTML = api.users.map((u) => `<option value="${u.uid}">${esc(rname(u.role))} — ${esc(u.name)}</option>`).join("");
    sel.value = "u-owner";
    sel.onchange = () => { S.scans = []; S.users = []; start({ uid: sel.value }); };
  }
  api.onAuth((u) => start(u));
})();
