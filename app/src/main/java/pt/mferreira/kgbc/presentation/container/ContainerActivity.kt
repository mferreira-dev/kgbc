package pt.mferreira.kgbc.presentation.container

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import pt.mferreira.kgbc.R
import pt.mferreira.kgbc.databinding.ActivityContainerBinding
import pt.mferreira.kgbc.presentation.base.BaseActivity

class ContainerActivity : BaseActivity() {

	private lateinit var binding: ActivityContainerBinding
	private lateinit var navController: NavController
	private lateinit var appBarConfiguration: AppBarConfiguration
	lateinit var viewModel: ContainerViewModel

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityContainerBinding.inflate(layoutInflater)
		setContentView(binding.root)
		viewModel = ViewModelProvider(this)[ContainerViewModel::class.java]

		setupUI()
		setupObservers()
	}

	override fun setupUI() {
		val navHostFragment =
			supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

		navController = navHostFragment.findNavController()

		// Up button won't be displayed on these fragments.
		appBarConfiguration = AppBarConfiguration(setOf(R.id.gameboyFragment))
		setupActionBarWithNavController(navController, appBarConfiguration)

		supportActionBar?.setDisplayShowTitleEnabled(false)
	}

	override fun setupObservers() {
		viewModel.isLoading.observe(this) {
			if (it.hasBeenHandled)
				return@observe

			binding.loadingProgressContainer.visibility =
				if (it.peekContent()) View.VISIBLE else View.GONE
		}
	}

}