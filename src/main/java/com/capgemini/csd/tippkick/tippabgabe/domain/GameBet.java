package com.capgemini.csd.tippkick.tippabgabe.domain;

import lombok.*;
import org.hibernate.annotations.Immutable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
public class GameBet {

    @Id
    @GeneratedValue
    private long id;
    @NotNull
    @CreatedDate
    private Instant created;

    private long matchId;
    private long ownerId;
    @Min(0)
    private int hometeamScore;
    @Min(0)
    private int foreignteamScore;

}
