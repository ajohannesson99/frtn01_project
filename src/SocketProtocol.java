

import java.io.*;
import java.net.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.geom.*;
import java.awt.print.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;


public class SocketProtocol {

    public static String getTag(String message) {
	StringTokenizer st = new StringTokenizer(message, "|");
	return st.nextToken();
    }

    public static String getValue(String message) {
	StringTokenizer st = new StringTokenizer(message,"|");
	String dummy = st.nextToken();
	return st.nextToken();
    }

    public static String create(String tag,String value) {
	return tag + "|" + value;
    }
}


