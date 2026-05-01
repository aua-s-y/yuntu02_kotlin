package com.example.yuntushaomiaojia.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.yuntushaomiaojia.databinding.FragmentHomeBinding
import com.example.yuntushaomiaojia.ui.tool.ToolActivity
import com.example.yuntushaomiaojia.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding
        get() = requireNotNull(_binding)

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        observeViewModel()
        bindClicks()
    }

    private fun observeViewModel() {
        viewModel.openToolEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { toolId ->
                ToolActivity.open(requireContext(), toolId)
            }
        }
    }

    private fun bindClicks() {
        binding.cardScanArchive.setOnClickListener { viewModel.openScanArchive() }
        binding.cardTextRecognition.setOnClickListener { viewModel.openTextRecognition() }
        binding.cardPlantRecognition.setOnClickListener { viewModel.openPlantRecognition() }
        binding.cardFruitRecognition.setOnClickListener { viewModel.openFruitRecognition() }
        binding.cardAnimalRecognition.setOnClickListener { viewModel.openAnimalRecognition() }
        binding.itemPdfToImage.setOnClickListener { viewModel.openPdfToImage() }
        binding.itemImageToPdf.setOnClickListener { viewModel.openImageToPdf() }
        binding.itemEncryptPdf.setOnClickListener { viewModel.openEncryptPdf() }
        binding.itemCompressPdf.setOnClickListener { viewModel.openCompressPdf() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
