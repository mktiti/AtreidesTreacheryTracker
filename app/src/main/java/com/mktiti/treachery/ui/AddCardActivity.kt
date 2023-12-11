package com.mktiti.treachery.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.mktiti.treachery.*
import com.mktiti.treachery.core.*
import com.mktiti.treachery.manager.ResourceLoader
import java.util.concurrent.locks.ReentrantLock

class AddCardActivity : AppCompatActivity() {
    private lateinit var filterAdapter: FilterAdapter

    private lateinit var cardListView: RecyclerView

    companion object {
        const val ADD_CARD_DATA_KEY = "add_card_data"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card)
        setSupportActionBar(findViewById(R.id.toolbar))

        title = "Add card"
        filterAdapter = FilterAdapter(
            this,
            ResourceLoader.getIconManager(this),
            this::addCardRequest
        )
        cardListView = findViewById<RecyclerView>(R.id.card_list).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = filterAdapter
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        Intent().apply {
            setResult(Activity.RESULT_OK, this)
        }

        finish()
    }

    private fun addCardRequest(card: Card, position: Int) {
        Intent().apply {
            putExtra(AddCardActivity.ADD_CARD_DATA_KEY, card.json())
            setResult(Activity.RESULT_OK, this)
        }

        finish()
    }
}