package com.numbergame;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.numbergame.service.NumberGameService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NumberGameApplicationTests {
	
	@Autowired
	NumberGameService responseService;
	
	@Autowired 
	TestRestTemplate template;
	
	@Mock
	HttpServletRequest request;
	
	@Mock
	HttpServletResponse response;
	
	@Value("${com.responselabel.partone}")
	String partOne;
	
	@Value("${com.responselabel.parttwo}")
	String partTwo;

	/**
	 * In this test, we are taking the question from the first endpoint (/question), and parsing the response to sum the numbers and sending
	 * the calculated sum to the second endpoint (/validate). Since we are calculating sum from the original response the sum should be same and response is
	 * as expected 
	 */
	@Test
	public void testCorrectSumResponse() {
		String questionResponse = template.getForObject("/question", String.class);
		Integer[] numbers = parseNumber(questionResponse);
		int sum = numbers[0]+numbers[1]+numbers[2];
		ResponseEntity<String> answerValidationResponse= template.getForEntity("/validate?inputQuestion="+questionResponse+"&sum="+sum, String.class);
		
		assertEquals(HttpStatus.OK, answerValidationResponse.getStatusCode());
	    String response = answerValidationResponse.getBody();
	    if (response != null) {
	        assertEquals("Thats Great", response);
	    }
	}
	
	/**
	 * In this test, we are taking the question from the first endpoint (/question), and sending 0 to the second endpoint (/validate). Since we are sending sum 
	 * as zero which is not the actual sum, it should send the response saying pleaase try again
	 *  
	 */
	@Test
	public void testWrongSumResponse() {
		String questionResponse = template.getForObject("/question", String.class);
		int sum = 0;
		ResponseEntity<String> answerValidationResponse = template.getForEntity("/validate?inputQuestion="+questionResponse+"&sum="+sum, String.class);
		
		assertEquals(HttpStatus.BAD_REQUEST, answerValidationResponse.getStatusCode());
	    String response = answerValidationResponse.getBody();
	    if (response != null) {
	        assertEquals("Thats Wrong. Plese Try Again", response);
	    }
	}
	
	/**
	 * In this test, we are taking the question from the first endpoint (/question), and parsing the response to sum the numbers and sending
	 * the calculated sum to the second endpoint (/validate). Meanwhile, one more request came to the first endpoint, but since we are calculating sum from the 
	 * first response which is not latest. It should send Response is tampered message.
	 * 
	 */
	@Test
	public void testOldResponseRejected() {
		String questionResponse = template.getForObject("/question", String.class);
		String latestQuestionResponse = template.getForObject("/question", String.class);

		Integer[] numbers = parseNumber(questionResponse);
		int sum = numbers[0]+numbers[1]+numbers[2];
		ResponseEntity<String> answerValidationResponse = template.getForEntity("/validate?inputQuestion="+questionResponse+"&sum="+sum, String.class);
		
		assertEquals(HttpStatus.BAD_REQUEST, answerValidationResponse.getStatusCode());
	    String response = answerValidationResponse.getBody();
	    if (response != null) {
	        assertEquals("Response is Tampered.", response);
	    }
	}
	
	@Test
	public void testQuestionManipulatedRejected() {
		String questionResponse = template.getForObject("/question", String.class);
		String manipulatedText = "this is manipulation";

		Integer[] numbers = parseNumber(questionResponse);
		int sum = numbers[0]+numbers[1]+numbers[2];
		ResponseEntity<String> answerValidationResponse = template.getForEntity("/validate?inputQuestion="+questionResponse+manipulatedText+"&sum="+sum, String.class);
		
		assertEquals(HttpStatus.BAD_REQUEST, answerValidationResponse.getStatusCode());
	    String response = answerValidationResponse.getBody();
	    if (response != null) {
	        assertEquals("Response is Tampered.", response);
	    }
	}
	
	/**
	 * In this test, we are taking the question from the first endpoint (/question), and parsing the response to sum the numbers and sending
	 * the calculated sum to the second endpoint (/validate). Meanwhile, one more request came to the first endpoint, and we are taking sum from the 
	 * second response which is latest. It should send OK response.
	 * 
	 */
	@Test
	public void testLatestResponseAccepted() {
		String questionResponse = template.getForObject("/question", String.class);
		String latestQuestionResponse = template.getForObject("/question", String.class);

		Integer[] numbers = parseNumber(latestQuestionResponse);
		int sum = numbers[0]+numbers[1]+numbers[2];
		ResponseEntity<String> answerValidationResponse = template.getForEntity("/validate?inputQuestion="+latestQuestionResponse+"&sum="+sum, String.class);
		
		assertEquals(HttpStatus.OK, answerValidationResponse.getStatusCode());
	    String response = answerValidationResponse.getBody();
	    if (response != null) {
	        assertEquals("Thats Great", response);
	    }
	}
	
	
	/**
	 * Parses the given text for the numbers to sum
	 * @param questionText
	 * @return
	 */
	public Integer[] parseNumber(String questionText) {
		Pattern p = Pattern.compile("([a-zA-Z, _:]*)([0-9]+),([0-9]+),([0-9]+)");//. represents single character  
		Matcher m = p.matcher(questionText);  
		Integer[] numbers = new Integer[3];
		
		while(m.find()) {
			numbers[0] = Integer.valueOf(m.group(2));
			numbers[1] = Integer.valueOf(m.group(3));
			numbers[2] = Integer.valueOf(m.group(4));					
		}
		
		return numbers;
				
		
	}
	
	

}
