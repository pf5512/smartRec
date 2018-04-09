package com.thousandsunny.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在9/9/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Entity
@Table(name = "core_app_visited_history")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AppVisitedHistory {

    private Long id;
    private String identifier;

    @Id
    @GeneratedValue(strategy = AUTO)
    @JsonIgnore
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
