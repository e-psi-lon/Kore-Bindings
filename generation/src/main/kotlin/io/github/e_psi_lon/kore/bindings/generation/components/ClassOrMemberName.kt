package io.github.e_psi_lon.kore.bindings.generation.components

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

sealed class ClassOrMemberName {
    data class Class(val name: ClassName) : ClassOrMemberName()
    data class Member(val name: MemberName) : ClassOrMemberName()
}

fun ClassName.toClassOrMemberName() = ClassOrMemberName.Class(this)
fun MemberName.toClassOrMemberName() = ClassOrMemberName.Member(this)