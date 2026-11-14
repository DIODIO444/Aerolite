package net.ccbluex.liquidbounce.utils.render;


public final class Translate {
    private float x;
    private float y;
    private boolean first = false;

    public Translate(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public final void interpolate(float targetX, float targetY, double smoothing) {
        if(first) {
            this.x = AnimationUtils.INSTANCE.animate(targetX, this.x, (float) smoothing);
            this.y = AnimationUtils.INSTANCE.animate(targetY, this.y, (float) smoothing);
        } else {
            this.x = targetX;
            this.y = targetY;
            first = true;
        }
    }
    public void translate(float targetX, float targetY) {
        x = AnimationUtils.INSTANCE.lstransition(x, targetX, 0.0);
        y = AnimationUtils.INSTANCE.lstransition(y, targetY, 0.0);
    }
    public void translate(float targetX, float targetY, double speed) {
        x = AnimationUtils.INSTANCE.lstransition(x, targetX, speed);
        y = AnimationUtils.INSTANCE.lstransition(y, targetY, speed);
    }
    public final void interpolate2(float targetX, float targetY, double smoothing) {
        this.x = targetX;
        this.y = AnimationUtils.INSTANCE.animate(targetY, this.y, (float) smoothing);
    }

    public float getX() {
        return this.x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return this.y;
    }

    public void setY(float y) {
        this.y = y;
    }
}