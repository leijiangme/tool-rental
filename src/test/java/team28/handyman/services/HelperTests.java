package team28.handyman.services;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class HelperTests {
	@Test
	public void shouldWorkWithASinglePair() {
		String key = "username";
		String value = "jdavenport@testing.com";
		final SqlParameterSource params = Helper.params(key, value);
		assertThat(params, is(notNullValue()));
		assertThat(value, is(params.getValue(key)));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowException() {
		Helper.params("here");
	}
	
	@Test
	public void shouldWorkWith2Pairs() {
		String key1 = "username",
				key2 = "time",
				value1 = "jdavenpo";
		Long value2 = System.currentTimeMillis();
		
		SqlParameterSource params = Helper.params(key1, value1, key2, value2);
		assertThat(params.getValue(key1), is(value1));
		assertThat(params.getValue(key2), is(value2));
	}
}
