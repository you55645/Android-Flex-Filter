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
 * 1. If you use xml to set attributes or you want to use default values, you can use the {@link #init(Context, int, Object, String, FilterClickCallback)}. If you want to do the init settings through code, use {@link #init(Context, int, Object, String, boolean, boolean, Orientation, int, FilterClickCallback)}.
 * --- Basic set up done, you can open up to see how it looks like.
 * 2. After init, you will have at least one filter, you can add filter later also, but if you only planning on using one. You can start to add options. (step 4).
 * 3. If you want to add more filters, use {@link #addFilter(Object, String)}.
 * 4. Adding options to filter you want to add by {@link #addFilterOption(int, Object, String, int, int)} or {@link #addFilterOption(int, Object, String, int, int, int, int, int)}.
 * 5. If you want to update certain option from certain filter, use {@link #updateCertainOption(int, Object, int)}. If you want to update all, use {@link #updateAllFilters()}.
 * --- Set up done.
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
    private FilterClickCallback<T> mFilterClickCallback;
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

    /**
     * If this flag set to true, will hide all the options that is 0.
     */
    private boolean mShouldHideZeroFilters = false;
    /**
     * If this flag set to true, will hide the default added all option.
     */
    private boolean mShouldHideAll = false;
    /**
     * A variable decide how many filters we show at one row. Default is one.
     */
    private int mFilterColCount = 1;
    /**
     * A variable to check where we are in horizontal or vertical mode.
     */
    private Orientation mOrientation = Orientation.VERTICAL;

    /**
     * Callback when option being pressed or called @link #optionSelect(int filterNum, T filterId)
     *
     * @param <T> T should be unique so you can used it to find out which option user select.
     */
    public interface FilterClickCallback<T> {
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
     * @param context             We use to inflate layouts.
     * @param titleLayout         The title you want for the filter, -1 means no title.
     * @param allT                A unique ID for the default all option.
     * @param defaultFormat       A String for the default all option.
     * @param hideAll             Should we hide the default all option?
     * @param hideZeroFilters     Should we hide the options that is 0?
     * @param orientation         The mOrientation of the filters' layout.
     * @param colCount            How many filters we show at one row. Default is one.
     * @param filterClickCallback Callback when option being pressed or called @link #optionSelect(int filterComplex, T filterId).
     * @return A number for default filter, use it when you want to update, show or hide certain filter.
     */
    public int init(Context context, @LayoutRes int titleLayout, final T allT, String defaultFormat, boolean hideAll, boolean hideZeroFilters, Orientation orientation, int colCount, FilterClickCallback<T> filterClickCallback) {
        int filterNum = init(context, titleLayout, allT, defaultFormat, filterClickCallback);

        setShouldHideZeroFilters(hideZeroFilters);
        setShouldHideAll(hideAll);
        setFilterOrientation(orientation);
        setFilterColCount(colCount);

        return filterNum;
    }

    /**
     * Use to init the whole filter with default.
     *
     * @param context             We use to inflate layouts.
     * @param titleLayout         The title you want for the filter, -1 means no title, 0 means default title.
     * @param allT                A unique ID for the default all option.
     * @param defaultFormat       A String for the default all option.
     * @param filterClickCallback Callback when option being pressed or called @link #optionSelect(int filterComplex, T filterId).
     * @return A number for default filter, use it when you want to update, show or hide certain filter.
     */
    public int init(Context context, @LayoutRes int titleLayout, final T allT, String defaultFormat, FilterClickCallback<T> filterClickCallback) {
        inflate(getContext(), R.layout.filter_layout, this);
        mFilterClickCallback = filterClickCallback;
        mContext = context;
        mTitleContainer = findViewById(R.id.filter_title_container);
        mFilterContainer = findViewById(R.id.filter_filter_container);
        mHorizontalScrollView = findViewById(R.id.filter_horizontalScrollView);
        mVerticalScrollView = findViewById(R.id.filter_verticalScrollView);

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

        addFilter(allT, defaultFormat);

        mCurrentOpeningFilters.add(0);

        setFilterOrientation(mOrientation);
        return 0;
    }
    //endregion

    /**
     * @param defaultT      A unique ID for the default all option.
     * @param defaultFormat A String for the default all option.
     * @return A number for this filter, use it when you want to update, show or hide certain filter.
     */
    public int addFilter(final T defaultT, String defaultFormat) {
        FlexboxLayout flexboxLayout = new FlexboxLayout(mContext);
        FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        flexboxLayout.setLayoutParams(lp);
        flexboxLayout.setFlexDirection(FlexDirection.ROW);
        flexboxLayout.setFlexWrap(FlexWrap.WRAP);
        flexboxLayout.setJustifyContent(JustifyContent.FLEX_START);
        flexboxLayout.setAlignItems(AlignItems.FLEX_START);
        flexboxLayout.setAlignContent(AlignContent.FLEX_START);

        mFilterContainer.addView(flexboxLayout);

        mFilters.add(new FilterHolder(new ArrayList<Option>(), flexboxLayout));

        addFilterOption(mFilters.size() - 1, defaultT, defaultFormat, 0, getScreenWidthPixel(mContext), dpToPixels(mContext, 8), dpToPixels(mContext, 8), dpToPixels(mContext, 8));

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
            mFilters.get(openFilters.get(i)).setContainerSize(getScreenWidthPixel(mContext) / mFilterColCount);
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
            mFilters.get(filterNum).setOptionsDeco(filterId);
            close();
            updateFilter(filterNum);

            mFilterClickCallback.filterOptionClicked(mTitleView, filterNum, filterId);
        } else {
            // not in should we add?
            mFilterClickCallback.filterOptionNotExistError();
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
     * Add a option to filter with left margin 8dp, right 0dp, up and down 8dp.
     *
     * @param filterNum    The filter number where you want to add an option.
     * @param filterId     The unique ID you give to the option.
     * @param formatString The display text format you will have on the option.
     * @param count        The count of this option from the beginning.
     * @param width        The width of this option.
     */
    public void addFilterOption(int filterNum, T filterId, String formatString, int count, int width) {
        addFilterOption(filterNum, filterId, formatString, count, width, dpToPixels(mContext, 8), 0, dpToPixels(mContext, 8));
    }

    /**
     * Add a option to filter.
     *
     * @param filterNum    The filter number where you want to add an option.
     * @param filterId     The unique ID you give to the option.
     * @param formatString The display text format you will have on the option.
     * @param count        The count of this option from the beginning.
     * @param width        The width of this option.
     * @param leftMargin   The margin to the left.
     * @param rightMargin  The margin to the right.
     * @param upDownMargin The margin to the up and down.
     */
    public void addFilterOption(final int filterNum, T filterId, String formatString, int count, int width, int leftMargin, int rightMargin, int upDownMargin) {
        AutofitTextView autofitTextView = getModifiedTextView(filterNum, filterId, width, leftMargin, rightMargin, upDownMargin);
        mFilters.get(filterNum).addNewFilterButton(new Option(filterId, formatString, autofitTextView, count));

        updateAllFilters();
    }

    /**
     * Open the Dropdown.
     */
    public void open() {
        openAllOpeningFilter();
    }

    /**
     * Close the dropDown.
     */
    public void close() {
        closeAllOpeningFilter();
    }

    private void closeAllOpeningFilter() {
        for (int i = 0; i < mCurrentOpeningFilters.size(); i++) {
            closeAnim(mFilters.get(mCurrentOpeningFilters.get(i)).mContainer);
        }
    }

    public void setSelectedTextColor(int mSelectedTextColor) {
        this.mSelectedTextColor = mSelectedTextColor;
    }


    public void setUnSelectedTextColor(int mUnSelectedTextColor) {
        this.mUnSelectedTextColor = mUnSelectedTextColor;
    }

    public void setSelectedBackground(int mSelectedBackground) {
        this.mSelectedBackground = mSelectedBackground;
    }

    public void setUnSelectedBackground(int mUnSelectedBackground) {
        this.mUnSelectedBackground = mUnSelectedBackground;
    }

    public void setFilterColCount(int colCount) {
        mFilterColCount = colCount;
        setOpeningFilters(new ArrayList<>(mCurrentOpeningFilters));
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

    public void setShouldHideAll(boolean mShouldHideAll) {
        this.mShouldHideAll = mShouldHideAll;
        updateAllFilters();
    }

    private boolean isFiltersValid(List<Integer> checkFilters) {
        for (int i = 0; i < checkFilters.size(); i++) {
            if (mFilters.size() <= checkFilters.get(i)) {
                mFilterClickCallback.noSuchFilterError(checkFilters.get(i));
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

    private void updateFilter(int filter) {
        if (mShouldHideZeroFilters) {
            mFilters.get(filter).hideZeroOptions();
        }
        if (!mShouldHideAll) {
            mFilters.get(filter).setOptionVisible(0, true);
            mFilters.get(filter).updateAll();
        } else {
            mFilters.get(filter).setOptionVisible(0, false);
        }
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

        private boolean mIsRemoved = false;

        FilterHolder(List<Option> mOptions, FlexboxLayout mContainer) {
            this.mOptions = mOptions;
            this.mContainer = mContainer;

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
            int sum = 0;
            for (int i = 1; i < mOptions.size(); i++) {
                sum += mOptions.get(i).getResultCount();
            }
            mOptions.get(0).setResultCount(sum);
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
                } else {
                    mOptions.get(i).getAutofitTextView().setVisibility(VISIBLE);
                }
            }
        }

        void removeFilter() {
            mContainer.setVisibility(GONE);
            mIsRemoved = true;
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
            this.mContainer.addView(option.autofitTextView);
            this.mOptions.add(option);

            mContainer.post(new Runnable() {
                @Override
                public void run() {
                    if (isContainerVisible()) {
                        readyToTakeHeight(false);
                    } else {
                        setContainerVisible(true);
                        readyToTakeHeight(true);
                    }
                }
            });
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
            mContainer.post(new Runnable() {
                @Override
                public void run() {
                    mHeight = mContainer.getMeasuredHeight();
                    if (shouldSetToGoneWhenDone) {
                        setContainerVisible(false);
                    }
                }
            });
        }
    }
    //endregion

    //region Option.

    /**
     * Hold option variables we need.
     */
    class Option {
        private T filterId;
        private String filterStringFormat;
        private AutofitTextView autofitTextView;
        private int resultCount;

        public Option(T filterId, String filterStringFormat, AutofitTextView autofitTextView, int resultCount) {
            this.filterId = filterId;
            this.filterStringFormat = filterStringFormat;
            this.autofitTextView = autofitTextView;
            this.resultCount = resultCount;

            autofitTextView.setText(String.format(filterStringFormat, resultCount));
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
            return String.format(Locale.CHINESE, filterStringFormat, resultCount);
        }

        public T getFilterId() {
            return filterId;
        }

        public void setFilterId(T filterId) {
            this.filterId = filterId;
        }

        public void setFilterStringFormat(String filterStringFormat) {
            this.filterStringFormat = filterStringFormat;

            autofitTextView.setText(getString());
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
        int pxlHeight = displayMetrics.heightPixels;
        int pxlWidth = displayMetrics.widthPixels;
        return pxlWidth;
    }
    //endregion

}