package cz.kuryshee.safehome.safehomeclient;

import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Activity implements view with two tabs, where user can see latest events and photos from Raspberry Pi.
 * The views are displayed on separate Fragments {@link GalleryFragment} and {@link EventsFragment}.
 *
 * @author Ekaterina Kurysheva
 */
public class RpiControlActivity extends AppCompatActivity {

    /**
     * Methods sets the view of the activity.
     * @see AppCompatActivity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rpi_control);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        setViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    /**
     * Method sets the view, which handles switching between tabs.
     * @param viewPager
     */
    private void setViewPager(ViewPager viewPager){
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFrag(new EventsFragment(), getResources().getString(R.string.bn_events));
        adapter.addFrag(new GalleryFragment(), getResources().getString(R.string.bn_gallery));

        viewPager.setAdapter(adapter);
    }
}
