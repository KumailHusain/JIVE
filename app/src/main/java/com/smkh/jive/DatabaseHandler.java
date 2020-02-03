package com.smkh.jive;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Kumail on 29-Jan-18.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    public DatabaseHandler(Context context){
        super(context, "jiveDB.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE PDFTABLE (TYPE TEXT, PREVIEWTEXT TEXT, GRAPHICNAME TEXT, PDFHIGHNAME TEXT, PDFLOWNAME TEXT, PDFHIGHSIZE INTEGER, PDFLOWSIZE INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS PDFTABLE");
    }

    public void addPDF(String Type, String PreviewText, String GraphicName, String PDFHighName, String PDFLowName, Integer PDFHighSize, Integer PDFLowSize) {
        ContentValues values = new ContentValues();
        values.put("TYPE", Type);
        values.put("PREVIEWTEXT", PreviewText);
        values.put("GRAPHICNAME", GraphicName);
        values.put("PDFHIGHNAME", PDFHighName);
        values.put("PDFLOWNAME", PDFLowName);
        values.put("PDFHIGHSIZE", PDFHighSize);
        values.put("PDFLOWSIZE", PDFLowSize);
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert("PDFTABLE", null, values);
        db.close();
    }

    public Cursor getPDFs() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM PDFTABLE ORDER BY TYPE ASC", null);
    }
}