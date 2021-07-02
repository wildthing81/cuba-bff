/*
 * The code is copyright ©2021
 */

package com.foo.credible.repository

import com.anzi.credible.dto.BorrowerDto
import com.anzi.credible.entity.Borrower
import com.anzi.credible.entity.Institution

interface BorrowerRepository {

    fun fetchBorrowerById(id: String): Borrower

    fun createBorrower(institution: Institution, borrowerDto: BorrowerDto): Borrower

    fun updateBorrower(borrower: Borrower): Borrower
}
