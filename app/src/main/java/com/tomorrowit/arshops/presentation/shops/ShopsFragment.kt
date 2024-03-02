package com.tomorrowit.arshops.presentation.shops

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.LatLng
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.gorisse.thomas.sceneform.scene.await
import com.tomorrowit.arshops.R
import com.tomorrowit.arshops.ar.LocationMarker
import com.tomorrowit.arshops.ar.LocationScene
import com.tomorrowit.arshops.ar.rendering.LocationNodeRender
import com.tomorrowit.arshops.databinding.FragmentShopsBinding
import com.tomorrowit.arshops.model.NearbyStore
import com.tomorrowit.arshops.model.PlaceNode
import com.tomorrowit.arshops.model.getPositionVector
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private val TAG: String = ShopsFragment::class.java.simpleName

@AndroidEntryPoint
class ShopsFragment : Fragment() {
    private lateinit var binding: FragmentShopsBinding
    private val viewModel: ShopsViewModel by activityViewModels()

    //region AR RELATED
    private lateinit var arFragment: ArFragment
    private val arSceneView get() = arFragment.arSceneView
    private val scene get() = arSceneView.scene
    private var modelView: ViewRenderable? = null
    //endregion

    private var anchorNode: AnchorNode? = null

    private var places: List<NearbyStore>? = null

    //private var currentLocation: LatLng? = null
    private var currentLat: Double = 0.00
    private var currentLng: Double = 0.00

    //region AR Location library
    private var hasFinishedLoading = false
    private var locationScene: LocationScene? = null
    //private var exampleLayoutRenderable: ViewRenderable? = null
    //endregion

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentShopsBinding.inflate(inflater, container, false)
        .apply { binding = this }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arFragment = (binding.arFragment.getFragment() as ArFragment).apply {
            setOnSessionConfigurationListener { session, config ->
                // Modify the AR session configuration here
                //config.planeFindingMode = Config.PlaneFindingMode.DISABLED
                //config.geospatialMode = Config.GeospatialMode.ENABLED
            }
            //setOnTapArPlaneListener(::onTapPlane)
        }

        lifecycleScope.launch(Dispatchers.Main) {
            loadModels()

            scene.addOnUpdateListener { frameTime ->
                if (!hasFinishedLoading) {
                    return@addOnUpdateListener
                }
                if (locationScene == null) {
                    // If our locationScene object hasn't been setup yet, this is a good time to do it
                    // We know that here, the AR components have been initiated.
                    locationScene =
                        LocationScene(this@ShopsFragment.requireActivity(), arSceneView)

                    places?.forEach {
                        val layoutLocationMarker = LocationMarker(
                            it.locationlng.toDouble(), it.locationlat.toDouble(), getExampleView()
                        )

                        // An example "onRender" event, called every frame
                        // Updates the layout with the markers distance
                        layoutLocationMarker.renderEvent =
                            LocationNodeRender { node ->
                                val eView: View = modelView!!.getView()
                                eView.findViewById<TextView>(R.id.item_ar_shop_title).text = it.name
                                eView.findViewById<TextView>(R.id.item_ar_shop_desc).text = it.type
                                eView.findViewById<TextView>(R.id.item_ar_shop_distance)?.text =
                                    node?.distance.toString() + "M"
                            }
                        // Adding the marker
                        locationScene!!.mLocationMarkers.add(layoutLocationMarker)
                    }
                }
                val frame: Frame = arSceneView?.getArFrame() ?: return@addOnUpdateListener
                if (frame.getCamera().getTrackingState() !== TrackingState.TRACKING) {
                    return@addOnUpdateListener
                }
                if (locationScene != null) {
                    locationScene!!.processFrame(frame)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        observeState()
    }

    override fun onResume() {
        super.onResume()
        if (locationScene != null) {
            locationScene!!.resume()
        }
        try {
            arSceneView.resume()
        } catch (ex: CameraNotAvailableException) {
            //DemoUtils.displayError(this, "Unable to get camera", ex)
            //finish()
            return
        }
        if (arSceneView?.getSession() != null) {
            //showLoadingMessage()
        }
    }


    override fun onPause() {
        super.onPause()
        if (locationScene != null) {
            locationScene!!.pause()
        }
        arSceneView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        arSceneView.destroy()
    }

    private fun observeState() {
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when (state) {
                ShopState.Init -> {

                }

                is ShopState.ErrorShops -> {
                    Toast.makeText(
                        this@ShopsFragment.requireContext(),
                        "Failure loading locations",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is ShopState.SuccessShops -> {
                    places = state.list
                    Toast.makeText(
                        this@ShopsFragment.requireContext(),
                        "Success loading locations",
                        Toast.LENGTH_SHORT
                    ).show()
                    hasFinishedLoading = true
                }

                is ShopState.UpdateLocation -> {
                    currentLat = state.lat
                    currentLng = state.lng
                    Toast.makeText(
                        this@ShopsFragment.requireContext(),
                        "Location updated: ${state.lat} ${state.lng}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.i(
                        TAG,
                        "Location updated: ${state.lat} ${state.lng}"
                    )
                }
            }
        }.launchIn(lifecycleScope)
    }

    private suspend fun loadModels() {
        modelView = ViewRenderable.builder()
            .setView(context, R.layout.item_ar_shop)
            .await()
    }

    private fun getExampleView(): Node {
        val base = Node()
        base.setRenderable(modelView)
        modelView?.view?.setOnClickListener {
            Toast.makeText(
                this@ShopsFragment.requireActivity(),
                "Location marker touched.", Toast.LENGTH_SHORT
            ).show()
            false
        }
        return base
    }

    private fun onTapPlane(hitResult: HitResult, plane: Plane, motionEvent: MotionEvent) {
        val anchor = hitResult.createAnchor()
        anchorNode = AnchorNode(anchor)
        anchorNode?.parent = scene
        addPlaces(anchorNode!!)
    }

    private fun addPlaces(anchorNode: AnchorNode) {
        val rotationMatrix = (requireActivity() as ShopsActivity).rotationMatrix
        val orientationAngles = (requireActivity() as ShopsActivity).orientationAngles
        val sensorManager = (requireActivity() as ShopsActivity).sensorManager

        if (places != null) {
            for (place in places!!) {
                val placeNode = PlaceNode(this.requireContext(), place)
                placeNode.setParent(anchorNode)
                placeNode.localPosition =
                    place.getPositionVector(orientationAngles[0], LatLng(currentLat, currentLng))
                placeNode.setOnTapListener { _, _ ->
                    Toast.makeText(
                        this@ShopsFragment.requireContext(),
                        "Clicked on item",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}