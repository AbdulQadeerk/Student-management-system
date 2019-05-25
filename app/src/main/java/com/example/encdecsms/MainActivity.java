package com.example.encdecsms;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tabs)
    TabLayout mTabs;
    @BindView(R.id.main_viewpager)
    ViewPager mViewpager;

    private MySharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = MySharedPreferences.getInstance(this);

        if (sp.isLogin().equals("1")){
            Intent intent = new Intent(MainActivity.this,ChatActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle("Enc Dec SMS");

        setupViewPager(mViewpager);
        mTabs.setupWithViewPager(mViewpager);







    }


    private void setupViewPager(ViewPager viewPager) {
        MyFragmentAdapter adapter = new MyFragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(new RegisterFragment(),"Register");
        adapter.addFragment(new LoginFragment(),"Login");

        viewPager.setAdapter(adapter);
    }

}
