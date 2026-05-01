package com.example.yuntushaomiaojia.ui.quick

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.yuntushaomiaojia.R
import com.example.yuntushaomiaojia.databinding.FragmentQuickToolsBinding
import com.example.yuntushaomiaojia.ui.tool.ToolActivity
import com.example.yuntushaomiaojia.viewmodel.QuickToolsViewModel

class QuickToolsFragment : Fragment() {

    private var _binding: FragmentQuickToolsBinding? = null
    private val binding: FragmentQuickToolsBinding
        get() = requireNotNull(_binding)

    private lateinit var viewModel: QuickToolsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuickToolsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[QuickToolsViewModel::class.java]

        setupLanguageSelector()
        observeViewModel()
        bindClicks()
    }

    private fun setupLanguageSelector() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.language_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerSourceLanguage.adapter = adapter
        binding.spinnerTargetLanguage.adapter = adapter
        binding.spinnerTargetLanguage.setSelection(DEFAULT_TARGET_LANGUAGE_POSITION)
    }

    private fun observeViewModel() {
        viewModel.translateState.observe(viewLifecycleOwner) { state ->
            renderTranslateState(state)
        }
        viewModel.toastEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { messageRes ->
                Toast.makeText(requireContext(), messageRes, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.swapLanguageEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { positions ->
                binding.spinnerSourceLanguage.setSelection(positions.first)
                binding.spinnerTargetLanguage.setSelection(positions.second)
            }
        }
        viewModel.openToolEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { toolId ->
                ToolActivity.open(requireContext(), toolId)
            }
        }
    }

    private fun renderTranslateState(state: QuickToolsViewModel.TranslateState) {
        when {
            state.messageRes != null -> binding.tvTranslateResult.setText(state.messageRes)
            state.errorDetail != null -> {
                binding.tvTranslateResult.text = getString(
                    R.string.translate_failed_with_reason,
                    state.errorDetail
                )
            }
            else -> binding.tvTranslateResult.text = state.text.orEmpty()
        }
    }

    private fun bindClicks() {
        binding.btnTranslate.setOnClickListener {
            viewModel.translate(
                input = binding.etTranslateInput.text.toString(),
                sourcePosition = binding.spinnerSourceLanguage.selectedItemPosition,
                targetPosition = binding.spinnerTargetLanguage.selectedItemPosition
            )
        }
        binding.btnSwapLanguage.setOnClickListener {
            viewModel.swapLanguage(
                sourcePosition = binding.spinnerSourceLanguage.selectedItemPosition,
                targetPosition = binding.spinnerTargetLanguage.selectedItemPosition
            )
        }
        binding.itemPixelArt.setOnClickListener { viewModel.openPixelArt() }
        binding.itemColorize.setOnClickListener { viewModel.openColorize() }
        binding.itemTravelList.setOnClickListener { viewModel.openTravelList() }
        binding.itemFontZoom.setOnClickListener { viewModel.openFontZoom() }
        binding.itemQuickCompass.setOnClickListener { viewModel.openCompass() }
        binding.itemBookkeeping.setOnClickListener { viewModel.openBookkeeping() }
        binding.itemQuickExchange.setOnClickListener { viewModel.openExchangeRate() }
        binding.itemBaseConverter.setOnClickListener { viewModel.openBaseConverter() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val DEFAULT_TARGET_LANGUAGE_POSITION = 1
    }
}
