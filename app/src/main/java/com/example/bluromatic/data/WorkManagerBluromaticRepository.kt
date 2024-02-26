/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bluromatic.data

import android.content.Context
import android.net.Uri
import androidx.lifecycle.asFlow
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.bluromatic.IMAGE_MANIPULATION_WORK_NAME
import com.example.bluromatic.KEY_BLUR_LEVEL
import com.example.bluromatic.KEY_IMAGE_URI
import com.example.bluromatic.TAG_OUTPUT
import com.example.bluromatic.getImageUri
import com.example.bluromatic.workers.BlurWorker
import com.example.bluromatic.workers.CleanupWorker
import com.example.bluromatic.workers.SaveImageToFileWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapNotNull

class WorkManagerBluromaticRepository(context: Context) : BluromaticRepository {

    /**
     * Crear una variable llamada workManager y  llamamos a
     *WorkaManger.getInstance(context) para almacenar una isntacia de WorkManager
     */
    private val workManager = WorkManager.getInstance(context)
    //nuevo creamos una nueva variable lamdad imageUri la propagamos medianter getImageUri()
    private var imageUri: Uri = context.getImageUri() //


   // override val outputWorkInfo1: Flow<WorkInfo?> = MutableStateFlow(null)

    override  val outputWorkInfo: Flow<WorkInfo> =
        workManager.getWorkInfosByTagLiveData(TAG_OUTPUT).asFlow().mapNotNull {
            if(it.isNotEmpty())it.first() else null
        }

    /**
     * Create the WorkRequests to apply the blur and save the resulting image
     * @param blurLevel The amount to blur the image
     */
    override fun applyBlur(blurLevel: Int) {
        /**
         * para propagar una varible nueva llmada blurBuilder , crea una OneTimeWorkRequest
         * para el Worker de desenfoque y llama  a la funcion de extencion  OneTimeWorkRequestBuilder
         * desde WorkManager KTX
         */
        //

        //sustituir beginWith por beginUniqueWork
        var continuation = workManager.beginUniqueWork(
            IMAGE_MANIPULATION_WORK_NAME,ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequest.from(CleanupWorker::class.java))

       // var continuation2 = workManager.beginWith(OneTimeWorkRequest.from(CleanupWorker::class.java))

        val blurBuilder = OneTimeWorkRequestBuilder<BlurWorker>()

        //nuevo llamamos al metodo bluerBuilder.setInputData
        blurBuilder.setInputData(createInputDataForWorkRequest(blurLevel, imageUri))

        //para iniciar el trabajo llama el metodo enqueue() en el objeto workManger
        //workManager.enqueue(blurBuilder.build()) //quitar esta llamada
        continuation = continuation.then(blurBuilder.build())

        //crar una solicitud de trabajo para guardar la imagen y agregarla a la cadena

        val save = OneTimeWorkRequestBuilder<SaveImageToFileWorker>().addTag(TAG_OUTPUT)
            .build()
        continuation = continuation.then(save)

        //iniciar el trabajo
        continuation.enqueue()


    }

    /**
     * Cancel any ongoing WorkRequests
     * */
    override fun cancelWork() {}

    /**
     * Creates the input data bundle which includes the blur level to
     * update the amount of blur to be applied and the Uri to operate on
     * @return Data which contains the Image Uri as a String and blur level as an Integer
     */

    //nuevo para crear objetos de datos de entrada
    private fun createInputDataForWorkRequest(blurLevel: Int, imageUri: Uri): Data {
        /**
         * se crea un  objeto Data.Builder  coloca imageUri y blurLevel en el objeto como pares clave-valor.
         * A continuación, se crea un objeto de datos y se muestra cuando llama a return builder.build().
         */

        val builder = Data.Builder()
        builder.putString(KEY_IMAGE_URI, imageUri.toString()).putInt(KEY_BLUR_LEVEL, blurLevel)
        return builder.build()
    }
}
