package pt.mfkfdev.kgbc.presentation.base

import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
	abstract fun setupUI()
	abstract fun setupObservers()
}