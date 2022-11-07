package pt.mferreira.kgbc.presentation.gameboy

import android.app.Activity.RESULT_OK
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

		setupUI()
		setupButtons()
	}

	override fun setupButtons() {
		binding.gameboyDpadUp.setOnClickListener { }
		binding.gameboyDpadDown.setOnClickListener { }
		binding.gameboyDpadLeft.setOnClickListener { }
		binding.gameboyDpadRight.setOnClickListener { }

		binding.gameboyAbA.setOnClickListener { }
		binding.gameboyAbB.setOnClickListener { }

		binding.gameboyStart.setOnClickListener { }
		binding.gameboySelect.setOnClickListener { }
	}

	override fun setupUI() {
		menuHost.addMenuProvider(object : MenuProvider {
			override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
				if (BuildConfig.FLAVOR == DEV_FLAVOR)
					menuInflater.inflate(R.menu.dev_menu, menu)
				else
					menuInflater.inflate(R.menu.prod_menu, menu)
			}

			override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
				return when (menuItem.itemId) {
					R.id.menu_open -> {
						fragmentViewModel.openRom(filePicker)
						true
					}
					R.id.menu_power_on -> {
						fragmentViewModel.powerOn()
						true
					}
					R.id.menu_power_off -> {
						fragmentViewModel.powerOff()
						true
					}
					R.id.menu_dump_memory -> {
						fragmentViewModel.dumpMemory()
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

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

}