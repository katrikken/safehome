package cz.kuryshee.safehome.safehomeclient;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of class, which manages Fragments on {@link RpiControlActivity}.
 *
 * @author Ekaterina Kurysheva
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    /**
     * Constructor, which calls super method.
     */
    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    /**
     * @see FragmentPagerAdapter#getItem(int)
     */
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    /**
     * @see FragmentPagerAdapter#getCount()
     */
    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    /**
     * Method registeres available fragments at this viewpager.
     * @param fragment {@link Fragment}
     * @param title name of the fragment.
     */
    public void addFrag(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    /**
     * @see FragmentPagerAdapter#getPageTitle(int)
     */
    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }
}
