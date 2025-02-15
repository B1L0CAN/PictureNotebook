package com.bilocan.notdefteri.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import com.bilocan.notdefteri.R
import com.bilocan.notdefteri.databinding.FragmentNotBinding
import com.bilocan.notdefteri.model.Not
import com.bilocan.notdefteri.roomdb.NotlarDAO
import com.bilocan.notdefteri.roomdb.NotlarDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.IOException
import android.widget.ImageView
import android.view.WindowManager

class NotFragment : Fragment() {

    private var _binding : FragmentNotBinding?= null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var secilenGorsel : Uri?= null
    private var secilenBitmap : Bitmap?= null
    private var secilenNot: Not? = null

    private lateinit var db: NotlarDatabase
    private lateinit var notlarDao : NotlarDAO

    private val mDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()

        db = Room.databaseBuilder(requireContext(), NotlarDatabase::class.java,"Notlar")
            .build()
        notlarDao = db.notlarDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Klavye davranışını ayarla
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        // EditText'lere odaklanıldığında ScrollView'ı kaydır
        binding.baslikText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.scrollView.post {
                    binding.scrollView.smoothScrollTo(0, binding.titleInputLayout.top)
                }
            }
        }

        binding.icerikText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.scrollView.post {
                    binding.scrollView.smoothScrollTo(0, binding.noteInputLayout.top)
                }
            }
        }

        binding.imageView.setOnClickListener { gorselSec(it) }
        binding.silButton.setOnClickListener { sil(it) }
        binding.kaydetButton.setOnClickListener { kaydet(it) }
        binding.backButton.setOnClickListener { 
            requireActivity().onBackPressed()
        }

        arguments?.let {
            val bilgi = com.bilocan.notdefteri.view.NotFragmentArgs.fromBundle(it).bilgi

            if (bilgi == "yeniMi?"){
                secilenNot = null
                binding.silButton.isEnabled = false
                binding.kaydetButton.isEnabled = true
                binding.kaydetButton.text = "Kaydet"
            }
            else {
                binding.silButton.isEnabled = true
                binding.kaydetButton.isEnabled = true
                binding.kaydetButton.text = "Güncelle"

                val id = com.bilocan.notdefteri.view.NotFragmentArgs.fromBundle(it).id

                mDisposable.add(
                    notlarDao.findById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponse)
                )
            }
        }
    }

    private fun handleResponse(not: Not){
        try {
            val bitmap = BitmapFactory.decodeByteArray(not.gorsel, 0, not.gorsel.size)
            binding.imageView.apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = true
                setImageBitmap(bitmap)
            }
            
            binding.baslikText.setText(not.baslik)
            binding.icerikText.setText(not.icerik)
            
            secilenNot = not
            secilenBitmap = bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Görsel yüklenirken bir hata oluştu", Toast.LENGTH_LONG).show()
        }
    }

    fun kaydet(view: View) {
        val baslik = binding.baslikText.text.toString()
        val icerik = binding.icerikText.text.toString()

        if (baslik.isEmpty() || icerik.isEmpty()) {
            Toast.makeText(requireContext(), "Lütfen tüm alanları doldurun", Toast.LENGTH_LONG).show()
            return
        }

        if (secilenBitmap != null) {
            try {
                val outputStream = ByteArrayOutputStream()
                val compressedBitmap = if (secilenBitmap!!.width > 2048 || secilenBitmap!!.height > 2048) {
                    val scale = Math.min(2048f / secilenBitmap!!.width, 2048f / secilenBitmap!!.height)
                    val newWidth = (secilenBitmap!!.width * scale).toInt()
                    val newHeight = (secilenBitmap!!.height * scale).toInt()
                    Bitmap.createScaledBitmap(secilenBitmap!!, newWidth, newHeight, true)
                } else {
                    secilenBitmap!!
                }
                
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                val byteDizisi = outputStream.toByteArray()

                if (byteDizisi.size > 8 * 1024 * 1024) { // 8MB kontrol sınırı
                    Toast.makeText(requireContext(), "Resim boyutu çok büyük (Maksimum 8MB)", Toast.LENGTH_LONG).show()
                    return
                }

                if (secilenNot != null) {
                    val guncellenmisNot = Not(baslik = baslik, icerik = icerik, gorsel = byteDizisi)
                    guncellenmisNot.id = secilenNot!!.id
                    
                    mDisposable.add(
                        notlarDao.delete(secilenNot!!)
                            .andThen(notlarDao.insert(guncellenmisNot))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::handleResponseForInsertionAndDeletion)
                    )
                } else {
                    val not = Not(baslik = baslik, icerik = icerik, gorsel = byteDizisi)
                    mDisposable.add(
                        notlarDao.insert(not)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::handleResponseForInsertionAndDeletion)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Resim sıkıştırılırken bir hata oluştu", Toast.LENGTH_LONG).show()
            }
        } else if (secilenNot != null) {
            val guncellenmisNot = Not(baslik = baslik, icerik = icerik, gorsel = secilenNot!!.gorsel)
            guncellenmisNot.id = secilenNot!!.id
            
            mDisposable.add(
                notlarDao.delete(secilenNot!!)
                    .andThen(notlarDao.insert(guncellenmisNot))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsertionAndDeletion)
            )
        } else {
            Toast.makeText(requireContext(), "Lütfen bir görsel seçin", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleResponseForInsertionAndDeletion() {
        val action = com.bilocan.notdefteri.view.NotFragmentDirections.actionNotFragmentToNotlarFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }

    fun sil(view: View){
        if(secilenNot != null){
            mDisposable.add(
                notlarDao.delete(not = secilenNot!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsertionAndDeletion)
            )
        }
    }

    private fun galeriAc() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        try {
            activityResultLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Galeri açılırken bir hata oluştu", Toast.LENGTH_LONG).show()
        }
    }

    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    try {
                        secilenGorsel = uri
                        
                        // Kalıcı izin almak için flags kontrolü
                        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        // Kalıcı izin ver
                        requireActivity().contentResolver.takePersistableUriPermission(uri, takeFlags)

                        // Resmin boyutunu kontrol et
                        val fileSize = getFileSize(uri)
                        if (fileSize > 8 * 1024 * 1024) { // 8MB üstü
                            Toast.makeText(requireContext(), "Resim boyutu çok büyük (Maksimum 8MB)", Toast.LENGTH_LONG).show()
                            return@let
                        }
                        
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(requireActivity().contentResolver, uri)
                            secilenBitmap = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                                decoder.isMutableRequired = true
                                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                            }
                        } else {
                            val options = BitmapFactory.Options().apply {
                                inPreferredConfig = Bitmap.Config.RGB_565
                            }
                            requireActivity().contentResolver.openInputStream(uri)?.use { input ->
                                secilenBitmap = BitmapFactory.decodeStream(input, null, options)
                            }
                        }
                        
                        binding.imageView.apply {
                            scaleType = ImageView.ScaleType.FIT_CENTER
                            adjustViewBounds = true
                            setImageBitmap(secilenBitmap)
                        }
                        
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Görsel yüklenirken bir hata oluştu: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { izinVerildi ->
            if (izinVerildi) {
                galeriAc()
            } else {
                Toast.makeText(requireContext(), "Galeri erişim izni verilmedi", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getFileSize(uri: Uri): Long {
        return try {
            val fileDescriptor = requireContext().contentResolver.openFileDescriptor(uri, "r")
            val fileSize = fileDescriptor?.statSize ?: 0
            fileDescriptor?.close()
            fileSize
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    private fun galeriIzniKontrol(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun galeriIzniIste(view: View) {
        val izin = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), izin)) {
            Snackbar.make(view, "Not görseli eklemek için galeri erişimi gerekmektedir.", Snackbar.LENGTH_INDEFINITE)
                .setAction("İzin ver") {
                    permissionLauncher.launch(izin)
                }.show()
        } else {
            permissionLauncher.launch(izin)
        }
    }

    fun gorselSec(view: View) {
        if (galeriIzniKontrol()) {
            galeriAc()
        } else {
            galeriIzniIste(view)
        }
    }

    private fun kucukBitmapOlustur(kullanicininSectigiBitmap: Bitmap, maximumBoyut: Int ): Bitmap {
        var width = kullanicininSectigiBitmap.width
        var height = kullanicininSectigiBitmap.height

        val bitmapOrani : Double =width.toDouble() / height.toDouble()

        if (bitmapOrani > 1){
            width = maximumBoyut
            val kisaltilmisYukseklik =width/bitmapOrani
            height= kisaltilmisYukseklik.toInt()
        }else {
            height=maximumBoyut
            val kisaltilmisEn = height / bitmapOrani
            width = kisaltilmisEn.toInt()
        }
        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap,width,height,true)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        mDisposable.clear()
    }
}