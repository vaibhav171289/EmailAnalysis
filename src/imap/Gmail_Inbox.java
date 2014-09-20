package imap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
//import javax.mail.Part;
import javax.mail.Session;
import javax.mail.UIDFolder;

import au.com.bytecode.opencsv.CSVWriter;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
//import javax.mail.FetchProfile.Item;
public class Gmail_Inbox {
	private  Properties props=null;				//property object for loading the default properties
	private static  FileInputStream in= null;			
	private static String username,password;
	private  CSVWriter writer=null;
	private IMAPFolder currentFolder=null;
	int i=0;
	Message[] messages=null;
	//hello world
	public Gmail_Inbox()
	{	
		props= new Properties();
		password=username=null;

		try {
			
			in= new FileInputStream(new File("config.properties"));
		loadDefaultProperties();
		} catch (FileNotFoundException e) {
			System.out.println("File not found properties.config");
			e.printStackTrace();
		}
		file=new File(username+".csv");

		createFile();
		
	}
	private static File file=null;
	public  void  createFile() {
		try {

			
				writer = new CSVWriter(new FileWriter(file,true));

		} catch (FileNotFoundException e) {
			System.out.println(username+" file is not present");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("problem with file writer in constructor");
			e.printStackTrace();
		}
	}
	private static String host="pop.gmail.com";
	public static void main(String[] args) {

		Gmail_Inbox email= new Gmail_Inbox();
		//	startTime=System.currentTimeMillis();
if(!file.exists())
		{	email.writingToCSV("MessageID,UID,sender,reciever,CC,BCC,Date,Size,Subject,Attachment,folder".split("\\,"));

		}

		email.createSession(email);
		System.exit(0);
	}
	private String folder[]=null,line[]=null;
	public void loadDefaultProperties()
	{
		try {
			props.load(in);
			line=props.getProperty("details").split("\\#");
			username=line[0];
			password=line[1];
			folder=line[2].split("\\,");
			//password=props.getProperty("password");

			//			System.out.println(username+"\n"+password);
			in.close();
			
		} catch (IOException e) {
			System.out.println("Unable to  load default values from file");
			e.printStackTrace();
		}
	}

	public synchronized void writingToCSV(String [] line)
	{

		try {
			writer.writeNext(line);//(line[i]);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	/**
	 * @param host
	 */
	private static int start=1,end=10;
	//private static long startTime=0L,endTime=0L;
private long startUID=1L,endUID=UIDFolder.LASTUID;
	public void createSession(Gmail_Inbox email)
	{
		//		props.put("mail.imap.host", host);
		//		props.put("mail.imap.port", "110");
		//		props.put("mail.imap.starttls.enable", "true");
		//		endTime=System.currentTimeMillis();	
		System.out.println("check 1:");
		//		startTime=endTime;
		//----------------------


		props.setProperty("mail.store.protocol", "imaps");
		Session  session = Session.getDefaultInstance(props,null);
		IMAPStore store =null;

		//----------------------------------------
		//		endTime=System.currentTimeMillis();	
		//		System.out.println("check 2: "+(endTime-startTime));
		//		startTime=endTime;
		//		//-----------------------------------
		
		//----------------------------------------
		//		endTime=System.currentTimeMillis();	
		//		System.out.println("check 3: "+(endTime-startTime));
		//		startTime=endTime;
		//-----------------------------------

		try {

			//creating a POP3 store object and connecting it to server	
			store=(IMAPStore) session.getStore("imaps");
			store.connect(host, username, password);


			//----------------------------------------
			//			endTime=System.currentTimeMillis();	
			//			System.out.println("check 4: "+(endTime-startTime));
			//			startTime=endTime;
			//-----------------------------------
			for(int i=0;i<folder.length;i++)
			{
				if(folder[i].equals("inbox"))
					//creating a folder object and giving it the permission and open it
					currentFolder= (IMAPFolder) store.getFolder(folder[0]);
				else
					if(folder[i].equals("spam"))
						currentFolder= (IMAPFolder) store.getFolder(folder[1]);

				currentFolder.open(Folder.READ_ONLY);


				//----------------------------------------
				//			endTime=System.currentTimeMillis();	
				System.out.println("check 5: "+currentFolder.getFullName());
				//			startTime=endTime;
				//-----------------------------------
				messages= currentFolder.getMessagesByUID(startUID, endUID);
				

							System.out.println("Total no of messages-->"+currentFolder.getMessageCount());

				//----------------------------------------
				//			endTime=System.currentTimeMillis();	
				System.out.println("check 6: ");
				//			startTime=endTime;
				//			//-----------------------------------
				FetchProfile fp= new FetchProfile();
				fp.add(FetchProfile.Item.FLAGS);
				fp.add(FetchProfile.Item.ENVELOPE);
				fp.add(FetchProfile.Item.CONTENT_INFO);
				fp.add(username);
				currentFolder.fetch(messages, fp);

				int count=currentFolder.getMessageCount()/5;
				end=count;
				Thread[] threadArray=  new Thread[5];
				System.out.println("---???");
				for(i=0;i<5;i++)
				{
					System.out.println("Starting thread "+Thread.currentThread().getName());
					Runnable r= new Mythread(email,currentFolder, start, end);
					threadArray[i]= new Thread(r,"Thread@"+(i+1));
					threadArray[i].start();
					start=end+1;
					end=end+count;

				}

				int threadsAliveCount=0; 
				boolean flag=true,flag1=true,flag2=true,flag3=true,flag4=true;
				while(true)
				{
					if(!threadArray[0].isAlive() && flag)
					{
						flag=false;
						threadsAliveCount++;
						System.out.println("closing thread 1");
					}
					if(!threadArray[1].isAlive() && flag1)
					{
						flag1=false;
						threadsAliveCount++;

						System.out.println("closing thread 2");
					}
					if(!threadArray[2].isAlive() && flag2)
					{
						flag2=false;
						threadsAliveCount++;

						System.out.println("closing thread 3");
					}
					if(!threadArray[3].isAlive() && flag3)
					{
						flag3=false;
						threadsAliveCount++;

						System.out.println("closing thread 4");
					}
					if(!threadArray[4].isAlive() && flag4)
					{
						flag4=false;
						threadsAliveCount++;

						System.out.println("closing thread 5");
					}
					if(threadsAliveCount>4)
						break;
				}
				currentFolder.close(flag);
			}
			store.close();

			//----------------------------------------
			//			endTime=System.currentTimeMillis();	
			//			System.out.println("check 18: "+(endTime-startTime));
			//			startTime=endTime;
			//			//-----------------------------------

			//			System.out.println("Total time: "+System.currentTimeMillis()/60000+"mins");

		} catch (NoSuchProviderException e) {
			System.out.println("There is no provider for imap");
			e.printStackTrace();
		} catch (MessagingException e) {
			System.out.println("this is message exception unable to authenticate");
			e.printStackTrace();
		}


	}




}

class Mythread implements Runnable
{
	//	private Folder currentFolder=null;
	private int start,end;
	private Multipart multipart=null;
	//	private Part part=null;
	private String[] line=null;
	private IMAPFolder currentFolder=null;
	int i=0;
	Message[] messages=null;
	//private long startTime=0L,endTime=0L;
	private Gmail_Inbox email=null;
	public Mythread(Gmail_Inbox email,IMAPFolder folder,int start,int end)
	{
		this.email=email;
		this.currentFolder=folder;
		this.start=start;
		this.end=end;
		line=new String[11];
	}
	public void run() {
		try {
			messages=currentFolder.getMessages(start, end);

			for( i=0;i<messages.length;)
			{

				line=new String[11];

				//----------------------------------------
				//				endTime=System.currentTimeMillis();	
				System.out.println("check 7: ");
				//				startTime=endTime;
				//				//-----------------------------------
				Message m= messages[i];
				System.out.println("-----------------------------");
				System.out.println("Email Number: "+ (i+1)+"--->"+Thread.currentThread().getName());
				IMAPMessage mess= (IMAPMessage) m;


				//----------------------------------------
				//				endTime=System.currentTimeMillis();	
				//				System.out.println("check 8: "+(endTime-startTime));
				//				startTime=endTime;
				//				//-----------------------------------

				try {
					line[0]=mess.getMessageID();
				} catch (MessagingException e) {
					System.out.println("error in message id");
					e.printStackTrace();
				}
				//				System.out.println("Message ID: "+line[0]);


				//----------------------------------------
				//				endTime=System.currentTimeMillis();	
				//				System.out.println("check 9: "+(endTime-startTime));
				//				startTime=endTime;
				//				//-----------------------------------
				UIDFolder uf= (UIDFolder)currentFolder;
				long uid;
				try {
					uid = uf.getUID(mess);
					line[1]=""+uid;
					//					System.out.println("UID :" +line[1]);




					//----------------------------------------
					//					endTime=System.currentTimeMillis();	
					//					System.out.println("check 10: "+(endTime-startTime));
					//					startTime=endTime;
					//					//-----------------------------------
					line[2]=mess.getFrom()[0].toString();
					//					System.out.println("Sender: "+line[2]);
					Address address[]=mess.getRecipients(Message.RecipientType.TO);
					//					System.out.print("names of all recipents: ");

					if(address!=null  )
						if(address.length<2)
							line[3]=address[0].toString();
						else
							for(int j=1;j<address.length;j++)
							{
								line[3]+=address[j].toString()+",";
							}
					//					else
					//					line[3]=address[0].toString();
					//					System.out.print(line[3]);


					//----------------------------------------
					//					endTime=System.currentTimeMillis();	
					//					System.out.println("check 11: "+(endTime-startTime));
					//					startTime=endTime;
					//					//-----------------------------------
					//					System.out.print("\n");
					if(mess.getRecipients(Message.RecipientType.CC)!=null)
						line[4]=mess.getRecipients(Message.RecipientType.CC).toString();
					else
						line[4]="NA";
					//					System.out.println("CC: "+line[4]);


					//----------------------------------------
					//					endTime=System.currentTimeMillis();	
					//					System.out.println("check 12: "+(endTime-startTime));
					//					startTime=endTime;
					//					//-----------------------------------
					if(mess.getRecipients(Message.RecipientType.BCC)!=null)
						line[5]=mess.getRecipients(Message.RecipientType.BCC).toString();
					else
						line[5]="NA";
					//					System.out.println("BCC: "+line[5]);


					//----------------------------------------
					//					endTime=System.currentTimeMillis();	
					//					System.out.println("check 13: "+(endTime-startTime));
					//					startTime=endTime;
					//-----------------------------------
					Date d=mess.getReceivedDate();
					line[6]=d.toString();
					//					System.out.println("timestamp: "+line[6]);
					line[7]=Integer.toString(mess.getSize());
					//					System.out.println("Message Size: "+line[7]);

					//----------------------------------------
					//					endTime=System.currentTimeMillis();	
					//					System.out.println("check 14: "+(endTime-startTime));
					//					startTime=endTime;
					//					//-----------------------------------
					line[8]=mess.getSubject();
					//					System.out.println("Subject: "+line[8]);

					//					System.out.println("Type"+m.getContentType());

					//----------------------------------------
					//					endTime=System.currentTimeMillis();	
					//					System.out.println("check 15: "+(endTime-startTime));
					//					startTime=endTime;
					//					//-----------------------------------
					Object messagecontent = m.getContent();
					if(messagecontent instanceof Multipart)
					{
						//						System.out.println("Found Email with Attachments");

						multipart= (Multipart)m.getContent();
						//String contentType="";
						//						for(int j=0;j<multipart.getCount();j++)
						//						{
						//							part= multipart.getBodyPart(j);
						//							contentType= part.getContentType();
						//							System.out.println("Content: \n"+ contentType);
						//							if(contentType.startsWith("text/plain"))
						//								System.out.println("reading content of type text");
						//							else
						//							{
						//								String filename= part.getFileName();
						//
						////								System.out.println("file name is: "+filename);
						//							}
						//						}

						line[9]=Integer.toString(multipart.getCount());
					}else
					{
						//						System.out.println("Found the mail without attachement");

						line[9]=Integer.toString(0);

					}

					line[10]=currentFolder.getFullName();
					//----------------------------------------
					//					endTime=System.currentTimeMillis();	
					//					System.out.println("check 16: "+(endTime-startTime));
					//					startTime=endTime;
					//					//-----------------------------------
					new Gmail_Inbox().writingToCSV(line);

					//----------------------------------------
					//					endTime=System.currentTimeMillis();	
					//					System.out.println("check 17: "+(endTime-startTime));
					//					startTime=endTime;
					//					//-----------------------------------
					line=null;
				} catch (MessagingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				i++;
				System.out.println(Thread.currentThread().getName());


			}
		} catch (MessagingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
