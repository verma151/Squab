package com.example.chatapp

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.chatapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        register_button.setOnClickListener {

        performRegister()

        }
        Already_have_account_textview.setOnClickListener {
            Log.d("RegisterActivity","Try to show login Activity")
            val intent=Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }
        selectphoto_button_register.setOnClickListener {
            Log.d("RegisterActivity","Try to show photo selector")

            val intent=Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            startActivityForResult(intent,0)
        }
    }

    var selectedPhotoUri:Uri?=null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==0 && resultCode==Activity.RESULT_OK && data!=null){
            Log.d("RegisterActivity","Photo was selected")
            selectedPhotoUri=data.data
            val bitmap=MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
           selectphoto_imageview_register.setImageBitmap(bitmap)
            selectphoto_button_register.alpha=0f


//            val bitmapDrawable=BitmapDrawable(bitmap)
//            selectphoto_button_register.setBackgroundDrawable(bitmapDrawable)
        }
    }


    private fun performRegister(){
        val email=email_edittext_register.text.toString()
        val password=password_edittext_register.text.toString()
        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this,"Please text in email/password",Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("RegisterActivity","Email is:"+email)
        Log.d("RegisterActivity","Password: $password")

        //Firebase Auth
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if(!it.isSuccessful) return@addOnCompleteListener

                //else if
                Log.d("RegisterActivity","Successfully crerted user with uid: ${it.result?.user?.uid}")
                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Log.d("RegisterActivity","Failed to create user: ${it.message}")
                Toast.makeText(this,"Failed to create user: ${it.message}",Toast.LENGTH_SHORT).show()
            }
    }
    private fun uploadImageToFirebaseStorage(){
        if(selectedPhotoUri==null) return

        val filename=UUID.randomUUID().toString()
        val ref=FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity","Succesfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Log.d("RegisterActivity","File Location: $it")

                    saveUserToFirebaseDatabse(it.toString())
                }
            }
            .addOnFailureListener {
                Log.d("RegisterActivity", "Failed to upload image to storage: ${it.message}")
            }
    }
    private fun saveUserToFirebaseDatabse(profileImageUrl:String){
        val uid=FirebaseAuth.getInstance().uid ?:""
        val ref=FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user= User(uid,username_edittext_register.text.toString(),profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity","Finally we saved the user to Firebase Database")

                val intent=Intent(this,LatestMessagesActivity::class.java)
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("RegisterActivity", "Failed to set value to database: ${it.message}")
            }

    }
}

