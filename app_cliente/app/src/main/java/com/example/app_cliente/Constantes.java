package com.example.app_cliente;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/*
@Entity
public class User {
    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "first_name")
    public String firstName;

    @ColumnInfo(name = "last_name")
    public String lastName;
}*/

@Entity
public class Constantes {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "constante")
    public String constante;

    @ColumnInfo(name = "valor")
    public Float valor;
}
