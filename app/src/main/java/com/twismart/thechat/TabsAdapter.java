package com.twismart.thechat;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
/**
 * Created by sneyd on 8/17/2016.
 **/
public class TabsAdapter extends FragmentPagerAdapter {

    private String[] titleFragments;

    public TabsAdapter(FragmentManager fragmentManager, Context context){
        super(fragmentManager);
        titleFragments = context.getResources().getStringArray(R.array.mainactivity_title_fragments);
    }

    @Override
    public int getCount() {
        return titleFragments.length;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new FragmentFind();
            case 1:
                return new FragmentChats();
            default:
                return new FragmentProfile();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titleFragments[position];
    }
}