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
package CB_UI_Base.GL_UI.Controls.Dialogs;

import CB_UI_Base.GL_UI.Controls.Dialog;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.Label.HAlignment;
import CB_UI_Base.GL_UI.Controls.Label.VAlignment;
import CB_UI_Base.Math.CB_RectF;

/**
 * @author Longri
 */
public class Toast extends Dialog {
    public static final int LENGTH_SHORT = 1500;
    public static final int LENGTH_LONG = 3000;

    protected Label mTextField;

    public Toast(CB_RectF rec, String Name) {
        super(rec, Name);

        mTextField = new Label(this.name + " mTextField", rec);
        mTextField.setHAlignment(HAlignment.CENTER);
        mTextField.setVAlignment(VAlignment.CENTER);

        mTextField.setZeroPos();

        super.RemoveChildsFromOverlay();
        super.addChildToOverlay(mTextField);

    }

    public void setWrappedText(String txt) {
        mTextField.setWrappedText(txt);
    }

    @Override
    public void setWidth(float width) {
        super.setWidth(width);
        mTextField.setWidth(width);
        mTextField.setZeroPos();
    }

    @Override
    public void setHeight(float height) {
        super.setHeight(height);
        mTextField.setHeight(height - this.topBorder - this.bottomBorder);
        mTextField.setZeroPos();
    }

}
