package com.tomorrowit.arshops.model

import android.content.Context
import android.widget.TextView
import com.google.android.material.imageview.ShapeableImageView
import com.google.ar.sceneform.rendering.ViewRenderable
import com.tomorrowit.arshops.R

class PlaceNode(
    val context: Context,
    val nearbyStore: NearbyStore?
) : com.google.ar.sceneform.Node() {

    private var placeRenderable: ViewRenderable? = null

    override fun onActivate() {
        super.onActivate()

        if (scene == null) {
            return
        }

        if (placeRenderable != null) {
            return
        }

        ViewRenderable.builder()
            .setView(context, R.layout.item_ar_shop)
            .build()
            .thenAccept { renderable ->
                setRenderable(renderable)
                placeRenderable = renderable

                nearbyStore?.let {
                    ViewHandler.loadWithGlide(
                        context,
                        it.photo_reference,
                        renderable.view.findViewById<ShapeableImageView>(R.id.item_ar_shop_picture)
                    )
                    renderable.view.findViewById<TextView>(R.id.item_ar_shop_title).text = it.name
                    renderable.view.findViewById<TextView>(R.id.item_ar_shop_desc).text = it.type
                    renderable.view.findViewById<TextView>(R.id.item_ar_shop_distance).text =
                        it.distance.toString()
                }
            }
    }
}