package matheusjullien.photomap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

public class ScreenSlidePagerActivity extends FragmentActivity {
    private PageDatabase mPageDatabase;
    private LatLng mLatLng;
    private ArrayList<Page> pageArrayList;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(new Fabric.Builder(this)
                .kits(new Crashlytics(), new Answers())
                .debuggable(true)
                .build());
        setContentView(R.layout.activity_screenslidepager);

        mPageDatabase = new PageDatabase(this);
        mPageDatabase.open();

        Bundle bundle = getIntent().getParcelableExtra("bundle");

        if (bundle != null) {
            mLatLng = bundle.getParcelable("pageLatLng");

            pageArrayList = mPageDatabase.getPagesByLatLng(mLatLng);
        } else {
            pageArrayList = mPageDatabase.getAllPages();
        }

        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (pageArrayList.get(position).getType().equals("image")) {
                return PhotoFragment.newInstance(pageArrayList.get(position));
            } else {
                return VideoFragment.newInstance(pageArrayList.get(position));
            }
        }

        @Override
        public int getCount() { return pageArrayList.size(); }
    }

}
