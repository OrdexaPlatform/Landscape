package com.example.data.repository

import com.example.data.local.ClientDao
import com.example.data.local.FinancialAccountDao
import com.example.data.local.ProjectDao
import com.example.data.model.Client
import com.example.data.model.FinancialAccount
import com.example.data.model.Project
import kotlinx.coroutines.flow.Flow

class InvoiceRepository(
    private val clientDao: ClientDao,
    private val projectDao: ProjectDao,
    private val financialAccountDao: FinancialAccountDao
) {
    // Clients
    val allClients: Flow<List<Client>> = clientDao.getAllClients()
    
    suspend fun insertClient(client: Client): Long = clientDao.insertClient(client)
    suspend fun updateClient(client: Client) = clientDao.updateClient(client)
    suspend fun deleteClient(client: Client) = clientDao.deleteClient(client)
    suspend fun getClientById(id: Int): Client? = clientDao.getClientById(id)

    // Projects
    val allProjects: Flow<List<Project>> = projectDao.getAllProjects()
    
    fun getProjectsForClient(clientId: Int): Flow<List<Project>> = projectDao.getProjectsForClient(clientId)
    suspend fun insertProject(project: Project): Long = projectDao.insertProject(project)
    suspend fun updateProject(project: Project) = projectDao.updateProject(project)
    suspend fun deleteProject(project: Project) = projectDao.deleteProject(project)

    // Financial Accounts
    val allAccounts: Flow<List<FinancialAccount>> = financialAccountDao.getAllAccounts()
    val banksTotalBalance: Flow<Double?> = financialAccountDao.getBanksTotalBalance()
    val safesTotalBalance: Flow<Double?> = financialAccountDao.getSafesTotalBalance()

    suspend fun insertAccount(account: FinancialAccount): Long = financialAccountDao.insertAccount(account)
    suspend fun updateAccount(account: FinancialAccount) = financialAccountDao.updateAccount(account)
    suspend fun deleteAccount(account: FinancialAccount) = financialAccountDao.deleteAccount(account)
}
