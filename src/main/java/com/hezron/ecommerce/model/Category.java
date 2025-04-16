package com.hezron.ecommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(unique = true, length = 100)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private Set<Category> subcategories = new HashSet<>();

    // Helper methods for bidirectional relationship management
    public void addSubcategory(Category subcategory) {
        subcategories.add(subcategory);
        subcategory.setParent(this);
    }

    public void removeSubcategory(Category subcategory) {
        subcategories.remove(subcategory);
        subcategory.setParent(null);
    }
}