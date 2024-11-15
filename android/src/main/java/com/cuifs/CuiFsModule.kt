package com.cuifs

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.Promise

import android.os.Environment
import android.os.Build
import android.os.AsyncTask
import android.net.Uri
import android.util.Base64

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.ByteArrayOutputStream

@ReactModule(name = CuiFsModule.NAME)
class CuiFsModule(reactContext: ReactApplicationContext) :
  NativeCuiFsSpec(reactContext) {

  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  override fun getTypedExportedConstants(): Map<String, Any?> {
      val constants: MutableMap<String, Any?> = HashMap()
      constants["DocumentDirectory"] = 0
      constants["DocumentDirectoryPath"] = this.reactApplicationContext.filesDir.absolutePath
      constants["TemporaryDirectoryPath"] = this.reactApplicationContext.cacheDir.absolutePath
      constants["PicturesDirectoryPath"] = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
      constants["CachesDirectoryPath"] = this.reactApplicationContext.cacheDir.absolutePath
      constants["DownloadDirectoryPath"] = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
      constants["FileTypeRegular"] = 0
      constants["FileTypeDirectory"] = 1
      constants["ExternalStorageDirectoryPath"] = Environment.getExternalStorageDirectory()?.absolutePath
      constants["ExternalDirectoryPath"] = this.reactApplicationContext.getExternalFilesDir(null)?.absolutePath
      constants["ExternalCachesDirectoryPath"] = this.reactApplicationContext.externalCacheDir?.absolutePath
      return constants
  }
  
  @ReactMethod
  override fun copyFileRes(filename: String, destination: String, promise: Promise) {
      try {
          val res = getResIdentifier(filename)
          val `in`: InputStream = reactApplicationContext.resources.openRawResource(res)
          copyInputStream(`in`, filename, destination, promise)
      } catch (e: Exception) {
          reject(promise, filename, Exception(String.format("Res '%s' could not be opened", filename)))
      }
  }

  @ReactMethod
  override fun copyFile(filepath: String?, destPath: String?, options: ReadableMap?, promise: Promise) {
      object : CopyFileTask() {
          @Deprecated("Deprecated in Java")
          override fun onPostExecute(ex: Exception?) {
              if (ex == null) {
                  promise.resolve(null)
              } else {
                  ex.printStackTrace()
                  reject(promise, filepath, ex)
              }
          }
      }.execute(filepath, destPath)
  }

  @ReactMethod
  override fun writeFile(filepath: String, base64Content: String?, options: ReadableMap?, promise: Promise) {
      try {
          getOutputStream(filepath, false).use { outputStream ->
              val bytes = Base64.decode(base64Content, Base64.DEFAULT)
              outputStream.write(bytes)
          }
          // BEWARE: Must be outside the block above to be resolved after
          // the output stream is closed.
          promise.resolve(null)
      } catch (ex: Exception) {
          ex.printStackTrace()
          reject(promise, filepath, ex)
      }
  }

  private fun copyInputStream(input: InputStream, source: String, destination: String, promise: Promise) {
      try {
          copyInputStream(input, destination)
          promise.resolve(null)
      } catch (ex: Exception) {
          reject(promise, source, Exception(String.format("Failed to copy '%s' to %s (%s)", source, destination, ex.localizedMessage)))
      }
  }

  /**
   * Copies given InputStream to the specified destination.
   */
  private fun copyInputStream(stream: InputStream, destination: String) {
    var output: OutputStream? = null
    try {
      output = getOutputStream(destination, false)

      // The modern Android just has a method for stream piping.
      if (Build.VERSION.SDK_INT >= 33) stream.transferTo(output)

      // For legacy systems we fallback to the original library implementation.
      else {
        val buffer = ByteArray(1024 * 10) // 10k buffer
        var read: Int
        while (stream.read(buffer).also { read = it } != -1) {
          output.write(buffer, 0, read)
        }
      }
    } finally {
      stream.close()
      output?.close()
    }
  }

  @Throws(IORejectionException::class)
  private fun getOutputStream(filepath: String, append: Boolean): OutputStream {
        val uri = getFileUri(filepath, false)
        val stream: OutputStream? = try {
            reactApplicationContext.contentResolver.openOutputStream(uri, if (append) "wa" else writeAccessByAPILevel)
        } catch (ex: FileNotFoundException) {
            throw IORejectionException("ENOENT", "ENOENT: " + ex.message + ", open '" + filepath + "'")
        }
        if (stream == null) {
            throw IORejectionException("ENOENT", "ENOENT: could not open an output stream for '$filepath'")
        }
        return stream
    }

  @Throws(IORejectionException::class)
    private fun getInputStream(filepath: String): InputStream {
        val uri = getFileUri(filepath, false)
        val stream: InputStream? = try {
            reactApplicationContext.contentResolver.openInputStream(uri)
        } catch (ex: FileNotFoundException) {
            throw IORejectionException("ENOENT", "ENOENT: " + ex.message + ", open '" + filepath + "'")
        }
        if (stream == null) {
            throw IORejectionException("ENOENT", "ENOENT: could not open an input stream for '$filepath'")
        }
        return stream
    }
  
  @Throws(IORejectionException::class)
    private fun getFileUri(filepath: String, isDirectoryAllowed: Boolean): Uri {
        var uri = Uri.parse(filepath)
        if (uri.scheme == null) {
            // No prefix, assuming that provided path is absolute path to file
            val file = File(filepath)
            if (!isDirectoryAllowed && file.isDirectory) {
                throw IORejectionException("EISDIR", "EISDIR: illegal operation on a directory, read '$filepath'")
            }
            uri = Uri.fromFile(file)
        }
        return uri
    }

  private open inner class CopyFileTask : AsyncTask<String?, Void?, Exception?>() {
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg paths: String?): Exception? {
            var `in`: InputStream? = null
            var out: OutputStream? = null
            return try {
                val filepath = paths[0]!!
                val destPath = paths[1]!!
                `in` = getInputStream(filepath)
                out = getOutputStream(destPath, false)
                val buffer = ByteArray(1024)
                var length: Int
                while (`in`.read(buffer).also { length = it } > 0) {
                    out.write(buffer, 0, length)
                    Thread.yield()
                }
                null
            } catch (ex: Exception) {
                ex
            } finally {
                `in`?.close()
                out?.close()
            }
        }
    }

  private fun getResIdentifier(filename: String): Int {
        val suffix = filename.substring(filename.lastIndexOf(".") + 1)
        val name = filename.substring(0, filename.lastIndexOf("."))
        val isImage = suffix == "png" || suffix == "jpg" || suffix == "jpeg" || suffix == "bmp" || suffix == "gif" || suffix == "webp" || suffix == "psd" || suffix == "svg" || suffix == "tiff"
        return reactApplicationContext.resources.getIdentifier(name, if (isImage) "drawable" else "raw", reactApplicationContext.packageName)
    }
  
  private fun reject(promise: Promise, filepath: String?, ex: Exception?) {
        if (ex is FileNotFoundException) {
            rejectFileNotFound(promise, filepath)
            return
        }
        if (ex is IORejectionException) {
            promise.reject(ex.code, ex.message)
            return
        }
        promise.reject("RNFS", ex!!.message)
    }
  
  private fun rejectFileNotFound(promise: Promise, filepath: String?) {
        promise.reject("ENOENT", "ENOENT: no such file or directory, open '$filepath'")
    }

  private val writeAccessByAPILevel: String
        get() = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) "w" else "rwt"

  companion object {
    const val NAME = "CuiFs"
  }
}
