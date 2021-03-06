package CB_UI_Base.GL_UI.Menu;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.Label.HAlignment;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.Controls.MessageBox.ButtonDialog;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.utils.ColorDrawable;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.Math.UI_Size_Base;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.ArrayList;

public class Menu extends ButtonDialog {

    private static final int ANIMATION_DURATION = 1200;
    private static float mMoreMenuToggleButtonWidth = -1;
    private static CB_RectF sMenuRec = null;
    private static boolean MENU_REC_IsInitial = false;
    public float ItemHeight = -1f;
    public ArrayList<MenuItemBase> mItems = new ArrayList<MenuItemBase>();
    protected ArrayList<OnClickListener> mOnItemClickListeners;
    private V_ListView mListView;
    private Menu mMoreMenu = null;
    private boolean mMoreMenuVisible = false;
    private Button mMoreMenuToggleButton;
    private Label mMoreMenuLabel;
    /**
     * -1=not initial<br>
     * 0=left<br>
     * 1=to left<br>
     * 2= to right<br>
     * 3=right
     */
    private int mAnimationState = -1;// -1=not initial 0=left 1=to left 2= to right 3=right
    private int itemsCount = -1;
    private float animateStartTime;
    private boolean isMoreMenu = false;
    private String mMoreMenuTextRight = "";
    private String mMoreMenuTextLeft = "";
    private Menu mParentMenu;
    protected OnClickListener menuItemClickListener = new OnClickListener() {

        @Override
        public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
            GL.that.closeDialog(Menu.this);
            if (isMoreMenu)
                GL.that.closeDialog(mParentMenu);
            if (mOnItemClickListeners != null) {
                for (OnClickListener tmp : mOnItemClickListeners) {
                    if (tmp.onClick(v, x, y, pointer, button))
                        break;
                }
            }

            return true;
        }
    };
    private boolean mMoreMenuIsInitial = false;
    private int Level = 0;

    public Menu(String Name) {
        super(getMenuRec(), Name);

        if (ItemHeight == -1f)
            ItemHeight = UI_Size_Base.that.getButtonHeight();
        mListView = new V_ListView(this, "MenuList");
        mListView.setSize(this.getContentSize());
        mListView.setZeroPos();
        mListView.setDisposeFlag(false);
        this.addChild(mMoreMenu);
        initialDialog();
    }

    public static CB_RectF getMenuRec() {
        if (!MENU_REC_IsInitial) {
            float sollWidth = GL_UISizes.UI_Left.getWidth();

            sollWidth *= 0.83;
            sMenuRec = new CB_RectF(new SizeF(sollWidth, 50));
            MENU_REC_IsInitial = true;
        }
        return sMenuRec;
    }

    public void addMoreMenu(Menu menu, String TextLeft, String TextRight) {
        if (menu == null) {
            mMoreMenuTextRight = "";
            mMoreMenuTextLeft = "";
            mMoreMenu = null;
            return;
        }
        mMoreMenuTextRight = TextRight;
        mMoreMenuTextLeft = TextLeft;
        mMoreMenu = menu;
        mMoreMenu.isMoreMenu = true;
        mMoreMenu.setParentMenu(this);
        mMoreMenu.setVisible(false);
        mMoreMenu.Level = this.Level + 1;
        mMoreMenu.setBackground(new ColorDrawable(COLOR.getMenuBackColor()));
    }

    public Menu getMoreMenu() {
        return mMoreMenu;
    }

    public String getTextLeftMoreMenu() {
        return mMoreMenuTextLeft;
    }

    public String getTextRightMoreMenu() {
        return mMoreMenuTextRight;
    }

    private void setParentMenu(Menu menu) {
        mParentMenu = menu;
    }

    private void toggleMoreMenu() {
        mMoreMenuVisible = !mMoreMenuVisible;
        if (mMoreMenuVisible) {
            showMoreMenu();
        } else {
            hideMoreMenu();
        }
        animateStartTime = GL.that.getStateTime();
        mListView.notifyDataSetChanged();
    }

    private void showMoreMenu() {
        mMoreMenu.setVisible(true);
        mAnimationState = 1;
        mMoreMenu.setWidth(0);
        layout();
        int index = GL.that.getDialogLayer().getchilds().indexOf(mMoreMenuToggleButton);
        GL.that.getDialogLayer().getchilds().MoveItemLast(index);
    }

    private void hideMoreMenu() {
        mAnimationState = 2;
        mMoreMenu.setWidth(this.getWidth());
        layout();
        int index = GL.that.getDialogLayer().getchilds().indexOf(mMoreMenuToggleButton);
        GL.that.getDialogLayer().getchilds().MoveItemLast(index);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (mMoreMenuToggleButton != null) {
            if (visible)
                layout();
            mMoreMenuToggleButton.setVisible(visible);
        }
    }

    @Override
    protected void Initial() {
        super.Initial();

        if (!isMoreMenu) {
            // Menu level 1

            if (mItems.size() != itemsCount) {
                // new Hight calculation
                itemsCount = mItems.size();
                float higherValue = mTitleHeight + mHeaderHeight + mFooterHeight + (margin * 2);
                for (MenuItemBase item : mItems) {
                    higherValue += item.getHeight() + mListView.getDividerHeight();
                }
                higherValue = Math.min(higherValue, UI_Size_Base.that.getWindowHeight() * 0.95f);
                if (higherValue > UI_Size_Base.that.getWindowHeight() * 0.95f) {
                    higherValue = UI_Size_Base.that.getWindowHeight() * 0.95f;
                }
                float MenuWidth = GL_UISizes.UI_Left.getWidth();
                this.setSize(MenuWidth, higherValue);

                // initial more menus
                if (mMoreMenu != null)
                    mMoreMenu.Initial();

            }
        } else {
            this.setSize(mParentMenu.getWidth(), mParentMenu.getHeight());
        }

        if (mMoreMenuToggleButtonWidth == -1) {
            float mesuredLblHeigt = Fonts.MeasureSmall("T").height;
            mMoreMenuToggleButtonWidth = Sprites.btn.getLeftWidth() + Sprites.btn.getRightWidth() + (mesuredLblHeigt * 1.5f);
        }

        mListView.setSize(this.getContentSize());

        this.addChild(mListView);
        mListView.setBaseAdapter(new CustomAdapter());

        if (mMoreMenu != null && !mMoreMenuIsInitial) {
            mMoreMenu.Initial();
            mMoreMenu.setVisible(false);
            mMoreMenu.setZeroPos();
            mMoreMenu.setHeight(this.getHeight());
            mMoreMenu.setWidth(0);
            mMoreMenu.setY(0 - mFooterHeight);

            mMoreMenu.setBackground(new ColorDrawable(COLOR.getMenuBackColor()));

            this.addChild(mMoreMenu);

            mMoreMenuToggleButton = new Button("");
            mMoreMenuToggleButton.setWidth(mMoreMenuToggleButtonWidth);
            mMoreMenuToggleButton.setHeight(this.getContentSize().height);
            float MenuY = mParentMenu != null ? mParentMenu.getY() : this.getY();
            mMoreMenuToggleButton.setY(MenuY + mFooterHeight);
            GL.that.getDialogLayer().addChild(mMoreMenuToggleButton);
            mMoreMenuToggleButton.setOnClickListener(new OnClickListener() {

                @Override
                public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                    toggleMoreMenu();
                    return true;
                }
            });

            mMoreMenuLabel = new Label(mMoreMenuTextRight, Fonts.getSmall(), COLOR.getFontColor(), WrapType.SINGLELINE).setHAlignment(HAlignment.CENTER);
            // mMoreMenuLabel.setRec(mMoreMenuToggleButton);
            mMoreMenuLabel.setWidth(mMoreMenuToggleButton.getHeight());
            mMoreMenuLabel.setHeight(mMoreMenuToggleButton.getWidth());
            mMoreMenuLabel.setX(mMoreMenuToggleButton.getWidth());
            mMoreMenuLabel.setY(0);
            mMoreMenuLabel.setOrigin(0, 0);
            mMoreMenuLabel.setRotate(90);
            mMoreMenuLabel.withoutScissor = true;
            mMoreMenuToggleButton.addChild(mMoreMenuLabel);
            mMoreMenuIsInitial = true;
        }

        // set display center pos
        float cx = (UI_Size_Base.that.getWindowWidth() / 2) - this.getHalfWidth();
        float cy = (UI_Size_Base.that.getWindowHeight() / 2) - this.getHalfHeight();
        this.setPos(cx, cy);

        layout();
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);

        if (mMoreMenu == null || !mMoreMenuIsInitial || mMoreMenuToggleButton == null)
            return;

        if (mAnimationState > -1)
            mMoreMenuToggleButton.setY(this.getWorldRec().getY() + mFooterHeight);

        // Animation calculation
        if (mAnimationState == 1 || mAnimationState == 2) {
            float targetValue = this.getWidth() * 1.5f;

            float animateValue = (1 + ((int) ((GL.that.getStateTime() - animateStartTime) * 1000) % ANIMATION_DURATION) / (ANIMATION_DURATION / targetValue));

            if (mAnimationState == 1) {
                if (animateValue >= this.getWidth() - 10) {
                    animateValue = this.getWidth();
                    mAnimationState = 0;
                }
                mMoreMenu.setSize(animateValue, this.getHeight());
            } else {
                if (animateValue >= this.getWidth() - 10) {
                    animateValue = this.getWidth();
                    mMoreMenu.setVisible(false);
                    mAnimationState = 3;
                }
                mMoreMenu.setSize(this.getWidth() - animateValue, this.getHeight());
            }

            layout();
            GL.that.renderOnce();
        } else if (mAnimationState == -1) {
            if (mMoreMenu != null) {
                mMoreMenuToggleButton.setHeight(this.getContentSize().height);
                mAnimationState = 3;
                layout();
            }

        }

    }

    public void addItem(MenuItemBase menuItem) {
        menuItem.setOnClickListener(menuItemClickListener);
        mItems.add(menuItem);
        mListView.notifyDataSetChanged();
        //resetInitial();
    }

    public MenuItem addItem(int ID, String StringId) {
        return addItem(ID, StringId, "", false);
    }

    public MenuItem addItem(int ID, String StringId, boolean withoutTranslation) {
        return addItem(ID, StringId, "", withoutTranslation);
    }

    public MenuItem addItem(int ID, String StringId, String anhang, Sprite icon) {
        MenuItem item = addItem(ID, StringId, anhang);
        if (icon != null)
            item.setIcon(new SpriteDrawable(icon));
        return item;
    }

    public MenuItem addItem(int ID, String StringId, String anhang, Drawable icon) {
        MenuItem item = addItem(ID, StringId, anhang);
        if (icon != null)
            item.setIcon(icon);
        return item;
    }

    public MenuItem addItem(int ID, String StringId, String anhang) {
        return addItem(ID, StringId, anhang, false);
    }

    public MenuItem addItem(int index, String text, Drawable drawable, boolean withoutTranslation) {
        MenuItem item = addItem(index, text, "", withoutTranslation);
        if (drawable != null)
            item.setIcon(drawable);
        return item;
    }

    public MenuItem addItem(int ID, String StringId, String anhang, boolean withoutTranslation) {
        String trans;
        if (StringId == null || StringId.equals("")) {
            trans = anhang;
        } else {
            if (withoutTranslation)
                trans = StringId + anhang;
            else
                trans = Translation.Get(StringId) + anhang;
        }

        // layout();
        MenuItem item = new MenuItem(new SizeF(mListView.getWidth(), ItemHeight), mItems.size(), ID, "Menu Item@" + ID);

        item.setTitle(trans);
        addItem(item);

        return item;
    }

    public MenuItem addCheckableItem(int ID, String StringId, boolean checked) {
        MenuItem item = addItem(ID, StringId, "", false);
        item.setCheckable(true);
        item.setChecked(checked);
        return item;
    }

    private void layout() {

        if (mListView == null || mListView.isDisposed())
            return;

        try {
            float WithOffset = isMoreMenu ? mMoreMenuToggleButtonWidth : 0;
            if (isMoreMenu && mMoreMenu != null)
                WithOffset = mMoreMenuToggleButtonWidth;
            if (!isMoreMenu && mMoreMenu != null)
                WithOffset = mMoreMenuToggleButtonWidth / 1.1f;
            if (mListView != null) {
                mListView.setSize(this.getContentSize().width - WithOffset, this.getContentSize().height);
                mListView.setZeroPos();
                if (isMoreMenu && mMoreMenu != null)
                    WithOffset /= 2;
                if (!isMoreMenu && mMoreMenu != null)
                    WithOffset = 0;
                mListView.setX(WithOffset);
            }

            // Alle Items in der Breite anpassen
            float w = mListView.getWidth();
            for (MenuItemBase item : mItems) {
                item.setWidth(w);
            }

            if (mMoreMenuToggleButton != null) {

                switch (mAnimationState) {
                    case 0:
                        this.setWidth(getLeve0_Width());
                        mMoreMenu.setWidth(getLeve0_Width());
                        mMoreMenu.setX(-this.getLeftWidth() - this.getRightWidth() - 2.5f);
                        // TODO die -2,5f m�ssen auf meinem S3 sein,
                        // damit die linke Position passt auf dem desktop sind es 0 auf anderen?
                        // ich habe hier den zusammen hang noch nicht finden k�nnen
                        mMoreMenuToggleButton.setX(getLevel0_x() - mMoreMenuToggleButton.getHalfWidth() + (margin * 2));

                        mMoreMenuLabel.setText(mMoreMenuTextLeft);
                        break;
                    case 1:
                        mMoreMenu.setX(this.getWidth() - mMoreMenu.getWidth() - this.getLeftWidth());
                        mMoreMenuToggleButton.setX(getLevel0_x() + mMoreMenu.getX() - this.getLeftWidth());
                        break;
                    case 2:
                        mMoreMenu.setX(this.getWidth() - mMoreMenu.getWidth() - this.getLeftWidth());
                        mMoreMenuToggleButton.setX(getLevel0_x() + mMoreMenu.getX() - mMoreMenuToggleButton.getHalfWidth());
                        break;
                    case 3:
                        mMoreMenu.setWidth(0);
                        mMoreMenuToggleButton.setX(getLevel0_maxX() - mMoreMenuToggleButton.getHalfWidth() - (margin * 2));

                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Show() {
        layout();
        super.Show();

    }

    public MenuItem addItem(int ID, String StringId, Sprite icon) {
        MenuItem item = addItem(ID, StringId);
        if (icon != null)
            item.setIcon(new SpriteDrawable(icon));
        return item;
    }

    public void addOnClickListener(OnClickListener onItemClickListener) {
        if (this.mOnItemClickListeners == null)
            this.mOnItemClickListeners = new ArrayList<GL_View_Base.OnClickListener>();
        this.mOnItemClickListeners.add(onItemClickListener);
    }

    public void addOnClickListeners(ArrayList<OnClickListener> onItemClickListeners) {
        if (this.mOnItemClickListeners == null)
            this.mOnItemClickListeners = new ArrayList<GL_View_Base.OnClickListener>();
        this.mOnItemClickListeners.addAll(onItemClickListeners);
    }

    public ArrayList<OnClickListener> getOnItemClickListeners() {
        return this.mOnItemClickListeners;
    }

    public void setPrompt(String Prompt) {
        // set Title with full width, add many blanks: that is bad
        // this.setTitle(Prompt + "                                                       ");
        this.setTitle(Prompt);
        layout();
    }

    @Override
    public void onResized(CB_RectF rec) {
        super.onResized(rec);
        layout();
    }

    public ArrayList<MenuItemBase> getItems() {
        return mItems;
    }

    public void addItems(ArrayList<MenuItemBase> items) {
        for (MenuItemBase menuItem : items) {
            menuItem.setOnClickListener(menuItemClickListener);
            mItems.add(menuItem);
            mListView.notifyDataSetChanged();
        }
    }

    public void addDivider() {
        MenuItemDivider item = new MenuItemDivider(new SizeF(mListView.getWidth(), ItemHeight / 5), mItems.size(), "Menu Devider");
        item.setEnabled(false);
        addItem(item);
    }

    /**
     * Die indexes der Items werden neu erstellt.
     */
    public int reorganizeIndexes() {
        int Index = 0;
        for (MenuItemBase item : mItems) {
            item.setIndex(Index++);
        }
        return Index;
    }

    private float getLevel0_x() {
        if (mParentMenu == null)
            return this.getX();
        return mParentMenu.getLevel0_x();
    }

    private float getLevel0_maxX() {
        if (mParentMenu == null)
            return this.getMaxX();
        return mParentMenu.getLevel0_maxX();
    }

    private float getLeve0_Width() {
        if (mParentMenu == null)
            return this.getWidth();
        return mParentMenu.getLeve0_Width();
    }

    @Override
    public void dispose() {
        mMoreMenuTextRight = null;
        mMoreMenuTextLeft = null;
        mParentMenu = null;

        if (mMoreMenu != null) {
            mMoreMenu.dispose();
        }
        mMoreMenu = null;

        if (mMoreMenuToggleButton != null) {
            mMoreMenuToggleButton.dispose();
        }
        mMoreMenuToggleButton = null;

        if (mMoreMenuLabel != null) {
            mMoreMenuLabel.dispose();
        }
        mMoreMenuLabel = null;

        if (mItems != null) {
            for (MenuItemBase it : mItems) {
                it.dispose();
            }
            mItems.clear();
        }
        mItems = null;

        if (mListView != null) {
            mListView.dispose();
        }
        mListView = null;
        super.dispose();
    }

    public class CustomAdapter implements Adapter {

        @Override
        public ListViewItemBase getView(int position) {
            ListViewItemBase v = mItems.get(position);
            //v.setWidth(mListView.getWidth());
            //	    v.resetInitial();
            return v;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public float getItemSize(int position) {
            if (mItems == null || mItems.size() == 0 || mItems.size() < position)
                return 0;
            return mItems.get(position).getHeight();
        }
    }

}
