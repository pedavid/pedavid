package com.example.app_cliente;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/*
@Dao
public interface UserDao {
    @Query("SELECT * FROM user")
    List<User> getAll();

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    List<User> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
           "last_name LIKE :last LIMIT 1")
    User findByName(String first, String last);

    @Insert
    void insertAll(User... users);

    @Delete
    void delete(User user);
}
*/

@Dao
public interface ConstantesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertValues(Constantes constantes);

    @Query("SELECT * FROM Constantes")
    List<Constantes> getAll();

    @Query("SELECT EXISTS (SELECT 1 FROM Constantes)")
    boolean isNotEmpty();
}