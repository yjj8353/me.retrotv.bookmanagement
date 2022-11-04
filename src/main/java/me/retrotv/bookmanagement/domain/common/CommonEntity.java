package me.retrotv.bookmanagement.domain.common;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class CommonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @CreationTimestamp
    @Column(name = "REGIST_DATE", nullable = false)
    private LocalDateTime registDate;

    @UpdateTimestamp
    @Column(name = "UPDATE_DATE", nullable = false)
    private LocalDateTime updateDate;
}
