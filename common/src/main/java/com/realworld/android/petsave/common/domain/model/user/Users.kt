package com.realworld.android.petsave.common.domain.model.user

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "users", strict = false)
data class Users constructor(
    @field:ElementList(entry = "user", inline = true)
    @param:ElementList(entry = "user", inline = true)
    val list: List<User>? = null)