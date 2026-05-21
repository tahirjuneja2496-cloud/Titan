package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GymDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    @Query("SELECT COUNT(*) FROM users")
    fun getUsersCountFlow(): Flow<Int>

    @Query("SELECT * FROM users ORDER BY joinedAt DESC")
    fun getAllUsersFlow(): Flow<List<User>>

    // --- Admin ---
    @Query("SELECT * FROM admins WHERE id = 1 LIMIT 1")
    suspend fun getAdminUser(): AdminUser?

    @Query("SELECT COUNT(*) FROM admins")
    suspend fun getAdminsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createAdmin(admin: AdminUser)

    // --- Membership Plans ---
    @Query("SELECT * FROM membership_plans")
    fun getAllPlansFlow(): Flow<List<MembershipPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlan(plan: MembershipPlan)

    @Delete
    suspend fun deletePlan(plan: MembershipPlan)

    @Query("SELECT COUNT(*) FROM membership_plans")
    suspend fun getPlansCount(): Int

    // --- Trainers ---
    @Query("SELECT * FROM trainers")
    fun getAllTrainersFlow(): Flow<List<Trainer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTrainer(trainer: Trainer)

    @Delete
    suspend fun deleteTrainer(trainer: Trainer)

    @Query("SELECT COUNT(*) FROM trainers")
    suspend fun getTrainersCount(): Int

    // --- Bookings ---
    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun getAllBookingsFlow(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE userEmail = :email ORDER BY createdAt DESC")
    fun getBookingsForUserFlow(email: String): Flow<List<Booking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createBooking(booking: Booking)

    @Delete
    suspend fun deleteBooking(booking: Booking)

    // --- Notifications ---
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<NotificationMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationMessage)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: Int)

    // --- App Settings ---
    @Query("SELECT * FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): AppSetting?

    @Query("SELECT * FROM app_settings")
    fun getAllSettingsFlow(): Flow<List<AppSetting>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: AppSetting)
}
