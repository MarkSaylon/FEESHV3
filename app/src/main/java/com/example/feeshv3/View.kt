package com.example.feeshv3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.feeshv3.data.UserViewModel

class View : AppCompatActivity() {

    private lateinit var recordViewModel: UserViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        recyclerView = findViewById(R.id.rec)
        adapter = ListAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        recordViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        recordViewModel.readAllData.observe(this, Observer { records ->
            records?.let {
                adapter.setData(it)
            }
        })

        adapter.setOnItemLongClickListener { record ->
            // Delete the record from the database
            recordViewModel.deleteRecord(record)
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()

            // Notify the adapter that the data has changed
            adapter.notifyDataSetChanged()

            true // Return true to indicate that the click has been handled
        }


    }


}