/* 
 * Copyright (C) 2011-2012 team-cachebox.de
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
 *
 */

package CB_Core.Math;

/**
 * Die Size Structur enth�lt die Member width und height
 * 
 * @author Longri
 */
public class Size
{
	public int width;
	public int height;

	/**
	 * Constructor
	 * 
	 * @param width
	 * @param height
	 */
	public Size(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	public CB_Rect getBounds()
	{
		return getBounds(0, 0);
	}

	public CB_Rect getBounds(int x, int y)
	{
		return new CB_Rect(x, y, width + x, height + y);
	}

	public CB_Rect getBounds(int x, int y, int k, int l)
	{
		return new CB_Rect(x, y, width + x + k, height + y + l);
	}

	public Size Copy()
	{
		return new Size(this.width, this.height);
	}
}