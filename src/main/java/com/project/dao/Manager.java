package com.project.dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import com.project.domain.*;

public class Manager {
    private static SessionFactory factory;

    /**
     * Crea la SessionFactory per defecte
     */
    public static void createSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            
            // Registrem totes les classes que tenen anotacions JPA
            configuration.addAnnotatedClass(Biblioteca.class);
            configuration.addAnnotatedClass(Llibre.class);
            configuration.addAnnotatedClass(Exemplar.class);
            configuration.addAnnotatedClass(Prestec.class);
            configuration.addAnnotatedClass(Persona.class);
            configuration.addAnnotatedClass(Autor.class);

            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();
                
            factory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) {
            System.err.println("No s'ha pogut crear la SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Crea la SessionFactory amb un fitxer de propietats específic
     */
    public static void createSessionFactory(String propertiesFileName) {
        try {
            Configuration configuration = new Configuration();
            
            configuration.addAnnotatedClass(Biblioteca.class);
            configuration.addAnnotatedClass(Llibre.class);
            configuration.addAnnotatedClass(Exemplar.class);
            configuration.addAnnotatedClass(Prestec.class);
            configuration.addAnnotatedClass(Persona.class);
            configuration.addAnnotatedClass(Autor.class);

            Properties properties = new Properties();
            try (InputStream input = Manager.class.getClassLoader().getResourceAsStream(propertiesFileName)) {
                if (input == null) {
                    throw new IOException("No s'ha trobat " + propertiesFileName);
                }
                properties.load(input);
            }

            configuration.addProperties(properties);

            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();
                
            factory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) {
            System.err.println("Error creant la SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Tanca la SessionFactory
     */
    public static void close() {
        if (factory != null) {
            factory.close();
        }
    }

    // Mètodes genèrics CRUD
    public static <T> T save(T entity) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.persist(entity);
            tx.commit();
            return entity;
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public static <T> T update(T entity) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.merge(entity);
            tx.commit();
            return entity;
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public static <T> void delete(T entity) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.remove(entity);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public static <T> T get(Class<T> clazz, Serializable id) {
        Session session = factory.openSession();
        try {
            return session.get(clazz, id);
        } finally {
            session.close();
        }
    }

    // Mètodes específics per a la gestió de la biblioteca
    public static List<Biblioteca> getAllBiblioteques() {
        Session session = factory.openSession();
        try {
            return session.createQuery("from Biblioteca", Biblioteca.class).list();
        } finally {
            session.close();
        }
    }

    public static List<Llibre> getAllLlibres() {
        Session session = factory.openSession();
        try {
            return session.createQuery("from Llibre", Llibre.class).list();
        } finally {
            session.close();
        }
    }

    public static List<Persona> getAllPersones() {
        Session session = factory.openSession();
        try {
            return session.createQuery("from Persona", Persona.class).list();
        } finally {
            session.close();
        }
    }

    public static List<Autor> getAllAutors() {
        Session session = factory.openSession();
        try {
            return session.createQuery("from Autor", Autor.class).list();
        } finally {
            session.close();
        }
    }

    public static List<Prestec> getPrestecsActius() {
        Session session = factory.openSession();
        try {
            return session.createQuery("from Prestec p where p.actiu = true", Prestec.class).list();
        } finally {
            session.close();
        }
    }

    public static List<Prestec> getPrestecsRetardats() {
        Session session = factory.openSession();
        try {
            LocalDate avui = LocalDate.now();
            return session.createQuery(
                "from Prestec p where p.actiu = true and p.dataRetornPrevista < :avui", 
                Prestec.class)
                .setParameter("avui", avui)
                .list();
        } finally {
            session.close();
        }
    }

    public static List<Exemplar> getExemplarsDisponibles() {
        Session session = factory.openSession();
        try {
            List<Exemplar> exemplars = session.createQuery("from Exemplar e where e.disponible = true", Exemplar.class).list();
            exemplars.forEach(exemplar -> Hibernate.initialize(exemplar.getHistorialPrestecs()));
            return exemplars;
        } finally {
            session.close();
        }
    }

    public static Prestec ferPrestec(Exemplar exemplar, Persona persona, LocalDate dataPrestec, LocalDate dataRetornPrevista) {
        if (!exemplar.isDisponible()) {
            throw new IllegalStateException("L'exemplar no està disponible");
        }

        Prestec prestec = new Prestec(exemplar, persona, dataPrestec, dataRetornPrevista);
        exemplar.addPrestec(prestec);
        persona.addPrestec(prestec);

        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.persist(prestec);
            session.merge(exemplar);
            session.merge(persona);
            tx.commit();
            return prestec;
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public static void retornarPrestec(Prestec prestec, LocalDate dataRetornReal) {
        if (!prestec.isActiu()) {
            throw new IllegalStateException("El préstec ja ha estat retornat");
        }

        prestec.setDataRetornReal(dataRetornReal);
        prestec.setActiu(false);
        prestec.getExemplar().setDisponible(true);

        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.merge(prestec);
            session.merge(prestec.getExemplar());
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public static List<Llibre> cercarLlibrePerTitol(String titol) {
        Session session = factory.openSession();
        try {
            List<Llibre> llibres = session.createQuery(
                "from Llibre l where lower(l.titol) like lower(:titol)", 
                Llibre.class)
                .setParameter("titol", "%" + titol + "%")
                .list();
            llibres.forEach(llibre -> {
                Hibernate.initialize(llibre.getAutors());
                Hibernate.initialize(llibre.getExemplars());
            });
            return llibres;
        } finally {
            session.close();
        }
    }

    public static List<Llibre> cercarLlibrePerAutor(String nomAutor) {
        Session session = factory.openSession();
        try {
            List<Llibre> llibres =  session.createQuery(
                "select distinct l from Llibre l join l.autors a where lower(a.nom) like lower(:nom)", 
                Llibre.class)
                .setParameter("nom", "%" + nomAutor + "%")
                .list();
            llibres.forEach(llibre -> {
                Hibernate.initialize(llibre.getAutors());
                Hibernate.initialize(llibre.getExemplars());
            });
            return llibres;
        } finally {
            session.close();
        }
    }

    public static List<Prestec> getHistorialPrestecs(Persona persona) {
        Session session = factory.openSession();
        try {
            return session.createQuery(
                "from Prestec p where p.persona = :persona order by p.dataPrestec desc", 
                Prestec.class)
                .setParameter("persona", persona)
                .list();
        } finally {
            session.close();
        }
    }
}
