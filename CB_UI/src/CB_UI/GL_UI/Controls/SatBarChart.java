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
package CB_UI.GL_UI.Controls;

import CB_Locator.Events.GpsStateChangeEvent;
import CB_Locator.Events.GpsStateChangeEventList;
import CB_Locator.GPS;
import CB_Locator.GpsStrength;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;

public class SatBarChart extends CB_View_Base implements GpsStateChangeEvent {

    private boolean drawWithAlpha = false;
    private Image[] balken = null;
    private boolean redraw = true;

    public SatBarChart(CB_RectF rec, String Name) {
        super(rec, Name);
    }

    public void setDrawWithAlpha(boolean value) {
        drawWithAlpha = value;
        redraw = true;
    }

    @Override
    protected void render(Batch batch) {
        if (redraw)
            setSatStrength();
    }

    private void setSatStrength() {
        float minH = (Sprites.bar.getBottomHeight() / 2) + Sprites.bar.getTopHeight();

        float w = (this.getWidth() / 14);
        boolean small = Sprites.bar.getMinWidth() > w * 1.2f;
        if (small) {
            w = (this.getWidth() / 12);
        }

        // calc Colors
        Color red = Color.RED.cpy();
        Color grn = Color.GREEN.cpy();
        Color gry = Color.LIGHT_GRAY.cpy();

        if (drawWithAlpha) {
            red.a = 0.4f;
            grn.a = 0.4f;
            gry.a = 0.4f;
        }

        if (balken == null) {

            float iniHeight = small ? Sprites.barSmall.getTopHeight() : Sprites.bar.getTopHeight();

            w += 1;
            balken = new Image[14];
            balken[0] = new Image(new CB_RectF(0, 0, w, iniHeight), "", false);
            balken[1] = new Image(new CB_RectF(balken[0].getMaxX() - 1, 0, w, iniHeight), "", false);
            balken[2] = new Image(new CB_RectF(balken[1].getMaxX() - 1, 0, w, iniHeight), "", false);
            balken[3] = new Image(new CB_RectF(balken[2].getMaxX() - 1, 0, w, iniHeight), "", false);
            balken[4] = new Image(new CB_RectF(balken[3].getMaxX() - 1, 0, w, iniHeight), "", false);
            balken[5] = new Image(new CB_RectF(balken[4].getMaxX() - 1, 0, w, iniHeight), "", false);
            balken[6] = new Image(new CB_RectF(balken[5].getMaxX() - 1, 0, w, iniHeight), "", false);
            balken[7] = new Image(new CB_RectF(balken[6].getMaxX() - 1, 0, w, iniHeight), "", false);
            balken[8] = new Image(new CB_RectF(balken[7].getMaxX() - 1, 0, w, iniHeight), "", false);
            balken[9] = new Image(new CB_RectF(balken[8].getMaxX() - 1, 0, w, iniHeight), "", false);
            balken[10] = new Image(new CB_RectF(balken[9].getMaxX() - 1, 0, w, iniHeight), "", false);
            balken[11] = new Image(new CB_RectF(balken[10].getMaxX() - 1, 0, w, iniHeight), "", false);
            if (!small)
                balken[12] = new Image(new CB_RectF(balken[11].getMaxX() - 1, 0, w, iniHeight), "", false);
            if (!small)
                balken[13] = new Image(new CB_RectF(balken[12].getMaxX() - 1, 0, w, iniHeight), "", false);

            for (Image tmp : balken) {
                if (tmp != null) {
                    tmp.setDrawable(small ? Sprites.barSmall_0 : Sprites.bar_0);
                    this.addChild(tmp);
                }
            }
        }

        int count = 0;
        if (GPS.getSatList() != null) {
            for (int i = 0, n = GPS.getSatList().size(); i < n; i++) {
                GpsStrength tmp;
                try {
                    tmp = GPS.getSatList().get(i);
                } catch (Exception e) {
                    break;
                }

                try {
                    // balken höhe festlegen
                    if (balken[count] != null) {
                        float barHeight = Math.min((tmp.getStrength() * 3 / 100) * this.getHeight(), this.getHeight());

                        if (barHeight < minH) {
                            barHeight = small ? Sprites.barSmall.getTopHeight() : Sprites.bar.getTopHeight();
                            balken[count].setDrawable(small ? Sprites.barSmall_0 : Sprites.bar_0);
                        } else {
                            balken[count].setDrawable(small ? Sprites.barSmall : Sprites.bar);
                        }

                        balken[count].setHeight(barHeight);

                        // // balken farbe festlegen
                        if (tmp.getFixed()) {
                            balken[count].setColor(grn);
                        } else {
                            balken[count].setColor(red);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                count++;
                if (count >= 13)
                    break;
            }
        }

        // restliche balken ausschalten!
        if (count < 14) {
            for (int i = count; i <= 13; i++) {
                if (balken[i] != null)
                    balken[i].setColor(gry);
            }
        }

        redraw = false;
        GL.that.renderOnce();

    }

    @Override
    public void GpsStateChanged() {
        redraw = true;
        GL.that.renderOnce();
    }

    @Override
    public void dispose() {
        GpsStateChangeEventList.Remove(this);
    }

    @Override
    public void onShow() {
        GpsStateChangeEventList.Add(this);
        redraw = true;
    }

    @Override
    public void onHide() {
        GpsStateChangeEventList.Remove(this);
    }

    @Override
    public void onResized(CB_RectF rec) {
        super.onResized(rec);
        redraw = true;
    }
}
