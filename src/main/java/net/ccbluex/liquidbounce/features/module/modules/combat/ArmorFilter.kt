/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 *
 * NOTE: Inventory-management helpers that depend on FDP's coroutine-based inventory framework
 * (TickedActions, InventoryManager extensions, ArmorComparator sorting) are stubbed here. Only the
 * public API required by Aerolite's AutoArmor / InventoryManager is implemented; the filtering logic
 * currently falls back to the legacy comparator or no-op.
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.config.*

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.inventory.ArmorComparator
import net.ccbluex.liquidbounce.utils.inventory.getEnchantmentLevel
import net.ccbluex.liquidbounce.utils.inventory.ArmorSet
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack

@ModuleInfo(name = "ArmorFilter", category = ModuleCategory.COMBAT)
object ArmorFilter : Module() {

    init {
        subCategory = Category.SubCategory.COMBAT_LEGIT
    }

    private val priority by choices("Priority", arrayOf("Balanced", "Defense", "Durability", "Enchantments"), "Balanced")
    private val keepBackups by int("KeepBackups", 0, 0..3)
    private val dropLowDurability by boolean("DropLowDurability", true)
    private val dropDelay by int("DropDelay", 40, 10..500, suffix = "ms") { dropLowDurability }
    private val dropDurabilityPercent by int("DropDurability", 50, 0..100, suffix = "%") { dropLowDurability }
    private val minMaterial by choices("MinMaterial", arrayOf("Any", "Leather", "Gold", "Chain", "Iron", "Diamond"), "Any")
    private val minDurabilityPercent by int("MinDurabilityPercent", 10, 0..100)
    private val minProtectionLevel by int("MinProtectionLevel", 0, 0..4)
    private val minEnchantmentSum by int("MinEnchantmentSum", 0, 0..12)

    private var lastDropTime = 0L

    override val tag: String?
        get() = if (dropLowDurability) "${dropDelay}ms | ${dropDurabilityPercent}%" else priority

    fun dropLowDurabilityArmor(): Boolean {
        // TODO: depends on FDP inventory scheduling framework (TickedActions / InventoryManager).
        return false
    }

    fun selectBestArmorSet(stacks: List<ItemStack?>, entityStacksMap: Map<ItemStack, EntityItem>? = null): ArmorSet? {
        // TODO: full filtering depends on FDP inventory framework; currently returns the legacy best set.
        return ArmorComparator.getBestArmorSet(stacks, entityStacksMap)
    }

    fun getArmorCandidates(
        stacks: List<ItemStack?>,
        armorType: Int,
        entityStacksMap: Map<ItemStack, EntityItem>? = null,
        respectFilter: Boolean = true,
        priorityMode: String = priority,
    ): List<Pair<Int?, ItemStack>> {
        // TODO: depends on FDP inventory framework.
        return emptyList()
    }

    fun getUsefulArmorPieces(
        stacks: List<ItemStack?>,
        entityStacksMap: Map<ItemStack, EntityItem>? = null,
    ): Set<ItemStack> {
        return emptySet()
    }

    fun isUsefulArmor(
        stack: ItemStack?,
        stacks: List<ItemStack?>,
        entityStacksMap: Map<ItemStack, EntityItem>? = null,
    ): Boolean {
        return false
    }

    fun isArmorAllowed(stack: ItemStack?): Boolean {
        val piece = stack ?: return false
        val armor = piece.item as? ItemArmor ?: return false

        if (armorMaterialRank(armor.armorMaterial) < minimumMaterialRank()) {
            return false
        }

        if (piece.maxDamage > 0) {
            val durabilityPercent = piece.itemDamage * 100 / piece.maxDamage
            if (durabilityPercent < minDurabilityPercent) {
                return false
            }
        }

        if (piece.getEnchantmentLevel(Enchantment.protection) < minProtectionLevel) {
            return false
        }

        // TODO: enchantmentSum helper not wired up here.
        return true
    }

    fun scoreArmorStack(stack: ItemStack, priorityMode: String = priority): Double {
        // TODO: full scoring depends on FDP inventory extensions.
        return 0.0
    }

    private fun minimumMaterialRank() = when (minMaterial) {
        "Leather" -> 0
        "Gold" -> 1
        "Chain" -> 2
        "Iron" -> 3
        "Diamond" -> 4
        else -> Int.MIN_VALUE
    }

    private fun armorMaterialRank(material: ItemArmor.ArmorMaterial) = when (material) {
        ItemArmor.ArmorMaterial.LEATHER -> 0
        ItemArmor.ArmorMaterial.GOLD -> 1
        ItemArmor.ArmorMaterial.CHAIN -> 2
        ItemArmor.ArmorMaterial.IRON -> 3
        ItemArmor.ArmorMaterial.DIAMOND -> 4
        else -> Int.MAX_VALUE
    }
}
