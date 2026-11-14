/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.injection.forge;

import net.ccbluex.liquidbounce.injection.transformers.ForgeNetworkTransformer;
import net.ccbluex.liquidbounce.script.remapper.injection.transformers.AbstractJavaLinkerTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

/**
 * Coremod entry point.
 *
 * <p>Also bootstraps the ported {@code lain.mods.inputfix} IME support: its transformer patches
 * {@code GuiScreen} so composed characters coming from a Chinese / Japanese / Korean input method
 * reach the {@code GuiTextField} instead of being swallowed by LWJGL.</p>
 */
@IFMLLoadingPlugin.TransformerExclusions({"lain.mods.inputfix."})
public class MixinLoader implements IFMLLoadingPlugin {

    public MixinLoader() {
        System.out.println("[LiquidBounce] Injecting with IFMLLoadingPlugin.");

        MixinBootstrap.init();
        Mixins.addConfiguration("liquidbounce.forge.mixins.json");
        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
                ForgeNetworkTransformer.class.getName(),
                AbstractJavaLinkerTransformer.class.getName(),
                "lain.mods.inputfix.InputFixTransformer"
        };
    }

    @Override
    public String getModContainerClass() {
        return "lain.mods.inputfix.InputFixDummyContainer";
    }

    @Override
    public String getSetupClass() {
        return "lain.mods.inputfix.InputFixSetup";
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
