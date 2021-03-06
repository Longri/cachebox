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

package CB_Core.Types;

import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.Util.UnitFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Eine ArrayList<MeasuredCoord> welche die gemessenen Koordinaten aufnimmt, sortiert, Ausreißer eliminiert und über die Methode
 * "getMeasuredAverageCoord()" eine Durchschnitts Koordinate zurück gibt.
 *
 * @author Longri
 */
public class MeasuredCoordList extends ArrayList<MeasuredCoord> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Gibt die Durchschnittliche Koordinate dieser Liste zurück.
     *
     * @return Coordinate
     */
    public Coordinate getMeasuredAverageCoord() {

        Coordinate ret;

        if (this.size() == 0) {
            ret = new CoordinateGPS(0, 0);
            ret.setValid(false);

            return ret;
        }

        synchronized (this) {
            Iterator<MeasuredCoord> iterator = this.iterator();

            double sumLatitude = 0;
            double sumLongitude = 0;

            do {
                MeasuredCoord tmp = iterator.next();
                sumLatitude += tmp.getLatitude();
                sumLongitude += tmp.getLongitude();
            } while (iterator.hasNext());

            ret = new CoordinateGPS(sumLatitude / this.size(), sumLongitude / this.size());
            ret.setValid(true);

        }

        return ret;
    }

    /**
     * Gibt die Durchschnittliche Koordinate dieser Liste zurück. Wobei die Genauigkeit der gemessenen Koordinaten berücksichtigt wird!
     *
     * @return Coordinate
     */
    public Coordinate getAccuWeightedAverageCoord() {
        // TODO berechne Coord nach Genauigkeits Wichtung
        return getMeasuredAverageCoord(); // Vorerst, bis die Wichtung fertig
        // ist!
    }

    /**
     * überschreibt die add Methode um bei einer Listen Größe > 3 <br>
     * die MeasuredCoord.Referenz auf den Durchschnitt der Liste zu setzen.
     */
    @Override
    public boolean add(MeasuredCoord measuredCoord) {
        boolean ret = false;

        synchronized (this) {
            ret = super.add(measuredCoord);

            if (this.size() > 3) {
                MeasuredCoord.Referenz = this.getMeasuredAverageCoord();
            }
        }
        return ret;
    }

    /**
     * Sortiert die Koordinaten nach Entfernung zu MeasuredCoord.Referenz welche im ersten Schritt auf den Durchschnitt gesetzt wird.
     */
    public void sort() {
        synchronized (this) {
            MeasuredCoord.Referenz = this.getMeasuredAverageCoord();
            Collections.sort(this);
        }
    }

    /**
     * Setzt die Statisch Referenz Koordinate von MeasuredCoord auf die errechnete durchnitliche Koordinate
     */
    public void setAverage() {
        MeasuredCoord.Referenz = this.getMeasuredAverageCoord();
    }

    /**
     * Löscht die Ausreißer Werte, welche eine Distanz von mehr als 3m zur Referenz Koordinate haben.
     */
    public void clearDiscordantValue() {
        boolean ready = false;
        synchronized (this) {
            do {
                ready = true;

                this.setAverage();
                Iterator<MeasuredCoord> iterator = this.iterator();
                do {
                    MeasuredCoord tmp = iterator.next();
                    if (tmp.Distance(CalculationType.ACCURATE) > 3) {
                        this.remove(tmp);
                        ready = false;
                        break;
                    }
                } while (iterator.hasNext());
            } while (!ready);
            this.setAverage();
        }
    }

    public String toString() {
        String ret = "";
        if (this.getAccuWeightedAverageCoord().isValid()) {
            ret = UnitFormatter.FormatLatitudeDM(this.getAccuWeightedAverageCoord().getLatitude()) + " / " + UnitFormatter.FormatLongitudeDM(this.getAccuWeightedAverageCoord().getLongitude());
        }

        return ret;
    }
}
