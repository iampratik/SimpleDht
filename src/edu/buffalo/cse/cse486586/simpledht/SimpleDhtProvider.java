package edu.buffalo.cse.cse486586.simpledht;

import java.io.EOFException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.gesture.Prediction;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

public class SimpleDhtProvider extends ContentProvider {

	private DB_Helper db;
	String id;
	String myPort;
	String table = DB_Helper.getTableName();
	static final String TAG = SimpleDhtProvider.class.getSimpleName();
	static ArrayList<String> ports;
	static final int SERVER_PORT = 10000;
	static final String ORGANIZER = "11108";
	ArrayList<String> nodes;
	boolean newJoin = false;
	boolean first = false;
	boolean singleNode = false;
	static boolean isWaiting = true;
	boolean isInitiator = true;
	static String initiator = null;
	String predecessor;
	String successor;
	String first_node;
	String last_node;
	ArrayList<String> gDumpArr = new ArrayList<String>();
	StringBuilder values = new StringBuilder();
	HashMap<String, String> gDump = new HashMap<String, String>();
	Hashtable<String, String> portMap = new Hashtable<String, String>();
	ArrayList<String> cursorValues = new ArrayList<String>();
	String [] avds = {"5554","5556","5558","5560","5562"};

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		SQLiteDatabase sqldb = db.getWritableDatabase();
		int rows;
		if(selection.equals("@"))
		{
			rows = sqldb.delete(table, null, null);
			return rows;
		}
		else if(selection.equals("*"))
		{
			rows = sqldb.delete(table, null, null);
			Log.d("Delete", "forwarding from "+myPort+" to "+successor);
			if(rows!=0)
				new Thread(new MessageClient("deleteAll", null, null)).start();
			//new MessageClient().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"deleteAll");
		}
		
		else 
		{
		    Log.v("provider","inside delete");
			String hashkey = null;
			try {
			    hashkey = genHash(selection);
			} catch (NoSuchAlgorithmException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}
			if( (predecessor==null) || successor== null)
			{
				rows =  sqldb.delete(table, "( key = '"+selection+"' )", null);
				//Thread.sleep(500);
			}
			else if(first_node.equals(id) && (hashkey.compareTo(predecessor)>0 || hashkey.compareTo(id)<= 0))
			{
				Log.d("Delete", "deletion at "+myPort);
				Log.d("Delete", "deleting "+selection);
				rows =  sqldb.delete(table, "( key = '"+selection+"' )", null);
			}
			else if(hashkey.compareTo(predecessor)>0 && hashkey.compareTo(id)<= 0)
			{
			    Log.d("Delete", "deletion at "+myPort);
				Log.d("Delete", "deleting "+selection);
				rows =  sqldb.delete(table, "( key = '"+selection+"' )", null);
			}
			else
			{
				Log.d("Insert", "forwarding from "+myPort+" to "+successor);

				new Thread(new MessageClient("delete", selection, null)).start();
				//	new MessageClient().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"insert",key,value);
				//Thread.sleep(10000);
			}
		}
		getContext().getContentResolver().notifyChange(uri, null);
		sqldb.close();
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		long rowId;
		final String key =(String) values.get("key");
		final String value =(String) values.get("value");

		SQLiteDatabase sqldb = db.getWritableDatabase();
		try
		{
			Log.v("provider","inside insert");
			String hashkey = genHash(key);
			if( (predecessor==null) || successor== null)
			{
				rowId =  sqldb.insertOrThrow(table, null, values);
				//Thread.sleep(500);
			}
			else if(first_node.equals(id) && (hashkey.compareTo(predecessor)>0 || hashkey.compareTo(id)<= 0))
			{
				Log.d("Insert", "insertion at "+myPort);
				Log.d("insert", "inserting "+values.toString());
				rowId =  sqldb.insertOrThrow(table, null, values);
			}
			else if(hashkey.compareTo(predecessor)>0 && hashkey.compareTo(id)<= 0)
			{
				Log.d("Insert", "insertion at "+myPort);
				Log.d("insert", "inserting "+values.toString());
				rowId =  sqldb.insertOrThrow(table, null, values);
			}
			else
			{
				Log.d("Insert", "forwarding from "+myPort+" to "+successor);

				new Thread(new MessageClient("insert", key, value)).start();
				//	new MessageClient().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"insert",key,value);
				//Thread.sleep(10000);
			}

		}
		catch (android.database.sqlite.SQLiteConstraintException e)
		{
			Log.v("Exception",e.getMessage());
			sqldb.update(table, values, "( key = '"+values.keySet().toString()+"' )", null);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getContext().getContentResolver().notifyChange(uri, null);
		sqldb.close();
		return uri;

	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		db = new DB_Helper(getContext());
		TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
		String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		myPort = String.valueOf((Integer.parseInt(portStr) * 2));
		try {
			id = genHash(portStr);
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Log.d(TAG, "onCreate called");	

		for(int i =0; i<avds.length;i++)
		{
			try {
				portMap.put(genHash(avds[i]), avds[i]);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		try {
			/*
			 * Create a server socket as well as a thread (AsyncTask) that listens on the server
			 * port.
			 * 
			 */
			ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
			new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
		} catch (IOException e) {

			Log.e(TAG, "Can't create a ServerSocket");
		}

		if(portStr.equals("5554"))
		{
			Log.d(TAG, portStr);
			nodes = new ArrayList<String>();
			nodes.add(id);
			ports = new ArrayList<String>();
			ports.add(portStr);
			first_node = last_node = predecessor = successor = id;
		}
		else
		{
			//if(ports.contains("5554"))
			try {
				new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,portStr).get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if(successor== null && predecessor==null)
				singleNode = true;
			//else
			//	singleNode = true;
		}


		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		// TODO Auto-generated method stub
		Cursor cursor = null;
		SQLiteDatabase sqlDB = db.getWritableDatabase();
		Log.d("Query", "inside query");
;
		if(initiator==null)
		initiator = id;
		
		
		if(selection.equals("@"))
		{
			cursor = sqlDB.query(table, null, null, null, null, null, null);
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			// Cursor cursor = query.query(sqlDB, null,"(key = "+selection+")", null, null, null, null);
			Log.d("cursor", String.valueOf(cursor.getCount()));
			Log.d("query", selection);
//			
			return cursor;
		}
		else if(selection.equals("*"))
		{
			if(predecessor==null || successor == null)
			{
				return sqlDB.query(table, null, null, null, null, null, null);
			}
//			
			Log.d("Query", "* received");
			cursor = sqlDB.query(table, null, null, null, null, null, null);
			//cursor.setNotificationUri(getContext().getContentResolver(), uri);
			
			if (cursor.moveToFirst()) {
								while (!cursor.isAfterLast()) {
									int keyIndex = cursor.getColumnIndex("key");
									int valueIndex = cursor.getColumnIndex("value");
									String key = cursor.getString(keyIndex);
									String val = cursor.getString(valueIndex);
									
									gDump.put(key, val);
									cursor.moveToNext();
								}
							}
			
			// forward request to all
			new Thread(new MessageClient("gdump","*",initiator)).start();
			if( initiator.equals(id))
			{
			while(isWaiting)
			{


			}
			Log.d("Query", "after while");
			isWaiting= true;
			initiator = null;
			}

			
			MatrixCursor mc = new MatrixCursor(new String[] {"key","value"});
			for(String val : gDump.keySet())
			{
				mc.newRow().add(val).add(gDump.get(val));
			}
			initiator = null;
			return (Cursor)mc;
			
		}
		else 
		{
			Log.d("Query", "key received"+selection);
			cursor = sqlDB.query(table, null, "( key = '"+selection+"' )", null, null, null, null);
			//cursor.setNotificationUri(getContext().getContentResolver(), uri);

			if(cursor.getCount()<=0)
			{
				Log.d("Query", "Request forwarded");

				new Thread(new MessageClient("queryKey",selection,initiator)).start();
				Log.d("Query", "Initiator: "+initiator);
				
				//new MessageClient().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "queryKey",selection);
				if (initiator.equals(id))
				{
				Log.d("Query", "before while");

					while(isWaiting)
					{


					}
					Log.d("Query", "after while");
					isWaiting= true;
					initiator = null;
					MatrixCursor mc = new MatrixCursor(new String[] {"key","value"});
					mc.newRow().add(cursorValues.get(0)).add(cursorValues.get(1));
					cursorValues.clear();
					
					return (Cursor)mc;
				}
			}
			else
			{
				Log.d("Query", "cursor not null");
				//Log.d("Query", "Id :"+id);
				if(initiator.equals(id))
				{
				    initiator = null;
					return cursor;
				}
				//else if(!(id.equals(initiator)))
				else
				{
					Log.d("Query", "condition met");
					cursor.moveToNext();


					new Thread(new ReplyTask(cursor.getString(0),cursor.getString(1),initiator)).start();

				}

			}
		}
		initiator = null;
		return cursor;
	  
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	private String genHash(String input) throws NoSuchAlgorithmException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		byte[] sha1Hash = sha1.digest(input.getBytes());
		Formatter formatter = new Formatter();
		for (byte b : sha1Hash) {
			formatter.format("%02x", b);
		}
		//formatter.close();
		return formatter.toString();
	}


	public void sendInfo(String portStr)
	{

		Log.d("SendInfo", "inside send info");
		try {
			String port = String.valueOf((Integer.parseInt(portStr) * 2));
			Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
					Integer.parseInt(port));

			//BufferedOutputStream bfrOut = new BufferedOutputStream(socket.getOutputStream());
			ObjectOutputStream objout = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			// DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			String id = genHash(portStr);
			int pos = nodes.indexOf(id);
			Message msg = null;
			//StringBuilder response = new StringBuilder();
			if(nodes.size()>1)
			{
				if (pos>0 && pos<nodes.size()-1)
				{
					
					msg = new Message("SP", nodes.get(pos-1), nodes.get(pos+1),first_node,last_node);
				}
				else if(pos == 0)
				{

				    
				    String predecessor = null;
					if(id.equals(first_node))
					    	predecessor = last_node;
					
					msg = new Message("SP", predecessor,nodes.get(pos+1),first_node,last_node);
				}
				else if(pos == (nodes.size()-1))
				{

				    String successor = null;
				    if(id.equals(last_node))
					successor = first_node;
				    
				    msg = new Message("SP", nodes.get(pos-1),successor,first_node,last_node);
					
					
				}	

			}
			else if(nodes.size()==1)
			{			
				msg = new Message("SP", this.id,this.id,this.id,this.id);
			}
			
			objout.writeObject(msg);
			objout.close();
			socket.close();

		} catch (UnknownHostException e) {
			Log.e(TAG, "Sendinfo UnknownHostException");
		} catch (IOException e) {
			Log.e(TAG, "Sendinfo socket IOException");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

		@Override
		protected Void doInBackground(ServerSocket... sockets) {
			ServerSocket serverSocket = sockets[0];

			try{

				Log.d("Server", "Server started: "+myPort);

				while(true)
				{

					Log.d("Server", "waiting for connection");
					Socket s = serverSocket.accept();
					

					  Message msg = null;
					  BufferedInputStream bfrIn = new BufferedInputStream(s.getInputStream());
					  ObjectInputStream objin = new ObjectInputStream(bfrIn);

					Log.d("Server", "Connection established: "+myPort);
				
					try {


					    	if(bfrIn.available()>0)
					    	msg = (Message) objin.readObject();
						if(myPort.equals(ORGANIZER) && msg.getMsg().equals("Join") )
						{
							Log.d("Server", "Received request");
							nodes.add(genHash(msg.getport()));
							ports.add(msg.getport());
							Log.d("Server", "Node added : "+genHash(msg.getport()));

							Collections.sort(nodes);
							Log.v("Server","First node : "+ nodes.get(0).toString());
							first_node = nodes.get(0);
							last_node = nodes.get(nodes.size()-1);
							for(String port: ports)
							{
								sendInfo(port);
							}
							
						}
						else if(msg.getMsg().contains("insert"))
						{
							Log.d("Server", "insert received");
							ContentValues cv = new ContentValues();
							cv.put("key", msg.getKey());
							cv.put("value", msg.getValue());
							SimpleDhtActivity.mContentResolver.insert(SimpleDhtActivity.mUri, cv);
						}
						else if(msg.getMsg().equals("deleteAll"))
						{
							Log.d("Server", "deleteAll received");
							SimpleDhtActivity.mContentResolver.delete(SimpleDhtActivity.mUri, "*", null);
						}
						else if(msg.getMsg().equals("queryKey"))
						{
							Log.d("Server", "query request received: "+msg.getKey());
							initiator = msg.getValue();
							SimpleDhtActivity.mContentResolver.query(SimpleDhtActivity.mUri, null, msg.getKey(), null, null);
							

						}
						else if(msg.getMsg().contains("gdump"))
						{
							Log.d("Server", "gdump received: "+msg.getValue());
							//if(initiator==null)
							initiator = msg.getValue();
							gDump = msg.getData();
							
							if(initiator.equals(id))
							{
							isWaiting = false;
							
							}
							else
							SimpleDhtActivity.mContentResolver.query(SimpleDhtActivity.mUri, null, "*", null, null);


						}
						else if(msg.getMsg().equals("KeyFound"))
						{
							Log.d("Server", "key found");
							//Log.d("Server", initiator);
							Log.d("Server",id);
							//if(initiator.equals(id))
							//{
							cursorValues.add(msg.getKey());
							cursorValues.add(msg.getValue());
							isWaiting = false;
							//}
			
						}
						else if(msg.getMsg().equals("SP"))
						{
							successor = msg.getSuccessor();
							predecessor= msg.getPredecessor();
							first_node = msg.getFirst_node();
							last_node = msg.getLast_node();
							Log.d("Server Response", "Sucessor: "+successor);
							Log.d("Server Response","Predecessor: "+predecessor);
						}
						else if(msg.getMsg().equals("delete"))
						{
						    Log.d("Server", "delete received");
						SimpleDhtActivity.mContentResolver.delete(SimpleDhtActivity.mUri, msg.getKey(), null);
						}
					} 
					
					
					catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					objin.close();
					s.close();
				}


			} 
			catch ( EOFException ef)
			{
			    
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 

			return null;
		}
	}
	//	 

	class MessageClient implements Runnable
	{
		String msgType;
		String key;
		String value;

		public MessageClient(String msgType,String key,String value)
		{
			this.msgType = msgType;
			this.key = key;
			this.value = value;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub

			try {
				int suc_port= Integer.parseInt(portMap.get(successor))*2;
				Log.d("Message-Client", "Message sending to"+successor+"port num :"+suc_port);
				String port = String.valueOf((suc_port));

				Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
						Integer.parseInt(port));

				ObjectOutputStream objOut = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
				Message msg = null;


				if(msgType.equals("insert"))
				{

				    
				    msg = new Message(msgType, key, value);
				}
				else if(msgType.equals("deleteAll"))
				{
				    msg = new Message(msgType, "null", null);
				}
				else if(msgType.equals("queryKey"))
				{

					msg = new Message(msgType, key, value);
					//Log.d("Message-Client", "Query key message packed :"+response.toString());
				}
				else if(msgType.equals("gdump"))
				{

						msg = new Message(msgType, gDump, value);
				}
				else if(msgType.equals("delete"))
				{
				    msg = new Message(msgType, key, value);
				}
				
				objOut.writeObject(msg);
				objOut.close();
				socket.close();
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


	class ReplyTask implements Runnable
	{
		String key;
		String value;
		String init;

		public ReplyTask(String key,String value,String init)
		{
			this.key = key;
			this.value = value;
			this.init = init;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				int caller_port= Integer.parseInt(portMap.get(init))*2;

				Log.d("Reply-Task", "Sending reply to caller ,port num :"+caller_port);
				String port = String.valueOf((caller_port));

				Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
						Integer.parseInt(port));

				//BufferedOutputStream bfrOut = new BufferedOutputStream(socket.getOutputStream());
				//StringBuilder response = new StringBuilder();

				ObjectOutputStream objOut = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
				Message msg = new Message("KeyFound", key, value);
				
				
				objOut.writeObject(msg);
				objOut.close();
//				bfrOut.close();
				socket.close();
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


	private class ClientTask extends AsyncTask<String, Void, Void>
	{
		@Override
		protected Void doInBackground(String... msgs) {
			try {
				String remotePort = ORGANIZER;
				Log.d("ClientTask", "in doinbackground");
				Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(remotePort));
				String msgToSend = "Join";
				Message msg = new Message(msgToSend, msgs[0]);
				
				
				ObjectOutputStream objOut = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
				objOut.writeObject(msg);
				objOut.close();
				socket.close();

			} catch (UnknownHostException e) {
				Log.e(TAG, "ClientTask UnknownHostException");
			} catch (IOException e) {
				Log.e(TAG, "ClientTask socket IOException");
			}

			return null;
		}
	}
}

