package com.example.notdefteri.adapter

import android.R
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.notdefteri.databinding.RecyclerRowBinding
import com.example.notdefteri.model.Not
import com.example.notdefteri.view.NotlarFragment
import com.example.notdefteri.view.NotlarFragmentDirections
import android.graphics.BitmapFactory
import android.widget.ImageView
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.ImageButton
import android.view.WindowManager
import android.view.Gravity
import kotlin.math.min
import kotlin.math.max

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
            
            // ImageView ayarlarını her seferinde yeniden ayarla
            notImageView.apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = true
                setImageBitmap(bitmap)
            }
            
            root.setOnClickListener {
                val action = NotlarFragmentDirections.actionNotlarFragmentToNotFragment("eskiMi?", not.id)
                Navigation.findNavController(it).navigate(action)
            }

            buyutButton.setOnClickListener {
                val dialog = AlertDialog.Builder(fragment.requireContext())
                    .create()

                val dialogView = LayoutInflater.from(fragment.requireContext()).inflate(
                    com.example.notdefteri.R.layout.dialog_image_preview, null
                )

                val previewImageView = dialogView.findViewById<ImageView>(com.example.notdefteri.R.id.previewImageView)
                val backButton = dialogView.findViewById<ImageButton>(com.example.notdefteri.R.id.backButton)

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
                    
                    // Resmin gerçek boyutlarını al
                    val imageWidth = bitmap.width
                    val imageHeight = bitmap.height
                    
                    // Dialog boyutlarını hesapla
                    val dialogWidth = (screenWidth * 0.9f).toInt()
                    val dialogHeight = (screenHeight * 0.9f).toInt()
                    
                    val finalDialogWidth: Int
                    val finalDialogHeight: Int
                    
                    if (imageHeight > imageWidth) { // Dikey resim
                        val ratio = imageHeight.toFloat() / imageWidth.toFloat()
                        finalDialogHeight = dialogHeight
                        finalDialogWidth = (dialogHeight / ratio).toInt()
                    } else { // Yatay resim
                        val ratio = imageWidth.toFloat() / imageHeight.toFloat()
                        finalDialogWidth = dialogWidth
                        finalDialogHeight = (dialogWidth / ratio).toInt()
                    }
                    
                    setLayout(finalDialogWidth, finalDialogHeight)
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