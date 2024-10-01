package org.simple4j.wsfeeler.test.ws;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import spark.Spark;

public class UserWS
{

	private static Logger logger = LoggerFactory.getLogger(UserWS.class);
	private static ApplicationContext ac;

	public static void start()
	{
		ac = new ClassPathXmlApplicationContext("ws/main-appCntxt.xml");
		UserDAO userDAO = (UserDAO) ac.getBean("userDAO");
		
		try
		{
			//below line is needed for this sample WS only
			userDAO.createUserTable();
		}
		catch(Throwable t)
		{
			logger.warn("Probably the table exists already", t);
		}
		ObjectMapper om = new ObjectMapper();

		Spark.port(2001);
		Spark.get("/user/:userPK", (req, res) ->
			{
				res.header("Content-Type", "application/JSON");
				String userPK = req.params(":userPK");
				if (userPK == null || userPK.trim().length() < 1)
				{
					res.status(412);
					return "{\"errorCodes\" : [\"userPK-required\"]}";
				}
				if (userPK.trim().length() > 40)
				{
					res.status(412);
					return "{\"errorCodes\" : [\"userPK-maxlength\"]}";
				}
				UserVO user = userDAO.getUser(userPK);
				res.status(200);
				return om.writeValueAsString(user);
			});

		Spark.put("/user", (req, res) ->
			{
				logger.info("inside user put");
				if (req.contentLength() > 2048)
					throw new RuntimeException("content too large");
				res.header("Content-Type", "application/JSON");
				String bodyStr = req.body();
				UserVO userVO = om.readValue(bodyStr, UserVO.class);

				// input validation start
				StringBuilder sb = validateUserVO(userVO);
				if (sb.length() > 0)
				{
					res.status(412);
					return sb.toString();
				}
				// input validation end

				userVO.userPK = UUID.randomUUID().toString();
				userDAO.insertUser(userVO);
				res.status(200);
				return "{\"userPK\" : \""+userVO.userPK+"\"}";
			});

		Spark.post("/user", (req, res) ->
			{
				logger.info("inside user post");
				if (req.contentLength() > 2048)
					throw new RuntimeException("content too large");
				res.header("Content-Type", "application/JSON");
				String bodyStr = req.body();
				UserVO userVO = om.readValue(bodyStr, UserVO.class);

				// input validation start
				StringBuilder sb = validateUserVO(userVO);
				if (sb.length() > 0)
				{
					res.status(412);
					return sb.toString();
				}
				// input validation end

				userDAO.updateUser(userVO);
				res.status(200);
				return "{}";
			});

	}

	private static StringBuilder validateUserVO(UserVO userVO)
	{
		StringBuilder sb = new StringBuilder();
		String delim = "";
		if (userVO.displayName == null || userVO.displayName.trim().length() < 1)
		{
			sb.append(delim).append("displayName-required");
			delim = ",";
		}
		if (userVO.displayName.trim().length() > 20)
		{
			sb.append(delim).append("displayName-maxlength");
			delim = ",";
		}
		if (userVO.gender == null || userVO.gender.trim().length() < 1)
		{
			sb.append(delim).append("gender-required");
			delim = ",";
		}
		if (!userVO.gender.equalsIgnoreCase("F") && !userVO.gender.equalsIgnoreCase("M")
				&& !userVO.gender.equalsIgnoreCase("O"))
		{
			sb.append(delim).append("gender-invalid");
			delim = ",";
		}
		if (userVO.birthMonth != null && (userVO.birthMonth < 1 || userVO.birthMonth > 12))
		{
			sb.append(delim).append("birthMonth-invalid");
			delim = ",";
		}
		return sb;
	}

	public static void stop()
	{

	}
}
