/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.visual.HideClans
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import java.util.Locale

@ModuleInfo(name = "TargetFocus", category = ModuleCategory.CLIENT, canEnable = false)
object TargetFocus : Module() {

    init {
        subCategory = Category.SubCategory.CLIENT_GENERAL
    }

    private val targetNameValue = text("Name", "")
        .describe("Name of the player to focus on.")
    private val allEntities by boolean("AllEntities", true)
        .describe("Hide all entities, not just players.")
    private val automatic by boolean("Automatic", false)
        .describe("Automatically pick the target to focus.")
    private val mode by choices(
        "Mode",
        arrayOf("LowArmor", "BreakArmor", "Health", "Both", "BrowseAllPlayers", "SwitchVisibleHit", "SwitchVisibleTime"),
        "LowArmor"
    ) { automatic }
        .describe("How the focused target is chosen automatically.")
    private val browseMode by choices("BrowseMode", arrayOf("Hits", "Time"), "Hits") {
        automatic && mode == "BrowseAllPlayers"
    }
        .describe("Switch browsed players by hit count or time.")
    private val browseHits by int("BrowseHits", 5, 1..20) {
        automatic && (mode == "BrowseAllPlayers" && browseMode == "Hits" || mode == "SwitchVisibleHit")
    }
        .describe("Hits before switching to the next player.")
    private val browseTimeMs by int("BrowseTimeMs", 3000, 1..10000) {
        automatic && (mode == "BrowseAllPlayers" && browseMode == "Time" || mode == "SwitchVisibleTime")
    }
        .describe("Milliseconds before switching to the next player.")
    private val clearBrowseCacheValue = boolean("ClearBrowseCache", false) {
        automatic && mode == "BrowseAllPlayers"
    }
    private val clearBrowseCache by clearBrowseCacheValue
    private val showBrowsedPlayers by boolean("ShowBrowsedPlayers", false) {
        automatic && mode == "BrowseAllPlayers"
    }
        .describe("Show a list of already browsed players.")
    private val bothHealthWeight by float("BothHealthWeight", 1F, 0.1F..5F) { automatic && mode == "Both" }
        .describe("Weight given to health in the Both mode score.")
    private val bothArmorWeight by float("BothArmorWeight", 1F, 0.1F..5F) { automatic && mode == "Both" }
        .describe("Weight given to armor in the Both mode score.")
    private val considerDurability by boolean("ConsiderDurability", true) {
        automatic && (mode == "BreakArmor" || mode == "Both")
    }
        .describe("Prefer armor pieces with lower durability.")
    private val breakArmorPriority by float("BreakArmorPriority", 5F, 0F..10F) {
        automatic && (mode == "BreakArmor" || mode == "Both")
    }
        .describe("Weight given to breaking armor in target scoring.")

    private val targetGroup = Configurable("Target")
    private val browseGroup = Configurable("Browse")
    private val scoringGroup = Configurable("Scoring")

    init {
        moveValues(targetGroup,
            "Name", "AllEntities", "Automatic", "Mode")

        moveValues(browseGroup,
            "BrowseMode", "BrowseHits", "BrowseTimeMs", "ClearBrowseCache", "ShowBrowsedPlayers")

        moveValues(scoringGroup,
            "BothHealthWeight", "BothArmorWeight", "ConsiderDurability", "BreakArmorPriority")

        addValues(listOf(targetGroup, browseGroup, scoringGroup))
    }
    private var lockedTargetName = ""
    private var browseClanKey = ""
    private val browsedPlayers = linkedSetOf<String>()
    private val processedBrowsedPlayers = linkedSetOf<String>()
    private var browseHitCount = 0
    private var lastBrowseSwitchTime = 0L
    private var observedHitThisTick: EntityPlayer? = null
    private var breakArmorTargetName = ""
    private var breakArmorArmorMask = 0
    private var damageTrackedName = ""
    private var lastTrackedHurtTime = 0

    override val tag: String?
        get() {
            if (automatic) {
                if (mode == "BrowseAllPlayers" && showBrowsedPlayers && browsedPlayers.isNotEmpty()) {
                    val remaining = (browsedPlayers - processedBrowsedPlayers).size.coerceAtLeast(0)
                    val target = lockedTargetName.takeIf { it.isNotEmpty() } ?: "Waiting"
                    return "[A] $target | $remaining"
                }

                return lockedTargetName.takeIf { it.isNotEmpty() }?.let { "[A] $it" } ?: "[A]"
            }

            return targetNameValue.get().trim().takeIf { it.isNotEmpty() }?.let { "| $it" }
        }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        updateTarget()
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        observedHitThisTick = event.targetEntity as? EntityPlayer
    }

    override fun onDisable() {
        clearRuntimeState()
    }

    private fun clearRuntimeState() {
        lockedTargetName = ""
        browseClanKey = ""
        browsedPlayers.clear()
        processedBrowsedPlayers.clear()
        browseHitCount = 0
        lastBrowseSwitchTime = 0L
        observedHitThisTick = null
        breakArmorTargetName = ""
        breakArmorArmorMask = 0
        damageTrackedName = ""
        lastTrackedHurtTime = 0
    }

    @JvmStatic
    fun isManagingVisibility(): Boolean {
        if (!handleEvents()) {
            return false
        }

        if (currentTargetName().isNotEmpty()) {
            return true
        }

        return automatic && mode == "BrowseAllPlayers" && browseClanKey.isNotEmpty() && browsedPlayers.isNotEmpty()
    }

    @JvmStatic
    fun shouldForceRender(entity: Entity?): Boolean {
        return isManagingVisibility() && isSelectedTarget(entity)
    }

    @JvmStatic
    fun shouldHideEntity(entity: Entity?): Boolean {
        if (!isManagingVisibility() || entity == null || entity == mc.thePlayer) {
            return false
        }

        if (isSelectedTarget(entity)) {
            return false
        }

        return allEntities || entity is EntityPlayer
    }

    @JvmStatic
    fun isSelectedTarget(entity: Entity?): Boolean {
        if (entity !is EntityPlayer) {
            return false
        }

        val targetName = normalizeName(currentTargetName())
        if (targetName.isEmpty()) {
            return false
        }

        return normalizeName(entity.name) == targetName ||
            normalizeName(entity.displayName?.unformattedText) == targetName ||
            normalizeName(entity.displayName?.formattedText) == targetName
    }

    private fun currentTargetName(): String {
        return if (automatic) lockedTargetName else targetNameValue.get().trim()
    }

    private fun updateTarget() {
        if (automatic) {
            updateAutomaticTarget()
        } else {
            lockedTargetName = targetNameValue.get().trim()
            browseHitCount = 0
            lastBrowseSwitchTime = 0L
            breakArmorTargetName = ""
            breakArmorArmorMask = 0
        }

        observedHitThisTick = null
    }

    // TODO: depends on full FDP auto-targeting scoring (priorityScore, armorMask, browse/switch logic).
    // For now, automatic mode simply locks the nearest ally-free player so isSelectedTarget works.
    private fun updateAutomaticTarget() {
        val world = mc.theWorld ?: return
        val localPlayer = mc.thePlayer ?: return
        val candidates = world.playerEntities.filterIsInstance<EntityPlayer>()
            .filter { it != localPlayer && !HideClans.isAlly(it) }

        when (mode) {
            "Health" -> candidates.minByOrNull { it.health }
            "LowArmor" -> candidates.minByOrNull { totalArmor(it) }
            "BreakArmor" -> candidates.minByOrNull { totalArmor(it) }
            "Both" -> candidates.minByOrNull { it.health * bothHealthWeight + totalArmor(it) * bothArmorWeight }
            else -> candidates.firstOrNull()
        }?.let { lockedTargetName = it.name } ?: run { lockedTargetName = "" }
    }

    private fun totalArmor(player: EntityPlayer): Float {
        return player.totalArmorValue.toFloat()
    }

    private fun normalizeName(name: String?): String {
        return (name ?: "").let {
            net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor(it)
        }.trim().lowercase(Locale.ROOT)
    }
}
