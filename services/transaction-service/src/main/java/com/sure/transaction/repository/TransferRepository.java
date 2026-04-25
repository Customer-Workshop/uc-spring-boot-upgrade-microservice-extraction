package com.sure.transaction.repository;

import com.sure.transaction.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<Transfer, String> {}
