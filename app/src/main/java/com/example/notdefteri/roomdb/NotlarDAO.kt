package com.bilocan.notdefteri.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.bilocan.notdefteri.model.Not
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface NotlarDAO {
    @Query("SELECT * FROM notlar")
    fun tumNotlar(): Flowable<List<Not>>

    @Query("SELECT * FROM notlar WHERE id = :id")
    fun findById(id: Int): Flowable<Not>

    @Insert
    fun insert(not: Not): Completable

    @Delete
    fun delete(not: Not): Completable
}