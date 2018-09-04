package com.android.furnitureplace.scene.ar


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.Toast
import com.android.furnitureplace.R
import com.android.furnitureplace.core.di.ModulesInstallable
import com.android.furnitureplace.core.fragment.BaseFragment
import com.android.furnitureplace.core.presentation.Presentable
import com.android.furnitureplace.database.FireBaseData
import com.android.furnitureplace.di.ARModule
import com.android.furnitureplace.scene.dialogs.FurnitureBottomSheet
import com.android.furnitureplace.utils.ScreenshotUtils
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.fragment_ar.*
import toothpick.Scope
import java.io.File
import javax.inject.Inject

private const val PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: Int = 14

class ARFragment : BaseFragment(), Presentable<ARScene.View, ARScene.Presenter>,
        ModulesInstallable, ARScene.View, View.OnClickListener {

    @Inject
    override lateinit var presenter: ARScene.Presenter
    private var arFragmentAr: ArFragment? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_ar, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arFragmentAr = childFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment
        addFurniture.setOnClickListener(this)
        share.setOnClickListener(this)
    }

    override fun installModules(scope: Scope) {
        scope.installModules(ARModule(this))
    }

    private val dialog = FurnitureBottomSheet()

    override fun onClick(view: View?) =
            when (view) {
                addFurniture -> {
                    dialog.setTargetFragment(this, 0)
                    dialog.show(fragmentManager, dialog.tag)
                }
                share -> {
                    checkPermission()
                }
                else -> Unit
            }

    override fun show(title: String?, sourceAndroid: String?) {
        dialog.dismissAllowingStateLoss()
        FireBaseData.downloadFile(File(context?.filesDir, "${title
                ?: ""}.sfb"), sourceAndroid ?: "") {
            ModelRenderable.builder()
                    .setSource(context, Uri.parse(File(context?.filesDir, "${title
                            ?: ""}.sfb").absolutePath))
                    .build()
                    .thenAccept { renderable ->
                        addModelToScene(renderable)
                    }
                    .exceptionally { _ ->
                        val toast = Toast.makeText(context, "Unable to load andy renderable", Toast.LENGTH_LONG)
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.show()
                        null
                    }
        }
    }

    private fun addModelToScene(renderable: ModelRenderable?) {
        arFragmentAr?.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
            if (renderable != null) {
                val anchor = hitResult.createAnchor()
                val anchorNode = AnchorNode(anchor)
                anchorNode.setParent(arFragmentAr?.arSceneView?.scene)
                val andy = TransformableNode(arFragmentAr?.transformationSystem)
                andy.setParent(anchorNode)
                andy.renderable = renderable
                andy.scaleController.isEnabled = false
                andy.select()
            }
        }
    }

    private fun checkPermission() {
        activity?.let {
            if (ContextCompat.checkSelfPermission(it,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(it,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                } else {
                    ActivityCompat.requestPermissions(it,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
                }
            } else {
                shareFile()
            }
        }
    }

    private fun shareFile() {
        ScreenshotUtils.createScreenshot(arFragmentAr) { path ->
            Intent().run {
                action = Intent.ACTION_SEND
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
                type = "image/*"
                startActivity(Intent.createChooser(this, "Share images..."))
            }
            File(path).deleteOnExit()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    shareFile()
                }
                return
            }
        }
    }

}
