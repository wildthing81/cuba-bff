/*
 * The code is copyright ©2021
 */

package com.foo.credible.service

import com.anzi.credible.dto.BorrowerDto

interface BorrowerService {
    companion object {
        const val NAME = "crd_BorrowerService"
    }

    fun createBorrower(borrowerDto: BorrowerDto): Any?

    fun fetchBorrower(id: String): Any

    fun fetchBorrowers(): Any

    fun updateBorrower(borrowerId: String, borrowerDto: BorrowerDto): Any?
}
