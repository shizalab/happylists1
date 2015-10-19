package com.happy.happylists;

import com.happy.happylists.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BaseKateg extends Activity implements
		ColorPickerDialog.OnColorChangedListener, OnItemClickListener, OnClickListener, OnKeyListener {

	static final String TAG = "myLogs";
	protected int _color;
	EditText acBC,acBK;
	Button btnAbk,btnDbk;
	ListView lvBK;
	TextView tvBKN;
	private Paint mPaint;
	DB db;
	Cursor spisCursor,spcursor;
	SimpleCursorAdapter scAdap;
	int kid;
	String kcol, hexColor;
	final int DIALOG_Del = 1;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bkateg);
		acBC = (EditText) findViewById(R.id.acBC);
		mPaint = new Paint();
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle("Справочник категорий");
		getActionBar().setIcon(R.drawable.blocnote50);
		db = new DB(this);
		db.open();
		acBK = (EditText) findViewById(R.id.acBK);
		btnAbk = (Button) findViewById(R.id.btnAbk);
		btnDbk = (Button) findViewById(R.id.btnDbk);
		lvBK = (ListView) findViewById(R.id.lvBK);
		CreateSpisok();
		//процедура нажатия на строку в ListView
		lvBK.setOnItemClickListener(this);
		kid = 0;
		//процедура нажатия на EditText (цвет)
		acBC.setOnClickListener(this);
		//процедура изменения названия категории
		acBK.setOnKeyListener(this);
		//Сохранить категорию в списке
		btnAbk.setOnClickListener(this);
		acBK.setText("");
		acBC.setBackgroundColor(Color.parseColor("#FAEBD7"));
		hexColor="#FAEBD7";
		btnDbk.setVisibility(View.GONE);
		btnDbk.setOnClickListener(this);
		tvBKN = (TextView) findViewById(R.id.tvBKN);
		tvBKN.setText("");
	}

	public boolean requery (){
		spisCursor.close();
		return  true;
	}

	protected void onDestroy() {
		super.onDestroy();
		// закрываем подключение при выходе
		db.close();
	}

	//Данные списка listView 
	private void CreateSpisok() {
		spisCursor = db.getAllKateg("Kategor", "kname");
		String[] from = new String[] { "kcolor", "kname" };
		int[] to = new int[] {R.id.ivBKC , R.id.tvBKN};
		scAdap = new SimpleCursorAdapter(this, R.layout.bklist, spisCursor, from, to,0);
		scAdap.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int column) {
				switch (view.getId()) {
					case R.id.ivBKC:
						ImageView iv = (ImageView) view.findViewById(R.id.ivBKC);
						String kcol = cursor.getString(cursor.getColumnIndex("kcolor"));
						iv.setVisibility(View.VISIBLE);
						iv.setBackgroundColor(Color.parseColor(kcol));
						return true;
				}
				return false;
			}
		});
		lvBK.setAdapter(scAdap);
	}

	//процедура нажатия на строку в ListView
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {
		// TODO Auto-generated method stub
		btnDbk.setVisibility(View.VISIBLE);
		kid = (int) id;
		TextView textView1 = (TextView) view.findViewById(R.id.tvBKN);
		acBK.setText(textView1.getText().toString());
		tvBKN.setText("Редактирование категории: "+textView1.getText().toString());
		spcursor = db.getKateg(kid);
		int kkid = spcursor.getColumnIndex("_id");
		spcursor.moveToFirst();
		do {
			if (!spcursor.isNull(kkid)) {
				kcol = spcursor.getString(spcursor.getColumnIndex("kcolor"));
			}
		} while (spcursor.moveToNext());
		spcursor.close();
		acBC.setBackgroundColor(Color.parseColor(kcol));
	}

	//процедура нажатия на EditText (цвет)
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.acBC:
				mPaint.setXfermode(null);
				mPaint.setAlpha(0xFF);
				mPaint.setColor(Color.BLUE);
				new ColorPickerDialog(this, this, mPaint.getColor()).show();
				break;
			case R.id.btnAbk:
				String strName = acBK.getText().toString();
				if (strName.length() == 0)
					Toast.makeText(this, "Введите название категории!", Toast.LENGTH_LONG).show();
				else
					saveData();
				break;
			case R.id.btnDbk:
				showDialog(DIALOG_Del);
				break;
		}
	}

	//процедура работы с колор-пикером
	public class SampleView extends View {
		// CONSTRUCTOR
		public SampleView(Context context) {
			super(context);
			setFocusable(true);
		}
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();

			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					invalidate();
					break;
				case MotionEvent.ACTION_MOVE:
					invalidate();
					break;
				case MotionEvent.ACTION_UP:
					invalidate();
					break;
			}
			return true;
		}
		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawColor(Color.GREEN);
			Bitmap b = Bitmap.createBitmap(300, 300, Bitmap.Config.ALPHA_8);
			Canvas c = new Canvas(b);
			c.drawRect(0, 0, 300, 300, mPaint);
			canvas.drawBitmap(b, 20, 20, mPaint);
		}
	}

	@Override
	public void colorChanged(int color) {
		// TODO Auto-generated method stub
		acBC.setBackgroundColor(color);
		_color = color;
		hexColor = String.format("#%06X", (0xFFFFFF & color));
	}

	//процедура изменения названия категории
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction() == KeyEvent.ACTION_DOWN &&
				(keyCode == KeyEvent.KEYCODE_ENTER))
		{
			// сохраняем текст, введенный до нажатия Enter в переменную
			String strName = acBK.getText().toString();
			if (strName.length()>0) {
				acBK.setFocusable(false);
				acBK.setFocusableInTouchMode(false);
				btnAbk.setFocusable(true);
			} else {
				Toast.makeText(this, "Название не может быть пустым", Toast.LENGTH_LONG).show();
				acBK.setText("");
			}
			return true;
		}
		return false;
	}

	//процедура создания диалога (сохранить/отменить)
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_Del) {
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			// заголовок
			adb.setTitle(R.string.delete);
			// сообщение
			adb.setMessage((R.string.del_data));
			// иконка
			adb.setIcon(android.R.drawable.ic_dialog_info);
			// кнопка положительного ответа
			adb.setPositiveButton(R.string.yes, myClickListener);
			// кнопка нейтрального ответа
			adb.setNeutralButton(R.string.cancel, myClickListener);
			// создаем диалог
			return adb.create();
		}
		return super.onCreateDialog(id);
	}

	Dialog.OnClickListener myClickListener = new Dialog.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
				// положительная кнопка
				case Dialog.BUTTON_POSITIVE:
					delData();
					break;
				// нейтральная кнопка
				case Dialog.BUTTON_NEUTRAL:
					break;
			}
		}
	};

	//процедура сохранения данных в базу
	private void saveData(){
		String strName = acBK.getText().toString();
		if (kid==0)
			db.addNewKateg("kategor",asUpperCaseFirstChar(strName),hexColor);
		else
			db.UpDateKateg("kategor",asUpperCaseFirstChar(strName),hexColor,kid);
		CreateSpisok();
		kid=0;
		acBK.setText("");
		tvBKN.setText("");
		acBC.setBackgroundColor(Color.parseColor("#FAEBD7"));
		hexColor="#FAEBD7";
		btnDbk.setVisibility(View.GONE);
	}

	//процедура удаления данных в базы
	private void delData() {
		if (kid != 20) {
			spcursor = db.getProdKateg(kid);
			if (spcursor.getCount()>0) {
				spcursor.moveToFirst();
				do {
					db.UpDateKatInProd("products",20,spcursor.getInt(spcursor.getColumnIndex("_id")));
				} while (spcursor.moveToNext());
				spcursor.close();
			}
			db.delRecKateg("kategor", kid);
			CreateSpisok();
			kid=0;
			acBK.setText("");
			tvBKN.setText("");
			acBC.setBackgroundColor(Color.parseColor("#FAEBD7"));
			hexColor="#FAEBD7";
			btnDbk.setVisibility(View.GONE);
			Toast.makeText(this, "Вся продукция данной категории перенесена в категорию Временная!", Toast.LENGTH_LONG).show();
		}
		else
			Toast.makeText(this, "Нельзя удалить данную категорию! Можно только переименовать.", Toast.LENGTH_LONG).show();
	}

	//процедура изменения первого символа с нижнего регистра в высокий
	public final static String asUpperCaseFirstChar(final String target) {
		if ((target == null) || (target.length() == 0)) {
			return target; // You could omit this check and simply live with an
			// exception if you like
		}
		return Character.toUpperCase(target.charAt(0))
				+ (target.length() > 1 ? target.substring(1) : "");
	}

}



