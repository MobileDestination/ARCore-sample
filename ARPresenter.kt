package com.android.furnitureplace.scene.ar

import com.android.furnitureplace.core.adapter.delegate.ViewType
import com.android.furnitureplace.core.presentation.ItemView
import com.android.furnitureplace.presentation.BaseViewPresenter
import com.android.furnitureplace.scene.dialogs.holders.CategoryViewType
import com.android.furnitureplace.scene.dialogs.holders.category.CategoryView
import com.android.furnitureplace.scene.dialogs.holders.furniture.FurnitureView
import com.android.furnitureplace.scene.dialogs.holders.furniture.FurnitureViewHolder
import javax.inject.Inject

class ARPresenter @Inject constructor(
        view: ARScene.View,
        override var furnitureRepository: ARScene.FurnitureRepository
) : BaseViewPresenter<ARScene.View>(view), ARScene.Presenter {

    override fun start() {
        super.start()
        furnitureRepository.loadAllFurniture()
    }

    override fun getItemViewType(position: Int): ViewType = CategoryViewType.CATEGORY

    override fun present(view: CategoryView, position: Int) {
        (view).showCategory(furnitureRepository.data[position])
    }

    override fun present(elementView: FurnitureView, parentPosition: Int, position: Int) {
        val element = furnitureRepository.data.getValues(furnitureRepository.data[parentPosition])?.get(position)
                ?: return
        elementView.showImage(element.preview ?: "")
        elementView.showTitle(element.title ?: "")
        elementView.showType(element.category ?: "")
        elementView.showPrice(element.price.toString())
        elementView.showButtons {
            view?.show(element.title, element.sourceAndroid)
        }
    }
}