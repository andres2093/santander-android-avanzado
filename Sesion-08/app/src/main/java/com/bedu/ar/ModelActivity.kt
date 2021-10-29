package com.bedu.ar

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bedu.ar.databinding.ActivityModelsBinding
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class ModelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModelsBinding

    private var arFragment: ArFragment? = null
    private var modelRenderable: ModelRenderable? = null

    private val modelUrl = "https://github.com/beduExpert/Android-Avanzado-2021/raw/main/Sesion-08/Reto-02/models/Chair.glb"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityModelsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        arFragment = supportFragmentManager.findFragmentById(R.id.fragment) as ArFragment?

        setUpModel()
        setUpPlane()
    }

    private fun setUpModel() {
        ModelRenderable.builder().setSource(
            this,
            RenderableSource.builder()
                .setSource(this, Uri.parse(modelUrl), RenderableSource.SourceType.GLB)
                .setScale(0.75f).setRecenterMode(RenderableSource.RecenterMode.ROOT).build()
        )
            .setRegistryId(modelUrl)
            .build()
            .thenAccept { renderable: ModelRenderable? ->
                modelRenderable = renderable
            }
            .exceptionally {
                Log.i("Model", "cant load")
                Toast.makeText(this@ModelActivity, "Model can't be Loaded", Toast.LENGTH_SHORT)
                    .show()
                null
            }
    }

    private fun setUpPlane() {
        arFragment!!.setOnTapArPlaneListener { hitResult: HitResult, _: Plane?, _: MotionEvent? ->
            val anchor = hitResult.createAnchor()
            val anchorNode =
                AnchorNode(anchor)
            anchorNode.setParent(arFragment!!.arSceneView.scene)
            createModel(anchorNode)
        }
    }

    private fun createModel(anchorNode: AnchorNode) {
        val node = TransformableNode(arFragment!!.transformationSystem)
        node.setParent(anchorNode)
        node.renderable = modelRenderable
        node.select()
    }
}
