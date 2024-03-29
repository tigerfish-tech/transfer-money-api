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
    MatcherAssert.assertThat("Check user id",
        result.getId(), is(notNullValue()));
    MatcherAssert.assertThat("Check user name",
        result.getFullName(), is(newUser.getFullName()));

    verify(userDao).insert(any());
    verify(userDao, never()).getById(any());
    verify(userDao, never()).update(any());
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
    MatcherAssert.assertThat("Check user id",
        updatedUser.getId(), is(userDaoEntity.getId()));
    MatcherAssert.assertThat("Check user name",
        updatedUser.getFullName(), is(userDaoEntity.getFullName()));

    verify(userDao).getById(any());
    verify(userDao).update(any());
    verify(userDao, never()).insert(any());
  }

  //Create/update user with empty name
  @Test(expected = IllegalArgumentException.class)
  public void updateUserEmptyNameExceptionTest() {
    User user = User.builder().build();
    userService.save(user);

    verify(userDao, never()).update(any());
    verify(userDao, never()).getById(any());
    verify(userDao, never()).insert(any());
  }

  //Update NULL user
  @Test(expected = IllegalArgumentException.class)
  public void updateNullUserExceptionTest() {
    User user = User.builder()
        .id(UUID.randomUUID().toString())
        .fullName("John Smith").build();

    given(userDao.getById(user.getId())).willReturn(null);

    userService.save(user);

    verify(userDao).getById(any());
    verify(userDao, never()).update(any());
    verify(userDao, never()).insert(any());
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

    verify(userDao).isExist(any());
    verify(userDao).getById(any());
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

    verify(userDao).isExist(any());
    verify(userDao).deleteById(argumentCaptor.capture());
    MatcherAssert.assertThat("Check param", argumentCaptor.getValue(), is(id));
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteUserByIdExceptionTest() {
    String id = UUID.randomUUID().toString();
    given(userDao.isExist(id)).willReturn(false);

    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

    userService.delete(id);

    verify(userDao).isExist(argumentCaptor.capture());
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

    given(userDao.findAll(10, 0)).willReturn(users);
    MatcherAssert.assertThat("Check user list",
        userService.findAll(10, 0), hasSize(10));
    verify(userDao).findAll(10, 0);
  }

  @Test
  public void findAllDefaultValuesSuccessTest() {
    List<UserDaoEntity> users = IntStream.range(0, 10)
        .boxed()
        .map(number -> {
          UserDaoEntity entity = new UserDaoEntity();
          entity.setId(UUID.randomUUID().toString());
          entity.setFullName("User " + number);
          return entity;
        })
        .collect(Collectors.toList());

    given(userDao.findAll(Integer.MAX_VALUE, 0)).willReturn(users);
    MatcherAssert.assertThat("Check user list",
        userService.findAll(), hasSize(10));
    verify(userDao).findAll(any(), any());
  }

  @Test
  public void isExistPositiveTest() {
    String id = UUID.randomUUID().toString();

    given(userDao.isExist(id)).willReturn(true);

    MatcherAssert.assertThat("Check user id",
        userService.exists(id), is(true));
    verify(userDao).isExist(any());
  }

}
