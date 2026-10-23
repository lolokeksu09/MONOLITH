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
        GLES20.glUniform1f(uniforms.getValue("uMode"),when(c.sceneId){"FLUX"->0f;"HALO"->1f;"NEBULA"->2f;"AURORA"->3f;"PULSE"->4f;"COMET"->5f;else->0f})
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
// Bounded, trig-free lattice hash: avoids huge sin() inputs that can vary on mobile GPUs.
float hash(vec2 p) {
  vec3 q=fract(vec3(p.xyx)*0.1031+fract(uSeed*0.00013));
  q+=dot(q,q.yzx+33.33);
  return fract((q.x+q.y)*q.z);
}
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
  } else if (uMode < 2.5) {
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
  } else if (uMode < 3.5) {
    // AURORA: slow luminous curtains, dark OLED background and soft depth.
    for(int i=0; i<5; ++i) {
      float n=float(i);
      float phase=uTime*0.12 + n*1.12 + uSeed*0.003;
      float ribbon=(n-2.0)*0.11 +
        0.13*sin(p.y*3.3 + phase) + 0.045*sin(p.y*8.8-phase*0.63);
      float d=abs(p.x-ribbon);
      float fine=exp(-d*(52.0 + uDensity*30.0));
      float veil=exp(-d*(13.0 + uDensity*12.0));
      float folding=0.50+0.50*sin(p.y*5.0 - phase*0.37);
      float vertical=smoothstep(-0.57,-0.27,p.y) *
                     (1.0-smoothstep(0.28,0.61,p.y));
      vec3 c=mix(uColorA,uColorB,n/4.0);
      sum+=c*(fine*(0.095+0.25*uGlow)+veil*(0.014+0.052*uGlow))*
        (0.35+0.65*folding)*vertical;
    }
  } else if (uMode < 4.5) {
    // PULSE: continuously moving, non-strobing energy rings.
    vec2 centre=vec2(0.0,0.02);
    float radial=length((p-centre)*vec2(1.03,1.0));
    for(int i=0;i<6;++i) {
      float n=float(i);
      float radius=0.055+n*(0.066+uDensity*0.008);
      radius+=0.018*sin(uTime*0.26-n*0.72+uSeed*0.009);
      float delta=abs(radial-radius);
      float core=exp(-delta*(255.0-uDensity*65.0));
      float halo=exp(-delta*28.0);
      float envelope=exp(-radius*0.86)*(0.76+0.24*cos(uTime*0.22-n*0.4));
      vec3 c=mix(uColorA,uColorB,n/5.0);
      sum+=c*(core*(0.26+uGlow*0.34)+halo*(0.017+uGlow*0.065))*envelope;
    }
  } else {
    // COMET: deterministic orbital traces with naturally fading tails.
    vec2 direction=normalize(vec2(0.84,-0.54));
    vec2 perp=vec2(-direction.y,direction.x);
    for(int i=0;i<7;++i) {
      float n=float(i);
      float randA=hash(vec2(n+2.0,18.0));
      float randB=hash(vec2(5.0,n+7.0));
      float phase=fract(randA+uTime*(0.024+0.006*randB));
      float travel=(phase*2.0-1.0)*1.5;
      float transverse=(randB-0.5)*(0.90+0.16*uDensity);
      vec2 head=direction*travel+perp*transverse;
      vec2 difference=p-head;
      float along=dot(difference,direction);
      float perpendicularDistance=abs(dot(difference,perp));
      float width=0.0017+0.0026*uDensity;
      float streak=exp(-perpendicularDistance*perpendicularDistance/(width*width));
      float behind=(1.0-smoothstep(-0.006,0.009,along));
      float tail=exp(min(0.0,along)*(22.0-13.0*uDensity));
      float headGlow=exp(-length(difference)*175.0);
      float halo=exp(-length(difference)*34.0);
      vec3 c=mix(uColorA,uColorB,n/6.0);
      sum+=c*(streak*behind*tail*(0.16+0.28*uGlow) +
        headGlow*(0.46+0.40*uGlow)+halo*0.035*uGlow);
    }
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
