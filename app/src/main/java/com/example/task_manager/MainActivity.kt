package com.example.task_manager

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val signInButton = findViewById<MaterialButton>(R.id.sign_in_button)
        signInButton.setOnClickListener {
            signIn()
        }

        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            val intent = Intent(this, ActivityTask::class.java)
            startActivity(intent)
            finish()
        }

    }

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            Toast.makeText(this, "Login com sucesso: ${account.email}", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ActivityTask::class.java)
            startActivity(intent)
            finish()

        } catch (e: ApiException) {
            Toast.makeText(this, "Erro no login: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }
}
