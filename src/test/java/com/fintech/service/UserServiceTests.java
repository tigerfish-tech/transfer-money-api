package com.fintech.service;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

import com.fintech.models.User;
import com.fintech.services.UserService;
import com.fintech.services.impl.DefaultUserService;
import java.security.InvalidParameterException;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTests {

  private UserService userService;

  @Before
  public void startUp() {
    userService = new DefaultUserService();
  }

  //Create new user and check if id is not null
  @Test
  public void createNewUserSuccessTest() {
    User user = User.builder().fullName("John Smith").build();

    User persistedUser = userService.save(user);

    MatcherAssert.assertThat("Check user id",
        user.getId(), is(nullValue()));
    MatcherAssert.assertThat("Check user id",
        persistedUser.getId(), is(notNullValue()));
    MatcherAssert.assertThat("Check user name",
        persistedUser.getFullName(), is(user.getFullName()));
  }

  //Create user and than update fullName
  @Test
  public void updateExistUserSuccessTest() {
    User user = User.builder().fullName("John Smith").build();
    User persisted = userService.save(user);

    User updateUser = User.builder().id(persisted.getId()).fullName("John Snow").build();
    User updatedUser = userService.save(updateUser);

    MatcherAssert.assertThat("Check user id",
        updatedUser.getId(), is(updateUser.getId()));
    MatcherAssert.assertThat("Check user name",
        updatedUser.getFullName(), is(updateUser.getFullName()));
  }

  //Update unexisting user exception
  @Test(expected = InvalidParameterException.class)
  public void updateWrongUserExceptionTest() {
    User updateUser = User.builder().id(UUID.randomUUID().toString()).fullName("John Snow").build();
    userService.save(updateUser);
  }

  //Create/update user with empty name
  @Test(expected = InvalidParameterException.class)
  public void updateUserEmptyNameExceptionTest() {
    User user = User.builder().fullName("John Smith").build();
    User persisted = userService.save(user);

    User updateUserAttempt1 = User.builder().id(persisted.getId()).build();
    userService.save(updateUserAttempt1);

    User updateUserAttempt2 = User.builder().id(persisted.getId()).fullName("").build();
    userService.save(updateUserAttempt2);
  }

  //Get user by id
  @Test
  public void getUserByIdSuccessTest() {
    User user = User.builder().fullName("John Smith").build();
    User persisted = userService.save(user);

    User result = userService.getById(persisted.getId());

    MatcherAssert.assertThat("Check user id",
        persisted.getId(), is(result.getId()));
    MatcherAssert.assertThat("Check user name",
        persisted.getFullName(), is(result.getFullName()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void getUserByIdFailedTest() {
    userService.getById(UUID.randomUUID().toString());
  }

  //Delete user by id
  @Test
  public void deleteUserByIdSuccessTest() {
    User user = User.builder().fullName("John Smith").build();
    User persisted = userService.save(user);

    userService.delete(persisted.getId());
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteUserByIdFailedTest() {
    userService.delete(UUID.randomUUID().toString());
  }

}
