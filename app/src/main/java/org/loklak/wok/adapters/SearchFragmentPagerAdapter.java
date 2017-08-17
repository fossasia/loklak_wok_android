package org.loklak.wok.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchFragmentPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment>  mFragmentList = new ArrayList<>();
    private List<String> mFragmentNameList = new ArrayList<>();

    public SearchFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentNameList.get(position);
    }

    public void addFragment(Fragment fragment, String pageTitle) {
        mFragmentList.add(fragment);
        mFragmentNameList.add(pageTitle);
    }
}
