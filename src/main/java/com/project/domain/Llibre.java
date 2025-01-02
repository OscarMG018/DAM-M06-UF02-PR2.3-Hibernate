package com.project.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "llibres")
public class Llibre implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long llibreId;

    @Column(nullable = false, unique = true)
    private String isbn;

    @Column(nullable = false)
    private String titol;

    private String editorial;
    private Integer anyPublicacio;

    @ManyToMany
    @JoinTable(
        name = "llibre_autor",
        joinColumns = @JoinColumn(name = "llibre_id"),
        inverseJoinColumns = @JoinColumn(name = "autor_id")
    )
    private Set<Autor> autors = new HashSet<>();

    @OneToMany(mappedBy = "llibre", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Exemplar> exemplars = new HashSet<>();

    // Constructors
    public Llibre() {}

    public Llibre(String isbn, String titol) {
        this.isbn = isbn;
        this.titol = titol;
    }

    // Getters and Setters
    public long getLlibreId() {
        return llibreId;
    }

    public void setLlibreId(long llibreId) {
        this.llibreId = llibreId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitol() {
        return titol;
    }

    public void setTitol(String titol) {
        this.titol = titol;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    public Integer getAnyPublicacio() {
        return anyPublicacio;
    }

    public void setAnyPublicacio(Integer anyPublicacio) {
        this.anyPublicacio = anyPublicacio;
    }

    public Set<Autor> getAutors() {
        return autors;
    }

    public void setAutors(Set<Autor> autors) {
        this.autors = autors;
    }

    public Set<Exemplar> getExemplars() {
        return exemplars;
    }

    public void setExemplars(Set<Exemplar> exemplars) {
        this.exemplars = exemplars;
    }

    // Helper methods
    public void addAutor(Autor autor) {
        this.autors.add(autor);
        autor.getLlibres().add(this);
    }

    public void removeAutor(Autor autor) {
        this.autors.remove(autor);
        autor.getLlibres().remove(this);
    }

    public void addExemplar(Exemplar exemplar) {
        exemplars.add(exemplar);
        exemplar.setLlibre(this);
    }

    public void removeExemplar(Exemplar exemplar) {
        exemplars.remove(exemplar);
        exemplar.setLlibre(null);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Llibre[id=%d, isbn='%s', titol='%s'", 
            llibreId, isbn, titol));
        
        if (editorial != null) {
            sb.append(String.format(", editorial='%s'", editorial));
        }
        if (anyPublicacio != null) {
            sb.append(String.format(", any=%d", anyPublicacio));
        }
        
        if (!autors.isEmpty()) {
            sb.append(", autors={");
            boolean first = true;
            for (Autor a : autors) {
                if (!first) sb.append(", ");
                sb.append(a.getNom());
                first = false;
            }
            sb.append("}");
        }
        
        if (!exemplars.isEmpty()) {
            sb.append(", exemplars={");
            boolean first = true;
            for (Exemplar e : exemplars) {
                if (!first) sb.append(", ");
                sb.append(e.getCodiBarres());
                first = false;
            }
            sb.append("}");
        }
        
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Llibre llibre = (Llibre) o;
        return llibreId == llibre.llibreId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(llibreId);
    }
}