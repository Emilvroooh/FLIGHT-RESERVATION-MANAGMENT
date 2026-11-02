package com.skyward.repository;
import com.skyward.entity.AirlineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AirlineRepository extends JpaRepository<AirlineEntity,Long> {}
