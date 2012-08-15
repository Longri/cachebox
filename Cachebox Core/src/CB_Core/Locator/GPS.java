/* 
 * Copyright (C) 2011 team-cachebox.de
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

package CB_Core.Locator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Klasse zum verwalten von GPS Status
 * 
 * @author Longri
 */
public class GPS
{

	private static int mSatVisible;
	private static int mSatFixed;

	private static ArrayList<GpsStrength> mSatList;

	public static int getVisibleSats()
	{
		return mSatVisible;
	}

	public static int getFixedSats()
	{
		return mSatFixed;
	}

	public static ArrayList<GpsStrength> getSatList()
	{
		return mSatList;
	}

	public static String getSatAndFix()
	{
		return String.valueOf(mSatVisible) + "/" + String.valueOf(mSatFixed);
	}

	public void setStatus(GpsStatus status)
	{
		if (status == null) return;

		Iterator<GpsSatellite> statusIterator = status.getSatellites().iterator();

		int satellites = 0;
		int fixed = 0;
		ArrayList<GpsStrength> SatList = new ArrayList<GpsStrength>();
		while (statusIterator.hasNext())
		{
			GpsSatellite sat = statusIterator.next();
			if (sat.usedInFix() == true)
			{
				fixed++;
			}
			satellites++;

			// satellite signal strength

			if (sat.usedInFix())
			{
				// Log.d("Cachbox satellite signal strength", "Sat #" + satellites + ": " + sat.getSnr() + " FIX");
				SatList.add(new GpsStrength(true, sat.getSnr()));
			}
			else
			{
				// Log.d("Cachbox satellite signal strength", "Sat #" + satellites + ": " + sat.getSnr());
				SatList.add(new GpsStrength(false, sat.getSnr()));
			}

		}

		mSatFixed = fixed;
		mSatVisible = satellites;
		Collections.sort(SatList);
		mSatList = SatList;
	}

	public static void setSatFixes(int fixed)
	{
		mSatFixed = fixed;
	}

	public static void setSatVisible(int satellites)
	{
		mSatVisible = satellites;
	}

	public static void setSatList(ArrayList<GpsStrength> coreSatList)
	{
		mSatList = coreSatList;
	}

}