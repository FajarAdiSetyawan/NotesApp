package com.fajaradisetyawan.notesapp.adapter

import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fajaradisetyawan.notesapp.R
import com.fajaradisetyawan.notesapp.model.Notes
import kotlinx.android.synthetic.main.item_notes.view.*

class NotesAdapter(): RecyclerView.Adapter<NotesAdapter.NotesViewHolder>() {

    var onItemClickListener:OnItemClickListener? = null
    var notes = ArrayList<Notes>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        return NotesViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_notes,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    fun setData(note: List<Notes>){
        notes = note as ArrayList<Notes>
    }

    fun setOnClickListener(listener: OnItemClickListener){
        onItemClickListener = listener
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        holder.itemView.tvTitle.text = notes[position].title
        holder.itemView.tvDateTime.text = notes[position].dateTime
        holder.itemView.tvDesc.text = notes[position].noteText

        if (notes[position].color != null){
            holder.itemView.cardView.setCardBackgroundColor(Color.parseColor(notes[position].color))
        }else{
            holder.itemView.cardView.setCardBackgroundColor(Color.parseColor(R.color.lightBlack.toString()))
        }

        if (notes[position].imgPath != null){
            holder.itemView.imgNoteItem.setImageBitmap(BitmapFactory.decodeFile(notes[position].imgPath))
            holder.itemView.imgNoteItem.visibility = View.VISIBLE
        }else{
            holder.itemView.imgNoteItem.visibility = View.GONE
        }

        if (notes[position].webLink != ""){
            holder.itemView.tvWebLink.text = notes[position].webLink
            holder.itemView.tvWebLink.visibility = View.VISIBLE
        }else{
            holder.itemView.tvWebLink.visibility = View.GONE
        }


        holder.itemView.cardView.setOnClickListener{
            onItemClickListener!!.onClicked(notes[position].id!!)
        }
    }

    class NotesViewHolder(view: View) : RecyclerView.ViewHolder(view){

    }


    interface OnItemClickListener{
        fun onClicked(noteId:Int)
    }

}