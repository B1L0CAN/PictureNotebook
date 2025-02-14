package com.example.notdefteri.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.notdefteri.model.Not

@Database(entities = [Not::class], version = 1)
abstract class NotlarDatabase : RoomDatabase() {
    abstract fun notlarDao(): NotlarDAO
}