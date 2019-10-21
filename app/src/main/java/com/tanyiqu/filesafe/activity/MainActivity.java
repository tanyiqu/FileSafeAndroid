package com.tanyiqu.filesafe.activity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.tanyiqu.filesafe.R;
import com.tanyiqu.filesafe.fragment.AboutFragment;
import com.tanyiqu.filesafe.fragment.DirsFragment;
import com.tanyiqu.filesafe.fragment.SettingFragment;

public class MainActivity extends AppCompatActivity {

    private static long exitTime = 0;
    public static DrawerLayout drawer;
    public static FragmentManager fragmentManager;
    public static boolean neededDoubleClickToExit = true;
    public static LayoutAnimationController controller;//用作子fragment中的recycler动画

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container,new DirsFragment())
                    .commit();
        }
        init();
    }

    private void init() {
        //初始化各种值
        drawer = findViewById(R.id.drawer);

        fragmentManager = getSupportFragmentManager();

        controller = new LayoutAnimationController(AnimationUtils.loadAnimation(this,R.anim.anim_files_show));

        //初始化侧滑菜单
        initNavigationView();

        //添加监听
        addListeners();

    }

    private void addListeners() {

    }

    private void initNavigationView() {
        NavigationView navigation = findViewById(R.id.navigation);
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawer.closeDrawers();
                switch (menuItem.getItemId()){
                    case R.id.action_setting:   //设置页面
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.anim.anim_fragment_right_in, R.anim.anim_fragment_left_out, R.anim.anim_fragment_left_in, R.anim.anim_fragment_right_out)
                                .replace(R.id.fragment_container,new SettingFragment())
                                .addToBackStack(null)
                                .commit();
                        break;
                    case R.id.action_about:     //关于页面
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.anim.anim_fragment_right_in, R.anim.anim_fragment_left_out, R.anim.anim_fragment_left_in, R.anim.anim_fragment_right_out)
                                .replace(R.id.fragment_container,new AboutFragment())
                                .addToBackStack(null)
                                .commit();
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        //注释掉这段，让Fragment跟随Activity一起被回收
        //super.onSaveInstanceState(outState, outPersistentState);
    }

        @Override
    public void onBackPressed() {
        //首先检测抽屉是否关闭
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            //如果打开了DrawerLayout则返回键是关闭
            drawer.closeDrawers();
            return;
        }
        //需要两次点击退出
        if(neededDoubleClickToExit){
            if ((System.currentTimeMillis() - exitTime) > 1800) {
                Snackbar.make(drawer,"重复此动作退出保险箱",Snackbar.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            }
            else {
                finish();
            }
        }else {
            //不需要两次点击返回，直接调用父类方法解决
            super.onBackPressed();
        }

    }



}
