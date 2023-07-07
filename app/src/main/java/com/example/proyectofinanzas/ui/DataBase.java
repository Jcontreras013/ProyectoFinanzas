package com.example.proyectofinanzas.ui;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBase extends SQLiteOpenHelper {
    private static final String NOMBRE_DB = "Datos_Financieros";
    private static final int VERSION_DB = 1;
    private static final String TABLA_INGRESOS =
            "CREATE TABLE Ingresos (id INTEGER PRIMARY KEY AUTOINCREMENT, MONTO INTEGER, descripcion TEXT, fecha DATE)";



    public DataBase(Context context) {
        super(context, NOMBRE_DB, null, VERSION_DB);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLA_INGRESOS);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Ingresos");
        db.execSQL(TABLA_INGRESOS);
    }
}
