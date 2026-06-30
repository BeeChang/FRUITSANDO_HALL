package example.yf.fruit_hall.data.schedule.model

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleResponse(
    val updated: String,
    val schedules: List<ScheduleDay>
)

@Serializable
data class ScheduleDay(
    val date: String,
    val shifts: Map<String, String>
)
