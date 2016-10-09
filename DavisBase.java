
import java.io.RandomAccessFile;
import java.sql.Date;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedMap;
import java.lang.Object;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

public class DavisBase {

	static String prompt = "davisql> ";
	static String database = "";
	static String table;
	static int inf = 0;
	static int countset = 0;

	public static void main(String[] args) throws EOFException {
		/* Display the welcome splash screen */
		splashScreen();
		Scanner scanner = new Scanner(System.in).useDelimiter(";");
		File f = new File("information_schema.schemata.tbl");
		if (!f.exists()) {
			createInfoSchema();
		}
		String userCommand;
		do {
			System.out.print(prompt);
			userCommand = scanner.next().trim();
			String[] a = userCommand.split(" ");
			switch (a[0]) {
			case "show":
				if (a[1].equals("schemas"))
					displaySchema();
				else if (a[1].equals("tables")) {
					displayTables();
				}
				break;
			case "use":
				database = a[1];
				use();
				break;
			case "select":
				table = a[3];
				if (a.length == 4)
					selectAll();
				
				 else { countset=0; selectSpecific(userCommand); } break;
				 
			case "create":
				if (a[1].equals("schema")) {
					database = a[2];
					createSchema();
				} else {
					if (a[1].equals("table")) {

						String[] t1 = a[2].replace("(", " ").split(" ");
						table = t1[0];
						createTable(userCommand);
					}
				}

				break;
			case "exit":
				break;
			case "insert":
				table = a[2];
				insertToTable(userCommand);
				break;
			case "help":
				help();
			default:
				System.out.println("Command not understood : " + userCommand);
			}
		} while (!userCommand.equals("exit"));
		System.out.println("Exiting.....");
	}

	public static void splashScreen() {
		System.out.println(line("*", 80));
		System.out.println("Welcome to DavisBaseLite"); // Display the string.
		version();
		System.out.println("Type \"help;\" to display supported commands.");
		System.out.println(line("*", 80));
	}

	public static void version() {
		System.out.println("DavisBaseLite v1.0\n");
	}

	public static String line(String s, int num) {
		String a = "";
		for (int i = 0; i < num; i++) {
			a += s;
		}
		return a;
	}

	public static void help() {
		System.out.println(line("*", 80));
		System.out.println();
		System.out.println("\tcreate schema <schema_name>; 						Create a new schema");
		System.out.println();
		System.out.println("\tshow schemas;  								Display all schemas");
		System.out.println();
		System.out.println(
				"\tuse <schema_name>;  							Select to use a particular database schema");
		System.out.println();
		System.out.println("\tshow tables;   								Display all tables in the selected schema");
		System.out.println();
		System.out.println(
				"\tcreate table <table_name>(<attribute_name>   				create a new table. primarykey and not null parameters are optional");
		System.out.println("\t\t<attribute_type> <primaryKey> <notnull>);");
		System.out.println();
		System.out.println(
				"\tinsert into <table_name>   						Insert a record into the table. Null values are to be explicity specified.");
		System.out.println("\t\tvalues(<value1>,value<2>);"
				+ " 					String and dates are to be enclosed in single quotes".toUpperCase());
		System.out.println();
		System.out.println("\tselect * from <table>;   						Display all records in the table.");
		System.out.println();
		System.out
				.println("\tselect * from <table_name> where ID=id;    				Display records whose ID is <id>.");
		System.out.println();
		System.out.println("\tversion;       								Show the program version.");
		System.out.println();
		System.out.println();

		System.out.println("\thelp;          								Show this help information");
		System.out.println();
		System.out.println("\texit;          								Exit the program");
		System.out.println();
		System.out.println();
		System.out.println(line("*", 80));
		System.out.println(line("*", 80));
		System.out.println("\tSupported datatypes and formats".toUpperCase());
		System.out.println(line("-", 80));
		System.out.println("int 				for 		Integer");
		System.out.println("float				for 		double");
		System.out.println("long (or) longint 		for 		Long");
		System.out.println("short (or) shortint		for 		short");
		System.out.println("byte 				for 		Byte");
		System.out.println("char(<length>) 			for 		fixed length character array");
		System.out.println("varchar(<length>) 		for 		variable length character array");
		System.out.println("date 				for 		Date");
		System.out.println("datetime 			for 		DateTime");
		System.out.println();
		System.out.println("DateTime format   		yyyy-MM-dd_hh:mm:ss");
		System.out.println("Date format  			yyyy-MM-dd");
		System.out.println();
		System.out.println("String and date values are to be enclosed within double quotes.".toUpperCase());
		System.out.println(line("*", 80));
	}

	public static void displaySchema() throws EOFException {
		int setCount = 0;
		try {
			RandomAccessFile schemataTableFile = new RandomAccessFile("information_schema.schemata.tbl", "rw");
			while (schemataTableFile.getFilePointer() < schemataTableFile.length()) {
				byte len = schemataTableFile.readByte();
				for (int i = 0; i < len; i++)
					System.out.print((char) schemataTableFile.readByte());
				System.out.println();
				setCount += 1;
			}
			schemataTableFile.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		System.out.println();
		System.out.println(setCount + " rows in set.".toUpperCase());
	}

	public static void use() throws EOFException {
		int flag = 0;
		inf = 0;
		try {
			RandomAccessFile pointerToFile = new RandomAccessFile("information_schema.schemata.tbl", "rw");
			do {
				Byte length = pointerToFile.readByte();
				String db = "";
				for (int i = 0; i < length; i++)
					db = db + (char) pointerToFile.readByte();
				if (db.equals(database))
					flag = 1;
			} while (pointerToFile.getFilePointer() != pointerToFile.length());
			if (flag == 1) {
				if (database.equals("information_schema"))
					inf = 1;
				System.out.println("Database Changed.");
			} else
				System.out.println("No Such database.");
			pointerToFile.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void displayTables() throws EOFException {
		int ds = databaseSelected();
		int setCount = 0;
		if ((ds == 0) || (inf == 1)) {
			try {
				RandomAccessFile schemataTableFile = new RandomAccessFile("information_schema.table.tbl", "rw");
				while (schemataTableFile.getFilePointer() != schemataTableFile.length()) {
					String db = "";
					String tb = "";
					byte len = schemataTableFile.readByte();
					for (int i = 0; i < len; i++)
						db = db + (char) schemataTableFile.readByte();
					len = schemataTableFile.readByte();
					for (int i = 0; i < len; i++)
						tb = tb + (char) schemataTableFile.readByte();
					if (db.equals(database)) {
						System.out.println(tb);
						setCount += 1;
					}
					schemataTableFile.readLong();
				}
				schemataTableFile.close();
			} catch (Exception e) {
				System.out.println(e);
			}
		} else
			System.out.println("Databse not selected");
		System.out.println();
		System.out.println(setCount + " rows in set.".toUpperCase());
	}

	public static int databaseSelected() throws EOFException {
		if (database.equals("information_schema"))
			return 1;
		else if (database.equals(""))
			return -1;
		else
			return 0;
	}

	public static void createSchema() throws EOFException {
		int flag = 0;
		try {
			RandomAccessFile pointerToFile = new RandomAccessFile("information_schema.schemata.tbl", "rw");
			do {
				String db = "";
				Byte length = pointerToFile.readByte();
				for (int i = 0; i < length; i++)
					db = db + (char) pointerToFile.readByte();
				if ((db.equals(database))) {
					flag = 1;
					break;
				}
			} while (pointerToFile.getFilePointer() != pointerToFile.length());
			if (flag == 1) {
				throw new Exception("Schema already exists with this name");
			} else {
				pointerToFile.skipBytes((int) pointerToFile.length());
				pointerToFile.writeByte(database.length());
				pointerToFile.writeBytes(database);
			}
			System.out.println();
			System.out.println("Query OK, 1 row afftected.");
			pointerToFile.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	static void createTable(String usercommand) throws EOFException {
		if (databaseSelected() == 0) {
			String temp;
			temp = usercommand.substring(usercommand.indexOf("(") + 1, usercommand.length() - 1);
			String[] b = temp.split(",");
			if (tableExists() == 0) {
				try {
					String filename = database + "." + table + "." + "tbl";
					RandomAccessFile newTableFile = new RandomAccessFile(filename, "rw");
					RandomAccessFile pointerToFile = new RandomAccessFile("information_schema.table.tbl", "rw");
					pointerToFile.skipBytes((int) pointerToFile.length());
					pointerToFile.writeByte(database.length());
					pointerToFile.writeBytes(database);
					pointerToFile.writeByte(table.length());
					pointerToFile.writeBytes(table);
					pointerToFile.writeLong(0);
					pointerToFile = new RandomAccessFile("information_schema.columns.tbl", "rw");
					for (int i = 0; i < b.length; i++) {
						int j = 0;
						String pri = "";
						String notnull = "YES";
						String[] c = b[i].split(" ");
						filename = database + "." + table + "." + c[0] + ".ndx";
						newTableFile = new RandomAccessFile(filename, "rw");
						pointerToFile.skipBytes((int) pointerToFile.length());
						pointerToFile.writeByte(database.length());
						pointerToFile.writeBytes(database);
						pointerToFile.writeByte(table.length());
						pointerToFile.writeBytes(table);
						pointerToFile.writeByte(c[j].length());
						pointerToFile.writeBytes(c[j]);
						j++;
						pointerToFile.writeInt(i + 1);
						pointerToFile.writeByte(c[j].length());
						pointerToFile.writeBytes(c[j]);
						for (j = 2; j < c.length; j++) {
							if (c[j].equals("notnull"))
								notnull = "NO";

							if (c[j].equals("primarykey")) {
								pri = "PRI";
								notnull = "NO";
							}
						}
						pointerToFile.writeByte(notnull.length());
						pointerToFile.writeBytes(notnull);
						pointerToFile.writeByte(pri.length());
						pointerToFile.writeBytes(pri);
					}
					System.out.println();
					System.out.println("QUERY OK, 0 rows affected.");
				} catch (Exception e) {
					System.out.println(e);
				}
			} else
				System.out.println(database + "." + table + " already exists");
		} else
			System.out.println("Database not selected");
	}

	public static int tableExists() throws EOFException {
		int flag = 0;
		try {
			RandomAccessFile pointerToFile = new RandomAccessFile("information_schema.table.tbl", "rw");
			do {
				String db = "";
				String tb = "";
				Byte length = pointerToFile.readByte();
				for (int i = 0; i < length; i++)
					db = db + (char) pointerToFile.readByte();
				length = pointerToFile.readByte();
				for (int i = 0; i < length; i++)
					tb = tb + (char) pointerToFile.readByte();
				pointerToFile.readLong();
				if ((tb.equals(table)) && (db.equals(database)))
					flag = 1;
			} while (pointerToFile.getFilePointer() != pointerToFile.length());
			if (flag == 1)
				return 1; // table exists with the name
			pointerToFile.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return 0;
	}

	public static void insertToTable(String usercommand) throws EOFException {
		if (databaseSelected() == 0)// DATABASE SELECETD OR NOT
		{
			if (tableExists() == 1)// IF A TABLE EXISTS
			{
				String temp;
				temp = usercommand.substring(usercommand.indexOf("(") + 1, usercommand.length() - 1);
				String[] values = temp.split(",");
				int originalColumnCount = columnCount();
				if (columnCount() == values.length)// COLUMN COUNT MATCH OR NOT
				{
					String[][] c = new String[originalColumnCount][4];
					c = getColumnInformation();
					int fl = 0;
					for (int z = 0; z < values.length; z++) {
						try // column type mismatch
						{
							if (values[z].equals("null"))
								System.out.println();
							else if (c[z][1].matches("byte"))
								Byte.parseByte(values[z]);
							else if (c[z][1].matches("int"))
								Integer.parseInt(values[z]);
							else if (c[z][1].matches("double"))
								Double.parseDouble(values[z]);
							else if (c[z][1].matches("float"))
								Float.parseFloat(values[z]);
							else if (c[z][1].matches("long") || (c[z][1].matches("longint")))
								Long.parseLong(values[z]);
							else if (c[z][1].matches("varchar(.*)")) {
								String l1 = c[z][1].replace("(", " ");
								l1 = l1.replace(")", " ");
								String[] n = l1.split(" ");
								int lent = Integer.parseInt(n[1]);
								values[z] = values[z].substring(1, (values[z].length() - 1));
								if (values[z].length() > lent)
									System.out.println("Out of bound");
							} else if (c[z][1].matches("char(.*)")) {
								String l1 = c[z][1].replace("(", " ");
								l1 = l1.replace(")", " ");
								String[] n = l1.split(" ");
								int lent = Integer.parseInt(n[1]);
								values[z] = values[z].substring(1, (values[z].length() - 1));
								if (values[z].length() > lent) {
									System.out.println("Out of bound");
									fl = 1;
								}
							} else if (c[z][1].matches("short") || (c[z][1].matches("shortint")))
								Short.parseShort(values[z]);
							else if (c[z][1].matches("datetime")) {
								values[z] = values[z].substring(1, values[z].length() - 1);
								String[] dat = values[z].split("_");
								String[] dt = dat[0].split("-");
								String[] tm = dat[1].split(":");
								if (dt.length != 3 || tm.length != 3)
									throw new Exception("Mismatch");
								if (dt[0].length() > 4)
									throw new Exception("Mismatch");
								if (dt[1].length() > 2)
									throw new Exception("Mismatch");
								if (dt[2].length() > 2)
									throw new Exception("Mismatch");
								if (tm[0].length() > 2)
									throw new Exception("Mismatch");
								if (tm[1].length() > 2)
									throw new Exception("Mismatch");
								if (tm[2].length() > 2)
									throw new Exception("Mismatch");
								if ((Integer.parseInt(dt[1]) > 12) || (Integer.parseInt(dt[1]) < 0)
										|| (Integer.parseInt(dt[2]) < 0) || (Integer.parseInt(dt[2]) > 31))
									throw new Exception("Mismatch");
								if ((Integer.parseInt(tm[0]) > 24) || (Integer.parseInt(tm[0]) < 0)
										|| (Integer.parseInt(tm[1]) < 0) || (Integer.parseInt(tm[1]) > 60)
										|| (Integer.parseInt(tm[2]) < 0) || (Integer.parseInt(tm[2]) > 60))
									throw new Exception("Mismatch");
								int month = Integer.parseInt(dt[1]);
								if (!(month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10
										|| month == 12)) {
									if (Integer.parseInt(dt[2]) == 31)
										throw new Exception("Wrong Date");
								}
								int year = Integer.parseInt(dt[0]);
								boolean isLeapYear = ((year % 4 == 0) && (year % 100 != 0) || (year % 400 == 0));
								int day = Integer.parseInt(dt[2]);
								if (!isLeapYear) {
									if ((month == 2) && (day > 28))
										throw new Exception("Mismatch");
								} else {
									if ((month == 2) && (day > 29))
										throw new Exception("Mismatch");
								}
							} else if (c[z][1].matches("date")) {
								String val = values[z].substring(1, values[z].length() - 1);
								String[] dt = val.split("-");
								if (dt.length != 3)
									throw new Exception("Mismatch");
								if (dt[0].length() > 4)
									throw new Exception("Mismatch");
								if (dt[1].length() > 2)
									throw new Exception("Mismatch");
								if (dt[2].length() > 2)
									throw new Exception("Mismatch");
								if ((Integer.parseInt(dt[1]) > 12) || (Integer.parseInt(dt[1]) < 0)
										|| (Integer.parseInt(dt[2]) < 0) || (Integer.parseInt(dt[2]) > 31))
									throw new Exception("Mismatch");
								int month = Integer.parseInt(dt[1]);
								if (!(month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10
										|| month == 12)) {
									if (Integer.parseInt(dt[2]) == 31)
										throw new Exception("Wrong Date");
								}
								int year = Integer.parseInt(dt[0]);
								boolean isLeapYear = ((year % 4 == 0) && (year % 100 != 0) || (year % 400 == 0));
								int day = Integer.parseInt(dt[2]);
								if (!isLeapYear) {
									if ((month == 2) && (day > 28))
										throw new Exception("Mismatch");
								} else {
									if ((month == 2) && (day > 29))
										throw new Exception("Mismatch");
								}

							}

							else {
								System.out.println("Wrong datatye");
								fl = 1;
							}
						} catch (Exception e) {
							fl = 1;
							System.out.println("Type mismatch");
						}
					}
					int cons = 0, constat = 1;
					if (fl == 0) {
						for (int z = 0; z < values.length; z++) {
							if (c[z][3].equals("PRI")) {
								cons = constraintCheckForInsert(c[z], values[z]);
							}
							if (c[z][2].equals("NO") && values[z].equals("null")) {
								constat = 0;
								System.out.println("Column " +( z+1) + " cannot be null");
								break;
							}
							if (cons == 1) {
								System.out.println("Duplicate Record");
								constat = 0;
								break;
							}
						}
					}
					if ((fl == 0) && (constat == 1))// No mismatch detected and
													// constraints satisfied
					{
						int fp;
						try {
							String filename = database + "." + table + ".tbl";
							RandomAccessFile pointerToFile = new RandomAccessFile(filename, "rw");
							fp = (int) pointerToFile.length();
							for (int z = 0; z < values.length; z++) {
								//System.out.println(values[z]);
								pointerToFile.seek(0);
								pointerToFile.skipBytes((int) pointerToFile.length());
								if (c[z][1].matches("byte")) {
									if (values[z].equals("null")) {
										pointerToFile.writeByte(Byte.MIN_VALUE);
										Byte v = (Byte.MIN_VALUE);
										writeToIndex(c[z][0], c[z][1], "null", fp);
									} else {
										pointerToFile.writeByte(Byte.parseByte(values[z]));
										writeToIndex(c[z][0], c[z][1], values[z], fp);
									}
								} else if (c[z][1].matches("int")) {
									if (values[z].equals("null")) {
										pointerToFile.writeInt(Integer.MIN_VALUE);
										Integer v = Integer.MIN_VALUE;
										writeToIndex(c[z][0], c[z][1], "null", fp);

									} else {
										pointerToFile.writeInt(Integer.parseInt(values[z]));
										writeToIndex(c[z][0], c[z][1], values[z], fp);
									}
								} else if (c[z][1].matches("double")) {
									if (values[z].equals("null")) {
										pointerToFile.writeDouble(Double.MIN_VALUE);
										Double v = Double.MIN_VALUE;
										writeToIndex(c[z][0], c[z][1], "null", fp);
									} else {
										pointerToFile.writeDouble(Double.parseDouble(values[z]));
										writeToIndex(c[z][0], c[z][1], values[z], fp);
									}
								} else if (c[z][1].matches("float")) {
									if (values[z].equals("null")) {
										pointerToFile.writeFloat(Float.MIN_VALUE);
										Float v = Float.MIN_VALUE;
										writeToIndex(c[z][0], c[z][1], "null", fp);
									} else {
										pointerToFile.writeFloat(Float.parseFloat(values[z]));
										writeToIndex(c[z][0], c[z][1], values[z], fp);
									}
								} else if (c[z][1].matches("long") || (c[z][1].matches("longint"))) {
									if (values[z].equals("null")) {
										pointerToFile.writeLong(Long.MIN_VALUE);
										Long v = Long.MIN_VALUE;
										writeToIndex(c[z][0], c[z][1], "null", fp);
									} else {
										pointerToFile.writeLong(Long.parseLong(values[z]));
										writeToIndex(c[z][0], c[z][1], values[z], fp);
									}
								} else if (c[z][1].matches("short") || (c[z][1].matches("shortint"))) {
									if (values[z].equals("null")) {
										pointerToFile.writeShort(Short.MIN_VALUE);
										Short v = Short.MIN_VALUE;
										writeToIndex(c[z][0], c[z][1], "null", fp);
									} else {
										pointerToFile.writeShort(Short.parseShort(values[z]));
										writeToIndex(c[z][0], c[z][1], values[z], fp);
									}
								} else if (c[z][1].matches("varchar(.*)")) {
									if (values[z].equals("null")) {
										pointerToFile.writeByte("null".length());
										pointerToFile.writeBytes("null");
										writeToIndex(c[z][0], c[z][1], "null", fp);
									} else {
										String l1 = c[z][1].replace("(", " ");
										l1 = l1.replace(")", " ");
										String[] n = l1.split(" ");
										int lent = Integer.parseInt(n[1]);
										if (values[z].length() <= lent) {
											pointerToFile.writeByte(values[z].length());
											pointerToFile.writeBytes(values[z]);
											writeToIndex(c[z][0], c[z][1], values[z], fp);
										}
									}
								} else if (c[z][1].matches("char(.*)")) {
									if (values[z].equals("null")) {
										pointerToFile.writeByte("null".length());
										pointerToFile.writeBytes("null");
										writeToIndex(c[z][0], c[z][1], "null", fp);
									} else {
										String l1 = c[z][1].replace("(", " ");
										l1 = l1.replace(")", " ");
										String[] n = l1.split(" ");
										int lent = Integer.parseInt(n[1]);
										if (values[z].length() == lent) {
											pointerToFile.writeByte(values[z].length());
											pointerToFile.writeBytes(values[z]);
											writeToIndex(c[z][0], c[z][1], values[z], fp);
										}
										if (values[z].length() < lent) {
											char[] a = new char[lent];
											char[] bh = values[z].toCharArray();
											for (int i = 0; i < bh.length; i++)
												a[i] = bh[i];
											for (int i = bh.length; i < a.length; i++)
												a[i] = '`';
											String ch = new String(a);
											pointerToFile.writeByte(ch.length());
											pointerToFile.writeBytes(ch);
											writeToIndex(c[z][0], c[z][1], ch, fp);
										}
									}
								} else if (c[z][1].equals("date")) {
									if (values[z].equals("null")) {
										pointerToFile.writeLong(Long.MIN_VALUE);
										Long v = Long.MIN_VALUE;
										writeToIndex(c[z][0], c[z][1], "null", fp);

									} else {
										values[z] = values[z].substring(1, values[z].length() - 1);
										String[] date = values[z].split("-");
										if (date[0].length() < 4)
											date[0] = String.format("%04d", Integer.parseInt(date[0]));
										if (date[1].length() < 2)
											date[1] = String.format("%02d", Integer.parseInt(date[1]));
										if (date[2].length() < 2) {
											date[2] = String.format("%02d", Integer.parseInt(date[2]));
										}
										String dat = date[0] + date[1] + date[2];
										long date1 = Long.parseLong(dat);
										pointerToFile.writeLong(date1);
										writeToIndex(c[z][0], c[z][1], values[z], fp);
									}
								} else if (c[z][1].equals("datetime")) {
									if (values[z].equals("null")) {
										pointerToFile.writeLong(Long.MIN_VALUE);
										Long v = Long.MIN_VALUE;
										writeToIndex(c[z][0], c[z][1], "null", fp);

									} else {
										String[] dateTime = values[z].split("_");
										String[] date = dateTime[0].split("-");
										String[] time = dateTime[1].split(":");
										// System.out.println(date[0]+"
										// "+date[1]+" "+date[2]+" "+time[0]+"
										// "+time[1]+" "+time[2]+" ");
										if (date[0].length() < 4)
											date[0] = String.format("%04d", Integer.parseInt(date[0]));
										if (date[1].length() < 2)
											date[1] = String.format("%02d", Integer.parseInt(date[1]));
										if (date[2].length() < 2) {
											date[2] = String.format("%02d", Integer.parseInt(date[2]));
										}

										if (time[0].length() < 2)
											time[0] = String.format("%02d", Integer.parseInt(time[0]));
										if (time[1].length() < 2)
											time[1] = String.format("%02d", Integer.parseInt(time[1]));
										if (time[2].length() < 2)
											time[2] = String.format("%02d", Integer.parseInt(time[2]));
										// System.out.println(date[0]+"
										// "+date[1]+" "+date[2]+" "+time[0]+"
										// "+time[1]+" "+time[2]+" ");
										String dat = date[0] + date[1] + date[2] + time[0] + time[1] + time[2];
										// System.out.println(dat);
										long date1 = Long.parseLong(dat);
										pointerToFile.writeLong(date1);
										writeToIndex(c[z][0], c[z][1], values[z], fp);
									}
								} else
									System.out.println("Wrong datatye");
							}
							System.out.println("Query OK" + " 1 row affected.");
							pointerToFile.close();
						} catch (Exception e) {
							System.out.println("Type mismatch");
						}
						try {
							RandomAccessFile pointerToFile = new RandomAccessFile("information_schema.table.tbl", "rw");// increasing
																														// number
																														// of
																														// rows
																														// in
																														// information_Schema.table
							do {
								String db = "";
								String tb = "";
								int len = pointerToFile.readByte();
								for (int i = 0; i < len; i++)
									db = db + (char) pointerToFile.readByte();
								len = pointerToFile.readByte();
								for (int i = 0; i < len; i++)
									tb = tb + (char) pointerToFile.readByte();
								long fp1 = pointerToFile.getFilePointer();
								long rows = pointerToFile.readLong();
								if (db.equals(database) && (tb.equals(table))) {
									pointerToFile.seek(fp1);
									pointerToFile.writeLong(rows + 1);
									break;
								}
							} while (pointerToFile.getFilePointer() <= pointerToFile.length());
							pointerToFile.close();
						} catch (Exception e) {
							System.out.println(e);
						}
					}
				} else
					System.out.println("Column count doesn't match value count at row 1");
			} else
				System.out.println(database + "." + table + " does not exist");
		} else
			System.out.println("Database not selected");
	}

	public static int columnCount() throws EOFException {
		int j = 0;
		try {
			RandomAccessFile pointerToFile = new RandomAccessFile("information_schema.columns.tbl", "rw");
			int count = 0;
			do {
				String db = "";
				String tb = "";
				String column_type = "";
				Byte length = pointerToFile.readByte();
				for (int k = 0; k < length; k++)
					db = db + (char) pointerToFile.readByte();
				length = pointerToFile.readByte();
				for (int k = 0; k < length; k++)
					tb = tb + (char) pointerToFile.readByte();
				length = pointerToFile.readByte();
				pointerToFile.skipBytes(length);
				pointerToFile.readInt();
				length = pointerToFile.readByte();
				for (int k = 0; k < length; k++)
					column_type = column_type + (char) pointerToFile.readByte();
				if ((table.equals(tb)) && (database.equals(db))) {
					j++;
				}
				length = pointerToFile.readByte();
				pointerToFile.skipBytes(length);
				length = pointerToFile.readByte();
				pointerToFile.skipBytes(length);
			} while (pointerToFile.getFilePointer() != pointerToFile.length());
			pointerToFile.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return j;
	}

	public static String[][] getColumnInformation() throws EOFException {
		int originalColumnCount = columnCount();
		String[][] c = new String[originalColumnCount][4];// Retrieving the
															// column name,
															// column type and
															// constraints of
															// the table
		int l = 0;
		try {
			RandomAccessFile pointerToFile = new RandomAccessFile("information_schema.columns.tbl", "rw");

			do {
				String db = "";
				String tb = "";
				String column_type = "";
				String column_name = "";
				Byte length = pointerToFile.readByte();
				for (int k = 0; k < length; k++)
					db = db + (char) pointerToFile.readByte();
				length = pointerToFile.readByte();
				for (int k = 0; k < length; k++)
					tb = tb + (char) pointerToFile.readByte();
				length = pointerToFile.readByte();
				for (int k = 0; k < length; k++)
					column_name = column_name + (char) pointerToFile.readByte();
				pointerToFile.readInt();
				length = pointerToFile.readByte();
				for (int k = 0; k < length; k++)
					column_type = column_type + (char) pointerToFile.readByte();
				String nullc = "";
				length = pointerToFile.readByte();
				for (int k = 0; k < length; k++)
					nullc = nullc + (char) pointerToFile.readByte();
				String pric = "";
				length = pointerToFile.readByte();
				for (int k = 0; k < length; k++)
					pric = pric + (char) pointerToFile.readByte();
				if ((table.equals(tb)) && (database.equals(db))) {
					c[l][0] = column_name;
					c[l][1] = column_type;
					c[l][2] = nullc;
					c[l][3] = pric;
					l++;
				}
			} while (pointerToFile.getFilePointer() != pointerToFile.length());
			pointerToFile.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return c;
	}

	public static int constraintCheckForInsert(String[] attribute, String Value) throws EOFException {
		String filename = database + "." + table + "." + attribute[0] + ".ndx";
		int duplicateFlag = 0;
		try {
			RandomAccessFile pointerToIndex = new RandomAccessFile(filename, "rw");
			ArrayList<Integer> a;
			if (attribute[1].equals("byte")) {
				Byte newval = Byte.parseByte(Value);
				Map<Byte, ArrayList<Integer>> map = new HashMap<Byte, ArrayList<Integer>>();
				Byte key;
				while (pointerToIndex.getFilePointer() < pointerToIndex.length()) {
					Byte keyy = pointerToIndex.readByte();
					int occ = 0;
					int count1 = pointerToIndex.readInt();
					a = new ArrayList<Integer>();
					while (occ < count1) {
						a.add(pointerToIndex.readInt());
						occ++;
					}
					map.put(keyy, a);
				}
				for (Byte ky : map.keySet()) {
					if (newval == ky)
						duplicateFlag = 1;
				}
			}
			if (attribute[1].equals("int")) {
				int newval = Integer.parseInt(Value);
				Map<Integer, ArrayList<Integer>> map = new HashMap<Integer, ArrayList<Integer>>();
				Integer key;
				while (pointerToIndex.getFilePointer() < pointerToIndex.length()) {
					Integer keyy = pointerToIndex.readInt();
					int occ = 0;
					int count1 = pointerToIndex.readInt();
					a = new ArrayList<Integer>();
					while (occ < count1) {
						a.add(pointerToIndex.readInt());
						occ++;
					}
					map.put(keyy, a);
				}
				for (Integer ky : map.keySet()) {
					if (newval == ky)
						duplicateFlag = 1;
				}
			}
			if (attribute[1].equals("float")) {
				float newval = Float.parseFloat(Value);
				Map<Float, ArrayList<Integer>> map = new HashMap<Float, ArrayList<Integer>>();
				Float key;
				while (pointerToIndex.getFilePointer() < pointerToIndex.length()) {
					Float keyy = pointerToIndex.readFloat();
					int occ = 0;
					int count1 = pointerToIndex.readInt();
					a = new ArrayList<Integer>();
					while (occ < count1) {
						a.add(pointerToIndex.readInt());
						occ++;
					}
					map.put(keyy, a);
				}
				for (Float ky : map.keySet()) {
					if (newval == ky)
						duplicateFlag = 1;
				}
			}
			if (attribute[1].equals("short") || attribute[1].equals("shortint")) {
				short newval = (short) Integer.parseInt(Value);
				Map<Short, ArrayList<Integer>> map = new HashMap<Short, ArrayList<Integer>>();
				Short key;
				while (pointerToIndex.getFilePointer() < pointerToIndex.length()) {
					Short keyy = pointerToIndex.readShort();
					int occ = 0;
					int count1 = pointerToIndex.readInt();
					a = new ArrayList<Integer>();
					while (occ < count1) {
						a.add(pointerToIndex.readInt());
						occ++;
					}
					map.put(keyy, a);
				}
				for (Short ky : map.keySet()) {
					if (newval == ky)
						duplicateFlag = 1;
				}
			}
			if (attribute[1].equals("double")) {
				double newval = Double.parseDouble(Value);
				Map<Double, ArrayList<Integer>> map = new HashMap<Double, ArrayList<Integer>>();
				Double key;
				while (pointerToIndex.getFilePointer() < pointerToIndex.length()) {
					Double keyy = pointerToIndex.readDouble();
					int occ = 0;
					int count1 = pointerToIndex.readInt();
					a = new ArrayList<Integer>();
					while (occ < count1) {
						a.add(pointerToIndex.readInt());
						occ++;
					}
					map.put(keyy, a);
				}
				for (Double ky : map.keySet()) {
					if (newval == ky)
						duplicateFlag = 1;
				}

			}
			if (attribute[1].equals("long") || attribute[1].equals("longint")) {
				long newval = Long.parseLong(Value);
				Map<Long, ArrayList<Integer>> map = new HashMap<Long, ArrayList<Integer>>();
				Long key;
				while (pointerToIndex.getFilePointer() < pointerToIndex.length()) {
					Long keyy = pointerToIndex.readLong();
					int occ = 0;
					int count1 = pointerToIndex.readInt();
					a = new ArrayList<Integer>();
					while (occ < count1) {
						a.add(pointerToIndex.readInt());
						occ++;
					}
					map.put(keyy, a);
				}
				for (Long ky : map.keySet()) {
					if (newval == ky)
						duplicateFlag = 1;
				}
			}
			if (attribute[1].matches("varchar(.*)")) {
				String newval = Value;
				String key = "";
				Map<String, ArrayList<Integer>> map = new HashMap<String, ArrayList<Integer>>();
				while (pointerToIndex.getFilePointer() < pointerToIndex.length()) {
					String keyy = "";
					Byte strlen = pointerToIndex.readByte();
					for (int p = 0; p < strlen; p++)
						keyy = keyy + (char) pointerToIndex.readByte();
					int occ = 0;
					int count1 = pointerToIndex.readInt();
					a = new ArrayList<Integer>();
					while (occ < count1) {
						a.add(pointerToIndex.readInt());
						occ++;
					}
					map.put(keyy, a);
				}
				for (String ky : map.keySet()) {
					if (newval.equals(ky)) {
						duplicateFlag = 1;
					}
				}
			}
			if (attribute[1].matches("char(.*)")) {
				Map<String, ArrayList<Integer>> map = new HashMap<String, ArrayList<Integer>>();
				attribute[1] = attribute[1].replace("(", " ");
				attribute[1] = attribute[1].replace(")", " ");
				String[] n = attribute[1].split(" ");
				char[] ap = new char[Integer.parseInt(n[1])];
				char[] bh = Value.toCharArray();
				for (int i = 0; i < bh.length; i++)
					ap[i] = bh[i];
				for (int i = bh.length; i < ap.length; i++)
					ap[i] = '`';
				String newval = new String(ap);
				// System.out.println(newval);
				while (pointerToIndex.getFilePointer() < pointerToIndex.length()) {
					String keyy = "";
					Byte strlen = pointerToIndex.readByte();
					for (int p = 0; p < strlen; p++)
						keyy = keyy + (char) pointerToIndex.readByte();
					// System.out.println(keyy);
					int occ = 0;
					int count1 = pointerToIndex.readInt();
					a = new ArrayList<Integer>();
					while (occ < count1) {
						a.add(pointerToIndex.readInt());
						occ++;
					}
					map.put(keyy, a);
				}
				for (String ky : map.keySet()) {
					if (newval.equals(ky)) {
						duplicateFlag = 1;
					}
				}
			}
			if (attribute[1].equals("date")) {
				String newval = Value.substring(1, Value.length() - 1);
				Map<Long, ArrayList<Integer>> map = new HashMap<Long, ArrayList<Integer>>();
				while (pointerToIndex.getFilePointer() < pointerToIndex.length()) {
					Long keyy = pointerToIndex.readLong();
					int occ = 0;
					int count1 = pointerToIndex.readInt();
					a = new ArrayList<Integer>();
					while (occ < count1) {
						a.add(pointerToIndex.readInt());
						occ++;
					}
					map.put(keyy, a);
				}
				String[] date = newval.split("-");
				if (date[0].length() < 4)
					date[0] = String.format("%04d", Integer.parseInt(date[0]));
				if (date[1].length() < 2)
					date[1] = String.format("%02d", Integer.parseInt(date[1]));
				if (date[2].length() < 2) {
					date[2] = String.format("%02d", Integer.parseInt(date[2]));
				}
				String pp = date[0] + date[1] + date[2];
				long date1 = Long.parseLong(pp);
				for (Long ky : map.keySet()) {
					if (ky == date1)
						duplicateFlag = 1;
				}
			}
			if (attribute[1].equals("datetime")) {
				String newval = Value;
				Map<Long, ArrayList<Integer>> map = new HashMap<Long, ArrayList<Integer>>();
				while (pointerToIndex.getFilePointer() < pointerToIndex.length()) {
					Long keyy = pointerToIndex.readLong();
					int occ = 0;
					int count1 = pointerToIndex.readInt();
					a = new ArrayList<Integer>();
					while (occ < count1) {
						a.add(pointerToIndex.readInt());
						occ++;
					}
					map.put(keyy, a);
				}
				String[] de = newval.split("_");
				String[] date = de[0].split("-");
				String[] time = de[1].split(":");
				if (date[0].length() < 4)
					date[0] = String.format("%04d", Integer.parseInt(date[0]));
				if (date[1].length() < 2)
					date[1] = String.format("%02d", Integer.parseInt(date[1]));
				if (date[2].length() < 2) {
					date[2] = String.format("%02d", Integer.parseInt(date[2]));
				}
				if (time[0].length() < 2)
					time[0] = String.format("%02d", Integer.parseInt(time[0]));
				if (time[1].length() < 2)
					time[1] = String.format("%02d", Integer.parseInt(time[1]));
				if (time[2].length() < 2)
					time[2] = String.format("%02d", Integer.parseInt(time[2]));
				String pp = date[0] + date[1] + date[2] + time[0] + time[1] + time[2];
				long date1 = Long.parseLong(pp);
				for (Long ky : map.keySet()) {
					if (ky == date1)
						duplicateFlag = 1;
				}
			}
			pointerToIndex.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return duplicateFlag;
	}

	public static void writeToIndex(String ColumnName, String ColumnType, String Value, int offset)
			throws EOFException {
		String indexf = database + "." + table + "." + ColumnName + ".ndx";
		ArrayList<Integer> a = new ArrayList<Integer>();
		int count;
		try {
			if (ColumnType.equals("byte")) {

				Map<Byte, ArrayList<Integer>> map = new HashMap<Byte, ArrayList<Integer>>();
				Byte key;
				RandomAccessFile pointerToIndex = new RandomAccessFile(indexf, "rw");
				while (pointerToIndex.getFilePointer() < pointerToIndex.length()) {
					Byte keyy = pointerToIndex.readByte();
					int occ = 0;
					int count1 = pointerToIndex.readInt();
					a = new ArrayList<Integer>();
					while (occ < count1) {
						a.add(pointerToIndex.readInt());
						occ++;
					}
					map.put(keyy, a);
				}
				int keyFound = 0;
				/*
				 * for(Byte ky:map.keySet()) System.out.println(ky);
				 */
				Byte newval = 0;
				if (!Value.equals("null"))
					newval = Byte.parseByte(Value);
				for (Byte ky : map.keySet()) {
					if ((ky == newval)) {
						keyFound = 1;
					}
					if ((ky == Byte.MIN_VALUE) && Value.equals("null"))
						keyFound = 1;
				}

				if (keyFound == 0) {
					if (Value.equals("null")) {
						a = new ArrayList<Integer>();
						a.add(offset);
						map.put(Byte.MIN_VALUE, a);
					} else {
						a = new ArrayList<Integer>();
						a.add(offset);
						map.put(Byte.parseByte(Value), a);
					}
				}
				if (keyFound == 1) {
					if (Value.equals("null")) {
						a = new ArrayList<Integer>(map.get(Byte.MIN_VALUE).size());
						a = map.get(Byte.MIN_VALUE);
						a.add(offset);
						map.put(Byte.MIN_VALUE, a);
					} else {
						a = new ArrayList<Integer>(map.get(newval).size());
						a = map.get(newval);
						a.add(offset);
						map.put(newval, a);
					}
				}
				pointerToIndex.close();
				pointerToIndex = new RandomAccessFile(indexf, "rw");
				for (Byte ky : map.keySet()) {
					pointerToIndex.writeByte(ky);
					a = new ArrayList<Integer>(map.get(ky).size());
					a = map.get(ky);
					pointerToIndex.writeInt(a.size());
					for (int p : a) {
						pointerToIndex.writeInt(p);
					}
				}
				pointerToIndex.close();
			}
			if (ColumnType.equals("int")) {
				int newval = 0;
				if (!Value.equals("null"))
					newval = Integer.parseInt(Value);
				Map<Integer, ArrayList<Integer>> map = new HashMap<Integer, ArrayList<Integer>>();
				Integer key;
				RandomAccessFile pointerToIndex = new RandomAccessFile(indexf, "rw");
				while (pointerToIndex.getFilePointer() < pointerToIndex.length()) {
					Integer keyy = pointerToIndex.readInt();
					int occ = 0;
					int count1 = pointerToIndex.readInt();
					a = new ArrayList<Integer>();
					while (occ < count1) {
						a.add(pointerToIndex.readInt());
						occ++;
					}
					map.put(keyy, a);
				}
				int keyFound = 0;
				for (Integer ky : map.keySet()) {
					if (ky == newval) {
						keyFound = 1;
					}
					if ((ky == Integer.MIN_VALUE) && Value.equals("null"))
						keyFound = 1;
				}
				if (keyFound == 0) {
					a = new ArrayList<Integer>();
					a.add(offset);
					map.put(newval, a);
				}
				if (keyFound == 1) {
					if (Value.equals("null")) {
						a = new ArrayList<Integer>(map.get(Integer.MIN_VALUE).size());
						a = map.get(Integer.MIN_VALUE);
						a.add(offset);
						map.put(Integer.MIN_VALUE, a);
					} else {
						a = new ArrayList<Integer>(map.get(newval).size());
						a = map.get(newval);
						a.add(offset);
						map.put(newval, a);
					}
				}
				pointerToIndex.close();
				pointerToIndex = new RandomAccessFile(indexf, "rw");
				for (Integer ky : map.keySet()) {
					pointerToIndex.writeInt(ky);
					a = new ArrayList<Integer>(map.get(ky).size());
					a = map.get(ky);
					pointerToIndex.writeInt(a.size());
					for (int p : a) {
						pointerToIndex.writeInt(p);
					}
				}
				pointerToIndex.close();
			}
			if (ColumnType.equals("float")) {
				float newval = 0;
				if (!Value.equals("null"))
					newval = Float.parseFloat(Value);
				Map<Float, ArrayList<Integer>> map = new HashMap<Float, ArrayList<Integer>>();
				Float key;
				RandomAccessFile pointerToIndex = new RandomAccessFile(indexf, "rw");
				while (pointerToIndex.getFilePointer() < pointerToIndex.length()) {
					Float keyy = pointerToIndex.readFloat();
					int occ = 0;
					int count1 = pointerToIndex.readInt();
					a = new ArrayList<Integer>();
					while (occ < count1) {
						a.add(pointerToIndex.readInt());
						occ++;
					}
					map.put(keyy, a);
				}
				int keyFound = 0;
				for (Float ky : map.keySet()) {
					if ((ky == newval)) {
						keyFound = 1;
					}
					if ((ky == Float.MIN_VALUE) && (Value.equals("null")))
						keyFound = 1;
				}
				if (keyFound == 0) {
					a = new ArrayList<Integer>();
					a.add(offset);
					map.put(newval, a);
				}
				if (keyFound == 1) {
					if (Value.equals("null")) {
						a = new ArrayList<Integer>(map.get(Float.MIN_VALUE).size());
						a = map.get(Float.MIN_VALUE);
						a.add(offset);
						map.put(Float.MIN_VALUE, a);
					} else {
						a = new ArrayList<Integer>(map.get(newval).size());
						a = map.get(newval);
						a.add(offset);
						map.put(newval, a);
					}

				}
				pointerToIndex.close();
				pointerToIndex = new RandomAccessFile(indexf, "rw");
				for (Float ky : map.keySet()) {
					pointerToIndex.writeFloat(ky);
					a = new ArrayList<Integer>(map.get(ky).size());
					a = map.get(ky);
					pointerToIndex.writeInt(a.size());
					for (int p : a) {
						pointerToIndex.writeInt(p);
					}
				}
				pointerToIndex.close();
			}
			if (ColumnType.equals("short") || ColumnType.equals("shortint")) {
				short newval = 0;
				if (!Value.equals("null"))
					newval = (short) Integer.parseInt(Value);
				Map<Short, ArrayList<Integer>> map = new HashMap<Short, ArrayList<Integer>>();
				Short key;
				RandomAccessFile pointerToIndex = new RandomAccessFile(indexf, "rw");
				while (pointerToIndex.getFilePointer() < pointerToIndex.length()) {
					Short keyy = pointerToIndex.readShort();
					int occ = 0;
					int count1 = pointerToIndex.readInt();
					a = new ArrayList<Integer>();
					while (occ < count1) {
						a.add(pointerToIndex.readInt());
						occ++;
					}
					map.put(keyy, a);
				}
				int keyFound = 0;
				for (Short ky : map.keySet()) {
					if ((ky == newval)) {
						keyFound = 1;
					}
					if ((Value.equals("null")) && (ky == Short.MIN_VALUE))
						keyFound = 1;
				}
				if (keyFound == 0) {
					a = new ArrayList<Integer>();
					a.add(offset);
					map.put(newval, a);
				}
				if (keyFound == 1) {
					if (Value.equals("null")) {
						a = new ArrayList<Integer>(map.get(Short.MIN_VALUE).size());
						a = map.get(Short.MIN_VALUE);
						a.add(offset);
						map.put(Short.MIN_VALUE, a);
					} else {
						a = new ArrayList<Integer>(map.get(newval).size());
						a = map.get(newval);
						a.add(offset);
						map.put(newval, a);
					}
				}
				pointerToIndex.close();
				pointerToIndex = new RandomAccessFile(indexf, "rw");
				for (Short ky : map.keySet()) {
					pointerToIndex.writeShort(ky);
					a = new ArrayList<Integer>(map.get(ky).size());
					a = map.get(ky);
					pointerToIndex.writeInt(a.size());
					for (int p : a) {
						pointerToIndex.writeInt(p);
					}
				}
				pointerToIndex.close();
			}
			if (ColumnType.equals("double")) {
				double newval = 0;
				if (!Value.equals("null"))
					newval = Double.parseDouble(Value);
				Map<Double, ArrayList<Integer>> map = new HashMap<Double, ArrayList<Integer>>();
				Double key;
				RandomAccessFile pointerToIndex = new RandomAccessFile(indexf, "rw");
				while (pointerToIndex.getFilePointer() < pointerToIndex.length()) {
					Double keyy = pointerToIndex.readDouble();
					int occ = 0;
					int count1 = pointerToIndex.readInt();
					a = new ArrayList<Integer>();
					while (occ < count1) {
						a.add(pointerToIndex.readInt());
						occ++;
					}
					map.put(keyy, a);
				}
				int keyFound = 0;
				for (Double ky : map.keySet()) {
					if ((ky == newval)) {
						keyFound = 1;
					}
					if ((ky == Double.MIN_VALUE) && (Value.equals("null")))
						keyFound = 1;
				}
				if (keyFound == 0) {
					a = new ArrayList<Integer>();
					a.add(offset);
					map.put(newval, a);
				}
				if (keyFound == 1) {
					if (Value.equals("null")) {
						a = new ArrayList<Integer>(map.get(Double.MIN_VALUE).size());
						a = map.get(Double.MIN_VALUE);
						a.add(offset);
						map.put(Double.MIN_VALUE, a);
					} else {
						a = new ArrayList<Integer>(map.get(newval).size());
						a = map.get(newval);
						a.add(offset);
						map.put(newval, a);
					}

				}
				pointerToIndex.close();
				pointerToIndex = new RandomAccessFile(indexf, "rw");
				for (Double ky : map.keySet()) {
					pointerToIndex.writeDouble(ky);
					a = new ArrayList<Integer>(map.get(ky).size());
					a = map.get(ky);
					pointerToIndex.writeInt(a.size());
					for (int p : a) {
						pointerToIndex.writeInt(p);
					}
				}
				pointerToIndex.close();
			}
			if (ColumnType.equals("long") || ColumnType.equals("longint")) {
				long newval = 0;
				if (!Value.equals("null"))
					newval = Long.parseLong(Value);
				Map<Long, ArrayList<Integer>> map = new HashMap<Long, ArrayList<Integer>>();
				Long key;
				RandomAccessFile pointerToIndex = new RandomAccessFile(indexf, "rw");
				while (pointerToIndex.getFilePointer() < pointerToIndex.length()) {
					Long keyy = pointerToIndex.readLong();
					int occ = 0;
					int count1 = pointerToIndex.readInt();
					a = new ArrayList<Integer>();
					while (occ < count1) {
						a.add(pointerToIndex.readInt());
						occ++;
					}
					map.put(keyy, a);
				}
				int keyFound = 0;
				for (Long ky : map.keySet()) {
					if ((ky == newval)) {
						keyFound = 1;
					}
					if ((ky == Long.MIN_VALUE) && (Value.equals("null")))
						keyFound = 1;
				}
				if (keyFound == 0) {
					a = new ArrayList<Integer>();
					a.add(offset);
					map.put(newval, a);
				}
				if (keyFound == 1) {
					if (Value.equals("null")) {
						a = new ArrayList<Integer>(map.get(Long.MIN_VALUE).size());
						a = map.get(Long.MIN_VALUE);
						a.add(offset);
						map.put(Long.MIN_VALUE, a);
					} else {
						a = new ArrayList<Integer>(map.get(newval).size());
						a = map.get(newval);
						a.add(offset);
						map.put(newval, a);
					}
				}
				pointerToIndex.close();
				pointerToIndex = new RandomAccessFile(indexf, "rw");
				for (Long ky : map.keySet()) {
					pointerToIndex.writeLong(ky);
					a = new ArrayList<Integer>(map.get(ky).size());
					a = map.get(ky);
					pointerToIndex.writeInt(a.size());
					for (int p : a) {
						pointerToIndex.writeInt(p);
					}
				}
				pointerToIndex.close();
			}
			if (ColumnType.matches("varchar(.*)")) {
				String newval = Value;
				// System.out.println(Value);
				Map<String, ArrayList<Integer>> map = new HashMap<String, ArrayList<Integer>>();
				RandomAccessFile pointerToIndex = new RandomAccessFile(indexf, "rw");
				while (pointerToIndex.getFilePointer() < pointerToIndex.length()) {
					String keyy = "";
					Byte strlen = pointerToIndex.readByte();
					for (int p = 0; p < strlen; p++)
						keyy = keyy + (char) pointerToIndex.readByte();
					// System.out.println(keyy);
					int occ = 0;
					int count1 = pointerToIndex.readInt();
					a = new ArrayList<Integer>();
					while (occ < count1) {
						a.add(pointerToIndex.readInt());
						occ++;
					}
					map.put(keyy, a);
				}
				int keyFound = 0;
				for (String ky : map.keySet()) {
					if (ky.equals(newval)) {
						keyFound = 1;
						// System.out.println("keyfound");
					}
					if (Value.equals("null") && ky.equals("null"))
						keyFound = 1;
				}
				if (keyFound == 0) {
					a = new ArrayList<Integer>();
					a.add(offset);
					map.put(newval, a);
				}
				if (keyFound == 1) {
					if (Value.equals("null")) {
						a = new ArrayList<Integer>(map.get("null").size());
						a = map.get("null");
						a.add(offset);
						map.put("null", a);
					} else {
						a = new ArrayList<Integer>(map.get(newval).size());
						a = map.get(newval);
						a.add(offset);
						map.put(newval, a);
					}

				}
				pointerToIndex.close();
				pointerToIndex = new RandomAccessFile(indexf, "rw");
				for (String ky : map.keySet()) {
					pointerToIndex.writeByte(ky.length());
					pointerToIndex.writeBytes(ky);
					a = new ArrayList<Integer>(map.get(ky).size());
					a = map.get(ky);
					pointerToIndex.writeInt(a.size());
					for (int p : a) {
						pointerToIndex.writeInt(p);
					}
				}
				pointerToIndex.close();
			}
			if (ColumnType.matches("char(.*)")) {
				String newval = Value;
				String key = "";
				// System.out.println(Value);
				Map<String, ArrayList<Integer>> map = new HashMap<String, ArrayList<Integer>>();
				RandomAccessFile pointerToIndex = new RandomAccessFile(indexf, "rw");
				while (pointerToIndex.getFilePointer() < pointerToIndex.length()) {
					String keyy = "";
					Byte strlen = pointerToIndex.readByte();
					for (int p = 0; p < strlen; p++)
						keyy = keyy + (char) pointerToIndex.readByte();
					// System.out.println(keyy);
					int occ = 0;
					int count1 = pointerToIndex.readInt();
					a = new ArrayList<Integer>();
					while (occ < count1) {
						a.add(pointerToIndex.readInt());
						occ++;
					}
					map.put(keyy, a);
				}
				int keyFound = 0;
				for (String ky : map.keySet()) {
					if ((ky.equals(newval))) {
						keyFound = 1;
						// System.out.println("keyfound");
					}
					if (ky.equals("null") && Value.equals("null"))
						keyFound = 1;
				}
				if (keyFound == 0) {
					a = new ArrayList<Integer>();
					a.add(offset);
					map.put(newval, a);
				}
				if (keyFound == 1) {
					if (Value.equals("null")) {
						a = new ArrayList<Integer>(map.get("null").size());
						a = map.get("null");
						a.add(offset);
						map.put("null", a);
					} else {
						a = new ArrayList<Integer>(map.get(newval).size());
						a = map.get(newval);
						a.add(offset);
						map.put(newval, a);
					}

				}
				pointerToIndex.close();
				pointerToIndex = new RandomAccessFile(indexf, "rw");
				for (String ky : map.keySet()) {
					pointerToIndex.writeByte(ky.length());
					pointerToIndex.writeBytes(ky);
					a = new ArrayList<Integer>(map.get(ky).size());
					a = map.get(ky);
					pointerToIndex.writeInt(a.size());
					for (int p : a) {
						pointerToIndex.writeInt(p);
					}
				}
				pointerToIndex.close();
			}
			if (ColumnType.equals("date")) {
				String newval = Value;
				// System.out.println(Value);
				Map<Long, ArrayList<Integer>> map = new HashMap<Long, ArrayList<Integer>>();
				RandomAccessFile pointerToIndex = new RandomAccessFile(indexf, "rw");
				while (pointerToIndex.getFilePointer() < pointerToIndex.length()) {
					Long keyy = pointerToIndex.readLong();
					// String keyy=Long.toString(d);
					int occ = 0;
					int count1 = pointerToIndex.readInt();
					a = new ArrayList<Integer>();
					while (occ < count1) {
						a.add(pointerToIndex.readInt());
						occ++;
					}
					map.put(keyy, a);
				}
				int keyFound = 0;
				long date1 = 0;
				if (!Value.equals("null")) {
					String[] date = newval.split("-");
					if (date[0].length() < 4)
						date[0] = String.format("%04d", Integer.parseInt(date[0]));
					if (date[1].length() < 2)
						date[1] = String.format("%02d", Integer.parseInt(date[1]));
					if (date[2].length() < 2) {
						date[2] = String.format("%02d", Integer.parseInt(date[2]));
					}
					String dat = date[0] + date[1] + date[2];
					date1 = Long.parseLong(dat);
				}
				for (Long ky : map.keySet()) {
					if ((ky == date1)) {
						keyFound = 1;
					}
					if (ky == Long.MIN_VALUE && Value.equals("null"))
						keyFound = 1;
				}
				if (keyFound == 0) {
					if (Value.equals("null")) {
						a = new ArrayList<Integer>();
						a.add(offset);
						map.put(Long.MIN_VALUE, a);
					} else {
						a = new ArrayList<Integer>();
						a.add(offset);
						map.put(date1, a);
					}
				}
				if (keyFound == 1) {
					if (Value.equals("null")) {
						// Integer siz=map.get(Long.MIN_VALUE).size();
						// System.out.println(Long.MIN_VALUE);
						a = new ArrayList<Integer>(map.get(Long.MIN_VALUE).size()); // System.out.println("hey");
						a = map.get(Long.MIN_VALUE);
						a.add(offset);
						map.put(Long.MIN_VALUE, a);
					} else {
						a = new ArrayList<Integer>(map.get(date1).size());
						a = map.get(date1);
						a.add(offset);
						map.put(date1, a);
					}
				}
				pointerToIndex.close();
				pointerToIndex = new RandomAccessFile(indexf, "rw");
				for (Long ky : map.keySet()) {
					pointerToIndex.writeLong(ky);
					a = new ArrayList<Integer>(map.get(ky).size());
					a = map.get(ky);
					pointerToIndex.writeInt(a.size());
					for (int p : a) {
						pointerToIndex.writeInt(p);
					}
				}
				pointerToIndex.close();
			}
			if (ColumnType.equals("datetime")) {
				String newval = Value;
				Map<Long, ArrayList<Integer>> map = new HashMap<Long, ArrayList<Integer>>();
				RandomAccessFile pointerToIndex = new RandomAccessFile(indexf, "rw");
				while (pointerToIndex.getFilePointer() < pointerToIndex.length()) {
					Long keyy = pointerToIndex.readLong();
					int occ = 0;
					int count1 = pointerToIndex.readInt();
					a = new ArrayList<Integer>();
					while (occ < count1) {
						a.add(pointerToIndex.readInt());
						occ++;
					}
					map.put(keyy, a);
				}
				long date1 = 0;
				if (!Value.equals("null")) {
					String[] de = newval.split("_");
					String[] date = de[0].split("-");
					String[] time = de[1].split(":");
					if (date[0].length() < 4)
						date[0] = String.format("%04d", Integer.parseInt(date[0]));
					if (date[1].length() < 2)
						date[1] = String.format("%02d", Integer.parseInt(date[1]));
					if (date[2].length() < 2) {
						date[2] = String.format("%02d", Integer.parseInt(date[2]));
					}
					if (time[0].length() < 2)
						time[0] = String.format("%02d", Integer.parseInt(time[0]));
					if (time[1].length() < 2)
						time[1] = String.format("%02d", Integer.parseInt(time[1]));
					if (time[2].length() < 2)
						time[2] = String.format("%02d", Integer.parseInt(time[2]));
					String pp = date[0] + date[1] + date[2] + time[0] + time[1] + time[2];
					date1 = Long.parseLong(pp);
				}
				int keyFound = 0;
				for (Long ky : map.keySet()) {
					if ((ky == date1))
						keyFound = 1;
					if (ky == Long.MIN_VALUE && Value.equals("null"))
						keyFound = 1;
				}
				if (keyFound == 0) {
					if (Value.equals("null")) {
						a = new ArrayList<Integer>();
						a.add(offset);
						map.put(Long.MIN_VALUE, a);
					} else {
						a = new ArrayList<Integer>();
						a.add(offset);
						map.put(date1, a);
					}
				}
				if (keyFound == 1) {
					if (Value.equals("null")) {
						a = new ArrayList<Integer>(map.get(Long.MIN_VALUE).size());
						a = map.get(Long.MIN_VALUE);
						a.add(offset);
						map.put(Long.MIN_VALUE, a);
					} else {
						a = new ArrayList<Integer>(map.get(date1).size());
						a = map.get(date1);
						a.add(offset);
						map.put(date1, a);
					}

				}
				pointerToIndex.close();
				pointerToIndex = new RandomAccessFile(indexf, "rw");
				for (Long ky : map.keySet()) {
					pointerToIndex.writeLong(ky);
					a = new ArrayList<Integer>(map.get(ky).size());
					a = map.get(ky);
					pointerToIndex.writeInt(a.size());
					for (int p : a) {
						pointerToIndex.writeInt(p);
					}
				}
				pointerToIndex.close();
			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void createInfoSchema() throws EOFException {
		try {

			// three files one for schema, one for tables and one for columns
			RandomAccessFile schemataTableFile = new RandomAccessFile("information_schema.schemata.tbl", "rw");
			RandomAccessFile tablesTableFile = new RandomAccessFile("information_schema.table.tbl", "rw");
			RandomAccessFile columnsTableFile = new RandomAccessFile("information_schema.columns.tbl", "rw");

			// table 'schemata' initial values
			schemataTableFile.writeByte("information_schema".length());
			schemataTableFile.writeBytes("information_schema");

			// table 'table' initial values
			tablesTableFile.writeByte("information_schema".length());
			tablesTableFile.writeBytes("information_schema");
			tablesTableFile.writeByte("SCHEMATA".length());
			tablesTableFile.writeBytes("SCHEMATA");
			tablesTableFile.writeLong(1);

			tablesTableFile.writeByte("information_schema".length());
			tablesTableFile.writeBytes("information_schema");
			tablesTableFile.writeByte("TABLES".length());
			tablesTableFile.writeBytes("TABLES");
			tablesTableFile.writeLong(3);

			tablesTableFile.writeByte("information_schema".length());
			tablesTableFile.writeBytes("information_schema");
			tablesTableFile.writeByte("COLUMNS".length());
			tablesTableFile.writeBytes("COLUMNS");
			tablesTableFile.writeLong(7);

			// table 'columns' initial' values
			columnsTableFile.writeByte("information_schema".length());
			columnsTableFile.writeBytes("information_schema");
			columnsTableFile.writeByte("SCHEMATA".length());
			columnsTableFile.writeBytes("SCHEMATA");
			columnsTableFile.writeByte("SCHEMA_NAME".length());
			columnsTableFile.writeBytes("SCHEMA_NAME");
			columnsTableFile.writeInt(1);
			columnsTableFile.writeByte("varchar(64)".length());
			columnsTableFile.writeBytes("varchar(64)");
			columnsTableFile.writeByte("NO".length());
			columnsTableFile.writeBytes("NO");
			columnsTableFile.writeByte("".length());
			columnsTableFile.writeBytes("");

			columnsTableFile.writeByte("information_schema".length());
			columnsTableFile.writeBytes("information_schema");
			columnsTableFile.writeByte("TABLES".length());
			columnsTableFile.writeBytes("TABLES");
			columnsTableFile.writeByte("TABLE_SCHEMA".length());
			columnsTableFile.writeBytes("TABLE_SCHEMA");
			columnsTableFile.writeInt(1);
			columnsTableFile.writeByte("varchar(64)".length());
			columnsTableFile.writeBytes("varchar(64)");
			columnsTableFile.writeByte("NO".length());
			columnsTableFile.writeBytes("NO");
			columnsTableFile.writeByte("".length());
			columnsTableFile.writeBytes("");

			columnsTableFile.writeByte("information_schema".length());
			columnsTableFile.writeBytes("information_schema");
			columnsTableFile.writeByte("TABLES".length());
			columnsTableFile.writeBytes("TABLES");
			columnsTableFile.writeByte("TABLE_NAME".length());
			columnsTableFile.writeBytes("TABLE_NAME");
			columnsTableFile.writeInt(2);
			columnsTableFile.writeByte("varchar(64)".length());
			columnsTableFile.writeBytes("varchar(64)");
			columnsTableFile.writeByte("NO".length());
			columnsTableFile.writeBytes("NO");
			columnsTableFile.writeByte("".length());
			columnsTableFile.writeBytes("");

			columnsTableFile.writeByte("information_schema".length());
			columnsTableFile.writeBytes("information_schema");
			columnsTableFile.writeByte("TABLES".length());
			columnsTableFile.writeBytes("TABLES");
			columnsTableFile.writeByte("TABLE_ROWS".length());
			columnsTableFile.writeBytes("TABLE_ROWS");
			columnsTableFile.writeInt(3);
			columnsTableFile.writeByte("long int".length());
			columnsTableFile.writeBytes("long int");
			columnsTableFile.writeByte("NO".length());
			columnsTableFile.writeBytes("NO");
			columnsTableFile.writeByte("".length());
			columnsTableFile.writeBytes("");

			columnsTableFile.writeByte("information_schema".length());
			columnsTableFile.writeBytes("information_schema");
			columnsTableFile.writeByte("COLUMNS".length());
			columnsTableFile.writeBytes("COLUMNS");
			columnsTableFile.writeByte("TABLE_SCHEMA".length());
			columnsTableFile.writeBytes("TABLE_SCHEMA");
			columnsTableFile.writeInt(1);
			columnsTableFile.writeByte("varchar(64)".length());
			columnsTableFile.writeBytes("varchar(64)");
			columnsTableFile.writeByte("NO".length());
			columnsTableFile.writeBytes("NO");
			columnsTableFile.writeByte("".length());
			columnsTableFile.writeBytes("");

			columnsTableFile.writeByte("information_schema".length());
			columnsTableFile.writeBytes("information_schema");
			columnsTableFile.writeByte("COLUMNS".length());
			columnsTableFile.writeBytes("COLUMNS");
			columnsTableFile.writeByte("TABLE_NAME".length());
			columnsTableFile.writeBytes("TABLE_NAME");
			columnsTableFile.writeInt(2);
			columnsTableFile.writeByte("varchar(64)".length());
			columnsTableFile.writeBytes("varchar(64)");
			columnsTableFile.writeByte("NO".length());
			columnsTableFile.writeBytes("NO");
			columnsTableFile.writeByte("".length());
			columnsTableFile.writeBytes("");

			columnsTableFile.writeByte("information_schema".length());
			columnsTableFile.writeBytes("information_schema");
			columnsTableFile.writeByte("COLUMNS".length());
			columnsTableFile.writeBytes("COLUMNS");
			columnsTableFile.writeByte("COLUMN_NAME".length());
			columnsTableFile.writeBytes("COLUMN_NAME");
			columnsTableFile.writeInt(3);
			columnsTableFile.writeByte("varchar(64)".length());
			columnsTableFile.writeBytes("varchar(64)");
			columnsTableFile.writeByte("NO".length());
			columnsTableFile.writeBytes("NO");
			columnsTableFile.writeByte("".length());
			columnsTableFile.writeBytes("");

			columnsTableFile.writeByte("information_schema".length());
			columnsTableFile.writeBytes("information_schema");
			columnsTableFile.writeByte("COLUMNS".length());
			columnsTableFile.writeBytes("COLUMNS");
			columnsTableFile.writeByte("ORDINAL_POSITION".length());
			columnsTableFile.writeBytes("ORDINAL_POSITION");
			columnsTableFile.writeInt(4);
			columnsTableFile.writeByte("int".length());
			columnsTableFile.writeBytes("int");
			columnsTableFile.writeByte("NO".length());
			columnsTableFile.writeBytes("NO");
			columnsTableFile.writeByte("".length());
			columnsTableFile.writeBytes("");

			columnsTableFile.writeByte("information_schema".length());
			columnsTableFile.writeBytes("information_schema");
			columnsTableFile.writeByte("COLUMNS".length());
			columnsTableFile.writeBytes("COLUMNS");
			columnsTableFile.writeByte("COLUMN_TYPE".length());
			columnsTableFile.writeBytes("COLUMN_TYPE");
			columnsTableFile.writeInt(5);
			columnsTableFile.writeByte("varchar(64)".length());
			columnsTableFile.writeBytes("varchar(64)");
			columnsTableFile.writeByte("NO".length());
			columnsTableFile.writeBytes("NO");
			columnsTableFile.writeByte("".length());
			columnsTableFile.writeBytes("");

			columnsTableFile.writeByte("information_schema".length());
			columnsTableFile.writeBytes("information_schema");
			columnsTableFile.writeByte("COLUMNS".length());
			columnsTableFile.writeBytes("COLUMNS");
			columnsTableFile.writeByte("IS_NULLABLE".length());
			columnsTableFile.writeBytes("IS_NULLABLE");
			columnsTableFile.writeInt(6);
			columnsTableFile.writeByte("varchar(3)".length());
			columnsTableFile.writeBytes("varchar(3)");
			columnsTableFile.writeByte("NO".length());
			columnsTableFile.writeBytes("NO");
			columnsTableFile.writeByte("".length());
			columnsTableFile.writeBytes("");

			columnsTableFile.writeByte("information_schema".length());
			columnsTableFile.writeBytes("information_schema");
			columnsTableFile.writeByte("COLUMNS".length());
			columnsTableFile.writeBytes("COLUMNS");
			columnsTableFile.writeByte("COLUMN_KEY".length());
			columnsTableFile.writeBytes("COLUMN_KEY");
			columnsTableFile.writeInt(7);
			columnsTableFile.writeByte("varchar(3)".length());
			columnsTableFile.writeBytes("varchar(3)");
			columnsTableFile.writeByte("NO".length());
			columnsTableFile.writeBytes("NO");
			columnsTableFile.writeByte("".length());
			columnsTableFile.writeBytes("");
			schemataTableFile.close();

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void selectFromSchemata() throws EOFException {
		try {
			RandomAccessFile pointerToSchemata = new RandomAccessFile("information_schema.schemata.tbl", "rw");
			System.out.println("SCHEMA_NAME");
			int setCount = 0;
			while (pointerToSchemata.getFilePointer() < pointerToSchemata.length()) {
				int length = (int) pointerToSchemata.readByte();
				String schema = "";
				for (int i = 0; i < length; i++)
					schema = schema + (char) pointerToSchemata.readByte();
				System.out.println(schema);
				setCount += 1;
			}
			System.out.println();
			System.out.println(setCount + " rows in set.".toUpperCase());

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void selectFromColumns() throws EOFException {
		int setCount = 0;
		try {
			RandomAccessFile pointerToSchemata = new RandomAccessFile("information_schema.columns.tbl", "rw");
			System.out.println(
					"TABLE_SCHEMA || TABLE_NAME || COLUMN_NAME || ORDINAL_POSITION || COLUMN_TYPE || IS_NULLABLE || COLUMN_KEY");
			while (pointerToSchemata.getFilePointer() < pointerToSchemata.length()) {
				int length = (int) pointerToSchemata.readByte();
				String schema = "";
				for (int i = 0; i < length; i++)
					schema = schema + (char) pointerToSchemata.readByte();
				length = (int) pointerToSchemata.readByte();
				String tab = "";
				for (int i = 0; i < length; i++)
					tab = tab + (char) pointerToSchemata.readByte();
				length = (int) pointerToSchemata.readByte();
				String cname = "";
				for (int i = 0; i < length; i++)
					cname = cname + (char) pointerToSchemata.readByte();
				int op = pointerToSchemata.readInt();
				length = (int) pointerToSchemata.readByte();
				String ctype = "";
				for (int i = 0; i < length; i++)
					ctype = ctype + (char) pointerToSchemata.readByte();
				length = (int) pointerToSchemata.readByte();
				String nullable = "";
				for (int i = 0; i < length; i++)
					nullable = nullable + (char) pointerToSchemata.readByte();
				length = (int) pointerToSchemata.readByte();
				String ckey = "";
				for (int i = 0; i < length; i++)
					ckey = ckey + (char) pointerToSchemata.readByte();
				System.out.println(schema + " || " + tab + " || " + cname + " || " + op + " || " + ctype + " || "
						+ nullable + " || " + ckey);
				setCount += 1;
			}
			System.out.println();
			System.out.println(setCount + " rows in set.".toUpperCase());
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	public static void selectFromTable() throws EOFException
	{
		int setCount=0;
		try
		{
			RandomAccessFile pointerToSchemata=new RandomAccessFile("information_schema.table.tbl","rw");
			System.out.println("TABLE_SCHEMA || TABLE_NAME || TABLE_ROWS");
			while(pointerToSchemata.getFilePointer()<pointerToSchemata.length())
			{
				int length=(int)pointerToSchemata.readByte();
				String schema="";
				for(int i=0;i<length;i++)
					schema=schema+(char)pointerToSchemata.readByte();
				length=(int)pointerToSchemata.readByte();
				String tab="";
				for(int i=0;i<length;i++)
					tab=tab+(char)pointerToSchemata.readByte();
				Long rows=pointerToSchemata.readLong();
				System.out.println(schema+" || "+tab+" || "+rows);
				setCount+=1;
			}
			pointerToSchemata.close();
			System.out.println();
			System.out.println(setCount+" rows in set.".toUpperCase());
		} 
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	public static void selectAll() throws EOFException
	{
		if(databaseSelected()==1)
		{
			if(table.equals("schemata"))
				selectFromSchemata();
			if(table.equals("table"))
				selectFromTable();
			if(table.equals("columns"))
				selectFromColumns();
		}
		else if(databaseSelected()==0)//DATABASE SELECETD OR NOT
		{
			if(tableExists()==1)//IF A TABLE EXISTS
			{
					String filename=database+"."+table+".tbl";
					int originalColumnCount=columnCount();
					String[][] c=new String[originalColumnCount][4];//Retrieving the column name, column type and constraints of the table
					c=getColumnInformation();	
					for(int i=0;i<originalColumnCount;i++)
						System.out.print(" || "+c[i][0]);
					System.out.println();
					int setCount=0;
						try
						{
							RandomAccessFile pointerToFile=new RandomAccessFile(filename,"rw");
							do
							{
								for( int i=0;i<originalColumnCount;i++)
									{
									if(c[i][1].equals("byte"))
									{
										Byte as=pointerToFile.readByte();
										if(as==Byte.MIN_VALUE)
											System.out.print(" || "+null);
										else
											System.out.print(" || "+as);
									}
									if(c[i][1].equals("int"))
									{
										Integer as=pointerToFile.readInt();
										if(as==Integer.MIN_VALUE)
											System.out.print(" || "+null);
										else
											System.out.print(" || "+as);
									}
									if(c[i][1].equals("float"))
									{
										Float as=pointerToFile.readFloat();
										if(as==Float.MIN_VALUE)
											System.out.print(" || "+null);
										else
											System.out.print(" || "+as);
									}
									if(c[i][1].equals("double"))
									{
										Double as=pointerToFile.readDouble();
										if(as==Double.MIN_VALUE)
											System.out.print(" || "+null);
										else
											System.out.print(" || "+as);
									}
									if(c[i][1].equals("short")||c[i][1].equals("shortint"))
										{
											Short as=pointerToFile.readShort();
											if(as==Short.MIN_VALUE)
												System.out.print(" || "+null);
											else
												System.out.print(" || "+as);
										}
									if(c[i][1].equals("long")||c[i][1].equals("longint"))
									{
										Long as=pointerToFile.readLong();
										if(as==Long.MIN_VALUE)
											System.out.print(" || "+null);
										else
											System.out.print(" || "+as);
									}
									if(c[i][1].matches("varchar(.*)"))
									{
										int len=(int)(pointerToFile.readByte());
										String val="";
										for(int p=0;p<len;p++)
										{
											val=val+(char)pointerToFile.readByte();
										}
										System.out.print(" || "+val);	
									}
									if(c[i][1].matches("char(.*)"))
									{
										int len=(int)(pointerToFile.readByte());
										String val="";
										for(int p=0;p<len;p++)
										{
											val=val+(char)pointerToFile.readByte();
										}
										String v=(String) val.subSequence(0,val.indexOf('`'));
										System.out.print(" || "+v);	
									}
									if(c[i][1].equals("date"))
									{
										Long dat1=pointerToFile.readLong();
										if(dat1!=Long.MIN_VALUE)
										{
											String a=dat1.toString();
											a=String.format("%08d", Integer.parseInt(a));
											String date1="";
											String date2="";
											String date3="";
											for(int y=0;y<4;y++)
												date1=date1+a.charAt(y);
											for(int y=4;y<6;y++)
												date2=date2+a.charAt(y);
											for(int y=6;y<8;y++)
												date3=date3+a.charAt(y);
											String formattedDate=date1+"-"+date2+"-"+date3;
											System.out.print(" || "+formattedDate);
										}
										else
										{
											System.out.println(" || null");
										}
									}
									if(c[i][1].equals("datetime"))
									{
										Long dat1=pointerToFile.readLong();
										if(dat1!=Long.MIN_VALUE)
										{
											String a=dat1.toString();
											a=String.format("%014d", Long.parseLong(a));
											String date1="";
											String date2="";
											String date3="",date4="",date5="",date6="";
											for(int y=0;y<4;y++)
												date1=date1+a.charAt(y);
											for(int y=4;y<6;y++)
												date2=date2+a.charAt(y);
											for(int y=6;y<8;y++)
												date3=date3+a.charAt(y);
											for(int y=8;y<10;y++)
												date4=date4+a.charAt(y);
											for(int y=10;y<12;y++)
												date5=date5+a.charAt(y);
											for(int y=12;y<14;y++)
												date6=date6+a.charAt(y);
											String formattedDate=date1+"-"+date2+"-"+date3+"_"+date4+":"+date5+":"+date6;
											System.out.print(" || "+formattedDate);
										}
										else
											System.out.println(" || null");
									}
									}
								setCount+=1;
								System.out.println();
							}while(pointerToFile.getFilePointer()<pointerToFile.length());	
							System.out.println();
							System.out.println(setCount+" rows in set.".toUpperCase());
							pointerToFile.close();
						}
						catch(Exception e)
						{
							System.out.println(e);
						}
			}
			else
				System.out.println(database+"."+table+" does not exist");
		}
		else
			System.out.println("Database not selected");
}
	public static void selectSpecific(String Command) throws EOFException
	{
		if(databaseSelected()==0)//DATABASE SELECETD OR NOT
		{
			if(tableExists()==1)//IF A TABLE EXISTS
			{
				String filename=database+"."+table+".tbl";
				String[] usr=Command.split(" ");
				String[] fin6=usr[5].split("=");
				String[] fin=new String[2];
				int equalTo=0,notEqualTo=0,lessThan=0,GreaterThan=0,lessThanEqualTo=0,GreaterThanEqualTo=0;
				if(fin6.length>1)
				{//equal to
					for(int w=0;w<fin6.length;w++)
						fin[w]=fin6[w];
					equalTo=1;
				}
				String com=usr[5].replace('!',' ');
				com=com.replace('=',' ');
				String[] fin1=com.split("  ");
				if((fin1.length>1)&&(equalTo==1))
				{
					for(int w=0;w<fin1.length;w++)
						fin[w]=fin1[w];
					equalTo=0;
					notEqualTo=1;
				}
				String[] fin2=usr[5].split("<");
				if((fin2.length>1)&&(equalTo==0))
				{
					for(int w=0;w<fin2.length;w++)
						fin[w]=fin2[w];
					lessThan=1;
				}
				String[] fin3=usr[5].split(">");
				if((fin3.length>1)&&(equalTo==0))
				{
					for(int w=0;w<fin3.length;w++)
						fin[w]=fin3[w];
					GreaterThan=1;
				}
				com=usr[5].replace('<',' ');
				com=com.replace('=',' ');
				//System.out.println(com);
				String[] fin4=com.split("  ");
				if((fin4.length>1)&&(equalTo==1))
				{
					for(int w=0;w<fin4.length;w++)
						fin[w]=fin4[w];
					equalTo=0;
					notEqualTo=0;
					lessThanEqualTo=1;
				}
				com=usr[5].replace('>',' ');
				com=com.replace('=',' ');
				String[] fin5=com.split("  ");
				if((fin5.length>1)&&(equalTo==1))
				{
					for(int w=0;w<fin5.length;w++)
						fin[w]=fin5[w];
					equalTo=0;
					notEqualTo=0;
					lessThanEqualTo=0;
					GreaterThanEqualTo=1;
				}
				//for(int w=0;w<fin.length;w++)
					//System.out.println(fin[w]);
				//System.out.println(" "+equalTo+" "+notEqualTo+" "+lessThan+" "+GreaterThan+" "+lessThanEqualTo+" "+GreaterThanEqualTo);
				String index=database+"."+table+"."+fin[0]+".ndx";
				try
				{
					RandomAccessFile pointerToFile=new RandomAccessFile(filename,"rw");
					RandomAccessFile pointerToIndex=new RandomAccessFile(index,"rw");
					RandomAccessFile pointer = new RandomAccessFile("information_schema.columns.tbl", "rw");
					int originalColumnCount=columnCount();
					String[][] c=new String[originalColumnCount][4];//Retrieving the column name, column type and constraints of the table
					c=getColumnInformation();
					for(int i=0;i<originalColumnCount;i++)
						System.out.print(" || "+c[i][0]);
					System.out.println();
					int l=0;
					String Type=getColumnType(fin[0]);
					ArrayList<Integer> a;
					if(Type.equals("byte"))
					{
						Map<Byte, ArrayList<Integer>> map = new HashMap<Byte,ArrayList<Integer>>();
						Byte key;
						while(pointerToIndex.getFilePointer()<pointerToIndex.length())
						{
							Byte keyy=pointerToIndex.readByte();
							int occ=0;
							int count1=pointerToIndex.readInt();
							a=new ArrayList<Integer>();
							while(occ<count1){
								a.add(pointerToIndex.readInt());occ++;}
							map.put(keyy,a);
						}
						ArrayList<Integer> position=new ArrayList<Integer>();
						/*for(Byte ky:map.keySet())
							System.out.println(ky);*/
						for(Byte ky:map.keySet())
						{
							if(fin[1].equals("null"))
							{
								if(ky==Byte.MIN_VALUE)
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
							}
							else
							{
								if(ky==Byte.parseByte(fin[1])&&(equalTo==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if(ky<Byte.parseByte(fin[1])&&(lessThan==1))
								{
									position=map.get(ky);
									
									for(int offset:position)
										sel(offset);
								}
								if(ky>Byte.parseByte(fin[1])&&(GreaterThan==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if(ky<=Byte.parseByte(fin[1])&&(lessThanEqualTo==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if(ky>=Byte.parseByte(fin[1])&&(GreaterThanEqualTo==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if(ky!=Byte.parseByte(fin[1])&&(notEqualTo==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
							}
							
						}	
					}
					if(Type.equals("int"))
					{
						Map<Integer, ArrayList<Integer>> map = new HashMap<Integer,ArrayList<Integer>>();
						Integer key;
						while(pointerToIndex.getFilePointer()<pointerToIndex.length())
						{
							Integer keyy=pointerToIndex.readInt();
							int occ=0;
							int count1=pointerToIndex.readInt();
							a=new ArrayList<Integer>();
							while(occ<count1){
								a.add(pointerToIndex.readInt());occ++;}
							map.put(keyy,a);
						}
						ArrayList<Integer> position=new ArrayList<Integer>();
						for(Integer ky:map.keySet())
						{
							if(fin[1].equals("null"))
							{
								if(ky==Integer.MIN_VALUE)
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
							}
							else
							{
								if(ky==Integer.parseInt(fin[1])&&(equalTo==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if(ky<Integer.parseInt(fin[1])&&(lessThan==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if(ky>Integer.parseInt(fin[1])&&(GreaterThan==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if(ky<=Integer.parseInt(fin[1])&&(lessThanEqualTo==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if(ky>=Integer.parseInt(fin[1])&&(GreaterThanEqualTo==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if(ky!=Integer.parseInt(fin[1])&&(notEqualTo==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
							}
						}
					}
				if(Type.equals("float"))
				{
						Map<Float, ArrayList<Integer>> map = new HashMap<Float,ArrayList<Integer>>();
						Float key;
						while(pointerToIndex.getFilePointer()<pointerToIndex.length())
						{
							Float keyy=pointerToIndex.readFloat();
							int occ=0;
							int count1=pointerToIndex.readInt();
							a=new ArrayList<Integer>();
							while(occ<count1){
								a.add(pointerToIndex.readInt());occ++;}
							map.put(keyy,a);
						}
						ArrayList<Integer> position=new ArrayList<Integer>();
						for(Float ky:map.keySet())
						{
							if(fin[1].equals("null"))
							{
								if(ky==Float.MIN_VALUE)
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
							}
							else
							{
								if(ky==Float.parseFloat(fin[1])&&(equalTo==1))
								
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if(ky<Float.parseFloat(fin[1])&&(lessThan==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if(ky>Float.parseFloat(fin[1])&&(GreaterThan==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if(ky<=Float.parseFloat(fin[1])&&(lessThanEqualTo==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if(ky>=Float.parseFloat(fin[1])&&(GreaterThanEqualTo==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if(ky!=Float.parseFloat(fin[1])&&(notEqualTo==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
						}}
					}
				if(Type.equals("short")||Type.equals("shortint"))
				{
					Map<Short, ArrayList<Integer>> map = new HashMap<Short,ArrayList<Integer>>();
					Short key;
					while(pointerToIndex.getFilePointer()<pointerToIndex.length())
					{
						Short keyy=pointerToIndex.readShort();
						int occ=0;
						int count1=pointerToIndex.readInt();
						a=new ArrayList<Integer>();
						while(occ<count1){
							a.add(pointerToIndex.readInt());occ++;}
						map.put(keyy,a);
					}
					ArrayList<Integer> position=new ArrayList<Integer>();
					for(Short ky:map.keySet())
					{
						if(fin[1].equals("null"))
						{
							if(ky==Short.MIN_VALUE)
							{
								position=map.get(ky);
								for(int offset:position)
									sel(offset);
							}
						}
						else
							{if(ky==Short.parseShort(fin[1])&&(equalTo==1))
							{
								position=map.get(ky);
								for(int offset:position)
									sel(offset);
							}
								if(ky<Short.parseShort(fin[1])&&(lessThan==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if(ky>Short.parseShort(fin[1])&&(GreaterThan==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if(ky<=Short.parseShort(fin[1])&&(lessThanEqualTo==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if(ky>=Short.parseShort(fin[1])&&(GreaterThanEqualTo==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if(ky!=Short.parseShort(fin[1])&&(notEqualTo==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
					}}
					}
				if(Type.equals("double"))
				{
					Map<Double, ArrayList<Integer>> map = new HashMap<Double,ArrayList<Integer>>();
					Double key;
					while(pointerToIndex.getFilePointer()<pointerToIndex.length())
					{
						Double keyy=pointerToIndex.readDouble();
						int occ=0;
						int count1=pointerToIndex.readInt();
						a=new ArrayList<Integer>();
						while(occ<count1){
							a.add(pointerToIndex.readInt());occ++;}
						map.put(keyy,a);
					}
					ArrayList<Integer> position=new ArrayList<Integer>();
					for(Double ky:map.keySet())
					{
						if(fin[1].equals("null"))
						{
							if(ky==Double.MIN_VALUE)
							{
								position=map.get(ky);
								for(int offset:position)
									sel(offset);
							}
						}
						else
						{
							if(ky==Double.parseDouble(fin[1])&&(equalTo==1))
							{
								position=map.get(ky);
								for(int offset:position)
									sel(offset);
							}
								if(ky<Double.parseDouble(fin[1])&&(lessThan==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if(ky>Double.parseDouble(fin[1])&&(GreaterThan==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if(ky<=Double.parseDouble(fin[1])&&(lessThanEqualTo==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if(ky>=Double.parseDouble(fin[1])&&(GreaterThanEqualTo==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if(ky!=Double.parseDouble(fin[1])&&(notEqualTo==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
					}}
				}
				if(Type.equals("long")||Type.equals("longint"))
				{
					Map<Long, ArrayList<Integer>> map = new HashMap<Long,ArrayList<Integer>>();
					Long key;
					while(pointerToIndex.getFilePointer()<pointerToIndex.length())
					{
						Long keyy=pointerToIndex.readLong();
						int occ=0;
						int count1=pointerToIndex.readInt();
						a=new ArrayList<Integer>();
						while(occ<count1){
							a.add(pointerToIndex.readInt());occ++;}
						map.put(keyy,a);
					}
					ArrayList<Integer> position=new ArrayList<Integer>();
					for(Long ky:map.keySet())
					{
						if(fin[1].equals("null"))
						{
							if(ky==Long.MIN_VALUE)
							{
								position=map.get(ky);
								for(int offset:position)
									sel(offset);
							}
						}
						else
						{
							if(ky==Long.parseLong(fin[1])&&(equalTo==1))
							{
								position=map.get(ky);
							for(int offset:position)
								sel(offset);
							}
							if(ky<Long.parseLong(fin[1])&&(lessThan==1))
							{
								position=map.get(ky);
								for(int offset:position)
									sel(offset);
							}
							if(ky>Long.parseLong(fin[1])&&(GreaterThan==1))
							{
								position=map.get(ky);
								for(int offset:position)
									sel(offset);
							}
							if(ky<=Long.parseLong(fin[1])&&(lessThanEqualTo==1))
							{
								position=map.get(ky);
								for(int offset:position)
									sel(offset);
							}
							if(ky>=Long.parseLong(fin[1])&&(GreaterThanEqualTo==1))
							{
								position=map.get(ky);
								for(int offset:position)
									sel(offset);
							}
							if(ky!=Long.parseLong(fin[1])&&(notEqualTo==1))
							{
								position=map.get(ky);
								for(int offset:position)
									sel(offset);
							}
					}}
				}
				if(Type.matches("varchar(.*)"))
				{
					String key="";
					//System.out.println(Value);
					Map<String, ArrayList<Integer>> map = new HashMap<String,ArrayList<Integer>>();
					while(pointerToIndex.getFilePointer()<pointerToIndex.length())
					{
						String keyy="";
						Byte strlen=pointerToIndex.readByte();
						for(int p=0;p<strlen;p++)
							keyy=keyy+(char)pointerToIndex.readByte();
						//System.out.println(keyy);
						int occ=0;
						int count1=pointerToIndex.readInt();
						a=new ArrayList<Integer>();
						while(occ<count1){
							a.add(pointerToIndex.readInt());occ++;}
						map.put(keyy,a);
					}
					fin[1]=fin[1].substring(1, fin[1].length()-1);
					ArrayList<Integer> position=new ArrayList<Integer>();
					for(String ky:map.keySet())
					{
						if(fin[1].equals("null"))
						{
							if(ky.equals("null"))
							{
								position=map.get(ky);
								for(int offset:position)
									sel(offset);
							}
						}
						else
						{
							if(ky.equals(fin[1])&&(equalTo==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if((ky.compareTo(fin[1])>=0)&&(GreaterThanEqualTo==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if((ky.compareTo(fin[1])<=0)&&(lessThanEqualTo==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if((ky.compareTo(fin[1])>0)&&(GreaterThan==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if((ky.compareTo(fin[1])<0)&&(lessThan==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
								if((!ky.equals(fin[1]))&&(notEqualTo==1))
								{
									position=map.get(ky);
									for(int offset:position)
										sel(offset);
								}
					}	}
				}
				if(Type.matches("char(.*)"))
				{
					String key="";
					//System.out.println(Value);
					Map<String, ArrayList<Integer>> map = new HashMap<String,ArrayList<Integer>>();
					while(pointerToIndex.getFilePointer()<pointerToIndex.length())
					{
						String keyy="";
						Byte strlen=pointerToIndex.readByte();
						for(int p=0;p<strlen;p++)
							keyy=keyy+(char)pointerToIndex.readByte();
						//System.out.println(keyy);
						int occ=0;
						int count1=pointerToIndex.readInt();
						a=new ArrayList<Integer>();
						while(occ<count1){
							a.add(pointerToIndex.readInt());occ++;}
						map.put(keyy,a);
					}
					fin[1]=fin[1].substring(1, fin[1].length()-1);
					String len=Type.substring(Type.indexOf('(')+1, Type.indexOf(')'));
					int lk=Integer.parseInt(len);
					char[] ap=new char[lk];
					char[] bh=fin[1].toCharArray();
					for(int i=0;i<bh.length;i++)
						ap[i]=bh[i];
					for(int i=bh.length;i<ap.length;i++)
						ap[i]='`';
					fin[1]=new String(ap);
					ArrayList<Integer> position=new ArrayList<Integer>();
					for(String ky:map.keySet())
					{
						if(fin[1].equals("null"))
						{
							if(ky.equals("null"))
							{
								position=map.get(ky);
								for(int offset:position)
									sel(offset);
							}
						}
						else
						{
						if(ky.equals(fin[1])&&(equalTo==1))
						{
							position=map.get(ky);
							for(int offset:position)
								sel(offset);
						}
						if((ky.compareTo(fin[1])>=0)&&(GreaterThanEqualTo==1))
						{
							position=map.get(ky);
							for(int offset:position)
								sel(offset);
						}
						if((ky.compareTo(fin[1])<=0)&&(lessThanEqualTo==1))
						{
							position=map.get(ky);
							for(int offset:position)
								sel(offset);
						}
						if((ky.compareTo(fin[1])>0)&&(GreaterThan==1))
						{
							position=map.get(ky);
							for(int offset:position)
								sel(offset);
						}
						if((ky.compareTo(fin[1])<0)&&(lessThan==1))
						{
							position=map.get(ky);
							for(int offset:position)
								sel(offset);
						}
						if((!ky.equals(fin[1]))&&(notEqualTo==1))
						{
							position=map.get(ky);
							for(int offset:position)
								sel(offset);
						}
					}
					}
					}
				if(Type.equals("date"))
				{
					String key="";
					Map<Long, ArrayList<Integer>> map = new HashMap<Long,ArrayList<Integer>>();
					while(pointerToIndex.getFilePointer()<pointerToIndex.length())
					{
						Long keyy=pointerToIndex.readLong();
						int occ=0;
						int count1=pointerToIndex.readInt();
						a=new ArrayList<Integer>();
						while(occ<count1){
							a.add(pointerToIndex.readInt());occ++;}
						map.put(keyy,a);
					}
					int keyFound=0;
					fin[1]=fin[1].substring(1, fin[1].length()-1);
					String[] date=fin[1].split("-");
					if(date[0].length()<4)
						date[0]=String.format("%04d",Integer.parseInt(date[0]));
					if(date[1].length()<2)
						date[1]=String.format("%02d", Integer.parseInt(date[1]));
					if(date[2].length()<2)
					{
						date[2]=String.format("%02d", Integer.parseInt(date[2]));
					}
					String dat=date[0]+date[1]+date[2];
					long date1=Long.parseLong(dat);
					ArrayList<Integer> position=new ArrayList<Integer>();
					for(Long ky:map.keySet())
					{
						if(fin[1].equals("null"))
						{
							if(ky==Long.MIN_VALUE)
							{
								position=map.get(ky);
								for(int offset:position)
									sel(offset);
							}
						}
						else
						{
						if(ky==date1&&(equalTo==1))
						{
							position=map.get(ky);
							for(int offset:position){
								sel(offset);}
						}
						if(ky<date1&&(lessThan==1))
						{
							position=map.get(ky);
							for(int offset:position)
								sel(offset);
						}
						if(ky>date1&&(GreaterThan==1))
						{
							position=map.get(ky);
							for(int offset:position)
								sel(offset);
						}
						if(ky<=date1&&(lessThanEqualTo==1))
						{
							position=map.get(ky);
							for(int offset:position)
								sel(offset);
						}
						if(ky>=date1&&(GreaterThanEqualTo==1))
						{
							position=map.get(ky);
							for(int offset:position)
								sel(offset);
						}
						if(ky!=date1&&(notEqualTo==1))
						{
							position=map.get(ky);
							for(int offset:position)
								sel(offset);
						}
						}	
					}
				}
				if(Type.equals("datetime"))
				{
					String newval=fin[1].substring(1, fin[1].length()-1);
					Map<Long, ArrayList<Integer>> map = new HashMap<Long,ArrayList<Integer>>();
					while(pointerToIndex.getFilePointer()<pointerToIndex.length())
					{
						Long keyy=pointerToIndex.readLong();
						int occ=0;
						int count1=pointerToIndex.readInt();
						a=new ArrayList<Integer>();
						while(occ<count1){
							a.add(pointerToIndex.readInt());occ++;}
						map.put(keyy,a);
					}
					String[] de=newval.split("_");
					String[] date=de[0].split("-");
					String[] time=de[1].split(":");
					if(date[0].length()<4)
						date[0]=String.format("%04d",Integer.parseInt(date[0]));
					if(date[1].length()<2)
						date[1]=String.format("%02d", Integer.parseInt(date[1]));
					if(date[2].length()<2)
					{
						date[2]=String.format("%02d", Integer.parseInt(date[2]));
					}
					if(time[0].length()<2)
						time[0]=String.format("%02d", Integer.parseInt(time[0]));
					if(time[1].length()<2)
						time[1]=String.format("%02d", Integer.parseInt(time[1]));
					if(time[2].length()<2)
						time[2]=String.format("%02d", Integer.parseInt(time[2]));
					String pp=date[0]+date[1]+date[2]+time[0]+time[1]+time[2];
					long date1=Long.parseLong(pp);
					ArrayList<Integer> position=new ArrayList<Integer>();
					int keyFound=0;
					for(Long ky : map.keySet())
					{
						if(fin[1].equals("null"))
						{
							if(ky==Long.MIN_VALUE)
							{
								position=map.get(ky);
								for(int offset:position)
									sel(offset);
							}
						}
						else
						{
							if(ky==date1&&(equalTo==1))
						
						{
							position=map.get(ky);
							for(int offset:position)
							sel(offset);
						}
						if(ky<date1&&(lessThan==1))
						{
							position=map.get(ky);
							for(int offset:position)
								sel(offset);
						}
						if(ky>date1&&(GreaterThan==1))
						{
							position=map.get(ky);
							for(int offset:position)
								sel(offset);
						}
						if(ky<=date1&&(lessThanEqualTo==1))
						{
							position=map.get(ky);
							for(int offset:position)
								sel(offset);
						}
						if(ky>=date1&&(GreaterThanEqualTo==1))
						{
							position=map.get(ky);
							for(int offset:position)
								sel(offset);
						}
						if(ky!=date1&&(notEqualTo==1))
						{
							position=map.get(ky);
							for(int offset:position)
								sel(offset);
						}
					}}
					}
					System.out.println();
					System.out.println(countset+" rows in set");
					pointerToFile.close();
					pointerToIndex.close();
					pointer.close();
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
			}
			else
				System.out.println(database+"."+table+" does not exist");
		}
		else
			System.out.println("Database not selected");
	}
	public static String getColumnType(String ColumnName) throws EOFException
	{
		String type="";
		try
		{
			RandomAccessFile pointerToFile = new RandomAccessFile("information_schema.columns.tbl", "rw");

			do
			{
				String db="";
				String tb="";
				String column_type="";
				String column_name="";
				Byte length=pointerToFile.readByte();
				for(int k=0;k<length;k++)
					db=db+(char)pointerToFile.readByte();
				length=pointerToFile.readByte();
				for(int k=0;k<length;k++)
					tb=tb+(char)pointerToFile.readByte();
				length=pointerToFile.readByte();
				for(int k=0;k<length;k++)
					column_name=column_name+(char)pointerToFile.readByte();
				pointerToFile.readInt();
				length=pointerToFile.readByte();
				for(int k=0;k<length;k++)
					column_type=column_type+(char)pointerToFile.readByte();
				String nullc="";
				length=pointerToFile.readByte();
				for(int k=0;k<length;k++)
					nullc=nullc+(char)pointerToFile.readByte();
				String pric="";
				length=pointerToFile.readByte();
				for(int k=0;k<length;k++)
					pric=pric+(char)pointerToFile.readByte();
				if((table.equals(tb))&&(database.equals(db))&&ColumnName.equals(column_name))
				{
					type=column_type;
				}
			}while(pointerToFile.getFilePointer()!=pointerToFile.length());
			pointerToFile.close();
			int fl=0;
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		return type;
	}
	public static void sel(int offset) throws EOFException
	{
			
				String filename=database+"."+table+".tbl";
				int originalColumnCount=columnCount();
				String[][] c=new String[originalColumnCount][4];//Retrieving the column name, column type and constraints of the table
				int l=0;
				try
				{
						RandomAccessFile pointerToFile = new RandomAccessFile("information_schema.columns.tbl", "rw");
						do
						{
							String db="";
							String tb="";
							String column_type="";
							String column_name="";
							Byte length=pointerToFile.readByte();
							for(int k=0;k<length;k++)
								db=db+(char)pointerToFile.readByte();
							length=pointerToFile.readByte();
							for(int k=0;k<length;k++)
								tb=tb+(char)pointerToFile.readByte();
							length=pointerToFile.readByte();
							for(int k=0;k<length;k++)
								column_name=column_name+(char)pointerToFile.readByte();
							pointerToFile.readInt();
							length=pointerToFile.readByte();
							for(int k=0;k<length;k++)
								column_type=column_type+(char)pointerToFile.readByte();
							String nullc="";
							length=pointerToFile.readByte();
							for(int k=0;k<length;k++)
								nullc=nullc+(char)pointerToFile.readByte();
							String pric="";
							length=pointerToFile.readByte();
							for(int k=0;k<length;k++)
								pric=pric+(char)pointerToFile.readByte();
							if((table.equals(tb))&&(database.equals(db)))
							{
								c[l][0]=column_name;
								c[l][1]=column_type;
								c[l][2]=nullc;
								c[l][3]=pric;
								l++;
							}
						}while(pointerToFile.getFilePointer()!=pointerToFile.length());
						pointerToFile.close();
						pointerToFile=new RandomAccessFile(filename,"rw");
						pointerToFile.seek(offset);
							for( int i=0;i<originalColumnCount;i++)
								{
								if(c[i][1].equals("byte"))
								{
									Byte as=pointerToFile.readByte();
									if(as==Byte.MIN_VALUE)
										System.out.print(" || "+null);
									else
										System.out.print(" || "+as);
								}
								if(c[i][1].equals("int"))
								{
									Integer as=pointerToFile.readInt();
									if(as==Integer.MIN_VALUE)
										System.out.print(" || "+null);
									else
										System.out.print(" || "+as);
								}
								if(c[i][1].equals("float"))
								{
									Float as=pointerToFile.readFloat();
									if(as==Float.MIN_VALUE)
										System.out.print(" || "+null);
									else
										System.out.print(" || "+as);
								}
								if(c[i][1].equals("double"))
								{
									Double as=pointerToFile.readDouble();
									if(as==Double.MIN_VALUE)
										System.out.print(" || "+null);
									else
										System.out.print(" || "+as);
								}
								if(c[i][1].equals("short")||c[i][1].equals("shortint"))
									{
										Short as=pointerToFile.readShort();
										if(as==Short.MIN_VALUE)
											System.out.print(" || "+null);
										else
											System.out.print(" || "+as);
									}
								if(c[i][1].equals("long")||c[i][1].equals("longint"))
								{
									Long as=pointerToFile.readLong();
									if(as==Long.MIN_VALUE)
										System.out.print(" || "+null);
									else
										System.out.print(" || "+as);
								}
								if(c[i][1].matches("varchar(.*)"))
								{
									int len=(int)(pointerToFile.readByte());
									String val="";
									for(int p=0;p<len;p++)
									{
										val=val+(char)pointerToFile.readByte();
									}
									System.out.print(" || "+val);	
								}
								if(c[i][1].matches("char(.*)"))
								{
									int len=(int)(pointerToFile.readByte());
									String val="";
									for(int p=0;p<len;p++)
									{
										val=val+(char)pointerToFile.readByte();
									}
									String v=(String) val.subSequence(0,val.indexOf('`'));
									System.out.print(" || "+v);	
								}
								if(c[i][1].equals("date"))
								{
									Long dat1=pointerToFile.readLong();
									if(dat1!=Long.MIN_VALUE)
									{
										String a=dat1.toString();
										a=String.format("%08d", Integer.parseInt(a));
										String date1="";
										String date2="";
										String date3="";
										for(int y=0;y<4;y++)
											date1=date1+a.charAt(y);
										for(int y=4;y<6;y++)
											date2=date2+a.charAt(y);
										for(int y=6;y<8;y++)
											date3=date3+a.charAt(y);
										String formattedDate=date1+"-"+date2+"-"+date3;
										System.out.print(" || "+formattedDate);
									}
									else
									{
										System.out.println(" || null");
									}
								}
								if(c[i][1].equals("datetime"))
								{
									Long dat1=pointerToFile.readLong();
									if(dat1!=Long.MIN_VALUE)
									{
										String a=dat1.toString();
										a=String.format("%014d", Long.parseLong(a));
										String date1="";
										String date2="";
										String date3="",date4="",date5="",date6="";
										for(int y=0;y<4;y++)
											date1=date1+a.charAt(y);
										for(int y=4;y<6;y++)
											date2=date2+a.charAt(y);
										for(int y=6;y<8;y++)
											date3=date3+a.charAt(y);
										for(int y=8;y<10;y++)
											date4=date4+a.charAt(y);
										for(int y=10;y<12;y++)
											date5=date5+a.charAt(y);
										for(int y=12;y<14;y++)
											date6=date6+a.charAt(y);
										String formattedDate=date1+"-"+date2+"-"+date3+"_"+date4+":"+date5+":"+date6;
										System.out.print(" || "+formattedDate);
									}
									else
										System.out.println(" || null");
								}
							}
							countset+=1;
							System.out.println();
							pointerToFile.close();
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	}
}