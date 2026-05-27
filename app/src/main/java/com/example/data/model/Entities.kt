package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "financial_accounts")
data class FinancialAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val balance: Double,
    val type: String // "BANK" or "SAFE"
)

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

@Entity(
    tableName = "projects",
    foreignKeys = [
        ForeignKey(
            entity = Client::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientId: Int,
    val name: String,
    val hasElectronicInvoice: Boolean,
    val hasAssignmentLetter: Boolean,
    val hasDeliveryReceipt: Boolean,
    val deliveryReceiptDate: Long? = null, // Date of delivery statement
    val paymentTermDays: Int = 30, // Total payment term days (e.g. 30)
    val totalValue: Double = 0.0, // Total project value (القيمة الاجمالية)
    val actualCost: Double = 0.0, // Actual cost (قيمة التكلفة الفعلية)
    val isCompleted: Boolean = false,
    val completionDate: Long? = null // Date of completion
) {
    // Calculated profit (الربح هيكون نقص التكلفة الاجمالية من التكلفة الفعلية)
    val profit: Double
        get() = totalValue - actualCost

    // Calculated loss (خسارة) if any
    val loss: Double
        get() = if (actualCost > totalValue) actualCost - totalValue else 0.0
}
