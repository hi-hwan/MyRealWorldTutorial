package com.realworld.android.petsave.report.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.realworld.android.petsave.common.data.api.ReportManager
import com.realworld.android.petsave.common.utils.Encryption
import com.realworld.android.petsave.common.utils.Encryption.Companion.encryptFile
import com.realworld.android.petsave.report.databinding.FragmentReportDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.io.RandomAccessFile
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@AndroidEntryPoint
class ReportDetailFragment : Fragment() {

    @Inject
    lateinit var reportManager: ReportManager

    companion object {
        private const val PIC_FROM_GALLERY = 2
        private const val REPORT_APP_ID = 46341
        private const val REPORT_SESSION_KEY = "session_key_test"
    }

    object ReportTracker {
        var reportNumber = AtomicInteger()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                selectImageFromGallery()
            }
        }

    private val selectImageFromGalleryResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                getFileName(uri)
            }
        }

    @Volatile
    private var isSendingReport = false

    private val binding get() = _binding!!
    private var _binding: FragmentReportDetailBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportDetailBinding.inflate(inflater, container, false)

        binding.sendButton.setOnClickListener {
            sendReportPressed()
        }

        binding.uploadPhotoButton.setOnClickListener {
            uploadPhotoPressed()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }

    override fun onPause() {
        context?.cacheDir?.deleteRecursively()
        context?.externalCacheDir?.deleteRecursively()
        super.onPause()
    }

    private fun setupUI() {
        binding.detailsEdtxtview.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.detailsEdtxtview.setRawInputType(InputType.TYPE_CLASS_TEXT)
    }

    private fun sendReportPressed() {
        if (!isSendingReport) {
            isSendingReport = true
            var success = true

            //1. Save report
            var reportString = binding.categoryEdtxtview.text.toString()
            reportString += " : "
            reportString += binding.detailsEdtxtview.text.toString()
            val reportID = UUID.randomUUID().toString()

            context?.let { theContext ->
                val file = File(theContext.filesDir?.absolutePath, "$reportID.txt")
                val encryptedFile = encryptFile(theContext, file)
                encryptedFile.openFileOutput().bufferedWriter().use {
                    it.write(reportString)
                }
            }
            // testCustomEncryption(reportString)
            ReportTracker.reportNumber.incrementAndGet()

            //2. Send report
            val mainActivity = activity
            var requestSignature = ""
            val postParameters = mapOf(
                "application_id" to REPORT_APP_ID,
                "report_id" to reportID,
                "report" to reportString
            )
            if (postParameters.isNotEmpty()) {
                //send report
                reportManager.sendReport(postParameters) {
                    val reportSent: Boolean = it["success"] as Boolean
                    if (reportSent) {
                        //TODO: Verify signature here
                        success = true
                    } //end if (reportSent) {
                    onReportReceived(success)
                } //mainActivity.reportManager.sendReport(postParameters) {
            } //end if (postParameters.isNotEmpty()) {
        }
    }

    private fun onReportReceived(success: Boolean) {
        isSendingReport = false
        if (success) {
            context?.let {
                val report = "Report: ${ReportTracker.reportNumber.get()}"
                val toast = Toast.makeText(
                    it, "Thank you for your report.$report", Toast
                        .LENGTH_LONG
                )
                toast.show()
            }
        } else {
            val toast = Toast.makeText(
                context,
                "There was a problem sending the report.",
                Toast.LENGTH_LONG
            )
            toast.setGravity(Gravity.TOP, 0, 0)
            toast.show()

            val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as
                    InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
        }
    }

    private fun testCustomEncryption(reportString: String) {
        val password = REPORT_SESSION_KEY.toCharArray()
        val bytes = reportString.toByteArray(Charsets.UTF_8)
        val map = Encryption.encrypt(bytes, password)
        val reportID = UUID.randomUUID().toString()
        val outFile = File(activity?.filesDir?.absolutePath, "$reportID.txt")
        ObjectOutputStream(FileOutputStream(outFile)).use {
            it.writeObject(map)
        }

        val decryptedBytes = Encryption.decrypt(map, password)
        decryptedBytes?.let {
            val decryptedString = String(it, Charsets.UTF_8)
            Log.e("Encryption Test", "The decrypted string is: $decryptedString")
        }
    }

    private fun uploadPhotoPressed() {
        context?.let {
            if (ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                selectImageFromGallery()
            }
        }
    }

    private fun selectImageFromGallery() = selectImageFromGalleryResult.launch("image/*")

    private fun getFileName(selectedImage: Uri) {
        // Validate image
        val isValid = isValidJPEGAtPath(selectedImage)
        if (isValid) {
            //get filename
            val fileNameColumn = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
            val nameCursor = activity?.contentResolver?.query(
                selectedImage, fileNameColumn,
                null, null, null
            )
            nameCursor?.moveToFirst()
            val nameIndex = nameCursor?.getColumnIndex(fileNameColumn[0])
            var filename = ""
            nameIndex?.let {
                filename = nameCursor.getString(it)
            }
            nameCursor?.close()

            //update UI with filename
            binding.uploadStatusTextview.text = filename
        } else {
            val toast = Toast.makeText(context, "Please choose a JPEG image", Toast.LENGTH_LONG)
            toast.show()
        }
    }

    private fun isValidJPEGAtPath(selectedImage: Uri): Boolean {
        var success = false
        val file = File(context?.cacheDir, "temp.jpg")
        val inputStream = activity?.contentResolver?.openInputStream(selectedImage)
        val outputStream = activity?.contentResolver?.openOutputStream(Uri.fromFile(file))
        outputStream?.let {
            inputStream?.copyTo(it)

            val randomAccessFile = RandomAccessFile(file, "r")
            val length = randomAccessFile.length()
            val lengthError = (length < 10L)
            val start = ByteArray(2)
            randomAccessFile.readFully(start)
            randomAccessFile.seek(length - 2)
            val end = ByteArray(2)
            randomAccessFile.readFully(end)
            success = !lengthError && start[0].toInt() == -1 && start[1].toInt() == -40 &&
                    end[0].toInt() == -1 && end[1].toInt() == -39

            randomAccessFile.close()
            outputStream.close()
        }
        inputStream?.close()
        file.delete()

        return success
    }
}
