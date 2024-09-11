package projects.dnetsova.taskmanager.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import projects.dnetsova.taskmanager.entities.User;
import projects.dnetsova.taskmanager.exceptions.DuplicateUserException;
import projects.dnetsova.taskmanager.exceptions.InvalidUserException;
import projects.dnetsova.taskmanager.models.CustomPage;
import projects.dnetsova.taskmanager.repositories.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Creates a user with the given name and attempts to save it to the database.
     * If a user with that name already exists, a DataIntegrityViolationException is thrown,
     * which is caught and rethrown as a DuplicateUserException.
     * @param name the new user name. Must not be null, empty, or consist solely of whitespace or tabs.
     * @throws DuplicateUserException thrown if a user with the given name already exists
     */
    public void addUser(String name) throws DuplicateUserException {
        try {
            User user = new User(name);
            this.userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateUserException(name);
        }
    }

    /**
     * If a user with the given name is present in the database, this user is deleted,
     * otherwise, an InvalidUserException is thrown.
     * @param name the name of the user to be deleted.
     * @throws InvalidUserException in case a user with the given name doesn't exist.
     */
    public void removeUser(String name) throws InvalidUserException {
        int deletedRows = this.userRepository.delete(name);

        if (deletedRows == 0) throw new InvalidUserException(name);
    }

    /**
     * If a user with the given name is not present in the database,
     * an InvalidUserException is thrown. Otherwise, the user is renamed
     * with the given new name. If the new name already exists,
     * a DuplicateUserException is thrown.
     * @param name the original user name to be changed.
     * @param newName the new name to which the user will be renamed.
     * @throws InvalidUserException in case a user with such a name doesn't exist.
     * @throws DuplicateUserException if the newName is already taken by another user.
     */
    public void updateUser(String name, String newName) throws InvalidUserException, DuplicateUserException {
        int updatedRows = 0;

        try {
            updatedRows = this.userRepository.updateUserName(name, newName);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateUserException(newName);
        }

        if (updatedRows == 0) throw new InvalidUserException(name);
    }

    /**
     * Retrieves a Page of usernames according to
     * the given pagination information - page and size.
     * @param page The page index. The first page is 1.
     * @param size The number of items per page.
     * @return CustomPage<String> - object which specifies and simplifies
     * pagination information. Contains List of username String objects,
     * number of total pages and number of total elements.
     * @throws IllegalArgumentException when page and/or size are not positive.
     */
    public CustomPage<String> getAllUsers(int page, int size) throws IllegalArgumentException {
        if (page <= 0) throw new IllegalArgumentException("Page must be greater than 0");
        if (size <= 0) throw new IllegalArgumentException("Size must be greater than 0");

        Page<String> p = this.userRepository.findAllUserNames(PageRequest.of(page - 1, size));
      return new CustomPage<>(p.getContent(), p.getTotalPages(), p.getTotalElements());
    }
}
