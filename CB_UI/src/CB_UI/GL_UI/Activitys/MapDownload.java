package CB_UI.GL_UI.Activitys;

import CB_Core.CB_Core_Settings;
import CB_Core.Import.BreakawayImportThread;
import CB_Locator.Map.ManagerBase;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.ImportAnimation.AnimationType;
import CB_UI.GL_UI.Controls.MapDownloadItem;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Controls.ProgressBar;
import CB_UI_Base.GL_UI.Controls.ScrollBox;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Events.ProgressChangedEvent;
import CB_Utils.Events.ProgresssChangedEventList;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Log;
import CB_Utils.Util.FileIO;
import CB_Utils.http.Webb;
import com.thebuzzmedia.sjxp.XMLParser;
import com.thebuzzmedia.sjxp.rule.DefaultRule;
import com.thebuzzmedia.sjxp.rule.IRule;
import com.thebuzzmedia.sjxp.rule.IRule.Type;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

public class MapDownload extends ActivityBase implements ProgressChangedEvent {
    private static final String log = "MapDownload";
    private static MapDownload INSTANCE;
    private final String URL_FREIZEITKARTE = "http://repository.freizeitkarte-osm.de/repository_freizeitkarte_android.xml";
    boolean DownloadIsCompleted = false;
    int AllProgress = 0;
    int MapCount = 0;
    int errors = 0;
    CB_List<MapRepositoryInfo> mapInfoList = new CB_List<MapDownload.MapRepositoryInfo>();
    CB_List<MapDownloadItem> mapInfoItemList = new CB_List<MapDownloadItem>();
    MapRepositoryInfo actMapRepositoryInfo;
    private Button bOK, bCancel;
    private Label lblTitle, lblProgressMsg;
    private ProgressBar pgBar;
    private Boolean importStarted = false;
    private ScrollBox scrollBox;
    private ImportAnimation dis;
    private String repository_freizeitkarte_android = "";
    private boolean canceld = false;
    private boolean isChkRepository = false;

    private MapDownload() {
        super(ActivityRec(), "mapDownloadActivity");
        scrollBox = new ScrollBox(ActivityRec());
        this.addChild(scrollBox);
        createOkCancelBtn();
        createTitleLine();
        scrollBox.setHeight(lblProgressMsg.getY() - bOK.getMaxY() - margin - margin);
        scrollBox.setY(bOK.getMaxY() + margin);
        scrollBox.setBackground(this.getBackground());
    }

    public static MapDownload getInstance() {
        // cause Activity gets disposed and a second run will produce an error
        if (INSTANCE == null || INSTANCE.isDisposed()) {
            INSTANCE = new MapDownload();
        }
        return INSTANCE;
    }

    @Override
    public void onShow() {
        ProgresssChangedEventList.Add(this);
        chkRepository();
    }

    @Override
    public void onHide() {
        ProgresssChangedEventList.Remove(this);
    }

    private void createOkCancelBtn() {
        bOK = new Button(leftBorder, leftBorder, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "OK Button");
        bCancel = new Button(bOK.getMaxX(), leftBorder, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "Cancel Button");

        // Translations
        bOK.setText(Translation.Get("import"));
        bCancel.setText(Translation.Get("cancel"));

        this.addChild(bOK);
        bOK.setOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                ImportNow();
                return true;
            }

        });

        this.addChild(bCancel);
        bCancel.setOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                if (BreakawayImportThread.isCanceled()) {
                    BreakawayImportThread.reset();
                    finish();
                    return true;
                }

                if (importStarted) {
                    GL_MsgBox.Show(Translation.Get("WantCancelImport"), Translation.Get("CancelImport"), MessageBoxButtons.YesNo, MessageBoxIcon.Stop, new OnMsgBoxClickListener() {

                        @Override
                        public boolean onClick(int which, Object data) {
                            if (which == GL_MsgBox.BUTTON_POSITIVE) {
                                finishImport();
                            }
                            return true;
                        }

                    });
                } else
                    finish();
                return true;
            }
        });

    }

    private void createTitleLine() {
        // Title+Progressbar

        float lineHeight = UI_Size_Base.that.getButtonHeight() * 0.75f;

        lblTitle = new Label(this.name + " lblTitle", leftBorder + margin, this.getHeight() - this.getTopHeight() - lineHeight - margin, innerWidth - margin, lineHeight);
        lblTitle.setFont(Fonts.getBig());
        float lblWidth = lblTitle.setText(Translation.Get("import")).getTextWidth();
        this.addChild(lblTitle);

        CB_RectF rec = new CB_RectF(lblTitle.getX() + lblWidth + margin, lblTitle.getY(), innerWidth - margin - margin - lblWidth, lineHeight);

        pgBar = new ProgressBar(rec, "ProgressBar");

        pgBar.setProgress(0, "");

        float SmallLineHeight = Fonts.MeasureSmall("Tg").height;

        lblProgressMsg = new Label(this.name + " lblProgressMsg", leftBorder + margin, lblTitle.getY() - margin - SmallLineHeight, innerWidth - margin - margin, SmallLineHeight);

        lblProgressMsg.setFont(Fonts.getSmall());

        this.addChild(pgBar);
        this.addChild(lblProgressMsg);

    }

    @Override
    public void ProgressChangedEventCalled(final String Message, final String ProgressMessage, final int Progress) {

        GL.that.RunOnGL(new IRunOnGL() {

            @Override
            public void run() {
                pgBar.setProgress(Progress);
                lblProgressMsg.setText(ProgressMessage);
                if (!Message.equals(""))
                    pgBar.setText(Message);
            }
        });

    }

    private void ImportNow() {
        if (importStarted)
            return;

        DownloadIsCompleted = false;

        // disable btn
        bOK.disable();
        bCancel.setText(Translation.Get("cancel"));

        // disable UI
        dis = new ImportAnimation(scrollBox);
        dis.setBackground(getBackground());
        dis.setAnimationType(AnimationType.Download);
        this.addChild(dis, false);

        canceld = false;
        importStarted = true;
        for (int i = 0, n = mapInfoItemList.size(); i < n; i++) {
            MapDownloadItem item = mapInfoItemList.get(i);
            item.beginDownload();
        }

        Thread dlProgressChecker = new Thread(new Runnable() {

            @Override
            public void run() {

                while (!DownloadIsCompleted) {
                    if (canceld) {
                        for (int i = 0, n = mapInfoItemList.size(); i < n; i++) {
                            MapDownloadItem item = mapInfoItemList.get(i);
                            item.cancelDownload();
                        }
                    }

                    int calcAll = 0;
                    int downloadCount = 0;
                    for (int i = 0, n = mapInfoItemList.size(); i < n; i++) {
                        MapDownloadItem item = mapInfoItemList.get(i);
                        int actPro = item.getDownloadProgress();
                        if (actPro > -1) {
                            calcAll += actPro;
                            downloadCount++;
                        }

                    }
                    int newAllProgress = downloadCount != 0 ? calcAll / downloadCount : 0;

                    if (AllProgress != newAllProgress) {
                        AllProgress = newAllProgress;
                        pgBar.setProgress(AllProgress);
                        lblProgressMsg.setText(AllProgress + " %");
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {

                        e.printStackTrace();
                    }

                    // chk download ready
                    boolean chk = true;
                    for (int i = 0, n = mapInfoItemList.size(); i < n; i++) {
                        MapDownloadItem item = mapInfoItemList.get(i);
                        if (!item.isFinished()) {
                            chk = false;
                            break;
                        }
                    }

                    if (chk) {
                        // all downloads ready
                        DownloadIsCompleted = true;
                        finishImport();
                    }

                }

            }
        });

        dlProgressChecker.start();
    }

    private void finishImport() {
        canceld = true;
        importStarted = false;
        fillDownloadList();
        if (dis != null) {
            this.removeChildsDirekt(dis);
            dis.dispose();
            dis = null;
        }
        pgBar.setProgress(0);

        if (DownloadIsCompleted) {
            lblProgressMsg.setText("");
            bCancel.setText(Translation.Get("ok"));
            // to prevent download again. On next start you must check again
            for (int i = 0, n = mapInfoItemList.size(); i < n; i++) {
                MapDownloadItem item = mapInfoItemList.get(i);
                item.enable();
            }
        } else
            lblProgressMsg.setText(Translation.Get("DownloadCanceld"));

        bOK.enable();
        if (ManagerBase.Manager != null)
            ManagerBase.Manager.initMapPacks();
    }

    private void chkRepository() {
        if (repository_freizeitkarte_android.length() == 0) {
            // Download and Parse
            // disable UI
            dis = new ImportAnimation(scrollBox);
            dis.setBackground(getBackground());
            dis.setAnimationType(AnimationType.Download);
            lblProgressMsg.setText(Translation.Get("ChkAvailableMaps"));
            this.addChild(dis, false);
            bOK.disable();

            if (!isChkRepository)
                readRepository();

        }
    }

    private void readRepository() {
        isChkRepository = true;
        Thread tread = new Thread(new Runnable() {

            @Override
            public void run() {
                // Read XML
                repository_freizeitkarte_android = Webb.create()
                        .get(URL_FREIZEITKARTE)
                        .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                        .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                        .ensureSuccess()
                        .asString()
                        .getBody();

                fillDownloadList();

                if (dis != null) {
                    MapDownload.this.removeChildsDirekt(dis);
                    dis.dispose();
                    dis = null;
                }
                bOK.enable();
                isChkRepository = false;
                lblProgressMsg.setText("");

                if (ManagerBase.Manager != null)
                    ManagerBase.Manager.initMapPacks();
            }

        });
        tread.start();

    }

    private void fillDownloadList() {
        scrollBox.removeChilds();

        Map<String, String> values = new HashMap<String, String>();
        System.setProperty("sjxp.namespaces", "false");
        List<IRule<Map<String, String>>> ruleList = new ArrayList<IRule<Map<String, String>>>();
        ruleList = createRepositoryRules(ruleList);

        @SuppressWarnings("unchecked")
        XMLParser<Map<String, String>> parserCache = new XMLParser<Map<String, String>>(ruleList.toArray(new IRule[0]));

        InputStream stream = new ByteArrayInputStream(repository_freizeitkarte_android.getBytes());
        parserCache.parse(stream, values);

        float yPos = 0;

        // get and check the target directory (global value)
        String workPath = Config.MapPackFolder.getValue();
        Boolean isWritable;
        if (workPath.length() > 0)
            isWritable = FileIO.canWrite(workPath);
        else
            isWritable = false;
        if (isWritable)
            Log.info(log, "Download to " + workPath);
        else {
            Log.err(log, "Download to " + workPath + " is not possible!");
            // don't use Config.MapPackFolder.getDefaultValue()
            // because it doesn't reflect own repository
            // own or global repository is writable by default, but do check again
            workPath = Config.MapPackFolderLocal.getValue();
            isWritable = FileIO.canWrite(workPath);
            Log.info(log, "Download to " + workPath + " is possible? " + isWritable);
        }

        // Create possible download List
        for (int i = 0, n = mapInfoList.size(); i < n; i++) {
            MapRepositoryInfo map = mapInfoList.get(i);

            MapDownloadItem item = new MapDownloadItem(map, workPath, MapDownload.this.innerWidth);
            item.setY(yPos);
            scrollBox.addChild(item);
            mapInfoItemList.add(item);
            yPos += item.getHeight() + margin;
        }

        scrollBox.setVirtualHeight(yPos);
    }

    private List<IRule<Map<String, String>>> createRepositoryRules(List<IRule<Map<String, String>>> ruleList) {
        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/Name") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                actMapRepositoryInfo.Name = text;
            }
        });

        String locale = Locale.getDefault().getLanguage();
        if (locale.contains("de")) {
            ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/DescriptionGerman") {
                @Override
                public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                    actMapRepositoryInfo.Description = text;
                }
            });
        } else {
            ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/DescriptionEnglish") {
                @Override
                public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                    actMapRepositoryInfo.Description = text;
                }
            });
        }

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/Url") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                actMapRepositoryInfo.Url = text;
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/Size") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                actMapRepositoryInfo.Size = Integer.parseInt(text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/Checksum") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                actMapRepositoryInfo.MD5 = text;
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/Freizeitkarte/Map") {
            @Override
            public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values) {

                if (isStartTag) {
                    actMapRepositoryInfo = new MapRepositoryInfo();
                } else {
                    mapInfoList.add(actMapRepositoryInfo);
                }

            }
        });
        return ruleList;
    }

    public static class MapRepositoryInfo {
        public String Name;
        public String Description;
        public String Url;
        public int Size;
        public String MD5;
    }

}
