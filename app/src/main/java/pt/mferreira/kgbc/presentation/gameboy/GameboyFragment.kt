package pt.mferreira.kgbc.presentation.gameboy

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import pt.mferreira.kgbc.BuildConfig
import pt.mferreira.kgbc.R
import pt.mferreira.kgbc.databinding.FragmentGameboyBinding
import pt.mferreira.kgbc.domain.emu.CPU
import pt.mferreira.kgbc.domain.emu.RomManager
import pt.mferreira.kgbc.presentation.base.BaseFragment
import pt.mferreira.kgbc.presentation.container.ContainerViewModel
import pt.mferreira.kgbc.utils.Globals.DEV_FLAVOR

class GameboyFragment : BaseFragment() {

	private var _binding: FragmentGameboyBinding? = null
	private val binding get() = _binding!!

	private lateinit var fragmentViewModel: GameboyViewModel
	private lateinit var activityViewModel: ContainerViewModel

	private lateinit var menuHost: MenuHost

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentGameboyBinding.inflate(inflater, container, false)
		fragmentViewModel = ViewModelProvider(requireActivity())[GameboyViewModel::class.java]
		activityViewModel = ViewModelProvider(requireActivity())[ContainerViewModel::class.java]
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		menuHost = requireActivity()
		RomManager.deleteTempRom(requireContext())

		setupUI()
		setupButtons()
	}

	override fun setupButtons() {

	}

	override fun setupUI() {
		menuHost.addMenuProvider(object : MenuProvider {
			override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
				if (BuildConfig.FLAVOR == DEV_FLAVOR)
					menuInflater.inflate(R.menu.debug_menu, menu)
				else
					menuInflater.inflate(R.menu.release_menu, menu)
			}

			override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
				return when (menuItem.itemId) {
					R.id.menu_open -> {
						openRom()
						true
					}
					R.id.menu_dump_memory -> {
						CPU.dumpMemory(requireContext())
						true
					}
					else -> false
				}
			}
		}, viewLifecycleOwner, Lifecycle.State.RESUMED)
	}

	private val filePicker = registerForActivityResult(StartActivityForResult()) { result ->
		if (result.resultCode != RESULT_OK)
			return@registerForActivityResult

		fragmentViewModel.handleFilePickerResult(result)
	}

	private fun openRom() {
		val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*" }
		filePicker.launch(intent)
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

}