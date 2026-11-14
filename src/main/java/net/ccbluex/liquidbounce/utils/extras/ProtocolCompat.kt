package net.ccbluex.liquidbounce.utils.extras

/**
 * ViaForge is a runtime mod and is not on the compile classpath, so the 1.12.2+ protocol checks
 * used by the ported Gapple code are resolved reflectively. When ViaForge is absent (plain 1.8.9)
 * every check returns false and the legacy path is used.
 */
object ProtocolCompat {

    private val modernProtocol: Boolean by lazy {
        try {
            val base = Class.forName("de.florianmichael.vialoadingbase.ViaLoadingBase")
            val instance = base.getMethod("getInstance").invoke(null) ?: return@lazy false

            val version = try {
                instance.javaClass.getMethod("getTargetVersion").invoke(instance)
            } catch (t: Throwable) {
                instance.javaClass.getField("targetVersion").get(instance)
            } ?: return@lazy false

            val id = try {
                version.javaClass.getMethod("getVersion").invoke(version) as? Int
            } catch (t: Throwable) {
                try {
                    version.javaClass.getField("version").get(version) as? Int
                } catch (t2: Throwable) {
                    null
                }
            } ?: return@lazy false

            id >= 340 // 1.12.2
        } catch (t: Throwable) {
            false
        }
    }

    val isModernProtocol: Boolean get() = modernProtocol
}
