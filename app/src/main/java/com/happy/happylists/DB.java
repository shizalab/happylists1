package com.happy.happylists;

import java.sql.Date;
import java.text.SimpleDateFormat;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DB {

	static final String TAG = "myLogs";

	private SQLiteDatabase database;

	private final Context mCtx;

	public DB(Context ctx) {
		mCtx = ctx;
	}

	// открыть подключение
	public void open() {
		//Наш ключевой хелпер
		if (database==null) {
			ExternalDbOpenHelper dbOpenHelper = new ExternalDbOpenHelper(mCtx, "Pokypka");
			database = dbOpenHelper.openDataBase();
			//  Log.d(TAG, "открыли базу ");
		}
		//Все, база открыта!
	}

	// закрыть подключение
	public void close() {
		if (database!=null) {
			database.close();
			//	Log.d(TAG, "закрыли базу ");
		}
	}


	public Cursor getMaxSpisok(String DB_TABLE) {
		return database.rawQuery("Select max(snom) as snom, sname, _id  from "+DB_TABLE, null);
	}

	// получить все данные из таблицы DB_TABLE
	public Cursor getAllSpisok(String DB_TABLE, String DB_SEL,String DB_GR,String DB_OB) {
		return database.query(DB_TABLE, null, null, null, DB_GR, null, DB_OB);
	}

	// получить все данные из таблицы DB_TABLE
	public Cursor getSpisok(int DB_SEL, int ysl) {
		if (ysl==0)
			return database.rawQuery("Select s._id as _id, s.snom,s.sname,p.pname,e.ename,skol,sprice,svagno,skorz,v.abv , (select kcolor from kategor where _id=p.kid ) as kc from spisok s, products p, Edin e, valuta v where s.pid=p._id and s.eid=e._id and s.vid=v._id and s.snom =(select snom from Spisok where _id ="+DB_SEL+") order by skorz,p.kid", null);
		else
			return database.rawQuery("select _id, sname,snom,sdate,vid from Spisok where _id ="+DB_SEL, null);
	}

	//получить все данные из таблицы DB_TABLE
	public Cursor getSpisok2(int DB_SEL) {
		return database.rawQuery("Select s._id as _id, s.snom,s.sname,p.pname,e.ename,skol,sprice,svagno,skorz,v.abv , (select kcolor from kategor where _id=p.kid ) as kc from spisok s, products p, Edin e, valuta v where s.pid=p._id and s.eid=e._id and s.vid=v._id and s._id ="+DB_SEL, null);
	}

	//получить имя списка из таблицы DB_TABLE
	public Cursor getNameSpisok(String DB_SEL) {
		return database.rawQuery("select _id, sname,snom,sdate from Spisok where sname = ('"+DB_SEL+"')", null);
	}

	// добавить запись в DB_TABLE
	public void addNewSpisok(String DB_TABLE, int snom,String snam) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		String datetime = sdf.format(new Date(System.currentTimeMillis()));
		ContentValues cv = new ContentValues();
		cv.put("snom", snom);
		cv.put("sname", snam);
		cv.put("sdate", datetime);
		cv.put("pid", "0");
		cv.put("skol", "0");
		cv.put("sprice", "0");
		cv.put("svagno", "0");
		cv.put("skorz", "0");
		cv.put("eid", 0);
		cv.put("vid", 4);
		cv.put("klid", 0);
		database.insert(DB_TABLE, null, cv);
	}

	// добавить запись продукта в DB_TABLE
	public void addProdSpisok(String DB_TABLE,int snom,String snam,String dat,int pid, float skol, float sprice, int v, int skr, int eid, int vid) {
		ContentValues cv = new ContentValues();
		cv.put("snom", snom);
		cv.put("sname", snam);
		cv.put("sdate", dat);
		cv.put("pid", pid);
		cv.put("skol", skol);
		cv.put("sprice", sprice);
		cv.put("svagno", v);
		cv.put("skorz", skr);
		cv.put("eid", eid);
		cv.put("vid", vid);
		cv.put("klid", 0);
		database.insert(DB_TABLE, null, cv);
	}

	// удалить список из DB_TABLE
	public void delRec(String DB_TABLE,long id) {
		String txt = "(select s.snom FROM spisok s where s._id ="+id+")";
		database.delete(DB_TABLE, "snom = " + txt, null);
		database.delete("Nastr", "sn = " + txt, null);
	}

	// удалить продукт в списке из DB_TABLE
	public void delRecPS(String DB_TABLE,long id) {
		database.delete(DB_TABLE, "_id = " + id, null);
	}

	//обновить запись из DB_TABLE
	public void UpDateNSp(String DB_TABLE,String txt,int id) {
		ContentValues cv = new ContentValues();
		cv.put("sname", txt);
		String whereArg = "(select snom from spisok where _id ="+id+")";
		database.update(DB_TABLE, cv, "snom = "+ whereArg, null);
	}

	//обновить запись продукта в списке в DB_TABLE
	public void upProdSpisok(String DB_TABLE,int pid, float skol, float sprice, int v, int eid, int _id) {
		ContentValues cv = new ContentValues();
		cv.put("pid", pid);
		cv.put("skol", skol);
		cv.put("sprice", sprice);
		cv.put("svagno", v);
		cv.put("eid", eid);
		database.update(DB_TABLE,cv, "_id = "+ _id, null);
	}

	//получить имя списка из таблицы DB_TABLE
	public Cursor getSkorSpisok(long id) {
		return database.rawQuery("select _id, skorz,skol,sprice from Spisok where _id = "+id, null);
	}

	//получить сумму в корзине из таблицы DB_TABLE
	public Cursor getSumInKor(long id) {
		return database.rawQuery("select (skol*sprice) as sm from Spisok where skorz=1 and snom=(select snom from Spisok where _id ="+id+")", null);
	}

	//обновить запись из DB_TABLE
	public void UpDateKSp(String DB_TABLE,int txt,long id) {
		ContentValues cv = new ContentValues();
		cv.put("skorz", txt);
		database.update(DB_TABLE, cv, "_id = "+ id, null);
	}

	//получить все данные из таблицы DB_TABLE
	public Cursor getProdName(String DB_TABLE,String partialValue) {
		if (partialValue != "") {
			// Log.d(TAG, "выполняем rawQuery, где partialValue= "+partialValue);
			return database.rawQuery("SELECT _ID, PNAME,eid FROM "+DB_TABLE+" WHERE PNAME LIKE '%"+partialValue+"%'", null);
		} else {
			// Log.d(TAG, "выполняем query");
			return database.rawQuery("SELECT _ID, PNAME,eid FROM "+DB_TABLE, null);
		}
	}

	public Cursor getProdNM(String pnam) {
		return database.rawQuery("select _id, pname from products where pname = '"+pnam+"'", null);
	}

	//добавить запись продукта в DB_TABLE
	public void addProd(String DB_TABLE,int kid,String pnam,int eid) {
		ContentValues cv = new ContentValues();
		cv.put("kid", kid);
		cv.put("pname", pnam);
		cv.put("eid", eid);
		database.insert(DB_TABLE, null, cv);
	}

	public Cursor getProdED(long pid) {
		return database.rawQuery("select ename from edin where _id= (SELECT eid FROM Products WHERE _id="+pid+")", null);
	}

	//получить все данные из таблицы DB_TABLE
	public Cursor getEdinName(String DB_TABLE,String partialValue) {
		if (partialValue != "") {
			return database.rawQuery("SELECT _ID, ENAME FROM "+DB_TABLE+" WHERE ENAME LIKE '%"+partialValue+"%'", null);
		} else {
			return database.rawQuery("SELECT _ID, ENAME FROM "+DB_TABLE+" order by ENAME", null);
		}
	}

	//добавить запись единицы в DB_TABLE
	public void addEdin(String DB_TABLE,String enam) {
		ContentValues cv = new ContentValues();
		cv.put("ename", enam);
		database.insert(DB_TABLE, null, cv);
	}

	public Cursor getEd(int eid) {
		return database.rawQuery("select _ID, ENAME from edin where _ID="+eid, null);
	}

	//удалить единицу в списке из DB_TABLE
	public void delRecEd(String DB_TABLE,int id) {
		database.delete(DB_TABLE, "_id = " + id, null);
	}

	public Cursor getEdinInProd(int eid) {
		return database.rawQuery("select * from products where  eid="+eid, null);
	}

	//обновить запись из DB_TABLE
	public void UpDateEdin(String DB_TABLE, String enam,int id) {
		ContentValues cv = new ContentValues();
		cv.put("ename", enam);
		database.update(DB_TABLE, cv, "_id = "+ id, null);
	}

	public Cursor getProdPrice(long pid) {
		return database.rawQuery("select _id, prices from pprice where pid = "+pid, null);
	}

	//добавить запись продукта в DB_TABLE
	public void addPriceProd(String DB_TABLE,int pid,float pp) {
		ContentValues cv = new ContentValues();
		cv.put("pid", pid);
		cv.put("prices", pp);
		database.insert(DB_TABLE, null, cv);
	}
	//обновить запись из DB_TABLE
	public void upPriceProd(String DB_TABLE, float pp,int pid) {
		ContentValues cv = new ContentValues();
		cv.put("prices", pp);
		database.update(DB_TABLE, cv, "pid = "+ pid, null);
	}

	//удалить продукт в списке из DB_TABLE
	public void delPriceProd(String DB_TABLE,int id) {
		database.delete(DB_TABLE, "_id = " + id, null);
	}

	// добавить запись в DB_TABLE
	public void addNewNastr(String DB_TABLE, int snom) {
		ContentValues cv = new ContentValues();
		cv.put("sn", snom);
		cv.put("kateg", "1");
		cv.put("kolvo", "1");
		cv.put("edizm", "1");
		cv.put("price", "1");
		cv.put("vagn", "1");
		cv.put("ekr", "0");
		cv.put("valuta", "4");
		database.insert(DB_TABLE, null, cv);
	}

	//открыть настройку для указанного списка
	public Cursor getNastr(long pid) {
		return database.rawQuery("select * from Nastr where sn=(select snom from spisok where _id = "+pid+")", null);
	}

	//открыть настройку для указанного списка
	public Cursor getNastrSN(long pid) {
		return database.rawQuery("select * from Nastr where sn="+pid, null);
	}

	//обновить запись из DB_TABLE
	public void UpDateNastr(String DB_TABLE,String fld, int txt,int id) {
		//Log.d(TAG, "sn="+id+", "+fld+"="+txt);
		String tt = "(select s.snom FROM spisok s where s._id ="+id+")";
		ContentValues cv = new ContentValues();
		cv.put(fld, txt);
		database.update(DB_TABLE, cv, "sn = "+ tt, null);
	}

	// получить все данные из таблицы DB_TABLE
	public Cursor getAllKateg(String DB_TABLE,String DB_OB) {
		return database.rawQuery("select * from "+DB_TABLE+" order by "+DB_OB, null);
	}

	// добавить запись в DB_TABLE
	public void addNewKateg(String DB_TABLE, String kname, String kcolor) {
		ContentValues cv = new ContentValues();
		cv.put("kname", kname);
		cv.put("kcolor", kcolor);
		database.insert(DB_TABLE, null, cv);
	}

	public Cursor getKateg(int kid) {
		return database.rawQuery("select _id, kname, kcolor from kategor where _id = "+kid, null);
	}

	//обновить запись из DB_TABLE
	public void UpDateKateg(String DB_TABLE,String txt1,String txt2,int id) {
		ContentValues cv = new ContentValues();
		cv.put("kname", txt1);
		cv.put("kcolor", txt2);
		database.update(DB_TABLE, cv, "_id = "+ id, null);
	}

	public Cursor getProdKateg(int kid) {
		return database.rawQuery("select * from products where kid="+kid, null);
	}

	//удалить продукт в списке из DB_TABLE
	public void delRecKateg(String DB_TABLE,int id) {
		database.delete(DB_TABLE, "_id = " + id, null);
	}

	//получить все данные из таблицы DB_TABLE
	public Cursor getAllBProd(String partialValue) {
		if (partialValue != "")
			return database.rawQuery("select p._id,k.kname,p.pname,e.ename from products p, kategor k, edin e where p.kid=k._id and p.eid=e._id and p.pname LIKE '%"+partialValue+"%' order by p.pname", null);
		else
			return database.rawQuery("select p._id,k.kname,p.pname,e.ename from products p, kategor k, edin e where p.kid=k._id and p.eid=e._id order by p.pname", null);
	}

	//получить все данные по категории из таблицы DB_TABLE
	public Cursor getKategBProd(int partialValue) {
		return database.rawQuery("select p._id,k.kname,p.pname,e.ename from products p, kategor k, edin e where p.kid=k._id and p.eid=e._id and p.kid = "+partialValue+" order by p.pname", null);
	}


	public Cursor getBProd(int pid) {
		return database.rawQuery("select _id, kid, pname, eid from products where _id = "+pid, null);
	}

	//добавить запись в DB_TABLE
	public void addNewProd(String DB_TABLE, int kd, String pnam, int ed) {
		ContentValues cv = new ContentValues();
		cv.put("kid", kd);
		cv.put("pname", pnam);
		cv.put("eid", ed);
		cv.put("pimg", "");
		database.insert(DB_TABLE, null, cv);
	}

	//обновить запись из DB_TABLE
	public void UpDateProd(String DB_TABLE, int kd, String pnam, int ed,int id) {
		ContentValues cv = new ContentValues();
		cv.put("kid", kd);
		cv.put("pname", pnam);
		cv.put("eid", ed);
		database.update(DB_TABLE, cv, "_id = "+ id, null);
	}

	//обновить категорию в справочнике продукции DB_TABLE
	public void UpDateKatInProd(String DB_TABLE, int kd, int id) {
		ContentValues cv = new ContentValues();
		cv.put("kid", kd);
		database.update(DB_TABLE, cv, "_id = "+ id, null);
	}

	//обновить единицу в справочнике продукции DB_TABLE
	public void UpDateEdinInProd(String DB_TABLE, int ed, int id) {
		ContentValues cv = new ContentValues();
		cv.put("eid", ed);
		database.update(DB_TABLE, cv, "_id = "+ id, null);
	}

	public Cursor getProdinSP(int pid) {
		return database.rawQuery("select * from spisok where  pid="+pid, null);
	}

	//удалить продукт в списке из DB_TABLE
	public void delRecProd(String DB_TABLE,int id) {
		database.delete(DB_TABLE, "_id = " + id, null);
	}

	public Cursor getAllValut() {
		return database.rawQuery("select * from valuta", null);
	}

	public Cursor getValinSpis(int id) {
		return database.rawQuery("select distinct vid from spisok where snom =(select s.snom FROM spisok s where s._id ="+ id+")", null);
	}

	//обновить запись из DB_TABLE
	public void UpDateValInSpis(String DB_TABLE,int txt,int id) {
		String tt = "(select s.snom FROM spisok s where s._id ="+id+")";
		ContentValues cv = new ContentValues();
		cv.put("vid", txt);
		database.update(DB_TABLE, cv, "snom = "+ tt, null);
	}

}