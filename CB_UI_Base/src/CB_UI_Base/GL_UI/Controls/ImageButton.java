/*
 * Copyright (C) 2015 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_UI_Base.GL_UI.Controls;

import CB_UI_Base.Math.CB_RectF;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class ImageButton extends Button {
    protected final Image image;
    float mScale = 1f;
    float mAngle = 0;

    public ImageButton(String name) {
        super(name);
        this.setText("");
        image = new Image(this.ScaleCenter(0.8f), "", false);
        this.addChild(image);
    }

    public ImageButton(CB_RectF rec, String name) {
        super(rec, name);
        image = new Image(this.ScaleCenter(0.8f), "", false);
        this.addChild(image);
    }

    public ImageButton(ImageLoader img) {
        super("");
        image = new Image(img, this.ScaleCenter(0.8f), "", false);
        this.addChild(image);
    }

    public ImageButton(Image image) {
        super("");
        if (image == null) {
            this.image = new Image(this.ScaleCenter(0.8f), "", false);
        } else {
            this.image = image;
        }
        this.addChild(image);
    }

    @Override
    protected void render(Batch batch) {
        super.render(batch);
        if (isDisabled) {
            image.setColor(new Color(1f, 1f, 1f, 0.5f));
        } else {
            image.setColor(null);
        }
    }

    private void chkImagePos() {
        CB_RectF thisRectF = this.copy();
        thisRectF.setPos(0, 0);
        image.setRec(thisRectF.ScaleCenter(0.8f * mScale));
    }

    public void setImage(Drawable drawable) {
        image.setDrawable(drawable);
        chkImagePos();
    }

    public void setImage(Sprite sprite) {
        image.setSprite(sprite, false);
        chkImagePos();
    }

    public void setImageRotation(Float angle) {
        mAngle = angle;
        chkImagePos();
        image.setRotate(angle);
        image.setOrigin(image.getHalfWidth(), image.getHalfHeight());
    }

    public void setImageScale(float scale) {
        mScale = scale;
        chkImagePos();
        image.setRotate(mAngle);
        image.setOrigin(image.getHalfWidth(), image.getHalfHeight());
    }

    @Override
    public void resize(float width, float height) {
        chkImagePos();
        image.setRotate(mAngle);
        image.setOrigin(image.getHalfWidth(), image.getHalfHeight());
    }

    @Override
    public void dispose() {
        image.dispose();
        super.dispose();
    }

    public void clearImage() {
        image.clearImage();
    }

}
