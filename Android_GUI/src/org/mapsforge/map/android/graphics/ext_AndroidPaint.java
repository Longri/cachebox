/*
 * Copyright (C) 2014 team-cachebox.de
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
package org.mapsforge.map.android.graphics;

import CB_UI_Base.graphics.*;
import CB_UI_Base.graphics.extendedInterfaces.ext_Matrix;
import CB_UI_Base.graphics.extendedInterfaces.ext_Paint;
import CB_Utils.Util.HSV_Color;
import org.mapsforge.core.graphics.FontFamily;
import org.mapsforge.core.graphics.FontStyle;

/**
 * @author Longri
 */
public class ext_AndroidPaint extends AndroidPaint implements ext_Paint {

    FontStyle fontStyle;
    FontFamily fontFamily;

    // ############################################################################################
    // Overrides for CB.ext_Paint
    // ############################################################################################

    public ext_AndroidPaint(ext_Paint paint) {

    }

    public ext_AndroidPaint() {

    }

    @Override
    public void setAlpha(int i) {

    }

    @Override
    public void setStrokeJoin(Join join) {

    }

    @Override
    public void setRadialGradiant(float x, float y, float radius, int[] colors, float[] positions, TileMode tileMode) {

    }

    @Override
    public void setGradientMatrix(ext_Matrix matrix) {

    }

    @Override
    public void setLinearGradient(float x1, float y1, float x2, float y2, int[] colors, float[] positions, TileMode tileMode) {

    }

    @Override
    public GL_Style getGL_Style() {

        return null;
    }

    @Override
    public void setDashPathEffect(float[] strokeDasharray, float offset) {

    }

    @Override
    public void delDashPathEffect() {

    }

    @Override
    public ext_Matrix getGradiantMatrix() {

        return null;
    }

    @Override
    public void setStyle(GL_Style fill) {

    }

    @Override
    public GL_FontStyle getGLFontStyle() {

        switch (fontStyle) {
            case BOLD:
                return GL_FontStyle.BOLD;
            case BOLD_ITALIC:
                return GL_FontStyle.BOLD_ITALIC;
            case ITALIC:
                return GL_FontStyle.ITALIC;
            case NORMAL:
                return GL_FontStyle.NORMAL;
            default:
                return GL_FontStyle.NORMAL;
        }

    }

    // ############################################################################################
    // Overrides for mapsforge.AndroidPaint
    // ############################################################################################

    @Override
    public GL_FontFamily getGLFontFamily() {

        switch (fontFamily) {
            case DEFAULT:
                return GL_FontFamily.DEFAULT;
            case MONOSPACE:
                return GL_FontFamily.MONOSPACE;
            case SANS_SERIF:
                return GL_FontFamily.SANS_SERIF;
            case SERIF:
                return GL_FontFamily.SERIF;
            default:
                return GL_FontFamily.DEFAULT;
        }
    }

    @Override
    public HSV_Color getHSV_Color() {
        HSV_Color c = new HSV_Color(this.paint.getColor());
        return c;
    }

    @Override
    public float getTextSize() {
        return this.paint.getTextSize();
    }

    @Override
    public float getStrokeWidth() {
        return this.paint.getStrokeWidth();
    }

}
