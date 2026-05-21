package com.example.data

import kotlinx.coroutines.flow.Flow

class GymRepository(private val gymDao: GymDao) {

    val allUsersCountFlow: Flow<Int> = gymDao.getUsersCountFlow()
    val allUsersFlow: Flow<List<User>> = gymDao.getAllUsersFlow()

    val allPlansFlow: Flow<List<MembershipPlan>> = gymDao.getAllPlansFlow()
    val allTrainersFlow: Flow<List<Trainer>> = gymDao.getAllTrainersFlow()
    val allBookingsFlow: Flow<List<Booking>> = gymDao.getAllBookingsFlow()
    val allNotificationsFlow: Flow<List<NotificationMessage>> = gymDao.getAllNotificationsFlow()
    val allSettingsFlow: Flow<List<AppSetting>> = gymDao.getAllSettingsFlow()

    fun getBookingsForUser(email: String): Flow<List<Booking>> {
        return gymDao.getBookingsForUserFlow(email)
    }

    suspend fun getUserByEmail(email: String): User? {
        return gymDao.getUserByEmail(email)
    }

    suspend fun insertUser(user: User): Long {
        return gymDao.insertUser(user)
    }

    suspend fun getAdminUser(): AdminUser? {
        return gymDao.getAdminUser()
    }

    suspend fun getAdminsCount(): Int {
        return gymDao.getAdminsCount()
    }

    suspend fun createAdmin(admin: AdminUser) {
        gymDao.createAdmin(admin)
    }

    suspend fun savePlan(plan: MembershipPlan) {
        gymDao.savePlan(plan)
    }

    suspend fun deletePlan(plan: MembershipPlan) {
        gymDao.deletePlan(plan)
    }

    suspend fun saveTrainer(trainer: Trainer) {
        gymDao.saveTrainer(trainer)
    }

    suspend fun deleteTrainer(trainer: Trainer) {
        gymDao.deleteTrainer(trainer)
    }

    suspend fun createBooking(booking: Booking) {
        gymDao.createBooking(booking)
    }

    suspend fun deleteBooking(booking: Booking) {
        gymDao.deleteBooking(booking)
    }

    suspend fun insertNotification(notification: NotificationMessage) {
        gymDao.insertNotification(notification)
    }

    suspend fun deleteNotificationById(id: Int) {
        gymDao.deleteNotificationById(id)
    }

    suspend fun getSetting(key: String): String? {
        return gymDao.getSetting(key)?.value
    }

    suspend fun saveSetting(key: String, value: String) {
        gymDao.saveSetting(AppSetting(key, value))
    }

    suspend fun prepopulateIfEmpty() {
        if (gymDao.getPlansCount() == 0) {
            gymDao.savePlan(MembershipPlan(
                name = "Basic Plan",
                price = 29.99,
                features = "Access to Cardio Zone,Free Weights Access,1 Free Fitness Assessment,Locker Room & Showers Access"
            ))
            gymDao.savePlan(MembershipPlan(
                name = "Premium Plan",
                price = 59.99,
                features = "All Basic Plan Features,Unlimited Group Sessions,24/7 Private Gym Access,10% Retail Shop Discount,1 Guest Pass Per Month"
            ))
            gymDao.savePlan(MembershipPlan(
                name = "Personal Training Plan",
                price = 129.99,
                features = "All Premium Plan Features,1-on-1 Expert Fitness Trainer,Custom Weekly Smart Meal Plans,Monthly Body Composition Scans,Priority Class/Slot Booking"
            ))
        }

        if (gymDao.getTrainersCount() == 0) {
            gymDao.saveTrainer(Trainer(
                name = "Sarah Jenkins",
                specialization = "Yoga & Pilates Expert",
                experience = "6 Years",
                photoResName = "ic_trainer_1",
                contactPhone = "+1 (555) 019-2831",
                bio = "Sarah helps clients combine deep core strength development with mental mindfulness. Passionate about posture correction, flexibility, and alignment."
            ))
            gymDao.saveTrainer(Trainer(
                name = "Alex Carter",
                specialization = "Powerlifting & Hypertrophy",
                experience = "8 Years",
                photoResName = "ic_trainer_2",
                contactPhone = "+1 (555) 012-4493",
                bio = "Alex is a competitive powerlifter specializing in compound lift mastery, core strength optimization, and calculated muscle hypertrophy progression."
            ))
            gymDao.saveTrainer(Trainer(
                name = "Marcus Thorne",
                specialization = "HIIT & Athletic Recovery",
                experience = "5 Years",
                photoResName = "ic_trainer_3",
                contactPhone = "+1 (555) 015-8821",
                bio = "Marcus creates energetic aerobic conditioning workouts focusing on metabolic rate acceleration, cardiovascular stamina, and dynamic functional movement."
            ))
        }

        if (gymDao.getSetting("motivational_header") == null) {
            gymDao.saveSetting(AppSetting("motivational_header", "THE BEST WAY TO PREDICT YOUR FUTURE IS TO CREATE IT."))
        }
        if (gymDao.getSetting("diet_tip_1") == null) {
            gymDao.saveSetting(AppSetting("diet_tip_1", "HYDRATION GOALS: Focus on drinking 3 to 4 liters of clean water daily. Skeletal muscles are over 70% water, and dehydration reduces peak force output."))
        }
        if (gymDao.getSetting("diet_tip_2") == null) {
            gymDao.saveSetting(AppSetting("diet_tip_2", "PROTEIN SYNTHESIS: Aim for 1.8 to 2.2 grams of dietary protein per kilogram of body mass. High-quality sources include eggs, grass-fed meats, and whey."))
        }
        if (gymDao.getSetting("diet_tip_3") == null) {
            gymDao.saveSetting(AppSetting("diet_tip_3", "SLEEP TO GROW: Growth hormone is synthesized during slow-wave sleep. Ensure 8 hours of uninterrupted rest to repair muscle fiber micro-tears."))
        }

        // Add a default welcoming mock notification if empty
        val defaultNotifications = gymDao.getAllNotificationsFlow()
        // We will insert in ViewModel to avoid blocking database flows.
    }
}
