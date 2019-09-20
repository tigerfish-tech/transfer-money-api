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
import com.fintech.models.dao.UserDaoEntity;
import com.fintech.services.UserService;
import com.fintech.services.impl.DefaultUserService;
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
  private UserDao<UserDaoEntity, String> userDao;

  @Before
  public void startUp() {
    userDao = mock(UserDao.class);
    userService = new DefaultUserService(userDao);
  }

  //Create new user and check if id is not null
  @Test
  public void createNewUserSuccessTest() {
    UserDaoEntity userDaoEntity = new UserDaoEntity();
    userDaoEntity.setId(UUID.randomUUID().toString());
    userDaoEntity.setFullName("John Smith");

    given(userDao.insert(any())).willReturn(userDaoEntity);

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

    UserDaoEntity userDaoEntity = new UserDaoEntity();
    userDaoEntity.setId(id);
    userDaoEntity.setFullName("John Smith");

    given(userDao.update(userDaoEntity)).willReturn(userDaoEntity);
    given(userDao.getById(id)).willReturn(userDaoEntity);

    User updateInfo = User.builder().id(id).fullName("John Smith").build();

    User updatedUser = userService.save(updateInfo);

    verify(userDao, times(1)).update(any());
    MatcherAssert.assertThat("Check user id",
        updatedUser.getId(), is(userDaoEntity.getId()));
    MatcherAssert.assertThat("Check user name",
        updatedUser.getFullName(), is(userDaoEntity.getFullName()));
  }

  //Create/update user with empty name
  @Test(expected = IllegalArgumentException.class)
  public void updateUserEmptyNameExceptionTest() {
    User user = User.builder().build();
    userService.save(user);

    verify(userDao, never()).update(any());
  }

  //Get user by id
  @Test
  public void getUserByIdSuccessTest() {
    String id = UUID.randomUUID().toString();
    UserDaoEntity entity = new UserDaoEntity();
    entity.setId(id);
    entity.setFullName("John Smith");

    given(userDao.isExist(id)).willReturn(true);
    given(userDao.getById(id)).willReturn(entity);

    User result = userService.getById(id);

    verify(userDao, times(1)).isExist(any());
    verify(userDao, times(1)).getById(any());
    MatcherAssert.assertThat("Check user id",
        result.getId(), is(entity.getId()));
    MatcherAssert.assertThat("Check user name",
        result.getFullName(), is(entity.getFullName()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void getUserByIdFailedTest() {
    String id = UUID.randomUUID().toString();

    userService.getById(id);

    given(userDao.isExist(id)).willReturn(false);

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
  public void deleteUserByIdExceptionTest() {
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
    List<UserDaoEntity> users = IntStream.range(0, 10)
        .boxed()
        .map(number -> {
          UserDaoEntity entity = new UserDaoEntity();
          entity.setId(UUID.randomUUID().toString());
          entity.setFullName("User " + number);
          return entity;
        })
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
        userService.exists(id), is(true));
    verify(userDao, times(1)).isExist(any());
  }

}
