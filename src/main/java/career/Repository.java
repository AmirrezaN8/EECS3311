package career;

import java.util.List;

/** Stage 1 CRUD contract, typed to each repository's primary entity. Missing IDs return null. */
public interface Repository<T> {
    void save(T entity);
    T findById(String id);
    List<T> findAll();
    void delete(String id);
}
