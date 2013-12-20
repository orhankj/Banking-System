import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;
public class jdbc {
	private static Connection connection=null;
	private static Statement statement=null;
	private static ResultSet resultSet=null;
	private static PreparedStatement preparedStatement=null;
	private int  printFlag=0;
	private Scanner input=new Scanner(System.in);
	public void connect(){
	try
	{
		
		Class.forName("com.mysql.jdbc.Driver");
		connection=DriverManager.getConnection("jdbc:mysql://localhost:3306/bank","root","luitenica23");
	    statement=connection.createStatement();
	    System.out.println("Connected");
	    System.out.println("Please enter your EGN and password to log in");String egn=input.nextLine();String pass=input.nextLine();
         if( Login(egn,pass)==1)
       {		
        
	      System.out.println("Choose an option:\n 1.Widrow \n 2.Deposit \n 3.Transfer \n 4.Show my Widrows \n 5.Show my Deposits \n 6.Show my Transfers ");
		 int n=input.nextInt();
		 switch (n){
		 case 1: Login(egn,pass);
		   System.out.println("Please enter the amount of your widrow");double amount=input.nextDouble();
			try {
				Widrow( connection, preparedStatement,Login(egn,pass),amount);break;
			} catch (SQLException e) {
				System.out.println("Error1: "+e.getMessage());
			}
		 case 2:System.out.println("Please enter the amount of your deposit");double amount1=input.nextDouble();
		 Deposit( connection, preparedStatement,Login(egn,pass), amount1);break;
		 case 3:System.out.println("Please enter the amount of your transfer");double amount2=input.nextDouble();input.nextLine();
		 System.out.println("Please enter the EGN of the reciever");String EGN=input.nextLine();
		 Transaction( connection, preparedStatement,Login(egn,pass),EGN,amount2);break;
		 case 4: showWidrows(connection,Login(egn,pass));
		 case 5: showDeposits(connection,Login(egn,pass));
		 case 6:showTransfers(connection,Login(egn,pass));
		 }
	  }
         else System.out.println("No match, try again");
	}
	
	catch(ClassNotFoundException error)
	{
		System.out.println("Error: "+error.getMessage());
	}
	catch(SQLException error1){
		System.out.println("Error1: "+error1.getMessage());
	}
	finally
	{
		if(connection!=null) try{connection.close();} catch(SQLException ignore){}
		if(statement!=null) try{statement.close();} catch(SQLException ignore){}
	}
	 
}
	
	public static void Widrow(Connection connection,PreparedStatement preparedStatement1,int ID,Double amount) throws SQLException{
		PreparedStatement st=null;
		 preparedStatement1=connection.prepareStatement("INSERT INTO widrows(id,amount,date)VALUES(?,?,?)");
			preparedStatement1.setInt(1,ID);
			preparedStatement1.setDouble(2,amount);
			preparedStatement1.setTimestamp(3,getCurrentTimeStamp());
            st=connection.prepareStatement("UPDATE accounts SET balance=balance-? where clientID=?");
            st.setDouble(1,amount);
            st.setInt(2,ID);
            preparedStatement1.executeUpdate();
            st.executeUpdate();
	}
	public static void Deposit(Connection connection,PreparedStatement preparedStatement1,int ID,Double amount) throws SQLException{
		PreparedStatement st=null;
		Date date= new Date();
		 preparedStatement1=connection.prepareStatement("INSERT INTO deposits(id,amount,date)VALUES(?,?,?)");
			preparedStatement1.setInt(1,ID);
			preparedStatement1.setDouble(2,amount);
			preparedStatement1.setTimestamp(3,getCurrentTimeStamp());
            st=connection.prepareStatement("UPDATE accounts SET balance=balance+? where clientID=?");
            st.setDouble(1,amount);
            st.setInt(2,ID);
            preparedStatement1.executeUpdate();
            st.executeUpdate();
	}
	public static void Transaction(Connection connection,PreparedStatement preparedStatement1,int senderID,String egn,Double amount) throws SQLException{
		PreparedStatement st=null;
		PreparedStatement st2=null;
		PreparedStatement st3=null;
		ResultSet rs=null;int recieverid = 0;
		st3=connection.prepareStatement("Select id from clients where EGN=?");
		st3.setString(1,egn);
	  rs=st3.executeQuery();
	  while(rs.next()){
		recieverid=rs.getInt("id"); 
	  }
		    preparedStatement1=connection.prepareStatement("INSERT INTO transfers(senderID,recieverID,amount,date)VALUES(?,?,?,?)");
			preparedStatement1.setInt(1,senderID);
			preparedStatement1.setInt(2,recieverid);
			preparedStatement1.setDouble(3,amount);
			preparedStatement1.setTimestamp(4,getCurrentTimeStamp());
            st=connection.prepareStatement("UPDATE accounts SET balance = balance-? WHERE clientID = ?;");
            st.setDouble(1,amount);
            st.setInt(2,senderID);
            st.executeUpdate();
            st2=connection.prepareStatement("UPDATE accounts SET balance = balance +? WHERE clientID = ?");
            st2.setDouble(1,amount);
            st2.setInt(2,recieverid);
            preparedStatement1.executeUpdate();
            st2.executeUpdate();
	}
	@SuppressWarnings("finally")
	public int Login(String EGN,String password){
		Statement ps=null;
		ResultSet result=null;
		String egn,pass,fName,LName,address;
		int id = 0;
		boolean flag=false;
		try {
			ps=connection.createStatement();
			ps.executeQuery("Select * from clients");
			result=ps.getResultSet();
			while(result.next())
			{
				egn=new String(result.getString("EGN"));
				pass=new String(result.getString("password"));
			    id = result.getInt("id");
			    fName = result.getString("firstName");
				LName = result.getString("LastName");
				address = result.getString("address");
				if(pass.equals(password) && egn.equals(EGN)){
					printFlag++;
					if(printFlag==1)
					System.out.println("Hello "+fName+" "+LName+" your id:"+id+" password:"+password+" EGN:"+EGN+" address:"+address);
				flag=true;
				break;}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally
		{
			if(flag==true) return id;
			else return 0;
		}
		
	
		}
	private static java.sql.Timestamp getCurrentTimeStamp() {
		 
		java.util.Date today = new java.util.Date();
		return new java.sql.Timestamp(today.getTime());
	 
	}
    public static void showWidrows(Connection con,int ID) throws SQLException
    {
    PreparedStatement ps=null;
    	ResultSet rs=null;
    	double amount=0;
    	java.sql.Timestamp date;
    	System.out.println("ID:"+ID);
    	ps=con.prepareStatement("Select amount,date from widrows where id=?");
    	ps.setInt(1,ID);
          rs=ps.executeQuery();
          while(rs.next())
          {  
        	  amount=rs.getDouble("amount");
        	  date=rs.getTimestamp("date");
        	  System.out.println("amount:"+amount+" date:"+date);
          }
    }
    public static void showDeposits(Connection con,int ID) throws SQLException
    {
    PreparedStatement ps=null;
    	ResultSet rs=null;
    	double amount=0;
    	java.sql.Timestamp date;
    	System.out.println("ID:"+ID);
    	ps=con.prepareStatement("Select amount,date from deposits where id=?");
    	ps.setInt(1,ID);
          rs=ps.executeQuery();
          while(rs.next())
          {  
        	  amount=rs.getDouble("amount");
        	  date=rs.getTimestamp("date");
        	  System.out.println("amount:"+amount+" date:"+date);
          }
    }
    public static void showTransfers(Connection con,int ID) throws SQLException
    {
    PreparedStatement ps=null; PreparedStatement ps2=null;
    	ResultSet rs=null;ResultSet rs2=null;
    	double amount=0;
    	Array recieverID;
    	java.sql.Timestamp date = null;
    	String fName = null;
    	String lName = null;
    	String egn=null;
    	int i=0;
    	Array idta = null;
    	ArrayList<Double> list=new ArrayList<Double>();
    	ArrayList<java.sql.Timestamp> list2=new ArrayList<java.sql.Timestamp>();
    	ArrayList<String> list3=new ArrayList<String>();
    	ArrayList<String> list4=new ArrayList<String>();
    	//ArrayList<Integer> list5=new ArrayList<Integer>();
    	ps=con.prepareStatement("Select recieverID,amount,date from transfers where senderID=?");
    	ps.setInt(1,ID);
          rs=ps.executeQuery();
          while(rs.next())
          {  
        	  recieverID=rs.getArray("recieverID");
        	  amount=rs.getDouble("amount");
        	  date=rs.getTimestamp("date");
        	  list.add(i,amount );
        	 list2.add(i,date );
        	 idta=recieverID;
        	 i++;
          }
        
     
          i=0;
          ps2=con.prepareStatement(" Select firstName,lastName from clients where id=?");
        	 ps2.setArray(1,idta);
      	 rs2=ps2.executeQuery();
      	 while(rs2.next())
      	{
      		fName=rs2.getString("firstName");
      		lName=rs2.getString("lastName");
      	    list3.add(i,fName );
      	    list4.add(i,lName ); 
      	  
      	    i++;
      	}
      	list.toArray();list2.toArray();list3.toArray();list4.toArray();
      	String[] fname = list3.toArray(new String[list3.size()]);
      	String[] lname = list4.toArray(new String[list4.size()]);
      	Double[] pari = list.toArray(new Double[list.size()]);
      	java.sql.Timestamp[] data = list2.toArray(new java.sql.Timestamp[list2.size()]);
        

      	for(i=0;i<list.size() && i<list2.size() && i<list3.size() && i<list4.size();i++)
       {
            System.out.println("i:"+i);

        System.out.println("Reciever:"+fname[i]+" "+lname[i]+" amount:"+pari[i]+" date:"+data[i]);
       }    
    }
    
}

