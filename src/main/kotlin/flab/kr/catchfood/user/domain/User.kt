package flab.kr.catchfood.user.domain

import jakarta.persistence.*

@Entity
@Table(name = "`User`")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "NAME", nullable = false, length = 20)
    val name: String,

    @Column(name = "PREF_LIKES", length = 200)
    var prefLikes: String? = null,

    @Column(name = "PREF_DISLIKES", length = 200)
    var prefDislikes: String? = null,

    @Column(name = "PREF_ETC", length = 200)
    var prefEtc: String? = null
)
