package com.bilocan.notdefteri.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notlar")
data class Not(
    @ColumnInfo(name = "baslik")
    var baslik: String,
    
    @ColumnInfo(name = "icerik")
    var icerik: String,
    
    @ColumnInfo(name = "gorsel")
    var gorsel: ByteArray
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
} 