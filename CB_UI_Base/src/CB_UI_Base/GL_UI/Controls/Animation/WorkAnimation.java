package CB_UI_Base.GL_UI.Controls.Animation;

import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.Math.CB_RectF;

public class WorkAnimation extends RotateAnimation {
    protected static WorkAnimation mINSTANCE = new WorkAnimation();

    public WorkAnimation() {
        super(new CB_RectF(0, 0, 50, 50), "DownloadAnimation");

        setSprite(Sprites.getSprite(IconName.settings.name()));
        setOrigin(this.getHalfWidth(), this.getHalfHeight());
        play(ANIMATION_DURATION);
    }

    public static AnimationBase GetINSTANCE() {
        return mINSTANCE;
    }

    public static AnimationBase GetINSTANCE(CB_RectF rec) {
        mINSTANCE.setRec(rec);
        return mINSTANCE;
    }

    @Override
    public AnimationBase INSTANCE() {
        return mINSTANCE;
    }

    @Override
    public AnimationBase INSTANCE(CB_RectF rec) {
        mINSTANCE.setRec(rec);
        return mINSTANCE;
    }

    /**
     * ljhgjlk
     */
    @Override
    public void resize(float width, float height) {
        super.resize(width, height);
        setOrigin(this.getHalfWidth(), this.getHalfHeight());
    }

    @Override
    public void play() {
        play(ANIMATION_DURATION);
    }

    @Override
    public void dispose() {
        // do nothing, is FINAL GLOBAL animation
    }

}
