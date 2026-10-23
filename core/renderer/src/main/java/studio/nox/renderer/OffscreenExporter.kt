package studio.nox.renderer

import android.graphics.Bitmap
import android.opengl.GLES20
import studio.nox.model.SceneConfig
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Must run off UI thread; uses a private GLES/EGL pbuffer and releases it. */
object OffscreenExporter {
    fun render(config:SceneConfig,width:Int,height:Int):Bitmap {
        require(width in 1..4096 && height in 1..4096 && width.toLong()*height<=14_000_000) { "Invalid PNG dimensions" }
        val gl=GlBackend.create(null,width,height)
        try {
            gl.draw(config,0f,0f,0f,-1f,-1f)
            val bytes=ByteBuffer.allocateDirect(width*height*4).order(ByteOrder.nativeOrder())
            GLES20.glReadPixels(0,0,width,height,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE,bytes)
            check(GLES20.glGetError()==GLES20.GL_NO_ERROR) { "Pixel export failed" }
            bytes.rewind()
            val pixels=IntArray(width*height)
            for(y in 0 until height) for(x in 0 until width) {
                val r=bytes.get().toInt() and 255
                val g=bytes.get().toInt() and 255
                val b=bytes.get().toInt() and 255
                val a=bytes.get().toInt() and 255
                pixels[(height-1-y)*width+x]=(a shl 24) or (r shl 16) or (g shl 8) or b
            }
            return Bitmap.createBitmap(pixels,width,height,Bitmap.Config.ARGB_8888)
        } finally { gl.finish() }
    }
}
