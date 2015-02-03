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
package CB_UI_Base.GL_UI.Controls.html;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;

import net.htmlparser.jericho.Source;

import org.junit.Test;

import CB_UI_Base.GL_UI.Controls.Label.HAlignment;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Util.HSV_Color;

import com.badlogic.gdx.graphics.Color;

/**
 * @author Longri
 */
public class HtmlView_Test {
    static final String br = "\r\n";
    static String HTMLSOURCE;
    static String HTMLSOURCE_2;
    static String HTMLSOURCE_3;
    static String HTMLSOURCE_4;
    static String HTML_IMAGE_TAG = "<img src=\"http://img.geocaching.com/cache/e96baf07-b869-4568-a1ef-8a69d27a3e43.jpg\" />";
    static float DEFAULT_FONTSIZE;

    static {
	HTMLSOURCE = getFileFromResource("HTMLSOURCE.html");
	HTMLSOURCE_2 = getFileFromResource("HTMLSOURCE_2.html");
	HTMLSOURCE_3 = getFileFromResource("HTMLSOURCE_3.html");
	HTMLSOURCE_4 = getFileFromResource("HTMLSOURCE_4.html");

	if (UiSizes.that == null) {
	    new UiSizes().setScale(1.5f);
	}
	DEFAULT_FONTSIZE = 14 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR;
    }

    private static String getFileFromResource(String file) {
	// Get file from resources folder
	try {
	    InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
	    Writer writer = new StringWriter();

	    char[] buffer = new char[1024];
	    try {
		Reader reader = new BufferedReader(new InputStreamReader(inputStream, "CP1252"));
		int n;
		while ((n = reader.read(buffer)) != -1) {
		    writer.write(buffer, 0, n);
		}
	    } finally {
		inputStream.close();
	    }

	    return writer.toString();

	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	return "";
    }

    @Test
    public void parserTest() {
	Source source = new CB_FormatedHtmlSource(HTMLSOURCE);
	CB_Html_Renderer renderer = new CB_Html_Renderer(source);
	List<Html_Segment> segmentList = renderer.getElementList();

	Html_Segment testSeg = segmentList.get(0);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(new HSV_Color("333399")));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 18 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.startsWith("FEZ - An der W"));

	testSeg = segmentList.get(1);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(testSeg instanceof Html_Segment_Image);
	assertTrue(testSeg.formatetText.startsWith("http://img.geocaching.com/cache/e96b"));

	testSeg = segmentList.get(2);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(new HSV_Color("333399")));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 14 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.startsWith(br + br));

	testSeg = segmentList.get(3);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(new HSV_Color("333399")));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 14 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.startsWith("Hier geht es um einen klei"));

	testSeg = segmentList.get(4);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(new HSV_Color("333399")));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 12 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.startsWith("In diesem Park befinden si"));

	testSeg = segmentList.get(5);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(new HSV_Color("333399")));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 14 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.startsWith(br + br + "Viel Spa� bei der Suche"));

	testSeg = segmentList.get(6);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(new HSV_Color("FF0000")));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 14 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.equals(br + br));

	testSeg = segmentList.get(7);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(new HSV_Color("FF0000")));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 14 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.startsWith("PS: Passt bitte auf die grau"));

	testSeg = segmentList.get(8);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(new HSV_Color("333399")));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 14 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.startsWith(br + br));

	testSeg = segmentList.get(9);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(testSeg instanceof Html_Segment_Image);
	assertTrue(testSeg.formatetText.startsWith("http://www.gif-star.com/tie"));

	testSeg = segmentList.get(10);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(Color.BLACK));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 14 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.equals(br));

	testSeg = segmentList.get(11);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(Color.BLACK));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 14 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.equals(br + br + " " + br + br));

	testSeg = segmentList.get(12);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(Color.BLACK));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 14 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.equals(br + br));

	testSeg = segmentList.get(13);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(Color.BLACK));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 14 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.equals(br + br));

	assertTrue(segmentList.size() == 14);
    }

    @Test
    public void parserTest2() {

	Source source = new CB_FormatedHtmlSource(HTMLSOURCE_4);
	CB_Html_Renderer renderer = new CB_Html_Renderer(source);
	List<Html_Segment> segmentList = renderer.getElementList();

	Html_Segment testSeg = segmentList.get(0);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(new HSV_Color("333399")));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 18 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.startsWith("FEZ - An der W"));

	testSeg = segmentList.get(1);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(testSeg instanceof Html_Segment_Image);
	assertTrue(testSeg.formatetText.startsWith("http://img.geocaching.com/cache/e96b"));

	testSeg = segmentList.get(2);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(new HSV_Color("333399")));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 14 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.startsWith(br + br));

	testSeg = segmentList.get(3);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(new HSV_Color("333399")));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 14 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.startsWith("Hier geht es um einen klei"));

	testSeg = segmentList.get(4);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(new HSV_Color("333399")));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 12 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.startsWith("In diesem Park befinden si"));

	testSeg = segmentList.get(5);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(new HSV_Color("333399")));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 14 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.startsWith(br + br + "Viel Spa� bei der Suche"));

	testSeg = segmentList.get(6);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(new HSV_Color("FF0000")));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 14 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.equals(br + br));

	testSeg = segmentList.get(7);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(new HSV_Color("FF0000")));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 14 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.startsWith("PS: Passt bitte auf die grau"));

	testSeg = segmentList.get(8);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(new HSV_Color("333399")));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 14 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.startsWith(br + br));

	testSeg = segmentList.get(9);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(testSeg instanceof Html_Segment_Image);
	assertTrue(testSeg.formatetText.startsWith("http://www.gif-star.com/tie"));

	testSeg = segmentList.get(10);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(Color.BLACK));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 14 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.equals(br));

	testSeg = segmentList.get(11);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(Color.BLACK));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 14 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.equals(br + br + " " + br + br));

	testSeg = segmentList.get(12);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(Color.BLACK));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 14 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.equals(br + br));

	testSeg = segmentList.get(13);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.CENTER);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(Color.BLACK));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == 14 * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR);
	assertTrue(testSeg.formatetText.equals(br + br));

	assertTrue(segmentList.size() == 14);
    }

    @Test
    public void parserTest3() {
	Source source = new CB_FormatedHtmlSource(HTMLSOURCE_2);
	CB_Html_Renderer renderer = new CB_Html_Renderer(source);
	List<Html_Segment> segmentList = renderer.getElementList();

	Html_Segment testSeg = segmentList.get(0);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.LEFT);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(Color.BLACK));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == DEFAULT_FONTSIZE);
	assertTrue(testSeg.formatetText.startsWith("Cacherausbildung ( oder : auch di"));

	testSeg = segmentList.get(1);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.LEFT);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(Color.BLACK));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == DEFAULT_FONTSIZE);
	assertTrue(testSeg.formatetText.equals(br + br));

	testSeg = segmentList.get(2);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.LEFT);
	assertTrue(testSeg instanceof Html_Segment_HR);
	assertTrue(testSeg.formatetText.startsWith("--Todo--HR----Todo--HR-"));

	testSeg = segmentList.get(3);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.LEFT);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(Color.BLACK));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == DEFAULT_FONTSIZE);
	assertTrue(testSeg.formatetText.startsWith(br + br + "Im FEZ kann man spielen, man"));

	testSeg = segmentList.get(4);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.LEFT);
	assertTrue(testSeg instanceof Html_Segment_Image);
	assertTrue(testSeg.formatetText.startsWith("http://imgcdn.geocaching.com/cache/large/00ababb1"));

	testSeg = segmentList.get(5);
	testSeg.resolveAtributes();
	assertTrue(testSeg.hAlignment == HAlignment.LEFT);
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontColor().equals(Color.BLACK));
	assertTrue(((Html_Segment_TextBlock) testSeg).getFontSize() == DEFAULT_FONTSIZE);
	assertTrue(testSeg.formatetText.startsWith(br + br + "4. Hinweis beachten : magnet"));

	assertTrue(segmentList.size() == 6);
    }

}