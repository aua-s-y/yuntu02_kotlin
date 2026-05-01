package com.example.yuntushaomiaojia.data.crypto

import android.net.Uri
import com.example.yuntushaomiaojia.data.file.FileStorageRepository
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class FileCryptoRepository(private val fileStorageRepository: FileStorageRepository) {

    @Throws(Exception::class)
    fun encryptFile(uri: Uri, password: String): File {
        val source = fileStorageRepository.readAllBytes(uri)
        val salt = randomBytes(16)
        val iv = randomBytes(12)
        val key = createAesKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(source)

        val file = File(fileStorageRepository.ensureOutputDir("encrypted"), "encrypted_${fileStorageRepository.timeText()}.pdf.aes")
        FileOutputStream(file).use { outputStream ->
            outputStream.write(ENCRYPTION_HEADER)
            outputStream.write(salt)
            outputStream.write(iv)
            outputStream.write(encrypted)
        }
        return file
    }

    @Throws(Exception::class)
    fun decryptFile(uri: Uri, password: String): File {
        val all = fileStorageRepository.readAllBytes(uri)
        if (all.size < ENCRYPTION_HEADER.size + 28) {
            throw IOException("文件格式不正确")
        }
        val header = all.copyOfRange(0, ENCRYPTION_HEADER.size)
        if (!header.contentEquals(ENCRYPTION_HEADER)) {
            throw IOException("这不是本 App 生成的加密文件")
        }

        var offset = ENCRYPTION_HEADER.size
        val salt = all.copyOfRange(offset, offset + 16)
        offset += 16
        val iv = all.copyOfRange(offset, offset + 12)
        offset += 12
        val encrypted = all.copyOfRange(offset, all.size)
        val key = createAesKey(password, salt)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        val decrypted = cipher.doFinal(encrypted)

        val file = File(fileStorageRepository.ensureOutputDir("pdf"), "decrypted_${fileStorageRepository.timeText()}.pdf")
        FileOutputStream(file).use { outputStream -> outputStream.write(decrypted) }
        return file
    }

    private fun createAesKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, 12000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }

    private fun randomBytes(size: Int): ByteArray {
        return ByteArray(size).also { bytes -> SecureRandom().nextBytes(bytes) }
    }

    private companion object {
        private val ENCRYPTION_HEADER = "YTSAES1".toByteArray(StandardCharsets.UTF_8)
    }
}
