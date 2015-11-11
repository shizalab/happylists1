package com.happy.happylists;


import com.happy.happylists.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;

public class MainActivity<TitlePageIndicator> extends FragmentActivity{

  static final String TAG = "myLogs";

  static final int PAGE_COUNT = 3;
  int backColor;
  Cursor cursor;
  final int DIALOG_EXIT = 1;

  DB db;

  public static ViewPager pager;
  PagerAdapter pagerAdapter;
  TitlePageIndicator titleIndicator;
  public static PagerTabStrip pagerTabStrip;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main);

    db = new DB(this);
    db.open();

    FindCreate();

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

  private void FindCreate(){
    int kl = 0;
    cursor = db.getkl();
    if (cursor.getCount() != 0) {
      cursor.moveToFirst();
      do {
        kl = Integer.parseInt(cursor.getString(cursor.getColumnIndex("klid")));
      } while (cursor.moveToNext());
    }
    cursor.close();
    if ((kl<6) && ( kl != 10))
      db.upkl("spisok", kl+1);
    if (kl==5)
      showDialog(DIALOG_EXIT);
  }


  protected void onDestroy() {
    super.onDestroy();
    db.close();
  }

  protected Dialog onCreateDialog(int id) {
    if (id == DIALOG_EXIT) {
      AlertDialog.Builder adb = new AlertDialog.Builder(this);
      adb.setTitle(R.string.titl);
      adb.setMessage(R.string.mess);
      adb.setNegativeButton(R.string.dyes, myClickListener);
      adb.setNeutralButton(R.string.late, myClickListener);
      adb.setPositiveButton(R.string.not, myClickListener);
      return adb.create();
    }
    return super.onCreateDialog(id);
  }

  DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
    public void onClick(DialogInterface dialog, int which) {
      switch (which) {
        case Dialog.BUTTON_POSITIVE:
          db.upkl("spisok", 10);
          break;
        case Dialog.BUTTON_NEUTRAL:
          db.upkl("spisok", 0);
          break;
        case Dialog.BUTTON_NEGATIVE:
          Intent intent = new Intent(Intent.ACTION_VIEW);
          intent.setData(Uri.parse("market://details?id="+getPackageName()));
          startActivity(intent);
      }
    }
  };
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