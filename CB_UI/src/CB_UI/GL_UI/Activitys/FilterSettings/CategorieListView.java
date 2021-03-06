package CB_UI.GL_UI.Activitys.FilterSettings;

import CB_Core.CoreSettingsForward;
import CB_Core.DAO.CategoryDAO;
import CB_Core.Types.Category;
import CB_Core.Types.GpxFilename;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.Math.CB_RectF;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Iterator;

public class CategorieListView extends V_ListView {

    public static final int COLLAPSE_BUTTON_ITEM = 0;
    public static final int CHECK_ITEM = 1;
    public static final int THREE_STATE_ITEM = 2;
    public static final int NUMERIC_ITEM = 3;
    public static CategorieEntry aktCategorieEntry;
    public static int windowW = 0;
    public static int windowH = 0;
    private final CustomAdapter lvAdapter;
    private ArrayList<CategorieEntry> lCategories;
    private ArrayList<CategorieListViewItem> lCategorieListViewItems;
    OnClickListener onItemClickListener = new OnClickListener() {

        @Override
        public boolean onClick(GL_View_Base v, int lastTouchX, int lastTouchY, int pointer, int button) {
            CB_RectF HitRec = v.copy();
            HitRec.setY(0);

            CB_RectF plusBtnHitRec = new CB_RectF(HitRec.getWidth() - HitRec.getHeight(), 0, HitRec.getHeight(), HitRec.getMaxY());
            CB_RectF minusBtnHitRec = new CB_RectF(HitRec.getX(), 0, HitRec.getHeight(), HitRec.getMaxY());

            float lastItemTouchX = ((CategorieListViewItem) v).lastItemTouchPos.x;
            float lastItemTouchY = ((CategorieListViewItem) v).lastItemTouchPos.y;

            if (plusBtnHitRec.contains(lastItemTouchX, lastItemTouchY)) {
                ((CategorieListViewItem) v).plusClick();
                if (lCategories != null) {
                    for (CategorieEntry tmp : lCategories) {
                        GpxFilename file = tmp.getFile();
                        if (file != null) {
                            tmp.setState(file.Checked ? 1 : 0);
                        }

                    }
                }
                SetCategory();
            } else if (minusBtnHitRec.contains(lastItemTouchX, lastItemTouchY)) {
                ((CategorieListViewItem) v).minusClick();
                SetCategory();
            }

            SetCategory();

            return true;
        }
    };

    public CategorieListView(CB_RectF rec) {
        super(rec, "");
        this.setHasInvisibleItems(true);
        fillCategorieList();
        this.setDisposeFlag(false);
        this.setBaseAdapter(null);
        lvAdapter = new CustomAdapter(lCategories, lCategorieListViewItems);
        this.setBaseAdapter(lvAdapter);

    }

    public void SetCategory() {
        // Set Categorie State
        if (lCategorieListViewItems != null) {
            for (CategorieListViewItem tmp : lCategorieListViewItems) {
                GpxFilename file = tmp.categorieEntry.getFile();

                for (int i = 0, n = CoreSettingsForward.Categories.size(); i < n; i++) {
                    Category cat = CoreSettingsForward.Categories.get(i);
                    int index = cat.indexOf(file);
                    if (index != -1) {

                        cat.get(index).Checked = (tmp.categorieEntry.getState() == 1) ? true : false;

                    } else {
                        if (tmp.getCategorieEntry().getCat() != null) {
                            if (cat == tmp.getCategorieEntry().getCat()) {
                                cat.pinned = tmp.getCategorieEntry().getCat().pinned;
                            }

                        }

                    }

                }

            }
        }
        CoreSettingsForward.Categories.WriteToFilter(EditFilterSettings.tmpFilterProps);

    }

    private void fillCategorieList() {

        CoreSettingsForward.Categories.ReadFromFilter(EditFilterSettings.tmpFilterProps);

        int Index = 0;

        for (int i = 0, n = CoreSettingsForward.Categories.size(); i < n; i++) {
            Category cat = CoreSettingsForward.Categories.get(i);
            CategorieListViewItem CollapseItem = addCategorieCollapseItem(Index++, Sprites.getSprite(IconName.docIcon.name()), cat, COLLAPSE_BUTTON_ITEM);

            for (GpxFilename File : cat) {
                CollapseItem.addChild(addCategorieItem(Index++, Sprites.getSprite(IconName.docIcon.name()), File, CHECK_ITEM));
            }
        }

        // lCategories is filled now we set the checked attr
        if (lCategories != null) {
            for (CategorieEntry tmp : lCategories) {
                GpxFilename file = tmp.getFile();
                if (file != null) {
                    tmp.setState(file.Checked ? 1 : 0);
                }

            }
        }

    }

    private CategorieListViewItem addCategorieItem(int Index, Sprite Icon, GpxFilename file, int ItemType) {
        if (lCategories == null) {
            lCategories = new ArrayList<CategorieListView.CategorieEntry>();
            lCategorieListViewItems = new ArrayList<CategorieListViewItem>();
        }
        CategorieEntry tmp = new CategorieEntry(file, Icon, ItemType);
        lCategories.add(tmp);
        CategorieListViewItem v = new CategorieListViewItem(EditFilterSettings.ItemRec, Index, tmp);
        // inital mit INVISIBLE
        v.setInvisible();
        v.setOnClickListener(onItemClickListener);
        lCategorieListViewItems.add(v);
        return v;
    }

    private CategorieListViewItem addCategorieCollapseItem(int Index, Sprite Icon, Category cat, int ItemType) {
        if (lCategories == null) {
            lCategories = new ArrayList<CategorieListView.CategorieEntry>();
            lCategorieListViewItems = new ArrayList<CategorieListViewItem>();
        }
        CategorieEntry tmp = new CategorieEntry(cat, Icon, ItemType);
        lCategories.add(tmp);

        CategorieListViewItem v = new CategorieListViewItem(EditFilterSettings.ItemRec, Index, tmp);
        lCategorieListViewItems.add(v);

        v.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int X, int Y, int pointer, int button) {
                CB_RectF HitRec = v.copy();
                HitRec.setY(0);

                CB_RectF plusBtnHitRec = new CB_RectF(HitRec.getWidth() - HitRec.getHeight(), 0, HitRec.getHeight(), HitRec.getMaxY());
                CB_RectF minusBtnHitRec = new CB_RectF(HitRec.getX(), 0, HitRec.getHeight(), HitRec.getMaxY());

                float lastTouchX = ((CategorieListViewItem) v).lastItemTouchPos.x;
                float lastTouchY = ((CategorieListViewItem) v).lastItemTouchPos.y;

                if (((CategorieListViewItem) v).getCategorieEntry().getItemType() == COLLAPSE_BUTTON_ITEM) {
                    if (plusBtnHitRec.contains(lastTouchX, lastTouchY)) {
                        ((CategorieListViewItem) v).plusClick();
                        if (lCategories != null) {
                            for (CategorieEntry tmp : lCategories) {
                                GpxFilename file = tmp.getFile();
                                if (file != null) {
                                    tmp.setState(file.Checked ? 1 : 0);
                                }

                            }
                        }
                        SetCategory();
                    } else if (minusBtnHitRec.contains(lastTouchX, lastTouchY)) {
                        ((CategorieListViewItem) v).minusClick();
                        SetCategory();
                    } else {
                        collapseButton_Clicked((CategorieListViewItem) v);
                        notifyDataSetChanged();
                    }

                } else {
                    if (plusBtnHitRec.contains(lastTouchX, lastTouchY)) {
                        SetCategory();
                    }
                }

                return true;
            }
        });

        return v;
    }

    private void collapseButton_Clicked(CategorieListViewItem item) {
        item.toggleChildeViewState();
        this.notifyDataSetChanged();
        this.invalidate();
    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {

        super.onTouchDown(x, y, pointer, button);
        synchronized (childs) {
            for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext(); ) {

                GL_View_Base view = iterator.next();

                // Invisible Views can not be clicked!
                if (!view.isVisible())
                    continue;
                if (view.contains(x, y)) {

                    ((CategorieListViewItem) view).lastItemTouchPos = new Vector2(x - view.getX(), y - view.getY());

                }

            }
        }

        return true;
    }

    public static class CategorieEntry {
        private static int IdCounter;
        private final GpxFilename mFile;
        private final int mItemType;
        private final int ID;
        private Category mCat;
        private Sprite mIcon;
        private Sprite[] mIconArray;
        private int mState = 0;
        private double mNumericMax;
        private double mNumericStep;
        private double mNumericState;

        public CategorieEntry(GpxFilename file, Sprite Icon, int itemType) {
            mCat = null;
            mFile = file;
            mIcon = Icon;
            mItemType = itemType;
            ID = IdCounter++;

        }

        public CategorieEntry(Category cat, Sprite Icon, int itemType) {
            mCat = cat;
            mFile = null;
            mIcon = Icon;
            mItemType = itemType;
            ID = IdCounter++;

        }

        public CategorieEntry(GpxFilename file, Sprite[] Icons, int itemType, double min, double max, double iniValue, double Step) {
            mFile = file;
            mIconArray = Icons;
            mItemType = itemType;
            mNumericMax = max;
            mNumericState = iniValue;
            mNumericStep = Step;
            ID = IdCounter++;
        }

        public void setState(int State) {
            mState = State;
        }

        public GpxFilename getFile() {
            return mFile;
        }

        public Sprite getIcon() {
            if (mItemType == NUMERIC_ITEM) {
                try {
                    double ArrayMultiplier = (mIconArray.length > 5) ? 2 : 1;

                    return mIconArray[(int) (mNumericState * ArrayMultiplier)];
                } catch (Exception e) {
                }

            }
            return mIcon;
        }

        public int getState() {
            return mState;
        }

        public void setState(float State) {
            mNumericState = State;
        }

        public int getItemType() {
            return mItemType;
        }

        public int getID() {
            return ID;
        }

        public double getNumState() {
            return mNumericState;
        }

        public void plusClick() {

            if (mItemType == COLLAPSE_BUTTON_ITEM) {
                // collabs Button chk clicked
                int State = mCat.getCheck();
                if (State == 0) {// keins ausgewählt, also alle anwählen

                    for (GpxFilename tmp : mCat) {
                        tmp.Checked = true;
                    }

                } else {// einer oder mehr ausgewählt, also alle abwählen

                    for (GpxFilename tmp : mCat) {
                        tmp.Checked = false;
                    }

                }
            } else {
                stateClick();
            }

        }

        public void minusClick() {
            if (mItemType == COLLAPSE_BUTTON_ITEM) {
                // Collabs Button Pin Clicked
                CategoryDAO dao = new CategoryDAO();
                dao.SetPinned(this.mCat, !this.mCat.pinned);
                // this.mCat.pinned = !this.mCat.pinned;

            } else {
                mNumericState -= mNumericStep;
                if (mNumericState < 0)
                    mNumericState = mNumericMax;
            }
        }

        public void stateClick() {

            mState += 1;
            if (mItemType == CategorieListView.CHECK_ITEM || mItemType == CategorieListView.COLLAPSE_BUTTON_ITEM) {
                if (mState > 1)
                    mState = 0;
            } else if (mItemType == CategorieListView.THREE_STATE_ITEM) {
                if (mState > 1)
                    mState = -1;
            }

            if (mItemType == CategorieListView.CHECK_ITEM) {
                if (mState == 0)
                    this.mFile.Checked = false;
                else
                    this.mFile.Checked = true;
            }
        }

        public String getCatName() {
            return mCat.GpxFilename;
        }

        public Category getCat() {
            return mCat;
        }

    }

    public class CustomAdapter implements Adapter {

        private final ArrayList<CategorieEntry> categorieList;
        private final ArrayList<CategorieListViewItem> lCategoriesListViewItems;

        public CustomAdapter(ArrayList<CategorieEntry> lCategories, ArrayList<CategorieListViewItem> CategorieListViewItems) {
            this.categorieList = lCategories;
            this.lCategoriesListViewItems = CategorieListViewItems;
        }

        @Override
        public int getCount() {
            if (categorieList == null)
                return 0;
            return categorieList.size();
        }

        public Object getItem(int position) {
            if (categorieList == null)
                return null;
            return categorieList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public ListViewItemBase getView(int position) {
            if (lCategoriesListViewItems == null)
                return null;
            CategorieListViewItem v = lCategoriesListViewItems.get(position);
            if (!v.isVisible())
                return null;

            return v;
        }

        @Override
        public float getItemSize(int position) {
            return EditFilterSettings.ItemRec.getHeight();
        }
    }

}
