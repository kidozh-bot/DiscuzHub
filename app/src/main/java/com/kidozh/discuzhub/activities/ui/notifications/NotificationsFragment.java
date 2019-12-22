package com.kidozh.discuzhub.activities.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ui.privateMessages.bbsPrivateMessageFragment;
import com.kidozh.discuzhub.activities.ui.publicPM.bbsPublicMessageFragment;
import com.kidozh.discuzhub.activities.ui.userThreads.bbsMyThreadFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationsFragment extends Fragment {
    private static final String TAG = NotificationsFragment.class.getSimpleName();
    @BindView(R.id.fragment_notifications_viewpager)
    ViewPager fragmentNotificationViewPager;
    @BindView(R.id.fragment_notifications_tablayout)
    TabLayout fragmentNotificationTabLayout;

    private NotificationsViewModel notificationsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        ButterKnife.bind(this,root);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureViewPager();
    }

    void configureViewPager(){
        Log.d(TAG,"Configuring notification fragment");
        fragmentNotificationTabLayout.setupWithViewPager(fragmentNotificationViewPager);
        notificationViewPagerAdapter adapter  = new notificationViewPagerAdapter(getChildFragmentManager());
        fragmentNotificationViewPager.setAdapter(adapter);
    }

    public class notificationViewPagerAdapter extends FragmentStatePagerAdapter {
        notificationViewPagerAdapter(FragmentManager fragmentManager){
            super(fragmentManager);
        }


        public notificationViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new bbsPublicMessageFragment();
                case 1:
                    return new bbsPrivateMessageFragment();
                case 2:
                    return new bbsMyThreadFragment();
            }
            return new bbsPublicMessageFragment();


        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return getString(R.string.bbs_notification_public_pm);
                case 1:
                    return getString(R.string.bbs_notification_my_pm);
                case 2:
                    return getString(R.string.bbs_notification_my_thread);
                    default:
                        return "";
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}