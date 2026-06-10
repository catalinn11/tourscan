package com.example.tourscan.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/**
 * Manages the SQLCipher passphrase using Android Keystore (AES/CBC/PKCS7).
 *
 * Generates a random passphrase, encrypts it with a Keystore-backed AES key,
 * and stores the encrypted result in regular SharedPreferences.
 */
class DatabaseKeyManager(private val context: Context) {

    companion object {
        private const val KEY_ALIAS = "tourscan_db_key"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"

        private const val PREFS_NAME = "tourscan_db_prefs"
        private const val PREF_ENCRYPTED = "encrypted_passphrase"
        private const val PREF_IV = "passphrase_iv"
    }

    private val keystore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val cipher = Cipher.getInstance(TRANSFORMATION)
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Creates a new AES key in Android Keystore.
     */
    private fun createKey(): SecretKey {
        return KeyGenerator.getInstance(ALGORITHM, "AndroidKeyStore")
            .apply {
                init(
                    KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(BLOCK_MODE)
                        .setEncryptionPaddings(PADDING)
                        .setRandomizedEncryptionRequired(true)
                        .setUserAuthenticationRequired(false)
                        .build()
                )
            }
            .generateKey()
    }

    /**
     * Gets the existing key from Keystore, or creates a new one.
     */
    private fun getKey(): SecretKey {
        val entry = keystore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return entry?.secretKey ?: createKey()
    }

    /**
     * Encrypts a plaintext passphrase and stores it in SharedPreferences.
     */
    private fun encryptAndStore(passphrase: String) {
        val key = getKey()
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(passphrase.toByteArray(Charsets.UTF_8))

        prefs.edit()
            .putString(PREF_ENCRYPTED, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .putString(PREF_IV, Base64.encodeToString(iv, Base64.NO_WRAP))
            .apply()
    }

    /**
     * Decrypts the stored passphrase from SharedPreferences.
     */
    private fun decryptStored(): String {
        val key = getKey()
        val encrypted = Base64.decode(prefs.getString(PREF_ENCRYPTED, null), Base64.NO_WRAP)
        val iv = Base64.decode(prefs.getString(PREF_IV, null), Base64.NO_WRAP)

        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        val decrypted = cipher.doFinal(encrypted)
        return String(decrypted, Charsets.UTF_8)
    }

    /**
     * Returns the database passphrase.
     * First call: generates random passphrase, encrypts & stores it.
     * Next calls: decrypts and returns the stored passphrase.
     */
    fun getOrCreatePassphrase(): String {
        // If we already have an encrypted passphrase, decrypt and return it
        if (prefs.contains(PREF_ENCRYPTED)) {
            return decryptStored()
        }

        // Generate a new random passphrase
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val random = SecureRandom()
        val passphrase = (1..48).map { chars[random.nextInt(chars.length)] }.joinToString("")

        // Encrypt and store it
        encryptAndStore(passphrase)

        return passphrase
    }
}
