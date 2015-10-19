package com.happy.happylists;

import java.util.ArrayList;

import com.happy.happylists.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PageFragment extends Fragment implements LoaderCallbacks<Cursor>,ExpandableListView.OnChildClickListener,
		OnGroupClickListener	 {

	static final String TAG = "myLogs";

	DB db;

	private static final String DB_STABLE = "Spisok";
	public static final String S_ID = "_id";
	public static final String S_NOM = "snom";
	public static final String S_NAME = "sname";
	public static final String S_DATE = "sdate";
	public static final String S_PID = "pid";
	public static final String S_KOL = "skol";
	public static final String S_PRICE = "sprice";
	public static final String S_VAGNO = "svagno";
	public static final String S_KORZ = "skorz";

	private static final String DB_PTABLE = "Products";
	public static final String P_ID = "_id";
	public static final String P_KID = "kid";
	public static final String P_NAME = "pname";
	public static final String P_EID = "eid";
	public static final String P_IM = "pimg";

	private static final String DB_ETABLE = "Edin";
	public static final String E_ID = "_id";
	public static final String E_NAME = "ename";

	private static final String DB_NTABLE = "Nastr";

	private static final String DB_VTABLE = "valuta";
	public static final String V_ID = "_id";
	public static final String V_NAME = "vname";

	static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";

	private static final int CM_DELETE_ID = 2;

	static int itemselected;

	int pageNumber,prodid,val_id;
	Float itog;
	int backColor_green;
	String[] pnames;

	Cursor spisCursor,spisCursor2, prodCursor,cursor,edinCursor,spprCursor;
	SimpleCursorAdapter scAdapter,pcAdapter,spAdapter,ecAdapter;
	ListView lvSS, lvData;
	AutoCompleteTextView acPN,acPE;
	EditText etCount, etPrice, etSName;
	Button btnAdd,btDel,btSave;
	CheckBox chbvagno;
	TextView tvItog,tvItogtxt;
	ExpandableListView elvMain;
	ElemAdapter expListAdapter;
	ArrayList<String> groupNames;
	ArrayList<ArrayList<Elements>> elems ;
	ArrayList<Elements> elem;
	Typeface faceHN, faceRM, faceRLI;

	int max_nom,sn,ik,kv,ei,pr,vl,ek,nid,nsn;

	static PageFragment newInstance(int page) {
		PageFragment pageFragment = new PageFragment();
		Bundle arguments = new Bundle();
		arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
		pageFragment.setArguments(arguments);
		return pageFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
		//открываем базу
		db = new DB(getActivity());
		db.open();
		// задаем шрифты
		faceHN = Typeface.createFromAsset(getActivity().getAssets(), getActivity().getResources().getString(R.string.helveticaNeueLight));
		faceRM = Typeface.createFromAsset(getActivity().getAssets(), getActivity().getResources().getString(R.string.robotoMedium));
		faceRLI = Typeface.createFromAsset(getActivity().getAssets(),getActivity().getResources().getString(R.string.robotoLightItalic));
	}

	//создаем контекстное меню
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
									ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterContextMenuInfo aMenuInfo = (AdapterContextMenuInfo) menuInfo;
		itemselected = aMenuInfo.position;
		menu.add(0, CM_DELETE_ID, 0, R.string.delete_record);
	}

	//процедура обработки нажатия меню
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
			case CM_DELETE_ID:
				db.delRec(DB_STABLE,acmi.id);
				getActivity().getSupportLoaderManager().getLoader(0).forceLoad();
				FindMaxSP();
				getCursor(sn);
				tvItog.setText(String.format("%.2f",itog));
				GetNastr(sn);
				CreateSPList();
				break;
			default:
				return super.onContextItemSelected(item);
		}
		return true;
	}

	//создание страниц 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View view;

		if(pageNumber == 0) {
			/* работа в первой странице*/

			view = inflater.inflate(R.layout.sspisok, null);
			lvSS = (ListView) view.findViewById(R.id.lvSS);
			CreateListSpisok();

			//присвоение и обработка кнопки "Создать новый список"
			Button btSS = (Button) view.findViewById(R.id.btSS);
			btSS.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					addNewSpisok();
					//обновили список
					getActivity().getSupportLoaderManager().getLoader(0).forceLoad();
					spisCursor.moveToLast();
					//процедура программного нажатия на строку списка
					int activePosition =lvSS.getCount(); // последний элемент списка
					lvSS.performItemClick(
							lvSS.getChildAt(activePosition),
							activePosition,
							lvSS.getAdapter().getItemId(activePosition));
				}
			});

			//процедура нажатия на строку в ListView
			lvSS.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if (id==0)
					{
						FindMaxSP();
						getCursor(sn);
					} else
						getCursor((int) id);
					MainActivity.pager.setCurrentItem(2);
					elvMain = (ExpandableListView) getActivity().findViewById(R.id.elvMain);
					GetNastr(sn);
					expListAdapter  = new ElemAdapter(getActivity(),groupNames, elems );
					elvMain.setAdapter(expListAdapter);
					MainActivity.pager.setCurrentItem(1);
					Init();
					CreateSPList();
					ClView();
				}
			});

		}
		else if(pageNumber == 1){
			 /* работа на второй странице*/

			itog=(float) 0;
			prodid = 0;
			view = inflater.inflate(R.layout.fproducts, null);
			acPN = (AutoCompleteTextView ) view.findViewById(R.id.acPN);
			acPE = (AutoCompleteTextView ) view.findViewById(R.id.acPE);
			etSName= (EditText) view.findViewById(R.id.etSName);
			etCount = (EditText) view.findViewById(R.id.etCount);
			etPrice = (EditText) view.findViewById(R.id.etPrice);
			chbvagno = (CheckBox) view.findViewById(R.id.chbvagno);
			tvItog = (TextView) view.findViewById(R.id.tvItog);
			tvItogtxt = (TextView) view.findViewById(R.id.tvItogtxt);
			etCount.setVisibility(View.GONE);
			etPrice.setVisibility(View.GONE);
			chbvagno.setVisibility(View.GONE);
			acPE.setVisibility(View.GONE);
			lvData = (ListView) view.findViewById(R.id.lvData);
			CreateListProducts();
			CreateSPList ();
			FindMaxSP();
			getCursor(sn);
			tvItog.setText(String.format("%.2f",itog));
			ControlFonts();

			acPE.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					CreateListEdin();
					return false;
				}
			});

			//процедура нажатия на EditText, изменение фокуса
			etSName.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					etSName.clearFocus();
					etSName.setFocusable(true);
					etSName.setFocusableInTouchMode(true);
					return false;
				}
			});

			//процедура проверки sn при изменении фокуса
			etSName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					// TODO Auto-generated method stub
					ControlSN();
					String strName = etSName.getText().toString();
					db.UpDateNSp(DB_STABLE, strName ,sn);
					getActivity().getSupportLoaderManager().getLoader(0).forceLoad();
				}
			});

			//процедура изменения названия списка
			etSName.setOnKeyListener(new View.OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					if((event.getAction() == KeyEvent.ACTION_DOWN )&&
							(keyCode == KeyEvent.KEYCODE_TAB ||
									keyCode == KeyEvent.KEYCODE_ENTER))
					{
						// сохраняем текст, введенный до нажатия Enter в переменную
						String strName = etSName.getText().toString();
						db.UpDateNSp(DB_STABLE, strName ,sn);
						getActivity().getSupportLoaderManager().getLoader(0).forceLoad();
						etSName.setFocusable(false);
						etSName.setFocusableInTouchMode(false);
						return true;
					}
					return false;
				}
			});

			//процедура долгого нажатия на строку в ListView
			lvData.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent,
											   View view, final int position, long id) {
					// TODO Auto-generated method stub
					TextView textView1 = (TextView) view.findViewById(R.id.tvPN);
					//проверяем, если продукт в корзине, то долгое нажатие не сработает пока не выкинем с корзины
					if ((textView1.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) == 0)
					{
						prodid = (int) id;
						float tv2 = 0;
						String tv3 = "";
						float tv4 = 0;
						int tv7 = 0;
						lvData.setClickable(false);
						etCount.setVisibility(View.VISIBLE);
						acPE.setVisibility(View.VISIBLE);
						etPrice.setVisibility(View.VISIBLE);
						chbvagno.setVisibility(View.VISIBLE);
						btDel.setVisibility(View.VISIBLE);
						btSave.setVisibility(View.VISIBLE);
						TextView textView2 = (TextView) view.findViewById(R.id.tvPK);
						TextView textView3 = (TextView) view.findViewById(R.id.tvPE);
						TextView textView4 = (TextView) view.findViewById(R.id.tvPP);
						if ((textView2.getText().toString().length()==0) ||
								(textView3.getText().toString().length()==0) ||
								(textView4.getText().toString().length()==0) )
						{
							cursor = db.getSpisok2(prodid);
							if (cursor.getCount() != 0) {
								cursor.moveToFirst();
								do {
									tv2 = Float.parseFloat(cursor.getString(cursor.getColumnIndex("skol")).replace(',', '.'));
									tv3 = cursor.getString(cursor.getColumnIndex("ename"));
									tv4 = Float.parseFloat(cursor.getString(cursor.getColumnIndex("sprice")).replace(',', '.'));
								} while (cursor.moveToNext());
							}
							cursor.close();
						}
						acPN.setText(textView1.getText().toString());
						if (textView2.getText().toString().length()==0)
							etCount.setText(Float.toString(tv2));
						else
							etCount.setText(textView2.getText().toString());
						if (textView3.getText().toString().length()==0)
							acPE.setText(tv3);
						else
							acPE.setText(textView3.getText().toString());
						if ((textView2.getText().toString().length()==0) ||
								(textView4.getText().toString().length()==0) )
							etPrice.setText(Float.toString(tv4));
						else {
							Float tv5 = Float.parseFloat(textView4.getText().toString().replace(',', '.'));
							Float tv6 = Float.parseFloat(textView2.getText().toString().replace(',', '.'));
							Float tv56 =round(tv5/tv6,2);
							etPrice.setText(Float.toString(tv56));
						}
						if (textView4.getVisibility()==View.GONE)
							etPrice.setEnabled(false);
						else
							etPrice.setEnabled(true);
						cursor = db.getSpisok2(prodid);
						if (cursor.getCount() != 0) {
							cursor.moveToFirst();
							do {
								tv7 = Integer.parseInt(cursor.getString(cursor.getColumnIndex("svagno")));
							} while (cursor.moveToNext());
						}
						cursor.close();
						if ( tv7==1)
							chbvagno.setChecked(true);
						else
							chbvagno.setChecked(false);
						etCount.requestFocus();
					}
					return true;
				}
			});

			//присвоение и обработка кнопки "Добавить продукт к списку"
			btnAdd = (Button) view.findViewById(R.id.btnAdd);
			btnAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (acPN.length() == 0)
						Toast.makeText(getActivity(), "Введите продукт!", Toast.LENGTH_LONG).show();
					else if (etCount.length() == 0)
						Toast.makeText(getActivity(), "Введите количество продукта", Toast.LENGTH_LONG).show();
					else if (acPE.length() == 0)
						Toast.makeText(getActivity(), "Введите единицу продукта", Toast.LENGTH_LONG).show();
					else if (etPrice.length() == 0)
						Toast.makeText(getActivity(), "Введите цену продукта", Toast.LENGTH_LONG).show();
					else {
						if ((Float.parseFloat(etCount.getText().toString()) <= 0) ||
								(Float.parseFloat(etPrice.getText().toString()) <= 0) )
							Toast.makeText(getActivity(), "Количество и цена должны быть больше нуля", Toast.LENGTH_LONG).show();
						else
						{
							ControlSN();
							addProdInSpisok();
							acPN.showDropDown();
							GetNastr(sn);
							CreateSPList();
							etSName.setFocusable(false);
							etSName.setFocusableInTouchMode(false);
							SumInKorz(sn);
						}
					}
				}
			});

			//присвоение и обработка кнопки "Удалить продукт с списка"
			btDel = (Button) view.findViewById(R.id.btDel);
			btDel.setVisibility(View.GONE);
			btDel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ClView();
					SumInKorz(prodid);
					db.delRecPS(DB_STABLE,prodid);
					ControlSN();
					lvData = (ListView) getActivity().findViewById(R.id.lvData);
					getActivity().getSupportLoaderManager().getLoader(1).forceLoad();
					lvData.setClickable(true);
					etCount.setVisibility(View.GONE);
					acPE.setVisibility(View.GONE);
					etPrice.setVisibility(View.GONE);
					chbvagno.setVisibility(View.GONE);
					btDel.setVisibility(View.GONE);
					btSave.setVisibility(View.GONE);
				}
			});

			//присвоение и обработка кнопки "Сохранить продукт в списке"
			btSave = (Button) view.findViewById(R.id.btSave);
			btSave.setVisibility(View.GONE);
			btSave.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					if (acPN.length() == 0)
						Toast.makeText(getActivity(), "Введите продукт!", Toast.LENGTH_LONG).show();
					else if (etCount.length() == 0)
						Toast.makeText(getActivity(), "Введите количество продукта", Toast.LENGTH_LONG).show();
					else if (acPE.length() == 0)
						Toast.makeText(getActivity(), "Введите единицу продукта", Toast.LENGTH_LONG).show();
					else if (etPrice.length() == 0)
						Toast.makeText(getActivity(), "Введите цену продукта", Toast.LENGTH_LONG).show();
					else {
						if ((Float.parseFloat(etCount.getText().toString()) <= 0) ||
								(Float.parseFloat(etPrice.getText().toString()) <= 0) )
							Toast.makeText(getActivity(), "Количество и цена должны быть больше нуля", Toast.LENGTH_LONG).show();
						else
						{
							UpdateProdInSpisok(prodid);
							getActivity().getSupportLoaderManager().getLoader(1).forceLoad();
							ClView();
							lvData.setClickable(true);
							etCount.setVisibility(View.GONE);
							acPE.setVisibility(View.GONE);
							etPrice.setVisibility(View.GONE);
							chbvagno.setVisibility(View.GONE);
							btDel.setVisibility(View.GONE);
							btSave.setVisibility(View.GONE);
							SumInKorz(prodid);
						}
					}
				}
			});

		}
		else if(pageNumber == 2) {
			/* работа на третьей странице*/

			view = inflater.inflate(R.layout.nastroy, null);

			elvMain = (ExpandableListView) view.findViewById(R.id.elvMain);
			FindMaxSP();
			GetNastr(sn);
			expListAdapter  = new ElemAdapter(getActivity(),groupNames, elems );
			expListAdapter.notifyDataSetChanged();
			elvMain.setAdapter(expListAdapter);
			lvData = (ListView) getActivity().findViewById(R.id.lvData);
			CreateSPList();
			// нажатие на элемент   
			elvMain.setOnChildClickListener(this);
			elvMain.setOnGroupClickListener(this);

		}
		else
			view = inflater.inflate(R.layout.fragment, null);

		return view;

	}

	//матем.округление
	public static float round(float value, int scale) {
		return (float) (Math.round(value * Math.pow(10, scale)) / Math.pow(10, scale));
	}

	public boolean requery (){
		spisCursor.close();
		spisCursor2.close();
		prodCursor.close();
		edinCursor.close();
		spprCursor.close();
		cursor.close();
		return  true;
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		db.close();
		pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
	}

	@Override
	public void onResume() {
		super.onResume();
		pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
		Init();
		if (pageNumber==2)	{
			getActivity().getSupportLoaderManager().getLoader(1).forceLoad();
			GetNastr(sn);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
	}

	@Override
	public void onStop() {
		super.onStop();
		pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
	}

	//процедура проверки sn
	public void ControlSN() {
		String namespasok = etSName.getText().toString();
		int sn_x=0;
		cursor = db.getNameSpisok(namespasok);
		if (cursor.getCount()>0) {
			int iid = cursor.getColumnIndex(S_ID);
			cursor.moveToFirst();
			do {
				if (!cursor.isNull(iid)) {
					sn_x= Integer.parseInt(cursor.getString(iid));
				}
			} while (cursor.moveToNext());
			cursor.close();
			if (sn_x!=sn) sn= sn_x;
		}
	}

	//процедура изменения фона для текста
	public void ControlFonts() {
		tvItog = (TextView) getActivity().findViewById(R.id.tvItog);
		tvItogtxt = (TextView) getActivity().findViewById(R.id.tvItogtxt);
		if (tvItog!= null)
		{
			tvItog.setTypeface(faceRM);
			tvItog.setTypeface(null, Typeface.BOLD);
		}
		if (tvItogtxt!= null)
		{
			tvItogtxt.setTypeface(faceRM);
			tvItogtxt.setTypeface(null, Typeface.BOLD);
		}
		if (etSName!= null)
			etSName.setTypeface(faceHN);
		if (acPN!= null)
			acPN.setTypeface(faceRLI);
		if (etCount!= null)
			etCount.setTypeface(faceRLI);
		if (acPE!= null)
			acPE.setTypeface(faceRLI);
		if (etPrice!= null)
			etPrice.setTypeface(faceRLI);
	}

	//процедура подсчета суммы
	private void SumInKorz(long idsm) {
		float sm=0;
		itog = (float) 0;
		cursor = db.getSumInKor(idsm);
		if (cursor.getCount()>0) {
			cursor.moveToFirst();
			do {
				sm= sm + Float.parseFloat(cursor.getString(cursor.getColumnIndex("sm")));
			} while (cursor.moveToNext());
			cursor.close();
		} else
			sm = 0;
		itog = itog +sm;
		tvItog= (TextView) getActivity().findViewById(R.id.tvItog);
		if (tvItog != null  || itog != 0)
			tvItog.setText(String.format("%.2f",itog));
		getActivity().getSupportLoaderManager().getLoader(1).forceLoad();
	}

	//процедура 
	public void Init() {
		etSName= (EditText) getActivity().findViewById(R.id.etSName);
		tvItog= (TextView) getActivity().findViewById(R.id.tvItog);
		lvData = (ListView) getActivity().findViewById(R.id.lvData);
		tvItogtxt = (TextView) getActivity().findViewById(R.id.tvItogtxt);
		tvItog = (TextView) getActivity().findViewById(R.id.tvItog);
		if (pr==0)
		{
			tvItogtxt.setVisibility(View.GONE);
			tvItog.setVisibility(View.GONE);
		} else {
			tvItogtxt.setVisibility(View.VISIBLE);
			tvItog.setVisibility(View.VISIBLE);
		}
	}

	// процедура добавления нового списка (первая страница)
	private void addNewSpisok() {
		itog=(float) 0;
		spisCursor2 = db.getMaxSpisok(DB_STABLE);
		max_nom = 0;
		int id_nom = spisCursor2.getColumnIndex(S_NOM);
		spisCursor2.moveToFirst();
		do {
			if (!spisCursor2.isNull(id_nom))
				max_nom= Integer.parseInt(spisCursor2.getString(id_nom));
		} while (spisCursor2.moveToNext());
		spisCursor2.close();
		max_nom = max_nom+1;
		//добавили в базу новый список
		db.addNewSpisok(DB_STABLE, max_nom ,"Список "+max_nom);
		spisCursor2 = db.getNastrSN(max_nom);
		if (spisCursor2.getCount()==0)
			db.addNewNastr("nastr", max_nom );
		spisCursor2.close();
		cursor = db.getMaxSpisok(DB_STABLE);
		int iid = cursor.getColumnIndex(S_ID);
		cursor.moveToFirst();
		do {
			if (!cursor.isNull(iid)) {
				sn= Integer.parseInt(cursor.getString(iid));
			}
		} while (cursor.moveToNext());
		cursor.close();
	}

	//процедура сохранения цены для продукта
	private void savePriceProd(int p_id, float ip){
		cursor = db.getProdPrice(p_id);
		int id_CC = cursor.getCount();
		if (id_CC == 0)
			db.addPriceProd("pprice",p_id,ip);
		else
			db.upPriceProd("pprice",ip,p_id);
		cursor.close();
	}

	//процедура добавления нового продукта в список (вторая страница)
	private void addProdInSpisok(){
		//ищем данные для sn (номер, название, дата)
		cursor = db.getSpisok(sn,1);
		int csnom = 0;
		String csname = "";
		String csdate = "";
		int iid = cursor.getColumnIndex(S_ID);
		cursor.moveToFirst();
		do {
			if (!cursor.isNull(iid)) {
				csnom= Integer.parseInt(cursor.getString(cursor.getColumnIndex(S_NOM)));
				csname= cursor.getString(cursor.getColumnIndex(S_NAME));
				csdate= cursor.getString(cursor.getColumnIndex(S_DATE));
				val_id = cursor.getInt(cursor.getColumnIndex("vid"));
			}
		} while (cursor.moveToNext());
		cursor.close();
		//ищем id продукта по названию 
		int p_id=0;
		cursor = db.getProdNM(acPN.getText().toString());
		int id_C = cursor.getCount();
		if (id_C == 0) {
			db.addProd(DB_PTABLE, 20, asUpperCaseFirstChar(acPN.getText().toString()), 11);
			cursor.close();
			cursor = db.getProdNM(asUpperCaseFirstChar(acPN.getText().toString()));
		}
		int pid = cursor.getColumnIndex(P_ID);
		cursor.moveToFirst();
		do {
			if (!cursor.isNull(pid)) {
				p_id= Integer.parseInt(cursor.getString(pid));
			}
		} while (cursor.moveToNext());
		cursor.close();
		//если проверяем с галочкой или без чекетбокс (важность)
		int chv = 0;
		if (chbvagno.isChecked()==true)
			chv=1;
		else
			chv=0;
		//ищем id единицы по названию 
		int e_id=0;
		cursor = db.getEdinName(DB_ETABLE,acPE.getText().toString());
		int id_CE = cursor.getCount();
		if (id_CE == 0) {
			db.addEdin(DB_ETABLE, acPE.getText().toString());
			cursor.close();
			cursor = db.getEdinName(DB_ETABLE,acPE.getText().toString());
		}
		int eid = cursor.getColumnIndex(E_ID);
		cursor.moveToFirst();
		do {
			if (!cursor.isNull(eid)) {
				e_id= Integer.parseInt(cursor.getString(eid));
			}
		} while (cursor.moveToNext());
		cursor.close();
		Float ik = Float.parseFloat(etCount.getText().toString().replace(',', '.'));
		Float ip = Float.parseFloat(etPrice.getText().toString().replace(',', '.'));
		//добавляем новый продукт в список
		db.addProdSpisok(DB_STABLE, csnom ,csname,csdate,p_id,ik,ip,chv,0,e_id,val_id);
		getActivity().getSupportLoaderManager().getLoader(1).forceLoad();
		savePriceProd(p_id,ip);
		Init();
		ClView();
	}

	//процедура обновления продукта в списке (вторая страница)
	private void UpdateProdInSpisok(int txt){
		//ищем id продукта по названию 
		int p_id=0;
		cursor = db.getProdNM(acPN.getText().toString());
		int id_C = cursor.getCount();
		if (id_C == 0) {
			db.addProd(DB_PTABLE, 13, asUpperCaseFirstChar(acPN.getText().toString()), 11);
			cursor.close();
			cursor = db.getProdNM(asUpperCaseFirstChar(acPN.getText().toString()));
		}
		int pid = cursor.getColumnIndex(P_ID);
		cursor.moveToFirst();
		do {
			if (!cursor.isNull(pid)) {
				p_id= Integer.parseInt(cursor.getString(pid));
			}
		} while (cursor.moveToNext());
		cursor.close();
		//если проверяем с галочкой или без чекетбокс (важность)
		int chv = 0;
		if (chbvagno.isChecked()==true)
			chv=1;
		else
			chv=0;
		//ищем id единицы по названию 
		int e_id=0;
		cursor = db.getEdinName(DB_ETABLE,acPE.getText().toString());
		int id_СЃE = cursor.getCount();
		if (id_СЃE == 0) {
			db.addEdin(DB_ETABLE, acPE.getText().toString());
			cursor.close();
			cursor = db.getEdinName(DB_ETABLE,acPE.getText().toString());
		}
		int eid = cursor.getColumnIndex(E_ID);
		cursor.moveToFirst();
		do {
			if (!cursor.isNull(eid)) {
				e_id= Integer.parseInt(cursor.getString(eid));
			}
		} while (cursor.moveToNext());
		cursor.close();
		Float ik = Float.parseFloat(etCount.getText().toString().replace(',', '.'));
		Float ip = Float.parseFloat(etPrice.getText().toString().replace(',', '.'));
		//добавляем новый продукт в список
		db.upProdSpisok(DB_STABLE, p_id,ik,ip,chv,e_id,txt);
		getActivity().getSupportLoaderManager().getLoader(1).forceLoad();
		savePriceProd(p_id,ip);
	}

	private void FindMaxSP() {
		spisCursor2 = db.getMaxSpisok(DB_STABLE);
		int id_id = spisCursor2.getColumnIndex(S_ID);
		spisCursor2.moveToFirst();
		do {
			if (!spisCursor2.isNull(id_id)) {
				sn= Integer.parseInt(spisCursor2.getString(id_id));
			} else {
				db.addNewSpisok(DB_STABLE, 1 ,"Список "+1);
				cursor = db.getMaxSpisok(DB_STABLE);
				int iid = cursor.getColumnIndex(S_ID);
				cursor.moveToFirst();
				do {
					if (!cursor.isNull(iid)) {
						sn= Integer.parseInt(cursor.getString(iid));
					}
				} while (cursor.moveToNext());
				cursor.close();
			}
		} while (spisCursor2.moveToNext());
		spisCursor2.close();
	}

	//процедура очиски полей для второй страницы
	private void ClView() {
		if (acPN != null && !acPN.isPopupShowing())
			this.acPN.setText("");
		if (etCount != null ) {
			etCount.setText("");
			etCount.setVisibility(View.GONE);
		}
		if (acPE != null && !acPE.isPopupShowing()) {
			acPE.setVisibility(View.GONE);
			acPE.setText("");
		}
		if (etPrice != null ) {
			etPrice.setText("");
			etPrice.setVisibility(View.GONE);
		}
		if (chbvagno != null ) {
			if (chbvagno.isChecked() == true)
				chbvagno.setChecked(false);
			chbvagno.setVisibility(View.GONE);
		}
		if (btDel != null ) {
			btDel.setVisibility(View.GONE);
		}
		if (btSave != null ) {
			btSave.setVisibility(View.GONE);
		}
		if (tvItog != null)
			tvItog.setText(String.format("%.2f",itog));
	}

	//Данные списка listView (первая страница)
	private void CreateListSpisok() {
		String[] from = new String[] { S_NAME, S_DATE };
		int[] to = new int[] {R.id.tvSS , R.id.tvSSinf};
		scAdapter = new SimpleCursorAdapter(getActivity(), R.layout.sspisoklist, null, from, to);
		scAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int column) {
				switch (view.getId()) {
					case R.id.tvSS:
						TextView tvSStmp = (TextView) view.findViewById(R.id.tvSS);
						tvSStmp.setText(cursor.getString(cursor.getColumnIndex(S_NAME)));
						tvSStmp.setTypeface(faceHN);
						return true;
					case R.id.tvSSinf:
						TextView tvSSinftmp = (TextView) view.findViewById(R.id.tvSSinf);
						tvSSinftmp.setText(cursor.getString(cursor.getColumnIndex(S_DATE)));
						tvSSinftmp.setTypeface(faceHN);
						return true;
				}
				return false;
			}
		});
		lvSS.setAdapter(scAdapter);
		//присвоили контекстное меню для списка
		registerForContextMenu(lvSS);
		// создаем лоадер для чтения данных
		getActivity().getSupportLoaderManager().initLoader(0, null, this);
	}

	//Данные списка listView (вторая страница)
	public void CreateSPList () {
		String[] from = new String[] {"kc", "skorz", "pname","ename", "skol", "abv", "sprice" };
		int[] to = new int[] {R.id.ivP,R.id.ivKorz,R.id.tvPN,R.id.tvPE,R.id.tvPK,R.id.tvPV,R.id.tvPP};
		spAdapter = new SimpleCursorAdapter(getActivity(), R.layout.fprodlist, spprCursor, from, to);
		// spAdapter = new SimpleCursorAdapter(getActivity(), R.layout.fprodlist, null, from, to);
		spAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int column) {
				switch (view.getId()) {
					case R.id.ivP:
						ImageView iv = (ImageView) view.findViewById(R.id.ivP);
						if (ik==0) {
							iv.setVisibility(View.GONE);
						} else {
							String kcol = cursor.getString(cursor.getColumnIndex("kc"));
							iv.setVisibility(View.VISIBLE);
							iv.setBackgroundColor(Color.parseColor(kcol));
						}
						return true;
					case R.id.ivKorz:
						int kzz = Integer.parseInt(cursor.getString(cursor.getColumnIndex("skorz")));
						ImageView ivk = (ImageView) view.findViewById(R.id.ivKorz);
						if (kzz==1)
							ivk.setBackgroundResource(R.drawable.bullchecked);
						else {
							int vagn = Integer.parseInt(cursor.getString(cursor.getColumnIndex("svagno")));
							if (vagn==1)
								ivk.setBackgroundResource(R.drawable.bullred);
							else
								ivk.setBackgroundResource(R.drawable.bull);
						}
						final int idi = Integer.parseInt(cursor.getString(cursor.getColumnIndex("_id")));
						ivk.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								int sk=0;
								spisCursor2 = db.getSkorSpisok(idi);
								int pid = spisCursor2.getColumnIndex(S_ID);
								spisCursor2.moveToFirst();
								do {
									if (!spisCursor2.isNull(pid)) {
										sk = Integer.parseInt(spisCursor2.getString(spisCursor2.getColumnIndex("skorz")));
									}
								} while (spisCursor2.moveToNext());
								spisCursor2.close();
								if (sk==0)
									db.UpDateKSp(DB_STABLE,1, idi);
								else
									db.UpDateKSp(DB_STABLE,0, idi);
								SumInKorz(idi);
							}
						});
						return true;
					case  R.id.tvPN:
						int kz = Integer.parseInt(cursor.getString(cursor.getColumnIndex("skorz")));
						TextView tPN = (TextView) view.findViewById(R.id.tvPN);
						if (kz==1)
						{
							tPN.setPaintFlags(tPN.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
							tPN.setTextColor(Color.parseColor("#BEBEBE"));
						}
						else
						{
							tPN.setPaintFlags(tPN.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
							tPN.setTextColor(Color.parseColor("#000000"));
						}
						tPN.setText(cursor.getString(cursor.getColumnIndex("pname")));
						tPN.setTypeface(faceHN);
						return true;
					case  R.id.tvPK:
						int kz1 = Integer.parseInt(cursor.getString(cursor.getColumnIndex("skorz")));
						TextView tvPK = (TextView) view.findViewById(R.id.tvPK);
						if (kv==0)
							tvPK.setVisibility(View.GONE);
						else {
							tvPK.setVisibility(View.VISIBLE);
							tvPK.setText(cursor.getString(cursor.getColumnIndex("skol")));
							tvPK.setTypeface(faceHN);
							if (kz1==1)
								tvPK.setTextColor(Color.parseColor("#BEBEBE"));
							else
								tvPK.setTextColor(Color.parseColor("#000000"));
							if (pr==0)
								tvPK.setTextSize(20);
							else
								tvPK.setTextSize(10);

						}
						return true;
					case  R.id.tvPE:
						int kz2 = Integer.parseInt(cursor.getString(cursor.getColumnIndex("skorz")));
						TextView tvPE = (TextView) view.findViewById(R.id.tvPE);
						if (kv==0)
							tvPE.setVisibility(View.GONE);
						else {
							tvPE.setVisibility(View.VISIBLE);
							tvPE.setText(cursor.getString(cursor.getColumnIndex("ename")));
							tvPE.setTypeface(faceHN);
							if (kz2==1)
								tvPE.setTextColor(Color.parseColor("#BEBEBE"));
							else
								tvPE.setTextColor(Color.parseColor("#000000"));
							if (pr==0)
								tvPE.setTextSize(20);
							else
								tvPE.setTextSize(10);
						}
						return true;
					case  R.id.tvPV:
						int kz3 = Integer.parseInt(cursor.getString(cursor.getColumnIndex("skorz")));
						TextView tvPV = (TextView) view.findViewById(R.id.tvPV);
						if (ei==0)
							tvPV.setVisibility(View.GONE);
						else {
							if (pr==0)
								tvPV.setVisibility(View.GONE);
							else {
								tvPV.setVisibility(View.VISIBLE);
								tvPV.setText(cursor.getString(cursor.getColumnIndex("abv")));
								tvPV.setTypeface(faceHN);
								if (kz3==1)
									tvPV.setTextColor(Color.parseColor("#BEBEBE"));
								else
									tvPV.setTextColor(Color.parseColor("#000000"));
							}
						}
						return true;

					case  R.id.tvPP:
						int kz4 = Integer.parseInt(cursor.getString(cursor.getColumnIndex("skorz")));
						TextView tv = (TextView) view.findViewById(R.id.tvPP);
						if (pr==0)
							tv.setVisibility(View.GONE);
						else {
							Float sk = Float.parseFloat(cursor.getString(cursor.getColumnIndex("skol")).replace(',', '.'));
							Float sp = Float.parseFloat(cursor.getString(cursor.getColumnIndex("sprice")).replace(',', '.'));
							Float it = sp*sk;
							tv.setVisibility(View.VISIBLE);
							tv.setText(String.format("%.2f",it));
							tv.setTypeface(faceHN);
							if (kz4==1)
								tv.setTextColor(Color.parseColor("#BEBEBE"));
							else
								tv.setTextColor(Color.parseColor("#000000"));
						}
						Init();
						return true;
				}
				return false;
			}
		});
		lvData.setAdapter(spAdapter);
		// создаем лоадер для чтения данных
		getActivity().getSupportLoaderManager().initLoader(1, null, this);
	}

	//процедура автозаполнения продукта (вторая страница)
	private void CreateListProducts() {
		prodCursor = db.getProdName(DB_PTABLE,"");
		String[] from = new String[] { P_NAME };
		int[] to = new int[] {R.id.text1};

		pcAdapter = new SimpleCursorAdapter(getActivity(),R.layout.item, prodCursor, from, to);
		// указываем какое имя вставлять после выбора всплывающего списка.
		//   Если этого не будет, то в название получите имя объекта в формате java
		pcAdapter.setStringConversionColumn(prodCursor.getColumnIndexOrThrow(P_NAME));
		//  настраиваем фильтрацию (что бы всплывали подсказки)
		pcAdapter.setFilterQueryProvider(new FilterQueryProvider() {
			public Cursor runQuery(CharSequence constraint) {
				String partialValue = null;
				if (constraint != null) {
					partialValue = constraint.toString();
				}
				return db.getProdName(DB_PTABLE,partialValue);
			}
		});
		acPN.setAdapter(pcAdapter);
		pcAdapter.notifyDataSetChanged();
		acPN.showDropDown();
		// Событие возникающее при изменение вводимого текста
		acPN.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				etCount.setVisibility(View.VISIBLE);
				etPrice.setVisibility(View.VISIBLE);
				chbvagno.setVisibility(View.VISIBLE);
				acPE.setVisibility(View.VISIBLE);
				etPrice.setText("1");
				acPE.setText("шт.");
			}
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {
				if (spAdapter.getCount()==0)
					itog = (float) 0;
			}
			public void afterTextChanged(Editable s) {
				pcAdapter.getFilter().filter(asUpperCaseFirstChar(s.toString()));
			}
		});
		//процедура изменения фокуса
		acPN.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				//проверка активна ли сумма
				ControlSN();
				spisCursor2 = db.getNastr(sn);
				int prtxt = 0;
				int id_CC= spisCursor2.getCount();
				if(id_CC>0) {
					spisCursor2.moveToFirst();
					do {
						prtxt = Integer.parseInt(spisCursor2.getString(spisCursor2.getColumnIndex("price")));
					} while (spisCursor2.moveToNext());
				}
				spisCursor2.close();
				if (prtxt==0)
					etPrice.setEnabled(false);
				else
					etPrice.setEnabled(true);
			}
		});
		// Событие при выборе элемента
		acPN.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> adapter, View view, int index, long id) {
				CreateListEdin();
				spisCursor2 = db.getProdED(id);
				int id_Col = spisCursor2.getCount();
				String txt = "";
				if(id_Col>0) {
					int eid = spisCursor2.getColumnIndex("ename");
					spisCursor2.moveToFirst();
					do {
						txt = spisCursor2.getString(eid);
					} while (spisCursor2.moveToNext());
				}
				spisCursor2.close();
				acPE.setText(txt);

				//подстановка сохраненной цены
				spisCursor2 = db.getProdPrice(id);
				int id_C = spisCursor2.getCount();
				float pri  = (float) 0;
				if(id_C>0) {
					spisCursor2.moveToFirst();
					do {
						pri = Float.parseFloat(spisCursor2.getString(spisCursor2.getColumnIndex("prices")).replace(',', '.'));
					} while (spisCursor2.moveToNext());
				}
				spisCursor2.close();
				etPrice.setText(Float.toString(pri));
				etCount.requestFocus();

			}
		});
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

	//процедура автозаполнения единицы (вторая страница)
	private void CreateListEdin() {
		edinCursor = db.getEdinName(DB_ETABLE,"");
		String[] from = new String[] { E_NAME};
		int[] to = new int[] {R.id.text2};
		ecAdapter = new SimpleCursorAdapter(getActivity(),R.layout.eitem, edinCursor, from, to);
		// указываем какое имя вставлять после выбора всплывающего списка. 
		//   Если этого не будет, то в название получите имя объекта в формате java
		ecAdapter.setStringConversionColumn(edinCursor.getColumnIndexOrThrow(E_NAME));
		// настраиваем фильтрацию (что бы всплывали подсказки)
		ecAdapter.setFilterQueryProvider(new FilterQueryProvider() {
			public Cursor runQuery(CharSequence constraint) {
				String partialValue = null;
				if (constraint != null) {
					partialValue = constraint.toString();
				}
				return db.getEdinName(DB_ETABLE,partialValue);
			}
		});
		acPE.setAdapter(ecAdapter);
		ecAdapter.notifyDataSetChanged();
		acPE.showDropDown();
		// Событие возникающее при изменение вводимого текста
		acPE.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {
			}
			public void afterTextChanged(Editable s) {
				ecAdapter.getFilter().filter(s.toString());
			}
		});
		// Событие при выборе элемента
		acPE.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> adapter, View view, int index, long id) {
				//  Object itemPostion = (Object) adapter.getItemAtPosition(index);
				//   Log.d(TAG, "Выбранный элемент=" + itemPostion.toString()+"\n Значение столбца _id="+id );
			}
		});
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		switch (arg0) {
			case 0:
				return new CursorLoader(getActivity()){
					@Override
					public Cursor loadInBackground() {
						spisCursor = db.getAllSpisok(DB_STABLE, null, S_NOM, S_NOM);
						if (spisCursor.getCount() == 0) {
							db.addNewSpisok(DB_STABLE, 1 ,"Список "+1);
							spisCursor = db.getAllSpisok(DB_STABLE, null, S_NOM, S_NOM);
						}
						return spisCursor;
					}
				};
			case 1:
				return new CursorLoader(getActivity()){
					@Override
					public Cursor loadInBackground() {
						if ((sn>0) && (MainActivity.pager.getCurrentItem()==1))
							ControlSN();
						spprCursor = db.getSpisok(sn,0);
						return spprCursor;
					}
				};
			default:
				return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
			case 0:
				if(scAdapter!=null && arg1!=null)
					scAdapter.swapCursor((android.database.Cursor) arg1);
				else
					Log.v(TAG,"OnLoadFinished: scAdapter is null");
				break;
			case 1:
				if(spAdapter!=null && arg1!=null) {
					spAdapter.swapCursor((android.database.Cursor) arg1);
				} else
					Log.v(TAG,"OnLoadFinished: spAdapter is null");
				break;
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
			case 0:
				if(scAdapter!=null)
					scAdapter.swapCursor(null);
				else
					Log.v(TAG,"OnLoadFinished: scAdapter is null");
				break;
			case 1:
				if(spAdapter!=null)
					spAdapter.swapCursor(null);
				else
					Log.v(TAG,"OnLoadFinished: spAdapter is null");
				break;
		}
	}

	public Cursor getCursor(int sppr) {
		itog = (float) 0;
		Cursor spprCursor = db.getSpisok(sppr,0);
		int id_Col = spprCursor.getCount();
		if(id_Col>0) {
			int id_id = spprCursor.getColumnIndex(S_ID);
			spprCursor.moveToFirst();
			do {
				if (etSName != null )
					etSName.setText(spprCursor.getString(spprCursor.getColumnIndex(S_NAME)));
				sn= Integer.parseInt(spprCursor.getString(id_id));
				Float ik = Float.parseFloat(spprCursor.getString(spprCursor.getColumnIndex("skol")).replace(',', '.'));
				Float ip = Float.parseFloat(spprCursor.getString(spprCursor.getColumnIndex("sprice")).replace(',', '.'));
				if (Integer.parseInt(spprCursor.getString(spprCursor.getColumnIndex("skorz")))==1)
					itog = itog + (ik*ip);
			} while (spprCursor.moveToNext());
		} else if(id_Col==0) {
			spisCursor2 = db.getSpisok(sppr,1);
			int id_C = spisCursor2.getCount();
			if(id_C>0) {
				int id_i = spisCursor2.getColumnIndex(S_ID);
				spisCursor2.moveToFirst();
				do {
					if (etSName != null )
						etSName.setText(spisCursor2.getString(spisCursor2.getColumnIndex(S_NAME)));
					sn= Integer.parseInt(spisCursor2.getString(id_i));
				} while (spisCursor2.moveToNext());
			}
			spisCursor2.close();
		}
		getActivity().getSupportLoaderManager().restartLoader(1, null, this);
		return spprCursor;
	}

	//процедура создания 3тьей страницы
	private void GetNastr(int sns) {
		ik=0; 	kv=0;	ei=0;	pr=0;
		vl=0;	ek=0; nid=0;
		spisCursor2 = db.getNastr(sns);
		if (spisCursor2.getCount()>0) {
			nid = spisCursor2.getColumnIndex("_id");
			spisCursor2.moveToFirst();
			do {
				if (!spisCursor2.isNull(nid)) {
					nid =Integer.parseInt(spisCursor2.getString(spisCursor2.getColumnIndex("_id")));
					nsn =Integer.parseInt(spisCursor2.getString(spisCursor2.getColumnIndex("sn")));
					ik =Integer.parseInt(spisCursor2.getString(spisCursor2.getColumnIndex("kateg")));
					kv=Integer.parseInt(spisCursor2.getString(spisCursor2.getColumnIndex("kolvo")));
					ei=Integer.parseInt(spisCursor2.getString(spisCursor2.getColumnIndex("edizm")));
					pr=Integer.parseInt(spisCursor2.getString(spisCursor2.getColumnIndex("price")));
					ek=Integer.parseInt(spisCursor2.getString(spisCursor2.getColumnIndex("ekr")));
					vl=Integer.parseInt(spisCursor2.getString(spisCursor2.getColumnIndex("valuta")));
				}
			} while (spisCursor2.moveToNext());
			spisCursor2.close();
		}
		if ((nid == 0) && (nsn>0))
		{
			spisCursor2 = db.getNastrSN(nsn);
			if (spisCursor2.getCount()>0) {
				nid = spisCursor2.getColumnIndex("_id");
				spisCursor2.moveToFirst();
				do {
					if (!spisCursor2.isNull(nid)) {
						nid =Integer.parseInt(spisCursor2.getString(spisCursor2.getColumnIndex("_id")));
						nsn =Integer.parseInt(spisCursor2.getString(spisCursor2.getColumnIndex("sn")));
						ik =Integer.parseInt(spisCursor2.getString(spisCursor2.getColumnIndex("kateg")));
						kv=Integer.parseInt(spisCursor2.getString(spisCursor2.getColumnIndex("kolvo")));
						ei=Integer.parseInt(spisCursor2.getString(spisCursor2.getColumnIndex("edizm")));
						pr=Integer.parseInt(spisCursor2.getString(spisCursor2.getColumnIndex("price")));
						ek=Integer.parseInt(spisCursor2.getString(spisCursor2.getColumnIndex("ekr")));
						vl=Integer.parseInt(spisCursor2.getString(spisCursor2.getColumnIndex("valuta")));
					}
				} while (spisCursor2.moveToNext());
				spisCursor2.close();
			}
		}
		//Log.d(TAG, "процедура GetNastr: id="+nid+",sn="+nsn+", ik="+ik+", kv="+kv+", ei="+ei+", pr="+pr+", ek="+ek+", vl="+vl);
		groupNames = new ArrayList<String>();
		groupNames.add( "Действия со списком" );
		groupNames.add( "Справочники" );
		groupNames.add( "Функции" );

		elems = new ArrayList<ArrayList<Elements>>();
		elem = new ArrayList<Elements>();
		if (ik==1)
			elem.add( new Elements( "Использовать категории", true ) );
		else
			elem.add( new Elements( "Использовать категории", false ) );
		if (kv==1)
			elem.add( new Elements( "Отображать кол-во", true ) );
		else
			elem.add( new Elements( "Отображать кол-во", false ) );
		if (ei==1)
			elem.add( new Elements( "Отображать валюту", true ) );
		else
			elem.add( new Elements( "Отображать валюту", false ) );
		if (pr==1)
			elem.add( new Elements( "Отображать сумму", true ) );
		else
			elem.add( new Elements( "Отображать сумму", false ) );
		elems.add( elem );

		elem = new ArrayList<Elements>();
		elem.add( new Elements( "Категории",false ) );
		elem.add( new Elements( "Продукция",false ) );
		elem.add( new Elements( "Единицы",false ) );
		elems.add( elem );

		elem = new ArrayList<Elements>();
		elem.add( new Elements( "Валюта",false ) );
		if (ek==0)
			elem.add( new Elements( "Не выключать экран",false ) );
		else
			elem.add( new Elements( "Не выключать экран",true ) );
		elem.add( new Elements( "О программе", false ) );
		elems.add( elem );
	}

	//процедура нажатия на строк в listview (третья страница)
	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
								int groupPosition, int childPosition, long id) {
		// TODO Auto-generated method stub
	/*Log.d(TAG, "onChildClick groupPosition = " + groupPosition + 
			  " childPosition = " + childPosition + 
			  " id = " + id);*/
		//PowerManager - для запрета блокировки экрана
		PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
		ControlSN();
		CheckBox cb = (CheckBox)v.findViewById( R.id.chbnas );
		switch (groupPosition) {
			case 0:
				/*первая группа - Действия со списком*/
				if (childPosition==0)
				{
					if (cb.isChecked()==true)
					{
						db.UpDateNastr(DB_NTABLE,"kateg",0,sn);
						cb.setChecked(false);
					} else
					{
						db.UpDateNastr(DB_NTABLE,"kateg",1,sn);
						cb.setChecked(true);
					}
				}
				if (childPosition==1)
				{
					if (cb.isChecked()==true)
					{
						db.UpDateNastr(DB_NTABLE,"kolvo",0,sn);
						cb.setChecked(false);
					} else
					{
						db.UpDateNastr(DB_NTABLE,"kolvo",1,sn);
						cb.setChecked(true);
					}
				}
				if (childPosition==2)
				{
					if (cb.isChecked()==true)
					{
						db.UpDateNastr(DB_NTABLE,"edizm",0,sn);
						cb.setChecked(false);
					} else
					{
						db.UpDateNastr(DB_NTABLE,"edizm",1,sn);
						cb.setChecked(true);
					}
				}
				if (childPosition==3)
				{
					if (cb.isChecked()==true)
					{
						db.UpDateNastr(DB_NTABLE,"price",0,sn);
						cb.setChecked(false);
					} else
					{
						db.UpDateNastr(DB_NTABLE,"price",1,sn);
						cb.setChecked(true);
					}
				}
				MainActivity.pager.setCurrentItem(1);
				break;
			case 1:
				/*вторая группа - Справочники*/
				if (childPosition==0)
				{
					Intent intent = new Intent(getActivity(), BaseKateg.class);
					startActivity(intent);
				}
				if (childPosition==1)
				{
					Intent intent = new Intent(getActivity(), BaseProd.class);
					startActivity(intent);
				}
				if (childPosition==2)
				{
					Intent intent = new Intent(getActivity(), BaseEdin.class);
					startActivity(intent);
				}
				Init();
				lvData = (ListView) getActivity().findViewById(R.id.lvData);
				getActivity().getSupportLoaderManager().getLoader(1).forceLoad();
				GetNastr(sn);
				break;
			case 2:
				/*третья группа - Функции*/
				if (childPosition==0)
				{
					spisCursor2 = db.getValinSpis(sn);
					if (spisCursor2.getCount()>0) {
						spisCursor2.moveToFirst();
						do {
							val_id = spisCursor2.getInt(spisCursor2.getColumnIndex("vid"));
						} while (spisCursor2.moveToNext());
						spisCursor2.close();
					}
					Dialog dialog = new Dialog(getActivity());
					cursor = db.getAllValut();
					getActivity().startManagingCursor(cursor);
					AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
					adb.setTitle(R.string.cursor);
					if (val_id==0)
						adb.setSingleChoiceItems(cursor, -1, V_NAME, myClickListener);
					else
						adb.setSingleChoiceItems(cursor, val_id-1, V_NAME, myClickListener);
					adb.setPositiveButton(R.string.ok, myClickListener);
					dialog = adb.create();
					dialog.show();
				}
				if (childPosition==1)
				{
					if (cb.isChecked()==false) {
						getActivity().getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
						wl.acquire();
						cb.setChecked(true);
					} else {
						getActivity().getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
						if (wl.isHeld())
							wl.release();
						cb.setChecked(false);
					}
				}
				if (childPosition==2)
				{
					Dialog dialog = new Dialog(getActivity());
					AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
					adb.setIcon(R.drawable.info50);
					adb.setTitle(R.string.about);
					String alert1 = getResources().getText(R.string.proger).toString();
					String alert2 = getResources().getText(R.string.ver).toString();
					String alert3 = getResources().getText(R.string.text).toString();
					String alert4 = getResources().getText(R.string.dizain).toString();
					adb.setMessage(alert1+"\n"+alert4+"\n"+alert2+"\n"+alert3);
					adb.setPositiveButton("ok", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
					dialog = adb.create();
					dialog.show();
				}
				break;
		}
		elvMain = (ExpandableListView) getActivity().findViewById(R.id.elvMain);
		GetNastr(sn);
		expListAdapter  = new ElemAdapter(getActivity(),groupNames, elems );
		elvMain.setAdapter(expListAdapter);
		lvData = (ListView) getActivity().findViewById(R.id.lvData);
		CreateSPList();
		Init();
		return true;
	}

	//обработчик нажатия на пункт списка диалога или кнопку
	OnClickListener myClickListener = new OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			ListView lv = ((AlertDialog) dialog).getListView();
			if (which == Dialog.BUTTON_POSITIVE) {
				// выводим в лог позицию выбранного элемента
				ControlSN();
				db.UpDateValInSpis("spisok",lv.getCheckedItemPosition()+1,sn);
				getActivity().getSupportLoaderManager().getLoader(1).forceLoad();
				GetNastr(sn);
			}
		}
	};

	//процедура сворачивание(Collapse) / разворачивание(Expand) групп 
	@Override
	public boolean onGroupClick(ExpandableListView parent, View v,
								int groupPosition, long id) {
		// TODO Auto-generated method stub
		switch (groupPosition) {
			case 0:
				if (parent.isGroupExpanded(0))
					parent.collapseGroup(0);
				else
				{
					parent.expandGroup(0);
					parent.collapseGroup(1);
					parent.collapseGroup(2);
				}
				break;
			case 1:
				if (parent.isGroupExpanded(1))
					parent.collapseGroup(1);
				else
				{
					parent.expandGroup(1);
					parent.collapseGroup(0);
					parent.collapseGroup(2);
				}
				break;
			case 2:
				if (parent.isGroupExpanded(2))
					parent.collapseGroup(2);
				else
				{
					parent.expandGroup(2);
					parent.collapseGroup(0);
					parent.collapseGroup(1);
				}
				break;
		}
		return true;
	}



}