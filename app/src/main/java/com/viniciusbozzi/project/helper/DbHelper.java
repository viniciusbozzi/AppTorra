package com.viniciusbozzi.project.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 DESENVOLVIDO POR VINICIUS BOZZI*/

public class DbHelper extends SQLiteOpenHelper {

    public static int VERSION = 1;
    public static String NOME_DB = "DB_GRAFICOS";
    public static String TABELA_GRAFICOS = "graficos";

    public DbHelper(Context context) {
        super(context, NOME_DB, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE IF NOT EXISTS " + TABELA_GRAFICOS
                + " (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " nome TEXT NOT NULL, tempo TEXT NOT NULL, temperatura TEXT NOT NULL, uniqueArrays TEXT NOT NULL," +
                " idtorra TEXT NOT NULL, global INTEGER NOT NULL); ";

        try {
            db.execSQL( sql );
            Log.i("INFO DB", "Sucesso ao criar a tabela" );
        }catch (Exception e){
            Log.i("INFO DB", "Erro ao criar a tabela" + e.getMessage() );
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        String sql = "DROP TABLE IF EXISTS " + TABELA_GRAFICOS + " ;" ;

        try {
            db.execSQL( sql );
            onCreate(db);
            Log.i("INFO DB", "Sucesso ao atualizar App" );
        }catch (Exception e){
            Log.i("INFO DB", "Erro ao atualizar App" + e.getMessage() );
        }

    }

}
