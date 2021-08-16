package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.chatapp.models.ChatMessage
import com.example.chatapp.models.User
import com.example.chatapp.views.LatestMessagesRow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessagesActivity : AppCompatActivity() {

    companion object{
        var currentUser: User?=null
        val TAG="LatestMessages"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)
        recyclerview_latest_messages.adapter=adapter
        recyclerview_latest_messages.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, view ->
            Log.d(TAG,"123")
            val intent=Intent(this,ChatLogActivity::class.java)
            val row=item as LatestMessagesRow
            intent.putExtra(NewMessageActivity.USER_KEY,row.chatPartnerUser)
            startActivity(intent)

        }

    //    setupDummyRows()

        listenforLatestMessages()
        fetchCurrentUser()

        verifyUserIsLoggedIn()
    }



    val latestMessagesMap=HashMap<String,ChatMessage>()

    private fun refereshrecyclerview(){
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessagesRow(it))
        }
    }

    private fun listenforLatestMessages(){
        val fromId=FirebaseAuth.getInstance().uid
        val ref=FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object:ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, previousChildName: String?) {
             val chatMessage=p0.getValue(ChatMessage::class.java)?:return
             latestMessagesMap[p0.key!!]=chatMessage
             refereshrecyclerview()

            }

            override fun onChildChanged(p0: DataSnapshot, previousChildName: String?) {
                val chatMessage=p0.getValue(ChatMessage::class.java)?:return
                latestMessagesMap[p0.key!!]=chatMessage
                refereshrecyclerview()
            }
            override fun onChildMoved(p0: DataSnapshot, previousChildName: String?) {
            }
            override fun onChildRemoved(p0: DataSnapshot) {

            }
            override fun onCancelled(p0: DatabaseError) {

            }


        })


    }


    val adapter=GroupAdapter<ViewHolder>()
//    private fun setupDummyRows(){
//
//
//       adapter.add(LatestMessagesRow())
//        adapter.add(LatestMessagesRow())
//        adapter.add(LatestMessagesRow())
//
//
//
//    }


    private fun fetchCurrentUser(){
    val uid=FirebaseAuth.getInstance().uid
    val ref=FirebaseDatabase.getInstance().getReference("/users/$uid")
    ref.addListenerForSingleValueEvent(object :ValueEventListener{
        override fun onDataChange(p0: DataSnapshot) {
         currentUser=p0.getValue(User::class.java)
         Log.d("LatestMessages","Current user ${currentUser?.profileImageUrl}")
        }
        override fun onCancelled(p0: DatabaseError) {

        }

    })

    }

    private fun verifyUserIsLoggedIn(){
        val uid=FirebaseAuth.getInstance().uid
        if(uid==null){
            val intent=Intent(this,RegisterActivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
       when(item?.itemId){
        R.id.menu_new_message-> {
        val intent=Intent(this,NewMessageActivity::class.java)
        startActivity(intent)
        }
        R.id.menu_sign_out->{
        FirebaseAuth.getInstance().signOut()
            val intent=Intent(this,RegisterActivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

       }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)

    }
}