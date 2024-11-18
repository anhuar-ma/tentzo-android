package com.example.app_tentzo

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Referenciar vistas
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val forgotPasswordButton = findViewById<Button>(R.id.forgotPasswordButton)

        var isPasswordVisible = false

        // Lógica para mostrar/ocultar contraseña al tocar el ícono
        passwordEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = passwordEditText.compoundDrawables[2] // Ícono en la posición 'drawableEnd'
                if (drawableEnd != null && event.rawX >= (passwordEditText.right - drawableEnd.bounds.width())) {
                    // Alternar visibilidad de la contraseña
                    isPasswordVisible = !isPasswordVisible
                    if (isPasswordVisible) {
                        passwordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    } else {
                        passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                    }
                    // Mantener el cursor al final del texto
                    passwordEditText.setSelection(passwordEditText.text.length)
                    return@setOnTouchListener true
                }
            }
            false
        }

        // Lógica del botón de inicio de sesión
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Ingreso exitoso", Toast.LENGTH_SHORT).show()
                            // Redirigir a NavigationActivity
                            val intent = Intent(this, NavigationActivity::class.java)
                            startActivity(intent)
                            finish() // Finaliza LoginActivity
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Ingrese correo y contraseña", Toast.LENGTH_SHORT).show()
            }
        }

        // Lógica del botón "Olvidé mi contraseña"
        forgotPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isNotEmpty()) {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Correo de restablecimiento enviado a $email", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Ingrese su correo para restablecer la contraseña", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
