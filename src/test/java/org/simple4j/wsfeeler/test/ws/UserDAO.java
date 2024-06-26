package org.simple4j.wsfeeler.test.ws;


public interface UserDAO {

	public void createUserTable();
	public void insertUser(UserVO userVO);
	public boolean updateUser(UserVO userVO);
	public UserVO getUser(String userPK);
}
