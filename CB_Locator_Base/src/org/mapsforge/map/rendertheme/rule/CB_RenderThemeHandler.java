/*
 * Copyright 2010, 2011, 2012, 2013 mapsforge.org
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
package org.mapsforge.map.rendertheme.rule;

import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.xmlpull.v1.XmlPullParser;

/**
 * SAX2 handler to parse XML render theme files.
 */
public final class CB_RenderThemeHandler extends RenderThemeHandler {

    public CB_RenderThemeHandler(GraphicFactory graphicFactory, DisplayModel displayModel, String relativePathPrefix, XmlRenderTheme xmlRenderTheme, XmlPullParser pullParser) {
        super(graphicFactory, displayModel, relativePathPrefix, xmlRenderTheme, pullParser);

    }

}
