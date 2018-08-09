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
package CB_Core.DAO;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.slf4j.LoggerFactory;

import CB_Core.Database;
import CB_Core.Types.Trackable;
import CB_Utils.Log.Log;
import de.cb.sqlite.CoreCursor;
import de.cb.sqlite.Database_Core.Parameters;

public class TrackableDAO {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(TrackableDAO.class);

    private Trackable ReadFromCursor(CoreCursor reader) {
        try {
            Trackable trackable = new Trackable(reader);

            return trackable;
        } catch (Exception exc) {
            Log.err(log, "Read Trackable", "", exc);
            return null;
        }
    }

    public void WriteToDatabase(Trackable trackable) {
        try {
            Log.info(log, "Write Trackable createArgs");
            Parameters args = createArgs(trackable);
            Log.info(log, "Write Trackable insert");
            Database.FieldNotes.insert("Trackable", args);
        } catch (Exception exc) {
            Log.err(log, "Write Trackable", exc);
        }
    }

    public void UpdateDatabase(Trackable trackable) {
        try {
            Log.info(log, "Write Trackable createArgs");
            Parameters args = createArgs(trackable);
            Log.info(log, "Write Trackable update");
            Database.FieldNotes.update("Trackable", args, "GcCode='" + trackable.getGcCode() + "'", null);
        } catch (Exception exc) {
            Log.err(log, "Ubdate Trackable", exc);
        }

    }

    private Parameters createArgs(Trackable trackable) {
        String stimestampCreated = "";
        String stimestampLastVisit = "";

        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            stimestampCreated = iso8601Format.format(trackable.getDateCreated());
        } catch (Exception e) {
            Log.err(log, "stimestampCreated", e);
        }

        try {
            String lastVisit = trackable.getLastVisit();
            if (lastVisit.length() > 0)
                stimestampLastVisit = iso8601Format.format(lastVisit);
            else
                stimestampLastVisit = "";
        } catch (Exception e) {
            Log.err(log, "stimestampLastVisit", e);
        }

        Log.info(log,"new Parameters()");
        Parameters args = new Parameters();
        try {
            args.put("Archived",trackable.getArchived() ? 1 : 0);
            putArgs(args,"GcCode", trackable.getGcCode());
            putArgs(args,"CurrentGoal", trackable.getCurrentGoal());
            putArgs(args,"CurrentOwnerName", trackable.getCurrentOwner());
            putArgs(args,"DateCreated", stimestampCreated);
            putArgs(args,"Description", trackable.getDescription());
            putArgs(args,"IconUrl", trackable.getIconUrl());
            putArgs(args,"ImageUrl", trackable.getImageUrl());
            putArgs(args,"name", trackable.getName());
            putArgs(args,"OwnerName", trackable.getOwner());
            putArgs(args,"Url", trackable.getUrl());
            putArgs(args,"TypeName", trackable.getTypeName());
            putArgs(args,"LastVisit", stimestampLastVisit);
            putArgs(args,"Home", trackable.getHome());
            putArgs(args,"TravelDistance", trackable.getTravelDistance());
            putArgs(args,"CacheID", trackable.getCurrentGeocacheCode());
        } catch (Exception e) {
            Log.err(log, "args", e);
        }
        return args;
    }

    private void putArgs(Parameters args, String Name, Object value) {
        Log.info(log,Name + "=" + value);
        args.put(Name,value);
    }

    public Trackable getFromDbByGcCode(String GcCode) {
        String where = "GcCode = \"" + GcCode + "\"";
        String query = "select Id ,Archived ,GcCode ,CacheId ,CurrentGoal ,CurrentOwnerName ,DateCreated ,Description ,IconUrl ,ImageUrl ,Name ,OwnerName ,Url,TypeName, Home,TravelDistance   from Trackable WHERE " + where;
        CoreCursor reader = Database.FieldNotes.rawQuery(query, null);

        try {
            if (reader != null && reader.getCount() > 0) {
                reader.moveToFirst();
                Trackable ret = ReadFromCursor(reader);

                reader.close();
                return ret;
            } else {
                if (reader != null)
                    reader.close();
                return null;
            }
        } catch (Exception e) {
            if (reader != null)
                reader.close();
            e.printStackTrace();
            return null;
        }

    }

}
