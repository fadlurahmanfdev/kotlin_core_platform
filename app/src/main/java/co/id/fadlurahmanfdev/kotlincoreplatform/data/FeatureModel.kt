package co.id.fadlurahmanfdev.kotlincoreplatform.data

import androidx.annotation.DrawableRes

data class FeatureModel(
    @DrawableRes val featureIcon: Int,
    val enum: String,
    val title: String,
    val desc: String? = null,
)
