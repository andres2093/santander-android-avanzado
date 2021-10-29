package com.bedu.ar

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bedu.ar.common.helpers.CameraPermissionHelper
import com.bedu.ar.common.helpers.DisplayRotationHelper
import com.bedu.ar.common.rendering.BackgroundRenderer
import com.bedu.ar.common.rendering.ObjectRenderer
import com.bedu.ar.databinding.ActivityFaceBinding
import com.google.android.material.snackbar.Snackbar
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import java.io.IOException
import java.util.*
import javax.microedition.khronos.opengles.GL10

class FaceActivity : AppCompatActivity(), GLSurfaceView.Renderer {

    private lateinit var binding: ActivityFaceBinding

    private lateinit var surfaceView: GLSurfaceView

    private var installRequested = false
    private var session: Session? = null
    private var displayRotationHelper: DisplayRotationHelper? = null

    private val backgroundRenderer: BackgroundRenderer = BackgroundRenderer()
    private val augmentedFaceRenderer: AugmentedFaceRenderer = AugmentedFaceRenderer()
    private val noseObject: ObjectRenderer = ObjectRenderer()
    private val rightEarObject: ObjectRenderer = ObjectRenderer()
    private val leftEarObject: ObjectRenderer = ObjectRenderer()

    private val noseMatrix = FloatArray(16)
    private val rightEarMatrix = FloatArray(16)
    private val leftEarMatrix = FloatArray(16)

    private var freckles = "models/fox/freckles.png"
    private var noseFur = "models/fox/nose_fur.png"
    private var earFur = "models/fox/ear_fur.png"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFaceBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnFox.setOnClickListener {
            earFur = "models/fox/ear_fur.png"
            freckles = "models/fox/freckles.png"
            noseFur = "models/fox/nose_fur.png"

            surfaceView.onPause()
            surfaceView.onResume()
        }
        binding.btnPig.setOnClickListener {
            earFur = "models/pig/ear_fur.png"
            freckles = "models/pig/freckles.png"
            noseFur = "models/pig/nose_fur.png"

            surfaceView.onPause()
            surfaceView.onResume()
        }

        surfaceView = binding.surfaceview
        displayRotationHelper = DisplayRotationHelper( /*context=*/this)

        // Set up renderer.
        surfaceView.preserveEGLContextOnPause = true
        surfaceView.setEGLContextClientVersion(2)
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0) // Alpha used for plane blending.
        surfaceView.setRenderer(this)
        surfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        surfaceView.setWillNotDraw(false)
        installRequested = false
    }

    override fun onDestroy() {
        if (session != null) {
            session!!.close()
            session = null
        }
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        if (session == null) {
            var exception: Exception? = null
            var message: String? = null
            try {
                when (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        installRequested = true
                        return
                    }
                    ArCoreApk.InstallStatus.INSTALLED -> {
                    }
                }

                if (!CameraPermissionHelper.hasCameraPermission(this)) {
                    CameraPermissionHelper.requestCameraPermission(this)
                    return
                }

                session = Session( /* context= */this, EnumSet.noneOf(
                    Session.Feature::class.java
                )
                )
                val cameraConfigFilter = CameraConfigFilter(session)
                cameraConfigFilter.facingDirection = CameraConfig.FacingDirection.FRONT
                val cameraConfigs = session!!.getSupportedCameraConfigs(cameraConfigFilter)
                if (cameraConfigs.isNotEmpty()) {
                    session!!.cameraConfig = cameraConfigs[0]
                } else {
                    message = "This device does not have a front-facing (selfie) camera"
                    exception = UnavailableDeviceNotCompatibleException(message)
                }
                configureSession()
            } catch (e: UnavailableArcoreNotInstalledException) {
                message = "Please install ARCore"
                exception = e
            } catch (e: UnavailableUserDeclinedInstallationException) {
                message = "Please install ARCore"
                exception = e
            } catch (e: UnavailableApkTooOldException) {
                message = "Please update ARCore"
                exception = e
            } catch (e: UnavailableSdkTooOldException) {
                message = "Please update this app"
                exception = e
            } catch (e: UnavailableDeviceNotCompatibleException) {
                message = "This device does not support AR"
                exception = e
            } catch (e: Exception) {
                message = "Failed to create AR session"
                exception = e
            }
            if (message != null) {
                Snackbar.make(surfaceView, message, Snackbar.LENGTH_SHORT).show()
                Log.e(TAG, "Exception creating session", exception)
                return
            }
        }

        try {
            session!!.resume()
        } catch (e: CameraNotAvailableException) {
            Snackbar.make(
                surfaceView,
                "Camera not available. Try restarting the app.",
                Snackbar.LENGTH_SHORT
            ).show()
            session = null
            return
        }
        surfaceView.onResume()
        displayRotationHelper?.onResume()
    }

    public override fun onPause() {
        super.onPause()
        if (session != null) {
            displayRotationHelper?.onPause()
            surfaceView.onPause()
            session!!.pause()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        results: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(
                this,
                "Camera permission is needed to run this application",
                Toast.LENGTH_LONG
            )
                .show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                CameraPermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
        setModel()
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        displayRotationHelper?.onSurfaceChanged(width, height)
        GLES20.glViewport(0, 0, width, height)
        setModel()
    }

    override fun onDrawFrame(gl: GL10) {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        if (session == null) {
            return
        }
        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper?.updateSessionIfNeeded(session!!)
        try {
            session!!.setCameraTextureName(backgroundRenderer.textureId)

            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.
            val frame = session!!.update()
            val camera = frame.camera

            // Get projection matrix.
            val projectionMatrix = FloatArray(16)
            camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100.0f)

            // Get camera matrix and draw.
            val viewMatrix = FloatArray(16)
            camera.getViewMatrix(viewMatrix, 0)

            // Compute lighting from average intensity of the image.
            // The first three components are color scaling factors.
            // The last one is the average pixel intensity in gamma space.
            val colorCorrectionRgba = FloatArray(4)
            frame.lightEstimate.getColorCorrection(colorCorrectionRgba, 0)

            // If frame is ready, render camera preview image to the GL surface.
            backgroundRenderer.draw(frame)

            // ARCore's face detection works best on upright faces, relative to gravity.
            // If the device cannot determine a screen side aligned with gravity, face
            // detection may not work optimally.
            val faces = session!!.getAllTrackables(
                AugmentedFace::class.java
            )
            for (face in faces) {
                if (face.trackingState != TrackingState.TRACKING) {
                    break
                }
                val scaleFactor = 1.0f

                // Face objects use transparency so they must be rendered back to front without depth write.
                GLES20.glDepthMask(false)

                // Each face's region poses, mesh vertices, and mesh normals are updated every frame.

                // 1. Render the face mesh first, behind any 3D objects attached to the face regions.
                val modelMatrix = FloatArray(16)
                face.centerPose.toMatrix(modelMatrix, 0)
                augmentedFaceRenderer.draw(
                    projectionMatrix, viewMatrix, modelMatrix, colorCorrectionRgba, face
                )

                // 2. Next, render the 3D objects attached to the forehead.
                face.getRegionPose(AugmentedFace.RegionType.FOREHEAD_RIGHT).toMatrix(rightEarMatrix, 0)
                rightEarObject.updateModelMatrix(rightEarMatrix, scaleFactor)
                rightEarObject.draw(
                    viewMatrix,
                    projectionMatrix,
                    colorCorrectionRgba,
                    DEFAULT_COLOR
                )
                face.getRegionPose(AugmentedFace.RegionType.FOREHEAD_LEFT).toMatrix(leftEarMatrix, 0)
                leftEarObject.updateModelMatrix(leftEarMatrix, scaleFactor)
                leftEarObject.draw(viewMatrix, projectionMatrix, colorCorrectionRgba, DEFAULT_COLOR)

                // 3. Render the nose last so that it is not occluded by face mesh or by 3D objects attached
                // to the forehead regions.
                face.getRegionPose(AugmentedFace.RegionType.NOSE_TIP).toMatrix(noseMatrix, 0)
                noseObject.updateModelMatrix(noseMatrix, scaleFactor)
                noseObject.draw(viewMatrix, projectionMatrix, colorCorrectionRgba, DEFAULT_COLOR)
            }
        } catch (t: Throwable) {
            // Avoid crashing the application due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread", t)
        } finally {
            GLES20.glDepthMask(true)
        }
    }

    private fun setModel() {
        // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
        try {
            // Create the texture and pass it to ARCore session to be filled during update().
            backgroundRenderer.createOnGlThread( /*context=*/this)
            augmentedFaceRenderer.createOnGlThread(this, freckles)
            augmentedFaceRenderer.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f)
            noseObject.createOnGlThread( /*context=*/this, "models/nose.obj", noseFur)
            noseObject.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f)
            noseObject.setBlendMode(ObjectRenderer.BlendMode.AlphaBlending)
            rightEarObject.createOnGlThread(this, "models/forehead_right.obj", earFur)
            rightEarObject.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f)
            rightEarObject.setBlendMode(ObjectRenderer.BlendMode.AlphaBlending)
            leftEarObject.createOnGlThread(this, "models/forehead_left.obj", earFur)
            leftEarObject.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f)
            leftEarObject.setBlendMode(ObjectRenderer.BlendMode.AlphaBlending)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read an asset file", e)
        }
    }

    private fun configureSession() {
        val config = Config(session)
        config.augmentedFaceMode = Config.AugmentedFaceMode.MESH3D
        session!!.configure(config)
    }

    companion object {
        private val TAG = FaceActivity::class.java.simpleName
        private val DEFAULT_COLOR = floatArrayOf(0f, 0f, 0f, 0f)
    }

}
