package com.extrainch.ngao.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.extrainch.ngao.databinding.ItemSliderBinding
import com.smarteist.autoimageslider.SliderViewAdapter


class AdapterImageSlider(private var images: IntArray) :
    SliderViewAdapter<AdapterImageSlider.SliderViewHolder>() {
    var binding: ItemSliderBinding? = null

    override fun onCreateViewHolder(parent: ViewGroup): SliderViewHolder {
        binding = ItemSliderBinding.inflate(LayoutInflater.from(parent.context))
        val v: View = binding!!.root
        return SliderViewHolder(v)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        holder.imgV.setImageResource(images[position])
    }

    override fun getCount(): Int {
        return images.size
    }

    class SliderViewHolder(itemView: View) : ViewHolder(itemView) {
        var imgV: ImageView

        init {
            val sliderItemBinding: ItemSliderBinding = ItemSliderBinding.bind(itemView)
            imgV = sliderItemBinding.imageView
        }
    }
}