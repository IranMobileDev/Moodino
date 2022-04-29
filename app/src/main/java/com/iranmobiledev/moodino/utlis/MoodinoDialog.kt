package com.iranmobiledev.moodino.utlis

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import com.iranmobiledev.moodino.R
import com.iranmobiledev.moodino.databinding.DialogViewBinding
import com.iranmobiledev.moodino.listener.DialogEventListener

class MoodinoDialog(
    @StringRes private val mainText: Int,
    @StringRes private val subText: Int,
    @DrawableRes private val icon: Int,
    @StringRes private val deleteText: Int,
    @StringRes private val cancelText: Int,
) : DialogFragment() {


    private var dialogEventListener: DialogEventListener? = null
    private lateinit var binding: DialogViewBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogViewBinding.inflate(layoutInflater)
        setupUi()
        setupClicks()
        val alertDialog = AlertDialog.Builder(context)
        return alertDialog.setView(binding.root).create()
    }

    private fun setupClicks() {
        binding.leftButton.setOnClickListener {
            dialogEventListener?.clickedItem(R.id.leftButton)
        }
        binding.rightButton.setOnClickListener {
            dialogEventListener?.clickedItem(R.id.rightButton)
        }

    }

    private fun setupUi() {

        if (mainText == -1)
            binding.mainTextDialog.visibility = View.GONE
        else
            binding.mainTextDialog.text = getString(mainText)

        if (subText == -1)
            binding.subTextDialog.visibility = View.GONE
        else
            binding.subTextDialog.text = getString(subText)

        if (deleteText != -1)
            binding.rightButton.text = getString(deleteText)
        else
            binding.rightButton.setText(R.string.dialogDeleteText)

        if (cancelText != -1)
            binding.leftButton.text = getString(cancelText)
        else
            binding.leftButton.setText(R.string.dialogCancelText)
        binding.dialogIcon.setImageResource(icon)
    }

    fun setItemEventListener(dialogEventListener: DialogEventListener) {
        this.dialogEventListener = dialogEventListener
    }
}