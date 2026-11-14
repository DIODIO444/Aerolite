//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.aerolite;

public enum Direction {
    FORWARDS,
    BACKWARDS;

    private Direction() {
    }

    public Direction opposite() {
        return this == FORWARDS ? BACKWARDS : FORWARDS;
    }
}
