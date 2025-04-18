package flab.kr.catchfood.store.domain

import jakarta.persistence.*

@Entity
@Table(name = "Menu")
class Menu(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORE_ID", nullable = false)
    val store: Store,

    @Column(name = "NAME", nullable = false, length = 50)
    val name: String,

    @Column(name = "PRICE", nullable = false)
    val price: Int,

    @Column(name = "IMAGE_URL", length = 2000)
    val imageUrl: String? = null
)
