package com.kaizen.khushu.data.repository

import com.kaizen.khushu.data.model.LearnSection
import com.kaizen.khushu.data.model.LearnTopic
import com.kaizen.khushu.data.model.WordData

object LearnRepository {
    fun getSections(): List<LearnSection> {
        return listOf(
            LearnSection(
                id = "foundations",
                sectionTitle = "Foundations",
                color = 0xFF4A3B6BL, // Deep Purple
                topics = listOf(
                    LearnTopic(
                        id = "articles_of_faith",
                        title = "Articles of Faith",
                        arabicText = "الشهادة أن لا إله إلا الله وأن محمدا رسول الله",
                        words = listOf(
                            WordData(id = "1", arabic = "الشهادة", translation = "The testimony"),
                            WordData(id = "2", arabic = "أن", translation = "That"),
                            WordData(id = "3", arabic = "لا", translation = "No"),
                            WordData(id = "4", arabic = "إله", translation = "God"),
                            WordData(id = "5", arabic = "إلا", translation = "Except"),
                            WordData(id = "6", arabic = "الله", translation = "Allah"),
                            WordData(id = "7", arabic = "وأن", translation = "And that"),
                            WordData(id = "8", arabic = "محمدا", translation = "Muhammad"),
                            WordData(id = "9", arabic = "رسول", translation = "Messenger"),
                            WordData(id = "10", arabic = "الله", translation = "Allah")
                        ),
                        translations = mapOf("en" to "The testimony that there is no god but Allah and Muhammad is His messenger"),
                        referenceSource = "Quran",
                        referenceNumber = "49:16"
                    ),
                    LearnTopic(
                        id = "five_pillars",
                        title = "Five Pillars",
                        arabicText = "الصلاة والزكاة والصوم والحج",
                        words = listOf(
                            WordData(id = "11", arabic = "الصلاة", translation = "Prayer"),
                            WordData(id = "12", arabic = "والزكاة", translation = "And zakah"),
                            WordData(id = "13", arabic = "والصوم", translation = "And fasting"),
                            WordData(id = "14", arabic = "والحج", translation = "And pilgrimage")
                        ),
                        translations = mapOf("en" to "The five pillars of Islam: prayer, zakah, fasting, and pilgrimage")
                    ),
                    LearnTopic(
                        id = "tawhid",
                        title = "Understanding Tawhid",
                        arabicText = "التوحيد هو أساس الإسلام",
                        words = listOf(
                            WordData(id = "15", arabic = "التوحيد", translation = "Monotheism"),
                            WordData(id = "16", arabic = "هو", translation = "It is"),
                            WordData(id = "17", arabic = "أساس", translation = "Foundation"),
                            WordData(id = "18", arabic = "الإسلام", translation = "Islam")
                        ),
                        translations = mapOf("en" to "Tawhid is the foundation of Islam")
                    )
                )
            ),
            LearnSection(
                id = "purification",
                sectionTitle = "Purification",
                color = 0xFF8B5A4BL, // Burnished Terracotta
                topics = listOf(
                    LearnTopic(
                        id = "wudu",
                        title = "Step-by-Step Wudu",
                        arabicText = "الوضوء غسل الوجه واليدين",
                        words = listOf(
                            WordData(id = "19", arabic = "الوضوء", translation = "Ablution"),
                            WordData(id = "20", arabic = "غسل", translation = "Washing"),
                            WordData(id = "21", arabic = "الوجه", translation = "Face"),
                            WordData(id = "22", arabic = "واليدين", translation = "And hands")
                        ),
                        translations = mapOf("en" to "Wudu is washing the face and hands")
                    ),
                    LearnTopic(
                        id = "ghusl",
                        title = "Rules of Ghusl",
                        arabicText = "الغسل واجب بعد الجماع",
                        words = listOf(
                            WordData(id = "23", arabic = "الغسل", translation = "Bathing"),
                            WordData(id = "24", arabic = "واجب", translation = "Obligatory"),
                            WordData(id = "25", arabic = "بعد", translation = "After"),
                            WordData(id = "26", arabic = "الجماع", translation = "Intercourse")
                        ),
                        translations = mapOf("en" to "Ghusl is obligatory after intercourse")
                    ),
                    LearnTopic(
                        id = "tayammum",
                        title = "Tayammum",
                        arabicText = "التيمم بديل عن الماء",
                        words = listOf(
                            WordData(id = "27", arabic = "التيمم", translation = "Dry ablution"),
                            WordData(id = "28", arabic = "بديل", translation = "Substitute"),
                            WordData(id = "29", arabic = "عن", translation = "From"),
                            WordData(id = "30", arabic = "الماء", translation = "Water")
                        ),
                        translations = mapOf("en" to "Tayammum is a substitute for water")
                    )
                )
            ),
            LearnSection(
                id = "prayer",
                sectionTitle = "The Prayer",
                color = 0xFF2E5A4EL, // Deep Emerald
                topics = listOf(
                    LearnTopic(
                        id = "prerequisites",
                        title = "Prayer Prerequisites",
                        arabicText = "الطهارة والستر والإستقبال",
                        words = listOf(
                            WordData(id = "31", arabic = "الطهارة", translation = "Purification"),
                            WordData(id = "32", arabic = "والستر", translation = "And covering"),
                            WordData(id = "33", arabic = "والإستقبال", translation = "And facing")
                        ),
                        translations = mapOf("en" to "Purification, covering, and facing the qiblah are prerequisites for prayer")
                    ),
                    LearnTopic(
                        id = "step_by_step_salah",
                        title = "Step-by-Step Salah",
                        arabicText = "الصلاةقيام وركوع وسجود",
                        words = listOf(
                            WordData(id = "34", arabic = "الصلاة", translation = "Prayer"),
                            WordData(id = "35", arabic = "قيام", translation = "Standing"),
                            WordData(id = "36", arabic = "وركوع", translation = "And bowing"),
                            WordData(id = "37", arabic = "وسجود", translation = "And prostration")
                        ),
                        translations = mapOf("en" to "Prayer consists of standing, bowing, and prostrating")
                    ),
                    LearnTopic(
                        id = "rakat_breakdown",
                        title = "Rakat Breakdown",
                        arabicText = "الظهر أربع ركعات",
                        words = listOf(
                            WordData(id = "38", arabic = "الظهر", translation = "Dhuhr"),
                            WordData(id = "39", arabic = "أربع", translation = "Four"),
                            WordData(id = "40", arabic = "ركعات", translation = "units/rakats")
                        ),
                        translations = mapOf("en" to "Dhuhr prayer has four rakat units")
                    ),
                    LearnTopic(
                        id = "sajdah_sahw",
                        title = "Sajdah Sahw",
                        arabicText = "سجود السهو للت弥补",
                        words = listOf(
                            WordData(id = "41", arabic = "سجود", translation = "Prostration"),
                            WordData(id = "42", arabic = "السهو", translation = "Forgetfulness"),
                            WordData(id = "43", arabic = "للت弥补", translation = "To make up")
                        ),
                        translations = mapOf("en" to "Prostration of forgetfulness to make up for missed parts")
                    )
                )
            ),
            LearnSection(
                id = "recitations",
                sectionTitle = "Essential Recitations",
                color = 0xFFD4AF37L, // Metallic Gold
                topics = listOf(
                    LearnTopic(
                        id = "surah_fatiha",
                        title = "Surah Al-Fatiha",
                        arabicText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ۝ الْحَمْدُ لِلَّهِ رَبِّ الْعَٰلَمِينَ ۝ الرَّحْمَٰنِ الرَّحِيمِ ۝ مَٰلِكِ يَوْمِ الدِّينِ ۝ إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ ۝ اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ ۝ صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ",
                        tajweedMarkup = "بِسْمِ اللَّهِ الرَّحْمَ{madd}ٰ{/madd}نِ الرَّحِ{madd}ي{/madd}مِ ۝ الْحَمْدُ لِلَّهِ رَبِّ الْعَ{madd}ٰ{/madd}لَمِ{madd}ي{/madd}نَ ۝ الرَّحْمَ{madd}ٰ{/madd}نِ الرَّحِ{madd}ي{/madd}مِ ۝ مَ{madd}ٰ{/madd}لِكِ يَوْمِ الدِّ{madd}ي{/madd}نِ ۝ إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِ{madd}ي{/madd}نُ ۝ اهْدِنَا الصِّرَاطَ الْمُسْتَقِ{madd}ي{/madd}مَ ۝ صِرَاطَ الَّذِ{madd}ي{/madd}نَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَ{madd}ا{/madd} الضَّالِّ{madd}ي{/madd}نَ",
                        words = listOf(
                            WordData(id = "44", arabic = "بِسْمِ", translation = "In the name"),
                            WordData(id = "45", arabic = "اللَّهِ", translation = "of Allah"),
                            WordData(id = "46", arabic = "الرَّحْمَٰنِ", translation = "The Most Gracious"),
                            WordData(id = "47", arabic = "الرَّحِيمِ", translation = "The Most Merciful"),
                            WordData(id = "48", arabic = "الْحَمْدُ", translation = "All praise"),
                            WordData(id = "49", arabic = "لِلَّهِ", translation = "is for Allah"),
                            WordData(id = "50", arabic = "رَبِّ", translation = "Lord"),
                            WordData(id = "51", arabic = "الْعَٰلَمِينَ", translation = "of all worlds"),
                            WordData(id = "52f", arabic = "مَٰلِكِ", translation = "Master"),
                            WordData(id = "53f", arabic = "يَوْمِ", translation = "of the Day"),
                            WordData(id = "54f", arabic = "الدِّينِ", translation = "of Judgement"),
                            WordData(id = "55f", arabic = "إِيَّاكَ", translation = "You alone"),
                            WordData(id = "56f", arabic = "نَعْبُدُ", translation = "we worship"),
                            WordData(id = "57f", arabic = "نَسْتَعِينُ", translation = "we seek help"),
                            WordData(id = "58f", arabic = "اهْدِنَا", translation = "Guide us"),
                            WordData(id = "59f", arabic = "الصِّرَاطَ", translation = "the path"),
                            WordData(id = "60f", arabic = "الْمُسْتَقِيمَ", translation = "the straight")
                        ),
                        translations = mapOf("en" to "In the name of Allah, the Most Gracious, the Most Merciful. All praise is for Allah — Lord of all worlds, the Most Gracious, the Most Merciful, Master of the Day of Judgement. You alone we worship and You alone we ask for help. Guide us along the straight path — the path of those You have blessed, not those You are angry with or those who are astray."),
                        referenceSource = "Quran",
                        referenceNumber = "1:1-7",
                        audioFilename = "fatiha.mp3"
                    ),
                    LearnTopic(
                        id = "ayatul_kursi",
                        title = "Ayatul Kursi",
                        arabicText = "اللّهُ لا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ",
                        words = listOf(
                            WordData(id = "52", arabic = "اللّهُ", translation = "Allah"),
                            WordData(id = "53", arabic = "لا", translation = "No"),
                            WordData(id = "54", arabic = "إِلَهَ", translation = "god"),
                            WordData(id = "55", arabic = "إِلَّا", translation = "except"),
                            WordData(id = "56", arabic = "هُوَ", translation = "He"),
                            WordData(id = "57", arabic = "الْحَيُّ", translation = "The Living"),
                            WordData(id = "58", arabic = "الْقَيُّومُ", translation = "The Self-Subsisting")
                        ),
                        translations = mapOf("en" to "Allah, there is no god but He, the Living, the Self-Subsisting"),
                        referenceSource = "Quran",
                        referenceNumber = "2:255"
                    ),
                    LearnTopic(
                        id = "three_quls",
                        title = "Three Quls",
                        arabicText = "قُل هُوَ اللّهُ أَحَدٌ",
                        words = listOf(
                            WordData(id = "59", arabic = "قُل", translation = "Say"),
                            WordData(id = "60", arabic = "هُوَ", translation = "He is"),
                            WordData(id = "61", arabic = "اللّهُ", translation = "Allah"),
                            WordData(id = "62", arabic = "أَحَدٌ", translation = "One")
                        ),
                        translations = mapOf("en" to "Say: He is Allah, the One"),
                        referenceSource = "Quran",
                        referenceNumber = "112:1"
                    )
                )
            ),
            LearnSection(
                id = "daily_fortification",
                sectionTitle = "Daily Fortification",
                color = 0xFF3B4D61L, // Twilight Blue
                topics = listOf(
                    LearnTopic(
                        id = "morning_adhkar",
                        title = "Morning Adhkar",
                        arabicText = "اللهم بك أصبحنا",
                        words = listOf(
                            WordData(id = "63", arabic = "اللهم", translation = "O Allah"),
                            WordData(id = "64", arabic = "بك", translation = "By You"),
                            WordData(id = "65", arabic = "أصبحنا", translation = "we have reached morning")
                        ),
                        translations = mapOf("en" to "O Allah, by You we have reached morning")
                    ),
                    LearnTopic(
                        id = "evening_adhkar",
                        title = "Evening Adhkar",
                        arabicText = "اللهم بك أمسينا",
                        words = listOf(
                            WordData(id = "66", arabic = "اللهم", translation = "O Allah"),
                            WordData(id = "67", arabic = "بك", translation = "By You"),
                            WordData(id = "68", arabic = "أمسينا", translation = "we have reached evening")
                        ),
                        translations = mapOf("en" to "O Allah, by You we have reached evening")
                    ),
                    LearnTopic(
                        id = "duas_after_fard",
                        title = "Duas After Fard Salah",
                        arabicText = "اللهم اغفر لي",
                        words = listOf(
                            WordData(id = "69", arabic = "اللهم", translation = "O Allah"),
                            WordData(id = "70", arabic = "اغفر", translation = "Forgive"),
                            WordData(id = "71", arabic = "لي", translation = "me")
                        ),
                        translations = mapOf("en" to "O Allah, forgive me")
                    )
                )
            )
        )
    }
}