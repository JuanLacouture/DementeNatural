package com.example.dementenatural

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Registrar_Venta : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var productList: RecyclerView
    private lateinit var totalAmount: TextView
    private lateinit var confirmSaleButton: View
    private var total: Double = 0.0
    private val selectedProducts = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_venta)

        searchInput = findViewById(R.id.searchInput)
        productList = findViewById(R.id.productList)
        totalAmount = findViewById(R.id.totalAmount)
        confirmSaleButton = findViewById(R.id.confirmSaleButton)

        // RecyclerView setup
        productList.layoutManager = LinearLayoutManager(this)

        // Listener for search input
        searchInput.addTextChangedListener {
            searchProducts(it.toString())
        }

        confirmSaleButton.setOnClickListener {
            confirmSale()
        }

        // Load user data and filter products based on the user's sede
        loadUserData()
    }

    private fun loadUserData() {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email
        val userRef = FirebaseDatabase.getInstance().getReference("Users")

        val userQuery = userRef.orderByChild("email").equalTo(userEmail)
        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.children.first().getValue(User::class.java)
                    val userSede = user?.sede
                    if (userSede != null) {
                        loadProducts(userSede)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Registrar_Venta, "Error al cargar los datos del usuario", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadProducts(sede: String) {
        val productsRef = FirebaseDatabase.getInstance().getReference("Inventario")
        val productQuery = productsRef.orderByChild("sede").equalTo(sede)

        productQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = mutableListOf<Product>()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue<Product>()
                    if (product?.quantity ?: 0 > 0) { // Solo productos con stock disponible
                        productList.add(product!!)
                    }
                }
                // Actualiza el RecyclerView con los productos encontrados
                updateRecyclerView(productList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Registrar_Venta, "Error al cargar los productos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun searchProducts(query: String) {
        val filteredProducts = mutableListOf<Product>()
        val productsRef = FirebaseDatabase.getInstance().getReference("Inventario")
        val productQuery = productsRef.orderByChild("name").startAt(query).endAt(query + "\uf8ff")

        productQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue<Product>()
                    if (product?.quantity ?: 0 > 0) { // Solo productos con stock disponible
                        filteredProducts.add(product!!)
                    }
                }
                // Actualiza el RecyclerView con los productos filtrados
                updateRecyclerView(filteredProducts)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Registrar_Venta, "Error al cargar los productos filtrados", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateRecyclerView(products: List<Product>) {
        val adapter = ProductAdapter(products) { product ->
            addProductToSale(product)
        }
        productList.adapter = adapter
    }

    private fun addProductToSale(product: Product) {
        // Si el producto tiene más de 0 en stock, se puede agregar
        if (product.quantity > 0) {
            // Si el producto solo tiene 1 en stock, no se podrá agregar más veces
            if (product.quantity == 1 && selectedProducts.count { it.name == product.name } >= 1) {
                Toast.makeText(this, "No puedes agregar más de este producto", Toast.LENGTH_SHORT).show()
                return
            }

            selectedProducts.add(product)
            total += product.price
            totalAmount.text = "$total COP"

            // Reduce el stock temporalmente (no en Firebase aún)
            val productRef = FirebaseDatabase.getInstance().getReference("Inventario").child(product.name)
            productRef.child("quantity").setValue(product.quantity - 1)
        }
    }

    private fun confirmSale() {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email
        val saleId = FirebaseDatabase.getInstance().getReference("Ventas").push().key

        val sale = Sale(
            saleId = saleId,
            email = userEmail,
            products = selectedProducts,
            total = total
        )

        val saleRef = FirebaseDatabase.getInstance().getReference("Ventas")
        saleRef.child(saleId!!).setValue(sale).addOnCompleteListener {
            if (it.isSuccessful) {
                // Actualizar el inventario después de confirmar la venta
                updateInventory()
                Toast.makeText(this, "Venta registrada", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error al registrar la venta", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateInventory() {
        for (product in selectedProducts) {
            val productRef = FirebaseDatabase.getInstance().getReference("Inventario").child(product.name)
            val newQuantity = product.quantity - selectedProducts.count { it.name == product.name }
            productRef.child("quantity").setValue(newQuantity)
        }
    }

    // Adapter para RecyclerView
    class ProductAdapter(
        private val productList: List<Product>,
        private val onProductClick: (Product) -> Unit
    ) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sale_product, parent, false)
            return ProductViewHolder(view)
        }

        override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
            val product = productList[position]
            holder.bind(product)
        }

        override fun getItemCount(): Int = productList.size

        inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val productName: TextView = itemView.findViewById(R.id.productName)
            private val productPrice: TextView = itemView.findViewById(R.id.productPrice)

            fun bind(product: Product) {
                productName.text = product.name
                productPrice.text = "${product.price} COP"
                itemView.setOnClickListener {
                    onProductClick(product)
                }
            }
        }
    }

    data class Sale(
        val saleId: String? = null,
        val email: String? = null,
        val products: List<Product> = listOf(),
        val total: Double = 0.0
    )
}
