package pt.mferreira.kgbc.domain.emu.ppu

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import pt.mferreira.kgbc.domain.emu.cpu.CPU
import pt.mferreira.kgbc.domain.emu.ppu.Constants.DEFAULT_HEIGHT
import pt.mferreira.kgbc.domain.emu.ppu.Constants.DEFAULT_WIDTH
import pt.mferreira.kgbc.domain.emu.ppu.Constants.DISPLAY_SCALE
import pt.mferreira.kgbc.domain.emu.ppu.Constants.NATIVE_COLOR_1
import pt.mferreira.kgbc.domain.emu.ppu.Constants.NATIVE_COLOR_2
import pt.mferreira.kgbc.domain.emu.ppu.Constants.NATIVE_COLOR_3
import pt.mferreira.kgbc.domain.emu.ppu.Constants.NATIVE_COLOR_4
import pt.mferreira.kgbc.domain.emu.ppu.Constants.PPU_IO_LENGTH
import pt.mferreira.kgbc.domain.emu.ppu.Constants.PPU_IO_START_ADDRESS
import pt.mferreira.kgbc.domain.emu.ppu.Constants.VRAM_LENGTH
import pt.mferreira.kgbc.domain.emu.ppu.Constants.VRAM_START_ADDRESS
import pt.mferreira.kgbc.domain.entities.RefUByte
import pt.mferreira.kgbc.utils.convertPxToDp

class PPU(context: Context, attrs: AttributeSet) : View(context, attrs) {

	private val density = resources.displayMetrics.density

	private lateinit var cpu: CPU

	private lateinit var vram: Array<RefUByte>
	private lateinit var io: Array<RefUByte>

	private val colors = listOf(
		Paint().apply {
			isAntiAlias = true
			color = Color.parseColor(NATIVE_COLOR_1)
		},
		Paint().apply {
			isAntiAlias = true
			color = Color.parseColor(NATIVE_COLOR_2)
		},
		Paint().apply {
			isAntiAlias = true
			color = Color.parseColor(NATIVE_COLOR_3)
		},
		Paint().apply {
			isAntiAlias = true
			color = Color.parseColor(NATIVE_COLOR_4)
		},
	)

	private val rects = listOf(
		Rect(0, 0, 0, 0),
		Rect(0, 0, 0, 0),
		Rect(0, 0, 0, 0),
		Rect(0, 0, 0, 0)
	)

	fun setCpu(cpu: CPU) {
		this.cpu = cpu
		fetchMemory()
	}

	private fun fetchMemory() {
		vram = cpu.fetchMemory(VRAM_START_ADDRESS, VRAM_LENGTH)
		io = cpu.fetchMemory(PPU_IO_START_ADDRESS, PPU_IO_LENGTH)
	}

	override fun onDraw(canvas: Canvas?) {
		super.onDraw(canvas)

//		if (canvas == null)
//			return
//
//		for (h in 0 until DEFAULT_HEIGHT) {
//			for (w in 0 until DEFAULT_WIDTH) {
//				val rect = if (h == 0 || h % 2 > 0)
//					rects.first()
//				else
//					rects.last()
//
//				val paint = if (h == 0 || h % 2 > 0)
//					colors.first()
//				else
//					colors.last()
//
//				rect.left = (w * DISPLAY_SCALE).convertPxToDp(density)
//				rect.top = (h * DISPLAY_SCALE).convertPxToDp(density)
//				rect.right = ((w * DISPLAY_SCALE) + DISPLAY_SCALE).convertPxToDp(density)
//				rect.bottom = ((h * DISPLAY_SCALE) + DISPLAY_SCALE).convertPxToDp(density)
//
//				canvas.drawRect(rect, paint)
//			}
//		}
	}

}