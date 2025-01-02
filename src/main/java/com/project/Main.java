package com.project;

import com.project.dao.Manager;
import com.project.domain.*;
import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // Inicialitzar Hibernate
            Manager.createSessionFactory();
            System.out.println("\nHibernate iniciat\n");
            // Crear una biblioteca
            Biblioteca biblioteca = new Biblioteca("Biblioteca Central", "Barcelona");
            biblioteca.setAdreca("Carrer Major, 1");
            biblioteca.setTelefon("93 123 45 67");
            biblioteca.setEmail("biblioteca.central@bcn.cat");
            Manager.save(biblioteca);
            System.out.println("Biblioteca creada: " + biblioteca);

            // Crear autors
            Autor autor1 = new Autor("Gabriel García Márquez");
            Autor autor2 = new Autor("Isabel Allende");
            Manager.save(autor1);
            Manager.save(autor2);
            System.out.println("Autors creats: " + autor1 + ", " + autor2);

            // Crear llibres
            Llibre llibre1 = new Llibre("978-84-376-0494-7", "Cien años de soledad");
            llibre1.setEditorial("Editorial Sudamericana");
            llibre1.setAnyPublicacio(1967);
            llibre1.addAutor(autor1);

            Llibre llibre2 = new Llibre("978-84-01-34161-8", "La casa de los espíritus");
            llibre2.setEditorial("Plaza & Janés");
            llibre2.setAnyPublicacio(1982);
            llibre2.addAutor(autor2);

            Manager.save(llibre1);
            Manager.save(llibre2);
            System.out.println("Llibres creats: " + llibre1 + ", " + llibre2);

            // Crear exemplars
            Exemplar exemplar1 = new Exemplar("EX001", llibre1, biblioteca);
            Exemplar exemplar2 = new Exemplar("EX002", llibre1, biblioteca);
            Exemplar exemplar3 = new Exemplar("EX003", llibre2, biblioteca);

            Manager.save(exemplar1);
            Manager.save(exemplar2);
            Manager.save(exemplar3);
            System.out.println("Exemplars creats: " + exemplar1 + ", " + exemplar2 + ", " + exemplar3);

            // Crear persones
            Persona persona1 = new Persona("12345678A", "Joan Garcia");
            persona1.setTelefon("666 777 888");
            persona1.setEmail("joan.garcia@email.com");

            Persona persona2 = new Persona("87654321B", "Maria Martínez");
            persona2.setTelefon("999 888 777");
            persona2.setEmail("maria.martinez@email.com");

            Manager.save(persona1);
            Manager.save(persona2);
            System.out.println("Persones creades: " + persona1 + ", " + persona2);

            // Fer préstecs
            LocalDate avui = LocalDate.now();
            LocalDate retornPrevist1 = avui.plusDays(15);
            LocalDate retornPrevist2 = avui.plusDays(15);

            Prestec prestec1 = Manager.ferPrestec(exemplar1, persona1, avui, retornPrevist1);
            System.out.println("Préstec realitzat: " + prestec1);

            Prestec prestec2 = Manager.ferPrestec(exemplar3, persona2, avui, retornPrevist2);
            System.out.println("Préstec realitzat: " + prestec2);

            // Mostrar exemplars disponibles
            System.out.println("\nExemplars disponibles:");
            List<Exemplar> exemplarsDisponibles = Manager.getExemplarsDisponibles();
            exemplarsDisponibles.forEach(System.out::println);

            // Cercar llibres per títol
            System.out.println("\nCerca de llibres que contenen 'sol':");
            List<Llibre> llibresTrobats = Manager.cercarLlibrePerTitol("sol");
            llibresTrobats.forEach(System.out::println);

            // Cercar llibres per autor
            System.out.println("\nCerca de llibres de 'García Márquez':");
            List<Llibre> llibresAutor = Manager.cercarLlibrePerAutor("García Márquez");
            llibresAutor.forEach(System.out::println);

            // Retornar un préstec
            System.out.println("\nRetornant préstec:");
            Manager.retornarPrestec(prestec1, avui.plusDays(10));
            System.out.println("Préstec retornat: " + prestec1);

            // Mostrar préstecs actius
            System.out.println("\nPréstecs actius:");
            List<Prestec> prestecsActius = Manager.getPrestecsActius();
            prestecsActius.forEach(System.out::println);

            // Mostrar historial de préstecs d'una persona
            System.out.println("\nHistorial de préstecs de " + persona1.getNom() + ":");
            List<Prestec> historialPrestecs = Manager.getHistorialPrestecs(persona1);
            historialPrestecs.forEach(System.out::println);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Tancar Hibernate
            Manager.close();
        }
    }
}