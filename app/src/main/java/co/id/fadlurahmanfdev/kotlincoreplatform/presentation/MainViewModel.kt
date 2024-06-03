package co.id.fadlurahmanfdev.kotlincoreplatform.presentation

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import co.id.fadlurahmanfdev.kotlincoreplatform.domain.ExampleCorePlatformUseCase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainViewModel(
    private val exampleCorePlatformUseCase: ExampleCorePlatformUseCase
) : ViewModel() {

    private val disposable = CompositeDisposable()

    fun checkIsLocationEnabled(context: Context) {
        val isEnabled = exampleCorePlatformUseCase.isLocationEnabled(context)
        Log.d(
            MainViewModel::class.java.simpleName,
            "IS LOCATION ENABLED: $isEnabled"
        )
    }

    fun getCurrentLocation() {
        disposable.add(exampleCorePlatformUseCase.getCurrentLocation().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { coordinate ->
                    println("COORDINATE MODEL: $coordinate")
                },
                {
                    println("ERROR COORDINATE: $it")
                },
                {
                    println("COMPLETE")
                }
            ))
    }

    fun getAddress() {
        disposable.add(exampleCorePlatformUseCase.getAddress().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { address ->
                    println("ADDRESS MODEL: $address")
                },
                {
                    println("ERROR ADDRESS: $it")
                },
                {
                    println("COMPLETE")
                }
            ))
    }

}