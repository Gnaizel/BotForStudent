package org.example.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "groups")
@NoArgsConstructor
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long tgGroupId;
    private String tgGroupName;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "groups")
    private List<User> tgUsers;
}
