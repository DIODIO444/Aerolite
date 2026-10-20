/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config

import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import kotlin.random.Random

/**
 * FDPClient-compatible: a random [Int] within this [IntegerRangeValue]'s current range.
 */
val IntegerRangeValue.random: Int
    get() = if (start > endInclusive) start else Random.nextInt(start, endInclusive + 1)

/**
 * `true` when this value sits on its minimum, i.e. the feature is effectively disabled.
 */
fun IntegerValue.isMinimal(): Boolean = get() <= minimum

fun FloatValue.isMinimal(): Boolean = get() <= minimum
