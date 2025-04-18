package flab.kr.catchfood.store.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalTime

@Entity
@Table(name = "Store")
class Store(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "NAME", nullable = false, length = 100)
    val name: String,

    @Column(name = "CATEGORY", nullable = false, length = 30)
    val category: String,

    @Column(name = "DISTANCE_IN_MINUTES_BY_WALK", nullable = false)
    val distanceInMinutesByWalk: Int,

    @Column(name = "BUSINESS_OPEN_HOUR", nullable = false)
    val businessOpenHour: LocalTime,

    @Column(name = "BUSINESS_CLOSE_HOUR", nullable = false)
    val businessCloseHour: LocalTime,

    @Column(name = "ADDRESS", nullable = false, length = 200)
    val address: String,

    @Column(name = "CONTACT", nullable = false, length = 30)
    val contact: String,

    @Column(name = "RATING_STARS", nullable = false, precision = 2, scale = 1)
    val ratingStars: BigDecimal
) {
    @OneToMany(mappedBy = "store", cascade = [CascadeType.ALL], orphanRemoval = true)
    val menus: MutableList<Menu> = mutableListOf()
}
