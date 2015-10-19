package com.happy.happylists;

import com.happy.happylists.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class BaseProd extends Activity implements OnItemClickListener, OnKeyListener,
		OnClickListener {

	static final String TAG = "myLogs";
	EditText etBPN;
	Spinner spBPE,spBPK;
	Button btnAbp,btnDbp;
	ListView lvBPS;
	TextView tvBPN;
	DB db;
	PageFragment pf;
	Cursor spisCursor,spcursor,spKCursor,spECursor,cursor;
	SimpleCursorAdapter scAdap,scKAdap,scEAdap;
	int pid,kat_txt,ed_txt;
	final int DIALOG_Del = 1;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bprod);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle("Справочник продукции");
		getActionBar().setIcon(R.drawable.blocnote50);
		db = new DB(this);
		db.open();
		etBPN = (EditText) findViewById(R.id.etBPN);
		spBPK = (Spinner) findViewById(R.id.spBPK);
		spBPE = (Spinner) findViewById(R.id.spBPE);
		btnAbp = (Button) findViewById(R.id.btnAbp);
		btnDbp = (Button) findViewById(R.id.btnDbp);
		lvBPS = (ListView) findViewById(R.id.lvBPS);
		//Данные списка listView
		CreateSpisok();
		//Данные списка Spinner (категории)
		CreateSKateg();
		//Данные списка Spinner (единицы измерений)
		CreateSEdin();
		//процедура нажатия на строку в ListView
		lvBPS.setOnItemClickListener(this);
		pid = 0;
		//процедура изменения названия продукции
		etBPN.setOnKeyListener(this);
		etBPN.setOnClickListener(this);
		//Сохранить категорию в списке
		btnAbp.setOnClickListener(this);
		etBPN.setText("");
		btnDbp.setVisibility(View.GONE);
		btnDbp.setOnClickListener(this);
		// Событие возникающее при изменение вводимого текста
		etBPN.addTextChangedListener(filterTextWatcher);
		tvBPN = (TextView) findViewById(R.id.tvBPN);
		tvBPN.setText("");
	}

	public boolean requery (){
		spisCursor.close();
		spKCursor.close();
		spECursor.close();
		return  true;
	}

	protected void onDestroy() {
		super.onDestroy();
		// закрываем подключение при выходе
		db.close();
	}

	//Данные списка listView
	private void CreateSpisok() {
		spisCursor = db.getAllBProd("");
		String[] from = new String[] { "pname" };
		int[] to = new int[] {R.id.tvBPN};
		scAdap = new SimpleCursorAdapter(this, R.layout.bplist, spisCursor, from, to);
		scAdap.setStringConversionColumn(spisCursor.getColumnIndexOrThrow("pname"));
		scAdap.setFilterQueryProvider(new FilterQueryProvider() {
			public Cursor runQuery(CharSequence constraint) {
				String partialValue = null;
				if (constraint != null) {
					partialValue = constraint.toString();
				}
				return db.getAllBProd(partialValue);
			}
		});
		lvBPS.setAdapter(scAdap);
		scAdap.notifyDataSetChanged();
	}

	//Данные списка Spinner (категории)
	private void CreateSKateg() {
		spKCursor = db.getAllKateg("Kategor", "kname");
		String[] from = new String[] { "kname"};
		int[] to = new int[] {android.R.id.text1 };
		scKAdap = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, spKCursor, from, to);
		scKAdap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spBPK.setAdapter(scKAdap);
	}

	//Данные списка Spinner (единицы измерений)
	private void CreateSEdin() {
		spECursor = db.getEdinName("Edin", "");
		String[] from = new String[] { "ename"};
		int[] to = new int[] {android.R.id.text1  };
		scEAdap = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, spECursor, from, to);
		scEAdap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spBPE.setAdapter(scEAdap);
	}

	//процедура нажатия на строку в ListView
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {
		// TODO Auto-generated method stub
		btnDbp.setVisibility(View.VISIBLE);
		pid = (int) id;
		TextView textView1 = (TextView) view.findViewById(R.id.tvBPN);
		etBPN.setText(textView1.getText().toString());
		tvBPN.setText("Редактирование продукции: "+textView1.getText().toString());
		spcursor = db.getBProd(pid);
		int kp = spcursor.getColumnIndex("_id");
		spcursor.moveToFirst();
		do {
			if (!spcursor.isNull(kp)) {
				kat_txt = spcursor.getInt(spcursor.getColumnIndex("kid"));
				ed_txt = spcursor.getInt(spcursor.getColumnIndex("eid"));
			}
		} while (spcursor.moveToNext());
		spcursor.close();
		scKAdap.notifyDataSetChanged();
		SelectSpinnerItemByValue(spBPK, kat_txt);
		scEAdap.notifyDataSetChanged();
		SelectSpinnerItemByValue(spBPE, ed_txt);
	}

	public static void SelectSpinnerItemByValue(Spinner spnr, long value)
	{
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) spnr.getAdapter();
		for (int position = 0; position < adapter.getCount(); position++)
		{
			if(adapter.getItemId(position) == value)
			{
				spnr.setSelection(position);
				return;
			}
		}
	}

	//процедура изменения названия продукции
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction() == KeyEvent.ACTION_DOWN &&
				(keyCode == KeyEvent.KEYCODE_ENTER))
		{
			// сохраняем текст, введенный до нажатия Enter в переменную
			String strName = etBPN.getText().toString();
			if (strName.length()>0) {
				etBPN.setFocusable(false);
				etBPN.setFocusableInTouchMode(false);
				btnAbp.setFocusable(true);
			} else {
				Toast.makeText(this, "Название не может быть пустым", Toast.LENGTH_LONG).show();
				etBPN.setText("");
			}
			return true;
		}
		return false;
	}

	//процедура нажатия на кнопку
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.etBPN:
				etBPN.setFocusable(true);
				etBPN.setFocusableInTouchMode(true);
				break;
			case R.id.btnAbp:
				String strName = etBPN.getText().toString();
				if (strName.length() == 0)
					Toast.makeText(this, "Введите название продукции!", Toast.LENGTH_LONG).show();
				else
					saveData();
				break;
			case R.id.btnDbp:
				showDialog(DIALOG_Del);
				break;
		}
	}

	//процедура создания диалога (сохранить/отменить)
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_Del) {
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			// заголовок
			adb.setTitle(R.string.delete);
			// сообщение
			adb.setMessage((R.string.del_prod));
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
		String strName = etBPN.getText().toString();
		int strKat = (int) spBPK.getSelectedItemId();
		int strEdn = (int) spBPE.getSelectedItemId();
		if (pid==0) {
			db.addNewProd("products",strKat,asUpperCaseFirstChar(strName),strEdn);
			cursor = db.getProdNM(strName);
			int ppid = cursor.getColumnIndex("_id");
			cursor.moveToFirst();
			do {
				if (!cursor.isNull(ppid)) {
					pid= Integer.parseInt(cursor.getString(ppid));
				}
			} while (cursor.moveToNext());
			cursor.close();
			db.addPriceProd("pprice",pid,1);
		} else
			db.UpDateProd("products",strKat,asUpperCaseFirstChar(strName),strEdn,pid);
		CreateSpisok();
		pid=0;
		etBPN.setText("");
		tvBPN.setText("");
		spBPK.setSelection(0);
		spBPE.setSelection(0);
		btnDbp.setVisibility(View.GONE);
	}

	//процедура удаления данных в базы
	private void delData() {
		spcursor = db.getProdinSP(pid);
		if (spcursor.getCount()>0) {
			spcursor.moveToFirst();
			do {
				db.delRecPS("spisok",spcursor.getInt(spcursor.getColumnIndex("_id")));
			} while (spcursor.moveToNext());
			spcursor.close();
		}
		db.delRecProd("products", pid);
		db.delPriceProd("pprice", pid);
		CreateSpisok();
		pid=0;
		etBPN.setText("");
		tvBPN.setText("");
		spBPK.setSelection(0);
		spBPE.setSelection(0);
		btnDbp.setVisibility(View.GONE);
	}

	private TextWatcher filterTextWatcher = new TextWatcher() {
		public void afterTextChanged(Editable s) {
		}
		public void beforeTextChanged(CharSequence s, int start, int count,
									  int after) {
		}
		public void onTextChanged(CharSequence s, int start, int before,
								  int count) {
			scAdap.getFilter().filter(asUpperCaseFirstChar(s.toString()));
		}
	};

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