package net.ccbluex.liquidbounce.ui.i18n

import net.ccbluex.liquidbounce.utils.client.ClientUtils
import java.io.InputStreamReader
import java.util.*

class Language(val locale: String, defaultLocale: String) {

    private val translateMap = HashMap<String, String>()

    constructor(defaultLocale: String) : this(defaultLocale, defaultLocale)

    init {
        if (defaultLocale != locale) {
            read(defaultLocale)
        }

        read(locale)
    }

    private fun read(locale: String) {
        val stream = LanguageManager::class.java.classLoader
            .getResourceAsStream("assets/minecraft/aerolite/$locale.lang")

        // A missing translation file must not take the client down: fall back to the raw keys.
        if (stream == null) {
            ClientUtils.LOGGER.warn("Missing language file 'assets/minecraft/aerolite/$locale.lang'")
            return
        }

        val prop = Properties()

        stream.use {
            InputStreamReader(it, Charsets.UTF_8).use { reader -> prop.load(reader) }
        }

        for ((key, value) in prop.entries) {
            if (key is String && value is String) {
                translateMap[key] = value
            }
        }
    }

    fun get(key: String): String {
        return translateMap[key] ?: key
    }
}