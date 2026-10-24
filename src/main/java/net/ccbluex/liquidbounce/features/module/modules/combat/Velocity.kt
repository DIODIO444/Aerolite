package net.ccbluex.liquidbounce.features.module.modules.combat



import java.util.LinkedHashMap
import java.util.Locale
import net.ccbluex.liquidbounce.config.boolean
import net.ccbluex.liquidbounce.config.choices
import net.ccbluex.liquidbounce.config.float
import net.ccbluex.liquidbounce.config.floatRange
import net.ccbluex.liquidbounce.config.int
import net.ccbluex.liquidbounce.config.intRange
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.GameLoopEvent
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.StrafeEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.exploit.Disabler
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.utils.attack.CPSCounter
import net.ccbluex.liquidbounce.utils.attack.EntityUtils
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.client.PacketUtils
import net.ccbluex.liquidbounce.utils.client.PacketUtils.getRealMotionX
import net.ccbluex.liquidbounce.utils.client.PacketUtils.getRealMotionY
import net.ccbluex.liquidbounce.utils.client.PacketUtils.getRealMotionZ
import net.ccbluex.liquidbounce.utils.client.PacketUtils.schedulePacketProcess
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.extensions.attackEntityWithModifiedSprint
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils
import net.ccbluex.liquidbounce.utils.movement.MovementUtils
import net.ccbluex.liquidbounce.utils.rotation.RaycastUtils
import net.ccbluex.liquidbounce.utils.rotation.RaycastUtils.runWithModifiedRaycastResult
import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.currentRotation
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.block.BlockAir
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.client.C0FPacketConfirmTransaction
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition
import net.ccbluex.liquidbounce.utils.client.getField_149152_f
import net.ccbluex.liquidbounce.utils.client.getField_149153_g
import net.ccbluex.liquidbounce.utils.client.getField_149159_h
import net.ccbluex.liquidbounce.utils.client.setField_149152_f
import net.ccbluex.liquidbounce.utils.client.setField_149153_g
import net.ccbluex.liquidbounce.utils.client.setField_149159_h
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3


public object Velocity : Module("Velocity", Category.COMBAT) {
    val mode: String by choices("Mode",
 arrayOf("Simple",
 "AAC",
 "AACPush",
 "AACZero",
 "AACv4",
 "Reverse",
 "SmoothReverse",
 "Jump",
 "Glitch",
 "Legit",
 "GhostBlock",
 "Vulcan",
 "S32Packet",
 "MatrixReduce",
 "IntaveReduce",
 "Delay",
 "GrimC03",
 "Hypixel",
 "HypixelAir",
 "Click",
 "BlocksMC"),
 "Simple")
    val attackReduce: Boolean by boolean("Reduce", true) { mode == "Hypixel" }
    val attackReduceRange: Float by float("ReduceRange", 3.0F, 1.0F..6.0F) { mode == "Hypixel" && attackReduce }
    val attackReduceJumpReset: Boolean by boolean("JumpReset", false) { mode == "Hypixel" && attackReduce }
    val attackReduceTickExact: Boolean by boolean("TickExact", true) { mode == "Hypixel" }
    val attackReduceTick500: Int by int("500", 3, 0..20) { mode == "Hypixel" && attackReduceTickExact }
    val attackReduceTick1000: Int by int("1000", 4, 0..20) { mode == "Hypixel" && attackReduceTickExact }
    val attackReduceTick2000: Int by int("2000", 4, 0..20) { mode == "Hypixel" && attackReduceTickExact }
    val attackReduceTick3000: Int by int("3000", 5, 0..20) { mode == "Hypixel" && attackReduceTickExact }
    val attackReduceTick4000: Int by int("4000", 6, 0..20) { mode == "Hypixel" && attackReduceTickExact }
    val attackReduceTick5000: Int by int("5000", 6, 0..20) { mode == "Hypixel" && attackReduceTickExact }
    val attackReduceTick6000: Int by int("6000", 7, 0..20) { mode == "Hypixel" && attackReduceTickExact }
    val attackReduceTick7000: Int by int("7000", 7, 0..20) { mode == "Hypixel" && attackReduceTickExact }
    val attackReduceTick8000: Int by int("8000", 8, 0..20) { mode == "Hypixel" && attackReduceTickExact }
    val attackReduceTick9000: Int by int("9000", 8, 0..20) { mode == "Hypixel" && attackReduceTickExact }
    val attackReduceTick10000: Int by int("10000", 9, 0..20) { mode == "Hypixel" && attackReduceTickExact }
    val horizontal: Float by float("Horizontal", 0.0F, -1.0F..1.0F) { mode in arrayOf("Simple", "AAC", "Legit") }
    val vertical: Float by float("Vertical", 0.0F, -1.0F..1.0F) { mode in arrayOf("Simple", "Legit") }
    val reverseStrength: Float by float("ReverseStrength", 1.0F, 0.1F..1.0F) { mode == "Reverse" }
    val reverse2Strength: Float by float("SmoothReverseStrength", 0.05F, 0.02F..0.1F) { mode == "SmoothReverse" }
    val onLook: Boolean by boolean("onLook", false) { mode in arrayOf("Reverse", "SmoothReverse") }
    val range: Float by float("Range", 3.0F, 1.0F..5.0F) { onLook && mode in arrayOf("Reverse", "SmoothReverse") }
    val maxAngleDifference: Float by float("MaxAngleDifference", 45.0F, 5.0F..90.0F) { onLook && mode in arrayOf("Reverse", "SmoothReverse") }
    val aacPushXZReducer: Float by float("AACPushXZReducer", 2.0F, 1.0F..3.0F) { mode == "AACPush" }
    val aacPushYReducer: Boolean by boolean("AACPushYReducer", true) { mode == "AACPush" }
    val aacv4MotionReducer: Float by float("AACv4MotionReducer", 0.62F, 0.0F..1.0F) { mode == "AACv4" }
    val legitDisableInAir: Boolean by boolean("DisableInAir", true) { mode == "Legit" }
    val chance: Int by int("Chance", 100, 0..100) { mode == "Jump" || mode == "Legit" || mode == "Hypixel" && attackReduce && attackReduceJumpReset }
    val jumpCooldownMode: String by choices(
 "JumpCooldownMode", arrayOf("Ticks", "ReceivedHits"), "Ticks") { mode == "Jump" || mode == "Hypixel" && attackReduce && attackReduceJumpReset }
    val ticksUntilJump: Int by int("TicksUntilJump", 4, 0..20) { jumpCooldownMode == "Ticks" && (mode == "Jump" || mode == "Hypixel" && attackReduce && attackReduceJumpReset) }
    val hitsUntilJump: Int by int("ReceivedHitsUntilJump", 2, 0..5) { jumpCooldownMode == "ReceivedHits" && (mode == "Jump" || mode == "Hypixel" && attackReduce && attackReduceJumpReset) }
    val hurtTimeRange: IntRange by intRange("HurtTime", 1..9, 1..10) { mode == "GhostBlock" }
    val spoofDelay: Int by int("SpoofDelay", 500, 0..5000) { mode == "Delay" }
    var delayMode = false
    val reduceFactor: Float by float("Factor", 0.6F, 0.6F..1.0F) { mode == "IntaveReduce" }
    val hurtTime: Int by int("HurtTime", 9, 1..10) { mode == "IntaveReduce" }
    val pauseOnExplosion: Boolean by boolean("PauseOnExplosion", true)
    val ticksToPause: Int by int("TicksToPause", 20, 1..50) { pauseOnExplosion }
    private val limitMaxMotionValue by boolean("LimitMaxMotion", false)
    val maxXZMotion: Float by float("MaxXZMotion", 0.4F, 0.0F..1.9F) { limitMaxMotionValue }
    val maxYMotion: Float by float("MaxYMotion", 0.36F, 0.0F..0.46F) { limitMaxMotionValue }
    val clicks: IntRange by intRange("Clicks", 3..5, 1..20) { mode == "Click" }
    val hurtTimeToClick: Int by int("HurtTimeToClick", 10, 0..10) { mode == "Click" }
    val whenFacingEnemyOnly: Boolean by boolean("WhenFacingEnemyOnly", true) { mode == "Click" }
    val ignoreBlocking: Boolean by boolean("IgnoreBlocking", false) { mode == "Click" }
    val clickRange: Float by float("ClickRange", 3.0F, 1.0F..6.0F) { mode == "Click" }
    val swingMode: String by choices(
 "SwingMode", arrayOf("Off", "Normal", "Packet"), "Normal") { mode == "Click" }
    val velocityTimer: MSTimer = MSTimer()
    var hasReceivedVelocity = false
    var reverseHurt = false
    var jump = false
    var limitUntilJump = 0
    var intaveTick = 0
    var lastAttackTime = 0L
    var intaveDamageTick = 0
    val packets: LinkedHashMap<Packet<*>, Long> = LinkedHashMap()
    var timerTicks = 0
    var transaction = false
    var pauseTicks = 0

    override val tag: String
        get() = if (mode != "Simple" && mode != "Legit") {
            mode
        } else {
            "${(horizontal * 100).toInt()}% ${(vertical * 100).toInt()}%"
        }

    public override fun onDisable() {
        pauseTicks = 0;
        val player2 = mc.thePlayer
        if (player2 != null) {
            player2.speedInAir = 0.02F;
        }

        timerTicks = 0;
        Hypixel.reset();
        this.reset();
    }
    fun checkAir(blockPos: BlockPos): Boolean {
        val player2 = mc.theWorld
        if (player2 == null) {
            return false;
        } else if (!player2.isAirBlock(blockPos)) {
            return false;
        } else {
            timerTicks = 20;
            PacketUtils.sendPackets(
                C03PacketPlayer(true),
                C07PacketPlayerDigging(Action.STOP_DESTROY_BLOCK, blockPos, EnumFacing.DOWN)
            )
            player2.setBlockToAir(blockPos);
            return true;
        }
    }
    fun getDirection(): Double {
        var moveYaw: Float = mc.thePlayer.rotationYaw;
        if (mc.thePlayer.moveForward != 0.0F && mc.thePlayer.moveStrafing == 0.0F) {
            moveYaw += if (mc.thePlayer.moveForward > 0.0F) 0 else 180;
        } else if (mc.thePlayer.moveForward != 0.0F && mc.thePlayer.moveStrafing != 0.0F) {
            if (mc.thePlayer.moveForward > 0.0F) {
                moveYaw = moveYaw + (if (mc.thePlayer.moveStrafing > 0.0F) -45 else 45);
            } else {
                moveYaw = moveYaw - (if (mc.thePlayer.moveStrafing > 0.0F) -45 else 45);
            }

            moveYaw = moveYaw + (if (mc.thePlayer.moveForward > 0.0F) 0 else 180);
        } else if (mc.thePlayer.moveStrafing != 0.0F && mc.thePlayer.moveForward == 0.0F) {
            moveYaw += if (mc.thePlayer.moveStrafing > 0.0F) -90 else 90;
        }

        return Math.floorMod(moveYaw.toInt(), 360).toDouble();
    }
    fun sendPacketsByOrder(velocity: Boolean) {
        synchronized(packets) {
            packets.entries.removeAll { (packet, sentAt) ->
                // Packets younger than the spoof delay are held back unless we are flushing everything.
                if (!velocity && sentAt > System.currentTimeMillis() - spoofDelay) {
                    false
                } else {
                    PacketUtils.schedulePacketProcess(packet)
                    true
                }
            }
        }
    }
    fun reset() {
        this.sendPacketsByOrder(true);
        packets.clear();
    }
    fun shouldJump(): Boolean {
        val player2 = jumpCooldownMode.lowercase(Locale.ROOT)
        return if (player2 == "ticks") limitUntilJump >= this.ticksUntilJump else player2 == "receivedhits" && limitUntilJump >= this.hitsUntilJump;
    }
    fun handleVelocity(event: PacketEvent) {
        val packet = event.packet
        if (packet is S12PacketEntityVelocity) {
            event.cancelEvent();
            if (this.horizontal == 0.0F && this.vertical == 0.0F) {
                return;
            }

            if (this.horizontal != 0.0F) {
                var distXZ: Double = getRealMotionX(packet as S12PacketEntityVelocity);
                var maxYMotion: Double = getRealMotionZ(packet as S12PacketEntityVelocity);
                if (limitMaxMotionValue) {
                    val distXZx: Double = Math.sqrt(distXZ * distXZ + maxYMotion * maxYMotion);
                    if (distXZx > this.maxXZMotion) {
                        val ratioXZ: Double = this.maxXZMotion / distXZx;
                        distXZ *= ratioXZ;
                        maxYMotion *= ratioXZ;
                    }
                }

                mc.thePlayer.motionX = distXZ * this.horizontal;
                mc.thePlayer.motionZ = maxYMotion * this.horizontal;
            }

            if (this.vertical != 0.0F) {
                var text: Double = getRealMotionY(packet as S12PacketEntityVelocity);
                if (limitMaxMotionValue) {
                    text = text.coerceAtMost(this.maxYMotion + 7.5E-4);
                }

                mc.thePlayer.motionY = text * this.vertical;
            }
        } else if (packet is S27PacketExplosion) {
            if (this.horizontal != 0.0F && this.vertical != 0.0F) {
                (packet as S27PacketExplosion).setField_149152_f(0.0);
                (packet as S27PacketExplosion).setField_149153_g(0.0);
                (packet as S27PacketExplosion).setField_149159_h(0.0);
                return;
            }

            (packet as S27PacketExplosion).setField_149152_f((packet as S27PacketExplosion).getField_149152_f() * horizontal);
            (packet as S27PacketExplosion).setField_149153_g((packet as S27PacketExplosion).getField_149153_g() * vertical);
            (packet as S27PacketExplosion).setField_149159_h((packet as S27PacketExplosion).getField_149159_h() * horizontal);
            if (limitMaxMotionValue) {
                val value = Math.sqrt(
                    packet.getField_149152_f() * packet.getField_149152_f() +
                        packet.getField_149159_h() * packet.getField_149159_h()
                ).toFloat()
                val distY = packet.getField_149153_g().toFloat()
                val value2: Float = this.maxYMotion + 7.5E-4F;
                if (value > this.maxXZMotion) {
                    val ratioXZ: Float = this.maxXZMotion / value;
                    packet.setField_149152_f(packet.getField_149152_f() * ratioXZ)
                    packet.setField_149159_h(packet.getField_149159_h() * ratioXZ)
                }

                if (distY > value2) {
                    packet.setField_149153_g(packet.getField_149153_g() * (value2 / distY))
                }
            }
        }
    }
    fun getNearestEntityInRange(range: Float = this.range): Entity? {
        val player = mc.thePlayer ?: return null

        return mc.theWorld.loadedEntityList
            .filter { EntityUtils.isSelected(it, true) && player.getDistanceToEntityBox(it) <= range }
            .minByOrNull { player.getDistanceToEntityBox(it) }
    }

    /** Whether the crosshair direction is within [maxAngleDifference] of [entity]. */
    private fun isLookingOnEntities(entity: Entity, maxAngleDifference: Float): Boolean {
        val player = mc.thePlayer ?: return false
        val eyes = player.getPositionEyes(1f)
        val target = Vec3(entity.posX, entity.entityBoundingBox.minY + entity.eyeHeight, entity.posZ)

        val diffX = target.xCoord - eyes.xCoord
        val diffY = target.yCoord - eyes.yCoord
        val diffZ = target.zCoord - eyes.zCoord
        val diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ)

        val yaw = Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0
        val pitch = -Math.toDegrees(Math.atan2(diffY, diffXZ))

        val yawDiff = MathHelper.wrapAngleTo180_float(yaw.toFloat() - player.rotationYaw)
        val pitchDiff = MathHelper.wrapAngleTo180_float(pitch.toFloat() - player.rotationPitch)

        return Math.abs(yawDiff) <= maxAngleDifference && Math.abs(pitchDiff) <= maxAngleDifference
    }

    private fun getSpeed(): Double {
        val player = mc.thePlayer ?: return 0.0

        return Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ)
    }

    private fun setSpeed(speed: Double) {
        val player = mc.thePlayer ?: return
        var forward = player.movementInput.moveForward.toDouble()
        var strafe = player.movementInput.moveStrafe.toDouble()

        if (forward == 0.0 && strafe == 0.0) {
            return
        }

        var yaw = player.rotationYaw.toDouble()

        if (forward != 0.0) {
            if (strafe > 0.0) {
                yaw += (if (forward > 0.0) -45 else 45)
            } else if (strafe < 0.0) {
                yaw += (if (forward > 0.0) 45 else -45)
            }

            strafe = 0.0
            forward = if (forward > 0.0) 1.0 else -1.0
        }

        val cos = Math.cos(Math.toRadians(yaw + 90.0))
        val sin = Math.sin(Math.toRadians(yaw + 90.0))

        player.motionX = forward * speed * cos + strafe * speed * sin
        player.motionZ = forward * speed * sin - strafe * speed * cos
    }

    val onUpdate = handler<UpdateEvent>(always = false) {
        val player2 = mc.thePlayer
        if (player2 == null) {
            return@handler
        } else if (!player2.isInWater() && !player2.isInLava() && !player2.isInWeb && !player2.isDead) {
            val text= mode.toLowerCase(Locale.ROOT);
            when (text) {        "intavereduce" -> {
                        if (!hasReceivedVelocity) {
                            return@handler
                        }

                        var var9: Int = intaveTick++;
                        if (mc.thePlayer.hurtTime == 2) {
                            var9 = intaveDamageTick++;
                            if (player2.onGround && intaveTick % 2 == 0 && intaveDamageTick <= 10) {
                                player2.tryJump();
                                intaveTick = 0;
                            }

                            hasReceivedVelocity = false;
                        }
                            }
        "smoothreverse" -> {
                        val nearbyEntityx = getNearestEntityInRange(0.0F)
                        if (hasReceivedVelocity) {
                            if (nearbyEntityx == null) {
                                player2.speedInAir = 0.02F;
                                reverseHurt = false;
                            } else if (onLook && !isLookingOnEntities(nearbyEntityx, maxAngleDifference)) {
                                hasReceivedVelocity = false;
                                player2.speedInAir = 0.02F;
                                reverseHurt = false;
                            } else {
                                if (player2.hurtTime > 0) {
                                    reverseHurt = true;
                                }

                                if (!player2.onGround) {
                                    player2.speedInAir = if (reverseHurt) reverse2Strength else 0.02F;
                                } else if (velocityTimer.hasTimePassed(80)) {
                                    hasReceivedVelocity = false;
                                    player2.speedInAir = 0.02F;
                                    reverseHurt = false;
                                }
                            }
                        }
                            }
        "glitch" -> {
                        player2.noClip = hasReceivedVelocity;
                        if (player2.hurtTime == 7) {
                            player2.motionY = 0.4;
                        }

                        hasReceivedVelocity = false;
                            }
        "aacpush" -> {
                        if (jump) {
                            if (player2.onGround) {
                                jump = false;
                            }
                        } else {
                            if (player2.hurtTime > 0 && player2.motionX != 0.0 && player2.motionZ != 0.0) {
                                player2.onGround = true;
                            }

                            if (player2.hurtResistantTime > 0 && aacPushYReducer && !Speed.handleEvents()) {
                                player2.motionY -= 0.014999993;
                            }
                        }

                        if (player2.hurtResistantTime >= 19) {
                            val value3: Float = aacPushXZReducer;
                            player2.motionX /= value3;
                            player2.motionZ /= value3;
                        }
                            }
        "aaczero" -> {
                        if (player2.hurtTime > 0) {
                            if (!hasReceivedVelocity || player2.onGround || player2.fallDistance > 2.0F) {
                                return@handler
                            }

                            player2.motionY--;
                            player2.isAirBorne = true;
                            player2.onGround = true;
                        } else {
                            hasReceivedVelocity = false;
                        }
                            }
        "aac" -> if (hasReceivedVelocity && velocityTimer.hasTimePassed(80)) {
                        player2.motionX = player2.motionX * horizontal;
                        player2.motionZ = player2.motionZ * horizontal;
                        hasReceivedVelocity = false;
                            }
        "aacv4" -> if (player2.hurtTime > 0 && !player2.onGround) {
                        val flag: Float = aacv4MotionReducer;
                        player2.motionX *= flag;
                        player2.motionZ *= flag;
                            }
        "legit" -> {
                        if (legitDisableInAir && !MovementUtils.isOnGround(0.5)) {
                            return@handler
                        }

                        if (mc.thePlayer.maxHurtResistantTime != mc.thePlayer.hurtResistantTime || mc.thePlayer.maxHurtResistantTime == 0) {
                            return@handler
                        }

                        if ((0..100).random() < chance) {
                            val text3: Float = horizontal / 100.0F;
                            val vertical: Float = vertical / 100.0F;
                            player2.motionX *= text3;
                            player2.motionZ *= text3;
                            player2.motionY *= vertical;
                        }
                            }
        "reverse" -> {
                        val horizontal = getNearestEntityInRange(0.0F)
                        if (!hasReceivedVelocity) {
                            return@handler
                        }

                        if (horizontal != null) {
                            if (!player2.onGround) {
                                if (onLook && !isLookingOnEntities(horizontal, maxAngleDifference)) {
                                    return@handler
                                }

                                setSpeed(getSpeed() * reverseStrength);
                            } else if (velocityTimer.hasTimePassed(80)) {
                                hasReceivedVelocity = false;
                            }
                        }
                            }
        "hypixelair" -> if (hasReceivedVelocity) {
                        if (player2.onGround) {
                            player2.tryJump();
                        }

                        hasReceivedVelocity = false;
                    }
    }


            return@handler
        } else {
            return@handler
        }
    }

    val onGameTick = handler<GameTickEvent>(always = false) {
        val player = mc.thePlayer ?: return@handler

        if (mc.theWorld == null) {
            return@handler
        }

        if (mode == "Click" && player.hurtTime == hurtTimeToClick && (!ignoreBlocking || !player.isBlocking() && !KillAura.blockStatus)) {
            val thePlayer = player
            val crosshairTarget = mc.objectMouseOver
            val hitEntity = crosshairTarget?.entityHit

            val value: Entity? = if (hitEntity != null) {
                hitEntity.takeIf { EntityUtils.isSelected(it, true) }
            } else if (whenFacingEnemyOnly) {
                val rotation = RotationUtils.currentRotation ?: Rotation(player.rotationYaw, player.rotationPitch)
                var found: Entity? = null

                runWithModifiedRaycastResult(rotation, clickRange.toDouble(), 0.0) { raytrace ->
                    val entity = raytrace.entityHit ?: return@runWithModifiedRaycastResult
                    if (EntityUtils.isSelected(entity, true)) {
                        found = entity
                    }
                }

                found
            } else {
                getNearestEntityInRange(clickRange)?.takeIf { EntityUtils.isSelected(it, true) }
            }

            if (value == null) {
                return@handler
            }

            repeat(clicks.random()) {
                if (swingMode.equals("normal", true)) {
                    thePlayer.swingItem()
                } else {
                    PacketUtils.sendPacket(C0APacketAnimation())
                }

                thePlayer.attackEntityWithModifiedSprint(value) {
                    if (swingMode.equals("normal", true)) {
                        thePlayer.swingItem()
                    } else {
                        PacketUtils.sendPacket(C0APacketAnimation())
                    }
                }
                CPSCounter.registerClick(CPSCounter.MouseButton.LEFT)
            }
        }
    }

    val onAttack = handler<AttackEvent>(always = false) {
        val player2 = mc.thePlayer
        if (player2 == null) {
            return@handler
        } else if (mode == "IntaveReduce" && hasReceivedVelocity) {
            if (player2.hurtTime == hurtTime && System.currentTimeMillis() - lastAttackTime <= 8000L) {
                player2.motionX = player2.motionX * reduceFactor;
                player2.motionZ = player2.motionZ * reduceFactor;
            }

            lastAttackTime = System.currentTimeMillis();
            return@handler
        } else {
            return@handler
        }
    }

    val onPacket = handler<PacketEvent>(always = false, priority = 1) { event ->
        val player2 = mc.thePlayer
        if (player2 == null) {
            return@handler
        } else {
            Hypixel.onPacket(event);
            val packet = event.packet
            if (!handleEvents()) {
                return@handler
            } else if (pauseTicks > 0) {
                pauseTicks += -1;
                return@handler
            } else if (event.isCancelled) {
                return@handler
            } else {

                if (packet is S12PacketEntityVelocity && player2.getEntityId() == (packet as S12PacketEntityVelocity).getEntityID() && (packet as S12PacketEntityVelocity).motionY > 0 && ((packet as S12PacketEntityVelocity).motionX != 0 || (packet as S12PacketEntityVelocity).motionZ != 0) || packet is S27PacketExplosion && player2.motionY + (packet as S27PacketExplosion).getField_149153_g() > 0.0 && (
 player2.motionX + (packet as S27PacketExplosion).getField_149152_f() != 0.0 || player2.motionZ + (packet as S27PacketExplosion).getField_149159_h() != 0.0
 )) {
                    velocityTimer.reset();
                    if (pauseOnExplosion && packet is S27PacketExplosion && player2.motionY + (packet as S27PacketExplosion).getField_149153_g() > 0.0 && (
 player2.motionX + (packet as S27PacketExplosion).getField_149152_f() != 0.0 || player2.motionZ + (packet as S27PacketExplosion).getField_149159_h() != 0.0
 )) {
                        pauseTicks = ticksToPause;
                    }

                    val text2= mode.toLowerCase(Locale.ROOT);
                    when (text2) {        "intavereduce" -> {}
        "smoothreverse" -> {}
        "matrixreduce" -> if (packet is S12PacketEntityVelocity && (packet as S12PacketEntityVelocity).getEntityID() == player2.getEntityId()) {
                                (packet as S12PacketEntityVelocity).motionX = ((packet as S12PacketEntityVelocity).getMotionX() * 0.33).toInt();
                                (packet as S12PacketEntityVelocity).motionZ = ((packet as S12PacketEntityVelocity).getMotionZ() * 0.33).toInt();
                                if (player2.onGround) {
                                    (packet as S12PacketEntityVelocity).motionX = ((packet as S12PacketEntityVelocity).getMotionX() * 0.86).toInt();
                                    (packet as S12PacketEntityVelocity).motionZ = ((packet as S12PacketEntityVelocity).getMotionZ() * 0.86).toInt();
                                }
                                    }
        "glitch" -> {
                                if (!player2.onGround) {
                                    return@handler
                                }

                                hasReceivedVelocity = true;
                                event.cancelEvent();
                                    }
        "aaczero" -> {}
        "simple" -> {
                                handleVelocity(event);
                                    }
        "vulcan" -> {
                                event.cancelEvent();
                                    }
        "blocksmc" -> if (packet is S12PacketEntityVelocity && (packet as S12PacketEntityVelocity).getEntityID() == player2.getEntityId()) {
                                hasReceivedVelocity = true;
                                event.cancelEvent();
                                PacketUtils.sendPacket(C0BPacketEntityAction(player2, net.minecraft.network.play.client.C0BPacketEntityAction.Action.START_SNEAKING));
                                PacketUtils.sendPacket(C0BPacketEntityAction(player2, net.minecraft.network.play.client.C0BPacketEntityAction.Action.STOP_SNEAKING));
                                    }
        "aac" -> {}
        "jump" -> {
                                var packetDirection: Double = 0.0;
                                if (packet is S12PacketEntityVelocity) {
                                    if ((packet as S12PacketEntityVelocity).getEntityID() != player2.getEntityId()) {
                                        return@handler
                                    }

                                    packetDirection = Math.atan2(
 (packet as S12PacketEntityVelocity).motionX.toDouble(), (packet as S12PacketEntityVelocity).motionZ.toDouble()
 );
                                } else if (packet is S27PacketExplosion) {
                                    packetDirection = Math.atan2(
 player2.motionX + (packet as S27PacketExplosion).getField_149152_f(),
 player2.motionZ + (packet as S27PacketExplosion).getField_149159_h()
 );
                                }

                                val angle = Math.toDegrees(packetDirection)
                                var amount2 = (angle % 360.0 + 360.0) % 360.0
                                amount2 = (Math.abs(amount2 + getDirection()) % 360.0 + 360.0) % 360.0
                                if (amount2 <= 180 + 120.0 / 2 && amount2 >= 180 - 120.0 / 2) {
                                    hasReceivedVelocity = true;
                                }
                                    }
        "grimc03" -> if (net.ccbluex.liquidbounce.utils.MovementUtils.isMoving()) {
                                hasReceivedVelocity = true;
                                event.cancelEvent();
                                    }
        "ghostblock" -> {}
        "reverse" -> {}
        "hypixelair" -> {
                                hasReceivedVelocity = true;
                                event.cancelEvent();
                                    }
        "hypixel" -> {
                                hasReceivedVelocity = true;
                                Hypixel.onVelocity(event, player2);
                                    }
        "s32packet" -> {
                                hasReceivedVelocity = true;
                                event.cancelEvent();
                            }
        else -> return@handler
    }


                    hasReceivedVelocity = true;
                }

                if (mode == "BlocksMC" && hasReceivedVelocity && packet is C0BPacketEntityAction) {
                    hasReceivedVelocity = false;
                    event.cancelEvent();
                }

                if (mode == "Vulcan") {
                    if (Disabler.handleEvents() && Disabler.verusCombat.get() && (!Disabler.onlyCombat.get() || KillAura.handleEvents())) {
                        return@handler
                    }

                    if (packet is S32PacketConfirmTransaction) {
                        event.cancelEvent();
                        PacketUtils.sendPacket(
                            C0FPacketConfirmTransaction(
                                if (transaction) 1 else -1,
                                (if (transaction) -1 else 1).toShort(),
                                transaction
                            )
                        );
                        transaction = !transaction;
                    }
                }

                if (mode == "S32Packet" && packet is S32PacketConfirmTransaction) {
                    if (!hasReceivedVelocity) {
                        return@handler
                    }

                    event.cancelEvent();
                    hasReceivedVelocity = false;
                }

                return@handler
            }
        }
    }

    val onTick = handler<GameTickEvent>(always = false) {
        val player2 = mc.thePlayer
        if (player2 == null) {
            return@handler
        } else if (mode == "Hypixel") {
            Hypixel.onTick(player2);
            return@handler
        } else if (!(mode == "GrimC03")) {
            return@handler
        } else {
            if (timerTicks > 0 && mc.timer.timerSpeed <= 1.0F) {
                val pos: Float = 0.8F + 0.2F * (20 - timerTicks) / 20;
                mc.timer.timerSpeed = pos.coerceAtMost(1.0F);
                timerTicks += -1;
            } else if (mc.timer.timerSpeed <= 1.0F) {
                mc.timer.timerSpeed = 1.0F;
            }

            if (hasReceivedVelocity && checkAir(BlockPos(player2.posX, player2.posY, player2.posZ))) {
                hasReceivedVelocity = false;
            }

            return@handler
        }
    }

    // $VF: Extended synchronized range to monitorexit
    val onDelayPacket = handler<PacketEvent>(always = false) { event ->
        val packet = event.packet
        if (event.isCancelled) {
            return@handler
        } else {
            if (mode == "Delay") {
                if (packet is S32PacketConfirmTransaction || packet is S12PacketEntityVelocity) {
                    event.cancelEvent();
                    synchronized (packets) {
                        packets.put(packet, System.currentTimeMillis());
                    }
                }

                delayMode = true;
            } else {
                delayMode = false;
            }

            return@handler
        }
    }

    val onWorld = handler<WorldEvent>(always = false) {
        packets.clear();
        return@handler
    }

    val onGameLoop = handler<GameLoopEvent>(always = false) {
        if (mode == "Delay") {
            sendPacketsByOrder(false);
        }

        return@handler
    }

    val onJump = handler<JumpEvent>(always = false) { event ->
        val thePlayer = mc.thePlayer
        if (thePlayer != null && !thePlayer.isInWater() && !thePlayer.isInLava() && !thePlayer.isInWeb) {
            val player2= mode.toLowerCase(Locale.ROOT);
            if (player2 == "aacpush") {
                jump = true;
                if (!thePlayer.isCollidedVertically) {
                    event.cancelEvent();
                }
            } else if (player2 == "aaczero" && thePlayer.hurtTime > 0) {
                event.cancelEvent();
            }

            return@handler
        } else {
            return@handler
        }
    }

    val onStrafe = handler<StrafeEvent>(always = false) {
        val player2 = mc.thePlayer
        if (player2 == null) {
            return@handler
        } else if ((!(mode == "Jump") || !hasReceivedVelocity) && (
 !(mode == "Hypixel") || !attackReduce || !attackReduceJumpReset || !Hypixel.hasPendingJumpReset
 )) {
            val text3= jumpCooldownMode.toLowerCase(Locale.ROOT);
            if (text3 == "ticks") {
                val count2: Int = limitUntilJump++;
            } else if (text3 == "receivedhits" && player2.hurtTime == 9) {
                val count3: Int = limitUntilJump++;
            }

            return@handler
        } else {
            if (player2.movementInput?.jump != true && (0..100).random() < chance && shouldJump() && player2.isSprinting() && player2.onGround && player2.hurtTime == 9) {
                player2.tryJump();
                limitUntilJump = 0;
            }

            if (mode == "Jump") {
                hasReceivedVelocity = false;
            } else {
                Hypixel.consumeJumpReset();
            }

            return@handler
        }
    }

    val onBlockBB = handler<BlockBBEvent>(always = false) { event ->
        // BlockBBEvent fires on the server thread for EVERY entity's collision check (and mc.thePlayer
        // is null while the world loads), so bail out before touching the player at all.
        if (mode != "GhostBlock") {
            return@handler
        }

        val player = mc.thePlayer ?: return@handler
        if (!hasReceivedVelocity) {
            return@handler
        }

        val hurtRange = hurtTimeRange
        if (hurtRange.first <= player.hurtTime && player.hurtTime <= hurtRange.last) {
            if (event.block is BlockAir && event.y == (player.posY + 1).toInt()) {
                event.boundingBox = AxisAlignedBB(
                    event.x.toDouble(),
                    event.y.toDouble(),
                    event.z.toDouble(),
                    event.x.toDouble() + 1.0,
                    event.y.toDouble() + 1.0,
                    event.z.toDouble() + 1.0
                )
            }
        } else if (player.hurtTime == 0) {
            hasReceivedVelocity = false
        }
    }
}
