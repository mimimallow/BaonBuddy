package com.example.data.model

enum class TipCategory(val label: String) {
    ALL("All"),
    DAILY_BAON("Daily Baon"),
    FOOD("Food & Canteen"),
    COMMUTE("Commute & Fare"),
    SCHOOL("School & Study"),
    SAVING_HABITS("Saving Habits")
}

data class SavingTip(
    val id: String,
    val title: String,
    val description: String,
    val impactTag: String,
    val category: TipCategory
)

object SavingTipsCatalog {
    val tips: List<SavingTip> = listOf(
        SavingTip(
            id = "tip_baon_envelope",
            title = "Pay Your Savings Goal First",
            description = "Before spending your daily baon, set aside 10% to 20% immediately. Even ₱20 a day becomes ₱400+ in a school month.",
            impactTag = "Save ~₱400/mo",
            category = TipCategory.DAILY_BAON
        ),
        SavingTip(
            id = "tip_water_tumbler",
            title = "Bring a Refillable Water Tumbler",
            description = "Buying bottled water or iced drinks twice a day quickly eats up ₱30–₱60. Carry a reusable bottle and refill at campus stations.",
            impactTag = "Save ~₱35/day",
            category = TipCategory.FOOD
        ),
        SavingTip(
            id = "tip_student_discount",
            title = "Always Present Your Student ID",
            description = "Use your 20% student discount on jeepneys, buses, MRT/LRT, and participating bookstores. Keep your school ID accessible.",
            impactTag = "20% off fares",
            category = TipCategory.COMMUTE
        ),
        SavingTip(
            id = "tip_baon_snacks",
            title = "Pack Homemade Snacks for Break Time",
            description = "Bringing biscuits, sandwiches, or fruit from home prevents expensive impulse trips to convenience stores between classes.",
            impactTag = "Save ~₱50/day",
            category = TipCategory.FOOD
        ),
        SavingTip(
            id = "tip_coin_jar",
            title = "Save All Your Loose Coins Every Night",
            description = "Empty your ₱5, ₱10, and ₱20 coins into a dedicated jar or wallet pocket every evening and log them into a BaonBuddy Savings Goal.",
            impactTag = "Effortless habit",
            category = TipCategory.SAVING_HABITS
        ),
        SavingTip(
            id = "tip_print_notes",
            title = "Share Printing & Use Digital Handouts",
            description = "Print two pages per sheet in black-and-white or review PDFs on your phone/tablet when color copies aren't required.",
            impactTag = "Cut print costs 50%",
            category = TipCategory.SCHOOL
        ),
        SavingTip(
            id = "tip_walk_short",
            title = "Walk Short Last-Mile Distances",
            description = "If a tricycle or jeepney ride is under 10 minutes of walking and safe, walking saves ₱15–₱25 each way while boosting energy.",
            impactTag = "Save ~₱30/day",
            category = TipCategory.COMMUTE
        ),
        SavingTip(
            id = "tip_24hr_rule",
            title = "Use the 24-Hour Pause Rule",
            description = "Want an unplanned snack, accessory, or online checkout? Wait 24 hours. Most impulse wants fade by the next morning.",
            impactTag = "Stop impulse buys",
            category = TipCategory.SAVING_HABITS
        ),
        SavingTip(
            id = "tip_weekly_cap",
            title = "Split Allowance Into School & Buffer Funds",
            description = "Keep your essential fare and lunch money separate from extra spending money so you never run short before Friday.",
            impactTag = "Zero Friday crunch",
            category = TipCategory.DAILY_BAON
        ),
        SavingTip(
            id = "tip_bundle_supplies",
            title = "Check Old Notebooks Before Buying New",
            description = "Reuse unused pages from last semester and share bulk packs of pens or bond paper with classmates to pay lower unit prices.",
            impactTag = "Save ~₱150/sem",
            category = TipCategory.SCHOOL
        ),
        SavingTip(
            id = "tip_combo_meals",
            title = "Choose Filling Carinderia or Canteen Meals",
            description = "A balanced rice meal keeps you full longer than sugary milk tea or chips, reducing extra afternoon snack cravings.",
            impactTag = "Better value",
            category = TipCategory.FOOD
        ),
        SavingTip(
            id = "tip_review_sunday",
            title = "Do a 3-Minute Sunday Baon Check-In",
            description = "Open BaonBuddy History every Sunday evening to spot your top spending category and adjust next week's target.",
            impactTag = "Stay on track",
            category = TipCategory.DAILY_BAON
        )
    )
}
