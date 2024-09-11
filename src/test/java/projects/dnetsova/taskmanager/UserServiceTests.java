package projects.dnetsova.taskmanager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import projects.dnetsova.taskmanager.entities.User;
import projects.dnetsova.taskmanager.exceptions.DuplicateUserException;
import projects.dnetsova.taskmanager.exceptions.InvalidUserException;
import projects.dnetsova.taskmanager.models.CustomPage;
import projects.dnetsova.taskmanager.repositories.UserRepository;
import projects.dnetsova.taskmanager.services.UserService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class UserServiceTests {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;


    private final String name = "user";
    private final String newName = "newUser";
    private final User user = new User(name);

    @Test
    public void addUserMustThrowDuplicateUserExceptionWhenUserNameExists() {
        Mockito.when(userRepository.saveAndFlush(this.user)).thenThrow(DataIntegrityViolationException.class);
        Assertions.assertThrows(DuplicateUserException.class, () -> userService.addUser(this.name),
                "Expected DuplicateUserException to be thrown when adding a user with an existing name.");
        verify(userRepository).saveAndFlush(this.user);
    }

    @Test
    public void addUserMustSaveUserInTheDatabase() throws DuplicateUserException {
        userService.addUser(this.name);
        verify(userRepository).saveAndFlush(this.user);

    }

    @Test
    public void removeUserMustThrowInvalidUserExceptionWhenUserNameDoesNotExist() {
        Mockito.when(userRepository.delete(this.name)).thenReturn(0);
        Assertions.assertThrows(InvalidUserException.class, () -> userService.removeUser(this.name),
                "Expected InvalidUserException to be thrown when removing a user with a non-existing name.");
        verify(userRepository).delete(this.name);
    }

    @Test
    public void removeUserMustDeleteTheUserByName() throws InvalidUserException {
       Mockito.when(userRepository.delete(this.name)).thenReturn(1);
        userService.removeUser(this.name);
        verify(userRepository).delete(this.name);
    }

    @Test
    public void updateUserMustThrowInvalidUserExceptionWhenUserNameDoesNotExist() {
        Mockito.when(userRepository.updateUserName(this.name, newName)).thenReturn(0);
        Assertions.assertThrows(InvalidUserException.class, ()  -> userService.updateUser(this.name, this.newName),
                "Expected InvalidUserException to be thrown when updating a user with a non-existing name.");
        verify(userRepository).updateUserName(this.name, newName);
    }

    @Test
    public void updateUserMustThrowDuplicateUserExceptionWhenTheGivenNewUserNameIsTaken() {
        Mockito.when(userRepository.updateUserName(this.name, newName)).thenThrow(DataIntegrityViolationException.class);
        Assertions.assertThrows(DuplicateUserException.class, () -> userService.updateUser(this.name, this.newName),
                "Expected DuplicateUserException to be thrown when updating a user with an existing name.");
        verify(userRepository).updateUserName(this.name, newName);
    }

    @Test
    public void updateUserMustFindTheUserByNameAndUpdateItWithTheNewName() throws InvalidUserException, DuplicateUserException {
        Mockito.when(userRepository.updateUserName(this.name, newName)).thenReturn(1);
                userService.updateUser(this.name, newName);
                verify(userRepository).updateUserName(this.name, newName);
    }
    @Test
    public void getAllUsersMustThrowIllegalArgumentExceptionWhenPageIsNotPositive() {
        int page = 0;
        int size = 10;

        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.getAllUsers(page, size),
                "Expected IllegalArgumentException to be thrown when page is negative or zero.");
        verify(userRepository, never()).findAllUserNames(any(PageRequest.class));
    }

    @Test
    public void getAllUsersMustThrowIllegalArgumentExceptionWhenSizeIsNotPositive() {
        int page = 2;
        int size = -2;

        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.getAllUsers(page, size),
                "Expected IllegalArgumentException to be thrown when size is negative or zero.");
        verify(userRepository, never()).findAllUserNames(any(PageRequest.class));
    }

    @Test
    public void getAllUsersMustReturnCustomPageOfAllUsers() {
        int page = 2;
        int size = 3;

        List<String> users = Arrays.asList("user", "user2", "user3");
        Page<String> userPage = new PageImpl<>(users, PageRequest.of(page - 1, size), size);

        Mockito.when(userRepository.findAllUserNames(PageRequest.of(page - 1, size))).thenReturn(userPage);

        CustomPage<String> resultCustomPage = userService.getAllUsers(page, size);

        Assertions.assertNotNull(resultCustomPage);
        Assertions.assertEquals(userPage.getTotalElements(), resultCustomPage.totalElementsCount());
        Assertions.assertEquals(userPage.getTotalPages(), resultCustomPage.totalPageCount());
        Assertions.assertEquals("user", resultCustomPage.elements().get(0));
        Assertions.assertEquals("user2", resultCustomPage.elements().get(1));
        Assertions.assertEquals("user3", resultCustomPage.elements().get(2));
    }
}
