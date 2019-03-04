package com.trials.poly

import android.R.attr.configure
import android.util.Log
import com.google.ar.core.Config
import com.google.ar.core.Config.UpdateMode
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment
import com.google.a.b.a.a.a.e


class CustomArFragment : ArFragment() {


  override fun getSessionConfiguration(session: Session): Config {
    planeDiscoveryController.setInstructionView(null)
    val config = Config(session)
    config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
    session.configure(config)
    arSceneView.setupSession(session)

    if ((activity as MainActivity).setupAugmentedImagesDb(config, session)) {
      Log.d(TAG, "Success")
    } else {
      Log.e(TAG, "Fail  ure setting up db")
    }
    return config
  }

  companion object {
    val TAG = CustomArFragment::class.java.simpleName
  }

}