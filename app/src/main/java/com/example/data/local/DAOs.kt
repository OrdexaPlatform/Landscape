package com.example.data.local

import androidx.room.*
import com.example.data.model.Client
import com.example.data.model.FinancialAccount
import com.example.data.model.Project
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): Flow<List<Client>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: Client): Long

    @Update
    suspend fun updateClient(client: Client)

    @Delete
    suspend fun deleteClient(client: Client)
    
    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    suspend fun getClientById(id: Int): Client?
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY id DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE clientId = :clientId ORDER BY id DESC")
    fun getProjectsForClient(clientId: Int): Flow<List<Project>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)
}

@Dao
interface FinancialAccountDao {
    @Query("SELECT * FROM financial_accounts ORDER BY type ASC, name ASC")
    fun getAllAccounts(): Flow<List<FinancialAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: FinancialAccount): Long

    @Update
    suspend fun updateAccount(account: FinancialAccount)

    @Delete
    suspend fun deleteAccount(account: FinancialAccount)

    @Query("SELECT SUM(balance) FROM financial_accounts WHERE type = 'BANK'")
    fun getBanksTotalBalance(): Flow<Double?>

    @Query("SELECT SUM(balance) FROM financial_accounts WHERE type = 'SAFE'")
    fun getSafesTotalBalance(): Flow<Double?>
}
