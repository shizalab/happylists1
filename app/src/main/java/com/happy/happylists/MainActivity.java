package com.happy.happylists;


import com.happy.happylists.R;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

public class MainActivity<TitlePageIndicator> extends FragmentActivity{

  static final String TAG = "myLogs";

  static final int PAGE_COUNT = 3;
  int backColor;

  public static ViewPager pager;
  PagerAdapter pagerAdapter;
  TitlePageIndicator titleIndicator;
  public static PagerTabStrip pagerTabStrip;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main);

    //убрать иконку приложения
    getActionBar().hide();

    backColor = Color.rgb(150, 240, 101);

    //Создаем 3 страницы
    pager = (ViewPager) findViewById(R.id.pager);
    pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
    pager.setAdapter(pagerAdapter);

    pagerTabStrip = (PagerTabStrip) findViewById(R.id.pagerTabStrip);
    // pagerTabStrip.setTextSize(PAGE_COUNT, 9);
    pagerTabStrip.setDrawFullUnderline(true);
    pagerTabStrip.setTabIndicatorColor(backColor);

    pager.setCurrentItem(1);

    //работа со страницами
    pager.setOnPageChangeListener(new OnPageChangeListener() {

      //номер текущей отображенной страницы
      @Override
      public void onPageSelected(int position) {
        // Log.d(TAG, "onPageSelected (выбрана страница), position = " + position);
      }

      //текущее значение скроллера при пролистывании
      @Override
      public void onPageScrolled(int position, float positionOffset,
                                 int positionOffsetPixels) {
        //  Log.d(TAG, "скролингуем " + position);
      }

      //сообщает нам о состоянии, в котором находится скроллер
      @Override
      public void onPageScrollStateChanged(int state) {
      }

    });
  }

  //создание фрагмента для страниц
  private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

    public MyFragmentPagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int position) {
      return PageFragment.newInstance(position);
    }

    @Override
    public int getCount() {
      return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      switch(position) {
        case 0:
          return getResources().getString(R.string.tspiski);
        //  break;
        case 1:
          return getResources().getString(R.string.tpokupki);
        //    break;
        case 2:
          return getResources().getString(R.string.tsetting);
        //      break;
      }
      return String.valueOf("");
    }

  }


} 