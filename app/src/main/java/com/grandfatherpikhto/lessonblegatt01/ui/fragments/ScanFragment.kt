package com.grandfatherpikhto.lessonblegatt01.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.grandfatherpikhto.blin.BleManager
import com.grandfatherpikhto.blin.BleScanManager
import com.grandfatherpikhto.lessonblegatt01.GattApplication
import com.grandfatherpikhto.lessonblegatt01.R
import com.grandfatherpikhto.lessonblegatt01.databinding.FragmentScanBinding
import com.grandfatherpikhto.lessonblegatt01.ui.adapters.RvBtAdapter
import com.grandfatherpikhto.lessonblegatt01.ui.models.MainActivityViewModel
import com.grandfatherpikhto.lessonblegatt01.ui.models.ScanViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ScanFragment : Fragment() {


    private var _binding: FragmentScanBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val rvBtAdapter = RvBtAdapter()
    private val logTag = this.javaClass.simpleName

    private val mainActivityViewModel: MainActivityViewModel by activityViewModels<MainActivityViewModel>()
    private val scanViewModel: ScanViewModel by viewModels<ScanViewModel> ()

    private var _bleManager: BleManager? = null
    private val bleManager: BleManager get() = _bleManager!!

    private val scanMenuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            Log.d(logTag, "onCreateMenu()")
            menuInflater.inflate(R.menu.menu_scan, menu)
            menu.findItem(R.id.action_scan)?.let { actionScan ->
                lifecycleScope.launch {
                    bleManager.flowScanState.collect { state ->
                        if (state == BleScanManager.State.Scanning) {
                            actionScan.title = getString(R.string.action_stop_scan)
                            actionScan.setIcon(R.drawable.ic_stop_scan)
                        } else {
                            actionScan.title = getString(R.string.action_scan)
                            actionScan.setIcon(R.drawable.ic_start_scan)
                        }
                    }
                }
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when(menuItem.itemId) {
                R.id.action_scan -> {
                    if (bleManager.scanState == BleScanManager.State.Scanning) {
                        bleManager.stopScan()
                    } else {
                        bleManager.startScan(stopTimeout = 10000L)
                    }
                    true
                }
                else -> { true }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentScanBinding.inflate(inflater, container, false)
        _bleManager = (requireActivity().application as GattApplication).bleManager!!

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFragment()
        linkMenu(true)
    }

    override fun onPause() {
        bleManager.stopScan()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        linkMenu(false)
    }

    private fun linkMenu(link: Boolean) {
        val menuHost: MenuHost = requireActivity()
        if (link) {
            menuHost.addMenuProvider(scanMenuProvider)
        } else {
            menuHost.removeMenuProvider(scanMenuProvider)
        }
    }

    private fun initFragment() {
        rvBtAdapter.setItems(bleManager.scanner.devices)

        binding.apply {
            rvBtDevices.adapter = rvBtAdapter
            rvBtDevices.layoutManager = LinearLayoutManager(requireContext())
        }

        rvBtAdapter.setOnClickListener { bluetoothDevice, _ ->
            mainActivityViewModel.changeCurrentDevice(bluetoothDevice)
            val navController = findNavController()
            val currentDestination = navController.currentDestination
            lifecycleScope.launch {
                if (currentDestination?.id == R.id.scanFragment) {
                    navController.navigate(R.id.action_scanFragment_to_gattFragment)
                }
            }
        }

        lifecycleScope.launch {
            scanViewModel.flowDevice.filterNotNull().collect { bluetoothDevice ->
                rvBtAdapter.addItem(bluetoothDevice)
            }
        }

        lifecycleScope.launch {
            bleManager.flowDevice.filterNotNull().collect { bluetoothDevice ->
                scanViewModel.changeDevice(bluetoothDevice)
            }
        }

        lifecycleScope.launch {
            bleManager.flowScanError.collect { errorCode ->
                scanViewModel.changeScanError(errorCode)
            }
        }
    }
}