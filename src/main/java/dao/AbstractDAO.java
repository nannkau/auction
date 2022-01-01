package dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.transaction.TransactionManager;
import java.io.Serializable;
import java.util.List;
public abstract class AbstractDAO <T,ID extends Serializable> {
    private Class<T> clazz;

    protected EntityManager entityManager;
    private  EntityManagerFactory entityManagerFactory;
    private EntityTransaction transaction;
    public AbstractDAO() {
        this.entityManagerFactory = Persistence.createEntityManagerFactory("auction");
        this.entityManager=this.entityManagerFactory.createEntityManager();
        this.transaction=entityManager.getTransaction();
    }
    public final void setClazz(final Class<T> clazzToSet) {
        this.clazz = clazzToSet;
    }

    public T findOne(final ID id) {
        transaction.begin();
        T result = entityManager.find(clazz, id);
        transaction.commit();
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        transaction.begin();
        List<T> result=entityManager.createQuery("from " + clazz.getName()).getResultList();
        transaction.commit();
        return result;
    }

    public T create(final T entity) {
        transaction.begin();
        entityManager.persist(entity);
        entityManager.flush();
        transaction.commit();

        return entity;
    }

    public T update(final T entity) {
        transaction.begin();
        T result = entityManager.merge(entity);
        entityManager.flush();
        transaction.commit();
        return result;
    }

    public void delete(final T entity) {
        transaction.begin();
        entityManager.remove(entity);
        entityManager.flush();
        transaction.commit();

    }

    public void deleteById(final ID entityId) {
        final T entity = findOne(entityId);
        delete(entity);
    }
}
