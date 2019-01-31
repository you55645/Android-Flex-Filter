package projects.tryhard.flexiblefilter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import projects.tryhard.androidflexiblefilter.FlexibleFilter;

public class MainActivity extends AppCompatActivity {

    FlexibleFilter<String> mFilter1;
    FlexibleFilter<String> mFilter2;
    boolean filter2ShowCount = true;
    Button mOpenFilter2Button;
    FlexibleFilter<String> mFilter3;
    Button mOpenFilter3Button;
    FlexibleFilter<String> mFilter4;
    List<Human> mHumanList;
    RecyclerView mHumanRV;
    HumanAdapter mHumanAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFilter1 = findViewById(R.id.filter1);
        mFilter2 = findViewById(R.id.filter2);
        mOpenFilter2Button = findViewById(R.id.filter2_open);
        mFilter3 = findViewById(R.id.filter3);
        mOpenFilter3Button = findViewById(R.id.filter3_open);
        mFilter4 = findViewById(R.id.filter4);
        mHumanRV = findViewById(R.id.filter4_rv);

        FlexibleFilter.OptionGetStringCallback<String> optionGetStringCallback1 = new FlexibleFilter.OptionGetStringCallback<String>() {
            @Override
            public String getString(String filterId, int count) {
                return String.format(Locale.CHINESE, "%s(%d)", filterId, count);
            }
        };


        //region Filter 1
        int filter1filterNum = mFilter1.init(this, 0, "ALL", new FlexibleFilter.FilterCallback<String>() {
            @Override
            public void filterOptionClicked(View titleView, int filterNum, String filterId) {
                TextView title = titleView.findViewById(R.id.filter_title);
                title.setText("Current selecting: " + filterId);
            }

            @Override
            public void filterOptionNotExistError() {

            }

            @Override
            public void noSuchFilterError(int notExistFilterNum) {

            }
        });
        mFilter1.addFilterOption(filter1filterNum, "A", 1, getScreenWidthPixel(this) / 3, optionGetStringCallback1);
        mFilter1.addFilterOption(filter1filterNum, "B", 2, getScreenWidthPixel(this) / 3, optionGetStringCallback1);
        mFilter1.addFilterOption(filter1filterNum, "C", 3, getScreenWidthPixel(this) / 3, optionGetStringCallback1);
        //endregion

        //region Filter 2
        final FlexibleFilter.OptionGetStringCallback<String> optionGetStringCallback2 = new FlexibleFilter.OptionGetStringCallback<String>() {
            @Override
            public String getString(String filterId, int count) {
                String text = filterId;
                if (filter2ShowCount) {
                    text += "-" + count;
                }
                return text;
            }

        };

        final int filter2filterNum = mFilter2.init(this, R.layout.filter_custom_title_for_2, "ALL", new FlexibleFilter.FilterCallback<String>() {
            @Override
            public void filterOptionClicked(View titleView, int filterNum, String filterId) {
                switch (filterId) {
                    case "ToggleTitle":
                        if (titleView.getVisibility() == View.GONE) {
                            titleView.setVisibility(View.VISIBLE);
                        } else {
                            titleView.setVisibility(View.GONE);
                        }
                        break;
                    case "ToggleAll":
                        mFilter2.setShouldHideAll(!mFilter2.isShouldHideAll());
                        break;
                    case "AddOp1":
                        addAnOptionToFilter2(1, optionGetStringCallback2);
                        mFilter2.updateAllFilters();
                        break;
                    case "AddOp0":
                        addAnOptionToFilter2(0, optionGetStringCallback2);
                        mFilter2.updateAllFilters();
                        break;
                    case "ToggleTextCount":
                        toggleFilter2ShouldShowCount();
                        mFilter2.updateAllFilters();
                        break;
                    case "Toggle0Count":
                        mFilter2.setShouldHideZeroFilters(!mFilter2.isShouldHideZeroFilters());
                        break;
                }
            }

            @Override
            public void filterOptionNotExistError() {

            }

            @Override
            public void noSuchFilterError(int notExistFilterNum) {

            }
        });
        mFilter2.getTitleView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFilter2.isCurrentOpen()) {
                    mFilter2.close();
                } else {
                    mFilter2.open();
                }
            }
        });
        mOpenFilter2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFilter2.open();
            }
        });
        mFilter2.setShouldCloseAfterClick(false);

        mFilter2.addFilterOption(filter2filterNum, "ToggleTitle", 1, getScreenWidthPixel(this) / 2, optionGetStringCallback2);
        mFilter2.addFilterOption(filter2filterNum, "ToggleAll", 1, getScreenWidthPixel(this) / 2, optionGetStringCallback2);
        mFilter2.addFilterOption(filter2filterNum, "AddOp1", 1, getScreenWidthPixel(this) / 2, optionGetStringCallback2);
        mFilter2.addFilterOption(filter2filterNum, "AddOp0", 1, getScreenWidthPixel(this) / 2, optionGetStringCallback2);
        mFilter2.addFilterOption(filter2filterNum, "ToggleTextCount", 1, getScreenWidthPixel(this) / 2, optionGetStringCallback2);
        mFilter2.addFilterOption(filter2filterNum, "Toggle0Count", 1, getScreenWidthPixel(this) / 2, optionGetStringCallback2);
        //endregion

        //region Filter 3
        final FlexibleFilter.OptionGetStringCallback<String> optionGetStringCallback3 = new FlexibleFilter.OptionGetStringCallback<String>() {
            @Override
            public String getString(String filterId, int count) {
                return filterId;
            }

        };

        final int filter3filterNum = mFilter3.init(this, R.layout.filter_custom_title_for_3, "ALL", -1, true, true, FlexibleFilter.Orientation.HORIZONTAL, -1, false, false, new FlexibleFilter.FilterCallback<String>() {
            @Override
            public void filterOptionClicked(View titleView, int filterNum, String filterId) {
                EditText editText = titleView.findViewById(R.id.filter3_searchbar);
                editText.setText(filterId);
            }

            @Override
            public void filterOptionNotExistError() {

            }

            @Override
            public void noSuchFilterError(int notExistFilterNum) {

            }
        });
        mFilter3.addFilterOption(filter3filterNum, "AAA", 1, getScreenWidthPixel(this) * 2 / 5, optionGetStringCallback3);
        mFilter3.addFilterOption(filter3filterNum, "AAB", 1, getScreenWidthPixel(this) * 2 / 5, optionGetStringCallback3);
        mFilter3.addFilterOption(filter3filterNum, "ABB", 1, getScreenWidthPixel(this) * 2 / 5, optionGetStringCallback3);
        mFilter3.addFilterOption(filter3filterNum, "BBB", 1, getScreenWidthPixel(this) * 2 / 5, optionGetStringCallback3);
        mFilter3.addFilterOption(filter3filterNum, "BBA", 1, getScreenWidthPixel(this) * 2 / 5, optionGetStringCallback3);
        mFilter3.addFilterOption(filter3filterNum, "BAA", 1, getScreenWidthPixel(this) * 2 / 5, optionGetStringCallback3);
        mFilter3.addFilterOption(filter3filterNum, "BAB", 1, getScreenWidthPixel(this) * 2 / 5, optionGetStringCallback3);
        mFilter3.addFilterOption(filter3filterNum, "ABA", 1, getScreenWidthPixel(this) * 2 / 5, optionGetStringCallback3);

        View filter3Title = mFilter3.getTitleView();
        final EditText editText = filter3Title.findViewById(R.id.filter3_searchbar);
        AppCompatImageView searchButtom = filter3Title.findViewById(R.id.filter3_search_button);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                List<String> filter3AllFilterIds = mFilter3.getAllFilters(filter3filterNum);
                String key = editable.toString().toUpperCase();
                for (int i = 0; i < filter3AllFilterIds.size(); i++) {
                    if (!filter3AllFilterIds.get(i).contains(key)) {
                        mFilter3.updateCertainOption(filter3filterNum, filter3AllFilterIds.get(i), 0);
                    } else {
                        mFilter3.updateCertainOption(filter3filterNum, filter3AllFilterIds.get(i), 1);
                    }
                }
            }
        });

        searchButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "You searched " + editText.getText(), Toast.LENGTH_SHORT).show();
                editText.setText("");
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mFilter3.open();
            }
        }, 4000);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) mFilter3.open();
            }
        });
        //endregion

        mHumanList = new ArrayList<>();
        mHumanList.add(new Human("Tim", "Men", 17));
        mHumanList.add(new Human("Dean", "Men", 24));
        mHumanList.add(new Human("Sam", "Men", 36));
        mHumanList.add(new Human("Flake", "Men", 38));
        mHumanList.add(new Human("Nancy", "Women", 2));
        mHumanList.add(new Human("Amy", "Women", 19));
        mHumanList.add(new Human("Lily", "Women", 29));

        mHumanRV.setLayoutManager(new LinearLayoutManager(this));
        mHumanAdapter = new HumanAdapter(this, mHumanList);
        mHumanRV.setAdapter(mHumanAdapter);

        mFilter4.init(this, R.layout.filter_custom_title_for_4, "ALL", new FlexibleFilter.FilterCallback<String>() {
            private String currentFilter1Id = "";
            private String currentFilter2Id = "";

            @Override
            public void filterOptionClicked(View titleView, int filterNum, String filterId) {
                TextView title = titleView.findViewById(R.id.filter_title);
                if (filterNum == 0) {
                    currentFilter1Id = filterId;
                    List<Human> manLists = getSexList("Men");
                    List<Human> womanLists = getSexList("Women");
                    switch (filterId) {
                        case "ALL":
                            mFilter4.updateCertainOption(1, "0-20", get0To20Count(mHumanList));
                            mFilter4.updateCertainOption(1, "20-", getAbove20Count(mHumanList));
                            mHumanAdapter.replace(mHumanList);
                            break;
                        case "Men":
                            mFilter4.updateCertainOption(1, "0-20", get0To20Count(manLists));
                            mFilter4.updateCertainOption(1, "20-", getAbove20Count(manLists));
                            mHumanAdapter.replace(manLists);
                            break;
                        case "Women":
                            mFilter4.updateCertainOption(1, "0-20", get0To20Count(womanLists));
                            mFilter4.updateCertainOption(1, "20-", getAbove20Count(womanLists));
                            mHumanAdapter.replace(womanLists);
                            break;
                    }
                } else if (filterNum == 1) {
                    currentFilter2Id = filterId;
                    switch (filterId){
                        "0-20"
                        "20-"
                    }
                }
                title.setText(String.format(Locale.CHINESE, "Filter1: %s, Filter2: %s", currentFilter1Id, currentFilter2Id));
            }

            @Override
            public void filterOptionNotExistError() {

            }

            @Override
            public void noSuchFilterError(int notExistFilterNum) {

            }
        });
        mFilter4.addFilter("ALL", -1);
        mFilter4.setFilterColCount(2);
        mFilter4.setOpeningFilters(new ArrayList<Integer>() {{
            add(0);
            add(1);
        }});

        FlexibleFilter.OptionGetStringCallback<String> optionGetStringCallback4 = new FlexibleFilter.OptionGetStringCallback<String>() {
            @Override
            public String getString(String filterId, int count) {
                return String.format(Locale.CHINESE, "%s(%d)", filterId, count);
            }
        };

        mFilter4.addFilterOption(0, "Men", getSexList("Men").size(), getScreenWidthPixel(this) / 2, optionGetStringCallback4);
        mFilter4.addFilterOption(0, "Women", getSexList("Women").size(), getScreenWidthPixel(this) / 2, optionGetStringCallback4);

        mFilter4.addFilterOption(1, "0-20", get0To20Count(mHumanList), getScreenWidthPixel(this) / 2, optionGetStringCallback4);
        mFilter4.addFilterOption(1, "20-", getAbove20Count(mHumanList), getScreenWidthPixel(this) / 2, optionGetStringCallback4);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mFilter4.open();
            }
        }, 1000);

        mFilter4.setShouldCloseAfterClick(false);
    }

    private void addAnOptionToFilter2(int count, FlexibleFilter.OptionGetStringCallback<String> optionGetStringCallback2) {
        mFilter2.addFilterOption(0, (mFilter2.getAllFilters(0).size() - 6) + "", count, getScreenWidthPixel(this) / 3, optionGetStringCallback2);
    }

    private void toggleFilter2ShouldShowCount() {
        filter2ShowCount = !filter2ShowCount;
    }

    private List<Human> getSexList(String sex) {
        List<Human> list = new ArrayList<>();
        for (int i = 0; i < mHumanList.size(); i++) {
            if (mHumanList.get(i).getSex().equals(sex)) list.add(mHumanList.get(i));
        }
        return list;
    }

    private int get0To20Count(List<Human> humanList) {
        int count = 0;
        for (int i = 0; i < humanList.size(); i++) {
            if (humanList.get(i).getAge() <= 20) count++;
        }
        return count;
    }

    private int getAbove20Count(List<Human> humanList) {
        int count = 0;
        for (int i = 0; i < humanList.size(); i++) {
            if (humanList.get(i).getAge() > 20) count++;
        }
        return count;
    }

    private class Human {
        private String name;
        private String sex;
        private int age;

        public Human(String name, String sex, int age) {
            this.name = name;
            this.sex = sex;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public String getSex() {
            return sex;
        }

        public int getAge() {
            return age;
        }
    }

    public class HumanAdapter extends RecyclerView.Adapter<HumanAdapter.ViewHolder> {
        Context mContext;
        List<Human> mHumanList;

        public HumanAdapter(Context mContext, List<Human> mHumanList) {
            this.mContext = mContext;
            this.mHumanList = mHumanList;
        }

        public void replace(List<Human> newList){
            mHumanList = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.filter_item_for_4, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            Human human = mHumanList.get(i);
            viewHolder.mName.setText(human.getName());
            viewHolder.mSex.setText(human.getSex());
            viewHolder.mAge.setText(human.getAge() + "");
        }

        @Override
        public int getItemCount() {
            return mHumanList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView mName;
            TextView mSex;
            TextView mAge;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                mName = itemView.findViewById(R.id.human_name);
                mSex = itemView.findViewById(R.id.human_sex);
                mAge = itemView.findViewById(R.id.human_age);
            }
        }
    }

    private int getScreenWidthPixel(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }
}
