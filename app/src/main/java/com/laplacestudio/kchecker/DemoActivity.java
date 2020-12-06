package com.laplacestudio.kchecker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DemoActivity extends AppCompatActivity {

    private static String tag = "Demo";

    public static final String FRAGMENT_ARG_TITLE = "FRAGMENT TITLE";

    public static final int REQ_CODE_CONTROL_IMAGE = 1;
    public static final int REQ_CODE_PANEL_IMAGE =2 ;

    TabLayout demoTabLayout;
    ViewPager demoViewPager;

    List<Fragment> demoFragments;
    public FragmentTestClassifier fragmentTestClassifier=FragmentTestClassifier.newInstance();
    public FragmentCutAndClassify fragmentCutAndClassify = FragmentCutAndClassify.newInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        demoTabLayout = findViewById(R.id.tab_layout);
        demoViewPager = findViewById(R.id.view_pager);

        demoFragments = initFragments();
        DemoPagerAdapter adapter = new DemoPagerAdapter(getSupportFragmentManager(), demoFragments);
        initTabLayout(adapter);

        demoViewPager.setAdapter(adapter);
        demoTabLayout.setupWithViewPager(demoViewPager);

    }

    private void initTabLayout(DemoPagerAdapter adapter) {
        for (int f = 0; f < adapter.fragments.size(); f++) {
            TabLayout.Tab tab = demoTabLayout.newTab();
            View tabView = makeTabView((String) adapter.getPageTitle(f));
            tab.setCustomView(tabView);
            demoTabLayout.addTab(tab);
        }
    }

    private View makeTabView(String title) {
        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.layout_tab_view, null);
        AppCompatTextView tvTitle = view.findViewById(R.id.tab_view_title);
        tvTitle.setText(title);
        return view;
    }

    private List<Fragment> initFragments() {
        List<Fragment> fragments = new ArrayList<>();

        fragments.add(fragmentTestClassifier);
        fragments.add(fragmentCutAndClassify);

        return fragments;
    }

    public void selectDemoFragment(Fragment fragment){
        int index=demoFragments.indexOf(fragment);
        if(index<0){
            return;
        }
        Objects.requireNonNull(demoTabLayout.getTabAt(index)).select();
    }

    private static class DemoPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments;


        public DemoPagerAdapter(@NonNull FragmentManager fm, List<Fragment> fragmentsList) {
            super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            fragments = fragmentsList;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Bundle bundle = fragments.get(position).getArguments();
            if (bundle == null) {
                return "";
            }
            return (CharSequence) bundle.get(DemoActivity.FRAGMENT_ARG_TITLE);
        }
    }
}