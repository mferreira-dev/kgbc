package pt.mfkfdev.kgbc.presentation.gameboy

import android.os.Bundle
import android.view.*
import androidx.lifecycle.ViewModelProvider
import pt.mfkfdev.kgbc.R
import pt.mfkfdev.kgbc.databinding.FragmentGameboyBinding
import pt.mfkfdev.kgbc.presentation.base.BaseFragment
import pt.mfkfdev.kgbc.presentation.container.ContainerViewModel

class GameboyFragment : BaseFragment() {
	private var _binding: FragmentGameboyBinding? = null
	private val binding get() = _binding!!
	lateinit var fragmentViewModel: GameboyViewModel
	lateinit var activityViewModel: ContainerViewModel

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

	override fun setupUI() {

	}

	override fun setupButtons() {

	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		super.onCreateOptionsMenu(menu, inflater)
		inflater.inflate(R.menu.menu, menu)
	}
}