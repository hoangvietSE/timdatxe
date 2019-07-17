package com.example.anothertimdatxe.base.network

import com.example.anothertimdatxe.BuildConfig
import com.example.anothertimdatxe.base.ApiConstant
import com.example.anothertimdatxe.entity.ForgotResult
import com.example.anothertimdatxe.entity.RegisResult
import com.example.anothertimdatxe.entity.UserData
import com.example.anothertimdatxe.entity.UserListPostEntity
import com.example.anothertimdatxe.entity.response.*
import com.example.anothertimdatxe.request.*
import com.example.anothertimdatxe.util.CarBookingSharePreference
import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

//singleton
object RetrofitManager {
    const val TIME: Long = 60
    private fun createRetrofit(baseUrl: String): Retrofit {
        //timeout for connection is 120s
        var client = OkHttpClient.Builder()
                .writeTimeout(RetrofitManager.TIME, TimeUnit.SECONDS)
                .readTimeout(RetrofitManager.TIME, TimeUnit.SECONDS)
                .connectTimeout(RetrofitManager.TIME, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            var logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            client.addInterceptor(logging)
        }

        return Retrofit.Builder()
                .client(client.build())
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                //this verifies that using RxJava2 for this API call
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }

    private val apiService = createRetrofit(BuildConfig.BASE_URL).create(ApiService::class.java)

    private fun <T> getSubcriber(callBack: ICallBack<T>): DisposableSingleObserver<Response<T>> {
        return object : DisposableSingleObserver<Response<T>>() {
            override fun onSuccess(response: Response<T>) {
                /*if (t.status == 200) {
                    callBack.onSuccess(t.data)
                } else {
                    if (TextUtils.isEmpty(t.msg)) {
                        callBack.onError(ApiException(""))
                    } else {
                        callBack.onError(ApiException((t.msg)))
                    }
                }*/
                handleResponse(callBack, response)
            }

            override fun onError(e: Throwable) {
                callBack.onError(ApiException(e.message))
            }
        }
    }

    private fun <T> handleResponse(callBack: ICallBack<T>, response: Response<T>) {
        when {
            response.code() == ApiConstant.httpStatusCode.OK -> callBack.onSuccess(response.body()!!)
            response.code() == ApiConstant.httpStatusCode.CREATE -> callBack.onSuccess(response.body()!!)
            response.code() == ApiConstant.httpStatusCode.UNAUTHORIZED -> handleErrorResponse(callBack, response)
            else -> handleErrorResponse(callBack, response)
        }
    }

    private fun <T> handleErrorResponse(callBack: ICallBack<T>, response: Response<T>) {
        callBack.onError(ApiException("Error"))
    }

    //POJO Object to Json
    //convert request object to JSON and add to body of POST HTTP request
    private fun createPostRequest(request: Any): RequestBody {
        var requestInJson: String = Gson().toJson(request)
        return RequestBody.create(MultipartBody.FORM, requestInJson) //data is divided to many part
    }

    //Utilize RxJava2 to run this on another thread and get result in Main Thread by Obverser
    fun loginUser(callBack: ICallBack<BaseResult<UserData>>, request: LoginRequest): Disposable {
        val requestBody = createPostRequest(request)
        val subcribe = getSubcriber(callBack)
        var disposable: Disposable = apiService.loginUser(requestBody)
                //Observable: I/O thread
                //request will be execute in I/O thread
                //Schedulers.io(): network call, file, database...
                .observeOn(AndroidSchedulers.mainThread())
                //Observer: Main Thread
                //get return data in main thread
                //AndroidSchedulers.mainThread(): allow in UI thread
                .subscribeOn(Schedulers.io())
                .subscribeWith(subcribe)
        return disposable
    }

    fun loginDriver(callBack: ICallBack<BaseResult<UserData>>, request: LoginRequest): Disposable {
        val subscribe = getSubcriber(callBack)
        return apiService.loginDriver(createPostRequest(request))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(subscribe)
    }

    fun registerDriver(callBack: ICallBack<BaseResult<RegisResult>>, request: RegisterRequest): Disposable {
        val subcriber = getSubcriber(callBack)
        return apiService.registerDriver(createPostRequest(request))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(subcriber)
    }

    fun activeDriver(callBack: ICallBack<BaseResult<UserData>>, request: ActiveRequest): Disposable {
        val subscriber = getSubcriber(callBack)
        return apiService.activeDriver(createPostRequest(request))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(subscriber)
    }

    fun resetUserPassword(callBack: ICallBack<BaseResult<ForgotResult>>, request: ForgotRequest): Disposable {
        val subscriber = getSubcriber(callBack)
        return apiService.resetUserPassword(createPostRequest(request))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(subscriber)
    }

    fun resetDriverPassword(callBack: ICallBack<BaseResult<ForgotResult>>, request: ForgotRequest): Disposable {
        val subscriber = getSubcriber(callBack)
        return apiService.resetDriverPassword(createPostRequest(request))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(subscriber)
    }

    fun userUpdatePassword(callBack: ICallBack<BaseResult<UserData>>, request: UpdatePasswordRequest): Disposable {
        val subscriber = getSubcriber(callBack)
        return apiService.userUpdatePassword(createPostRequest(request))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(subscriber)
    }

    fun driverUpdatePassword(callBack: ICallBack<BaseResult<UserData>>, request: UpdatePasswordRequest): Disposable {
        val subscriber = getSubcriber(callBack)
        return apiService.userUpdatePassword(createPostRequest(request))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(subscriber)
    }

    //user change password
    fun userChangePassWord(iCallBack: ICallBack<BaseResult<UserData>>, request: ChangePasswordRequest): Disposable {
        val subscriber = getSubcriber(iCallBack)
        return apiService.userChangePassword(
                CarBookingSharePreference.getUserId(),
                createPostRequest(request)
        )//id of viethoangtien@gmail.com
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(subscriber)
    }

    fun loginSocial(callBack: ICallBack<BaseResult<UserData>>, request: LoginFacebookRequest): Disposable {
        val subscriber = getSubcriber(callBack)
        return apiService.loginSocial(createPostRequest(request))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(subscriber)
    }

    fun onGetFaqs(iCallBack: ICallBack<BaseResult<List<FaqsResponse>>>, request: MutableMap<String, Any>): Disposable {
        val subcriber = getSubcriber(iCallBack)
        return apiService.onGetFaqs(request)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(subcriber)
    }

    fun getUserPostCreated(iCallBack: ICallBack<BaseResult<List<UserListPostEntity>>>): Disposable {
        val subscriber = getSubcriber(iCallBack)
        return apiService.getUserPostCreated(CarBookingSharePreference.getUserId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(subscriber)
    }

    fun getUserInfo(iCallBack: ICallBack<BaseResult<UserData>>): Disposable {
        val subscriber = getSubcriber(iCallBack)
        return apiService.getUserInfo(CarBookingSharePreference.getUserId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(subscriber)
    }

    fun userUpdateProfile(part: MultipartBody.Part?, request: Map<String, RequestBody>): Single<BaseResult<UserData>> {
        return apiService.userUpdateProfile(CarBookingSharePreference.getAccessToken(), CarBookingSharePreference.getUserId(),
                part, request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getListPostFindUser(request: MutableMap<String, Any>): Single<BaseResult<List<DriverPostResponse>>> {
        return apiService.getListPostFindUser(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getListPostFindCar(request: MutableMap<String, Any>): Single<BaseResult<List<UserPostResponse>>> {
        return apiService.getListPostFindCar(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getUserHistory(iCallBack: ICallBack<BaseResponse<List<UserHistoryResponse>>>, id: Int): Disposable {
        var subscribe = getSubcriber(iCallBack)
        return apiService.getUserHistory(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(subscribe)
    }

    fun getDriverHistory(iCallBack: ICallBack<BaseResponse<List<DriverHistoryResponse>>>, id: Int): Disposable {
        var subscribe = getSubcriber(iCallBack)
        return apiService.getDriverHistory(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(subscribe)
    }

    fun getDriverRevenue(iCallBack: ICallBack<BaseRevenueResponse<List<DriverRevenueResponse>>>, month: Int): Disposable {
        val subscribe = getSubcriber(iCallBack)
        return apiService.getDriverRevenue(CarBookingSharePreference.getAccessToken(), CarBookingSharePreference.getUserId(), month)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(subscribe)
    }
}