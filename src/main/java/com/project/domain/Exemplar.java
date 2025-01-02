package com.project.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "exemplars")
public class Exemplar implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long exemplarId;

    @Column(nullable = false, unique = true)
    private String codiBarres;

    @ManyToOne
    @JoinColumn(name = "llibre_id", nullable = false)
    private Llibre llibre;

    @ManyToOne
    @JoinColumn(name = "biblioteca_id", nullable = false)
    private Biblioteca biblioteca;

    private boolean disponible = true;

    @OneToMany(mappedBy = "exemplar", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Prestec> historialPrestecs = new HashSet<>();

    // Constructors
    public Exemplar() {}

    public Exemplar(String codiBarres, Llibre llibre, Biblioteca biblioteca) {
        this.codiBarres = codiBarres;
        this.llibre = llibre;
        this.biblioteca = biblioteca;
    }

    // Getters and Setters
    public long getExemplarId() {
        return exemplarId;
    }

    public void setExemplarId(long exemplarId) {
        this.exemplarId = exemplarId;
    }

    public String getCodiBarres() {
        return codiBarres;
    }

    public void setCodiBarres(String codiBarres) {
        this.codiBarres = codiBarres;
    }

    public Llibre getLlibre() {
        return llibre;
    }

    public void setLlibre(Llibre llibre) {
        this.llibre = llibre;
    }

    public Biblioteca getBiblioteca() {
        return biblioteca;
    }

    public void setBiblioteca(Biblioteca biblioteca) {
        this.biblioteca = biblioteca;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public Set<Prestec> getHistorialPrestecs() {
        return historialPrestecs;
    }

    public void setHistorialPrestecs(Set<Prestec> historialPrestecs) {
        this.historialPrestecs = historialPrestecs;
    }

    // Helper methods
    public void addPrestec(Prestec prestec) {
        historialPrestecs.add(prestec);
        prestec.setExemplar(this);
        this.disponible = false;
    }

    public void removePrestec(Prestec prestec) {
        historialPrestecs.remove(prestec);
        prestec.setExemplar(null);
        this.disponible = true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Exemplar[id=%d, codi='%s', disponible=%s", 
            exemplarId, codiBarres, disponible));
        
        if (llibre != null) {
            sb.append(String.format(", llibre='%s'", llibre.getTitol()));
        }
        
        if (biblioteca != null) {
            sb.append(String.format(", biblioteca='%s'", biblioteca.getNom()));
        }
        
        if (!historialPrestecs.isEmpty()) {
            long prestectsActius = historialPrestecs.stream()
                .filter(p -> p.isActiu())
                .count();
            sb.append(String.format(", prestecs=%d (actius=%d)", 
                historialPrestecs.size(), prestectsActius));
        }
        
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exemplar exemplar = (Exemplar) o;
        return exemplarId == exemplar.exemplarId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(exemplarId);
    }
}