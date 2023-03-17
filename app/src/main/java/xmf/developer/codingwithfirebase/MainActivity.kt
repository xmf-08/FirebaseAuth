package xmf.developer.codingwithfirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import xmf.developer.codingwithfirebase.databinding.ActivityMainBinding

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    lateinit var googleSignInClient:GoogleSignInClient
    lateinit var auth:FirebaseAuth
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        googleSingInFunction()
    }

    private fun googleSingInFunction() {

        if (auth.currentUser!=null){
            binding.btnReg.text = auth.currentUser?.displayName
            binding.btnReg.isEnabled = false
            binding.txtSigning.text = "You are already Signed In"
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)


        binding.btnReg.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, 1)
        }

        binding.btnReg.setOnLongClickListener {
            auth.signOut()
            Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show()
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {

                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "onActivityResult: ${account.displayName}")
                firebaseAuthWithGoogle(account.idToken!!)
            }catch (e:ApiException){
                Log.d(TAG, "onActivityResult: failure ${e.message}")
            }
        }
    }

     fun firebaseAuthWithGoogle(idToken: String) {
         val credential = GoogleAuthProvider.getCredential(idToken, null)
         auth.signInWithCredential(credential)
             .addOnCompleteListener(this) { task ->
                 if (task.isSuccessful) {
                     // Sign in success, update UI with the signed-in user's information
                     Log.d(TAG, "signInWithCredential:success")
                     val user = auth.currentUser
//                    updateUI(user)
                     Toast.makeText(this, "${user?.email}", Toast.LENGTH_SHORT).show()
                 } else {
                     // If sign in fails, display a message to the user.
                     Log.w(TAG, "signInWithCredential:failure", task.exception)
//                    updateUI(null)
                     Toast.makeText(this, "${task.exception?.message}", Toast.LENGTH_SHORT).show()
                 }
             }
    }
}