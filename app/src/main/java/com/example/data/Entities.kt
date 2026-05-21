package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val fullName: String,
    val passwordHash: String,
    val joinedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "admins")
data class AdminUser(
    @PrimaryKey val id: Int = 1,
    val email: String,
    val passwordHash: String
)

@Entity(tableName = "membership_plans")
data class MembershipPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val billingCycle: String = "month",
    val features: String // Comma separated features
)

@Entity(tableName = "trainers")
data class Trainer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val specialization: String,
    val experience: String, // e.g. "5 Years"
    val photoResName: String, // To load vector drawing/resource
    val contactPhone: String,
    val bio: String = ""
)

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val userName: String,
    val planName: String,
    val trainerName: String,
    val timeSlot: String, // "Morning (6 AM - 8 AM)", etc.
    val bookingDate: String, // String representation format "YYYY-MM-DD"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey val key: String,
    val value: String
)
