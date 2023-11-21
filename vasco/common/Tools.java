/* $Id: Tools.java,v 1.3 2007/10/28 15:38:14 jagan Exp $ */
package vasco.common;

import java.util.*;
import javax.swing.JApplet; // import java.applet.Applet;
import javax.swing.*; // import java.awt.*;
import java.net.*;
import java.io.*;

/**
 * This class provides utility methods for various operations.
 */
public class Tools {

	static String serverName = "donar.umiacs.umd.edu";
	static String localServer = "localhost";
	static int portNumber = 1602;

	static public Applet currentApplet = null; // keeps track of the current running applet to easily switch between them

    /**
     * Displays an error message using a message box.
     *
     * @param msg The error message to be displayed.
     */
	public static void errorMessage(String msg) {
		MessageBox mb = new MessageBox(msg);
		// mb.show();
	}

    /**
     * Formats a string to break lines at a specified width.
     *
     * @param s     The input string to be formatted.
     * @param width The maximum width for each line.
     * @return The formatted string.
     */
	public static String formatHelp(String s, int width) {
		int i;
		int index = 0;
		while (s.length() - index > width) {
			for (i = Math.min(index + width, s.length() - 1); i > index; i--) {
				if (s.charAt(i) == ' ') {
					s = s.substring(0, i) + new String("\n") + s.substring(i + 1);
					index = i + 1;
					break;
				}
			}
			if (index == i) {
				index += Math.min(index + width, s.length() - index);
			}
		}
		return s;
	}

    /**
     * Draws coordinates on the specified graphics object.
     *
     * @param g The Graphics object on which coordinates are to be drawn.
     */
	public static void drawCoordinates(Graphics g) {
		g.setColor(Color.black);
		String s1 = "[512, 512]";
		String s2 = "[512, 0]";
		FontMetrics fm = g.getFontMetrics(g.getFont());
		int h = fm.getHeight();
		int w1 = fm.stringWidth(s1);
		int w2 = fm.stringWidth(s2);
		g.drawString("[0,0]", 0, h);
		g.drawString(s1, 512 - w1, 512);
		g.drawString(s2, 512 - w2, h);
		g.drawString("[0, 512]", 0, 512);
	}

	/**
	 * Retrieves the content of a file from the server.
	 *
	 * @param datatype The datatype of the data.
	 * @param filename The name of the file to retrieve.
	 * @return An array of strings representing the content of the file, or an empty array if the file is not found.
	 */
	public static String[] getFile(String datatype, String filename) {
		BufferedReader is;
		PrintWriter os;
		Socket socket;
		String[] res;
		String str;

		// Added for SIGMOD 2010
		return new String[0];
		/*
		 * if (filename == null || filename.compareTo("") == 0) return new String[0];
		 * 
		 * Vector v = new Vector(); try { try { socket = new Socket(serverName,
		 * portNumber); } catch(Exception e) { socket = new Socket(localServer,
		 * portNumber); } is = new BufferedReader(new
		 * InputStreamReader(socket.getInputStream())); os = new
		 * PrintWriter(socket.getOutputStream()); os.println("LOAD");
		 * os.println(datatype); os.println(filename); os.flush(); while ((str =
		 * is.readLine()).compareTo(".") != 0) { v.addElement(str); } res = new
		 * String[v.size()]; v.copyInto(res); socket.close(); } catch (Exception e) {
		 * errorMessage("File not found or no response from server"); return new
		 * String[0]; } return res;
		 */
	}

	/**
	 * Deletes a file on the server.
	 *
	 * @param datatype The datatype of the data.
	 * @param filename The name of the file to delete.
	 */
	public static void deleteFile(String datatype, String filename) {

		return;

		/*
		 * BufferedReader is; PrintWriter os; Socket socket; String[] res; String str;
		 * if (filename == null || filename.compareTo("") == 0) return; try { try {
		 * socket = new Socket(serverName, portNumber); } catch(Exception e) { socket =
		 * new Socket(localServer, portNumber); } is = new BufferedReader(new
		 * InputStreamReader(socket.getInputStream())); os = new
		 * PrintWriter(socket.getOutputStream()); os.println("DELE");
		 * os.println(datatype); os.println(filename); os.flush(); String retStr =
		 * is.readLine(); if (!retStr.equals("OK")) errorMessage(retStr);
		 * socket.close(); } catch (Exception e) {
		 * errorMessage("File not found, not owner or no response from server");
		 * System.err.println("deleteFile exception: " + e.getMessage()); }o
		 */
	}

	/**
	 * Retrieves the list of files in a directory from the server.
	 *
	 * @param datatype The datatype of the data.
	 * @return An array of strings representing the list of files in the directory, or an empty array if no response is received.
	 */
	public static String[] getDir(String datatype) {
		BufferedReader is;
		PrintWriter os;
		Socket socket;
		String str;
		String[] res;
		Vector v = new Vector();
		return new String[0];
		/*
		 * try { try { socket = new Socket(serverName, portNumber); } catch(Exception e)
		 * { socket = new Socket(localServer, portNumber); } is = new BufferedReader(new
		 * InputStreamReader(socket.getInputStream())); os = new
		 * PrintWriter(socket.getOutputStream()); os.println("DIR");
		 * os.println(datatype); os.flush(); while ((str = is.readLine()).compareTo(".")
		 * != 0) { v.addElement(str); } res = new String[v.size()]; v.copyInto(res);
		 * socket.close(); } catch (Exception e) {
		 * System.err.println("getDir exception: " + e.getMessage());
		 * errorMessage("No response from server"); return new String[0]; } return res;
		 */
	}

    /**
     * Puts data into a file on the server.
     *
     * @param datatype The datatype of the data.
     * @param filename The name of the file.
     * @param data     The data to be written to the file.
     */
	public static void putFile(String datatype, String filename, String[] data) {
		writeFile(datatype, filename, data, "SAVE");
	}

	/**
	 * Appends data to an existing file on the server.
	 *
	 * @param datatype The datatype of the data.
	 * @param filename The name of the file to which data will be appended.
	 * @param data     The data to be appended to the file.
	 */
	public static void appendFile(String datatype, String filename, String[] data) {
		writeFile(datatype, filename, data, "APPEND");
	}

	/**
	 * Writes data to a file on the server using the specified operation.
	 *
	 * @param datatype The datatype of the data.
	 * @param filename The name of the file to which data will be written.
	 * @param data     The data to be written to the file.
	 * @param op       The operation to perform when writing the file.
	 */
	public static void writeFile(String datatype, String filename, String[] data, String op) {
		writeFile(datatype, filename, data, op, true);
	}

	/**
	 * Writes data to a file on the server using the specified operation and optionally displays a message.
	 *
	 * @param datatype The datatype of the data.
	 * @param filename The name of the file to which data will be written.
	 * @param data     The data to be written to the file.
	 * @param op       The operation to perform when writing the file.
	 * @param showmsg  Whether to display a message upon completion.
	 */
	public static void writeFile(String datatype, String filename, String[] data, String op, boolean showmsg) {
		BufferedReader is;
		PrintWriter os;
		Socket socket;
		String[] res;
		String str;
		return;
		/*
		 * if (filename == null || filename.compareTo("") == 0) return; try { try {
		 * socket = new Socket(serverName, portNumber); } catch(Exception e) { socket =
		 * new Socket(localServer, portNumber); } is = new BufferedReader(new
		 * InputStreamReader(socket.getInputStream())); os = new
		 * PrintWriter(socket.getOutputStream()); os.println(op); os.println(datatype);
		 * os.println(filename); for (int i = 0; i < data.length; i++)
		 * os.println(data[i]); os.println("."); os.flush(); String response =
		 * is.readLine(); if(showmsg)errorMessage(response); socket.close(); } catch
		 * (Exception e) { errorMessage("Error saving the data file");
		 * System.err.println("putFile exception: " + e.getMessage()); }
		 */
	}
}
