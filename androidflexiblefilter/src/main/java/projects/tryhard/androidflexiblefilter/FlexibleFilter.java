package projects.tryhard.androidflexiblefilter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.android.flexbox.AlignContent;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A class for filter, set up like this. For more detail and samples, go to {@see <a hreh="https://github.com/you55645/Android-Flex-Filter" >FlexibleFilter</a>}.
 * 1. If you use xml to set attributes or you want to use default values, you can use the {@link #init(Context, int, Object, FilterCallback)}. If you want to do the init settings through code, use {@link #init(Context, int, Object, int, boolean, boolean, Orientation, int, boolean, boolean, FilterCallback)}.
 * --- Basic set up done, you can open up to see how it looks like.
 * 2. After init, you will have at least one filter, you can add filter later also, but if you only planning on using one. You can start to add options. (step 4).
 * 3. If you want to add more filters, use {@link #addFilter(Object, int)}.
 * 4. Adding options to filter you want to add by {@link #addFilterOption(int, Object, int, int, OptionGetStringCallback)}  or {@link #addFilterOption(int, Object, int, int, int, int, int, , OptionGetStringCallback)}.
 * 5. If you want to update certain option from certain filter, use {@link #updateCertainOption(int, Object, int)}. If you want to update all, use {@link #updateAllFilters()}.
 * --- Set up done.
 *
 * @param <T> A class for you to decide the unique ID of every option, which will be passed when user clicked an option or call {@link #optionSelect(int, Object)} through code.
 */
public class FlexibleFilter<T> extends LinearLayout {
    /**
     * A Enum used to decide the mOrientation of the whole Filter Layout
     */
    public enum Orientation {
        VERTICAL,
        HORIZONTAL
    }

    /**
     * Default color of the text color, can be changed.
     */
    private @ColorRes
    int mUnSelectedTextColor = R.color.black;
    private @ColorRes
    int mSelectedTextColor = R.color.white;

    /**
     * Default background of the filter option background, can be changed.
     */
    private @DrawableRes
    int mSelectedBackground = R.drawable.filter_selector_default_selected;
    private @DrawableRes
    int mUnSelectedBackground = R.drawable.filter_selector_default_not_selected;

    private Context mContext;

    private List<FilterHolder> mFilters;
    private FilterCallback<T> mFilterCallback;
    private List<Integer> mCurrentOpeningFilters;

    /**
     * views used.
     */
    private ScrollView mVerticalScrollView;
    private HorizontalScrollView mHorizontalScrollView;
    private LinearLayout mTitleContainer;
    private FlexboxLayout mFilterContainer;
    /**
     * Will return this view in callback so you can do things with it.
     */
    private View mTitleView;


    private int mDefaultEmptyViewLayout;
    /**
     * If this flag set to true, will hide all the options that is 0.
     */
    private boolean mShouldHideZeroFilters = false;
    /**
     * If this flag set to true, will hide the default added all option.
     */
    private boolean mShouldHideAll = false;
    /**
     * A variable decide how many filters we show at one row. Default is one.  -2 means wrap_content. -1 means match_parent, but it's seems only works on vertical orientation.
     */
    private int mFilterColCount = 1;
    /**
     * A variable to check where we are in horizontal or vertical mode.
     */
    private Orientation mOrientation = Orientation.VERTICAL;
    /**
     * A variable to decide whether to close the dropdown or not after click on an option.
     */
    private boolean mShouldCloseAfterClick = true;

    /**
     *  A variable to decide when option select, change its' color or not.
     */
    private boolean mChangeColorWhenSelect = true;

    /**
     * Callback when option being pressed or called @link #optionSelect(int filterNum, T filterId)
     *
     * @param <T> T should be unique so you can used it to find out which option user select.
     */
    public interface FilterCallback<T> {
        /**
         * Call when option being pressed or called @link #optionSelect(int filterNum, T filterId)
         *
         * @param titleView The title view you passed in when init.
         * @param filterNum Let you know which filter user select.
         * @param filterId  Option unique value.
         */
        void filterOptionClicked(View titleView, int filterNum, T filterId);

        /**
         * Get called when you select an non-exist option.
         */
        void filterOptionNotExistError();

        /**
         * Get called when is no FilterNum match that.
         */
        void noSuchFilterError(int notExistFilterNum);
    }

    /**
     * A callback for you to decide the text on option.
     */
    public interface OptionGetStringCallback<T> {
        String getString(T filterId, int count);
    }

    //region View constructors.
    public FlexibleFilter(Context context) {
        super(context);
    }

    public FlexibleFilter(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.FlexibleFilter);

        mFilterColCount = attributes.getInteger(R.styleable.FlexibleFilter_colCount, 1);
        mShouldHideAll = attributes.getBoolean(R.styleable.FlexibleFilter_shouldHideAll, false);
        mShouldHideZeroFilters = attributes.getBoolean(R.styleable.FlexibleFilter_shouldHideZeroFilters, false);
        mShouldCloseAfterClick = attributes.getBoolean(R.styleable.FlexibleFilter_shouldCloseAfterClick, true);
        mChangeColorWhenSelect = attributes.getBoolean(R.styleable.FlexibleFilter_changeColorWhenSelect, true);

        if (attributes.getInt(R.styleable.FlexibleFilter_orientation, 0) == 0) {
            mOrientation = Orientation.VERTICAL;
        } else {
            mOrientation = Orientation.HORIZONTAL;
        }

        attributes.recycle();
    }

    public FlexibleFilter(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.FlexibleFilter, defStyleAttr, 0);

        mFilterColCount = attributes.getInteger(R.styleable.FlexibleFilter_colCount, 1);
        mShouldHideAll = attributes.getBoolean(R.styleable.FlexibleFilter_shouldHideAll, false);
        mShouldHideZeroFilters = attributes.getBoolean(R.styleable.FlexibleFilter_shouldHideZeroFilters, false);
        mShouldCloseAfterClick = attributes.getBoolean(R.styleable.FlexibleFilter_shouldCloseAfterClick, true);
        mChangeColorWhenSelect = attributes.getBoolean(R.styleable.FlexibleFilter_changeColorWhenSelect, true);

        if (attributes.getInt(R.styleable.FlexibleFilter_orientation, 0) == 0) {
            mOrientation = Orientation.VERTICAL;
        } else {
            mOrientation = Orientation.HORIZONTAL;
        }

        attributes.recycle();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FlexibleFilter(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    //endregion

    //region Init.

    /**
     * Use to init the whole filter with more detail.
     *
     * @param context            We use to inflate layouts.
     * @param titleLayout        The title you want for the filter, -1 means no title, 0 means default title.
     * @param allT               A unique ID for the default all option.
     * @param emptyDefaultLayout A default Empty Layout, -1 means use default.
     * @param hideAll            Should we hide the default all option?
     * @param hideZeroFilters    Should we hide the options that is 0?
     * @param orientation        The mOrientation of the filters' layout.
     * @param colCount           How many filters we show at one row. Default is one.
     * @param filterCallback     Callbacks when option being pressed or called @link #optionSelect(int filterComplex, T filterId), or error happens.
     * @return A number for default filter, use it when you want to update, show or hide certain filter.
     */
    public int init(Context context, @LayoutRes int titleLayout, final T allT, @LayoutRes int emptyDefaultLayout, boolean hideAll, boolean hideZeroFilters, Orientation orientation, int colCount, boolean shouldCloseAfterClick, boolean changeColorWhenSelect, FilterCallback<T> filterCallback) {
        int filterNum = init(context, titleLayout, allT, filterCallback);

        setShouldHideZeroFilters(hideZeroFilters);
        setShouldHideAll(hideAll);
        setFilterOrientation(orientation);
        setFilterColCount(colCount);
        setShouldCloseAfterClick(shouldCloseAfterClick);
        setChangeColorWhenSelect(changeColorWhenSelect);

        if (emptyDefaultLayout != -1) {
            mDefaultEmptyViewLayout = emptyDefaultLayout;
        } else {
            mDefaultEmptyViewLayout = R.layout.filter_default_empty_view;
        }

        return filterNum;
    }

    /**
     * Use to init the whole filter with default.
     *
     * @param context        We use to inflate layouts.
     * @param titleLayout    The title you want for the filter, -1 means no title, 0 means default title.
     * @param allT           A unique ID for the default all option.
     * @param filterCallback Callbacks when option being pressed or called @link #optionSelect(int filterComplex, T filterId), or error happens.
     * @return A number for default filter, use it when you want to update, show or hide certain filter.
     */
    public int init(Context context, @LayoutRes int titleLayout, final T allT, FilterCallback<T> filterCallback) {
        inflate(getContext(), R.layout.filter_layout, this);
        mFilterCallback = filterCallback;
        mContext = context;
        mTitleContainer = findViewById(R.id.filter_title_container);
        mFilterContainer = findViewById(R.id.filter_filter_container);
        mHorizontalScrollView = findViewById(R.id.filter_horizontalScrollView);
        mVerticalScrollView = findViewById(R.id.filter_verticalScrollView);

        mDefaultEmptyViewLayout = R.layout.filter_default_empty_view;

        mCurrentOpeningFilters = new ArrayList<>();

        if (titleLayout != -1) {
            if (titleLayout == 0) {
                mTitleView = LayoutInflater.from(context).inflate(R.layout.filter_default_title, null);
                mTitleView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!isCurrentOpen()) {
                            open();
                        } else {
                            close();
                        }
                    }
                });
            } else {
                mTitleView = LayoutInflater.from(context).inflate(titleLayout, null);
            }
            mTitleContainer.addView(mTitleView);
        }

        mFilters = new ArrayList<>();

        addFilter(allT, -1);

        mCurrentOpeningFilters.add(0);

        setFilterOrientation(mOrientation);
        return 0;
    }
    //endregion

    /**
     * @param defaultT        A unique ID for the default all option.
     * @param emptyViewLayout A view to display when no option are shown. -1 means default emptyView.
     * @return A number for this filter, use it when you want to update, show or hide certain filter.
     */
    public int addFilter(final T defaultT, @LayoutRes int emptyViewLayout) {
        FlexboxLayout flexboxLayout = new FlexboxLayout(mContext);
        FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        flexboxLayout.setLayoutParams(lp);
        flexboxLayout.setFlexDirection(FlexDirection.ROW);
        flexboxLayout.setFlexWrap(FlexWrap.WRAP);
        flexboxLayout.setJustifyContent(JustifyContent.FLEX_START);
        flexboxLayout.setAlignItems(AlignItems.FLEX_START);
        flexboxLayout.setAlignContent(AlignContent.FLEX_START);

        mFilterContainer.addView(flexboxLayout);

        if (emptyViewLayout != -1) {
            View emptyView = LayoutInflater.from(mContext).inflate(emptyViewLayout, null);
            mFilters.add(new FilterHolder(new ArrayList<Option>(), flexboxLayout, emptyView));
        } else {
            View emptyView = LayoutInflater.from(mContext).inflate(mDefaultEmptyViewLayout, null);
            mFilters.add(new FilterHolder(new ArrayList<Option>(), flexboxLayout, emptyView));
        }

        addFilterOption(mFilters.size() - 1, defaultT, 0, getScreenWidthPixel(mContext), dpToPixels(mContext, 8), dpToPixels(mContext, 8), dpToPixels(mContext, 8), new OptionGetStringCallback<T>() {
            @Override
            public String getString(T filterId, int count) {
                return String.format(Locale.CHINESE, "All(%d)", count);
            }
        });

        return mFilters.size() - 1;
    }

    /**
     * Update all exist filters, including those are hiding.
     */
    public void updateAllFilters() {
        for (int i = 0; i < mFilters.size(); i++) {
            updateFilter(i);
        }
    }

    /**
     * Check if the Dropdown part being opened.
     *
     * @return true if opened.
     */
    public boolean isCurrentOpen() {
        return mFilters.get(mCurrentOpeningFilters.get(0)).isContainerVisible();
    }

    /**
     * Just open one filter.
     *
     * @param filterNum The filter number tou want to open.
     */
    public void setOpeningFilterOne(int filterNum) {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(filterNum);
        setOpeningFilters(list);
    }

    /**
     * Want to open multiple filters at the same time.
     *
     * @param openFilters The filter numbers tou want to open.
     */
    public void setOpeningFilters(List<Integer> openFilters) {
        if (!isFiltersValid(openFilters)) return;
        if (isCurrentOpen()) {
            hideAllOpeningContainer();
        }

        for (int i = 0; i < openFilters.size(); i++) {
            mFilters.get(openFilters.get(i)).setContainerVisible(isCurrentOpen());
            if (mFilterColCount > 0) {
                mFilters.get(openFilters.get(i)).setContainerSize(getScreenWidthPixel(mContext) / mFilterColCount);
            } else if (mFilterColCount == -1) {
                mFilters.get(openFilters.get(i)).setContainerSize(ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }

        mCurrentOpeningFilters.clear();
        mCurrentOpeningFilters = openFilters;
        updateAllFilters();
    }

    /**
     * Get Opening filter numbers.
     *
     * @return A list contain all the filter numbers opening.
     */
    public List<Integer> getCurrentOpeningFilters() {
        return mCurrentOpeningFilters;
    }

    /**
     * Perform a click on an option by code.
     *
     * @param filterNum The unique filter number you want to click.
     * @param filterId  The unique filter option ID you want to click.
     */
    public void optionSelect(int filterNum, T filterId) {

        if (isFilterOpening(filterNum)) {
//            mCurrentOpeningFilters = filterNum;

            if(mChangeColorWhenSelect){
                mFilters.get(filterNum).setOptionsDeco(filterId);
            }

            if (mShouldCloseAfterClick) {
                close();
            }

            mFilters.get(filterNum).setCurrentSelected(filterId);

            updateFilter(filterNum);

            mFilterCallback.filterOptionClicked(mTitleView, filterNum, filterId);
        } else {
            // not in should we add?
            mFilterCallback.filterOptionNotExistError();
        }
    }

    /**
     * Update a certain filter's certain option count.
     *
     * @param filterNum The certain filter number.
     * @param filterId  The certain option ID.
     * @param count     New count.
     */
    public void updateCertainOption(int filterNum, T filterId, int count) {
        mFilters.get(filterNum).getFilterButton(filterId).setResultCount(count);
        updateFilter(filterNum);
    }

    /**
     * I don't really remove it, I just hide it up and set a flag to it so it won't be open in any circumstances.
     *
     * @param filterNum The filter number you want to remove.
     */
    public void removeFilter(int filterNum) {
        mFilters.get(filterNum).removeFilter();
        updateAllFilters();
    }

    /**
     * Add a option to filter with left margin 4dp, right 4dp, up and down 8dp.
     *
     * @param filterNum                The filter number where you want to add an option.
     * @param filterId                 The unique ID you give to the option.
     * @param count                    The count of this option from the beginning.
     * @param width                    The width of this option.
     * @param mOptionGetStringCallback For you to decide the text on the option.
     */
    public void addFilterOption(int filterNum, T filterId, int count, int width, OptionGetStringCallback<T> mOptionGetStringCallback) {
        addFilterOption(filterNum, filterId, count, width, dpToPixels(mContext, 8), dpToPixels(mContext, 4), dpToPixels(mContext, 4), mOptionGetStringCallback);
    }

    /**
     * Add a option to filter.
     *
     * @param filterNum                The filter number where you want to add an option.
     * @param filterId                 The unique ID you give to the option.
     * @param count                    The count of this option from the beginning.
     * @param width                    The width of this option.
     * @param leftMargin               The margin to the left.
     * @param rightMargin              The margin to the right.
     * @param upDownMargin             The margin to the up and down.
     * @param mOptionGetStringCallback For you to decide the text on the option.
     */
    public void addFilterOption(final int filterNum, T filterId, int count, int width, int leftMargin, int rightMargin, int upDownMargin, OptionGetStringCallback<T> mOptionGetStringCallback) {
        AutofitTextView autofitTextView = getModifiedTextView(filterNum, filterId, width, leftMargin, rightMargin, upDownMargin);
        mFilters.get(filterNum).addNewFilterButton(new Option(filterId, autofitTextView, count, mOptionGetStringCallback));

        updateAllFilters();
    }

    /**
     * Open the Dropdown.
     */
    public void open() {
        openAllOpeningFilter();

        mFilterContainer.requestFocus();
    }

    /**
     * Close the dropDown.
     */
    public void close() {
        closeAllOpeningFilter();
    }

    public void setSelectedTextColor(int mSelectedTextColor) {
        this.mSelectedTextColor = mSelectedTextColor;
        updateAllFilters();
    }


    public void setUnSelectedTextColor(int mUnSelectedTextColor) {
        this.mUnSelectedTextColor = mUnSelectedTextColor;
        updateAllFilters();
    }

    public void setSelectedBackground(int mSelectedBackground) {
        this.mSelectedBackground = mSelectedBackground;
        updateAllFilters();
    }

    public void setUnSelectedBackground(int mUnSelectedBackground) {
        this.mUnSelectedBackground = mUnSelectedBackground;
        updateAllFilters();
    }

    public void setFilterColCount(int colCount) {
        mFilterColCount = colCount;
        setOpeningFilters(new ArrayList<>(mCurrentOpeningFilters));
    }

    public void setShouldCloseAfterClick(boolean shouldCloseAfterClick) {
        this.mShouldCloseAfterClick = shouldCloseAfterClick;
    }

    public void setChangeColorWhenSelect(boolean changeColorWhenSelect) {
        this.mChangeColorWhenSelect = changeColorWhenSelect;
    }

    public List<T> getAllFilters(int filterNum) {
        List<T> allFilterIds = new ArrayList<>();
        for (int i = 0; i < mFilters.get(filterNum).getOptions().size(); i++) {
            allFilterIds.add(mFilters.get(filterNum).getOptions().get(i).getFilterId());
        }
        return allFilterIds;
    }

    public View getTitleView() {
        return mTitleView;
    }

    private void closeAllOpeningFilter() {
        for (int i = 0; i < mCurrentOpeningFilters.size(); i++) {
            closeAnim(mFilters.get(mCurrentOpeningFilters.get(i)).mContainer);
        }
    }


    public void setFilterOrientation(Orientation orientation) {
        ViewParent viewParent = mFilterContainer.getParent();
        if (viewParent != null) {
            ((ViewGroup) viewParent).removeView(mFilterContainer);
        }
        if (orientation.equals(Orientation.VERTICAL)) {
            mVerticalScrollView.setVisibility(VISIBLE);
            mHorizontalScrollView.setVisibility(GONE);
            mVerticalScrollView.addView(mFilterContainer);
        } else {
            mVerticalScrollView.setVisibility(GONE);
            mHorizontalScrollView.setVisibility(VISIBLE);
            mHorizontalScrollView.addView(mFilterContainer);
        }
        updateAllFilters();
    }

    public void setShouldHideZeroFilters(boolean mShouldHideZeroFilters) {
        this.mShouldHideZeroFilters = mShouldHideZeroFilters;
        updateAllFilters();
    }

    public boolean isShouldHideAll() {
        return mShouldHideAll;
    }

    public boolean isShouldHideZeroFilters() {
        return mShouldHideZeroFilters;
    }

    public void setShouldHideAll(boolean mShouldHideAll) {
        this.mShouldHideAll = mShouldHideAll;
        updateAllFilters();
    }

    private boolean isFiltersValid(List<Integer> checkFilters) {
        for (int i = 0; i < checkFilters.size(); i++) {
            if (mFilters.size() <= checkFilters.get(i)) {
                mFilterCallback.noSuchFilterError(checkFilters.get(i));
                return false;
            }
        }
        return true;
    }

    private void hideAllOpeningContainer() {
        for (int i = 0; i < mCurrentOpeningFilters.size(); i++) {
            mFilters.get(mCurrentOpeningFilters.get(i)).setContainerVisible(false);
        }
    }

    private boolean isFilterOpening(int filterNum) {
        for (int i = 0; i < mCurrentOpeningFilters.size(); i++) {
            if (mCurrentOpeningFilters.get(i) == filterNum) return true;
        }
        return false;
    }

    private void updateFilter(int filterNum) {
        if (mShouldHideZeroFilters) {
            mFilters.get(filterNum).hideZeroOptions();
            mFilters.get(filterNum).showNonZeroOptions();
        } else {
            mFilters.get(filterNum).showAllOptions();
        }
        if (!mShouldHideAll) {
            mFilters.get(filterNum).setOptionVisible(0, true);
        } else {
            mFilters.get(filterNum).setOptionVisible(0, false);
        }
        mFilters.get(filterNum).updateAll();
    }

    private void openGenreSelectorLayout(int openFilter) {
        if (mFilters.get(openFilter).getHeight() == 0) {
            mFilters.get(openFilter).setContainerVisible(true);
//            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            mFilters.get(openFilter).mContainer.setLayoutParams(lp);
            mFilters.get(openFilter).readyToTakeHeight(true);
        } else {
            mFilters.get(openFilter).setContainerVisible(true);
            ValueAnimator va = createDropAnim(mFilters.get(openFilter).mContainer, 0, mFilters.get(openFilter).getHeight());
            va.start();
        }
//        mOpenArrow.setVisibility(View.INVISIBLE);
//        mCloseArrow.setVisibility(View.VISIBLE);
    }

    private void closeAnim(final View view) {
        int origHeight = view.getHeight();
        if (origHeight == 0) {
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//            mFilters.get(mCurrentOpeningFilters).setFilterHeight(view.getMeasuredHeight());
            view.setVisibility(View.GONE);
        } else {
            ValueAnimator va = createDropAnim(view, origHeight, 0);
            va.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                }
            });
            va.start();
        }
    }

    private AutofitTextView getModifiedTextView(final int filterNum, final T filterId, int width, int marginLeft, int marginRight, int marginUpAndDown) {
        width -= (marginLeft + marginRight);

        AutofitTextView autofitTextView = new AutofitTextView(mContext);
        autofitTextView.setTextColor(ContextCompat.getColor(mContext, mUnSelectedTextColor));
        autofitTextView.setBackground(ContextCompat.getDrawable(mContext, mUnSelectedBackground));
        autofitTextView.setTextAlignment(TEXT_ALIGNMENT_CENTER);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT);
        lp.setMargins(marginLeft, marginUpAndDown, marginRight, marginUpAndDown);
        autofitTextView.setLayoutParams(lp);
        autofitTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                optionSelect(filterNum, filterId);
            }
        });
        return autofitTextView;
    }

    private void openAllOpeningFilter() {
        for (int i = 0; i < mCurrentOpeningFilters.size(); i++) {
            openGenreSelectorLayout(mCurrentOpeningFilters.get(i));
        }
    }

    private ValueAnimator createDropAnim(final View view, int start, int end) {
        ValueAnimator va = ValueAnimator.ofInt(start, end);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = value;
                view.setLayoutParams(layoutParams);
            }
        });
        return va;
    }

    //region FilterHolder

    /**
     * Use to hold things we need for a filter.
     */
    class FilterHolder {
        private List<Option> mOptions;
        private FlexboxLayout mContainer;
        private int mHeight = 0;
        private T mCurrentSelected = null;
        private View mEmptyView;

        private boolean mIsRemoved = false;

        FilterHolder(List<Option> mOptions, FlexboxLayout mContainer, View emptyView) {
            this.mOptions = mOptions;
            this.mContainer = mContainer;
            this.mEmptyView = emptyView;

            mContainer.addView(mEmptyView);
            mEmptyView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            mEmptyView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            mEmptyView.setVisibility(GONE);

            setContainerVisible(false);
        }

        Option getFilterButton(T filterId) {
            for (int i = 0; i < mOptions.size(); i++) {
                if (mOptions.get(i).getFilterId().equals(filterId)) {
                    return mOptions.get(i);
                }
            }
            return mOptions.get(0);
        }

        void updateAll() {
            if (mOptions.size() == 0) return;

            boolean isEveryOptionCountZero = true;
            int sum = 0;
            for (int i = 0; i < mOptions.size(); i++) {
                if (i != 0) {
                    // Don't need to add default all.
                    sum += mOptions.get(i).getResultCount();
                }

                if(sum > 0){
                    isEveryOptionCountZero = false;
                }

                if (mCurrentSelected != null && mCurrentSelected.equals(mOptions.get(i).getFilterId())) {
                    mOptions.get(i).invalidate(true);
                } else {
                    mOptions.get(i).invalidate(false);
                }
            }
            mOptions.get(0).setResultCount(sum);

            if (isEveryOptionCountZero && mShouldHideAll) {
                mEmptyView.setVisibility(VISIBLE);
            } else {
                mEmptyView.setVisibility(GONE);
            }

            mContainer.invalidate();
        }

        void setOptionVisible(int pos, boolean visible) {
            if (pos < mOptions.size()) {
                if (visible) {
                    mOptions.get(pos).getAutofitTextView().setVisibility(VISIBLE);
                } else {
                    mOptions.get(pos).getAutofitTextView().setVisibility(GONE);
                }
            }
        }

        void hideZeroOptions() {
            for (int i = 0; i < mOptions.size(); i++) {
                if (mOptions.get(i).getResultCount() == 0) {
                    mOptions.get(i).getAutofitTextView().setVisibility(GONE);
                }
            }
        }

        void showNonZeroOptions() {
            for (int i = 0; i < mOptions.size(); i++) {
                if (mOptions.get(i).getResultCount() != 0) {
                    mOptions.get(i).getAutofitTextView().setVisibility(VISIBLE);
                }
            }
        }

        void showAllOptions() {
            for (int i = 0; i < mOptions.size(); i++) {
                    mOptions.get(i).getAutofitTextView().setVisibility(VISIBLE);
            }
        }

        void removeFilter() {
            mContainer.setVisibility(GONE);
            mIsRemoved = true;
        }

        public List<Option> getOptions() {
            return mOptions;
        }

        void setOptionsDeco(T filterId) {
            for (int i = 0; i < mOptions.size(); i++) {
                if (mOptions.get(i).getFilterId().equals(filterId)) {
                    mOptions.get(i).setSelected();
                } else {
                    mOptions.get(i).setUnSelected();
                }
            }
        }

        void addNewFilterButton(final Option option) {
            mContainer.addView(option.autofitTextView);
            mOptions.add(option);


                    if (isContainerVisible()) {
                        readyToTakeHeight(false);
                    } else {
                        setContainerVisible(true);
                        readyToTakeHeight(true);
                    }
        }

        void setContainerSize(int width) {
            mContainer.getLayoutParams().width = width;
            readyToTakeHeight(isContainerVisible());
        }

        boolean isContainerVisible() {
            return mContainer.getVisibility() == VISIBLE;
        }

        void setContainerVisible(boolean visible) {
            if (mIsRemoved) return;
            if (visible) {
                mContainer.setVisibility(VISIBLE);
            } else {
                mContainer.setVisibility(GONE);
            }
        }

        public int getHeight() {
            return mHeight;
        }

        void readyToTakeHeight(final boolean shouldSetToGoneWhenDone) {
            FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.width = mContainer.getLayoutParams().width;
            mContainer.setLayoutParams(lp);

//            mContainer.setVisibility(GONE);
//            mContainer.setVisibility(VISIBLE);
            mContainer.post(new Runnable() {
                @Override
                public void run() {
                    mHeight = mContainer.getMeasuredHeight();
                    if (shouldSetToGoneWhenDone) {
                        setContainerVisible(false);
                    }
                }
            });

//            mContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                public void onGlobalLayout() {
//                    mContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                    // do the rest of your stuff here
//
//                    mHeight = mContainer.getMeasuredHeight();
//                    if (shouldSetToGoneWhenDone) {
//                        setContainerVisible(false);
//                    }
//                }
//            });
        }

        public void setCurrentSelected(T currentSelected) {
            this.mCurrentSelected = currentSelected;
        }
    }
    //endregion

    //region Option.

    /**
     * Hold option variables we need.
     */
    class Option {
        private T filterId;
        private AutofitTextView autofitTextView;
        private int resultCount;
        private OptionGetStringCallback<T> mOptionGetStringCallback;

        public Option(T filterId, AutofitTextView autofitTextView, int resultCount, OptionGetStringCallback<T> optionGetStringCallback) {
            this.filterId = filterId;
            this.autofitTextView = autofitTextView;
            this.resultCount = resultCount;
            this.mOptionGetStringCallback = optionGetStringCallback;

            autofitTextView.setText(optionGetStringCallback.getString(filterId, resultCount));
        }

        public void invalidate(boolean isSelected) {
            if (isSelected && mChangeColorWhenSelect) {
                autofitTextView.setBackground(ContextCompat.getDrawable(mContext, mSelectedBackground));
                autofitTextView.setTextColor(ContextCompat.getColor(mContext, mSelectedTextColor));
            } else {
                autofitTextView.setBackground(ContextCompat.getDrawable(mContext, mUnSelectedBackground));
                autofitTextView.setTextColor(ContextCompat.getColor(mContext, mUnSelectedTextColor));
            }
            setResultCount(resultCount);
            autofitTextView.invalidate();
        }

        public void setSelected() {
            autofitTextView.setBackground(ContextCompat.getDrawable(mContext, mSelectedBackground));
            autofitTextView.setTextColor(ContextCompat.getColor(mContext, mSelectedTextColor));
        }

        public void setUnSelected() {
            autofitTextView.setBackground(ContextCompat.getDrawable(mContext, mUnSelectedBackground));
            autofitTextView.setTextColor(ContextCompat.getColor(mContext, mUnSelectedTextColor));
        }

        public AutofitTextView getAutofitTextView() {
            return autofitTextView;
        }

        public String getString() {
            return mOptionGetStringCallback.getString(filterId, resultCount);
        }

        public T getFilterId() {
            return filterId;
        }

        public void setFilterId(T filterId) {
            this.filterId = filterId;
        }


        public int getResultCount() {
            return resultCount;
        }

        public void setResultCount(int resultCount) {
            this.resultCount = resultCount;

            autofitTextView.setText(getString());
        }
    }
    //endregion

    //region Utils.
    private int dpToPixels(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private int getScreenWidthPixel(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

//    private void measure(FlexboxLayout flexboxLayout) {
//        if (this.getOrientation() == LinearLayout.VERTICAL) {
//            int h = 0;
//            int w = 0;
//            this.measureChildren(0, 0);
//            for (int i = 0; i < this.getChildCount(); i++) {
//                View v = this.getChildAt(i);
//                h += v.getMeasuredHeight();
//                w = (w < v.getMeasuredWidth()) ? v.getMeasuredWidth() : w;
//            }
//            height = (h < height) ? height : h;
//            width = (w < width) ? width : w;
//        }
//        flexboxLayout.setMeasuredDimension(width, height);
//    }
    //endregion
}