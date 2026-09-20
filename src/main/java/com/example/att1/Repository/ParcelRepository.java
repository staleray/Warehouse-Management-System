package com.example.att1.Repository;

import com.example.att1.Entity.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {

    @Query("select p from Parcel p join fetch p.order")
    List<Parcel> findAllWithOrder();
}

