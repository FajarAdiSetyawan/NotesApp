package com.fajaradisetyawan.notesapp.view.fragment

import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.fajaradisetyawan.notesapp.R
import com.fajaradisetyawan.notesapp.db.NoteDb
import com.fajaradisetyawan.notesapp.model.Notes
import com.fajaradisetyawan.notesapp.util.BaseFragment
import dev.shreyaspatil.MaterialDialog.MaterialDialog
import ir.gapgame.cutoast.CuToast
import kotlinx.android.synthetic.main.fragment_create_note.*
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*

class CreateNoteFragment : BaseFragment(), EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

    var selectedColor = "#171C26"
    var currentDate: String? = null
    private var READ_STORAGE_PERM = 123
    private var REQUEST_CODE_IMAGE = 456
    private var selectedImagePath = ""
    private var webLink = ""
    private var noteId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteId = requireArguments().getInt("noteId", -1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_note, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            CreateNoteFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (noteId != -1){
            launch {
                context?.let {
                    var notes = NoteDb.getAllDatabase(it).noteDao().getSpesificNotes(noteId)
                    colorView.setBackgroundColor(Color.parseColor(notes.color))
                    et_title_note.setText(notes.title)
                    et_sub_title_note.setText(notes.subTitle)
                    et_note_desc.setText(notes.noteText)
                    if (notes.imgPath != ""){
                        selectedImagePath = notes.imgPath!!
                        imgNote.setImageBitmap(BitmapFactory.decodeFile(notes.imgPath))
                        layoutImage.visibility = View.VISIBLE
                        imgNote.visibility = View.VISIBLE
                        imgDelete.visibility = View.VISIBLE
                    }else{
                        layoutImage.visibility = View.GONE
                        imgNote.visibility = View.GONE
                        imgDelete.visibility = View.GONE
                    }

                    if (notes.webLink != ""){
                        webLink = notes.webLink!!
                        tvWebLink.text = notes.webLink
                        etWebLink.setText(notes.webLink)
                        layoutWebUrl.visibility = View.VISIBLE
                        imgUrlDelete.visibility = View.VISIBLE
                    }else{
                        imgUrlDelete.visibility = View.GONE
                        layoutWebUrl.visibility = View.GONE
                    }
                }
            }
        }

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
          BroadcastReceiver, IntentFilter("bottom_sheet_action")
        )

        val sdf = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss")
        currentDate = sdf.format(Date())
        tv_datetime.text = currentDate

        colorView.setBackgroundColor(Color.parseColor(selectedColor))

        btn_done.setOnClickListener {
            if (noteId != -1){
                updateNote()

            }else{
                saveNote()

            }
        }

        btn_back.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }


        btnMore.setOnClickListener {
            var noteBottomFragment = NoteBottomSheetFragment.newInstance(noteId)
            noteBottomFragment.show(requireActivity().supportFragmentManager, "Note Bottom Sheet Fragment")
        }

        imgDelete.setOnClickListener {

            val mDialog = MaterialDialog.Builder(requireActivity())
                .setTitle(getString(R.string.delete))
                .setMessage(getString(R.string.deleteimg))
                .setCancelable(false)
                .setAnimation(R.raw.delete)
                .setPositiveButton(
                    requireActivity().getString(R.string.deleteBtn),
                    R.drawable.ic_baseline_delete_outline_24
                ) { dialogInterface, which ->
                    // Delete Operation
                    selectedImagePath = ""
                    layoutImage.visibility = View.GONE

                    CuToast.messageSize = 16
                    CuToast.sound = true
                    CuToast.darkMode = true
                    CuToast.showCustom(
                        activity,
                        requireActivity().getString(R.string.deleteBtn),
                        requireActivity().getString(R.string.deleteNote),
                        Toast.LENGTH_SHORT,
                        resources.getColor(R.color.greenPrimary),
                        R.drawable.ic_check
                    )
                    // close dialog
                    dialogInterface.dismiss()

                }
                .setNegativeButton(
                    requireActivity().getString(R.string.cancel),
                    R.drawable.ic_baseline_close_24
                ) { dialogInterface, which -> dialogInterface.dismiss() }
                .build()

            // Show Dialog
            // Show Dialog
            mDialog.show()
        }

        btnOk.setOnClickListener {
            if (etWebLink.text.toString().trim().isNotEmpty()){
                checkWebUrl()
            }else{
//                MotionToast.createToast(requireActivity(),
//                    requireActivity().getString(R.string.urlblank),
//                    requireActivity().getString(R.string.urlempty),
//                    MotionToast.TOAST_WARNING,
//                    MotionToast.GRAVITY_BOTTOM,
//                    MotionToast.LONG_DURATION,
//                    ResourcesCompat.getFont(requireContext(),R.font.lato))
                CuToast.messageSize = 16
                CuToast.sound = true
                CuToast.darkMode = true
                CuToast.showCustom(
                    activity,
                    requireActivity().getString(R.string.urlblank),
                    requireActivity().getString(R.string.urlempty), Toast.LENGTH_SHORT
                    , resources.getColor(R.color.yellowPrimary)
                    , R.drawable.ic_baseline_warning_24
                )
            }
        }

        btnCancel.setOnClickListener {
            if (noteId != -1){
                tvWebLink.visibility = View.VISIBLE
                layoutWebUrl.visibility = View.GONE
            }else{
                layoutWebUrl.visibility = View.GONE
            }
        }

        imgUrlDelete.setOnClickListener {
            webLink = ""
            tvWebLink.visibility = View.GONE
            imgUrlDelete.visibility = View.GONE
            layoutWebUrl.visibility = View.GONE
        }

        tvWebLink.setOnClickListener{
            var intent = Intent(Intent.ACTION_VIEW, Uri.parse(etWebLink.text.toString()))
            startActivity(intent)
        }
    }

    private fun updateNote(){
        launch {
            context?.let {
                var notes = NoteDb.getAllDatabase(it).noteDao().getSpesificNotes(noteId)
                notes.title = et_title_note.text.toString()
                notes.subTitle = et_sub_title_note.text.toString()
                notes.noteText = et_note_desc.text.toString()
                notes.dateTime = currentDate
                notes.color = selectedColor
                notes.imgPath = selectedImagePath
                notes.webLink = webLink

                NoteDb.getAllDatabase(it).noteDao().updateNotes(notes)
                et_title_note.setText("")
                et_sub_title_note.setText("")
                et_note_desc.setText("")
                layoutImage.visibility = View.GONE
                imgNote.visibility = View.GONE
                tvWebLink.visibility = View.GONE
                requireActivity().supportFragmentManager.popBackStack()

                CuToast.messageSize = 16
                CuToast.sound = true
                CuToast.darkMode = true
                CuToast.showCustom(
                    activity,
                    requireActivity().getString(R.string.update),
                    requireActivity().getString(R.string.update_add_note),
                    Toast.LENGTH_SHORT,
                    resources.getColor(R.color.success),
                    R.drawable.ic_check
                )
            }
        }
    }

    private fun saveNote(){
        if (et_title_note.text.isNullOrEmpty()){
            CuToast.messageSize = 16
            CuToast.sound = true
            CuToast.darkMode = true
            CuToast.showCustom(
                activity,
                requireActivity().getString(R.string.titleblank),
                requireActivity().getString(R.string.titleempty),
                Toast.LENGTH_SHORT,
                resources.getColor(R.color.yellowPrimary),
                R.drawable.ic_baseline_warning_24
            )
        }else if (et_sub_title_note.text.isNullOrEmpty()){
            CuToast.messageSize = 16
            CuToast.sound = true
            CuToast.darkMode = true
            CuToast.showCustom(
                activity,
                requireActivity().getString(R.string.subtitleblank),
                requireActivity().getString(R.string.subtitleempty),
                Toast.LENGTH_SHORT,
                resources.getColor(R.color.yellowPrimary),
                R.drawable.ic_baseline_warning_24
            )
        }else if (et_note_desc.text.isNullOrEmpty()){
            CuToast.messageSize = 16
            CuToast.sound = true
            CuToast.darkMode = true
            CuToast.showCustom(
                activity,
                requireActivity().getString(R.string.descblank),
                requireActivity().getString(R.string.descempty),
                Toast.LENGTH_SHORT,
                resources.getColor(R.color.yellowPrimary),
                R.drawable.ic_baseline_warning_24
            )
        }else{
            launch {
                var notes = Notes()
                notes.title = et_title_note.text.toString()
                notes.subTitle = et_sub_title_note.text.toString()
                notes.noteText = et_note_desc.text.toString()
                notes.dateTime = currentDate
                notes.color = selectedColor
                notes.imgPath = selectedImagePath
                notes.webLink = webLink
                context?.let {
                    NoteDb.getAllDatabase(it).noteDao().insertNotes(notes)
                    et_title_note.setText("")
                    et_sub_title_note.setText("")
                    et_note_desc.setText("")
                    layoutImage.visibility = View.GONE
                    imgNote.visibility = View.GONE
                    tvWebLink.visibility = View.GONE
                    requireActivity().supportFragmentManager.popBackStack()
                }
                CuToast.messageSize = 16
                CuToast.sound = true
                CuToast.darkMode = true
                CuToast.showCustom(
                    activity,
                    requireActivity().getString(R.string.success),
                    requireActivity().getString(R.string.success_add_note),
                    Toast.LENGTH_SHORT,
                    resources.getColor(R.color.success),
                    R.drawable.ic_check
                )
            }
        }
    }

    private fun deleteNote(){
        launch {
            context?.let {
                NoteDb.getAllDatabase(it).noteDao().deleteSpesificNotes(noteId)
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    private fun checkWebUrl(){
        if (Patterns.WEB_URL.matcher(etWebLink.text.toString()).matches()){
            layoutWebUrl.visibility = View.GONE
            etWebLink.isEnabled = false
            webLink = etWebLink.text.toString()
            tvWebLink.visibility = View.VISIBLE
            tvWebLink.text = etWebLink.text.toString()
        }else{
            CuToast.messageSize = 16
            CuToast.sound = true
            CuToast.darkMode = true
            CuToast.showCustom(
                activity,
                requireActivity().getString(R.string.urlnotvalid),
                requireActivity().getString(R.string.urlnotvalid),
                Toast.LENGTH_SHORT,
                resources.getColor(R.color.redPrimary),
                R.drawable.ic_baseline_error_outline_24
            )
        }
    }

    private val BroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            var actionColor = intent!!.getStringExtra("action")
            when(actionColor!!){
                "Blue" -> {
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "Yellow" -> {
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "Pink" -> {
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "Green" -> {
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "Orange" -> {
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "Black" -> {
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "Image" -> {
                    readStorageTask()
                    layoutWebUrl.visibility = View.GONE
                }
                "WebUrl" -> {
                    layoutWebUrl.visibility = View.VISIBLE
                }
                "DeleteNote" -> {
                    deleteNote()
                }
                else -> {
                    layoutImage.visibility = View.GONE
                    imgNote.visibility = View.GONE
                    layoutWebUrl.visibility = View.GONE
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }
            }
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(BroadcastReceiver)
        super.onDestroy()
    }

    private fun hasReadStoragePermission(): Boolean{
        return EasyPermissions.hasPermissions(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun readStorageTask(){
        if (hasReadStoragePermission()){
            pickImageFromGallery()
        }else{
            EasyPermissions.requestPermissions(
                requireActivity(),
                getString(R.string.storage_permission_text),
                READ_STORAGE_PERM,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun pickImageFromGallery(){
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(requireActivity().packageManager) != null){
            startActivityForResult(intent, REQUEST_CODE_IMAGE)
        }
    }

    private fun getPathFromUri(contentUri: Uri): String?{
        var filePath: String? = null
        var cursor = requireActivity().contentResolver.query(contentUri, null, null, null, null)
        if (cursor == null){
            filePath = contentUri.path
        }else{
            cursor.moveToFirst()
            var index = cursor.getColumnIndex("_data")
            filePath = cursor.getString(index)
            cursor.close()
        }
        return filePath
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK){
            if (data != null){
                var selectedImgUri = data.data
                if (selectedImgUri != null){
                    try {
                        var inputStream = requireActivity().contentResolver.openInputStream(selectedImgUri)
                        var bitmap = BitmapFactory.decodeStream(inputStream)
                        imgNote.setImageBitmap(bitmap)
                        imgNote.visibility = View.VISIBLE
                        layoutImage.visibility = View.VISIBLE

                        selectedImagePath = getPathFromUri(selectedImgUri)!!
                    }catch (e: Exception){
                        Toast.makeText(requireContext(),e.message,Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, requireActivity())
    }


    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(requireActivity(), perms)){
            AppSettingsDialog.Builder(requireActivity()).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onRationaleDenied(requestCode: Int) {
    }

    override fun onRationaleAccepted(requestCode: Int) {
    }


}