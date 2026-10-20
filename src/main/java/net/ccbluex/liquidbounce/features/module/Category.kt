/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

/**
 * Compatibility shim mapping the new module DSL (`Category.COMBAT`, `Category.SubCategory.COMBAT_RAGE`)
 * onto this client's [ModuleCategory] enum.
 */
object Category {
    val COMBAT: ModuleCategory = ModuleCategory.COMBAT
    val PLAYER: ModuleCategory = ModuleCategory.PLAYER
    val MOVEMENT: ModuleCategory = ModuleCategory.MOVEMENT
    val RENDER: ModuleCategory = ModuleCategory.RENDER
    val CLIENT: ModuleCategory = ModuleCategory.CLIENT
    val WORLD: ModuleCategory = ModuleCategory.WORLD
    val MISC: ModuleCategory = ModuleCategory.MISC

    enum class SubCategory(val displayName: String, val icon: String) {
        NONE("", ""),
        COMBAT_RAGE("Rage", "a"),
        COMBAT_LEGIT("Legit", "e"),
        MOVEMENT_MAIN("Main", "g"),
        MOVEMENT_EXTRAS("Extras", "f"),
        RENDER_SELF("Self", "m"),
        RENDER_OVERLAY("Overlay", "h"),
        PLAYER_COUNTER("Counterattack", "n"),
        PLAYER_ASSIST("Assist", "l"),
        CLIENT_GENERAL("Client", "h"),
        CONFIGS("Configs", "x"),
        MISCELLANEOUS("Miscellaneous", "u"),
        EXPLOIT_EXTRAS("Extras", "j"),
        GENERAL("General", "h");

        override fun toString() = displayName
    }
}
