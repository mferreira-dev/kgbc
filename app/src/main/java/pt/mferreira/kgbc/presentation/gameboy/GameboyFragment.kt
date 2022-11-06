package pt.mferreira.kgbc.presentation.gameboy

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import pt.mferreira.kgbc.BuildConfig
import pt.mferreira.kgbc.R
import pt.mferreira.kgbc.databinding.FragmentGameboyBinding
import pt.mferreira.kgbc.domain.emu.cpu.CPU
import pt.mferreira.kgbc.presentation.base.BaseFragment
import pt.mferreira.kgbc.presentation.container.ContainerViewModel
import pt.mferreira.kgbc.utils.Globals.DEV_FLAVOR
import pt.mferreira.kgbc.utils.convertToHex4
import pt.mferreira.kgbc.utils.displayToast

class GameboyFragment : BaseFragment() {

	private var _binding: FragmentGameboyBinding? = null
	private val binding get() = _binding!!

	private lateinit var fragmentViewModel: GameboyViewModel
	private lateinit var activityViewModel: ContainerViewModel

	private lateinit var menuHost: MenuHost

	private lateinit var registers: List<TextView>

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
		setupObservers()
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
		registers = listOf(
			binding.gameboyDebugValueA,
			binding.gameboyDebugValueF,
			binding.gameboyDebugValueB,
			binding.gameboyDebugValueC,
			binding.gameboyDebugValueD,
			binding.gameboyDebugValueE,
			binding.gameboyDebugValueH,
			binding.gameboyDebugValueL,
			binding.gameboyDebugValueAf,
			binding.gameboyDebugValueBc,
			binding.gameboyDebugValueDe,
			binding.gameboyDebugValueHl,
			binding.gameboyDebugValueSp,
			binding.gameboyDebugValuePc
		)

		if (BuildConfig.FLAVOR == DEV_FLAVOR)
			binding.gameboyDebug.visibility = View.VISIBLE

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
						fragmentViewModel.openRom(filePicker)
						true
					}
					R.id.menu_dump_memory -> {
						CPU.dump(requireContext())
						true
					}
					R.id.menu_boot -> {
						CPU.boot()
						true
					}
					else -> false
				}
			}
		}, viewLifecycleOwner, Lifecycle.State.RESUMED)
	}

	override fun setupObservers() {
		fragmentViewModel.registerValues.observe(viewLifecycleOwner) {
			it.forEachIndexed { index, int ->
				registers[index].text = int.toUShort().convertToHex4()
			}
		}
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