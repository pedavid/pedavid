package com.example.app_cliente;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Constantes.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ConstantesDao constantesDao();
}
