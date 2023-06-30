package com.send_message_to_server_test

import ApiService
import Data
import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/*
How to set up google sign in.
https://www.youtube.com/watch?v=suVgcrPwYKQ
1. Go to https://console.cloud.google.com/apis/dashboard
2. Create a new project
3. Go to Credentials and create a new OAuth 2.0 client ID for Android
4. Get the SHA-1 fingerprint
    1. Click Gradle button on right side of Android Studio
    2. If you don't have a Tasks folder
        - Click on wrench icon(Build Tool Settings)
        - Gradle Settings
        - Experimental
        - Uncheck Do not build Gradle task list during Gradle sync
        - Click apply
        - Click Sync Project with Gradle files
    3. Tasks -> android -> signingReport
    4. Run and copy SHA1
5. Create OAuth 2.0 with SHA-1 fingerprint
*/

class MainActivity : AppCompatActivity() {
    lateinit var context: MainActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        context = this
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val google_sign_in_button = findViewById<Button>(R.id.google_sign_in)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 1)

        val google_sign_in_options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        val google_sign_in_client = GoogleSignIn.getClient(this, google_sign_in_options)

        google_sign_in_button.setOnClickListener {
            google_sign_in_client.signOut()
            val sign_in_intent = google_sign_in_client.signInIntent
            google_sign_in_intent_received.launch(sign_in_intent)
        }
    }

    private val google_sign_in_intent_received = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        val data = result.data
        if(data!=null) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            task.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //get google account
                    val google_account: GoogleSignInAccount = task.result
                    if (google_account == null){
                        Toast.makeText(this, "No Google Account.", Toast.LENGTH_SHORT).show()
                        recreate()
                    }
                    setContentView(R.layout.submit_page)

                    val message = findViewById<EditText>(R.id.message)
                    val submit_button = findViewById<Button>(R.id.submit)
                    val log_out_button = findViewById<Button>(R.id.log_out)
                    submit_button.setOnClickListener {
                        if(message.text.toString() != ""){
                            val send_data = Data(google_account.email!!, message.text.toString() )
                            sendDataToLink("http://10.0.0.150:3000", send_data)
                            message.setText("")
                        }else{
                            Toast.makeText(this, "No message.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    log_out_button.setOnClickListener {
                        recreate()
                    }
                }else{
                    Toast.makeText(this, "Google Sign In Failed. No task.", Toast.LENGTH_SHORT).show()
                }
            }
        }else{
            Toast.makeText(this, "Google Sign In Failed. No data.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendDataToLink(link: String, data: Data){
        val gson = GsonBuilder().setLenient().create()
        val retrofitBuilder = Retrofit.Builder().baseUrl(link).addConverterFactory(GsonConverterFactory.create(gson)).build().create(ApiService::class.java)
        val retrofitData = retrofitBuilder.postData(data)
        //send data
        retrofitData.enqueue(object : Callback<Data?> {
            override fun onResponse(call: Call<Data?>, response: Response<Data?>) {}
            override fun onFailure(call: Call<Data?>, t: Throwable) {
                Toast.makeText(context, "Message failure.", Toast.LENGTH_SHORT).show()
            }
        })
    }

}