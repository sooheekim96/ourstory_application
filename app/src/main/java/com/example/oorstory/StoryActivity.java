package com.example.oorstory;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.skt.Tmap.TMapTapi;
import java.util.HashMap;

import me.relex.circleindicator.CircleIndicator3;

/*import me.relex.circleindicator.CircleIndicator3;*/

public class StoryActivity extends AppCompatActivity {
    private String userLocation;
    private ImageView selectedImage;
    private LinearLayout btn_comment;
    private Button gamestart;
    private ViewPager2 mPager;
    private FragmentStateAdapter pagerAdapter;
    private int num_page = 2;
    private String[] descs = { "강남역 11번 출구", "잠실종합운동장"};
    private String desc ;

    /*    private CircleIndicator3 mIndicator;*/
    private CircleIndicator3 mIndicator;

    String title, theme, time;
    int star_num;
    TMapTapi tMapTapi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        mPager = findViewById(R.id.viewpager);
        pagerAdapter = new StoryFragmentAdapter(this, num_page) {
            @NonNull
            // @Override
            public Fragment getItem(int position) {
                return null;
            }
        };
        mPager.setAdapter(pagerAdapter);

        mIndicator = findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        mIndicator.createIndicators(num_page, 0);

        mPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        mPager.setCurrentItem(1, true);
        mPager.setOffscreenPageLimit(3);

        mPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (positionOffsetPixels == 0) {
                    mPager.setCurrentItem(position);
                    Log.e("onPageScrolled", "PageScrolled");
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.e("onPageSelected", "pageselected");
                Log.e("position", Integer.toString(position));
                desc = descs[position];
                gamestart.setText(desc + "로 출발하기");
                mIndicator.animatePageSelected(position%num_page);
                //Toast.makeText(StoryActivity.this, "onPageSelected", Toast.LENGTH_SHORT);
            }
        });

        btn_comment = findViewById(R.id.btn_comment);
        // intent 값 얻어오기
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        theme = intent.getStringExtra("theme");
        time = intent.getStringExtra("time");
        star_num = Integer.parseInt(intent.getStringExtra("star_num"));
        Log.e("받아온 정보", title+" "+theme+" "+time);

        // 데이터 넣기
        TextView title_story_detail = (TextView)findViewById(R.id.title_story_detail);
        TextView theme_detail = (TextView)findViewById(R.id.theme_detail);
        TextView time_detail = (TextView)findViewById(R.id.time_detail);
        title_story_detail.setText(title);
        theme_detail.setText(theme);
        time_detail.setText(time);

        // 별점 기록하기
        ImageView[] stars_list = {
                findViewById(R.id.diff1_onStory), findViewById(R.id.diff2_onStory), findViewById(R.id.diff3_onStory),
                findViewById(R.id.diff4_onStory), findViewById(R.id.diff5_onStory)};
        for (int i=0;i<star_num;i++){
            stars_list[i].setImageResource(R.drawable.starred);
        }

        // mapActivity로 돌아가기
        ImageButton back_btn = findViewById(R.id.back_btn_toMap);
        back_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(StoryActivity.this, MapActivity.class);
                finish();
            }
        });
        btn_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StoryActivity.this, CommentActivity.class);
                intent.putExtra("title", title);
                //finish();
                startActivity(intent);
            }
        });


        // 게임 시작하기 및 타이머 시작 + Tmap
        gamestart = (Button)findViewById(R.id.gameStart);
        tMapTapi = new TMapTapi(this);

        configureApp();
        setOnAuthentication();

        gamestart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                Intent Intent = new Intent(StoryActivity.this, StopWatchActivity.class);
                Intent.putExtra("title", title);
                Intent.putExtra("place", desc);
                startActivity(Intent);
                gamestart.setEnabled(false);
            }
        });

       //게임 끝내기, 버튼 재활성화
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("activateButton");

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                gamestart.setEnabled(true);
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);

    }



    // 카브뷰 펼치기/접기 이벤트
    public void unfold_cardview(View view){
        TextView textView;
        switch (view.getId()){
            case R.id.show_game_tv :
                textView = findViewById(R.id.explan_game_tv);
                Log.e("클릭!!게임",(textView.getVisibility()+""));
                break;
            case R.id.show_story_tv :
                textView = findViewById(R.id.explan_story_tv);
                Log.e("클릭!!스토리",(textView.getVisibility()+""));
                break;
            case R.id.show_place_tv :
                textView = findViewById(R.id.explan_place_tv);
                Log.e("클릭!!목표장소",(textView.getVisibility()+""));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
        if ((textView.getVisibility()+"").contains("8")) textView.setVisibility(View.VISIBLE);
        else textView.setVisibility(View.GONE);
    }


    // 외부 지도 어플로 연결, 목적지 정보 전달하기
    public void mapIcon_onClick(View view){
        Log.e("맵 아이콘 클릭", "맵 아이콘 클릭");
    }

    // API 설정
    private void configureApp() {
        tMapTapi.setSKTMapAuthentication(getString(R.string.map_api));
    }

    private void setOnAuthentication(){
        tMapTapi.setOnAuthenticationListener(new TMapTapi.OnAuthenticationListenerCallback() {
            @Override
            public void SKTMapApikeySucceed() {
                Log.d("test", "성공");
            }

            @Override
            public void SKTMapApikeyFailed(String errorMsg) {
                Log.d("test", "실패");
            }
        });
    }


}