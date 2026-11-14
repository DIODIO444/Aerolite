package net.ccbluex.liquidbounce.utils.kotlin

import kotlinx.coroutines.*
import kotlinx.coroutines.test.setMain
import net.minecraft.client.Minecraft
import net.minecraft.util.IThreadListener
import kotlin.coroutines.CoroutineContext

object SharedScopes {

    @JvmField
    val Default = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @JvmField
    val IO = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        // Set dispatcher for Dispatchers.Main
        Dispatchers.setMain(RenderDispatcher)
    }

    fun stop() {
        // Never let a partially resolvable coroutines runtime escape: this runs during shutdown and
        // anything thrown here used to abort the whole client teardown (including config saving).
        runCatching { Default.cancel() }
        runCatching { IO.cancel() }
    }
}

/**
 * To dispatch tasks on Client thread (Render thread)
 * @author MukjepScarlet
 */
private object RenderDispatcher : CoroutineDispatcher() {
    val mc: IThreadListener = Minecraft.getMinecraft()

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        try {
            if (mc.isCallingFromMinecraftThread) {
                block.run()
            } else {
                mc.addScheduledTask(block)
            }
        } catch (e: Throwable) {
            context[CoroutineExceptionHandler]?.handleException(context, e) ?: throw e
        }
    }
}
