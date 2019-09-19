package com.fintech.service;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fintech.dao.UserDao;
import com.fintech.models.User;
import com.fintech.services.UserService;
import com.fintech.services.impl.DefaultUserService;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTests {

  private UserService userService;
  private UserDao<User, String> userDao;

  @Before
  public void startUp() {
    userDao = mock(UserDao.class);
    userService = new DefaultUserService(userDao);
  }

  //Create new user and check if id is not null
  @Test
  public void createNewUserSuccessTest() {
    User persistedUser = User.builder()
        .id(UUID.randomUUID().toString())
        .fullName("John Smith").build();

    given(userDao.insert(any())).willReturn(persistedUser);

    User newUser = User.builder().fullName("John Smith").build();

    User result = userService.save(newUser);

    verify(userDao, times(1)).insert(any());
    MatcherAssert.assertThat("Check user id",
        result.getId(), is(notNullValue()));
    MatcherAssert.assertThat("Check user name",
        result.getFullName(), is(newUser.getFullName()));
  }

  //Create user and than update fullName
  @Test
  public void updateExistUserSuccessTest() {
    String id = UUID.randomUUID().toString();
    User updateUser = User.builder().id(id).fullName("John Snow").build();

    given(userDao.update(updateUser)).willReturn(updateUser);

    User updatedUser = userService.save(updateUser);

    verify(userDao, times(1)).update(any());
    MatcherAssert.assertThat("Check user id",
        updatedUser.getId(), is(updateUser.getId()));
    MatcherAssert.assertThat("Check user name",
        updatedUser.getFullName(), is(updateUser.getFullName()));
  }

  //Create/update user with empty name
  @Test(expected = InvalidParameterException.class)
  public void updateUserEmptyNameExceptionTest() {
    User user = User.builder().build();
    userService.save(user);

    verify(userDao, never()).update(any());
  }

  //Get user by id
  @Test
  public void getUserByIdSuccessTest() {
    String id = UUID.randomUUID().toString();
    User user = User.builder().id(id).fullName("John Smith").build();

    given(userDao.isExist(id)).willReturn(true);
    given(userDao.getById(id)).willReturn(user);

    User result = userService.getById(id);

    verify(userDao, times(1)).isExist(any());
    verify(userDao, times(1)).getById(any());
    MatcherAssert.assertThat("Check user id",
        result.getId(), is(user.getId()));
    MatcherAssert.assertThat("Check user name",
        result.getFullName(), is(user.getFullName()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void getUserByIdFailedTest() {
    userService.getById(UUID.randomUUID().toString());
    verify(userDao, times(1)).isExist(any());
    verify(userDao, never()).getById(any());
  }

  //Delete user by id
  @Test
  public void deleteUserByIdSuccessTest() {
    String id = UUID.randomUUID().toString();
    given(userDao.isExist(id)).willReturn(true);

    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

    userService.delete(id);

    verify(userDao, times(1)).isExist(any());
    verify(userDao, times(1)).deleteById(argumentCaptor.capture());
    MatcherAssert.assertThat("Check param", argumentCaptor.getValue(), is(id));
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteUserByIdFailedTest() {
    String id = UUID.randomUUID().toString();
    given(userDao.isExist(id)).willReturn(false);

    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

    userService.delete(id);

    verify(userDao, times(1)).isExist(argumentCaptor.capture());
    verify(userDao, never()).deleteById(any());
    MatcherAssert.assertThat("Check param", argumentCaptor.getValue(), is(id));
  }

  @Test
  public void findAllSuccessTest() {
    List<User> users = IntStream.range(0, 10)
        .boxed()
        .map(number -> User.builder().fullName("User " + number).build())
        .collect(Collectors.toList());

    given(userDao.findAll()).willReturn(users);
    MatcherAssert.assertThat("Check user list", userService.findAll(), hasSize(10));
    verify(userDao, times(1)).findAll();
  }

  @Test
  public void isExistPositiveTest() {
    String id = UUID.randomUUID().toString();

    given(userDao.isExist(id)).willReturn(true);

    MatcherAssert.assertThat("Check user id",
        userService.isUserExists(id), is(true));
    verify(userDao, times(1)).isExist(any());
  }

}
