package org.example.models;

import jakarta.persistence.*;
import lombok.*;
import org.example.enums.BotRoles;

@Getter
@Setter
@Entity
@Table(name = "groups")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long chatId;
    private String tgGroupName;

    @Enumerated(EnumType.STRING)
    private BotRoles tgRole;

    private int usersCount;

//    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "groups")
//    private List<User> tgUsers;
}
