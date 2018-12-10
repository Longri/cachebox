package CB_UI;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.Types.CacheDAO;
import CB_Core.DAO.ImageDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DAO.WaypointDAO;
import CB_Core.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;

import static CB_Core.Api.GroundspeakAPI.GeoCacheRelated;


import java.util.ArrayList;

public class WriteIntoDB {
    static CacheDAO cacheDAO = new CacheDAO();
    static LogDAO logDAO = new LogDAO();
    static ImageDAO imageDAO = new ImageDAO();
    static WaypointDAO waypointDAO = new WaypointDAO();

    public static void CachesAndLogsAndImagesIntoDB(ArrayList<GeoCacheRelated> geoCacheRelateds) throws InterruptedException {

        if (cacheDAO == null) {
            cacheDAO = new CacheDAO();
            logDAO = new LogDAO();
            imageDAO = new ImageDAO();
            waypointDAO = new WaypointDAO();
        }

        Database.Data.beginTransaction();

        for (GeoCacheRelated geoCacheRelated: geoCacheRelateds) {

            // Auf eventuellen Thread Abbruch reagieren
            Thread.sleep(2);

            Cache cache = geoCacheRelated.cache;
            Cache aktCache = Database.Data.Query.GetCacheById(cache.Id);

            if (aktCache != null && aktCache.isLive()) aktCache = null;

            if (aktCache == null) aktCache = cacheDAO.getFromDbByCacheId(cache.Id);

            // Read Detail Info of Cache if not available
            if ((aktCache != null) && (aktCache.detail == null)) {
                aktCache.loadDetail();
            }
            // If Cache into DB, extract saved rating
            if (aktCache != null) {
                cache.Rating = aktCache.Rating;
            }

            // Falls das Update nicht klappt (Cache noch nicht in der DB) Insert machen
            if (!cacheDAO.UpdateDatabase(cache)) {
                cacheDAO.WriteToDatabase(cache);
            }

            // Notes von Groundspeak überprüfen und evtl. in die DB an die vorhandenen Notes anhängen
            // todo solver extrahieren
            if (cache.getTmpNote() != null && cache.getTmpNote().length() > 0) {

                String oldNote = Database.GetNote(cache);
                if (oldNote != null) {
                    oldNote = oldNote.trim();
                }
                else {
                    oldNote = "";
                }
                String begin = "<Import from Geocaching.com>";
                if (!oldNote.startsWith(begin)) {
                    begin=System.getProperty("line.separator") + begin;
                }
                String end = "</Import from Geocaching.com>";
                int iBegin = oldNote.indexOf(begin);
                int iEnd = oldNote.indexOf(end);
                String newNote;
                if ((iBegin >= 0) && (iEnd > iBegin)) {
                    // Note from Groundspeak already in Database
                    // -> Replace only this part in whole Note
                    // Copy the old part of Note before the beginning of the groundspeak block
                    newNote = oldNote.substring(0, iBegin);
                    newNote += begin + System.getProperty("line.separator");
                    newNote += cache.getTmpNote();
                    newNote += System.getProperty("line.separator") + end;
                    newNote += oldNote.substring(iEnd + end.length(), oldNote.length());
                } else {
                    newNote = oldNote + System.getProperty("line.separator");
                    newNote += begin + System.getProperty("line.separator");
                    newNote += cache.getTmpNote();
                    newNote += System.getProperty("line.separator") + end;
                }
                cache.setTmpNote(newNote);
                Database.SetNote(cache, cache.getTmpNote());

            }

            // Delete LongDescription from this Cache! LongDescription is Loading by showing DescriptionView direct from DB
            cache.setLongDescription("");

            logDAO.deleteLogs(cache.Id); // the LogIDs will be changed with API 1.0
            for (LogEntry log : geoCacheRelated.logs) {
                logDAO.WriteToDatabase(log);
            }

            imageDAO.deleteImagesForCache(cache.getGcCode());
            for (ImageEntry image : geoCacheRelated.images) {
                imageDAO.WriteToDatabase(image, false);
            }

            for (int i = 0, n = cache.waypoints.size(); i < n; i++) {
                // must Cast to Full Waypoint. If Waypoint, is wrong createt!
                Waypoint waypoint = cache.waypoints.get(i);
                boolean update = true;

                // dont refresh wp if aktCache.wp is user changed
                if (aktCache != null) {
                    if (aktCache.waypoints != null) {
                        for (int j = 0, m = aktCache.waypoints.size(); j < m; j++) {
                            Waypoint wp = aktCache.waypoints.get(j);
                            if (wp.getGcCode().equalsIgnoreCase(waypoint.getGcCode())) {
                                if (wp.IsUserWaypoint)
                                    update = false;
                                break;
                            }
                        }
                    }
                }

                if (update) {
                    // do not store replication information when importing caches with GC api
                    if (!waypointDAO.UpdateDatabase(waypoint, false)) {
                        waypointDAO.WriteToDatabase(waypoint, false); // do not store replication information here
                    }
                }

            }

            if (aktCache == null) {
                Database.Data.Query.add(cache);
                // cacheDAO.WriteToDatabase(cache);
            } else {
                // 2012-11-17: do not remove old instance from Query because of problems with cacheList and MapView
                // Database.Data.Query.remove(Database.Data.Query.GetCacheById(cache.Id));
                // Database.Data.Query.add(cache);
                aktCache.copyFrom(cache);
                // cacheDAO.UpdateDatabase(cache);
            }

        }

        Database.Data.setTransactionSuccessful();
        Database.Data.endTransaction();

        Database.Data.GPXFilenameUpdateCacheCount();

    }

}
