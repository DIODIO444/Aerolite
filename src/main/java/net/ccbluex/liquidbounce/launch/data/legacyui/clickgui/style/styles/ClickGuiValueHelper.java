/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles;

import net.ccbluex.liquidbounce.config.Configurable;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.launch.data.legacyui.ClickGUIModule;
import net.ccbluex.liquidbounce.value.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * Helpers shared by the legacy ClickGUI styles.
 *
 * The new value DSL stores settings inside {@link Configurable} groups (e.g. KillAura's
 * `RotationSettings` or `AutoBlocking`). With `ClickGUIModule.groupSettings` enabled the tree is
 * flattened into "group header + children" rows; otherwise the classic flat value list is used.
 */
public final class ClickGuiValueHelper {

    private ClickGuiValueHelper() {
    }

    /**
     * Rows to render for [module] — either the classic flat list, or the grouped layout when
     * `GroupSettings` is enabled.
     */
    public static List<Value<?>> values(Module module) {
        if (!ClickGUIModule.groupSettings.get()) {
            return module.getValues();
        }

        return flatten(module);
    }

    /**
     * Flattens the value tree into a single list of rows. Group containers are included (so a style
     * can draw them as a header) and are immediately followed by their children.
     */
    public static List<Value<?>> flatten(Module module) {
        List<Value<?>> rows = new ArrayList<>();

        for (Value<?> value : module.getValueTree()) {
            if (value instanceof Configurable) {
                rows.add(value);
                collect((Configurable) value, rows);
            } else {
                rows.add(value);
            }
        }

        return rows;
    }

    private static void collect(Configurable group, List<Value<?>> rows) {
        for (Value<?> value : group.getValues()) {
            if (value instanceof Configurable) {
                rows.add(value);
                collect((Configurable) value, rows);
            } else {
                rows.add(value);
            }
        }
    }
}
