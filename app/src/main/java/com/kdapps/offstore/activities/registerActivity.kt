package com.kdapps.offstore.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.kdapps.offstore.R
import com.kdapps.offstore.firestore.FirestoreClass
import com.kdapps.offstore.models.User
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import org.w3c.dom.Text

class registerActivity : baseActivity() {

    var checkUsernameStatus:Boolean = false
    var Username:String? = null
    private lateinit var usernameReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        setupActionBar()
        usernameReference = FirebaseDatabase.getInstance().getReference("userNames")

        new_register.setOnClickListener(){
            registerUser()
        }


        login.setOnClickListener(){
            val intent = Intent(this@registerActivity, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private  fun setupActionBar(){
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if(actionBar!= null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
        }
        toolbar.setNavigationOnClickListener{ onBackPressed()}
    }

    private fun validateRegisterDetails(): Boolean{
        return when{
            TextUtils.isEmpty(full_name.text.toString().trim{ it <= ' '}) ->{
                showErrorSnackBar(resources.getString(R.string.err_first_name), true)
                false
            }

            TextUtils.isEmpty(username.text.toString().trim() {it <= ' '}) ->{
                showErrorSnackBar(resources.getString(R.string.err_last_name), true)
                false
            }


            TextUtils.isEmpty(email_id.text.toString().trim() {it <= ' '}) ->{
                showErrorSnackBar(resources.getString(R.string.err_email), true)
                false
            }

            TextUtils.isEmpty(password.text.toString().trim() { it <= ' ' }) ->{
                showErrorSnackBar(resources.getString(R.string.err_password),true)
                false
            }

            TextUtils.isEmpty(confirm_password.text.toString().trim() { it <= ' ' }) ->{
                showErrorSnackBar(resources.getString(R.string.err_confiem_password),true)
                false
            }

            password.text.toString().trim(){it <= ' '} != confirm_password.text.toString().trim(){it <=' '} ->{
                showErrorSnackBar(resources.getString(R.string.err_confirm_not_match_password), true)
                false
            }

            !terms.isChecked ->{
                showErrorSnackBar(resources.getString(R.string.err_terms_not_checked), true)
                false
            }

            !checkUsernameStatus ->{
                showErrorSnackBar("Username already taken",true)
                false
            }

            else ->{
                // showErrorSnackBar("Your details are valid", false)
                true
            }
        }

    }



    private fun registerUser(){
        Username = username.text.toString()
        checkUsername(Username!!)

        if (validateRegisterDetails() && checkUsernameStatus){

            showProgressDialog(resources.getString(R.string.please_wait))

            val email: String = email_id.text.toString().trim(){it <=' '}
            val password: String = password.text.toString().trim(){it <=' '}

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                    OnCompleteListener<AuthResult> { task ->

                        //if registration is successful
                        if(task.isSuccessful){
                            //firebase registered user
                            val firebaseUser: FirebaseUser = task.result!!.user!!
                            val user = User(
                                    firebaseUser.uid,
                                    full_name.text.toString().trim{it <= ' '},
                                    username.text.toString().trim{it <= ' '},
                                    email_id.text.toString().trim{it <= ' '}
                            )

                            FirestoreClass().registerUser(this@registerActivity, user)

                            //FirebaseAuth.getInstance().signOut()
                            //finish()

                        }else{
                            hideProgressDialog()
                            showErrorSnackBar(task.exception!!.message.toString(), true)
                        }
                    }
            )
        }
//        else{
//            showErrorSnackBar("Username already taken", true)
//        }
    }

    fun userRegistrationSuccess(){
        addUsernameToDatabase(Username)
        hideProgressDialog()

        Toast.makeText(this@registerActivity,"Register Successful", Toast.LENGTH_LONG).show()

    }

    private fun addUsernameToDatabase(Username: String?){
        usernameReference.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                usernameReference.child(Username!!).setValue(true)
            }

        })
    }

    private fun checkUsername(Username: String?){
        usernameReference.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                showErrorSnackBar("Something went wrong", true)
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(Username != null) {
                    checkUsernameStatus = !snapshot.hasChild(Username)
                }

            }

        })
    }

}