package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.config.ColorValue;
import net.ccbluex.liquidbounce.features.module.modules.render.Animations;
import net.ccbluex.liquidbounce.features.module.modules.combat.AutoArmor;
import net.ccbluex.liquidbounce.features.module.modules.player.InventoryCleaner;
import net.ccbluex.liquidbounce.features.module.modules.world.ChestStealer;
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager;
import net.ccbluex.liquidbounce.utils.render.EaseUtils;
import net.ccbluex.liquidbounce.utils.render.GuiAnimationState;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.timing.TickTimer;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
@SideOnly(Side.CLIENT)
public abstract class MixinGuiContainer extends MixinGuiScreen {

    @Shadow
    protected int xSize;
    @Shadow
    protected int ySize;
    @Shadow
    protected int guiLeft;
    @Shadow
    protected int guiTop;

    /** Timestamp of the last {@code initGui} call, used by the Aerolite open animation. */
    @Unique
    private long guiOpenTime = -1L;

    /** Whether this frame pushed a matrix for the open animation. */
    @Unique
    private boolean invAnimated = false;

    // Separate TickTimer instances to avoid timing conflicts
    @Unique
    final TickTimer tick0 = new TickTimer();
    @Unique
    final TickTimer tick1 = new TickTimer();
    @Unique
    final TickTimer tick2 = new TickTimer();

    @Inject(method = "initGui", at = @At("RETURN"), cancellable = true)
    private void init(CallbackInfo ci) {
        if (ChestStealer.INSTANCE.handleEvents() && ChestStealer.INSTANCE.getSilentGUI()) {
            if (mc.currentScreen instanceof GuiChest) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "initGui", at = @At("RETURN"))
    private void initGuiReturn(CallbackInfo ci) {
        guiOpenTime = System.currentTimeMillis();
    }

    @Inject(method = "drawScreen", at = @At("HEAD"), cancellable = true)
    private void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        // Reset first: every early return below must leave the background untransformed.
        GuiAnimationState.reset();

        if (ChestStealer.INSTANCE.handleEvents() && ChestStealer.INSTANCE.getSilentGUI()) {
            if (mc.currentScreen instanceof GuiChest) {
                ci.cancel();
                return;
            }
        }

        // Aerolite inventory open animation (InvMode / InvTime / InvEase).
        final Animations animations = Animations.INSTANCE;
        final int invTime = animations.getInvTime();
        final String mode = animations.getInvMode().toLowerCase();

        if (invTime <= 0 || guiOpenTime < 0 || (!mode.equals("slide") && !mode.equals("zoom"))) {
            return;
        }

        double pct = Math.max(invTime - (System.currentTimeMillis() - guiOpenTime), 0) / (double) invTime;
        if (pct <= 0) {
            return;
        }

        GL11.glPushMatrix();

        pct = EaseUtils.INSTANCE.apply(
                EaseUtils.EnumEasingType.valueOf(animations.getInvEaseMode()),
                EaseUtils.EnumEasingOrder.valueOf(animations.getInvEaseOrder()),
                pct
        );

        // The dim overlay fades in together with the panel: fully transparent while the panel is
        // still off-screen / collapsed, fully opaque once the animation is done.
        GuiAnimationState.backgroundAlpha = (float) Math.max(0.0, Math.min(1.0, 1.0 - pct));

        if (mode.equals("slide")) {
            GuiAnimationState.backgroundOffsetY = (float) (-(guiTop + ySize) * pct);
            GL11.glTranslated(0, -(guiTop + ySize) * pct, 0);
        } else {
            double scale = Math.max(1 - pct, 0.01);
            double offsetX = ((guiLeft + (xSize * 0.5 * pct)) / scale) - guiLeft;
            double offsetY = ((guiTop + (ySize * 0.5d * pct)) / scale) - guiTop;

            // The background undoes S(scale) * T(offset) to stay pinned to the screen.
            GuiAnimationState.backgroundScale = (float) scale;
            GuiAnimationState.backgroundOffsetX = (float) offsetX;
            GuiAnimationState.backgroundOffsetY = (float) offsetY;

            GL11.glScaled(scale, scale, scale);
            GL11.glTranslated(offsetX, offsetY, 0);
        }

        invAnimated = true;
    }

    @Inject(method = "drawScreen", at = @At("RETURN"))
    private void drawScreenReturn(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (invAnimated) {
            GL11.glPopMatrix();
            invAnimated = false;
        }

        // Remember how opaque the dim was on the last frame so the close fade-out starts from
        // there instead of snapping to full opacity first.
        GuiAnimationState.lastDrawnAlpha = GuiAnimationState.backgroundAlpha;
        GuiAnimationState.reset();
    }

    @Inject(method = "drawSlot", at = @At("HEAD"))
    private void drawSlot(Slot slot, CallbackInfo ci) {
        // Instances
        final InventoryManager inventoryManager = InventoryManager.INSTANCE;
        final ChestStealer chestStealer = ChestStealer.INSTANCE;
        final InventoryCleaner inventoryCleaner = InventoryCleaner.INSTANCE;
        final AutoArmor autoArmor = AutoArmor.INSTANCE;
        final RenderUtils renderUtils = RenderUtils.INSTANCE;

        // Slot X/Y
        int x = slot.xDisplayPosition;
        int y = slot.yDisplayPosition;

        // ChestStealer Highlight Values
        int chestStealerBackgroundColor = ((ColorValue) chestStealer.getBackgroundColor()).selectedColor().getRGB();
        int chestStealerBorderColor = ((ColorValue) chestStealer.getBorderColor()).selectedColor().getRGB();

        // InvCleaner & AutoArmor Highlight Values
        int invManagerBackgroundColor = ((ColorValue) inventoryManager.getBackgroundColor()).selectedColor().getRGB();
        int invManagerBorderColor = ((ColorValue) inventoryManager.getBorderColor()).selectedColor().getRGB();

        // Get the current slot being stolen
        int currentSlotChestStealer = inventoryManager.getChestStealerCurrentSlot();
        int currentSlotInvCleaner = inventoryManager.getInvCleanerCurrentSlot();
        int currentSlotAutoArmor = inventoryManager.getAutoArmorCurrentSlot();

        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_LIGHTING);

        if (mc.currentScreen instanceof GuiChest) {
            if (chestStealer.handleEvents() && !chestStealer.getSilentGUI() && chestStealer.getHighlightSlot()) {
                if (slot.slotNumber == currentSlotChestStealer && currentSlotChestStealer != -1 && currentSlotChestStealer != inventoryManager.getChestStealerLastSlot()) {
                    renderUtils.drawBorderedRect(x, y, x + 16, y + 16, chestStealer.getBorderStrength(), chestStealerBorderColor, chestStealerBackgroundColor);

                    // Prevent rendering the highlighted rectangle twice
                    if (!slot.getHasStack() && tick0.hasTimePassed(100)) {
                        inventoryManager.setChestStealerLastSlot(currentSlotChestStealer);
                        tick0.reset();
                    } else {
                        tick0.update();
                    }
                }
            }
        }

        if (mc.currentScreen instanceof GuiInventory) {
            if (inventoryManager.getHighlightSlotValue().get()) {
                if (inventoryCleaner.handleEvents()) {
                    if (slot.slotNumber == currentSlotInvCleaner && currentSlotInvCleaner != -1 && currentSlotInvCleaner != inventoryManager.getInvCleanerLastSlot()) {
                        renderUtils.drawBorderedRect(x, y, x + 16, y + 16, inventoryManager.getBorderStrength().get(), invManagerBorderColor, invManagerBackgroundColor);

                        // Prevent rendering the highlighted rectangle twice
                        if (!slot.getHasStack() && tick1.hasTimePassed(100)) {
                            inventoryManager.setInvCleanerLastSlot(currentSlotInvCleaner);
                            tick1.reset();
                        } else {
                            tick1.update();
                        }
                    }
                }

                if (autoArmor.handleEvents()) {
                    if (slot.slotNumber == currentSlotAutoArmor && currentSlotAutoArmor != -1 && currentSlotAutoArmor != inventoryManager.getAutoArmorLastSlot()) {
                        renderUtils.drawBorderedRect(x, y, x + 16, y + 16, inventoryManager.getBorderStrength().get(), invManagerBorderColor, invManagerBackgroundColor);

                        // Prevent rendering the highlighted rectangle twice
                        if (!slot.getHasStack() && tick2.hasTimePassed(100)) {
                            inventoryManager.setAutoArmorLastSlot(currentSlotAutoArmor);
                            tick2.reset();
                        } else {
                            tick2.update();
                        }
                    }
                }
            }
        }

        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }
}
