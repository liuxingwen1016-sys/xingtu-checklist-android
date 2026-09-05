package com.xinghan.xingtu.feature.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.xinghan.xingtu.R
import com.xinghan.xingtu.core.navigation.appContainer
import com.xinghan.xingtu.databinding.FragmentSettingsBinding
import com.xinghan.xingtu.domain.model.ThemeMode
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels { SettingsViewModel.provideFactory(appContainer) }
    private var rendering = false
    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> viewModel.setNotificationEnabled(granted) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.darkModeSwitch.setOnCheckedChangeListener { _, checked ->
            if (!rendering) viewModel.setThemeMode(if (checked) ThemeMode.DARK else ThemeMode.LIGHT)
        }
        binding.hapticSwitch.setOnCheckedChangeListener { _, checked ->
            if (!rendering) viewModel.setHapticEnabled(checked)
        }
        binding.notificationSwitch.setOnCheckedChangeListener { _, checked ->
            if (rendering) return@setOnCheckedChangeListener
            if (checked && Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.setNotificationEnabled(checked)
            }
        }
        binding.nativeLabRow.setOnClickListener { findNavController().navigate(R.id.nativeLabFragment) }
        binding.resetDemoRow.setOnClickListener { confirmReset() }
        binding.versionText.text = getString(R.string.settings_version, appVersion())
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.effects.collect(::handleEffect) }
            }
        }
    }

    private fun render(state: SettingsUiState) {
        rendering = true
        binding.darkModeSwitch.isChecked = state.themeMode == ThemeMode.DARK
        binding.hapticSwitch.isChecked = state.hapticEnabled
        binding.notificationSwitch.isChecked = state.notificationEnabled
        binding.resetProgress.isVisible = state.resetting
        binding.resetDemoRow.isEnabled = !state.resetting
        rendering = false
    }

    private fun handleEffect(effect: SettingsEffect) {
        when (effect) {
            is SettingsEffect.ShowMessage -> Snackbar.make(binding.root, effect.message, Snackbar.LENGTH_SHORT).show()
            is SettingsEffect.ThemeChanged -> AppCompatDelegate.setDefaultNightMode(effect.mode.nightMode())
        }
    }

    private fun confirmReset() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.settings_reset_confirm_title)
            .setMessage(R.string.settings_reset_confirm_message)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_confirm) { _, _ -> viewModel.resetDemoData() }
            .show()
    }

    private fun appVersion(): String = runCatching {
        requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName
    }.getOrNull().orEmpty().ifBlank { "1.0.0" }

    private fun ThemeMode.nightMode(): Int = when (this) {
        ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
