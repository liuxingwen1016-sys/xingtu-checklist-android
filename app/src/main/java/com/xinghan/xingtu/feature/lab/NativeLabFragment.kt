package com.xinghan.xingtu.feature.lab

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.xinghan.xingtu.R
import com.xinghan.xingtu.core.navigation.appContainer
import com.xinghan.xingtu.databinding.FragmentNativeLabBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NativeLabFragment : Fragment() {
    private var _binding: FragmentNativeLabBinding? = null
    private val binding get() = _binding!!
    private var sendAfterPermission = false
    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted && sendAfterPermission) sendNotification()
        sendAfterPermission = false
        renderDeviceInfo()
        if (!granted) showMessage(R.string.lab_notification_denied_message)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentNativeLabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        binding.notificationAction.setOnClickListener { requestOrSendNotification() }
        binding.vibrationAction.setOnClickListener {
            appContainer.hapticFeedback.confirmation()
            binding.vibrationState.setText(R.string.lab_state_success)
            showMessage(R.string.lab_vibration_done)
        }
        binding.shareAction.setOnClickListener { shareNextTrip() }
        binding.widgetAction.setOnClickListener { showWidgetHelp() }
        binding.rerunButton.setOnClickListener { renderDeviceInfo() }
        binding.root.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> renderWindowSize() }
        renderDeviceInfo()
    }

    private fun renderDeviceInfo() {
        binding.osValue.text = "Android ${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}"
        binding.modelValue.text = "${Build.MANUFACTURER} ${Build.MODEL}"
        val dark = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        binding.themeValue.text = if (dark) getString(R.string.settings_theme_dark) else getString(R.string.settings_theme_light)
        binding.notificationValue.setText(
            if (appContainer.notificationService.hasPermission()) R.string.lab_notification_granted
            else R.string.lab_notification_denied,
        )
        binding.notificationState.setText(
            if (appContainer.notificationService.hasPermission()) R.string.lab_state_ready
            else R.string.lab_state_denied,
        )
        renderWindowSize()
    }

    private fun renderWindowSize() {
        if (_binding == null || binding.root.width == 0) return
        val density = resources.displayMetrics.density
        binding.windowValue.text = "${(binding.root.width / density).toInt()} × ${(binding.root.height / density).toInt()} dp"
    }

    private fun requestOrSendNotification() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            sendAfterPermission = true
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            sendNotification()
        }
    }

    private fun sendNotification() {
        val sent = appContainer.notificationService.sendTestNotification()
        binding.notificationState.setText(if (sent) R.string.lab_state_success else R.string.lab_state_error)
        showMessage(if (sent) R.string.lab_notification_sent else R.string.lab_notification_denied_message)
    }

    private fun shareNextTrip() {
        viewLifecycleOwner.lifecycleScope.launch {
            val card = appContainer.tripRepository.observeDashboard().first().nextTrip
            if (card == null) {
                showMessage(R.string.lab_share_no_trip)
            } else {
                appContainer.shareLauncher.shareText(appContainer.buildShareSummary(card))
                binding.shareState.setText(R.string.lab_state_success)
            }
        }
    }

    private fun showWidgetHelp() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.lab_widget_desc_title)
            .setMessage(R.string.lab_widget_desc_content)
            .setPositiveButton(R.string.lab_widget_desc_neutral, null)
            .show()
        binding.widgetState.setText(R.string.lab_state_success)
    }

    private fun showMessage(message: Int) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
