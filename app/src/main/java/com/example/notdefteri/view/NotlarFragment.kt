package com.example.notdefteri.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.notdefteri.adapter.NotlarAdapter
import com.example.notdefteri.databinding.FragmentNotlarBinding
import com.example.notdefteri.model.Not
import com.example.notdefteri.roomdb.NotlarDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import android.text.Editable
import android.text.TextWatcher

class NotlarFragment : Fragment() {

    private var _binding: FragmentNotlarBinding? = null
    private val binding get() = _binding!!
    private val mDisposable = CompositeDisposable()
    private lateinit var notlarAdapter: NotlarAdapter
    private lateinit var db: NotlarDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        db = Room.databaseBuilder(requireContext(), NotlarDatabase::class.java, "Notlar")
            .build()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotlarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        notlarAdapter = NotlarAdapter(emptyList(), this)
        binding.recyclerView.adapter = notlarAdapter

        binding.fab.setOnClickListener {
            val action = NotlarFragmentDirections.actionNotlarFragmentToNotFragment("yeniMi?", 0)
            Navigation.findNavController(it).navigate(action)
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.toString()?.let { aramaYap(it) }
            }
        })

        tumNotlariAl()
    }

    private fun aramaYap(aramaMetni: String) {
        mDisposable.add(
            db.notlarDao().tumNotlar()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { notlarListesi ->
                    val filtrelenmisListe = if (aramaMetni.isEmpty()) {
                        notlarListesi
                    } else {
                        notlarListesi.filter { not ->
                            not.baslik.contains(aramaMetni, ignoreCase = true) ||
                            not.icerik.contains(aramaMetni, ignoreCase = true)
                        }
                    }
                    notlarAdapter.notlariGuncelle(filtrelenmisListe)
                }
        )
    }

    private fun tumNotlariAl() {
        mDisposable.add(
            db.notlarDao().tumNotlar()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { notlarListesi ->
                        notlarAdapter.notlariGuncelle(notlarListesi)
                    },
                    { throwable ->
                        throwable.printStackTrace()
                        Toast.makeText(requireContext(), "Notlar yüklenirken bir hata oluştu", Toast.LENGTH_LONG).show()
                    }
                )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}