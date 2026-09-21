package com.example.core.localization

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    TURKMEN("tk", "Türkmençe", "Türkmen dili"),
    RUSSIAN("ru", "Русский", "Русский язык"),
    ENGLISH("en", "English", "English");

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.find { it.code == code } ?: TURKMEN
    }
}
