package com.trials.poly

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.a.b.a.a.a.e
import android.content.Context.ACTIVITY_SERVICE
import android.support.v4.content.ContextCompat.getSystemService
import android.app.ActivityManager
import android.os.Build
import android.app.Activity
import android.content.Context
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.Gravity
import com.google.ar.sceneform.rendering.ModelRenderable
import android.support.v4.view.accessibility.AccessibilityRecordCompat.setSource
import android.net.Uri
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import android.view.MotionEvent
import com.google.ar.sceneform.AnchorNode
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import com.google.ar.core.*
import java.io.IOException
import com.google.ar.core.TrackingState
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.FrameTime
import android.support.annotation.RequiresApi
import com.google.ar.sceneform.rendering.Renderable


class MainActivity : AppCompatActivity() {

  private val MIN_OPENGL_VERSION = 3.0
  private var arFragment: ArFragment? = null
  private var shouldAddModel = true


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    if (checkIsSupportedDeviceOrFinish(this)) {
      arFragment = supportFragmentManager?.findFragmentById(R.id.sceneform_fragment) as CustomArFragment
      arFragment?.planeDiscoveryController?.hide()
      arFragment?.arSceneView?.scene?.addOnUpdateListener(this::onUpdateFrame)
    }
  }

  // The SDK requires Android API level 27 or newer and OpenGL ES version 3.0 or newer
  private fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
      Log.e(TAG, "SceneForm requires Android N or later")
      Toast.makeText(activity, "SceneForm requires Android N or later", Toast.LENGTH_LONG).show()
      activity.finish()
      return false
    }
    val openGlVersionString = (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
      .deviceConfigurationInfo
      .glEsVersion
    if (java.lang.Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
      Log.e(TAG, "SceneForm requires OpenGL ES 3.0 later")
      Toast.makeText(activity, "SceneForm requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
        .show()
      activity.finish()
      return false
    }
    return true
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  private fun onUpdateFrame(frameTime: FrameTime) {
    val frame = arFragment?.arSceneView?.arFrame
    val augmentedImages = frame?.getUpdatedTrackables(AugmentedImage::class.java) ?: return
    for (augmentedImage in augmentedImages) {
      if (augmentedImage.trackingState == TrackingState.TRACKING) {
        if (augmentedImage.name == "tiger" && shouldAddModel) {
          placeObject(
            arFragment,
            augmentedImage.createAnchor(augmentedImage.centerPose),
            Uri.parse("Mesh_BengalTiger.sfb")
          )
          shouldAddModel = false
        }
      }
    }
  }

  private fun loadAugmentedImage(): Bitmap? {
    try {
      assets.open("LampPost_Main.png").use { `is` -> return BitmapFactory.decodeStream(`is`) }
    } catch (e: IOException) {
      Log.e(TAG, "IO Exception", e)
    }
    return null
  }

  /**
  first initialize our database for
  this session and then add an image to this database.
   */
  fun setupAugmentedImagesDb(config: Config, session: Session): Boolean {
    val augmentedImageDatabase = AugmentedImageDatabase(session)
    val bitmap = loadAugmentedImage() ?: return false
    augmentedImageDatabase.addImage("tiger", bitmap)
    config.augmentedImageDatabase = augmentedImageDatabase
    return true
  }

  /**
  This method is used to build a renderable from the provided Uri.
  Once the renderable is built,
  it is passed into addNodeToScene method
  where the renderable is attached
  to a node and that node is placed onto the scene.
   */
  @RequiresApi(api = Build.VERSION_CODES.N)
  private fun placeObject(arFragment: ArFragment?, anchor: Anchor, uri: Uri) {
    ModelRenderable.builder()
      .setSource(arFragment?.context, uri)
      .build()
      .thenAccept { model ->
        addNodeToScene(arFragment, anchor, model)
      }.exceptionally {
        Toast.makeText(arFragment?.context, "Error:" + it.message, Toast.LENGTH_LONG).show()
        return@exceptionally null
      }
  }

  /**
  This method creates an AnchorNode from the received anchor,
  creates another node on which the renderable is attached,
  then adds this node to the AnchorNode and adds the AnchorNode to the scene.
   */
  private fun addNodeToScene(arFragment: ArFragment?, anchor: Anchor, renderable: Renderable) {
    val anchorNode = AnchorNode(anchor)
    val node = TransformableNode(arFragment?.transformationSystem)
    node.renderable = renderable
    node.setParent(anchorNode)
    arFragment?.arSceneView?.scene?.addChild(anchorNode)
    node.select()
  }

  companion object {
    private val TAG = MainActivity::class.java.simpleName

  }
}
