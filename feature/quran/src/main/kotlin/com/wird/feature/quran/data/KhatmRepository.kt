package com.wird.feature.quran.data

import com.wird.core.database.dao.KhatmDao
import com.wird.core.database.dao.LastPositionDao
import com.wird.core.database.entity.KhatmPlanEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.ceil

private const val TOTAL_AYAT = 6236

sealed interface KhatmUiState {
    data object NoPlan : KhatmUiState
    data class Active(
        val targetEpochDay: Long,
        val ayatRead: Int,
        val totalAyat: Int,
        val dailyTarget: Int,
        val daysRemaining: Int,
        val remainingAyat: Int,
        val todayPace: Int,
        val aheadBy: Int, // positive = ahead of schedule, negative = behind
        val finished: Boolean,
    ) : KhatmUiState {
        val progress: Float get() = (ayatRead.toFloat() / totalAyat).coerceIn(0f, 1f)
    }
}

interface KhatmRepository {
    fun observeState(): Flow<KhatmUiState>
    suspend fun createPlan(targetEpochDay: Long)
    suspend fun clearPlan()
}

class KhatmRepositoryImpl @Inject constructor(
    private val khatmDao: KhatmDao,
    private val lastPositionDao: LastPositionDao,
) : KhatmRepository {

    override fun observeState(): Flow<KhatmUiState> =
        combine(khatmDao.observe(), lastPositionDao.observe()) { plan, position ->
            if (plan == null) {
                KhatmUiState.NoPlan
            } else {
                buildState(plan, ayatRead = (position?.ayahId ?: 0).coerceIn(0, TOTAL_AYAT))
            }
        }

    override suspend fun createPlan(targetEpochDay: Long) {
        khatmDao.upsert(
            KhatmPlanEntity(
                startEpochDay = LocalDate.now().toEpochDay(),
                targetEpochDay = targetEpochDay,
            ),
        )
    }

    override suspend fun clearPlan() = khatmDao.clear()

    private fun buildState(plan: KhatmPlanEntity, ayatRead: Int): KhatmUiState.Active {
        val today = LocalDate.now().toEpochDay()
        val totalDays = (plan.targetEpochDay - plan.startEpochDay).coerceAtLeast(1)
        val daysElapsed = (today - plan.startEpochDay).coerceIn(0, totalDays)
        val daysRemaining = (plan.targetEpochDay - today).coerceAtLeast(0).toInt()

        val dailyTarget = ceil(TOTAL_AYAT.toDouble() / totalDays).toInt()
        val expectedByToday = (dailyTarget * (daysElapsed + 1)).coerceAtMost(TOTAL_AYAT.toLong()).toInt()
        val remaining = (TOTAL_AYAT - ayatRead).coerceAtLeast(0)
        val todayPace = if (daysRemaining > 0) ceil(remaining.toDouble() / daysRemaining).toInt() else remaining

        return KhatmUiState.Active(
            targetEpochDay = plan.targetEpochDay,
            ayatRead = ayatRead,
            totalAyat = TOTAL_AYAT,
            dailyTarget = dailyTarget,
            daysRemaining = daysRemaining,
            remainingAyat = remaining,
            todayPace = todayPace,
            aheadBy = ayatRead - expectedByToday,
            finished = ayatRead >= TOTAL_AYAT,
        )
    }
}
