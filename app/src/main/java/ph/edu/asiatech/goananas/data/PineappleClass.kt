package ph.edu.asiatech.goananas.data

/**
 * The six conditions recognized by the CNN (Chapter 3). The model has a seventh output,
 * "not_pineapple", which is not a condition: it is used to reject photos that do not show a pineapple.
 * Every text has an English and a Tagalog version.
 */
enum class PineappleClass(
    val label: String,
    val displayName: String,
    val displayNameTl: String,
    val localName: String,
    val partEn: String,
    val partTl: String,
    val symptomsEn: String,
    val symptomsTl: String,
    val causeEn: String,
    val causeTl: String,
    val managementEn: List<String>,
    val managementTl: List<String>,
    val actionEn: String,
    val actionTl: String,
    /** days before the app reminds the user to re-check the plant; 0 = no re-check needed */
    val recheckDays: Int,
) {
    FRUIT_ROT(
        label = "fruit_rot",
        displayName = "Fruit Rot", displayNameTl = "Bulok na Bunga", localName = "Bulok na bunga",
        partEn = "Fruit", partTl = "Bunga",
        symptomsEn = "Dark brown, soft, water-soaked areas on the fruit; the flesh under the spot turns brown and may smell sour.",
        symptomsTl = "May maitim na kayumanggi, malambot, at basang bahagi sa bunga; nagiging kayumanggi ang laman sa ilalim at maaaring maasim ang amoy.",
        causeEn = "Fungal infection (commonly Thielaviopsis or Fusarium) that enters through wounds and spreads in wet weather.",
        causeTl = "Impeksiyon ng fungus (karaniwang Thielaviopsis o Fusarium) na pumapasok sa sugat at kumakalat kapag basa ang panahon.",
        managementEn = listOf("Remove and bury or burn infected fruits away from the field.", "Avoid wounding fruits during weeding and harvesting.", "Apply approved bio-fungicides following the label."),
        managementTl = listOf("Alisin at ibaon o sunugin ang mga apektadong bunga malayo sa taniman.", "Iwasang masugatan ang bunga habang nagdadamo at nag-aani.", "Gumamit ng aprubadong bio-fungicide ayon sa label."),
        actionEn = "Remove infected fruits immediately to prevent fungal spore dispersal. Apply approved bio-fungicides.",
        actionTl = "Alisin agad ang mga apektadong bunga upang hindi kumalat ang fungus. Gumamit ng aprubadong bio-fungicide.",
        recheckDays = 7,
    ),
    HEALTHY(
        label = "healthy",
        displayName = "Healthy", displayNameTl = "Malusog", localName = "Malusog",
        partEn = "Whole plant", partTl = "Buong halaman",
        symptomsEn = "Green, firm leaves with normal color; no rotting, wilting, or abnormal crowns.",
        symptomsTl = "Berde at matigas ang mga dahon; walang bulok, pagkalanta, o kakaibang korona.",
        causeEn = "No disease or disorder was detected on the visible parts.",
        causeTl = "Walang nakitang sakit o diperensiya sa mga nakikitang bahagi.",
        managementEn = listOf("Continue standard irrigation and balanced fertilization.", "Inspect the plants monthly, especially for mealybugs (kuto-kuto)."),
        managementTl = listOf("Ipagpatuloy ang regular na pagdidilig at balanseng pag-aabono.", "Suriin ang mga halaman buwan-buwan, lalo na kung may kuto-kuto."),
        actionEn = "Crop status: Optimal. Continue standard irrigation and balanced fertilization schedules.",
        actionTl = "Kalagayan ng pananim: Maayos. Ipagpatuloy ang regular na pagdidilig at pag-aabono.",
        recheckDays = 0,
    ),
    HEART_ROT(
        label = "heart_rot",
        displayName = "Heart Rot", displayNameTl = "Bulok sa Puso", localName = "Bulok sa puso",
        partEn = "Heart / center leaves", partTl = "Puso / gitnang dahon",
        symptomsEn = "Young center leaves turn yellow to light brown; their bases become soft and water-soaked and can be pulled out easily. Also called crown or top rot.",
        symptomsTl = "Naninilaw hanggang mapusyaw na kayumanggi ang mga batang dahon sa gitna; lumalambot ang puno ng dahon at madaling mabunot. Tinatawag ding bulok sa korona.",
        causeEn = "Phytophthora species, favored by poorly drained soil and heavy rain (Joy & Sindhu, 2012).",
        causeTl = "Phytophthora na lumalala sa lupang hindi umaagos ang tubig at sa malakas na ulan (Joy & Sindhu, 2012).",
        managementEn = listOf("Remove and destroy the infected heart and whorl tissue.", "Improve field drainage and avoid low, wet areas.", "Apply copper-based fungicides following the label."),
        managementTl = listOf("Alisin at sirain ang apektadong puso ng halaman.", "Ayusin ang daluyan ng tubig at iwasan ang mababa at basang lugar.", "Gumamit ng copper-based na fungicide ayon sa label."),
        actionEn = "Remove and destroy infected heart/whorl tissue. Apply copper-based fungicides and improve drainage.",
        actionTl = "Alisin at sirain ang apektadong puso ng halaman. Gumamit ng copper-based na fungicide at ayusin ang daluyan ng tubig.",
        recheckDays = 7,
    ),
    MEALYBUG_WILT(
        label = "mealybug_wilt",
        displayName = "Mealybug Wilt", displayNameTl = "Pagkalanta dahil sa Kuto-kuto", localName = "Pagkalanta dahil sa kuto-kuto",
        partEn = "Leaves", partTl = "Dahon",
        symptomsEn = "Leaves turn reddish to yellow, the tips dry, and the leaves curl downward and wilt.",
        symptomsTl = "Namumula hanggang naninilaw ang mga dahon, natutuyo ang dulo, at kumukulot pababa at nalalanta.",
        causeEn = "A virus spread by mealybugs (kuto-kuto), which are protected by ants (PlantVillage, n.d.).",
        causeTl = "Virus na ikinakalat ng kuto-kuto na pinoprotektahan ng mga langgam (PlantVillage, n.d.).",
        managementEn = listOf("Quarantine affected plants and check nearby plants for mealybugs.", "Control the ants that protect the mealybugs.", "Apply systemic insecticides or introduce natural predators."),
        managementTl = listOf("Ihiwalay ang apektadong halaman at suriin ang mga katabi kung may kuto-kuto.", "Puksain ang mga langgam na nagpoprotekta sa kuto-kuto.", "Gumamit ng systemic insecticide o natural na kaaway ng kuto-kuto."),
        actionEn = "Quarantine affected plants. Apply systemic insecticides or introduce natural predators.",
        actionTl = "Ihiwalay ang apektadong halaman. Gumamit ng systemic insecticide o natural na kaaway ng kuto-kuto.",
        recheckDays = 14,
    ),
    MULTIPLE_CROWN(
        label = "multiple_crown_disorder",
        displayName = "Multiple Crown Disorder", displayNameTl = "Maraming Korona", localName = "Maraming korona",
        partEn = "Crown", partTl = "Korona",
        symptomsEn = "Two or more crowns, or a flat fan-shaped crown (fasciation), grow on top of one fruit.",
        symptomsTl = "Dalawa o higit pang korona, o pipi at hugis-pamaypay na korona, ang tumutubo sa iisang bunga.",
        causeEn = "A growth disorder linked to genetics, nutrient imbalance (calcium and zinc), and growing conditions.",
        causeTl = "Diperensiya sa paglaki na may kaugnayan sa lahi, kakulangan sa sustansiya (calcium at zinc), at kondisyon ng taniman.",
        managementEn = listOf("Monitor calcium and zinc through soil and foliar tests.", "Use planting materials from plants without the disorder."),
        managementTl = listOf("Bantayan ang calcium at zinc sa pamamagitan ng pagsusuri sa lupa at dahon.", "Gumamit ng pananim mula sa halamang walang ganitong diperensiya."),
        actionEn = "Monitor calcium and zinc. Conduct routine soil and foliar tests.",
        actionTl = "Bantayan ang calcium at zinc. Regular na suriin ang lupa at dahon.",
        recheckDays = 30,
    ),
    ROOT_ROT(
        label = "root_rot",
        displayName = "Root Rot", displayNameTl = "Bulok na Ugat", localName = "Bulok na ugat",
        partEn = "Base and roots", partTl = "Puno at ugat",
        symptomsEn = "Leaves turn yellow and dry from the tips; the roots and base of the stem rot, and the plant is easily pulled out.",
        symptomsTl = "Naninilaw at natutuyo ang dahon mula sa dulo; nabubulok ang ugat at puno, at madaling mabunot ang halaman.",
        causeEn = "Phytophthora species and root-feeding pests, favored by waterlogged soil.",
        causeTl = "Phytophthora at mga pesteng kumakain ng ugat; lumalala sa lupang laging may tubig.",
        managementEn = listOf("Improve field drainage immediately and avoid waterlogging.", "Remove badly affected plants.", "Treat the soil with fungicides following the label."),
        managementTl = listOf("Ayusin agad ang daluyan ng tubig at iwasang bumaha sa taniman.", "Alisin ang malubhang apektadong halaman.", "Gamutin ang lupa ng fungicide ayon sa label."),
        actionEn = "Improve field drainage immediately. Avoid waterlogging and treat soil with fungicides.",
        actionTl = "Ayusin agad ang daluyan ng tubig. Iwasang bumaha at gamutin ang lupa ng fungicide.",
        recheckDays = 7,
    );

    val isHealthy: Boolean get() = this == HEALTHY

    fun title(lang: String): String = if (lang == "tl") displayNameTl else displayName
    fun part(lang: String): String = if (lang == "tl") partTl else partEn
    fun symptoms(lang: String): String = if (lang == "tl") symptomsTl else symptomsEn
    fun cause(lang: String): String = if (lang == "tl") causeTl else causeEn
    fun management(lang: String): List<String> = if (lang == "tl") managementTl else managementEn
    fun action(lang: String): String = if (lang == "tl") actionTl else actionEn

    companion object {
        /** the six conditions in the order used for charts and the confidence breakdown */
        val modelOrder: List<PineappleClass> = listOf(FRUIT_ROT, HEALTHY, HEART_ROT, MEALYBUG_WILT, MULTIPLE_CROWN, ROOT_ROT)
        fun fromLabel(label: String): PineappleClass = entries.firstOrNull { it.label == label } ?: HEALTHY
        const val NOT_PINEAPPLE = "not_pineapple"
    }
}

/** plant parts the user can choose when taking a photograph (English, Tagalog) */
val PLANT_PARTS: List<Pair<String, String>> = listOf(
    "Leaf" to "Dahon",
    "Crown" to "Korona",
    "Heart / center" to "Puso / gitna",
    "Base / roots" to "Puno / ugat",
    "Fruit" to "Bunga",
    "Whole plant" to "Buong halaman",
)
