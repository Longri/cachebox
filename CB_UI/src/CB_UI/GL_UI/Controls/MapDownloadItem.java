package CB_UI.GL_UI.Controls;

import CB_Core.Import.UnZip;
import CB_UI.GL_UI.Activitys.MapDownload.MapRepositoryInfo;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.ChkBox;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.Label.HAlignment;
import CB_UI_Base.GL_UI.Controls.ProgressBar;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Log.Log;
import CB_Utils.Util.CopyHelper.Copy;
import CB_Utils.Util.FileIO;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import CB_Utils.http.Download;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class MapDownloadItem extends CB_View_Base {
    private static final String log = "MapDownloadItem";
    private final MapRepositoryInfo mapInfo;
    private final ChkBox checkBoxMap;
    private final float margin;
    private final Label lblName, lblSize;
    private final String workPath;
    private final AtomicBoolean DownloadRuns = new AtomicBoolean(false);
    private int lastProgress = 0;
    private ProgressBar pgBar;
    private boolean canceld = false;

    public MapDownloadItem(MapRepositoryInfo mapInfo, String workPath, float ItemWidth) {
        super(mapInfo.Name);
        this.mapInfo = mapInfo;
        this.workPath = workPath;
        margin = UI_Size_Base.that.getMargin();

        checkBoxMap = new ChkBox("Image");
        this.setHeight(checkBoxMap.getHeight() + (margin * 2));
        this.setWidth(ItemWidth);
        checkBoxMap.setX(margin);

        checkBoxMap.setY(margin);

        lblName = new Label(this.name + " lblName", checkBoxMap.getMaxX() + margin, checkBoxMap.getY(), innerWidth - margin * 3 - checkBoxMap.getWidth(), checkBoxMap.getHeight());
        lblName.setFont(Fonts.getNormal());

        // Cut "Freizeitkarte"
        String Name = mapInfo.Description.replace("Freizeitkarte ", "");
        lblName.setText(Name);

        lblSize = new Label(this.name + " lblSize", checkBoxMap.getMaxX() + margin, checkBoxMap.getY(), innerWidth - margin * 3 - checkBoxMap.getWidth(), checkBoxMap.getHeight());
        lblSize.setFont(Fonts.getNormal());

        // Format Size
        int s = mapInfo.Size / 1024 / 1024;
        lblSize.setHAlignment(HAlignment.RIGHT);
        lblSize.setText(s + " MB");

        this.addChild(checkBoxMap);
        this.addChild(lblName);
        this.addChild(lblSize);

        chkExists();
    }

    public static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        try {
                            files[i].delete();
                        } catch (IOException e) {
                            Log.err(log, e.getLocalizedMessage());
                        }
                    }
                }
            }
        }
        try {
            return (directory.delete());
        } catch (IOException e) {
            return false;
        }
    }

    private void chkExists() {
        int slashPos = mapInfo.Url.lastIndexOf("/");
        String zipFile = mapInfo.Url.substring(slashPos, mapInfo.Url.length());

        String FileString = FileIO.GetFileNameWithoutExtension(zipFile);

        File file = FileFactory.createFile(workPath + "/" + FileString);
        if (file.exists()) {
            checkBoxMap.setChecked(true);
            checkBoxMap.disable();
            checkBoxMap.setOnClickListener(new OnClickListener() {

                @Override
                public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                    if (checkBoxMap.isDisabled()) {
                        checkBoxMap.enable();
                    } else {
                        checkBoxMap.setChecked(true);
                        checkBoxMap.disable();
                    }

                    return true;
                }
            });
        }
    }

    public void beginDownload() {
        canceld = false;

        if (!checkBoxMap.isChecked() || checkBoxMap.isDisabled()) {
            lastProgress = -1;
            return;
        }

        DownloadRuns.set(true);
        float ProgressHeight = (Sprites.ProgressBack.getBottomHeight() + Sprites.ProgressBack.getTopHeight());
        CB_RectF rec = new CB_RectF(checkBoxMap.getMaxX() + margin, 0, innerWidth - margin * 3 - checkBoxMap.getWidth(), ProgressHeight);

        if (pgBar == null) {
            pgBar = new ProgressBar(rec, "");
            this.addChild(pgBar);
            lblName.setY(pgBar.getHalfHeight() - margin);
            lblSize.setY(pgBar.getHalfHeight() - margin);
        }

        lastProgress = 0;

        Thread runThread = new Thread(new Runnable() {

            @Override
            public void run() {
                int slashPos = mapInfo.Url.lastIndexOf("/");
                String zipFile = mapInfo.Url.substring(slashPos + 1, mapInfo.Url.length());
                String target = workPath + "/" + zipFile;

                pgBar.setProgress(lastProgress, lastProgress + " %");

                if (Download.Download(mapInfo.Url, target)) {
                    try {
                        UnZip.extractFolder(target);
                    } catch (Exception e) {
                        Log.err(log, e.getLocalizedMessage());
                    }

                    // Copy and Clear ? todo check is this necessary and ok?
                    File folder = FileFactory.createFile(workPath + "/" + FileIO.GetFileNameWithoutExtension(zipFile));
                    File newfolder = FileFactory.createFile(workPath + "/" + FileIO.GetFileNameWithoutExtension(folder.getName()));

                    if (folder.isDirectory()) {
                        folder.renameTo(newfolder);

                        try {
                            Copy.copyFolder(newfolder, FileFactory.createFile(workPath));
                        } catch (IOException e) {
                            Log.err(log, e.getLocalizedMessage());
                        }

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Log.err(log, e.getLocalizedMessage());
                        }

                        deleteDirectory(newfolder);
                    }

                }

                try {
                    FileFactory.createFile(target).delete();
                    Log.info(log, "Deleted " + target);
                } catch (IOException e) {
                    Log.err(log, e.getLocalizedMessage());
                }

                lastProgress = canceld ? 0 : 100;
                pgBar.setProgress(lastProgress, lastProgress + " %");
                DownloadRuns.set(false);
                Log.info(log, "Download everything ready");
            }
        });

        runThread.start();

    }

    public void cancelDownload() {
        canceld = true;
    }

    public int getDownloadProgress() {
        return lastProgress;
    }

    public boolean isFinished() {
            if (DownloadRuns.get())
                return false;
            else
                return true;
    }

    public void enable() {
        if (checkBoxMap.isChecked())
            checkBoxMap.disable();
        else
            checkBoxMap.enable();
    }
}
