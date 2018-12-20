/*
 * Copyright 2010, 2011, 2012, 2013 mapsforge.org
 * Copyright 2014, 2015 devemux86
 * Copyright 2014 Ludwig M Brinckmann
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.map.awt.graphics;

import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.*;
import org.mapsforge.core.model.Point;

import java.awt.*;
import java.awt.image.BufferedImage;

public class AwtPaint implements Paint {

    // Avoid creating unnecessary objects
    private final BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    protected java.awt.Color color;
    protected Font font;
    protected String fontName;
    protected int fontStyle;
    protected float strokeWidth;
    protected float textSize;
    Stroke stroke;
    Style style;
    TexturePaint texturePaint;
    private int cap;
    private int join;
    private float[] strokeDasharray;
    // needed to record size of bitmap shader to compute the shift
    private int shaderWidth;
    private int shaderHeight;
    public AwtPaint() {
        this.cap = getCap(Cap.ROUND);
        this.color = java.awt.Color.BLACK;
        this.style = Style.FILL;
        this.join = getJoin(Join.ROUND);
    }
    AwtPaint(Paint paint) {
        AwtPaint ap = (AwtPaint) paint;
        this.cap = ap.cap;
        this.color = ap.color;
        this.style = ap.style;
        this.join = ap.join;
        this.stroke = ap.stroke;
        this.fontStyle = ap.fontStyle;
        this.font = ap.font;
        this.fontName = ap.fontName;
        this.strokeWidth = ap.strokeWidth;
        this.textSize = ap.textSize;
        this.strokeDasharray = ap.strokeDasharray;
    }

    private static int getCap(Cap cap) {
        switch (cap) {
            case BUTT:
                return BasicStroke.CAP_BUTT;
            case ROUND:
                return BasicStroke.CAP_ROUND;
            case SQUARE:
                return BasicStroke.CAP_SQUARE;
        }

        throw new IllegalArgumentException("unknown cap: " + cap);
    }

    private static Cap getMampsforgeCap(int cap) {
        switch (cap) {
            case BasicStroke.CAP_BUTT:
                return Cap.BUTT;
            case BasicStroke.CAP_ROUND:
                return Cap.ROUND;
            case BasicStroke.CAP_SQUARE:
                return Cap.SQUARE;
        }

        throw new IllegalArgumentException("unknown cap: " + cap);
    }

    private static String getFontName(FontFamily fontFamily) {
        switch (fontFamily) {
            case MONOSPACE:
                return Font.MONOSPACED;
            case DEFAULT:
                return null;
            case SANS_SERIF:
                return Font.SANS_SERIF;
            case SERIF:
                return Font.SERIF;
        }

        throw new IllegalArgumentException("unknown fontFamily: " + fontFamily);
    }

    private static int getFontStyle(FontStyle fontStyle) {
        switch (fontStyle) {
            case BOLD:
                return Font.BOLD;
            case BOLD_ITALIC:
                return Font.BOLD | Font.ITALIC;
            case ITALIC:
                return Font.ITALIC;
            case NORMAL:
                return Font.PLAIN;
        }

        throw new IllegalArgumentException("unknown fontStyle: " + fontStyle);
    }

    private static int getJoin(Join join) {
        switch (join) {
            case ROUND:
                return BasicStroke.JOIN_ROUND;
            case BEVEL:
                return BasicStroke.JOIN_BEVEL;
            case MITER:
                return BasicStroke.JOIN_MITER;
        }

        throw new IllegalArgumentException("unknown cap: " + join);
    }

    @Override
    public int getTextHeight(String text) {
        Graphics2D graphics2d = bufferedImage.createGraphics();
        FontMetrics fontMetrics = graphics2d.getFontMetrics(this.font);
        graphics2d.dispose();
        return (int) this.font.createGlyphVector(fontMetrics.getFontRenderContext(), text).getVisualBounds().getHeight();
    }

    @Override
    public int getTextWidth(String text) {
        Graphics2D graphics2d = bufferedImage.createGraphics();
        FontMetrics fontMetrics = graphics2d.getFontMetrics(this.font);
        graphics2d.dispose();
        return fontMetrics.stringWidth(text);
    }

    @Override
    public boolean isTransparent() {
        return this.texturePaint == null && this.color.getAlpha() == 0;
    }

    @Override
    public void setBitmapShader(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        this.shaderWidth = bitmap.getWidth();
        this.shaderHeight = bitmap.getHeight();
        Rectangle rectangle = new Rectangle(0, 0, bitmap.getWidth(), bitmap.getHeight());
        this.texturePaint = new TexturePaint(AwtGraphicFactory.getBufferedImage(bitmap), rectangle);
    }

    /**
     * Shifts the bitmap pattern so that it will always start at a multiple of
     * itself for any tile the pattern is used. This ensures that regardless of
     * size of the pattern it tiles correctly.
     *
     * @param origin the reference point
     */
    @Override
    public void setBitmapShaderShift(Point origin) {
        if (this.texturePaint != null) {
            int relativeDx = ((int) -origin.x) % this.shaderWidth;
            int relativeDy = ((int) -origin.y) % this.shaderHeight;

            Rectangle rectangle = new Rectangle(relativeDx, relativeDy, this.shaderWidth, this.shaderHeight);
            this.texturePaint = new TexturePaint(this.texturePaint.getImage(), rectangle);
        }
    }

    @Override
    public void setColor(Color color) {
        this.color = AwtGraphicFactory.getColor(color);
    }

    @Override
    public void setDashPathEffect(float[] strokeDasharray) {
        this.strokeDasharray = strokeDasharray;
        createStroke();
    }

    @Override
    public void setStrokeCap(Cap cap) {
        this.cap = getCap(cap);
        createStroke();
    }

    @Override
    public void setStrokeJoin(Join join) {
        this.join = getJoin(join);
        createStroke();
    }

    @Override
    public void setTextAlign(Align align) {
        // this.align = align; //never read
    }

    @Override
    public void setTypeface(FontFamily fontFamily, FontStyle fontStyle) {
        this.fontName = getFontName(fontFamily);
        this.fontStyle = getFontStyle(fontStyle);
        createFont();
    }

    private void createFont() {
        if (this.textSize > 0) {
            this.font = new Font(this.fontName, this.fontStyle, (int) this.textSize);
        } else {
            this.font = null;
        }
    }

    private void createStroke() {
        if (this.strokeWidth <= 0) {
            return;
        }
        this.stroke = new BasicStroke(this.strokeWidth, this.cap, this.join, this.join == BasicStroke.JOIN_MITER ? 1.0f : 0, this.strokeDasharray, 0);
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    @Override
    public Cap getCap() {
        return getMampsforgeCap(this.cap);
    }

    @Override
    public int getColor() {
        return this.color.getRGB();
    }

    @Override
    public void setColor(int color) {
        this.color = new java.awt.Color(color, true);
    }

    @Override
    public Style getStyle() {
        return this.style;
    }

    //###################################################

    @Override
    public void setStyle(Style style) {
        this.style = style;
    }

    @Override
    public float getStrokeWidth() {
        return this.strokeWidth;
    }

    @Override
    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        createStroke();
    }

    @Override
    public float getTextSize() {
        return this.textSize;
    }

    @Override
    public void setTextSize(float textSize) {
        this.textSize = textSize;
        createFont();
    }

    @Override
    public float[] getDashArray() {
        return this.strokeDasharray;
    }
}
