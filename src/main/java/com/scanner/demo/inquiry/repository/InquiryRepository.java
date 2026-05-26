package com.scanner.demo.inquiry.repository;

import com.scanner.demo.inquiry.entity.Inquiry;
import com.scanner.demo.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Integer> {

    // 특정 유저의 문의 목록만 페이징 처리하여 조회
    Page<Inquiry> findAllByUser(User user, Pageable pageable);

}