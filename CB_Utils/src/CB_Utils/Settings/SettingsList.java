package CB_Utils.Settings;

import CB_Utils.Log.Log;
import de.cb.sqlite.Database_Core;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SettingsList extends ArrayList<SettingBase<?>> {
    private static final String log = "SettingsList";
    private static final long serialVersionUID = -969846843815877942L;
    private static SettingsList that;
    private boolean isLoaded = false;

    public SettingsList() {
        that = this;

        // add Member to list
        Member[] mbrs = this.getClass().getFields();

        for (Member mbr : mbrs) {
            if (mbr instanceof Field) {
                try {
                    Object obj = ((Field) mbr).get(this);
                    if (obj instanceof SettingBase<?>) {
                        add((SettingBase<?>) obj);
                    }
                } catch (IllegalArgumentException e) {

                    e.printStackTrace();
                } catch (IllegalAccessException e) {

                    e.printStackTrace();
                }
            }

        }

        mbrs = null;
    }

    public static SettingBase<?> addSetting(SettingBase<?> setting) {

        if (that == null)
            try {
                throw new InstantiationException("Settings List not initial");
            } catch (InstantiationException e) {
                e.printStackTrace();
                return null;
            }
        that.add(setting);

        return setting;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    @Override
    public boolean add(SettingBase<?> setting) {
        if (!that.contains(setting)) {
            return super.add(setting);
        }
        return false;
    }

    protected abstract Database_Core getSettingsDB();

    protected abstract Database_Core getDataDB();

    protected SettingsDAO createSettingsDAO() {
        return new SettingsDAO();
    }

    /**
     * Return true, if setting changes need restart
     *
     * @return
     */
    public boolean WriteToDB() {
        // Write into DB
        SettingsDAO dao = createSettingsDAO();
        getSettingsDB().beginTransaction();
        Database_Core Data = getDataDB();

        try {
            if (Data != null)
                Data.beginTransaction();
        } catch (Exception ex) {
            // do not change Data now!
            Data = null;
        }

        boolean needRestart = false;

        try {
            for (Iterator<SettingBase<?>> it = this.iterator(); it.hasNext(); ) {
                SettingBase<?> setting = it.next();
                if (!setting.isDirty())
                    continue; // is not changed -> do not

                if (SettingStoreType.Local == setting.getStoreType()) {
                    if (Data != null)
                        dao.WriteToDatabase(Data, setting);
                } else if (SettingStoreType.Global == setting.getStoreType() || (!PlatformSettings.canUsePlatformSettings() && SettingStoreType.Platform == setting.getStoreType())) {
                    dao.WriteToDatabase(getSettingsDB(), setting);
                } else if (SettingStoreType.Platform == setting.getStoreType()) {
                    dao.WriteToPlatformSettings(setting);
                    dao.WriteToDatabase(getSettingsDB(), setting);
                }

                if (setting.needRestart) {
                    needRestart = true;
                }

                setting.clearDirty();

            }
            if (Data != null)
                Data.setTransactionSuccessful();
            getSettingsDB().setTransactionSuccessful();

            return needRestart;
        } finally {
            getSettingsDB().endTransaction();
            if (Data != null)
                Data.endTransaction();
        }

    }

    public void ReadFromDB() {
        // Read from DB
        // Log.debug will not work, cause setting of loglevel is from settings (that and not yet read and set)
        try {
            Log.debug(log, "Load settings from: " + getSettingsDB().getDatabasePath());
            Log.debug(log, "and local settings: " + getDataDB().getDatabasePath());
        } catch (Exception e) {
            // gibt beim splash - Start: NPE in Translation.readMissingStringsFile
            // Nachfolgende Starts sollten aber protokolliert werden
        }

        AtomicInteger tryCount = new AtomicInteger(0);

        while (tryCount.incrementAndGet() < 10) {


            SettingsDAO dao = new SettingsDAO();
            try {
                for (Iterator<SettingBase<?>> it = this.iterator(); it.hasNext(); ) {
                    SettingBase<?> setting = it.next();
                    String debugString;

                    boolean isPlatform = false;
                    boolean isPlattformoverride = false;

                    if (SettingStoreType.Local == setting.getStoreType()) {
                        if (getDataDB() == null || getDataDB().getDatabasePath() == null)
                            setting.loadDefault();
                        else
                            setting = dao.ReadFromDatabase(getDataDB(), setting);
                    } else if (SettingStoreType.Global == setting.getStoreType() || (!PlatformSettings.canUsePlatformSettings() && SettingStoreType.Platform == setting.getStoreType())) {
                        setting = dao.ReadFromDatabase(getSettingsDB(), setting);
                    } else if (SettingStoreType.Platform == setting.getStoreType()) {
                        isPlatform = true;
                        SettingBase<?> cpy = setting.copy();
                        cpy = dao.ReadFromDatabase(getSettingsDB(), cpy);
                        setting = dao.ReadFromPlatformSetting(setting);

                        // chk for Value on User.db3 and cleared Platform Value

                        if (setting instanceof SettingString) {
                            SettingString st = (SettingString) setting;

                            if (st.value.length() == 0) {
                                // Platform Settings are empty use db3 value or default
                                setting = dao.ReadFromDatabase(getSettingsDB(), setting);
                                dao.WriteToPlatformSettings(setting);
                            }
                        } else if (!cpy.value.equals(setting.value)) {
                            if (setting.value.equals(setting.defaultValue)) {
                                // override Platformsettings with UserDBSettings
                                setting.setValueFrom(cpy);
                                dao.WriteToPlatformSettings(setting);
                                setting.clearDirty();
                                isPlattformoverride = true;
                            } else {
                                // override UserDBSettings with Platformsettings
                                cpy.setValueFrom(setting);
                                dao.WriteToDatabase(getSettingsDB(), cpy);
                                cpy.clearDirty();
                            }
                        }
                    }

                    if (setting instanceof SettingEncryptedString) {// Don't write encrypted settings in to a log file
                        debugString = "*******";
                    } else {
                        debugString = setting.value.toString();
                    }

                    if (isPlatform) {
                        if (isPlattformoverride) {
                            Log.trace(log, "Override Platform setting [" + setting.name + "] from DB to: " + debugString);
                        } else {
                            Log.trace(log, "Override PlatformDB setting [" + setting.name + "] from Platform to: " + debugString);
                        }
                    } else {
                        if (!setting.value.equals(setting.defaultValue)) {
                            Log.trace(log, "Change " + setting.getStoreType() + " setting [" + setting.name + "] to: " + debugString);
                        } else {
                            Log.trace(log, "Default " + setting.getStoreType() + " setting [" + setting.name + "] to: " + debugString);
                        }
                    }
                }
                tryCount.set(100);
            } catch (Exception e) {
                Log.err(log, "Error read settings, try again");
            }

        }
        Log.debug(log, "Settings are loaded");
        isLoaded = true;
    }

    public void LoadFromLastValue() {
        for (Iterator<SettingBase<?>> it = this.iterator(); it.hasNext(); ) {
            SettingBase<?> setting = it.next();
            setting.loadFromLastValue();
        }
    }

    public void SaveToLastValue() {
        for (Iterator<SettingBase<?>> it = this.iterator(); it.hasNext(); ) {
            SettingBase<?> setting = it.next();
            setting.saveToLastValue();
        }
    }

    public void LoadAllDefaultValues() {
        for (Iterator<SettingBase<?>> it = this.iterator(); it.hasNext(); ) {
            SettingBase<?> setting = it.next();
            setting.loadDefault();
        }
    }
}
