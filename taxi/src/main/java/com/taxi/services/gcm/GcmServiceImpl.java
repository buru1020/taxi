package com.taxi.services.gcm;

import java.io.EOFException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Builder;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.taxi.dao.room.RoomDao;
import com.taxi.dao.room.RoomMbrDao;
import com.taxi.vo.room.Room;
import com.taxi.vo.room.RoomMbr;


@Service
public class GcmServiceImpl implements GcmService {
	@Autowired RoomDao roomDao;
	@Autowired RoomMbrDao roomMbrDao;

	public static final String TAG = "sendManager";
	private static final Executor threadPool = Executors.newFixedThreadPool(5);
	protected static Logger logger = Logger.getLogger("service");
	public static final String APT_KEY = "AIzaSyBHxl2tGP3w99WhLk6UpC3F4x6L79ZdkXM";
	

	public GcmServiceImpl(){}


	/**
	 * 설  명: 방 출발전 알람 푸시
	 * 작성자: 김상헌
	 */
	public void performService() throws Exception {
		System.out.println("performService()");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		
		String criteriaTime = "0:15:1";
		
		List<Room> alramGcmTargetRoomList = roomDao.getAlramGcmTargetRoomList(criteriaTime);
		
		for( Room room : alramGcmTargetRoomList ) {
			List<String> regList = new ArrayList<String>();
			
			for( RoomMbr roomMbr : room.getRoomMbrList() ) {
				regList.add( roomMbr.getGcmRegId() );
			}
			
			Builder msgBuilder = new Message.Builder();
			msgBuilder.addData("className", GcmServiceImpl.StartAlramRunnable.class.getSimpleName());
			msgBuilder.addData("roomMbr", room.getRoomNo()+"");
			msgBuilder.addData("roomStartTime", sdf.format(room.getRoomStartTime()) );
			msgBuilder.addData("differenceTime", room.getDifferenceTime()+"");
			
			threadPool.execute( new StartAlramRunnable(regList, msgBuilder.build()) );
		}
		
	}

	/**
	 * 설  명: google 푸시 서버로 전송할 값 설정 부분
	 * 작성자: 김상헌 
	 */
	public void asyncSend(List<Map<String, Object>> gcmTargetMapList, Class<?> clazz)
			throws IOException, EOFException {
		final List<String> regList = new ArrayList<String>();
		
		for(int i = 0; i < gcmTargetMapList.size(); i++){
			regList.add( (String)gcmTargetMapList.get(i).get("gcmRegId") );
		}
		
		if ( regList.size() > 0 ) {
		 	if ( GcmServiceImpl.FeedRunnable.class == clazz ) {
		 		System.out.println("FeedRunnable Request()...............");
		 		
		 		Builder msgBuilder = new Message.Builder();
		    	for( java.util.Map.Entry<String, Object> entry : gcmTargetMapList.get(0).entrySet() ) {
		    		if ( !"gcmRegId".equals(entry.getKey()) ) {
		    			msgBuilder.addData(entry.getKey(), String.valueOf(entry.getValue()) );
		    		}
		    	}
		    	msgBuilder.addData("className", clazz.getSimpleName());
		    	
		    	Message message = msgBuilder.build();
	
			    threadPool.execute( new FeedRunnable(regList, message) );
			    
		 	} else if ( GcmServiceImpl.RoomRunnable.class == clazz ) {
		 		System.out.println("RoomRunnable Request()...............");
		 		
		 		Builder msgBuilder = new Message.Builder();
		    	
		    	for( java.util.Map.Entry<String, Object> entry : gcmTargetMapList.get(0).entrySet() ) {
		    		if ( !"gcmRegId".equals(entry.getKey()) ) {
		    			msgBuilder.addData(entry.getKey(), String.valueOf(entry.getValue()) );
		    		}
		    	}
		    	msgBuilder.addData("className", clazz.getSimpleName());
		    	
		    	Message message = msgBuilder.build(); 

			    threadPool.execute( new RoomRunnable(regList, message) );
			    
		 	} else if ( GcmServiceImpl.StartAlramRunnable.class == clazz ) {
		 		System.out.println("StartAlramRunnable Alarm Request()...............");

		 	}
		}

	}

	// * 피드 등록시 푸쉬 실행부분 
	// * @author Buru
	public class FeedRunnable implements  Runnable {
		
		Sender sender = null;
		List<String> regList = null;
	    Message message = null;
	    
	    public FeedRunnable( List<String> regList, Message message ) {
	    	this.sender = new Sender(APT_KEY);
	    	this.regList = regList;
	    	this.message = message;
		}
	
	    public void run() {
	
			MulticastResult multicastResult;
			
	        try {
	        	multicastResult = sender.send(message, regList, 5);
	
		        List<Result> results = multicastResult.getResults();
				for (int i = 0; i < regList.size(); i++) {
			        String regId = regList.get(i);
			        Result result = results.get(i);
			        String messageId = result.getMessageId();
			        if (messageId != null) {
			        	System.out.println("Succesfully sent message to device:" + regId + messageId);
			        	logger.info("Succesfully sent message to device: " + regId +
			                    "; messageId = " + messageId);
			        } else {
			        	String error = result.getErrorCodeName();
		        		if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
		        			logger.info("Unregistered device: " + regId);
		        		} else {
		        			logger.debug("Error sending message to " + regId + ": " + error);
		        		}
			        }
			   }
	      } catch (IOException e) {
		      logger.debug(Level.SEVERE, e);
		      return;
		  }
	  }
	}
	
	// * 새로운 멤버참여시 푸쉬 실행부분 
	// * @author Buru
	public class RoomRunnable implements  Runnable {
		
		Sender sender = null;
		List<String> regList = null;
	    Message message = null;
	    
	    public RoomRunnable( List<String> regList, Message message ) {
	    	this.sender = new Sender(APT_KEY);
	    	this.regList = regList;
	    	this.message = message; 
		}
	
	    public void run() {

	    	MulticastResult multicastResult;
			
	        try {
	        	multicastResult = sender.send(message, regList, 5);
	
		        List<Result> results = multicastResult.getResults();
				for (int i = 0; i < regList.size(); i++) {
			        String regId = regList.get(i);
			        Result result = results.get(i);
			        String messageId = result.getMessageId();
			        if (messageId != null) {
			        	System.out.println("Succesfully sent message to device:" + regId + messageId);
			        	logger.info("Succesfully sent message to device: " + regId +
			                    "; messageId = " + messageId);
			        } else {
			        	String error = result.getErrorCodeName();
		        		if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
		        			logger.info("Unregistered device: " + regId);
		        		} else {
		        			logger.debug("Error sending message to " + regId + ": " + error);
		        		}
			        }
			   }
	      } catch (IOException e) {
		      logger.debug(Level.SEVERE, e);
		      return;
		  }
	  }
	}
	
	
	// * 출발시간 알람 푸쉬 실행부분 
	// * @author Buru
	public class StartAlramRunnable implements  Runnable {
		
		Sender sender = null;
		List<String> regList = null;
	    Message message = null;
	    
	    public StartAlramRunnable( List<String> regList, Message message ) {
	    	this.sender = new Sender(APT_KEY);
	    	this.regList = regList;
	    	this.message = message; 
		}
//	    public StartAlramRunnable( List<String> regList, Map<String, String> bundleMap ) {
//	    	this.sender = new Sender(APT_KEY);
//	    	this.regList = regList;
//	    	
//	    	Builder msgBuilder = new Message.Builder();
//	    	msgBuilder.addData("className", this.getClass().getSimpleName());
//	    	for( java.util.Map.Entry<String, String> entry : bundleMap.entrySet() ) {
//	    		msgBuilder.addData(entry.getKey(), entry.getValue());
//	    	}
//	    	this.message = msgBuilder.build(); 
//		}

	    public void run() {

			MulticastResult multicastResult;
			
	        try {
	        	multicastResult = sender.send(message, regList, 5);

		        List<Result> results = multicastResult.getResults();
				for (int i = 0; i < regList.size(); i++) {
			        String regId = regList.get(i);
			        Result result = results.get(i);
			        String messageId = result.getMessageId();
			        if (messageId != null) {
			        	System.out.println("Succesfully sent message to device:" + regId + messageId);
			        	logger.info("Succesfully sent message to device: " + regId +
			                    "; messageId = " + messageId);
			        } else {
			        	String error = result.getErrorCodeName();
		        		if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
		        			logger.info("Unregistered device: " + regId);
		        		} else {
		        			logger.debug("Error sending message to " + regId + ": " + error);
		        		}
			        }
			   }
	      } catch (IOException e) {
		      logger.debug(Level.SEVERE, e);
		      return;
		  }
	  }
	}


	
/*	//====================== AS-IS =======================//
 	
	@Autowired RoomDao roomDao;
	@Autowired RoomMbrDao roomMbrDao;


*/
	
}
