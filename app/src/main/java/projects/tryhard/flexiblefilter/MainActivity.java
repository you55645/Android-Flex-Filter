package projects.tryhard.flexiblefilter;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import projects.tryhard.androidflexiblefilter.FlexibleFilter;

public class MainActivity extends AppCompatActivity {

    FlexibleFilter<String> mFilter;
    Button mToggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToggleButton = findViewById(R.id.filter_toggle);
        mFilter = findViewById(R.id.filter);


        int theFirstFilterId = mFilter.init(this, 0, "0", "All(%d)", new FlexibleFilter.FilterClickCallback<String>() {
            TextView title;

            @Override
            public void filterOptionClicked(View titleView, int filterNum, String filterId) {
                title = titleView.findViewById(R.id.filter_title);
                title.setText("Selecting:" + filterId);
            }

            @Override
            public void filterOptionNotExistError() {
                Toast.makeText(MainActivity.this, "This option does not exist!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void noSuchFilterError(int notExistFilterNum) {
                Toast.makeText(MainActivity.this, "This Filter number: " + notExistFilterNum + " does not exist!", Toast.LENGTH_SHORT).show();
            }
        });

        mFilter.addFilterOption(0, "A", "A(%d)", 0, getScreenWidthPixel(this) / 2);
        mFilter.addFilterOption(0, "B", "B(%d)", 41, getScreenWidthPixel(this) / 3);
        mFilter.addFilterOption(0, "C", "C(%d)", 8, getScreenWidthPixel(this) / 3);

        int theSecondFilterId = mFilter.addFilter("A", "All(%d)");
        mFilter.addFilterOption(1, "Filter1", "Filter1 C(%d)", 1, getScreenWidthPixel(this) / 3);
//
//        filter.addFilter("888", "All(%d)");
//        filter.addFilterOption(2, "A", "A(%d)", 5, CommonUtils.getScreenWidthPixel(getContext()) / 3);
//        filter.addFilterOption(2, "B", "B(%d)", 6, CommonUtils.getScreenWidthPixel(getContext()) / 3);
//        filter.addFilterOption(2, "C", "C(%d)", 7, CommonUtils.getScreenWidthPixel(getContext()) / 3);
//        filter.addFilterOption(2, "A", "A(%d)", 5, CommonUtils.getScreenWidthPixel(getContext()) / 3);
//        filter.addFilterOption(2, "B", "B(%d)", 6, CommonUtils.getScreenWidthPixel(getContext()) / 3);
//        filter.addFilterOption(2, "C", "C(%d)", 7, CommonUtils.getScreenWidthPixel(getContext()) / 3);

        mFilter.setOpeningFilters(new ArrayList<Integer>(){{
            add(0);
            add(1);
        }});

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mFilter.open();
            }
        }, 100);

        mToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mFilter.isCurrentOpen()) {
                    mToggleButton.setText("Close");
                    mFilter.open();
                } else {
                    mToggleButton.setText("Open");
                    mFilter.close();
                }
            }
        });
    }


    private int getScreenWidthPixel(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int pxlWidth = displayMetrics.widthPixels;
        return pxlWidth;
    }
}
