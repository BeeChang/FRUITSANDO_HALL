package example.yf.fruit_hall.data

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigRepository @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) {
    suspend fun fetchAndActivate(force: Boolean = false) {
        if (force) {
            remoteConfig.fetch(0L).await()
            remoteConfig.activate().await()
        } else {
            remoteConfig.fetchAndActivate().await()
        }
    }

    fun getScheduleJson(monthKey: String): String? =
        remoteConfig.getString(monthKey).ifEmpty { null }
}
