package com.android.furnitureplace.scene.ar

import com.android.furnitureplace.core.common.MultiMap
import com.android.furnitureplace.database.FireBaseData
import com.android.furnitureplace.model.ItemStore
import java.io.File
import java.util.concurrent.Executor
import javax.inject.Inject

class FurnitureRepository @Inject constructor() : ARScene.FurnitureRepository {

    override val data: MultiMap<String, ItemStore> = MultiMap()

    override var hasRecovery: Boolean = true
    override fun loadAllFurniture() {
        FireBaseData.getData {
            data.addAll(it)
        }
    }

    override fun downloadModel(file: File, reference: String, callback: (path: String) -> Unit) {
        FireBaseData.downloadFile(file, reference, callback)
    }

    override fun getItemCount(parentPosition: Int): Int =
            data.getValues(data[parentPosition])?.size ?: 0


    override fun getItemTop(parentPosition: Int): Int {
        data.getValues(data[parentPosition])?.forEachIndexed { index, _ ->
            return index
        }
        return 0
    }

}
