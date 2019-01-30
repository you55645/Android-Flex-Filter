package projects.tryhard.androidflexiblefilter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
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

public class FlexibleFilter<T> extends LinearLayout {
    /**
     * A Enum used to decide the orientation of the whole Filter Layout
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
    private List<Integer> mCurrentOpeningComplex;

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
     * Callback when option being pressed or called @link #optionSelect(int filterComplex, T filterId)
     * @param <T> T should be unique so you can used it to find out which option user select.
     */
    public interface FilterClickCallback<T> {
        /**
         * Call when option being pressed or called @link #optionSelect(int filterComplex, T filterId)
         * @param titleView The title view you passed in when init.
         * @param filterComplex Let you know which filter user select.
         * @param filterId Option unique value.
         */
        void filterOptionClicked(View titleView, int filterComplex, T filterId);
    }

    //region View constructors.
    public FlexibleFilter(Context context) {
        super(context);
    }

    public FlexibleFilter(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FlexibleFilter(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FlexibleFilter(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    //endregion

    //region Init.
    /**
     * Use to init the whole filter with more detail.
     * @param context We use to inflate layouts.
     * @param titleLayout The title you want for the filter, -1 means no title.
     * @param allT A unique ID for the default all option.
     * @param defaultFormat A String for the default all option.
     * @param hideAll Should we hide the default all option?
     * @param hideZeroFilters Should we hide the options that is 0?
     * @param orientation The orientation of the filters' layout.
     * @param colCount How many filters we show at one row. Default is one.
     * @param filterClickCallback Callback when option being pressed or called @link #optionSelect(int filterComplex, T filterId).
     */
    public void init(Context context, @LayoutRes int titleLayout, final T allT, String defaultFormat, boolean hideAll, boolean hideZeroFilters, Orientation orientation, int colCount, FilterClickCallback filterClickCallback) {
        init(context, titleLayout, allT, defaultFormat, filterClickCallback);

        setShouldHideZeroFilters(hideZeroFilters);
        setShouldHideAll(hideAll);
        setFilterOrientation(orientation);
        setFilterColCount(colCount);
    }

    /**
     * Use to init the whole filter with default.
     * @param context We use to inflate layouts.
     * @param titleLayout The title you want for the filter, -1 means no title.
     * @param allT A unique ID for the default all option.
     * @param defaultFormat A String for the default all option.
     * @param filterClickCallback Callback when option being pressed or called @link #optionSelect(int filterComplex, T filterId).
     */
    public void init(Context context, @LayoutRes int titleLayout, final T allT, String defaultFormat, FilterClickCallback filterClickCallback) {
        inflate(getContext(), R.layout.filter_layout, this);
        mFilterClickCallback = filterClickCallback;
        mContext = context;
        mTitleContainer = findViewById(R.id.filter_title_container);
        mFilterContainer = findViewById(R.id.filter_filter_container);
        mHorizontalScrollView = findViewById(R.id.filter_horizontalScrollView);
        mVerticalScrollView = findViewById(R.id.filter_verticalScrollView);

        mCurrentOpeningComplex = new ArrayList<>();

        if(titleLayout != -1){
            mTitleView = LayoutInflater.from(context).inflate(titleLayout, null);
            mTitleView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isCurrentOpen()) {
                        openFilterContainer();
                    } else {
                        closeFilterContainer();
                    }
                }
            });
            mTitleContainer.addView(mTitleView);
        }

        mFilters = new ArrayList<>();

        addFilter(allT, defaultFormat);

        mCurrentOpeningComplex.add(0);
    }
    //endregion

    public void addFilter(final T defaultT, String defaultFormat) {
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
    }

    public void updateAllFilters() {
        for (int i = 0; i < mFilters.size(); i++) {
            updateFilterComplex(i);
        }
    }

    public boolean isCurrentOpen() {
        return mFilters.get(mCurrentOpeningComplex.get(0)).isContainerVisible();
    }

    public void setCurrentOpeningComplex(List<Integer> openComplex) {
        if (!isComplexValid(openComplex)) return;
        if (isCurrentOpen()) {
            hideAllOpeningContainer();
        }

        for (int i = 0; i < openComplex.size(); i++) {
            mFilters.get(openComplex.get(i)).setContainerVisible(isCurrentOpen());
            mFilters.get(openComplex.get(i)).setContainerSize(getScreenWidthPixel(mContext) / mFilterColCount);
        }

        mCurrentOpeningComplex.clear();
        mCurrentOpeningComplex = openComplex;
        updateAllFilters();
    }

    public List<Integer> getCurrentOpeningComplex() {
        return mCurrentOpeningComplex;
    }

    public void optionSelect(int filterComplex, T filterId) {

        if (isSelectedFilterOpening(filterComplex)) {
//            mCurrentOpeningComplex = filterComplex;
            mFilters.get(filterComplex).setOptionsDeco(filterId);
            closeFilterContainer();
            updateFilterComplex(filterComplex);

            mFilterClickCallback.filterOptionClicked(mTitleView, filterComplex, filterId);
        } else {
            // not in should we add?
        }
    }

    public void updateCertainOption(int filterComplex, T filterId, int count) {
        mFilters.get(filterComplex).getFilterButton(filterId).setResultCount(count);
        updateFilterComplex(filterComplex);
    }

    public void removeFilter(int filterComplex, T filterId) {
        mFilters.get(filterComplex).removeFilter(filterId);
        updateAllFilters();
    }

    public void addFilterOption(int filterComplex, T filterId, String formatString, int count, int width) {
        addFilterOption(filterComplex, filterId, formatString, count, width, dpToPixels(mContext, 8), 0, dpToPixels(mContext, 8));
    }

    public void addFilterOption(final int filterComplex, T filterId, String formatString, int count, int width, int leftMargin, int rightMargin, int upDownMargin) {
        AutofitTextView autofitTextView = getModifiedTextView(filterComplex, filterId, width, leftMargin, rightMargin, upDownMargin);
        mFilters.get(filterComplex).addNewFilterButton(new Option(filterId, formatString, autofitTextView, count));

        updateAllFilters();
    }

    public void openFilterContainer() {
        openAllOpeningFilter();
    }

    public void closeFilterContainer() {
        closeAllOpeningFilter();
    }

    private void closeAllOpeningFilter() {
        for (int i = 0; i < mCurrentOpeningComplex.size(); i++) {
            closeAnim(mFilters.get(mCurrentOpeningComplex.get(i)).mContainer);
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
        setCurrentOpeningComplex(new ArrayList<>(mCurrentOpeningComplex));
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

    private boolean isComplexValid(List<Integer> openComplex) {
        for (int i = 0; i < openComplex.size(); i++) {
            if (mFilters.size() <= openComplex.get(i)) {
                return false;
            }
        }
        return true;
    }

    private void hideAllOpeningContainer() {
        for (int i = 0; i < mCurrentOpeningComplex.size(); i++) {
            mFilters.get(mCurrentOpeningComplex.get(i)).setContainerVisible(false);
        }
    }

    private boolean isSelectedFilterOpening(int filterComplex) {
        for (int i = 0; i < mCurrentOpeningComplex.size(); i++) {
            if (mCurrentOpeningComplex.get(i) == filterComplex) return true;
        }
        return false;
    }

    private void updateFilterComplex(int filterComplex) {
        if (mShouldHideZeroFilters) {
            mFilters.get(filterComplex).hideZeroOptions();
        }
        if (!mShouldHideAll) {
            mFilters.get(filterComplex).setOptionVisible(0, true);
            mFilters.get(filterComplex).updateAll();
        } else {
            mFilters.get(filterComplex).setOptionVisible(0, false);
        }
    }

    public void openGenreSelectorLayout(int openFilterComplex) {
        if (mFilters.get(openFilterComplex).getHeight() == 0) {
            mFilters.get(openFilterComplex).setContainerVisible(true);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mFilters.get(openFilterComplex).mContainer.setLayoutParams(lp);
            mFilters.get(openFilterComplex).readyToTakeHeight(true);
        } else {
            mFilters.get(openFilterComplex).setContainerVisible(true);
            ValueAnimator va = createDropAnim(mFilters.get(openFilterComplex).mContainer, 0, mFilters.get(openFilterComplex).getHeight());
            va.start();
        }
//        mOpenArrow.setVisibility(View.INVISIBLE);
//        mCloseArrow.setVisibility(View.VISIBLE);
    }

    private void closeAnim(final View view) {
        int origHeight = view.getHeight();
        if (origHeight == 0) {
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//            mFilters.get(mCurrentOpeningComplex).setFilterHeight(view.getMeasuredHeight());
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

    private AutofitTextView getModifiedTextView(final int filterComplex, final T filterId, int width, int marginLeft, int marginRight, int marginUpAndDown) {
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
                optionSelect(filterComplex, filterId);
            }
        });
        return autofitTextView;
    }

    private void openAllOpeningFilter() {
        for (int i = 0; i < mCurrentOpeningComplex.size(); i++) {
            openGenreSelectorLayout(mCurrentOpeningComplex.get(i));
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

        void setOptionVisible(int pos, boolean visible){
            if(pos < mOptions.size()){
                if(visible){
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

        void removeFilter(T filterId) {
            for (int i = 0; i < mOptions.size(); i++) {
                if (mOptions.get(i).getFilterId().equals(filterId)) {
                    mOptions.remove(i);
                }
            }
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