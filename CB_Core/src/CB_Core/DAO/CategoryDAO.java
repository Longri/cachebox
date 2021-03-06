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

import CB_Core.CoreSettingsForward;
import CB_Core.Database;
import CB_Core.Types.Categories;
import CB_Core.Types.Category;
import CB_Core.Types.GpxFilename;
import CB_Utils.Log.Log;
import de.cb.sqlite.CoreCursor;
import de.cb.sqlite.Database_Core.Parameters;

public class CategoryDAO {
    private static final String log = "CategoryDAO";

    public Category ReadFromCursor(CoreCursor reader) {
        Category result = new Category();

        result.Id = reader.getLong(0);
        result.GpxFilename = reader.getString(1);
        result.pinned = reader.getInt(2) != 0;

        // alle GpxFilenames einlesen
        CoreCursor reader2 = Database.Data.rawQuery("select ID, GPXFilename, Imported, CacheCount from GpxFilenames where CategoryId=?", new String[]{String.valueOf(result.Id)});
        reader2.moveToFirst();
        while (!reader2.isAfterLast()) {
            GpxFilenameDAO gpxFilenameDAO = new GpxFilenameDAO();
            GpxFilename gpx = gpxFilenameDAO.ReadFromCursor(reader2);
            result.add(gpx);
            reader2.moveToNext();
        }
        reader2.close();

        return result;
    }

    public void SetPinned(Category category, boolean pinned) {
        if (category.pinned == pinned)
            return;
        category.pinned = pinned;

        Parameters args = new Parameters();
        args.put("pinned", pinned);
        try {
            Database.Data.update("Category", args, "Id=" + String.valueOf(category.Id), null);
        } catch (Exception exc) {
            Log.err(log, "SetPinned", "CategoryDAO", exc);
        }
    }

    // Categories
    public void LoadCategoriesFromDatabase() {
        // read all Categories

        CoreSettingsForward.Categories.beginnTransaction();
        CoreSettingsForward.Categories.clear();

        CoreCursor reader = Database.Data.rawQuery("select ID, GPXFilename, Pinned from Category", null);
        if (reader != null) {
            reader.moveToFirst();
            while (!reader.isAfterLast()) {
                Category category = ReadFromCursor(reader);
                CoreSettingsForward.Categories.add(category);
                reader.moveToNext();
            }
            reader.close();
        }
        CoreSettingsForward.Categories.sort();
        CoreSettingsForward.Categories.endTransaction();
    }

    public void DeleteEmptyCategories() {
        CoreSettingsForward.Categories.beginnTransaction();

        Categories delete = new Categories();
        for (int i = 0, n = CoreSettingsForward.Categories.size(); i < n; i++) {
            Category cat = CoreSettingsForward.Categories.get(i);
            if (cat.CacheCount() == 0) {
                Database.Data.delete("Category", "Id=?", new String[]{String.valueOf(cat.Id)});
                delete.add(cat);
            }
        }

        for (int i = 0, n = delete.size(); i < n; i++) {
            CoreSettingsForward.Categories.remove(delete.get(i));
        }
        CoreSettingsForward.Categories.endTransaction();
    }
}
