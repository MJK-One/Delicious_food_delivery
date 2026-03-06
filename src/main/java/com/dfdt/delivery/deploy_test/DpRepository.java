package com.dfdt.delivery.deploy_test;

import com.dfdt.delivery.deploy_test.entity.Dp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DpRepository extends JpaRepository<Dp,Long> {
}