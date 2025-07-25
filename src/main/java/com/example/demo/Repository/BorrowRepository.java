package com.example.demo.Repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Model.Borrow;



public interface BorrowRepository extends JpaRepository<Borrow, Long> {
    List<Borrow> findByUserIdAndReturnedFalse(Long userId);
    List<Borrow> findByUserId(Long userId);
}
