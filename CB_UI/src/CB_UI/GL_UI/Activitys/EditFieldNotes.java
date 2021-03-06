/*
 * Copyright (C) 2015 team-cachebox.de
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
package CB_UI.GL_UI.Activitys;

import CB_Core.CB_Core_Settings;
import CB_Core.Types.FieldNoteEntry;
import CB_Core.Types.FieldNoteList;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.FilterSettings.FilterSetListView;
import CB_UI.GL_UI.Activitys.FilterSettings.FilterSetListView.FilterSetEntry;
import CB_UI.GL_UI.Activitys.FilterSettings.FilterSetListViewItem;
import CB_UI.GL_UI.Views.FieldNoteViewItem;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.Events.KeyboardFocusChangedEvent;
import CB_UI_Base.Events.KeyboardFocusChangedEventList;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.*;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import com.badlogic.gdx.math.Vector2;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EditFieldNotes extends ActivityBase implements KeyboardFocusChangedEvent {
    private final ArrayList<EditTextField> allTextFields = new ArrayList<EditTextField>();
    FilterSetListViewItem GcVote;
    Label title;
    private FieldNoteEntry altfieldNote;
    private FieldNoteEntry fieldNote;
    private Button btnOK = null;
    private Button btnCancel = null;
    private EditTextField etComment = null;
    private Image ivTyp = null;
    private Label tvFounds = null;
    private EditTextField tvDate = null;
    private EditTextField tvTime = null;
    private Label lblDate = null;
    private Label lblTime = null;
    private ScrollBox scrollBox = null;
    private Box scrollBoxContent;
    private boolean isNewFieldNote;
    private RadioButton rbDirectLog;
    private RadioButton rbOnlyFieldNote;
    private IReturnListener mReturnListener;

    public EditFieldNotes(FieldNoteEntry note, IReturnListener listener, boolean isNewFieldNote) {
        super(ActivityBase.ActivityRec(), "");
        this.isNewFieldNote = isNewFieldNote;
        mReturnListener = listener;
        fieldNote = note;
        altfieldNote = note.copy();
        initLayoutWithValues();
        iniTextfieldFocus();
    }

    public GL_View_Base touchDown(int x, int y, int pointer, int button) {
        if (GcVote != null && GcVote.getWorldRec().contains(x, y)) {
            GcVote.onTouchDown(x, y, pointer, button);
            GcVote.lastItemTouchPos = new Vector2(x - GcVote.getWorldRec().getX(), y - GcVote.getWorldRec().getY());
            return GcVote;
        } else {
            return super.touchDown(x, y, pointer, button);
        }
    }

    private void initLayoutWithValues() {
        initRow(BOTTOMUP);
        btnOK = new Button(Translation.Get("ok"));
        btnCancel = new Button(Translation.Get("cancel"));
        addNext(btnOK);
        addLast(btnCancel);
        scrollBox = new ScrollBox(innerWidth, getAvailableHeight());

        scrollBoxContent = new Box(scrollBox.getWidth(), 0);
        scrollBoxContent.initRow(BOTTOMUP);
        if (fieldNote.type.isDirectLogType())
            iniOptions();
        initLogText();
        iniGC_VoteItem();
        iniTime();
        iniDate();
        iniFoundLine();
        iniTitle();
        scrollBoxContent.adjustHeight();

        scrollBox.setVirtualHeight(scrollBoxContent.getHeight());
        scrollBox.addChild(scrollBoxContent);
        addLast(scrollBox);
        setOkAndCancelClickHandlers();
    }

    private void setValuesToLayout() {
        // initLogText();
        etComment.setText(fieldNote.comment);
        // iniDate();
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");
        String sDate = iso8601Format.format(fieldNote.timestamp);
        tvDate.setText(sDate);
        // iniTime();
        iso8601Format = new SimpleDateFormat("HH:mm");
        String sTime = iso8601Format.format(fieldNote.timestamp);
        tvTime.setText(sTime);
        // iniOptions();
        if (fieldNote.type.isDirectLogType()) {
            if (isNewFieldNote) {
                rbOnlyFieldNote.setChecked(true);
            } else {
                if (fieldNote.isDirectLog) {
                    rbDirectLog.setChecked(true);
                } else {
                    rbOnlyFieldNote.setChecked(true);
                }
            }
            rbDirectLog.setChecked(true);
        } else {
            rbOnlyFieldNote.setChecked(true);
        }
        // todo iniGC_VoteItem();
        // iniFoundLine();
        tvFounds.setText("#" + fieldNote.foundNumber);
        if (fieldNote.isTbFieldNote)
            tvFounds.setText("");
        // todo Icon anpassen ivTyp
        // iniTitle();
        title.setText(fieldNote.isTbFieldNote ? fieldNote.TbName : fieldNote.CacheName);
    }

    private void setOkAndCancelClickHandlers() {
        btnOK.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                if (mReturnListener != null) {

                    if (fieldNote.type.isDirectLogType()) {
                        fieldNote.isDirectLog = rbDirectLog.isChecked();
                    } else {
                        fieldNote.isDirectLog = false;
                    }

                    fieldNote.comment = etComment.getText();

                    if (GcVote != null) {
                        fieldNote.gc_Vote = (int) (GcVote.getValue() * 100);
                    } else fieldNote.gc_Vote = 0;

                    // parse Date and Time
                    String date = tvDate.getText();
                    String time = tvTime.getText();

                    date = date.replace("-", ".");
                    time = time.replace(":", ".");

                    try {
                        Date timestamp;
                        DateFormat formatter;

                        formatter = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                        timestamp = formatter.parse(date + "." + time + ".00");

                        fieldNote.timestamp = timestamp;
                    } catch (ParseException e) {
                        final GL_MsgBox msg = GL_MsgBox.Show(Translation.Get("wrongDate"), Translation.Get("Error"), MessageBoxButtons.OK, MessageBoxIcon.Error, new OnMsgBoxClickListener() {

                            @Override
                            public boolean onClick(int which, Object data) {
                                Timer runTimer = new Timer();
                                TimerTask task = new TimerTask() {

                                    @Override
                                    public void run() {
                                        show();
                                    }
                                };

                                runTimer.schedule(task, 200);

                                return true;
                            }
                        });

                        Timer runTimer = new Timer();
                        TimerTask task = new TimerTask() {

                            @Override
                            public void run() {
                                GL.that.showDialog(msg);
                            }
                        };

                        runTimer.schedule(task, 200);
                        return true;
                    }

                    // check of changes
                    if (!altfieldNote.equals(fieldNote)) {
                        fieldNote.uploaded = false;
                        fieldNote.UpdateDatabase();
                        FieldNoteList.CreateVisitsTxt(Config.FieldNotesGarminPath.getValue());
                    }

                    boolean dl = false;
                    if (fieldNote.isDirectLog)
                        dl = true;

                    mReturnListener.returnedFieldNote(fieldNote, isNewFieldNote, dl);
                }
                finish();
                return true;
            }
        });

        btnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                if (mReturnListener != null)
                    mReturnListener.returnedFieldNote(null, false, false);
                finish();
                return true;
            }
        });

    }

    private void iniTitle() {
        ivTyp = new Image(0, 0, UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight(), "", false);
        if (fieldNote.isTbFieldNote) {
            ivTyp.setImageURL(fieldNote.TbIconUrl);
        } else {
            ivTyp.setDrawable(FieldNoteViewItem.getTypeIcon(fieldNote));
        }
        Box b = new Box(ivTyp, "");
        b.addLast(ivTyp, FIXED);
        scrollBoxContent.addNext(b, 0.33f);
        title = new Label(fieldNote.isTbFieldNote ? fieldNote.TbName : fieldNote.CacheName);
        title.setFont(Fonts.getBig());
        scrollBoxContent.addLast(title);
    }

    private void iniFoundLine() {
        scrollBoxContent.addNext(new Label(Translation.Get("caches_found")), 0.6f); // dummy
        tvFounds = new Label("#" + fieldNote.foundNumber);
        if (fieldNote.isTbFieldNote)
            tvFounds.setText("");
        tvFounds.setFont(Fonts.getBig());
        scrollBoxContent.addLast(tvFounds, 0.4f);
    }

    private void iniDate() {
        //scrollBoxContent.addNext(new Label(), 0.33f); // dummy

        lblDate = new Label();
        lblDate.setFont(Fonts.getBig());
        lblDate.setText(Translation.Get("date") + ":");
        scrollBoxContent.addNext(lblDate, 0.6f);

        tvDate = new EditTextField(this, "tvDate");
        scrollBoxContent.addLast(tvDate, 0.4f);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");
        String sDate = iso8601Format.format(fieldNote.timestamp);
        tvDate.setText(sDate);
    }

    private void iniTime() {
        //scrollBoxContent.addNext(new Label(), 0.33f); // dummy

        lblTime = new Label();
        lblTime.setFont(Fonts.getBig());
        lblTime.setText(Translation.Get("time") + ":");
        scrollBoxContent.addNext(lblTime, 0.6f);

        tvTime = new EditTextField(this, "tvTime");
        scrollBoxContent.addLast(tvTime, 0.4f);
        DateFormat iso8601Format = new SimpleDateFormat("HH:mm");
        String sTime = iso8601Format.format(fieldNote.timestamp);
        tvTime.setText(sTime);
    }

    private void iniGC_VoteItem() {
        if (CB_Core_Settings.GcVotePassword.getEncryptedValue().length() > 0) {
            if (!fieldNote.isTbFieldNote) {
                FilterSetEntry tmp = new FilterSetEntry(Translation.Get("maxRating"), Sprites.Stars.toArray(), FilterSetListView.NUMERIC_ITEM, 0, 5, fieldNote.gc_Vote / 100.0, 0.5f);
                GcVote = new FilterSetListViewItem(new CB_RectF(0, 0, innerWidth, UI_Size_Base.that.getButtonHeight() * 1.1f), 0, tmp);
                scrollBoxContent.addLast(GcVote);
            }
        }
    }

    private void initLogText() {
        etComment = new EditTextField(this, "etComment").setWrapType(WrapType.WRAPPED);
        etComment.setHeight(getHeight() / 2.5f);
        scrollBoxContent.addLast(etComment);
        etComment.setText(fieldNote.comment);
    }

    private void iniOptions() {
        rbDirectLog = new RadioButton("direct_Log");
        rbOnlyFieldNote = new RadioButton("only_FieldNote");

        rbDirectLog.setText(Translation.Get("directLog"));
        rbOnlyFieldNote.setText(Translation.Get("onlyFieldNote"));

        RadioGroup Group = new RadioGroup();
        Group.add(rbOnlyFieldNote);
        Group.add(rbDirectLog);

        scrollBoxContent.addLast(rbDirectLog);
        scrollBoxContent.addLast(rbOnlyFieldNote);

        if (isNewFieldNote) {
            rbOnlyFieldNote.setChecked(true);
        } else {
            if (fieldNote.isDirectLog) {
                rbDirectLog.setChecked(true);
            } else {
                rbOnlyFieldNote.setChecked(true);
            }
        }
        rbDirectLog.setChecked(true);
        rbOnlyFieldNote.setChecked(false);
    }

    private void iniTextfieldFocus() {
        registerTextField(etComment);
        etComment.showLastLines();
        registerTextField(tvDate);
        registerTextField(tvTime);
    }

    public void registerTextField(final EditTextField textField) {
        allTextFields.add(textField);
    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {
        super.onTouchDown(x, y, pointer, button);

        if (etComment.contains(x, y)) {
            // TODO close SoftKeyboard
            scrollBoxContent.setY(0);
        }

        // for GCVote
        // for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
        for (Iterator<GL_View_Base> iterator = scrollBox.getchilds().reverseIterator(); iterator.hasNext(); ) {
            // Child View suchen, innerhalb derer Bereich der touchDown statt gefunden hat.
            GL_View_Base view = iterator.next();

            if (view instanceof FilterSetListViewItem) {
                if (view.contains(x, y)) {
                    ((FilterSetListViewItem) view).lastItemTouchPos = new Vector2(x - view.getX(), y - view.getY());
                }
            }
        }
        return true;
    }

    @Override
    public void dispose() {
        super.dispose();
        mReturnListener = null;
        fieldNote = null;
        btnOK = null;
        btnCancel = null;
        etComment = null;
        ivTyp = null;
        tvFounds = null;
        tvDate = null;
        tvTime = null;
        lblDate = null;
        lblTime = null;
        scrollBox = null;
        GcVote = null;
    }

    @Override
    public void onShow() {
        KeyboardFocusChangedEventList.Add(this);
    }

    @Override
    public void onHide() {
        KeyboardFocusChangedEventList.Remove(this);
    }

    @Override
    public void KeyboardFocusChanged(EditTextField editTextField) {
        if (editTextField == null) {
            if (scrollBoxContent != null)
                scrollBoxContent.setY(0);
        } else {
            scrollBoxContent.setY(scrollBoxContent.getHeight() - editTextField.getMaxY());
        }
    }

    public void setFieldNote(FieldNoteEntry note, IReturnListener listener, boolean isNewFieldNote) {
        this.isNewFieldNote = isNewFieldNote;
        mReturnListener = listener;
        fieldNote = note;
        setValuesToLayout();
        altfieldNote = note.copy();
    }

    public interface IReturnListener {
        public void returnedFieldNote(FieldNoteEntry fn, boolean isNewFieldNote, boolean directlog);
    }
}
