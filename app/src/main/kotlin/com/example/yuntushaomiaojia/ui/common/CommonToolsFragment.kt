package com.example.yuntushaomiaojia.ui.common

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.yuntushaomiaojia.R
import com.example.yuntushaomiaojia.data.notebook.NotebookRepository
import com.example.yuntushaomiaojia.databinding.DialogGenerateQrBinding
import com.example.yuntushaomiaojia.databinding.FragmentCommonToolsBinding
import com.example.yuntushaomiaojia.ui.tool.ToolActivity
import com.example.yuntushaomiaojia.viewmodel.CommonToolsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

class CommonToolsFragment : Fragment() {

    private var _binding: FragmentCommonToolsBinding? = null
    private val binding: FragmentCommonToolsBinding
        get() = requireNotNull(_binding)

    private var qrDialogBinding: DialogGenerateQrBinding? = null
    private lateinit var viewModel: CommonToolsViewModel

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startQrScan()
            } else {
                Toast.makeText(requireContext(), R.string.scan_permission_denied, Toast.LENGTH_SHORT).show()
            }
        }

    private val scanLauncher = registerForActivityResult(ScanContract(), ::handleScanResult)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommonToolsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val factory = CommonToolsViewModel.Factory(
            NotebookRepository(requireContext().applicationContext)
        )
        viewModel = ViewModelProvider(this, factory)[CommonToolsViewModel::class.java]

        observeViewModel()
        bindClicks()
        viewModel.loadSavedNotebookContent()
    }

    private fun observeViewModel() {
        viewModel.noteText.observe(viewLifecycleOwner) { note ->
            binding.etNotebook.setText(note)
        }
        viewModel.toastEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { messageRes ->
                Toast.makeText(requireContext(), messageRes, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.savedNoteHistoryEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { savedContent ->
                showSavedNoteDialog(savedContent)
            }
        }
        viewModel.qrBitmapEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { bitmap ->
                qrDialogBinding?.ivQrResult?.setImageBitmap(bitmap)
                qrDialogBinding?.tvQrTips?.text = qrDialogBinding?.etQrContent?.text?.toString()?.trim().orEmpty()
            }
        }
        viewModel.qrInputErrorEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { errorRes ->
                qrDialogBinding?.etQrContent?.error = getString(errorRes)
            }
        }
        viewModel.openToolEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { toolId ->
                ToolActivity.open(requireContext(), toolId)
            }
        }
    }

    private fun bindClicks() {
        binding.cardGenerateQr.setOnClickListener { showGenerateQrDialog() }
        binding.cardScanQr.setOnClickListener { requestScanPermissionAndOpenScanner() }
        binding.btnSaveNote.setOnClickListener {
            viewModel.saveNote(binding.etNotebook.text.toString())
        }
        binding.btnViewNote.setOnClickListener { viewModel.showSavedNote() }
        binding.itemCompass.setOnClickListener { viewModel.openCompass() }
        binding.itemExchangeRate.setOnClickListener { viewModel.openExchangeRate() }
        binding.itemWatermark.setOnClickListener { viewModel.openWatermark() }
        binding.itemPhotoGrid.setOnClickListener { viewModel.openPhotoGrid() }
    }

    private fun requestScanPermissionAndOpenScanner() {
        if (hasCameraPermission()) {
            startQrScan()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun startQrScan() {
        val options = ScanOptions()
            .setPrompt(getString(R.string.scan_prompt))
            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            .setBeepEnabled(true)
            .setOrientationLocked(true)
            .setCaptureActivity(PortraitCaptureActivity::class.java)
        scanLauncher.launch(options)
    }

    private fun handleScanResult(result: ScanIntentResult) {
        val contents = result.contents
        if (contents.isNullOrBlank()) {
            Toast.makeText(requireContext(), R.string.scan_result_empty, Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.scan_result_title)
            .setMessage(contents)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun showGenerateQrDialog() {
        val dialogBinding = DialogGenerateQrBinding.inflate(layoutInflater)
        qrDialogBinding = dialogBinding

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.common_qr_dialog_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.common_qr_dialog_positive, null)
            .setNegativeButton(R.string.common_qr_dialog_negative) { currentDialog, _ ->
                currentDialog.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                viewModel.generateQrCode(dialogBinding.etQrContent.text.toString())
            }
        }
        dialog.setOnDismissListener {
            qrDialogBinding = null
        }
        dialog.show()
    }

    private fun showSavedNoteDialog(savedContent: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.common_notebook)
            .setMessage(savedContent)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        qrDialogBinding = null
        _binding = null
    }
}
