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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
     * Deletes a user with the given ID from the database.
     * If no rows were modified (user doesn't exist), an InvalidUserException is thrown.
     * @param id the ID of the user to be deleted.
     * @throws InvalidUserException in case a user with the given ID doesn't exist.
     */
    public void removeUser(UUID id) throws InvalidUserException {
        int deletedRows = this.userRepository.deleteUserById(id);
        
        if (deletedRows == 0) {
            throw new InvalidUserException(id);
        }
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
     * Retrieves a Page of users according to
     * the given pagination information - page and size.
     * @param page The page index. The first page is 1.
     * @param size The number of items per page.
     * @return CustomPage<User> - object which specifies and simplifies
     * pagination information. Contains List of User objects with id and name,
     * number of total pages and number of total elements.
     * @throws IllegalArgumentException when page and/or size are not positive.
     */
    public CustomPage<projects.dnetsova.taskmanager.models.User> getAllUsers(int page, int size) throws IllegalArgumentException {
        if (page <= 0) throw new IllegalArgumentException("Page must be greater than 0");
        if (size <= 0) throw new IllegalArgumentException("Size must be greater than 0");

        Page<User> p = this.userRepository.findAll(PageRequest.of(page - 1, size));
        List<projects.dnetsova.taskmanager.models.User> userModels = p.getContent().stream()
                .map(user -> new projects.dnetsova.taskmanager.models.User(user.getId(), user.getName()))
                .collect(Collectors.toList());
        return new CustomPage<>(userModels, p.getTotalPages(), p.getTotalElements());
    }
}
