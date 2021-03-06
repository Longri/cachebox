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
package CB_UI.GL_UI.Main.Actions;

import CB_Core.Database;
import CB_Core.Import.DescriptionImageGrabber;
import CB_Core.Types.Cache;
import CB_UI.GlobalCore;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.Log.Log;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.LinkedList;

/**
 * @author Longri
 */
public class CB_Action_ShowDescExt extends CB_Action {

    private static final String log = "CB_Action_ShowDescExt";
    private final String TEMP_CACHE_HTML_FILE = "temp.html";

    private final LinkedList<String> NonLocalImages = new LinkedList<String>();
    private final LinkedList<String> NonLocalImagesUrl = new LinkedList<String>();

    public CB_Action_ShowDescExt() {
        super("descExt", MenuID.AID_SHOW_DescExt);
    }

    /**
     * Execute
     */
    @Override
    public void Execute() {
        if (getEnabled()) {

            //save desc Html local and show ext
            Cache cache = GlobalCore.getSelectedCache();
            NonLocalImages.clear();
            NonLocalImagesUrl.clear();
            String cachehtml = Database.GetShortDescription(cache) + Database.GetDescription(cache);
            String html = DescriptionImageGrabber.ResolveImages(cache, cachehtml, false, NonLocalImages, NonLocalImagesUrl);
            String header = "<!DOCTYPE html><html><head><meta http-equiv=\"Content-type\" content=\"text/html; charset=utf-8\" /></head><body>";
            html = header + html;

            // add 2 empty lines so that the last line of description can be selected with the markers
            // add trailer
            html += "</br></br>" + "</body></html>";

            String filePath = CB_UI_Base_Settings.ImageCacheFolder.getValue() + "/" + TEMP_CACHE_HTML_FILE;

            try {

                OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(filePath), Charset.forName("utf-8"));
                out.write(html);
                out.close();

                PlatformConnector.callUrl("file://" + filePath);

            } catch (IOException e) {
                e.printStackTrace();
                Log.err(log, "Write Temp HTML:", e);
            }
        }
    }

    @Override
    public boolean getEnabled() {
        // liefert true zurück wenn ein Cache gewählt ist und dieser einen Hint hat
        if (GlobalCore.getSelectedCache() == null)
            return false;

        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.hintIcon.name());
    }
}
