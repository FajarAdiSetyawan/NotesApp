package com.fajaradisetyawan.notesapp.db

import android.content.Context
import androidx.room.*
import com.fajaradisetyawan.notesapp.dao.NoteDao
import com.fajaradisetyawan.notesapp.model.Notes

@Database(entities = [Notes::class], version = 1, exportSchema = false)
abstract class NoteDb: RoomDatabase(){

    companion object{
        var noteDb: NoteDb? = null

        @Synchronized
        fun getAllDatabase(context: Context): NoteDb{
            if (noteDb == null){
                noteDb = Room.databaseBuilder(
                    context,
                    NoteDb::class.java,
                    "notes.db"
                ).build()
            }
            return noteDb!!
        }
    }

    abstract fun noteDao(): NoteDao

}