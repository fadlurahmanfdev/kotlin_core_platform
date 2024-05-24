package co.id.fadlurahmanfdev.kotlin_core_platform.data.model

data class AddressModel(
    val country: String,
    /**
     * province
     * */
    val adminArea: String,
    /**
     * city
     * */
    val subAdminArea: String,
    /**
     * district
     */
    val locality: String,
    /**
     * sub-district
     */
    val subLocality: String,
    val postalCode: String,
    val latitude: Double,
    val longitude: Double,
)
