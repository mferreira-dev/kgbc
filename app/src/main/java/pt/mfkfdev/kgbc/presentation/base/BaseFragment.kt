package pt.mfkfdev.kgbc.presentation.base

import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {
	abstract fun setupUI()
	abstract fun setupButtons()
}