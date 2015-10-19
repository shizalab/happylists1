package com.happy.happylists;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ExternalDbOpenHelper extends SQLiteOpenHelper {

	static final String TAG = "myLogs";

	//Путь к папке с базами на устройстве
	public static String DB_PATH;
	//Имя файла с базой
	public static String DB_NAME= "Pokypka";
	public SQLiteDatabase database;
	public final Context context;

	public SQLiteDatabase getDb() {
		return database;
	}

	public ExternalDbOpenHelper(Context context, String databaseName) {
		super(context, databaseName, null, 1);
		this.context = context;
		//Log.d(TAG, "указываем путь к базе");
		//Составим полный путь к базам для вашего приложения
		String packageName = context.getPackageName();
		DB_PATH = String.format("//data//data//%s//databases//", packageName);
		DB_NAME = databaseName;
		openDataBase();
	}

	//Создаст базу, если она не создана
	public void createDataBase() {
		boolean dbExist = checkDataBase();
		if (!dbExist) {
			this.getReadableDatabase();
			try {
				//Log.d(TAG, "Создаст базу, если она не создана");
				copyDataBase();
			} catch (IOException e) {
				Log.e(this.getClass().toString(), "Copying error");
				throw new Error("Error copying database!");
			}
		} else {
			//Log.d(TAG, "база уже создана");
			Log.i(this.getClass().toString(), "Database already exists");
		}
	}
	//Проверка существования базы данных
	private boolean checkDataBase() {
		SQLiteDatabase checkDb = null;
		try {
			String path = DB_PATH + DB_NAME;
			checkDb = SQLiteDatabase.openDatabase(path, null,
					SQLiteDatabase.OPEN_READONLY);
			//Log.d(TAG, "Проверка существования базы данных");
		} catch (SQLException e) {
			Log.e(this.getClass().toString(), "Error while checking db");
		}
		//Андроид не любит утечки ресурсов, все должно закрываться
		if (checkDb != null) {
			checkDb.close();
		}
		return checkDb != null;
	}
	//Метод копирования базы
	private void copyDataBase() throws IOException {
		// Открываем поток для чтения из уже созданной нами БД
		//источник в assets
		//Log.d(TAG, "копируем базу");
		InputStream externalDbStream = context.getAssets().open(DB_NAME);

		// Путь к уже созданной пустой базе в андроиде
		String outFileName = DB_PATH + DB_NAME;

		// Теперь создадим поток для записи в эту БД побайтно
		OutputStream localDbStream = new FileOutputStream(outFileName);

		// Собственно, копирование
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = externalDbStream.read(buffer)) > 0) {
			localDbStream.write(buffer, 0, bytesRead);
		}
		//  закроем потоки
		localDbStream.close();
		externalDbStream.close();

	}

	public SQLiteDatabase openDataBase() throws SQLException {
		String path = DB_PATH + DB_NAME;
		if (database == null) {
			createDataBase();
			database = SQLiteDatabase.openDatabase(path, null,
					SQLiteDatabase.OPEN_READWRITE);
			//	Log.d(TAG, "открываем БД для записи");
		}
		return database;
	}

	@Override
	public synchronized void close() {
		if (database != null) {
			database.close();
			//Log.d(TAG, "закрываем БД");
		}
		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}