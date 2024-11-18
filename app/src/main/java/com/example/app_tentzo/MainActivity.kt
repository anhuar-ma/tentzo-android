package com.example.app_tentzo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Verificar si el usuario ya está autenticado
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Usuario autenticado: redirigir a NavigationActivity
            startActivity(Intent(this, NavigationActivity::class.java))
            finish()
        } else {
            // Usuario no autenticado: mostrar pantalla principal
            setContentView(R.layout.activity_main)

            // Configurar Google Sign-In
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Agrega este ID en strings.xml
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)

            // Referenciar botones
            val loginButton: Button = findViewById(R.id.loginButton)
            val signUpButton: Button = findViewById(R.id.signUpButton)
            val googleLoginButton: LinearLayout = findViewById(R.id.googleLoginButton)

           // Configurar un listener para el clic
            googleLoginButton.setOnClickListener {
                // Acción al hacer clic
                Toast.makeText(this, "Iniciar sesión con Google", Toast.LENGTH_SHORT).show()
            }


            // Navegar a LoginActivity
            loginButton.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
            }

            // Navegar a SignUpActivity
            signUpButton.setOnClickListener {
                startActivity(Intent(this, SignUpActivity::class.java))
            }

            // Manejar Google Sign-In
            googleLoginButton.setOnClickListener {
                signInWithGoogle()
            }
        }
    }

    private fun signInWithGoogle() {
        // Configurar Google Sign-In para forzar la selección de cuenta
        googleSignInClient.signOut() // Esto asegura que se muestre el selector de cuentas

        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.w("MainActivity", "Google sign in failed", e)
                Toast.makeText(this, "Google Sign-In falló", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Inicio de sesión exitoso
                    Toast.makeText(this, "Inicio de sesión con Google exitoso", Toast.LENGTH_SHORT).show()
                    // Redirigir a NavigationActivity o cualquier otra actividad
                    val intent = Intent(this, NavigationActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Falló el inicio de sesión
                    Toast.makeText(this, "Autenticación fallida", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
