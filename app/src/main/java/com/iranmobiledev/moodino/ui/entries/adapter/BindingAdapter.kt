package com.iranmobiledev.moodino.ui.entries.adapter

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter("bind:loadImageUrl")
fun loadImageUrl(view : ImageView, imagePath: String){
    if(imagePath.isNotEmpty())
    Glide.with(view.context).load(imagePath).into(view)
}