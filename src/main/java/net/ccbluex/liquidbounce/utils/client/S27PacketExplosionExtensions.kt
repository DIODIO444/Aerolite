/*
 * Aerolite — FDPClient framework port
 * Compile-time-visible extension accessors for S27PacketExplosion's private motion fields,
 * delegating to the runtime mixin (IMixinS27PacketExplosion). Lets migrated code (e.g. AntiKnockback)
 * keep the FDPClient `packet.getField_149152_f()` / `packet.setField_149152_f(v)` call style.
 */
package net.ccbluex.liquidbounce.utils.client

import net.ccbluex.liquidbounce.injection.implementations.IMixinS27PacketExplosion
import net.minecraft.network.play.server.S27PacketExplosion

fun S27PacketExplosion.getField_149152_f(): Double = (this as IMixinS27PacketExplosion).field_149152_f.toDouble()
fun S27PacketExplosion.setField_149152_f(value: Double) { (this as IMixinS27PacketExplosion).field_149152_f = value.toFloat() }
fun S27PacketExplosion.getField_149153_g(): Double = (this as IMixinS27PacketExplosion).field_149153_g.toDouble()
fun S27PacketExplosion.setField_149153_g(value: Double) { (this as IMixinS27PacketExplosion).field_149153_g = value.toFloat() }
fun S27PacketExplosion.getField_149159_h(): Double = (this as IMixinS27PacketExplosion).field_149159_h.toDouble()
fun S27PacketExplosion.setField_149159_h(value: Double) { (this as IMixinS27PacketExplosion).field_149159_h = value.toFloat() }
