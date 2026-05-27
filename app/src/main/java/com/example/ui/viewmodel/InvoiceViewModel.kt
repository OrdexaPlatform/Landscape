package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Client
import com.example.data.model.FinancialAccount
import com.example.data.model.Project
import com.example.data.repository.InvoiceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// A structure helper for client creation form containing un-saved projects
data class ProjectDraft(
    val name: String = "",
    val hasElectronicInvoice: Boolean = false,
    val hasAssignmentLetter: Boolean = false,
    val hasDeliveryReceipt: Boolean = false,
    val deliveryReceiptDate: Long? = null,
    val paymentTermDays: Int = 30,
    val totalValue: Double = 0.0,
    val actualCost: Double = 0.0,
    val isCompleted: Boolean = false,
    val completionDate: Long? = null
)

class InvoiceViewModel(private val repository: InvoiceRepository) : ViewModel() {

    // UI States
    val clients: StateFlow<List<Client>> = repository.allClients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val financialAccounts: StateFlow<List<FinancialAccount>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val banksTotalBalance: StateFlow<Double> = repository.banksTotalBalance
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val safesTotalBalance: StateFlow<Double> = repository.safesTotalBalance
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Derived Financial Stats
    val totalCapital: Flow<Double> = combine(banksTotalBalance, safesTotalBalance) { banks, safes ->
        banks + safes
    }

    // Calculations of Profits (الأرباح)
    // Sum of projects profits (Total Project Value - Actual Cost)
    val totalProfits: Flow<Double> = projects.map { projectList ->
        projectList.sumOf { it.profit }
    }

    // Count of open projects (المشاريع المفتوحة - projects where isCompleted is false)
    val openProjectsCount: Flow<Int> = projects.map { projectList ->
        projectList.count { !it.isCompleted }
    }

    // Capital & Accounts Operations
    fun addFinancialAccount(name: String, balance: Double, type: String) {
        viewModelScope.launch {
            repository.insertAccount(FinancialAccount(name = name, balance = balance, type = type))
        }
    }

    fun updateFinancialAccount(account: FinancialAccount) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun deleteFinancialAccount(account: FinancialAccount) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }

    // Client and Project Multi-Creation
    fun createClientWithProjects(clientName: String, projectDrafts: List<ProjectDraft>) {
        viewModelScope.launch {
            if (clientName.isBlank()) return@launch
            
            // Insert Client and get generated ID
            val clientId = repository.insertClient(Client(name = clientName)).toInt()
            
            // For each project draft, insert into db
            for (draft in projectDrafts) {
                if (draft.name.isBlank()) continue
                val project = Project(
                    clientId = clientId,
                    name = draft.name,
                    hasElectronicInvoice = draft.hasElectronicInvoice,
                    hasAssignmentLetter = draft.hasAssignmentLetter,
                    hasDeliveryReceipt = draft.hasDeliveryReceipt,
                    deliveryReceiptDate = draft.deliveryReceiptDate,
                    paymentTermDays = draft.paymentTermDays,
                    totalValue = draft.totalValue,
                    actualCost = draft.actualCost,
                    isCompleted = draft.isCompleted,
                    completionDate = draft.completionDate
                )
                repository.insertProject(project)
            }
        }
    }

    // Single additions
    fun addClient(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.insertClient(Client(name = name))
            }
        }
    }

    fun deleteClient(client: Client) {
        viewModelScope.launch {
            repository.deleteClient(client)
        }
    }

    fun addProject(project: Project) {
        viewModelScope.launch {
            repository.insertProject(project)
        }
    }

    fun updateProject(project: Project) {
        viewModelScope.launch {
            repository.updateProject(project)
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }
}

// Room ViewModel Factory Pattern
class InvoiceViewModelFactory(private val repository: InvoiceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InvoiceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InvoiceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
