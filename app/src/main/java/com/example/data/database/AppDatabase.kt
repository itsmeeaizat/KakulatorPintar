package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.CalculationHistoryDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.ProductDao
import com.example.data.dao.UserDao
import com.example.data.entity.CalculationHistoryEntity
import com.example.data.entity.CategoryEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        CalculationHistoryEntity::class,
        CategoryEntity::class,
        ProductEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun calculationHistoryDao(): CalculationHistoryDao
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "calculator_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val instance = INSTANCE ?: return@launch
                    val categoryDao = instance.categoryDao()
                    val productDao = instance.productDao()

                    // Populate initial sample data if empty
                    if (categoryDao.getCategoryCount() == 0) {
                        populateSeedData(categoryDao, productDao)
                    }
                }
            }

            private suspend fun populateSeedData(categoryDao: CategoryDao, productDao: ProductDao) {
                // Main Categories (Level 1)
                val idMakanan = categoryDao.insertCategory(CategoryEntity(name = "Makanan Utama & Camilan", parentId = null)).toInt()
                val idMinuman = categoryDao.insertCategory(CategoryEntity(name = "Minuman & Beli Kopi", parentId = null)).toInt()
                val idSembako = categoryDao.insertCategory(CategoryEntity(name = "Sembako & Kebutuhan Dapur", parentId = null)).toInt()

                // Sub Categories (Level 2)
                val idMie = categoryDao.insertCategory(CategoryEntity(name = "Mie Instant & Bihun", parentId = idMakanan)).toInt()
                val idSnack = categoryDao.insertCategory(CategoryEntity(name = "Snack & Biskuit", parentId = idMakanan)).toInt()

                val idKopi = categoryDao.insertCategory(CategoryEntity(name = "Kopi & Teh Kemasan", parentId = idMinuman)).toInt()
                val idAir = categoryDao.insertCategory(CategoryEntity(name = "Air Mineral Botol/Gelas", parentId = idMinuman)).toInt()

                val idMinyak = categoryDao.insertCategory(CategoryEntity(name = "Minyak Goreng & Mentega", parentId = idSembako)).toInt()
                val idBumbu = categoryDao.insertCategory(CategoryEntity(name = "Bumbu & Penyedap", parentId = idSembako)).toInt()

                // Sub Sub Categories (Level 3 under Mie)
                val idIndomie = categoryDao.insertCategory(CategoryEntity(name = "Merk Indomie", parentId = idMie)).toInt()
                val idMieSedaap = categoryDao.insertCategory(CategoryEntity(name = "Merk Mie Sedaap", parentId = idMie)).toInt()

                // Seed Products
                productDao.insertProducts(
                    listOf(
                        ProductEntity(categoryId = idIndomie, name = "Indomie Goreng Spesial 85g", brand = "Indomie", price = 3500.0, stock = 35, barcode = "8991001"),
                        ProductEntity(categoryId = idIndomie, name = "Indomie Kuah Soto Medan 75g", brand = "Indomie", price = 3500.0, stock = 4, barcode = "8991005"),
                        ProductEntity(categoryId = idIndomie, name = "Indomie Ayam Bawang 75g", brand = "Indomie", price = 3500.0, stock = 20, barcode = "8991006"),

                        ProductEntity(categoryId = idMieSedaap, name = "Mie Sedaap Goreng Original", brand = "Wings Food", price = 3500.0, stock = 18, barcode = "8991007"),
                        ProductEntity(categoryId = idMieSedaap, name = "Mie Sedaap Soto Madura", brand = "Wings Food", price = 3500.0, stock = 3, barcode = "8991008"),

                        ProductEntity(categoryId = idSnack, name = "Taro Net Seaweed 36g", brand = "Taro", price = 5000.0, stock = 12, barcode = "8994001"),
                        ProductEntity(categoryId = idSnack, name = "Chitato Sapi Panggang 68g", brand = "Indofood", price = 11500.0, stock = 2, barcode = "8994002"),

                        ProductEntity(categoryId = idKopi, name = "Kopi Kapal Api Grande 25g", brand = "Kapal Api", price = 2500.0, stock = 40, barcode = "8991004"),
                        ProductEntity(categoryId = idKopi, name = "Teh Poci Celup Sosro 25s", brand = "Poci", price = 7500.0, stock = 9, barcode = "8991003"),

                        ProductEntity(categoryId = idAir, name = "Air Mineral Le Minerale 600ml", brand = "Mayora", price = 4000.0, stock = 3, barcode = "8991002"),
                        ProductEntity(categoryId = idAir, name = "Aqua Botol Sedang 600ml", brand = "Danone", price = 4000.0, stock = 24, barcode = "8991009"),

                        ProductEntity(categoryId = idMinyak, name = "Minyak Kita Refill 1 Liter", brand = "BPN", price = 16000.0, stock = 15, barcode = "8992001"),
                        ProductEntity(categoryId = idBumbu, name = "Masako Rasa Sapi Sachet 100g", brand = "Ajinomoto", price = 5000.0, stock = 10, barcode = "8992005")
                    )
                )
            }
        }
    }
}
