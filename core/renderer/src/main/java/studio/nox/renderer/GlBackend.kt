package studio.nox.renderer

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.util.Log
import studio.nox.model.SceneConfig
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** OpenGL ES 3 context with a GLES2-compatible shader; works on 3.0 capable phones. */
internal class GlBackend private constructor(
    private val display: EGLDisplay, private val surface: EGLSurface,
    private val context: EGLContext, private var width: Int, private var height: Int
) {
    private val buffer = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
        put(floatArrayOf(-1f,-1f, 1f,-1f, -1f,1f, 1f,1f)); position(0)
    }
    private val program: Int
    private val uniforms: Map<String,Int>

    init {
        val vertex = compile(GLES20.GL_VERTEX_SHADER,VERTEX)
        val fragment = compile(GLES20.GL_FRAGMENT_SHADER,FRAGMENT)
        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program,vertex); GLES20.glAttachShader(program,fragment)
        GLES20.glLinkProgram(program)
        val linked = IntArray(1)
        GLES20.glGetProgramiv(program,GLES20.GL_LINK_STATUS,linked,0)
        if (linked[0] == 0) throw IllegalStateException("Shader link: ${GLES20.glGetProgramInfoLog(program)}")
        GLES20.glDeleteShader(vertex); GLES20.glDeleteShader(fragment)
        uniforms = listOf("uTime","uResolution","uMode","uColorA","uColorB","uGlow","uDensity","uMotion","uTouch","uSeed")
            .associateWith { GLES20.glGetUniformLocation(program,it) }
    }

    fun resize(w: Int,h: Int) { width=w.coerceAtLeast(1);height=h.coerceAtLeast(1) }
    fun draw(config: SceneConfig, time: Float, tiltX: Float, tiltY: Float, touchX: Float, touchY: Float) {
        val c = config.normalized()
        GLES20.glViewport(0,0,width,height)
        GLES20.glClearColor(0f,0f,0f,1f); GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(program)
        GLES20.glUniform1f(uniforms.getValue("uTime"),time*c.speed)
        GLES20.glUniform2f(uniforms.getValue("uResolution"),width.toFloat(),height.toFloat())
        GLES20.glUniform1f(uniforms.getValue("uMode"),when(c.sceneId){"HALO"->1f;"NEBULA"->2f;else->0f})
        GLES20.glUniform3f(uniforms.getValue("uColorA"),((c.primary shr 16) and 255)/255f,((c.primary shr 8) and 255)/255f,(c.primary and 255)/255f)
        GLES20.glUniform3f(uniforms.getValue("uColorB"),((c.secondary shr 16) and 255)/255f,((c.secondary shr 8) and 255)/255f,(c.secondary and 255)/255f)
        GLES20.glUniform1f(uniforms.getValue("uGlow"),c.glow)
        GLES20.glUniform1f(uniforms.getValue("uDensity"),c.density)
        GLES20.glUniform2f(uniforms.getValue("uMotion"),tiltX*c.motion,tiltY*c.motion)
        GLES20.glUniform2f(uniforms.getValue("uTouch"),touchX,touchY)
        GLES20.glUniform1f(uniforms.getValue("uSeed"),c.seed.toFloat())
        val pos = GLES20.glGetAttribLocation(program,"aPosition")
        GLES20.glEnableVertexAttribArray(pos)
        buffer.position(0)
        GLES20.glVertexAttribPointer(pos,2,GLES20.GL_FLOAT,false,0,buffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4)
        GLES20.glDisableVertexAttribArray(pos)
    }
    fun swap() { if(!EGL14.eglSwapBuffers(display,surface)) Log.w("NOX","eglSwapBuffers failed: ${EGL14.eglGetError()}") }
    fun finish() {
        GLES20.glDeleteProgram(program)
        EGL14.eglMakeCurrent(display,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_CONTEXT)
        EGL14.eglDestroySurface(display,surface); EGL14.eglDestroyContext(display,context)
        EGL14.eglTerminate(display)
    }
    companion object {
        private const val EGL_OPENGL_ES3_BIT_KHR = 0x0040
        fun create(nativeSurface: Any?, w: Int, h: Int): GlBackend {
            val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            check(display != EGL14.EGL_NO_DISPLAY) { "EGL display unavailable" }
            check(EGL14.eglInitialize(display,IntArray(2),0,IntArray(2),0)) { "EGL initialize failed" }
            val attrs = intArrayOf(
                EGL14.EGL_RENDERABLE_TYPE,EGL_OPENGL_ES3_BIT_KHR,
                EGL14.EGL_SURFACE_TYPE,EGL14.EGL_WINDOW_BIT or EGL14.EGL_PBUFFER_BIT,
                EGL14.EGL_RED_SIZE,8,EGL14.EGL_GREEN_SIZE,8,EGL14.EGL_BLUE_SIZE,8,
                EGL14.EGL_ALPHA_SIZE,8,EGL14.EGL_NONE
            )
            val found = arrayOfNulls<EGLConfig>(1); val count=IntArray(1)
            check(EGL14.eglChooseConfig(display,attrs,0,found,0,1,count,0) && count[0]>0) { "GLES3 config unavailable" }
            val conf= requireNotNull(found[0]); val context=EGL14.eglCreateContext(display,conf,EGL14.EGL_NO_CONTEXT,intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION,3,EGL14.EGL_NONE),0)
            check(context != EGL14.EGL_NO_CONTEXT) { "GLES3 context unavailable" }
            val surface=if(nativeSurface!=null) EGL14.eglCreateWindowSurface(display,conf,nativeSurface,intArrayOf(EGL14.EGL_NONE),0)
                else EGL14.eglCreatePbufferSurface(display,conf,intArrayOf(EGL14.EGL_WIDTH,w,EGL14.EGL_HEIGHT,h,EGL14.EGL_NONE),0)
            check(surface != EGL14.EGL_NO_SURFACE) { "EGL surface unavailable: ${EGL14.eglGetError()}" }
            check(EGL14.eglMakeCurrent(display,surface,surface,context)) { "EGL makeCurrent failed" }
            return GlBackend(display,surface,context,w.coerceAtLeast(1),h.coerceAtLeast(1))
        }
        private fun compile(type:Int, source:String):Int {
            val shader=GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader,source);GLES20.glCompileShader(shader)
            val compiled=IntArray(1); GLES20.glGetShaderiv(shader,GLES20.GL_COMPILE_STATUS,compiled,0)
            if(compiled[0]==0) throw IllegalStateException("GLSL: ${GLES20.glGetShaderInfoLog(shader)}")
            return shader
        }
    }
}

private const val VERTEX = """
attribute vec2 aPosition;
varying vec2 vUv;
void main() { vUv = aPosition * 0.5 + 0.5; gl_Position = vec4(aPosition,0.0,1.0); }
"""

/** Three different procedural materials, designed for black AMOLED pixels. */
private const val FRAGMENT = """
precision highp float;
varying vec2 vUv;
uniform float uTime, uMode, uGlow, uDensity, uSeed;
uniform vec2 uResolution, uMotion, uTouch;
uniform vec3 uColorA, uColorB;
float hash(vec2 p) { return fract(sin(dot(p,vec2(127.1,311.7)) + uSeed * 0.01)*43758.5453); }
float lightline(float dist, float thickness) { return exp(-abs(dist)*thickness); }
void main() {
  vec2 uv = vUv;
  vec2 p = vec2((uv.x-0.5)*uResolution.x/uResolution.y, uv.y-0.5);
  p += uMotion*0.10;
  vec3 sum = vec3(0.0);
  if(uMode < 0.5) {
    // FLUX: staggered luminous ribbons, moving independently across the frame.
    for (int i=0; i<7; ++i) {
      float n=float(i);
      float shift=(n-3.0)*0.068;
      float wave=0.18*sin(p.y*5.8 + n*0.37 + uTime*0.28) + 0.055*sin(p.y*13.0 - uTime*0.41 + n);
      float d = p.x - wave - shift;
      float thin=lightline(d,310.0-90.0*uDensity);
      float halo=lightline(d,31.0)*(0.085 + uGlow*0.17);
      vec3 c=mix(uColorA,uColorB,n/6.0);
      float taper=0.3+0.7*pow(0.5+0.5*sin(p.y*4.2 + uTime*0.25 + n*0.83),2.0);
      sum += c*(thin*0.58+halo)*taper;
    }
  } else if (uMode < 1.5) {
    // HALO: depth-stacked elliptical orbits with bright corners.
    for (int i=0; i<5; ++i) {
      float n=float(i);
      float a=uTime*0.075 + n*1.21;
      mat2 rot=mat2(cos(a),-sin(a),sin(a),cos(a));
      vec2 e=rot*p;
      e.x *= 1.15+0.15*n;
      e.y *= 0.62+0.11*n;
      float radius=0.145+n*0.029;
      float dist=abs(length(e)-radius);
      float sharp=lightline(dist,340.0)*(0.58+0.2*sin(uTime*0.4+n));
      float halo=lightline(dist,33.0)*(0.07+0.13*uGlow);
      sum += mix(uColorB,uColorA,n/5.0)*(sharp+halo)*0.8;
    }
  } else {
    // NEBULA: stable star positions, soft glints and independently drifting dust.
    vec2 grid=p*vec2(17.0,17.0);
    vec2 cell=floor(grid);
    for(int x=-1; x<=1; ++x) for(int y=-1; y<=1; ++y) {
      vec2 id=cell+vec2(float(x),float(y));
      vec2 rnd=vec2(hash(id),hash(id+17.1));
      vec2 star=(id+rnd+0.08*sin(uTime*0.13+id.yx))/17.0;
      float d=length((p-star)*vec2(1.0,1.0));
      float density=step(hash(id+37.0),0.25 + uDensity*0.65);
      float twinkle=0.65+0.35*sin(uTime*0.7 + hash(id+5.0)*6.28);
      float beam=exp(-d*520.0)*twinkle;
      float glow=exp(-d*75.0)*(0.04+uGlow*0.12);
      sum+=mix(uColorA,uColorB,hash(id+11.0))*(beam+glow)*density;
    }
    float cloud=0.5+0.5*sin(p.x*12.0 + p.y*7.0 + uTime*0.17)*sin(p.y*10.0-p.x*3.0);
    sum+=uColorB*pow(cloud,5.0)*0.022*uGlow;
  }
  if(uTouch.x>=0.0) {
    vec2 tp=vec2((uTouch.x-0.5)*uResolution.x/uResolution.y,uTouch.y-0.5);
    float d=length(p-tp);
    sum += uColorA*exp(-d*25.0)*0.32;
  }
  // Preserve the deep-black backdrop instead of adding synthetic gray noise.
  gl_FragColor=vec4(min(sum,vec3(1.0)),1.0);
}
"""
