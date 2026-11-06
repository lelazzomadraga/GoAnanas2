package ph.edu.asiatech.goananas.data

/** the five kinds of accounts; each one sees its own functions in the app and in the web dashboard */
enum class Role(
    val key: String,
    val en: String,
    val tl: String,
    val descEn: String,
    val descTl: String,
) {
    FARMER("farmer", "Farmer / Farm worker", "Magsasaka / Manggagawa",
        "Scan plants, follow the advice, and get re-check reminders.",
        "Mag-scan ng halaman, sundin ang payo, at makatanggap ng paalala."),
    VENDOR("vendor", "Vendor", "Tindero / Tindera",
        "Check fruits before buying or selling and keep a record of each batch.",
        "Suriin ang bunga bago bilhin o ibenta at itala ang bawat batch."),
    TECHNICIAN("technician", "Agricultural Technician", "Agricultural Technician",
        "Review and confirm the scans of the farm, send advice, and build the dataset.",
        "Suriin at kumpirmahin ang mga scan ng taniman, magpadala ng payo, at buuin ang dataset."),
    OWNER("owner", "Farm Owner", "May-ari ng Taniman",
        "Monitor the whole farm, approve workers, and send announcements.",
        "Bantayan ang buong taniman, aprubahan ang mga manggagawa, at magpadala ng anunsiyo."),
    ADMIN("admin", "Administrator", "Administrator",
        "Manage all accounts, farms, the image dataset, and the model.",
        "Pamahalaan ang lahat ng account, taniman, dataset ng larawan, at model.");

    fun label(lang: String) = if (lang == "tl") tl else en
    fun desc(lang: String) = if (lang == "tl") descTl else descEn
    val canReview: Boolean get() = this == TECHNICIAN || this == OWNER || this == ADMIN
    val seesFarm: Boolean get() = this == TECHNICIAN || this == OWNER || this == ADMIN

    companion object {
        fun of(key: String?): Role = entries.firstOrNull { it.key == key } ?: FARMER
        /** roles a person can choose when registering; administrators are assigned by an existing administrator */
        val selfRegister = listOf(FARMER, VENDOR, TECHNICIAN, OWNER)
    }
}

object AccountStatus {
    const val ACTIVE = "active"
    const val PENDING = "pending"
    const val DISABLED = "disabled"
}

val GENDERS = listOf("Male" to "Lalaki", "Female" to "Babae", "Prefer not to say" to "Ayaw sabihin")
val AGE_RANGES = listOf("18–25", "26–35", "36–45", "46–55", "56 and above")
val FARMING_YEARS = listOf("Less than 1 year" to "Wala pang 1 taon", "1–5 years" to "1–5 taon", "6–10 years" to "6–10 taon", "More than 10 years" to "Higit 10 taon", "Not applicable" to "Hindi angkop")
