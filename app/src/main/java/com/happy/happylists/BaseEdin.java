package com.happy.happylists;

import com.happy.happylists.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BaseEdin extends Activity implements OnItemClickListener, OnKeyListener, OnClickListener {

	static final String TAG = "myLogs";
	EditText etBEN;
	Button btnAbe,btnDbe;
	ListView lvBE;
	DB db;
	TextView tvBEN;
	Cursor spisCursor,spcursor;
	SimpleCursorAdapter scAdap;
	int eid;
	final int DIALOG_Del = 1;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bedin);
		etBEN = (EditText) findViewById(R.id.etBEN);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle("Справочник единиц");
		getActionBar().setIcon(R.drawable.blocnote50);
		db = new DB(this);
		db.open();
		btnAbe = (Button) findViewById(R.id.btnAbe);
		btnDbe = (Button) findViewById(R.id.btnDbe);
		lvBE = (ListView) findViewById(R.id.lvBE);
		CreateSpisok();
		//процедура нажатия на строку в ListView
		lvBE.setOnItemClickListener(this);
		eid = 0;
		//процедура изменения названия категории
		etBEN.setOnKeyListener(this);
		//Сохранить категорию в списке
		btnAbe.setOnClickListener(this);
		etBEN.setText("");
		btnDbe.setVisibility(View.GONE);
		btnDbe.setOnClickListener(this);
		tvBEN = (TextView) findViewById(R.id.tvBEN);
		tvBEN.setText("");
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
		spisCursor = db.getEdinName("edin", "");
		String[] from = new String[] { "ename" };
		int[] to = new int[] {R.id.tvBEN};
		scAdap = new SimpleCursorAdapter(this, R.layout.belist, spisCursor, from, to);
		lvBE.setAdapter(scAdap);
	}

	//процедура нажатия на строку в ListView
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {
		// TODO Auto-generated method stub
		btnDbe.setVisibility(View.VISIBLE);
		eid = (int) id;
		TextView textView1 = (TextView) view.findViewById(R.id.tvBEN);
		etBEN.setText(textView1.getText().toString());
		tvBEN.setText("Редактирование единицы: "+textView1.getText().toString());
	}

	//процедура нажатия на EditText (цвет)
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.etBEN:
				etBEN.setFocusable(true);
				etBEN.setFocusableInTouchMode(true);
				break;
			case R.id.btnAbe:
				String strName = etBEN.getText().toString();
				if (strName.length() == 0)
					Toast.makeText(this, "Введите название единицы!", Toast.LENGTH_LONG).show();
				else
					saveData();
				break;
			case R.id.btnDbe:
				showDialog(DIALOG_Del);
				break;
		}
	}

	//процедура изменения названия категории
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction() == KeyEvent.ACTION_DOWN &&
				(keyCode == KeyEvent.KEYCODE_ENTER))
		{
			// сохраняем текст, введенный до нажатия Enter в переменную
			String strName = etBEN.getText().toString();
			if (strName.length()>0) {
				etBEN.setFocusable(false);
				etBEN.setFocusableInTouchMode(false);
				btnAbe.setFocusable(true);
			} else {
				Toast.makeText(this, "Название не может быть пустым", Toast.LENGTH_LONG).show();
				etBEN.setText("");
				tvBEN.setText("");
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
			adb.setMessage((R.string.del_edin));
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
		String strName = etBEN.getText().toString();
		if (eid==0)
			db.addEdin("edin",asUpperCaseFirstChar(strName));
		else
			db.UpDateEdin("edin",asUpperCaseFirstChar(strName),eid);
		CreateSpisok();
		eid=0;
		etBEN.setText("");
		tvBEN.setText("");
		btnDbe.setVisibility(View.GONE);
	}

	//процедура удаления данных в базы
	private void delData() {
		if (eid != 11) {
			spcursor = db.getEdinInProd(eid);
			if (spcursor.getCount()>0) {
				spcursor.moveToFirst();
				do {
					db.UpDateEdinInProd("products",11,spcursor.getInt(spcursor.getColumnIndex("_id")));
				} while (spcursor.moveToNext());
				spcursor.close();
			}
			db.delRecEd("edin", eid);
			CreateSpisok();
			eid=0;
			etBEN.setText("");
			tvBEN.setText("");
			btnDbe.setVisibility(View.GONE);
			Toast.makeText(this, "Все данные единицы в справочнике продукции переименованы как ШТ!", Toast.LENGTH_LONG).show();
		}
		else
			Toast.makeText(this, "Нельзя удалить данную единицу! Можно только переименовать.", Toast.LENGTH_LONG).show();
	}

	//процедура изменения первого символа с нижнего регистра в высокий
	public final static String asUpperCaseFirstChar(final String target) {
		if ((target == null) || (target.length() == 0)) {
			return target; // You could omit this check and simply live with an
			// exception if you like
		}
		return Character.toLowerCase(target.charAt(0))
				+ (target.length() > 1 ? target.substring(1) : "");
	}

}