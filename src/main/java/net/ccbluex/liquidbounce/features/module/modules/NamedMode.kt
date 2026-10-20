/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 *
 * Small framework shared by the ported mode-based modules (Flight / Speed / LongJump / NoWeb):
 * every mode is a class implementing [NamedMode], and the module keeps a list of them.
 */
package net.ccbluex.liquidbounce.features.module.modules

/**
 * A selectable implementation of a module mode (e.g. a fly or speed mode).
 */
interface NamedMode {
    val modeName: String
}

/**
 * Names of all modes, ready to be passed to `choices(...)`.
 */
fun <T : NamedMode> Iterable<T>.modeNames(): Array<String> = map { it.modeName }.toTypedArray()

fun <T : NamedMode> Array<T>.modeNames(): Array<String> = map { it.modeName }.toTypedArray()

/**
 * Looks up the mode called [name], falling back to the first mode when it is unknown
 * (e.g. after a config rename) so a stale config value can never crash the client.
 */
fun <T : NamedMode> Iterable<T>.selectedMode(name: String): T =
    firstOrNull { it.modeName.equals(name, ignoreCase = true) } ?: first()

fun <T : NamedMode> Array<T>.selectedMode(name: String): T =
    firstOrNull { it.modeName.equals(name, ignoreCase = true) } ?: first()
