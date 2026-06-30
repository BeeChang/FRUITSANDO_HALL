package example.yf.fruit_hall.data

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigRepository @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) {
    suspend fun fetchScheduleJson(monthKey: String): String? {
        remoteConfig.fetchAndActivate().await()
        val json = remoteConfig.getString(monthKey)
        return json.ifEmpty { null }
    }
}
