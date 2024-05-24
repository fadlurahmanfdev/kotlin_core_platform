package co.id.fadlurahmanfdev.kotlincoreplatform.presentation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import co.id.fadlurahmanfdev.kotlin_core_platform.data.repository.CorePlatformRepositoryImpl
import co.id.fadlurahmanfdev.kotlincoreplatform.R
import co.id.fadlurahmanfdev.kotlincoreplatform.data.FeatureModel
import co.id.fadlurahmanfdev.kotlincoreplatform.domain.ExampleCryptoUseCaseImpl

class MainActivity : AppCompatActivity(), ListExampleAdapter.Callback {
    lateinit var viewModel: MainViewModel

    private val features: List<FeatureModel> = listOf<FeatureModel>(
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Is Location Enabled",
            desc = "Check whether location enabled",
            enum = "CHECK_LOCATION_ENABLED"
        ),
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Get Coordinate",
            desc = "Get Coordinate Latitude Longitude",
            enum = "GET_COORDINATE"
        ),
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Get Address",
            desc = "Get Address",
            enum = "GET_ADDRESS"
        ),
    )

    private lateinit var rv: RecyclerView

    private lateinit var adapter: ListExampleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        rv = findViewById<RecyclerView>(R.id.rv)

        viewModel = MainViewModel(
            exampleCryptoUseCase = ExampleCryptoUseCaseImpl(
                platformRepository = CorePlatformRepositoryImpl()
            )
        )

        rv.setItemViewCacheSize(features.size)
        rv.setHasFixedSize(true)

        adapter = ListExampleAdapter()
        adapter.setCallback(this)
        adapter.setList(features)
        adapter.setHasStableIds(true)
        rv.adapter = adapter
    }

    override fun onClicked(item: FeatureModel) {
        when (item.enum) {
            "CHECK_LOCATION_ENABLED" -> {
                viewModel.checkIsLocationEnabled(this)
            }
            "GET_COORDINATE" -> {
                viewModel.getLastCoordinate(this)
            }
            "GET_ADDRESS" -> {
                viewModel.getAddress(this)
            }
        }
    }
}