package com.bilocan.notdefteri.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.RecyclerView
import com.bilocan.notdefteri.R
import com.bilocan.notdefteri.databinding.RecyclerRowBinding
import com.bilocan.notdefteri.model.Not
import com.bilocan.notdefteri.view.NotlarFragment
import android.graphics.BitmapFactory
import android.widget.ImageView
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.ImageButton
import android.view.WindowManager
import android.view.Gravity
import com.bilocan.notdefteri.view.NotlarFragmentDirections

class NotlarAdapter(private var notlarListesi: List<Not>, private val fragment: NotlarFragment) 
    : RecyclerView.Adapter<NotlarAdapter.NotlarHolder>() {

    class NotlarHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotlarHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotlarHolder(binding)
    }

    override fun getItemCount(): Int {
        return notlarListesi.size
    }

    override fun onBindViewHolder(holder: NotlarHolder, position: Int) {
        val not = notlarListesi[position]
        holder.binding.apply {
            notBaslikText.text = not.baslik
            val bitmap = BitmapFactory.decodeByteArray(not.gorsel, 0, not.gorsel.size)
            
            notImageView.apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = true
                setImageBitmap(bitmap)
            }
            
            root.setOnClickListener {
                val action: NavDirections = NotlarFragmentDirections.actionNotlarFragmentToNotFragment("eskiMi?", not.id)
                Navigation.findNavController(it).navigate(action)
            }

            buyutButton.setOnClickListener {
                val dialog = AlertDialog.Builder(fragment.requireContext())
                    .create()

                val dialogView = LayoutInflater.from(fragment.requireContext()).inflate(
                    R.layout.dialog_image_preview, null
                )

                val previewImageView = dialogView.findViewById<ImageView>(R.id.previewImageView)
                val backButton = dialogView.findViewById<ImageButton>(R.id.backButton)

                // Resmi ayarla
                previewImageView.apply {
                    setImageBitmap(bitmap)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    adjustViewBounds = true
                }
                
                backButton.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.setView(dialogView)
                
                // Dialog penceresini ayarla
                dialog.window?.apply {
                    setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    
                    // Ekran boyutlarını al
                    val displayMetrics = fragment.requireContext().resources.displayMetrics
                    val screenWidth = displayMetrics.widthPixels
                    val screenHeight = displayMetrics.heightPixels
                    
                    // Dialog boyutlarını hesapla
                    val dialogWidth = (screenWidth * 0.9f).toInt()
                    val dialogHeight = (screenHeight * 0.9f).toInt()
                    
                    setLayout(dialogWidth, dialogHeight)
                    setGravity(Gravity.CENTER)
                }
                
                dialog.show()
            }
        }
    }

    fun notlariGuncelle(yeniNotlarListesi: List<Not>) {
        notlarListesi = yeniNotlarListesi
        notifyDataSetChanged()
    }
}