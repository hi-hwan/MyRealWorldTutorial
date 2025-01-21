package com.realworld.android.petsave.report.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Base64
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
import com.realworld.android.petsave.common.data.api.ClientAuthenticator
import com.realworld.android.petsave.common.data.api.ReportManager
import com.realworld.android.petsave.common.utils.DataValidator.Companion.isValidJpegAtPath
import com.realworld.android.petsave.common.utils.Encryption
import com.realworld.android.petsave.common.utils.Encryption.Companion.encryptFile
import com.realworld.android.petsave.report.databinding.FragmentReportDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@AndroidEntryPoint
class ReportDetailFragment : Fragment() {

    @Inject
    lateinit var reportManager: ReportManager

    @Inject
    lateinit var clientAuthenticator: ClientAuthenticator

    companion object {
        private const val REPORT_APP_ID = 46341L
        private const val REPORT_PROVIDER_ID = 46341L
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
            //image from gallery
            if (uri != null) {
                // Get the full size image
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = activity?.contentResolver?.query(uri, filePathColumn,
                    null, null, null)
                cursor?.moveToFirst()
                val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
                val decodableImageString = columnIndex?.let {
                    cursor.getString(it)
                } ?: ""
                cursor?.close()
                showFileName(uri, decodableImageString)
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
            // Sanitize string (살균)
            // 문자열에서 취약한 문자를 제거
            // 예를 들어 [') OR 1=1 OR (password LIKE '* ] 처럼 SQL 언어를 사용하면 우회가 될 수 있다.
            reportString = reportString.replace("\\", "")
                .replace(";", "").replace("%", "")
                .replace("\"", "").replace("\'", "")

            val reportID = UUID.randomUUID().toString()

            context?.let { theContext ->
                val file = File(theContext.filesDir?.absolutePath, "$reportID.txt")
                val encryptedFile = encryptFile(theContext, file)
                encryptedFile.openFileOutput().bufferedWriter().use {
                    it.write(reportString)
                }
            }
            // testCustomEncryption(reportString)
            synchronized(this) {
                ReportTracker.reportNumber.incrementAndGet()
            }

            //2. Send report
            //Add Signature
            val id = REPORT_APP_ID * REPORT_PROVIDER_ID
            val stringToSign = "$id+$reportID+$reportString"
            val bytesToSign = stringToSign.toByteArray(Charsets.UTF_8)
            val signedData = clientAuthenticator.sign(bytesToSign)
            val requestSignature = Base64.encodeToString(signedData, Base64.NO_WRAP)
            val postParameters = mapOf(
                "application_id" to id,
                "report_id" to reportID,
                "report" to reportString,
                "signature" to requestSignature
            )

            if (postParameters.isNotEmpty()) {
                //send report
                reportManager.sendReport(postParameters) {
                    val reportSent: Boolean = it["success"] as Boolean
                    if (reportSent) {
                        val serverSignature = it["signature"] as String
                        val signatureBytes = Base64.decode(serverSignature, Base64.NO_WRAP)

                        val confirmationCode = it["confirmation_code"] as String
                        val confirmationBytes = confirmationCode.toByteArray(Charsets.UTF_8)

                        success = clientAuthenticator.verify(
                            signatureBytes,
                            confirmationBytes,
                            clientAuthenticator.serverPublicKeyString
                        )
                    }
                    onReportReceived(success)
                }
            }
        }
    }

    private fun onReportReceived(success: Boolean) {
        isSendingReport = false
        if (success) {
            context?.let {
                val report = synchronized(this) {
                    "Report: ${ReportTracker.reportNumber.get()}"
                }
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

    private fun showFileName(selectedImage: Uri, decodableImageString: String?) {
        // Validate image
        val isValid = isValidJpegAtPath(decodableImageString)
        if (isValid) {
            //get filename
            val fileNameColumn = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
            val nameCursor = activity?.contentResolver?.query(
                selectedImage, fileNameColumn,
                null, null, null
            )
            nameCursor?.moveToFirst()
            val nameIndex = nameCursor?.getColumnIndex(fileNameColumn[0])
            val fileName = nameIndex?.let {
                nameCursor.getString(it)
            } ?: ""
            nameCursor?.close()

            //update UI with filename
            binding.uploadStatusTextview.text = fileName
        } else {
            val toast = Toast.makeText(context, "Please choose a JPEG image", Toast.LENGTH_LONG)
            toast.show()
        }
    }
}
