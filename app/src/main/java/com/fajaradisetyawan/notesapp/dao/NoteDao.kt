package com.fajaradisetyawan.notesapp.dao

import androidx.room.*
import com.fajaradisetyawan.notesapp.model.Notes

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY id DESC")
    suspend fun getAllNotes(): List<Notes>

    @Query("SELECT * FROM notes WHERE id =:id")
    suspend fun getSpesificNotes(id: Int): Notes

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: Notes)

    @Delete
    suspend fun deleteNotes(notes: Notes)

    @Query("DELETE FROM notes WHERE id =:id")
    suspend fun deleteSpesificNotes(id: Int)

    @Update
    suspend fun updateNotes(notes: Notes)
}