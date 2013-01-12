package in.dobro.frbible;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{

private final static String DB_PATH = "/mnt/sdcard/";

String dbName;
Context context;

File dbFile;

public DBHelper(Context context, String dbName, Integer version) {
    super(context, dbName, null, version);
    this.context = context;
    this.dbName = dbName;
    dbFile= new File(DB_PATH + dbName);
}

@Override
public synchronized SQLiteDatabase getWritableDatabase() {

    if(!dbFile.exists()){
        SQLiteDatabase db = super.getWritableDatabase();
        copyDataBase(db.getPath());
        db.close();
    }
    return super.getWritableDatabase();
}

@Override
public synchronized SQLiteDatabase getReadableDatabase() {
    if(!dbFile.exists()){
        SQLiteDatabase db = super.getReadableDatabase();
        copyDataBase(db.getPath());
        db.close();
    }
    return super.getReadableDatabase();
}

@Override
public void onCreate(SQLiteDatabase db) {}

@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

public void copyDataBase(String dbPath){
    try{
        InputStream assestDB = context.getAssets().open(dbName);

        OutputStream appDB = new FileOutputStream(dbPath,false);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = assestDB.read(buffer)) > 0) {
            appDB.write(buffer, 0, length);
        }

        appDB.flush();
        appDB.close();
        assestDB.close();
    }catch(IOException e){
        e.printStackTrace();
    }

}

}