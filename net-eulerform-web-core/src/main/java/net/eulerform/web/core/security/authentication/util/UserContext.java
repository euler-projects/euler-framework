package net.eulerform.web.core.security.authentication.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import net.eulerform.common.GlobalProperties;
import net.eulerform.common.GlobalPropertyReadException;
import net.eulerform.web.core.security.authentication.entity.User;

public class UserContext {

	private static UserDetailsService userDetailsService;

	public static void setUserDetailsService(UserDetailsService userDetailsService) {
		UserContext.userDetailsService = userDetailsService;
	}

	private final static User ANONYMOUS_USER;
	private final static String ANONYMOUS_USERNAME = "anonymousUser";

	private final static Map<String, UserDetailsStore> USER_CACHE = new HashMap<>();
	private static long cacheSecond = 24 * 60 * 60;

	static {
		ANONYMOUS_USER = new User();
		ANONYMOUS_USER.setId(ANONYMOUS_USERNAME);
		ANONYMOUS_USER.setUsername(ANONYMOUS_USERNAME);
		ANONYMOUS_USER.setAuthorities(null);
		ANONYMOUS_USER.setAccountNonExpired(false);
		ANONYMOUS_USER.setAccountNonLocked(false);
		ANONYMOUS_USER.setEnabled(false);
		ANONYMOUS_USER.setCredentialsNonExpired(false);
		try {
			cacheSecond = Long.parseLong(GlobalProperties.get("userContext.cacheSecond"));
		} catch (GlobalPropertyReadException e) {
			// DO NOTHING
		}
	}

	public static User getCurrentUser() {
		UserDetails userDetails = getCurrentUserDetails();

		if (userDetails.getClass().isAssignableFrom(User.class)) {
			return (User) userDetails;
		}

		User result = new User();
		result.loadDataFromOtherUserDetails(userDetails);
		return result;
	}

	public static UserDetails getCurrentUserDetails() {
		try {
			SecurityContext context = SecurityContextHolder.getContext();
			if (context != null && context.getAuthentication() != null) {
				Object principal = context.getAuthentication().getPrincipal();

				if (principal.getClass().isAssignableFrom(User.class)) {
					UserDetails user = (UserDetails) principal;
					addUserDetailsToCache(user);
					return user;
				}

				if (principal.getClass().isAssignableFrom(String.class)
						&& (!ANONYMOUS_USERNAME.equalsIgnoreCase((String) principal))) {
					String username = (String) principal;
					UserDetails user = getUserDetailsFromCache(username);
					if (user != null) {
						return user;
					}
					user = userDetailsService.loadUserByUsername(username);
					if (user.getClass().isAssignableFrom(CredentialsContainer.class)) {
						((CredentialsContainer) user).eraseCredentials();
					}
					addUserDetailsToCache(user);
					return user;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// DO NOTHING
		}
		return ANONYMOUS_USER;
	}

	public static void addUserDetailsToCache(UserDetails userDeatils) {
		USER_CACHE.put(userDeatils.getUsername(), new UserDetailsStore(userDeatils));
	}

	public static void removeUserDetailsFromCache(String username) {
		USER_CACHE.remove(username);
	}

	public static UserDetails getUserDetailsFromCache(String username) {
		UserDetailsStore userStore = USER_CACHE.get(username);
		if(userStore == null)
			return null;
		
		Date now = new Date();
		if ((now.getTime() - userStore.getAddDate().getTime()) > (cacheSecond * 1000)) {
			removeUserDetailsFromCache(username);
			return null;
		}

		return userStore.getUserDetails();
	}

	private static class UserDetailsStore {
		private UserDetails userDetails;
		private Date addDate;

		private UserDetailsStore(UserDetails userDetails) {
			this.userDetails = userDetails;
			this.addDate = new Date();
		}

		private UserDetails getUserDetails() {
			return userDetails;
		}

		private Date getAddDate() {
			return addDate;
		}
	}
}
