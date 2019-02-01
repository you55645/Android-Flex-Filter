package projects.tryhard.androidflexiblefilter;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import projects.tryhard.androidflexiblefilter.FlexibleFilter.OptionGetStringCallback;

import static projects.tryhard.androidflexiblefilter.FlexibleFilter.mChangeColorWhenSelect;
import static projects.tryhard.androidflexiblefilter.FlexibleFilter.mSelectedBackground;
import static projects.tryhard.androidflexiblefilter.FlexibleFilter.mSelectedTextColor;
import static projects.tryhard.androidflexiblefilter.FlexibleFilter.mUnSelectedBackground;
import static projects.tryhard.androidflexiblefilter.FlexibleFilter.mUnSelectedTextColor;

/**
 * Hold option variables we need.
 */
public class Option<T> {
    private Context mContext;
    private T filterId;
    private AutofitTextView autofitTextView;
    private int resultCount;
    private OptionGetStringCallback<T> mOptionGetStringCallback;

    public Option(Context context, T filterId, AutofitTextView autofitTextView, int resultCount, OptionGetStringCallback<T> optionGetStringCallback) {
        this.mContext = context;
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